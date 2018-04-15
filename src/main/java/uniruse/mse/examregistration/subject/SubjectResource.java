package uniruse.mse.examregistration.subject;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PATCH;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/subjects")
public class SubjectResource {

	@Autowired
	private SubjectService subjectService;

	@RequestMapping(method = POST)
	public ResponseEntity<?> create(@RequestBody Subject subject) {
		subjectService.create(subject);

		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@RequestMapping(method = GET)
	public List<Subject> getSubjets() {
		return subjectService.getSubjects();
	}

	@RequestMapping(method = PATCH, path = "/{id}/assignees")
	public void assignProfessors(@PathVariable("id") Long subjectId, @RequestBody SubjectAssignmentRequest request) {
		subjectService.updateAssignees(subjectId, request.getAdded(), request.getRemoved());
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
