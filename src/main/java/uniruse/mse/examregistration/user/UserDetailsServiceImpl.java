package uniruse.mse.examregistration.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import uniruse.mse.examregistration.user.model.ApplicationUser;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
	@Autowired
	private UserService userService;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<ApplicationUser> userResult = this.userService.getByUsername(username);

		if (!userResult.isPresent()) {
			throw new UsernameNotFoundException(username);
		}

		ApplicationUser appUser = userResult.get();

		User user = new User(
			appUser.getUsername(),
			appUser.getPassword(),
			this.getGrantedAuthorities(
				Arrays.asList(appUser.getRole().name())
			)
		);

		return user;
	}

	private List<GrantedAuthority> getGrantedAuthorities(List<String> roles) {
		List<GrantedAuthority> authorities = new ArrayList<>();
		for (String privilege : roles) {
			authorities.add(new SimpleGrantedAuthority(privilege));
		}
		return authorities;
	}
}