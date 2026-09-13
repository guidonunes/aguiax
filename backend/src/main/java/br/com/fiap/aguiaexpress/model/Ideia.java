package br.com.fiap.aguiaexpress.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "t_ideias")
public class Ideia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ideia")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "id_status_ideia", nullable = false)
    private StatusIdeia statusIdeia;

    @ManyToOne
    @JoinColumn(name = "id_orientacao", nullable = false)
    private OrientacaoEstrategica orientacaoEstrategica;

    @Column(name = "ds_titulo", nullable = false, length = 150)
    private String titulo;

    @Column(name = "ds_descricao", nullable = false, length = 1000)
    private String descricao;

    @Column(name = "ds_problema", length = 1000)
    private String problema;

    @Column(name = "ds_solucao_proposta", length = 1000)
    private String solucaoProposta;

    @Column(name = "nr_impacto")
    private Integer impacto;

    @Column(name = "nr_viabilidade")
    private Integer viabilidade;

    @Column(name = "nr_prioridade", precision = 4, scale = 2, nullable = false)
    private BigDecimal prioridade;

    @Column(name = "nr_total_votos")
    private Integer totalVotos;

    @Column(name = "dt_criacao")
    private LocalDateTime dataCriacao;

    @Column(name = "dt_data_atualizacao")
    private LocalDateTime dataAtualizacao;
}