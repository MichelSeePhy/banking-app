alter table users
    add keycloak_id varchar(36) not null unique;
alter table users
    drop column password;