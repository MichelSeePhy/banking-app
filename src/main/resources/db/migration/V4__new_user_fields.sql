alter table users
    add active boolean default TRUE not null;

alter table users
    add created_at DATETIME default CURRENT_TIMESTAMP not null;

alter table users
    add updated_at datetime null;