package com.jwt.security;

import com.jwt.utils.TokenLifeTime;
import com.jwt.entity.Token;
import com.jwt.repository.TokenRepository;
import com.jwt.utils.MyKeyGenerator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Component
public class JwtService {

    private final TokenRepository tokenRepository;

    protected final SecretKey secretKey;

    private static final long EXPIRATION_TIME = TokenLifeTime.ONE_DAY.getDays();

    public JwtService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
        String secretString = MyKeyGenerator.generateSecretString();
        byte[] keyBytes = Decoders.BASE64.decode(secretString);
        this.secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    public String generateAccessToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME * 30))
                .signWith(secretKey)
                .compact();
    }

    public String extractUserName(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    private <T> T extractClaims(String token, Function<Claims, T> claimsTFunction) {
        final Claims claims = extractAllClaims(token);
        return claimsTFunction.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public void invalidateToken(String accessToken) {
        Optional<Token> tokenOpt = tokenRepository.findByAccessToken(accessToken);
        if (tokenOpt.isPresent()) {
            Token token = tokenOpt.get();
            token.setValid(false);
            tokenRepository.save(token);
        } else {
            Optional<Token> refreshTokenOpt = tokenRepository.findByRefreshToken(accessToken);
            if (refreshTokenOpt.isPresent()) {
                Token refreshToken = refreshTokenOpt.get();
                refreshToken.setValid(false);
                tokenRepository.save(refreshToken);
            }
        }
    }

    public void saveToken(String username, String accessToken, String refreshToken) {
        Token token = new Token();
        token.setUsername(username);
        token.setAccessToken(accessToken);
        token.setValid(true);
        token.setRefreshToken(refreshToken);
        token.setValid(true);
        tokenRepository.save(token);
    }

    public Token getAccessToken(String token) {
        Token tokenObj;
        if (token.startsWith("Bearer ")) {
            tokenObj = tokenRepository.findByAccessToken(token.substring(7)).get();
        } else {
            tokenObj = tokenRepository.findByAccessToken(token).get();
        }
        return tokenObj;
    }

    public List<Token> getTokens() {
        return tokenRepository.findAll();
    }
}