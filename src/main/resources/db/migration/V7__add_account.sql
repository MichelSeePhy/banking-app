create table accounts
(
    id              bigint auto_increment
        primary key,
    number          varchar(34)    not null UNIQUE,
    type            VARCHAR(25)    NOT NULL CHECK (type IN ('DEBIT', 'CREDIT')),
    status          VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE', 'BLOCKED', 'CLOSED')),
    balance         decimal(19, 2) not null default 0.00,
    operation_limit decimal(19, 2) null,
    credit_limit    decimal(19, 2) null,
    interest        int            null,
    created_at      datetime                default current_timestamp not null,
    updated_at      datetime       null,
    customer_id     bigint         not null,
    constraint account_customers_id_fk
        foreign key (customer_id) references customers (id) ON DELETE RESTRICT
);

