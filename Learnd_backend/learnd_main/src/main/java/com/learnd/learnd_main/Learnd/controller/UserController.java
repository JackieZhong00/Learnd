package com.learnd.learnd_main.Learnd.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.learnd.learnd_main.Learnd.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.learnd.learnd_main.Learnd.service.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

//    @GetMapping("/")
//    public List<User> getAllUsers() {
//        return userService.getUsers();
//    }

    //Authentication
    @PostMapping("/login")
    public ResponseEntity<Void> loginUser(@RequestBody User user, HttpServletResponse response) {
        Map<String, String> tokens = userService.verify(user); //checks to see if user credentials match db

        //manually set, so if Map has key with value "error" couldn't validate credentials
        if (tokens.containsKey("error")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        //if credentials are valid, set the cookies with the JWTs returned by userService.verify()
        Cookie accessTokenCookie = new Cookie("accessToken", tokens.get("accessToken"));
        accessTokenCookie.setPath("/");
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setMaxAge(60*30); //30 minutes


        Cookie refreshTokenCookie = new Cookie("refreshToken", tokens.get("refreshToken"));
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setMaxAge(60*60*24*30); //30 days

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);
        return ResponseEntity.ok().build();
    }

    //this endpoint by default is not accessible without a proper jwt from user
    //should return status code 401 if user does not have a valid jwt or does not have a jwt at all
    @GetMapping("/verifyJwt")
    public ResponseEntity<Map<String, String>> verifyJwt(HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println(authentication);
        //if user isn't authenticated
        if (!authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = authentication.getName();
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("email", email);

        return ResponseEntity.ok(responseBody);

    }


    //Post request for registering a new User
    @PostMapping("/register")
    public ResponseEntity<Map<String,String>> registerUser(@RequestBody User user, HttpServletResponse response) {
        Map<String,String> tokens = userService.register(user);
        if(tokens.containsKey("error")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Cookie accessTokenCookie = new Cookie("accessToken", tokens.get("accessToken"));
        accessTokenCookie.setPath("/");
        accessTokenCookie.setHttpOnly(true);

        Cookie refreshTokenCookie = new Cookie("refreshToken", tokens.get("refreshToken"));
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setHttpOnly(true);

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);
        return ResponseEntity.ok().build();
    }



    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie accessTokenCookie = new Cookie("accessToken", null);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setMaxAge(0);

        Cookie refreshTokenCookie = new Cookie("refreshToken", null);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setMaxAge(0);

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);
        userService.logout(request);
        return ResponseEntity.ok().build();
    }

}
