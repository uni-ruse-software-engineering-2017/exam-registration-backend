package uniruse.mse.examregistration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class ExamRegistrationBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExamRegistrationBackendApplication.class, args);
	}

	@Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder(10);
	}
}
