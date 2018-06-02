package uniruse.mse.examregistration.subject;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import uniruse.mse.examregistration.exception.ObjectAlreadyExistsException;
import uniruse.mse.examregistration.exception.ObjectNotFoundException;
import uniruse.mse.examregistration.exception.OperationNotAllowedException;
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
		final Subject example = new Subject();
		example.setName(subject.getName());

		final Optional<Subject> existingSubject = subjectRepository.findOne(Example.of(example));

		if (existingSubject.isPresent()) {
			throw new ObjectAlreadyExistsException("Subject with name '" + subject.getName() + "' already exists");
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
	
	public Subject update(Long subjectId, Subject subjectData) {
		Subject subjFound = subjectRepository.findById(subjectId).orElseThrow(
			() -> new ObjectNotFoundException("Subject was not found.")
		);
		
		if (subjectData.getName() != null) {
			subjFound.setName(subjectData.getName());			
		}
		
		if (subjectData.getDescription() != null) {
			subjFound.setDescription(subjectData.getDescription());			
		}

		return subjectRepository.save(subjFound);
	}

	public Subject updateAssignees(Long subjectId, String[] added, String[] removed) {
		final Optional<Subject> subjectOptional = subjectRepository.findById(subjectId);

		if (!subjectOptional.isPresent()) {
			throw new ObjectNotFoundException("Subject with id '" + subjectId + "' is not found");
		}

		final Subject subject = subjectOptional.get();

		if (added != null) {
			for (final String username : added) {
				final ApplicationUser professor = userService.getByUsername(username).get();

				if (professor.getRole().equals(UserRole.PROFESSOR)) {
					subject.getProfessors().add(professor);
				} else {
					throw new OperationNotAllowedException("The provided user '" + username + "' is not a professor");
				}
			}
		}

		if (removed != null) {
			for (final String username : removed) {
				final ApplicationUser professor = userService.getByUsername(username).get();

				subject.getProfessors().remove(professor);
			}
		}

		return subjectRepository.save(subject);
	}

}
