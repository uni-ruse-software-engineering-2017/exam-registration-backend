CREATE TABLE "user"
(
   id bigserial, 
   username character varying(100), 
   password character varying(200), 
   full_name character varying(1000), 
   role character varying(20), 
   CONSTRAINT user_pk PRIMARY KEY (id)
);