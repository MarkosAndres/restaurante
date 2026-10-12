package com.restaurant.pos.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController
{
    private Keycloak client()
    {
        return KeycloakBuilder.builder()
                .serverUrl(System.getenv().getOrDefault("KEYCLOAK_URL", "http://localhost:8081"))
                .realm("master")
                .clientId("admin-cli")
                .username(System.getenv().getOrDefault("KEYCLOAK_ADMIN", "admin"))
                .password(System.getenv().getOrDefault("KEYCLOAK_ADMIN_PASSWORD", "admin_dev_password"))
                .build();
    }

    @GetMapping
    public List<UserRepresentation> all()
    {
        try (var keycloak = client())
        {
            return keycloak.realm("restaurant-pos").users().list();
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody UserRequest request)
    {
        try (var keycloak = client())
        {
            var user = new UserRepresentation();
            user.setUsername(request.username());
            user.setFirstName(request.firstName());
            user.setLastName(request.lastName());
            user.setEmail(request.email());
            user.setEnabled(true);

            var response = keycloak.realm("restaurant-pos").users().create(user);
            if (response.getStatus() != 201)
            {
                return ResponseEntity.status(response.getStatus()).build();
            }

            String id = response.getLocation().getPath().replaceAll(".*/", "");
            var credential = new CredentialRepresentation();
            credential.setType("password");
            credential.setValue(request.password());
            credential.setTemporary(false);
            keycloak.realm("restaurant-pos").users().get(id).resetPassword(credential);

            var role = keycloak.realm("restaurant-pos").roles().get(request.role()).toRepresentation();
            keycloak.realm("restaurant-pos").users().get(id).roles().realmLevel().add(List.of(role));
            return ResponseEntity.status(201).body(Map.of("id", id));
        }
    }
}
