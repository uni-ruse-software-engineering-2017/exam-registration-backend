package uniruse.mse.examregistration.rusystem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class MockRuSystem implements RuStudentSystem {
	private List<RuStudentData> students;

	public MockRuSystem() {
	    try {
	    	final File inputF = new ClassPathResource("students.csv").getFile();
	    	final InputStream inputFS = new FileInputStream(inputF);
	    	final BufferedReader br = new BufferedReader(new InputStreamReader(inputFS, "UTF-8"));
	    	// skip the header of the csv
	    	students = br.lines().skip(1).map(mapToItem).collect(Collectors.toList());
	    	br.close();
	    } catch (final IOException e) {
	    	students = new ArrayList<>();
	    }
	}

	@Override
	public RuStudentData findByFacultyNumber(String fn) {
		for (final RuStudentData student : students) {
			if (student.getFacultyNumber().equals(fn)) {
				return student;
			}
		}
		return null;
	}

	@Override
	public List<RuStudentData> getAll() {
		return this.students;
	}

	@Override
	public boolean exists(String fn) {
		for (final RuStudentData student : students) {
			if (student.getFacultyNumber().equals(fn)) {
				return true;
			}
		}
		return false;
	}

	private final Function<String, RuStudentData> mapToItem = (line) -> {
		  final String[] p = line.split(";");
		  final RuStudentData stud = new RuStudentData();

		  stud.setFacultyNumber(p[0]);
		  stud.setFullName(p[1]);
		  stud.setStudyForm(Integer.decode(p[2]));
		  stud.setSpecialty(p[3]);
		  stud.setGroupNumber(p[4]);

		  return stud;
	};
}
