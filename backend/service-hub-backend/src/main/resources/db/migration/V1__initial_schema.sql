CREATE TABLE platform_metadata
(
    id          UUID PRIMARY KEY,
    app_name    VARCHAR(100)             NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL
);