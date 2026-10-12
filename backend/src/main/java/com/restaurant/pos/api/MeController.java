package com.restaurant.pos.api;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class MeController
{
    @GetMapping("/me")
    Map<String, Object> me(@AuthenticationPrincipal Jwt jasonWebToken)
    {
        return Map.of(
                "id", jasonWebToken.getSubject(),
                "username", jasonWebToken.getClaimAsString("preferred_username"),
                "name", jasonWebToken.getClaimAsString("name"),
                "roles", jasonWebToken.getClaimAsMap("realm_access"));
    }
}
