package com.tiendaTech.tienda.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(name = "constante")
public class Constante implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_constante")
    private Integer idConstante;

    @NotBlank(message = "El atributo es obligatorio")
    @Size(max = 25, message = "El atributo no puede superar 25 caracteres")
    @Column(
            name = "atributo",
            unique = true,
            nullable = false,
            length = 25
    )
    private String atributo;

    @NotBlank(message = "El valor es obligatorio")
    @Size(max = 150, message = "El valor no puede superar 150 caracteres")
    @Column(
            name = "valor",
            nullable = false,
            length = 150
    )
    private String valor;

    @Column(
            name = "fecha_creacion",
            nullable = false,
            updatable = false
    )
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @PrePersist
    public void antesDeGuardar() {
        fechaCreacion = LocalDateTime.now();
        fechaModificacion = LocalDateTime.now();
    }

    @PreUpdate
    public void antesDeModificar() {
        fechaModificacion = LocalDateTime.now();
    }
}