package uniruse.mse.examregistration.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Security configuration for the REST API.
 *
 * Source:
 * https://auth0.com/blog/implementing-jwt-authentication-on-spring-boot/
 */
@Configuration
@EnableWebSecurity
public class WebSecurity extends WebSecurityConfigurerAdapter {
	private UserDetailsService userDetailsService;
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	public WebSecurity(UserDetailsService userDetailsService,
			BCryptPasswordEncoder bCryptPasswordEncoder) {
		this.userDetailsService = userDetailsService;
		this.bCryptPasswordEncoder = bCryptPasswordEncoder;
	}

	/**
	 * Adds global security configuration for the application.
	 * 
	 * 1. Enables CORS (TODO: configure CORS as well).
	 * 2. Disables CSRF protection (it's not needed).
	 * 3. Makes POST '/login' and '/sign-up' public endpoints (no auth).
	 * 4. Makes all other endpoints require authentication.
	 * 5. Adds JWT Authentication and Authorization filters.
	 * 6. Turns off sessions (we should not keep any state).
	 * 
	 * Currently authentication is disabled when running tests!
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.cors()
			.and()
			.csrf()
			.disable()
			.authorizeRequests()
			.antMatchers(HttpMethod.POST, "/login", "/sign-up")
			.permitAll()
			.anyRequest()
			.authenticated()
			.and()
			.addFilter(new JWTAuthenticationFilter(authenticationManager()))
			.addFilter(new JWTAuthorizationFilter(authenticationManager()))
			.sessionManagement()
			.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
	}

	@Override
	public void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService)
			.passwordEncoder(bCryptPasswordEncoder);
	}
}