package uniruse.mse.examregistration.exam;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
class ExamEnrolmentRepository extends SimpleJpaRepository<ExamEnrolment, Long> {

	@Autowired
	public ExamEnrolmentRepository(EntityManager em) {
		super(ExamEnrolment.class, em);
	}

}
