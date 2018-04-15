package uniruse.mse.examregistration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import uniruse.mse.examregistration.user.model.ApplicationUser;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import static uniruse.mse.examregistration.security.SecurityConstants.EXPIRATION_TIME;
import static uniruse.mse.examregistration.security.SecurityConstants.HEADER_STRING;
import static uniruse.mse.examregistration.security.SecurityConstants.SECRET;
import static uniruse.mse.examregistration.security.SecurityConstants.TOKEN_PREFIX;
import static uniruse.mse.examregistration.security.SecurityConstants.ISSUER;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private AuthenticationManager authenticationManager;

    public JWTAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res)
            throws AuthenticationException {
        try {
            ApplicationUser creds = new ObjectMapper().readValue(req.getInputStream(), ApplicationUser.class);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    creds.getUsername(), creds.getPassword(), new ArrayList<>());
            return authenticationManager.authenticate(authentication);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse res, FilterChain chain,
            Authentication auth) throws IOException, ServletException {
        // get the user information
        User principal = (User) auth.getPrincipal();

        // construct the JWT
        // TODO: add user role
        String token = Jwts.builder().setSubject(principal.getUsername()).setIssuer(ISSUER)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .setIssuedAt(new Date(System.currentTimeMillis())).signWith(SignatureAlgorithm.HS512, SECRET.getBytes())
                .compact();

        // set the JWT as a header
        res.addHeader(HEADER_STRING, TOKEN_PREFIX + token);

        // set the JWT as the 'session' cookie key value
        Cookie jwtCookie = new Cookie("session", token);
        jwtCookie.setSecure(true);
        jwtCookie.setHttpOnly(true);
        res.addCookie(jwtCookie);
    }
}