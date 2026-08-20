CREATE TABLE article
(
    id           BIGINT PRIMARY KEY,
    title        TEXT        NOT NULL,
    url          TEXT        NOT NULL,
    image_url    TEXT,
    news_site    TEXT        NOT NULL,
    summary      TEXT,
    published_at TIMESTAMPTZ NOT NULL,
    updated_at   TIMESTAMPTZ NOT NULL
);