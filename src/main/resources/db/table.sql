create table admins
(
    id               bigint                              not null
        primary key,
    created_users_id bigint                              not null,
    created_date     timestamp default CURRENT_TIMESTAMP null,
    constraint admins_ibfk_1
        foreign key (created_users_id) references users (id)
);

create index created_users_id
    on admins (created_users_id);

create table answer
(
    id              bigint auto_increment
        primary key,
    answer          varchar(255)                        not null,
    score           float                               null,
    subject_id      bigint                              not null,
    created_date    timestamp default CURRENT_TIMESTAMP null,
    creator_user_id bigint                              not null,
    constraint answer_ibfk_1
        foreign key (subject_id) references subject (id),
    constraint answer_ibfk_2
        foreign key (creator_user_id) references users (id)
);

create index creator_user_id
    on answer (creator_user_id);

create index subject_id
    on answer (subject_id);

create table channel
(
    id               bigint                              not null
        primary key,
    created_users_id bigint                              not null,
    created_date     timestamp default CURRENT_TIMESTAMP null,
    constraint channel_ibfk_1
        foreign key (created_users_id) references users (id)
);

create index created_users_id
    on channel (created_users_id);

create table sender
(
    id              bigint auto_increment
        primary key,
    sendStatus      varchar(50) null,
    startTime       varchar(50) null,
    sendCount       bigint      null,
    sendLimitCount  bigint      null,
    sendUser        bigint      null,
    notSendUser     bigint      null,
    messageId       bigint      null,
    admin_id        bigint      null,
    admin_messageId bigint      null
);

create table subject
(
    id              bigint auto_increment
        primary key,
    name            varchar(255)                               not null,
    security_key    varchar(255)                               not null,
    is_delete       int       default 0                        not null,
    quiz_type       enum ('MILLIY_SERTIFIKAT', 'ATTESTATSIYA') not null,
    created_date    timestamp default CURRENT_TIMESTAMP        null,
    update_date     timestamp default CURRENT_TIMESTAMP        null,
    created_user_id bigint                                     not null,
    constraint security_key
        unique (security_key),
    constraint subject_ibfk_1
        foreign key (created_user_id) references users (id)
);

create index created_user_id
    on subject (created_user_id);

create table user_statuses
(
    id           int auto_increment
        primary key,
    name         varchar(255)                        not null,
    created_date timestamp default CURRENT_TIMESTAMP null
);

create table users
(
    id                   bigint                              not null
        primary key,
    step                 varchar(1000)                       null,
    status_id            int                                 not null,
    username             varchar(255)                        not null,
    current_security_key varchar(255)                        null,
    created_date         timestamp default CURRENT_TIMESTAMP null,
    updated_date         timestamp default CURRENT_TIMESTAMP null,
    constraint users_ibfk_1
        foreign key (status_id) references user_statuses (id)
);

create index status_id
    on users (status_id);

create table user_answers
(
    id                     bigint primary key auto_increment,
    user_id                bigint not null,
    subject_id             bigint not null,
    subject_name           varchar(255),
    all_answer_count       int    not null,
    correct_answer_count   int,
    incorrect_answer_count int,
    ball                   float,
    percentage             float,
    incorrect_answers_list varchar(1000),
    allAnswersList         varchar(1000),
    foreign key (user_id) references users (id),
    foreign key (subject_id) references subject (id)
)
