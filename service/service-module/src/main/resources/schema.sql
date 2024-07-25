drop table IF EXISTS events_rating CASCADE;
drop table IF EXISTS requests CASCADE;
drop table IF EXISTS compilations CASCADE;
drop table IF EXISTS events CASCADE;
drop table IF EXISTS users CASCADE;
drop table IF EXISTS categories CASCADE;

create TABLE IF NOT EXISTS users (
    id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name    varchar(100) NOT NULL,
    email   varchar(320) NOT NULL UNIQUE,
    rating integer default 0
);

create TABLE IF NOT EXISTS categories (
    id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name    varchar(100) NOT NULL UNIQUE
);

create TABLE IF NOT EXISTS events (
    id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    annotation    text NOT NULL,
    category_id BIGINT,
    confirmed_requests integer default 0,
    created_on timestamp default current_timestamp,
    description text,
    event_date timestamp not null,
    user_id BIGINT not null,
    location varchar(100),
    paid boolean default false,
    participant_limit integer not null default 0,
    published_on timestamp,
    request_moderation boolean default true,
    state varchar(15),
    title varchar(100) NOT NULL,
    views integer default 0,
    rating integer default 0,

    CONSTRAINT fk_events_to_categories FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT fk_events_to_users FOREIGN KEY (user_id) REFERENCES users (id)
);

create TABLE IF NOT EXISTS requests (
    id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    created    timestamp default current_timestamp,
    status varchar(15),
    event_id bigint,
    user_id bigint,

    CONSTRAINT fk_requests_to_events FOREIGN KEY (event_id) REFERENCES events (id),
    CONSTRAINT fk_requests_to_users FOREIGN KEY (user_id) REFERENCES users (id)
);

create TABLE IF NOT EXISTS compilations (
    id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pinned    boolean default false,
    title   varchar(200) NOT NULL UNIQUE,
    event_id bigint,

    CONSTRAINT fk_compilations_to_events FOREIGN KEY (event_id) REFERENCES events (id)
);

create TABLE IF NOT EXISTS events_rating (
    id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id  bigint,
    event_id bigint,
    rating varchar(10),

    CONSTRAINT fk_events_rating_to_events FOREIGN KEY (event_id) REFERENCES events (id),
    CONSTRAINT fk_events_rating_to_users FOREIGN KEY (user_id) REFERENCES users (id)
);