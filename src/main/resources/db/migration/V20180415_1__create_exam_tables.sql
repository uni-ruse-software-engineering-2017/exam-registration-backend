CREATE TABLE exam
(
   id bigserial, 
   start_time timestamp not null,
   end_time timestamp not null,
   hall character varying(200) not null, 
   max_seats int not null,
   subject_id bigint REFERENCES subject (id),
   professor_id bigint REFERENCES application_user (id),
   created_on timestamp not null,
   modified_on timestamp not null,

   CONSTRAINT exam_pk PRIMARY KEY (id),
);

create table exam_participation_request
(
   id bigserial,
   exam_id bigint REFERENCES exam (id),
   student_id bigint REFERENCES application_user (id),
   status character varying(20) not null,
   
   CONSTRAINT exam_participation_request_pk PRIMARY KEY (id),
);