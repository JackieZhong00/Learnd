package com.learnd.learnd_main.Learnd.security;

import com.learnd.learnd_main.Learnd.model.User;
import com.learnd.learnd_main.Learnd.repo.UserRepository;
import com.learnd.learnd_main.Learnd.service.JWTService;
import com.learnd.learnd_main.Learnd.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.Cookie;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JWTService jwtService;
    private final UserRepository userRepository;
    private final UserService userService;

    public OAuth2AuthenticationSuccessHandler(JWTService jwtService, UserRepository userRepository, UserService userService) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public Cookie createCookieFromToken(String token, String cookieName, int maxAge) {
        Cookie tokenCookie = new Cookie(cookieName, token);
        tokenCookie.setPath("/");
        tokenCookie.setHttpOnly(true);
        tokenCookie.setMaxAge(maxAge);
        return tokenCookie;
    }
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        System.out.println("OAuth2 success handler triggered for user: " + authentication.getName());
        //Extract user info from OAuth2AuthenticationToken
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String email = oauthToken.getPrincipal().getAttribute("email"); // Google returns email claim
        if(email == null || email.isEmpty()) {
            throw new BadCredentialsException("Invalid email address fetched from google");
        }


        //Generate JWT access + refresh tokens
        String accessToken = jwtService.generateAccessToken(email);
        String refreshToken = jwtService.generateRefreshToken(email);

        // Return JWTs in response
        Cookie accessTokenCookie = createCookieFromToken(accessToken, "accessToken",1800);
        Cookie refreshTokenCookie = createCookieFromToken(refreshToken, "refreshToken",60*60*24*30);
        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        // Immediately discard the session
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
            // Redirect explicitly to the user's deck home
            String username = email.split("@")[0]; // or fetchedUser.get().getUsername()
            response.sendRedirect("http://localhost:5173/" + username + "/deck_home");
        }
    }
}

