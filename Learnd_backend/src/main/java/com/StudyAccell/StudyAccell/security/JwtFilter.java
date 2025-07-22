package com.StudyAccell.StudyAccell.security;

import com.StudyAccell.StudyAccell.model.User;
import com.StudyAccell.StudyAccell.repo.UserRepository;
import com.StudyAccell.StudyAccell.service.CustomUserDetailsService;
import com.StudyAccell.StudyAccell.service.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final ApplicationContext context;
    private final UserRepository userRepository;

    public JwtFilter(JWTService jwtService, ApplicationContext context, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.context = context;
        this.userRepository = userRepository;
    }

    private void setAuthenticationState(String accessToken, HttpServletRequest request) {
        String email = jwtService.extractEmail(accessToken);
        UserDetails userDetails = context.getBean(CustomUserDetailsService.class).loadUserByUsername(email);

        UsernamePasswordAuthenticationToken authToken
                = new UsernamePasswordAuthenticationToken(userDetails,null, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String accessToken = null;
        String refreshToken = null;
        String email = "";
        String path = request.getRequestURI();

        if (path.equals("/api/user/login") || path.equals("/api/user/register")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (request.getCookies() != null){
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("accessToken")) {
                    accessToken = cookie.getValue();
                }
                if (cookie.getName().equals("refreshToken")) {
                    refreshToken = cookie.getValue();
                }
            }
        }

        //access is either null or expired, but refresh is good, then authorize, else return 401
        if(accessToken == null || jwtService.isTokenExpired(accessToken)) { //means user has never logged in
            if(refreshToken != null && !jwtService.isTokenExpired(refreshToken)) {
                email = jwtService.extractEmail(refreshToken);
                User fetchedUser = userRepository.findByEmail(email).orElseThrow();

                String newAccessJwt = jwtService.generateAccessToken(email);
                Cookie newAccessCookie = new Cookie("accessToken", newAccessJwt);
                newAccessCookie.setPath("/");
                newAccessCookie.setHttpOnly(true);
                newAccessCookie.setMaxAge(1800);
                response.addCookie(newAccessCookie);

                fetchedUser.incrementRefreshTokenVersion();
                String newRefreshJwt = jwtService.generateRefreshToken(email, fetchedUser.getRefreshTokenVersion());
                Cookie newRefreshCookie = new Cookie("refreshToken", newRefreshJwt);
                newRefreshCookie.setPath("/");
                newRefreshCookie.setHttpOnly(true);
                newRefreshCookie.setMaxAge(60*60*24*30);
                response.addCookie(newRefreshCookie);

                userRepository.save(fetchedUser);

                setAuthenticationState(newAccessJwt, request);
                filterChain.doFilter(request, response);
                return;
            }
            filterChain.doFilter(request, response);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); //401
            response.getWriter().write("Access and Refresh Tokens weren't passed into filter");
            return;
        }

        //when this point is reached, accessToken is not null, just double check if expired and valid
        if(!jwtService.isTokenExpired(accessToken)) {
            setAuthenticationState(accessToken, request);
            filterChain.doFilter(request, response);
            return;
        }


        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("Unauthorized: both tokens are expired, user needs to log in again");
    }
}
