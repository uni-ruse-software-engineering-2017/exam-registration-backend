package uniruse.mse.examregistration.exam;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
class ExamRepository extends SimpleJpaRepository<Exam, Long> {

	@Autowired
	public ExamRepository(EntityManager em) {
		super(Exam.class, em);
	}

}
