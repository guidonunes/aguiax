package br.com.fiap.aguiax.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "t_usuario_badges",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_usuario_badges_usuario_badge",
                        columnNames = {"id_usuario", "id_badge"}
                )
        }
)
public class UsuarioBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario_badge")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_badge", nullable = false)
    private Badge badge;

    @Column(name = "dt_conquista")
    private LocalDateTime dataConquista;
}