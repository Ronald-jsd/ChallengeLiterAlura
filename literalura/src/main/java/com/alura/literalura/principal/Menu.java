package com.alura.literalura.principal;

import com.alura.literalura.dto.AutorDTO;
import com.alura.literalura.dto.LibroDTO;
import com.alura.literalura.dto.RespuestaLibrosDTO;
import com.alura.literalura.model.Autor;
import com.alura.literalura.model.Libro;
import com.alura.literalura.service.AutorService;
import com.alura.literalura.service.ConsumoAPI;
import com.alura.literalura.service.ConvierteDatos;
import com.alura.literalura.service.LibroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;


@Component
public class Menu {
    @Autowired
    private LibroService libroService;

    @Autowired
    private AutorService autorService;

    @Autowired
    private ConsumoAPI consumoAPI;

    @Autowired
    private ConvierteDatos convierteDatos;

    private static final String BASE_URL = "https://gutendex.com/books/";
    private final Scanner scanner = new Scanner(System.in);

    public void mostrarMenu() {
        int opcion;

        do {
            imprimirMenu();
            opcion = leerOpcion();

            switch (opcion) {
                case 1 -> buscarLibroPorTitulo();
                case 2 -> listarLibrosRegistrados();
                case 3 -> listarAutoresRegistrados();
                case 4 -> listarAutoresVivosPorAno();
                case 5 -> listarLibrosPorIdioma();
                case 0 -> System.out.println("Saliendo...");
                default -> System.out.println("Opción no válida. Intente de nuevo.");
            }
        } while (opcion != 0);

        scanner.close();
    }

    private void imprimirMenu() {
        System.out.println("--- LITERALURA ---");
        System.out.println("1 - Buscar libro por título");
        System.out.println("2 - Listar libros registrados");
        System.out.println("3 - Listar autores registrados");
        System.out.println("4 - Listar autores vivos en un año");
        System.out.println("5 - Listar libros por idioma");
        System.out.println("0 - Salir");
        System.out.print("Seleccione una opción: ");
    }

    private int leerOpcion() {
        while (!scanner.hasNextInt()) {
            System.out.println("Por favor, ingrese un número válido.");
            scanner.next();
        }
        return scanner.nextInt();
    }

    private void buscarLibroPorTitulo() {
        System.out.print("Ingrese el título del libro: ");
        scanner.nextLine(); // Consumir salto de línea
        String titulo = scanner.nextLine();

        try {
            String json = consumoAPI.obtenerDatos(BASE_URL + "?search=" + URLEncoder.encode(titulo, StandardCharsets.UTF_8));
            RespuestaLibrosDTO respuesta = convierteDatos.obtenerDatos(json, RespuestaLibrosDTO.class);

            if (respuesta.getLibros().isEmpty()) {
                System.out.println("Libro no encontrado en la API.");
                return;
            }

            manejarLibrosEncontrados(respuesta.getLibros(), titulo);
        } catch (Exception e) {
            System.out.println("Error al obtener datos de la API: " + e.getMessage());
        }
    }

    private void manejarLibrosEncontrados(List<LibroDTO> libros, String tituloBuscado) {
        boolean libroRegistrado = false;

        for (LibroDTO libroDTO : libros) {
            if (libroDTO.getTitulo().equalsIgnoreCase(tituloBuscado)) {
                Optional<Libro> libroExistente = libroService.obtenerLibroPorTitulo(tituloBuscado);

                if (libroExistente.isPresent()) {
                    System.out.println("El libro ya está registrado: " + tituloBuscado);
                } else {
                    registrarLibro(libroDTO);
                }
                libroRegistrado = true;
                break;
            }
        }

        if (!libroRegistrado) {
            System.out.println("No se encontró un libro exactamente con el título '" + tituloBuscado + "'.");
        }
    }

    private void registrarLibro(LibroDTO libroDTO) {
        Libro libro = new Libro();
        libro.setTitulo(libroDTO.getTitulo());
        libro.setIdioma(libroDTO.getIdiomas().get(0));
        libro.setNumeroDescargas(libroDTO.getNumeroDescargas());

        AutorDTO autorDTO = libroDTO.getAutores().get(0);
        Autor autor = autorService.obtenerAutorPorNombre(autorDTO.getNombre())
                .orElseGet(() -> autorService.crearAutor(new Autor(
                        autorDTO.getNombre(),
                        autorDTO.getAnioNacimiento(),
                        autorDTO.getAnioFallecimiento()
                )));

        libro.setAutor(autor);
        libroService.crearLibro(libro);

        System.out.println("Libro registrado: " + libro.getTitulo());
        mostrarDetallesLibro(libroDTO);
    }

    private void listarLibrosRegistrados() {
        libroService.listarLibros().forEach(libro -> {
            System.out.println("------LIBRO--------");
            System.out.println("Título: " + libro.getTitulo());
            System.out.println("Autor: " + (libro.getAutor() != null ? libro.getAutor().getNombre() : "Desconocido"));
            System.out.println("Idioma: " + libro.getIdioma());
            System.out.println("Número de descargas: " + libro.getNumeroDescargas());
        });
    }

    private void listarAutoresRegistrados() {
        autorService.listarAutores().forEach(autor -> {
            System.out.println("-------AUTOR-------");
            System.out.println("Nombre: " + autor.getNombre());
            System.out.println("Fecha de nacimiento: " + autor.getAnioNacimiento());
            System.out.println("Fecha de fallecimiento: " +
                    (autor.getAnioFallecimiento() != null ? autor.getAnioFallecimiento() : "Desconocido"));
        });
    }

    private void listarAutoresVivosPorAno() {
        System.out.print("Ingrese el año: ");
        int ano = scanner.nextInt();
        scanner.nextLine(); // Consumir salto de línea

        List<Autor> autoresVivos = autorService.listarAutoresVivosEnAno(ano);
        if (autoresVivos.isEmpty()) {
            System.out.println("No se encontraron autores vivos en el año " + ano);
        } else {
            autoresVivos.forEach(autor -> System.out.println("Autor vivo: " + autor.getNombre()));
        }
    }

    private void listarLibrosPorIdioma() {
        System.out.print("Ingrese el idioma (es, en, fr, pt): ");
        String idioma = scanner.nextLine();

        libroService.listarLibrosPorIdioma(idioma).forEach(libro -> {
            System.out.println("------LIBRO--------");
            System.out.println("Título: " + libro.getTitulo());
        });
    }

    private void mostrarDetallesLibro(LibroDTO libroDTO) {
        System.out.println("------LIBRO--------");
        System.out.println("Título: " + libroDTO.getTitulo());
        System.out.println("Autor: " + (libroDTO.getAutores().isEmpty() ? "Desconocido" : libroDTO.getAutores().get(0).getNombre()));
        System.out.println("Idioma: " + libroDTO.getIdiomas().get(0));
        System.out.println("Número de descargas: " + libroDTO.getNumeroDescargas());
    }
}
