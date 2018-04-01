package uniruse.mse.examregistration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "uniruse.mse.examregistration")
@EnableTransactionManagement
public class H2Config {

}
