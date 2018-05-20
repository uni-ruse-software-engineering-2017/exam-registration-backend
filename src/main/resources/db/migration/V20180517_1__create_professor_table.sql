CREATE TABLE professor
(
   id bigserial, 
   phone_number character varying(20) null, 
   cabinet character varying(10) null, 
 
   user_id bigint references application_user,
   CONSTRAINT professor_pk PRIMARY KEY (id)
);