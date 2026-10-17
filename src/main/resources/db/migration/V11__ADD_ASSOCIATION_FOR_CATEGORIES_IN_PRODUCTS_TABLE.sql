ALTER TABLE products
    ADD product_id BIGINT;

ALTER TABLE products
    ADD CONSTRAINT FK_PRODUCTS_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES categories (id);