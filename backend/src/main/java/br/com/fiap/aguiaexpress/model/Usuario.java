package br.com.fiap.aguiaexpress.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_usuarios")
public class Usuario implements UserDetails {

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

    @Column(name = "ds_senha_hash", nullable = false, length = 255)
    private String senhaHash;

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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority(tipoPerfil.getNomePerfil().getRole())
        );
    }

    @Override
    public String getPassword() {
        return senhaHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == null || status == 1;
    }
}