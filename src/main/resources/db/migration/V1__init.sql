create table users
(
    id           bigint auto_increment
        primary key,
    first_name   varchar(255) not null,
    last_name    varchar(255) not null,
    email        varchar(25)  not null,
    phone_number varchar(25)  not null,
    password     varchar(255) not null,
    role         varchar(20)  not null
);

