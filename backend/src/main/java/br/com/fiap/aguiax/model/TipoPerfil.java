package br.com.fiap.aguiax.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="t_tipos_perfis")
public class TipoPerfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_perfil")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "nm_perfil", nullable = false, unique = true, length = 50)
    private NomePerfil nomePerfil;

    @Column(name = "ds_descricao", length = 255)
    private String descricaoPerfil;
}
