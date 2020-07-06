create table if not exists COUPON
(
    id bigint auto_increment not null,
    price int not null,
    expire_date_time datetime not null,
    created_at datetime,
    updated_at datetime,
    primary key(id)
);

create table if not exists COUPON_NUMBER
(
    id bigint auto_increment not null,
    number varchar(50) not null,
    coupon_id int not null,
    user_id int,
    use_flag bit,
    created_at datetime,
    updated_at datetime,
    primary key(id)
);
