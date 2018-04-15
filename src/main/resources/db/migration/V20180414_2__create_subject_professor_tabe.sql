CREATE TABLE subject_professor
(
   subject_id bigint REFERENCES subject (id),
   user_id bigint REFERENCES application_user (id)
);