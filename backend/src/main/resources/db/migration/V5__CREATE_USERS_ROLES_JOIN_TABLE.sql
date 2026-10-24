ALTER TABLE users
    DROP COLUMN roles;

CREATE TABLE users_roles
(
    roles_id BIGINT NOT NULL,
    user_id  BIGINT NOT NULL,
    CONSTRAINT pk_users_roles PRIMARY KEY (roles_id, user_id)
);

ALTER TABLE users_roles
    ADD CONSTRAINT fk_users_roles_role
        FOREIGN KEY (roles_id) REFERENCES roles (id);

ALTER TABLE users_roles
    ADD CONSTRAINT fk_users_roles_user
        FOREIGN KEY (user_id) REFERENCES users (id);
