package uniruse.mse.examregistration.subject;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public class SubjectRepository extends SimpleJpaRepository<Subject, Long> {

	@Autowired
	public SubjectRepository(EntityManager em) {
		super(Subject.class, em);
	}

}
