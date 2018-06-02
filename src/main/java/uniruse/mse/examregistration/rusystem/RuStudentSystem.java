package uniruse.mse.examregistration.rusystem;

import java.util.List;

public interface RuStudentSystem {
	RuStudentData findByFacultyNumber(String fn);

	List<RuStudentData> getAll();

	boolean exists(String fn);
}
