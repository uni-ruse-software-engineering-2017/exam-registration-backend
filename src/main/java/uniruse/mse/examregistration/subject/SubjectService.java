package uniruse.mse.examregistration.subject;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import uniruse.mse.examregistration.ObjectAlreadyExistsException;
import uniruse.mse.examregistration.user.ApplicationUser;
import uniruse.mse.examregistration.user.UserService;

@Service
public class SubjectService {

	@Autowired
	private SubjectRepository subjectRepository;

	@Autowired
	private UserService userService;

	public void create(Subject subject) {
		Subject example = new Subject();
		example.setName(subject.getName());

		Optional<Subject> existingSubject = subjectRepository.findOne(Example.of(example));

		if (existingSubject.isPresent()) {
			throw new ObjectAlreadyExistsException("Subject with username '" + subject.getName() + "' already exists");
		}

		subjectRepository.save(subject);
	}

	public List<Subject> getSubjects() {
		return subjectRepository.findAll(new Sort(Direction.ASC, "name"));
	}

	public void assign(Long subjectId, String[] professors) {
		Optional<Subject> subjectOptional = subjectRepository.findById(subjectId);

		if (subjectOptional.isPresent()) {
			Subject subject = subjectOptional.get();

			for (String username : professors) {
				Optional<ApplicationUser> professor = userService.getByUsername(username);
				subject.getProfessors().add(professor.get());
			}

			subjectRepository.save(subject);
		} else {
			// TODO throw exception
		}
	}

}
