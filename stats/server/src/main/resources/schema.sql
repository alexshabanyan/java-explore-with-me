drop table IF EXISTS hits CASCADE;

create TABLE IF NOT EXISTS hits (
    id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    app     varchar(100) NOT NULL,
    uri     varchar(100) NOT NULL,
    ip      varchar(100) NOT NULL,
    request_dt   timestamp NOT NULL
);