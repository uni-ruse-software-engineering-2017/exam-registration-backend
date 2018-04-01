CREATE TABLE application_user
(
   id bigserial, 
   username character varying(100) not null, 
   password character varying(200) not null, 
   full_name character varying(1000) not null, 
   role character varying(20) not null, 
   CONSTRAINT user_pk PRIMARY KEY (id),
   CONSTRAINT username_unique UNIQUE (username)
);