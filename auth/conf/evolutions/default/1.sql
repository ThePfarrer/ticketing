# --- !Ups
create table if not exists persons(
id serial not null,
email varchar not null,
password varchar  not null,
primary key(email)
);


# --- !Downs
drop table persons;
