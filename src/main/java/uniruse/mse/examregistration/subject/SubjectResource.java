package uniruse.mse.examregistration.subject;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PATCH;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import uniruse.mse.examregistration.exception.ObjectNotFoundException;

@RestController
@RequestMapping("/subjects")
public class SubjectResource {

	@Autowired
	private SubjectService subjectService;

	@RequestMapping(method = POST)
	@ResponseStatus(code = HttpStatus.CREATED)
	@PreAuthorize("hasRole('ADMIN')")
	public Subject create(@RequestBody Subject subject) {
		return subjectService.create(subject);
	}

	@RequestMapping(method = GET)
	public List<Subject> getSubjets() {
		return subjectService.getSubjects();
	}

	@RequestMapping(method = GET, path = "/{subjectId}")
	public Subject getSubject(@PathVariable Long subjectId) {
		final Subject subj = subjectService.getSubjectById(subjectId);

		if (subj == null) {
			throw new ObjectNotFoundException(
					"Subject with ID " + subjectId + " was not found.");
		}

		return subj;
	}

	@RequestMapping(method = DELETE, path = "/{subjectId}")
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@PreAuthorize("hasRole('ADMIN')")
	public void deleteSubject(@PathVariable Long subjectId) {
		subjectService.deleteSubject(subjectId);
	}
	
	@RequestMapping(method = PATCH, path = "/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public Subject editSubject(
		@PathVariable("id") Long subjectId,
		@RequestBody Subject subj
	) {
		return subjectService.update(subjectId, subj);
	}

	@RequestMapping(method = PATCH, path = "/{id}/assignees")
	public void assignProfessors(@PathVariable("id") Long subjectId,
			@RequestBody SubjectAssignmentRequest request) {
		subjectService.updateAssignees(subjectId, request.getAdded(),
				request.getRemoved());
	}

	public static class SubjectAssignmentRequest {
		private String[] added;

		private String[] removed;

		public String[] getAdded() {
			return added;
		}

		public void setAdded(String[] added) {
			this.added = added;
		}

		public String[] getRemoved() {
			return removed;
		}

		public void setRemoved(String[] removed) {
			this.removed = removed;
		}
	}

}
