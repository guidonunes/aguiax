package br.com.fiap.aguiax.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "t_projetos")
public class Projeto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_projeto")
    private Long id;

    @OneToOne
    @JoinColumn(name = "id_ideia", nullable = false, unique = true)
    private Ideia ideia;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_status_projeto", nullable = false)
    private StatusProjeto statusProjeto;

    @Column(name = "ds_titulo", nullable = false, length = 150)
    private String titulo;

    @Column(name = "ds_descricao", length = 1000)
    private String descricao;

    @Column(name = "ds_objetivo", length = 1000)
    private String objetivo;

    @Column(name = "vl_investimento", precision = 12, scale = 2)
    private BigDecimal investimento;

    @Column(name = "vl_roi", precision = 10, scale = 2)
    private BigDecimal roi;

    @Column(name = "vl_retorno_financeiro", precision = 12, scale = 2)
    private BigDecimal retornoFinanceiro;

    @Column(name = "vl_reducao_custos", precision = 12, scale = 2)
    private BigDecimal reducaoCustos;

    @Column(name = "vl_ganho_produtividade", precision = 10, scale = 2)
    private BigDecimal ganhoProdutividade;

    @Column(name = "dt_inicio")
    private LocalDate dataInicio;

    @Column(name = "dt_previsao_fim")
    private LocalDate dataPrevisaoFim;

    @Column(name = "dt_conclusao")
    private LocalDate dataConclusao;

    @Column(name = "dt_criacao")
    private LocalDateTime dataCriacao;

    @Column(name = "dt_data_atualizacao")
    private LocalDateTime dataAtualizacao;
}