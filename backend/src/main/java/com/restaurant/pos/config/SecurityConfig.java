package com.restaurant.pos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Configuration
@EnableMethodSecurity
public class SecurityConfig
{
    @Bean
    SecurityFilterChain security(HttpSecurity httpSecurity) throws Exception
    {
        return httpSecurity
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/*.js",
                                "/*.css",
                                "/assets/**",
                                "/actuator/health")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(converter())))
                .build();
    }

    private Converter<Jwt, JwtAuthenticationToken> converter()
    {
        return jwt -> {
            var roles = new ArrayList<SimpleGrantedAuthority>();
            Object realm = jwt.getClaim("realm_access");

            if (realm instanceof Map<?, ?> map
                    && map.get("roles") instanceof Collection<?> roleNames)
            {
                roleNames.forEach(role -> roles.add(
                        new SimpleGrantedAuthority("ROLE_" + role)));
            }

            return new JwtAuthenticationToken(
                    jwt,
                    roles,
                    jwt.getClaimAsString("preferred_username"));
        };
    }
}
