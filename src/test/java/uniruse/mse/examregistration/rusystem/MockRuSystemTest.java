package uniruse.mse.examregistration.rusystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

public class MockRuSystemTest {

	@Test
	public void test_shouldReadTheCSVFile() throws IOException {
		final MockRuSystem mock = new MockRuSystem();

		assertTrue(mock.getAll().size() > 0);
	}

	@Test
	public void test_shouldFindStudentByFacultyNumber() {
		final MockRuSystem mock = new MockRuSystem();

		final RuStudentData student = mock.findByFacultyNumber("166355");
		assertEquals("Дамла Алтънай Ахмедова", student.getFullName());
		assertEquals(1, student.getStudyForm().intValue());
		assertEquals("Социална педагогика", student.getSpecialty());
		assertEquals("64", student.getGroupNumber());
	}
}
