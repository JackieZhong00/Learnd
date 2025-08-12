package com.learnd.learnd_main.Learnd.security;

import com.learnd.learnd_main.Learnd.service.CustomOAuth2Service;
import com.learnd.learnd_main.Learnd.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final CustomUserDetailsService userDetailsService;
    private final JwtFilter jwtFilter;
    private final CustomOAuth2Service customOAuth2Service;

    public SecurityConfig(CustomUserDetailsService userDetailsService, JwtFilter jwtFilter, CustomOAuth2Service customOAuth2Service) {
        this.jwtFilter = jwtFilter;
        this.userDetailsService = userDetailsService;
        this.customOAuth2Service = customOAuth2Service;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                //AbstractHttpConfigurer::disable is just a method reference that allows you to shortcut
                //having to type out entire lambda expression of customizer -> customizer.disable()
                .csrf(AbstractHttpConfigurer::disable) //this disables csrf setting
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/api/user/login", "/api/user/register")
                        .permitAll()
                        .anyRequest().authenticated())
//                .httpBasic(Customizer.withDefaults())
//                .oauth2Login(Customizer.withDefaults())
//                .logout(Customizer.withDefaults())
                //why did i have to disable this for the oauth login to work
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }


    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setPasswordEncoder(new BCryptPasswordEncoder(10));
        authProvider.setUserDetailsService(userDetailsService);
        return authProvider;
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
        return config.getAuthenticationManager();
    }


}
