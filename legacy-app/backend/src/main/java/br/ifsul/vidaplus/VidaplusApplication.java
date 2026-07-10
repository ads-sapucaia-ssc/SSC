package br.ifsul.vidaplus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada do VidaPlus (legado).
 * Sistema de gestao de clinica usado como base da disciplina SSC.
 * Contem vulnerabilidades plantadas de proposito (ver docs/07).
 */
@SpringBootApplication
public class VidaplusApplication {
    public static void main(String[] args) {
        SpringApplication.run(VidaplusApplication.class, args);
    }
}
