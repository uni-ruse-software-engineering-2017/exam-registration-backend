package uniruse.mse.examregistration.rusystem;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

public class MockRuSystemTest {

	@Test
	public void test_shouldReadTheCSVFile() throws IOException {
		MockRuSystem mock = new MockRuSystem();
		
		assertTrue(mock.getAll().size() > 0);
	}

	@Test
	public void test_shouldFindStudentByFacultyNumber() {
		MockRuSystem mock = new MockRuSystem();
		
		RuStudentData student = mock.findByFacultyNumber("166355");
		// 166355;Дамла Алтънай Ахмедова;1;Социална педагогика;64
		assertEquals("Дамла Алтънай Ахмедова", student.getFullName());
		assertEquals(1, student.getStudyForm().intValue());
		assertEquals("Социална педагогика", student.getSpecialty());
		assertEquals(64, student.getGroupNumber().intValue());
	}
}
