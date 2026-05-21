package br.com.fiap.aguiaexpress.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "t_badges")
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_badge")
    private Long id;

    @Column(name = "nm_badge", nullable = false, unique = true, length = 80)
    private String nomeBadge;

    @Column(name = "ds_descricao", length = 255)
    private String descricao;

    @Column(name = "ds_criterio", length = 255)
    private String criterio;

    @Column(name = "ds_icone", length = 100)
    private String icone;

    @Column(name = "st_status")
    private Integer status;
}