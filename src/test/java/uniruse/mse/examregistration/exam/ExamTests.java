package uniruse.mse.examregistration.exam;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;

import uniruse.mse.examregistration.BaseTest;
import uniruse.mse.examregistration.exam.ExamParticipationRequest.ExamParticipationRequestStatus;
import uniruse.mse.examregistration.subject.Subject;
import uniruse.mse.examregistration.subject.SubjectService;
import uniruse.mse.examregistration.user.UserRole;
import uniruse.mse.examregistration.user.model.ApplicationUser;

public class ExamTests extends BaseTest {

	@Autowired
	private ExamRepository examRepository;

	@Autowired
	private ExamParticipationRequestRepository participationRepository;

	@Autowired
	private SubjectService subjectService;

	@Test
	@Transactional
	public void test() {
		ApplicationUser professor = createUser("grigorova", "123456", UserRole.PROFESSOR);
		Subject programming = createSubject("programming");

		Exam exam = new Exam();

		exam.setProfessor(professor);
		exam.setSubject(programming);
		exam.setHall("1.416");
		exam.setMaxSeats(4);
		exam.setStartTime(new Date());
		exam.setEndTime(new Date());

		examRepository.save(exam);

		ApplicationUser student = createUser("gosho", "123456", UserRole.STUDENT);

		ExamParticipationRequest request = new ExamParticipationRequest();

		request.setExam(exam);
		request.setStudent(student);
		request.setStatus(ExamParticipationRequestStatus.PENDING);

		participationRepository.save(request);


//		Exam example = new Exam();
//		example.setHall("1.416");

//		Optional<Exam> findById = examRepository.findOne(Example.of(example));
//
//		List<ExamParticipationRequest> participationRequests = findById.get().getParticipationRequests();
//
//		System.out.println(participationRequests);

		ExamParticipationRequest example = new ExamParticipationRequest();
		example.setExam(exam);

		List<ExamParticipationRequest> findAll = participationRepository.findAll(Example.of(example));

		System.out.println(findAll.size());
	}

	private Subject createSubject(String name) {
		Subject subject = new Subject();

		subject.setName(name);

		subjectService.create(subject);

		return subject;
	}

}
