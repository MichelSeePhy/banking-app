create table customers
(
    id           bigint auto_increment primary key,
    name         varchar(100) not null,
    address      varchar(255) not null,
    phone_number varchar(25)  not null,
    type         varchar(25)  not null,
    constraint chk_customer_type check (type in ('PRIVATE', 'ORGANIZATION'))
);

create table user_customer
(
    user_id     bigint not null,
    customer_id bigint not null,
    primary key (user_id, customer_id),
    constraint customer_fk
        foreign key (customer_id) references customers (id) on delete cascade,
    constraint user_fk
        foreign key (user_id) references users (id) on delete cascade
);

ALTER TABLE users ADD COLUMN private_customer_id BIGINT;
ALTER TABLE users ADD CONSTRAINT fk_private_customer
    FOREIGN KEY (private_customer_id) REFERENCES customers(id) on delete set null;
ALTER TABLE users ADD CONSTRAINT uk_private_customer UNIQUE (private_customer_id);