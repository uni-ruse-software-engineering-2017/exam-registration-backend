package uniruse.mse.examregistration.security;

import static uniruse.mse.examregistration.security.SecurityConstants.EXPIRATION_TIME;
import static uniruse.mse.examregistration.security.SecurityConstants.ISSUER;
import static uniruse.mse.examregistration.security.SecurityConstants.SECRET;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import uniruse.mse.examregistration.user.model.LoginUser;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
	private final AuthenticationManager authenticationManager;

	public JWTAuthenticationFilter(
			AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}

	@Override
	public Authentication attemptAuthentication(
		HttpServletRequest req,
		HttpServletResponse res
	) throws AuthenticationException {
		try {
			final LoginUser creds = new ObjectMapper().readValue(
				req.getInputStream(),
				LoginUser.class
			);

			final UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
				creds.getUsername(),
				creds.getPassword(),
				new ArrayList<>()
			);

			return authenticationManager.authenticate(authentication);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void successfulAuthentication(
		HttpServletRequest req,
		HttpServletResponse res,
		FilterChain chain,
		Authentication auth
	) throws IOException, ServletException {
		// get the user information
		final User principal = (User) auth.getPrincipal();
		final String role = auth.getAuthorities()
			.stream()
			.findFirst()
			.get()
			.getAuthority();

		// set the user's role
		final Map<String, Object> claims = new HashMap<>();
		claims.put("role", role);

		final String token = Jwts.builder()
			.setSubject(principal.getUsername())
			.addClaims(claims)
			.setIssuer(ISSUER)
			.setExpiration(
				new Date(System.currentTimeMillis() + EXPIRATION_TIME)
			)
			.setIssuedAt(new Date(System.currentTimeMillis()))
			.signWith(SignatureAlgorithm.HS512, SECRET.getBytes())
			.compact();

		// return a JSON response with the token
		final ObjectMapper mapper = new ObjectMapper();
		final Map<String, String> jsonResponse = new HashMap<>();
		jsonResponse.put("token", token);
		res.setContentType("application/json;charset=UTF-8");
		res.getWriter().print(mapper.writeValueAsString(jsonResponse));
		res.getWriter().flush();
	}
}