create table if not exists posts(
                                    id bigserial primary key,
                                    title varchar(256) not null,
                                    text varchar not null,
                                    image blob,
                                    likes_count integer not null default 0);

create table if not exists comments(
                                    id bigserial primary key,
                                    post_id bigint not null references posts(id),
                                    text varchar not null);

create table if not exists tags(
                                    id bigserial primary key,
                                    name varchar(64) not null unique);

create table if not exists post_tags(
                                    post_id bigint not null references posts(id),
                                    tag_id bigint not null references tags(id));
