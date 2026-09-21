package br.com.fiap.aguiax.controller;

import br.com.fiap.aguiax.audit.model.AuditEventType;
import br.com.fiap.aguiax.audit.model.AuditResourceType;
import br.com.fiap.aguiax.audit.service.AuditService;
import br.com.fiap.aguiax.config.security.TokenService;
import br.com.fiap.aguiax.dto.perfil.LoginDTO;
import br.com.fiap.aguiax.dto.perfil.TokenDTO;
import br.com.fiap.aguiax.model.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Login e emissão de JWT")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final AuditService auditService;

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(
            summary = "Autenticar usuário",
            description = "Valida email e senha e retorna um JWT com a identidade e o perfil do usuário."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticação realizada", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Email ou senha ausente ou email inválido"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    })
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

        auditService.registrarEvento(
                AuditEventType.USER_LOGIN,
                usuario.getId(),
                AuditResourceType.USER,
                usuario.getId().toString(),
                Map.of()
        );

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
