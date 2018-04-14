package uniruse.mse.examregistration.subject;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
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

	@RequestMapping(method = POST, path = "/{id}")
	public void assignProfessors(@PathVariable("id") Long subjectId, @RequestBody String[] professors) {
		subjectService.assign(subjectId, professors);
	}

}
