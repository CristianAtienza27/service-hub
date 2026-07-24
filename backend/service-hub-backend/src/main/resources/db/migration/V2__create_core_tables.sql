CREATE TABLE tenant
(
    id          UUID PRIMARY KEY,
    name        VARCHAR(150)             NOT NULL,
    slug        VARCHAR(100)             NOT NULL UNIQUE,
    status      VARCHAR(30)              NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE app_user
(
    id              UUID PRIMARY KEY,
    tenant_id       UUID                     NOT NULL,
    email           VARCHAR(255)             NOT NULL UNIQUE,
    password_hash   VARCHAR(255)             NOT NULL,
    role            VARCHAR(50)              NOT NULL,
    active          BOOLEAN                  NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_app_user_tenant
        FOREIGN KEY (tenant_id)
            REFERENCES tenant (id)
);

CREATE TABLE business
(
    id              UUID PRIMARY KEY,
    tenant_id       UUID                     NOT NULL UNIQUE,
    display_name    VARCHAR(150)             NOT NULL,
    description     TEXT,
    phone           VARCHAR(30),
    email           VARCHAR(255),
    logo_url        VARCHAR(500),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_business_tenant
        FOREIGN KEY (tenant_id)
            REFERENCES tenant (id)
);

CREATE TABLE service_request
(
    id                  UUID PRIMARY KEY,
    tenant_id           UUID                     NOT NULL,
    customer_name       VARCHAR(150)             NOT NULL,
    customer_email      VARCHAR(255),
    customer_phone      VARCHAR(30),
    description         TEXT                     NOT NULL,
    status              VARCHAR(30)              NOT NULL,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_service_request_tenant
        FOREIGN KEY (tenant_id)
            REFERENCES tenant (id)
);

CREATE INDEX idx_app_user_tenant
    ON app_user (tenant_id);

CREATE INDEX idx_service_request_tenant
    ON service_request (tenant_id);

CREATE INDEX idx_service_request_tenant_status
    ON service_request (tenant_id, status);