CREATE TABLE student
(
   faculty_number character(6) not null, 
   specialty character varying(100) not null, 
   group_number integer not null, 
   study_form character varying(20) not null, 
 
   user_id bigint references application_user,
   CONSTRAINT student_pk PRIMARY KEY (faculty_number)
);