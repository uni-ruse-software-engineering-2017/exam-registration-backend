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
	    	File inputF = new ClassPathResource("students.csv").getFile();
	    	InputStream inputFS = new FileInputStream(inputF);
	    	BufferedReader br = new BufferedReader(new InputStreamReader(inputFS));
	    	// skip the header of the csv
	    	students = br.lines().skip(1).map(mapToItem).collect(Collectors.toList());
	    	br.close();
	    } catch (IOException e) { 
	    	students = new ArrayList<>();
	    }
	}
	
	@Override
	public RuStudentData findByFacultyNumber(String fn) {
		for (RuStudentData student : students) {
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
		for (RuStudentData student : students) {
			if (student.getFacultyNumber().equals(fn)) {
				return true;
			}
		}
		return false;
	}

	private Function<String, RuStudentData> mapToItem = (line) -> {
		  String[] p = line.split(";");// a CSV has comma separated lines
		  RuStudentData item = new RuStudentData();
		  item.setFacultyNumber(p[0]);
		  
		  item.setFullName(p[1]);  
		  
		  item.setStudyForm(Integer.decode(p[2]));
		  
		  item.setSpecialty(p[3]);
		  
		  item.setGroupNumber(Integer.decode(p[4]));
		    
		  //more initialization goes here
		  return item;
	};
}
