package br.com.fiap.aguiaexpress.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_perfil", nullable = false)
    private TipoPerfil tipoPerfil;

    @Column(name = "nm_nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "ds_email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "ds_senha", nullable = false, length = 255)
    private String senha;

    @Column(name = "ds_cargo", length = 100)
    private String cargo;

    @Column(name = "ds_departamento", length = 100)
    private String departamento;

    @Column(name = "nr_pontos")
    private Integer pontos;

    @Column(name = "st_status")
    private Integer status;

    @Column(name = "dt_criacao")
    private LocalDateTime dataCriacao;

    @Column(name = "dt_data_atualizacao")
    private LocalDateTime dataAtualizacao;
}