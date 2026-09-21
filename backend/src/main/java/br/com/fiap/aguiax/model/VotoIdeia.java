package br.com.fiap.aguiax.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "t_votos_ideia")
public class VotoIdeia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_voto")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_ideia", nullable = false)
    private Ideia ideia;

    @Column(name = "nr_valor")
    private Integer valor;

    @Column(name = "dt_voto")
    private LocalDateTime dataVoto;
}