
-- Seed demo user (id=1) for testing
INSERT INTO users (id, email, display_name)
VALUES (1, 'demo@example.com', 'Demo User')
ON CONFLICT (id) DO NOTHING;

-- Seed inventory
INSERT INTO inventory (product_id, total_stock, reserved_stock)
VALUES ('SKU-BOOK-123', 10, 0)
ON CONFLICT (product_id) DO NOTHING;

INSERT INTO inventory (product_id, total_stock, reserved_stock)
VALUES ('SKU-MUG-456', 5, 0)
ON CONFLICT (product_id) DO NOTHING;
