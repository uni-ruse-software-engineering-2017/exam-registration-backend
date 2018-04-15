package uniruse.mse.examregistration.user;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Repository;

import uniruse.mse.examregistration.user.model.ApplicationUser;

@Repository
class UserRepository extends SimpleJpaRepository<ApplicationUser, Long> {
	@Autowired
	public UserRepository(EntityManager em) {
		super(ApplicationUser.class, em);
	}
}
