package Learnd.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JWTService {

    private String secretKey = "";

    public JWTService (){
        try {
            KeyGenerator keygen = KeyGenerator.getInstance("HmacSHA256");
            SecretKey key = keygen.generateKey();
            //you want to encode to base 64 and then encode the result to string for storage
            //getEncoded returns the byte[] version of they key
            secretKey = Base64.getEncoder().encodeToString(key.getEncoded());
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public String generateRefreshToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("refreshTokenVersion", 1);
        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 6)) // for 6 hrs
                .and()
                .signWith(getKey())
                .compact();
    }
    public String generateRefreshToken(String email, int newVersion) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("refreshTokenVersion", newVersion);
        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + (long)1000 * 60 * 60 * 720)) // for 30 days
                .and()
                .signWith(getKey())
                .compact();
    }

    public String generateAccessToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30)) // for 30 minutes
                .and()
                .signWith(getKey())
                .compact();
    }

    private SecretKey getKey(){
        byte[] bytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(bytes);
    }

    //calls extractClaim() which calls extractAllClaims() which verifies signature
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    //verifies signature, checks email to compare
    public boolean validateEmail(String token, UserDetails userDetails) {
        String email = extractEmail(token);
        return email.equals(userDetails.getUsername());
    }

    //takes in a function that gets a specific portion of claim
    //calls extractAllClaims to get all the claims
    //use the function that is passed in to extract specific portion of claim
    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token) // will throw exception if signature is not valid
                .getPayload();
    }

    //verifies signature, checks expiration, extracts email to compare,
    public boolean validateToken(String token, UserDetails userDetails) {
        //extractEmail validates the signature of the jwt
        final String userName = extractEmail(token);
        return userName.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public int getRefreshTokenVersion(String token) {
        return extractRefreshTokenVersion(token);
    }

    private int extractRefreshTokenVersion (String token) {
        return extractClaim(token, claims -> claims.get("refreshTokenVersion", Integer.class));
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
