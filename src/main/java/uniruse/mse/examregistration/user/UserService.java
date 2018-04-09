package uniruse.mse.examregistration.user;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Example;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import uniruse.mse.examregistration.ObjectAlreadyExistsException;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BCryptPasswordEncoder encoder;

	public void create(ApplicationUser user) {
		ApplicationUser example = new ApplicationUser();
		example.setUsername(user.getUsername());

		Optional<ApplicationUser> existingUser = userRepository.findOne(Example.of(example));

		if (existingUser.isPresent()) {
			throw new ObjectAlreadyExistsException("User with username '" + user.getUsername() + "' already exists");
		}

		user.setPassword(encoder.encode(user.getPassword()));

		userRepository.save(user);
	}

	public Optional<ApplicationUser> getByUsername(String username) {
		ApplicationUser example = new ApplicationUser();
		example.setUsername(username);
		return userRepository.findOne(Example.of(example));
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
