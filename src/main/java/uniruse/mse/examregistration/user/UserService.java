package uniruse.mse.examregistration.user;

import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Example;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import uniruse.mse.examregistration.InvalidEmailAddressException;
import uniruse.mse.examregistration.ObjectAlreadyExistsException;
import uniruse.mse.examregistration.user.model.ApplicationUser;
import uniruse.mse.examregistration.user.model.SignUpUser;

@Service
public class UserService {
	private static final Pattern STUDENT_EMAIL_REGEX = Pattern.compile("^s[0-9]{6}@stud\\.uni-ruse\\.bg$");

	private static final Pattern PROFESSOR_EMAIL_REGEX = Pattern.compile("^([\\w\\.\\-_]+)?\\w+@ami\\.uni-ruse\\.bg$");

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BCryptPasswordEncoder encoder;

	public ApplicationUser create(ApplicationUser user) {
		Optional<ApplicationUser> existingUser = this.getByUsername(user.getUsername());

		if (existingUser.isPresent()) {
			throw new ObjectAlreadyExistsException("User with username '" + user.getUsername() + "' already exists");
		}

		user.setPassword(encoder.encode(user.getPassword()));

		return userRepository.save(user);
	}

	public void create(SignUpUser user) {
		Optional<ApplicationUser> existingUser = this.getByUsername(user.getUsername());

		if (existingUser.isPresent()) {
			throw new ObjectAlreadyExistsException("User with username '" + user.getUsername() + "' already exists");
		}

		ApplicationUser newUser = new ApplicationUser();
		newUser.setUsername(user.getUsername());
		newUser.setPassword(encoder.encode(user.getPassword()));
		newUser.setFullName("");
		newUser.setRole(this.getRoleFromEmail(user.getUsername()));

		userRepository.save(newUser);
	}

	public Optional<ApplicationUser> getByUsername(String username) {
		if ("" == username || null == username) {
			return Optional.empty();
		}

		ApplicationUser byUsername = new ApplicationUser();
		byUsername.setUsername(username);

		return userRepository.findOne(Example.of(byUsername));
	}

	private UserRole getRoleFromEmail(String emailAddress) {
		if (STUDENT_EMAIL_REGEX.matcher(emailAddress).matches()) {
			return UserRole.STUDENT;
		} else if (PROFESSOR_EMAIL_REGEX.matcher(emailAddress).matches()) {
			return UserRole.PROFESSOR;
		}

		throw new InvalidEmailAddressException("Invalid email address provided.");
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
