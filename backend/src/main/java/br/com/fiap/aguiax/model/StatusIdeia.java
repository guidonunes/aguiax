package br.com.fiap.aguiax.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "t_status_ideia")
public class StatusIdeia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_status_ideia")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "nm_status", nullable = false, unique = true, length = 50)
    private NomeStatusIdeia status;

    @Column(name = "ds_descricao", length = 255)
    private String descricao;
}