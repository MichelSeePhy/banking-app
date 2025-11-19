alter table customers
    add created_at DATETIME default CURRENT_TIMESTAMP not null;

alter table customers
    add updated_at datetime null;