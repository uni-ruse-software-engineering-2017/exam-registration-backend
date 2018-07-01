package uniruse.mse.examregistration.user;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uniruse.mse.examregistration.exception.IllegalUsernameException;
import uniruse.mse.examregistration.exception.InvalidEmailAddressException;
import uniruse.mse.examregistration.exception.ObjectAlreadyExistsException;
import uniruse.mse.examregistration.exception.ObjectNotFoundException;
import uniruse.mse.examregistration.exception.OperationNotAllowedException;
import uniruse.mse.examregistration.exception.UserAlreadyActivatedException;
import uniruse.mse.examregistration.rusystem.RuStudentData;
import uniruse.mse.examregistration.rusystem.RuStudentSystem;
import uniruse.mse.examregistration.user.model.ApplicationUser;
import uniruse.mse.examregistration.user.model.PatchProfessorModel;
import uniruse.mse.examregistration.user.model.Professor;
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

	@Autowired
	private RuStudentSystem studentSystem;

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
	public Student createStudent(Student stud) {
		final Optional<ApplicationUser> existingUser = this.getByUsername(
			stud.getUsername()
		);

		if (existingUser.isPresent()) {
			throw new ObjectAlreadyExistsException("Student with username '"
				+ stud.getUsername() + "' already exists");
		}

		final String password = stud.getPassword();
		if (password == "" || password == null) {
			throw new IllegalArgumentException("Student password must be provided.");
		}

		stud.setPassword(encoder.encode(password));
		stud.setActive(false);

		return userRepository.save(stud);
	}

	@Transactional
	public Professor createProfessor(Professor professor) {
		final Optional<ApplicationUser> existingUser = this.getByUsername(
			professor.getUsername()
		);

		if (existingUser.isPresent()) {
			throw new ObjectAlreadyExistsException("Professor with username '"
					+ professor.getUsername() + "' already exists");
		}

		String password = professor.getPassword();
		if (password == "" || password == null) {
			// generate a placeholder password
			password = Instant.now().getEpochSecond()
					+ new StringBuilder(professor.getUsername()).reverse().toString();
		}

		professor.setPassword(encoder.encode(password));
		professor.setActive(false);

		return userRepository.save(professor);
	}


	/**
	 * Updates existing professor's information.
	 *
	 * @param professorId - the ID of the professor that will be updated
	 * @param profModel - the new information that will be saved
	 * @return updated professor instance
	 */
	public Professor updateProfessor(Long professorId, PatchProfessorModel profModel) {
		final Optional<ApplicationUser> professor = this.userRepository.findById(professorId);

		if (!professor.isPresent()) {
			throw new ObjectNotFoundException("Professor with ID '"
					+ professorId + "' was not found.");
		}

		final Professor existingProfessor = (Professor) professor.get();

		// nothing to update
		if (profModel == null) {
			return existingProfessor;
		}

		if (profModel.getCabinet() != null && !profModel.getCabinet().isEmpty()) {
			existingProfessor.setCabinet(profModel.getCabinet());
		}

		if (profModel.getFullName() != null && !profModel.getFullName().isEmpty()) {
			existingProfessor.setFullName(profModel.getFullName());
		}

		if (profModel.getPhoneNumber() != null && !profModel.getPhoneNumber().isEmpty()) {
			existingProfessor.setPhoneNumber(profModel.getPhoneNumber());
		}

		return this.userRepository.save(existingProfessor);
	}

	@Transactional
	public void signUp(SignUpUser user) {
		final ApplicationUser existingUser = this.getByUsername(user.getUsername()).orElse(null);

		if (isStudent(user.getUsername())) {
			final String fn = user.getUsername().substring(1, 7);

			if (!studentSystem.exists(fn)) {
				throw new ObjectNotFoundException("Student with FN '"
						+ fn + "' does not exist in our database.");
			}

			if (existingUser != null) {
				throw new ObjectAlreadyExistsException("Student with username '"
						+ user.getUsername() + "' already exists");
			}

			// create student user
			final Student student = new Student();
			student.setUsername(user.getUsername());
			student.setPassword(encoder.encode(user.getPassword()));
			student.setRole(this.getRoleFromEmail(user.getUsername()));
			student.setActive(false);

			// extract faculty number from email address
			student.setFacultyNumber(fn);

			final RuStudentData studentData = studentSystem.findByFacultyNumber(fn);
			student.setFullName(studentData.getFullName());
			student.setStudyForm(getStudyForm(studentData.getStudyForm()));
			student.setSpecialty(studentData.getSpecialty());
			student.setGroupNumber(studentData.getGroupNumber());
			userRepository.save(student);
			return;
		} else if (isProfessor(user.getUsername())) {
			if (existingUser == null) {
				throw new AccessDeniedException(
					"Professor with email address " + user.getUsername() + " has not been registered by an administrator yet."
				);
			}

			if (!existingUser.isActive()) {
				final Professor prof = (Professor) existingUser;
				prof.setPassword(encoder.encode(user.getPassword()));
				userRepository.save(prof);
				return;
			} else {
				throw new UserAlreadyActivatedException(
					"Professor with email address " + user.getUsername() + " has already activated their account."
				);
			}
		} else {
			throw new IllegalUsernameException(
				user.getUsername() + " is not a valid email address. You must provide your RU 'Angel Kanchev' university email address or your work one."
			);
		}
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

	/**
	 * Lists all student users in the system
	 * @return students
	 */
	public List<Student> getStudents() {
		final Student student = new Student();

		return userRepository.findAll(Example.of(student)).stream().map((stud) -> {
			// hide the password for security reasons
			stud.setPassword(null);
			return stud;
		}).collect(Collectors.toList());
	}

	/**
	 * Lists all professor users in the system
	 * @return professors
	 */
	public List<Professor> getProfessors() {
		final Professor professor = new Professor();

		return userRepository.findAll(Example.of(professor)).stream().map((prof) -> {
			// hide the password for security reasons
			prof.setPassword(null);
			return prof;
		}).collect(Collectors.toList());
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
	public String generateActivationToken(String username) {
		final Optional<ApplicationUser> userOptional = getByUsername(username);

		final ApplicationUser user = userOptional
			.orElseThrow(() -> new ObjectNotFoundException(
					"User '" + username + "' does not exist"));

		return encoder.encode(user.getId() + user.getUsername());
	}

	public UserRole getRoleFromEmail(String emailAddress) {
		if (this.isStudent(emailAddress)) {
			return UserRole.STUDENT;
		} else if (this.isProfessor(emailAddress)) {
			return UserRole.PROFESSOR;
		}

		throw new InvalidEmailAddressException(
				"Invalid email address provided.");
	}

	private boolean isStudent(String emailAddress) {
		return STUDENT_EMAIL_REGEX.matcher(emailAddress).matches();
	}

	private boolean isProfessor(String emailAddress) {
		return PROFESSOR_EMAIL_REGEX.matcher(emailAddress).matches();
	}

	private StudyForm getStudyForm(int n) {
		if (n == 1) {
			return StudyForm.FULL_TIME;
		} else if (n == 2) {
			return StudyForm.PART_TIME;
		} else {
			return StudyForm.REMOTE;
		}
	}

}
