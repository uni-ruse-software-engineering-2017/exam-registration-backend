package uniruse.mse.examregistration.subject;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import uniruse.mse.examregistration.ObjectAlreadyExistsException;
import uniruse.mse.examregistration.ObjectNotFoundException;
import uniruse.mse.examregistration.OperationNotAllowedException;
import uniruse.mse.examregistration.user.UserService;
import uniruse.mse.examregistration.user.model.ApplicationUser;
import uniruse.mse.examregistration.user.model.UserRole;

@Service
public class SubjectService {

	@Autowired
	private SubjectRepository subjectRepository;

	@Autowired
	private UserService userService;

	public Subject create(Subject subject) {
		Subject example = new Subject();
		example.setName(subject.getName());

		Optional<Subject> existingSubject = subjectRepository.findOne(Example.of(example));

		if (existingSubject.isPresent()) {
			throw new ObjectAlreadyExistsException("Subject with username '" + subject.getName() + "' already exists");
		}

		return subjectRepository.save(subject);
	}

	public List<Subject> getSubjects() {
		return subjectRepository.findAll(new Sort(Direction.ASC, "name"));
	}
	
	public Subject getSubjectById(Long id) {
		if(!subjectRepository.existsById(id)) {
			return null;
		};
		
		return subjectRepository.getOne(id);
	}

	public void deleteSubject(Long id ) {
		subjectRepository.deleteById(id);
	}

	public void updateAssignees(Long subjectId, String[] added, String[] removed) {
		Optional<Subject> subjectOptional = subjectRepository.findById(subjectId);

		if (!subjectOptional.isPresent()) {
			throw new ObjectNotFoundException("Subject with id '" + subjectId + "' is not found");
		}

		Subject subject = subjectOptional.get();

		if (added != null) {
			for (String username : added) {
				ApplicationUser professor = userService.getByUsername(username).get();

				if (professor.getRole().equals(UserRole.PROFESSOR)) {
					subject.getProfessors().add(professor);
				} else {
					throw new OperationNotAllowedException("The provided user '" + username + "' is not a professor");
				}
			}
		}

		if (removed != null) {
			for (String username : removed) {
				ApplicationUser professor = userService.getByUsername(username).get();

				subject.getProfessors().remove(professor);
			}
		}

		subjectRepository.save(subject);
	}

}
