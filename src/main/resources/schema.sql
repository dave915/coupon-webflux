create table if not exists COUPON
(
    id bigint auto_increment not null,
    price int not null,
    expire_date_time datetime not null,
--     created_at datetime,
--     updated_at datetime,
    primary key(id)
);
