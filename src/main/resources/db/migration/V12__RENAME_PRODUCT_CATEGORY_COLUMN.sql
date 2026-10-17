ALTER TABLE products
    RENAME COLUMN product_id TO category_id;

ALTER TABLE products
    ALTER COLUMN category_id SET NOT NULL;
