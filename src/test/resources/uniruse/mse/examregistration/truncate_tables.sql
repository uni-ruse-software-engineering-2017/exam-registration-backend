SET FOREIGN_KEY_CHECKS=0;

TRUNCATE TABLE application_user;
ALTER TABLE application_user ALTER COLUMN id RESTART WITH 1;

TRUNCATE TABLE subject;
ALTER TABLE subject ALTER COLUMN id RESTART WITH 1;

TRUNCATE TABLE student;

TRUNCATE TABLE professor;

TRUNCATE TABLE subject_professor;

TRUNCATE TABLE exam_participation_request;

SET FOREIGN_KEY_CHECKS=1;