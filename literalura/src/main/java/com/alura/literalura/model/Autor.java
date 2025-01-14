package com.alura.literalura.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "autores")
public class Autor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private int anioNacimiento;

    private Integer anioFallecimiento;

    @OneToMany(mappedBy = "autor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Libro> libros;

    public Autor() {}

    public Autor(String nombre, int anioNacimiento, int anioFallecimiento) {
        this.nombre = nombre;
        this.anioNacimiento = anioNacimiento;
        this.anioFallecimiento = anioFallecimiento;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getAnioNacimiento() {
        return anioNacimiento;
    }

    public void setAnioNacimiento(int anoNacimiento) {
        this.anioNacimiento = anoNacimiento;
    }

    public Integer getAnioFallecimiento() {
        return anioFallecimiento;
    }
    public void setAnioFallecimiento(Integer anoFallecimiento) {
        this.anioFallecimiento = anoFallecimiento;
    }

    public List<Libro> getLibros() {
        return libros;
    }

    public void setLibros(List<Libro> libros) {
        this.libros = libros;
    }

    @Override
    public String toString() {
        return  "*Autor: " + nombre + "\n" +
                "*Fecha de Nacimiento: " + anioNacimiento + "\n" +
                "*Fecha de Fallecimiento: " + (anioFallecimiento != null ? anioFallecimiento : "Desconocido(N/A)") + "\n" +
                "*Libros: " + (libros != null ? libros.size() : "Ningún libro registrado");
    }
}