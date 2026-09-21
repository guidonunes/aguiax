package br.com.fiap.aguiax.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "t_categorias")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Long id;

    @Column(name = "nm_categoria", nullable = false, unique = true, length = 80)
    private String nomeCategoria;

    @Column(name = "ds_descricao", length = 255)
    private String descricao;
}