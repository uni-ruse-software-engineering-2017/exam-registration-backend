package uniruse.mse.examregistration.user;

import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uniruse.mse.examregistration.InvalidEmailAddressException;
import uniruse.mse.examregistration.ObjectAlreadyExistsException;
import uniruse.mse.examregistration.ObjectNotFoundException;
import uniruse.mse.examregistration.OperationNotAllowedException;
import uniruse.mse.examregistration.user.model.ApplicationUser;
import uniruse.mse.examregistration.user.model.SignUpUser;
import uniruse.mse.examregistration.user.model.Student;
import uniruse.mse.examregistration.user.model.StudyForm;
import uniruse.mse.examregistration.user.model.UserRole;

@Service
public class UserService {
	private static final Pattern STUDENT_EMAIL_REGEX = Pattern
		.compile("^s[0-9]{6}@stud\\.uni-ruse\\.bg$");

	private static final Pattern PROFESSOR_EMAIL_REGEX = Pattern
		.compile("^([\\w\\.\\-_]+)?\\w+@ami\\.uni-ruse\\.bg$");

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder encoder;

	@Transactional
	public ApplicationUser create(ApplicationUser user) {
		final Optional<ApplicationUser> existingUser = this
			.getByUsername(user.getUsername());

		if (existingUser.isPresent()) {
			throw new ObjectAlreadyExistsException("User with username '"
					+ user.getUsername() + "' already exists");
		}

		user.setPassword(encoder.encode(user.getPassword()));
		user.setActive(false);

		return userRepository.save(user);
	}

	@Transactional
	public void create(SignUpUser user) {
		final Optional<ApplicationUser> existingUser = this
			.getByUsername(user.getUsername());

		if (existingUser.isPresent()) {
			throw new ObjectAlreadyExistsException("User with username '"
					+ user.getUsername() + "' already exists");
		}

		final Student student = new Student();
		student.setUsername(user.getUsername());
		student.setPassword(encoder.encode(user.getPassword()));
		student.setFullName("");
		student.setRole(this.getRoleFromEmail(user.getUsername()));
		student.setActive(false);

		// extract faculty number from email address
		student.setFacultyNumber(student.getUsername()
			.substring(1, 7));

		// TODO: get student data from external system
		student.setStudyForm(StudyForm.FULL_TIME);
		student.setSpecialty("Computer Science");
		student.setGroupNumber(50);

		userRepository.save(student);
	}

	@Transactional(readOnly = true)
	public Optional<ApplicationUser> getByUsername(String username) {
		if ("" == username || null == username) {
			return Optional.empty();
		}

		final ApplicationUser byUsername = new ApplicationUser();
		byUsername.setUsername(username);

		return userRepository.findOne(Example.of(byUsername));
	}

	@Transactional
	public void activate(String username, String token) {
		final Optional<ApplicationUser> userOptional = getByUsername(username);

		final ApplicationUser user = userOptional
			.orElseThrow(() -> new ObjectNotFoundException(
					"User '" + username + "' does not exist"));

		if (user.isActive()) {
			throw new OperationNotAllowedException("Account already activated");
		}

		if (!encoder.matches(user.getId() + user.getUsername(), token)) {
			throw new OperationNotAllowedException(
					"Provided token does not match");
		}

		user.setActive(true);

		userRepository.save(user);
	}

	@Transactional(readOnly = true)
	public String generateActicationToken(String username) {
		final Optional<ApplicationUser> userOptional = getByUsername(username);

		final ApplicationUser user = userOptional
			.orElseThrow(() -> new ObjectNotFoundException(
					"User '" + username + "' does not exist"));

		return encoder.encode(user.getId() + user.getUsername());
	}

	public UserRole getRoleFromEmail(String emailAddress) {
		if (STUDENT_EMAIL_REGEX.matcher(emailAddress)
			.matches()) {
			return UserRole.STUDENT;
		} else if (PROFESSOR_EMAIL_REGEX.matcher(emailAddress)
			.matches()) {
			return UserRole.PROFESSOR;
		}

		throw new InvalidEmailAddressException(
				"Invalid email address provided.");
	}
}
