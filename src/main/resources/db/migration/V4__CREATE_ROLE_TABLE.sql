CREATE SEQUENCE IF NOT EXISTS roles_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE roles
(
    id   BIGINT       NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT pk_roles PRIMARY KEY (id)
);

CREATE TABLE roles_privileges
(
    privileges_id BIGINT NOT NULL,
    role_id       BIGINT NOT NULL,
    CONSTRAINT pk_roles_privileges PRIMARY KEY (privileges_id, role_id)
);

ALTER TABLE roles_privileges
    ADD CONSTRAINT fk_rolpri_on_privilege FOREIGN KEY (privileges_id) REFERENCES privileges (id);

ALTER TABLE roles_privileges
    ADD CONSTRAINT fk_rolpri_on_role FOREIGN KEY (role_id) REFERENCES roles (id);