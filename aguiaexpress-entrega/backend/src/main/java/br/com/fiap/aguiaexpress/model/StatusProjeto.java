package br.com.fiap.aguiaexpress.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "t_status_projeto")
public class StatusProjeto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_status_projeto")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "nm_status", nullable = false, unique = true, length = 50)
    private NomeStatusProjeto status;

    @Column(name = "ds_descricao", length = 255)
    private String descricao;
}