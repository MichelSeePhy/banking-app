create table transactions
(
    id               varchar(36)    not null primary key,
    source_account_id bigint        null,
    target_account_id bigint        null,
    transaction_date datetime       not null default current_timestamp,
    amount           decimal(19, 2) not null,
    type             varchar(25)    not null,
    constraint fk_source_account foreign key (source_account_id) references accounts(id),
    constraint fk_target_account foreign key (target_account_id) references accounts(id),
    constraint type_check
        check (transactions.type in ('TOP_UP', 'TRANSFER', 'WITHDRAW', 'INTEREST_CHARGE'))
);