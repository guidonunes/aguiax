package br.com.fiap.aguiaexpress.controller;

import br.com.fiap.aguiaexpress.config.security.TokenService;
import br.com.fiap.aguiaexpress.dto.LoginDTO;
import br.com.fiap.aguiaexpress.dto.TokenDTO;
import br.com.fiap.aguiaexpress.model.Usuario;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<TokenDTO> login(
            @RequestBody @Valid LoginDTO loginDTO
    ) {
        UsernamePasswordAuthenticationToken usernamePassword =
                new UsernamePasswordAuthenticationToken(
                        loginDTO.email(),
                        loginDTO.senha()
                );

        Authentication authentication =
                authenticationManager.authenticate(usernamePassword);

        Usuario usuario = (Usuario) authentication.getPrincipal();

        String token = tokenService.gerarToken(usuario);

        TokenDTO tokenDTO = new TokenDTO(
                token,
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getTipoPerfil().getNomePerfil().name()
        );

        return ResponseEntity.ok(tokenDTO);
    }
}