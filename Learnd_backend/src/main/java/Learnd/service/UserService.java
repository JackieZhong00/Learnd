package Learnd.service;

import jakarta.servlet.http.HttpServletRequest;
import Learnd.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import Learnd.repo.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
public class UserService {
    private final UserRepository userRepository;
    private final AuthenticationManager authManager;
    private final JWTService jwtService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);


    public UserService(UserRepository userRepo, AuthenticationManager authManager, JWTService jwtService) {
        this.userRepository = userRepo;
        this.authManager = authManager;
        this.jwtService = jwtService;
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }


    public Optional<User> getUser(String username){
        return userRepository.findByUsername(username);
    }
    public List<User> getUsers(){
        return userRepository.findAll();
    }


    public Map<String,String> verify(User user){
        //authentication token is created with user credentials
        //token is passed into authenticate which then passes the token to
        // authentication provider specified in your config, in our case it is DaoAuthenticationProvider
        // DaoAuthenticationProvider will load user using the userDetailsService class that you defined
        // DaoAuthenticationProvider will then use your
        // specified password encrypter to match with password stored in db, in our case we use Bcrypt
        // authenticate will then return an Authentication object upon the receiving the AuthenticationProvider object
        // authenticate concludes this process by returning an Authentication object with isAuthenticated = true
        // the Authentication object also includes the user details

        Authentication auth = authManager
                .authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));
        if (auth.isAuthenticated()) {
            Map<String,String> tokens = new HashMap<>();
            tokens.put("accessToken", jwtService.generateAccessToken(user.getEmail()));
            tokens.put("refreshToken", jwtService.generateRefreshToken(user.getEmail()));

            Optional<User> fetchedUser = userRepository.findByEmail(user.getEmail());
            User userObj = fetchedUser.orElseThrow();
            userObj.setRefreshTokenVersion();
            System.out.println("verion number after verification: " + userObj.getRefreshTokenVersion());
            userRepository.save(userObj);
            SecurityContextHolder.getContext().setAuthentication(auth);
            return tokens;
        }
        Map<String,String> emptyTokens = new HashMap<>();
        emptyTokens.put("error", "failed authentication");
        System.out.println("failed authentication ");
        return emptyTokens;
    }



    public Map<String,String> register(User user){
        Authentication auth = authManager
                .authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));
        if (auth.isAuthenticated()) {
            Map<String, String> emptyTokens = new HashMap<>();
            emptyTokens.put("error", "user with this email already exists");
            return emptyTokens;
        }
        //store hashed pass + lower cased email + username (parsed email) + refreshTokenVersion into db
        user.setPassword(encoder.encode(user.getPassword()));
        String email = user.getEmail();
        user.setEmail(email.toLowerCase());
        int index = email.indexOf('@');
        user.setUsername(email.substring(0, index).toLowerCase());
        user.setRefreshTokenVersion();
        userRepository.save(user);

        Map<String,String> tokens = new HashMap<>();
        tokens.put("accessToken", jwtService.generateAccessToken(user.getEmail()));
        tokens.put("refreshToken", jwtService.generateRefreshToken(user.getEmail()));
        return tokens;

    }

    public ResponseEntity<Void> logout (HttpServletRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        user.incrementRefreshTokenVersion();
        userRepository.save(user);

        return ResponseEntity.ok().build();
    }

}
