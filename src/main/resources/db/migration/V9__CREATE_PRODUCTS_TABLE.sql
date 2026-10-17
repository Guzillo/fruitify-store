CREATE TABLE products
(
    id             BIGINT         NOT NULL,
    description    VARCHAR(2048)  NOT NULL,
    name           VARCHAR(255)   NOT NULL,
    price          DECIMAL(19, 2) NOT NULL,
    stock_quantity INTEGER        NOT NULL,
    is_active      BOOLEAN        NOT NULL,
    image_url      VARCHAR(255),
    CONSTRAINT pk_products PRIMARY KEY (id)
);

ALTER TABLE products
    ADD CONSTRAINT uc_products_name UNIQUE (name);