package br.com.fiap.aguiaexpress.model;

public enum NomePerfil {

    OPERACIONAL("ROLE_OPERACIONAL"),
    GESTOR("ROLE_GESTOR"),
    LIDER("ROLE_LIDER");

    private final String role;

    NomePerfil(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}