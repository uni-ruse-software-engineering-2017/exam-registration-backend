package uniruse.mse.examregistration.exam;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
class ExamParticipationRequestRepository extends SimpleJpaRepository<ExamParticipationRequest, Long> {

	@Autowired
	public ExamParticipationRequestRepository(EntityManager em) {
		super(ExamParticipationRequest.class, em);
	}

}
