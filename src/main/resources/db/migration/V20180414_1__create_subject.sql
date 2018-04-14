CREATE TABLE subject
(
   id bigserial, 
   name character varying(100) not null, 
   description character varying(200) not null,
   CONSTRAINT subject_pk PRIMARY KEY (id),
   CONSTRAINT name_unique UNIQUE (name)
);