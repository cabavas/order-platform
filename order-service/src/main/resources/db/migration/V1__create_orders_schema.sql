-- ============================================================
-- V1: Schema inicial de pedidos
-- ============================================================

CREATE TABLE orders (
                        id           UUID PRIMARY KEY,
                        customer_id  UUID          NOT NULL,
                        status       VARCHAR(30)   NOT NULL,
                        total_amount NUMERIC(19,2) NOT NULL,
                        currency     VARCHAR(3)    NOT NULL DEFAULT 'BRL',
                        created_at   TIMESTAMPTZ   NOT NULL,
                        updated_at   TIMESTAMPTZ   NOT NULL,
                        version      BIGINT        NOT NULL DEFAULT 0
);

CREATE TABLE order_items (
                             id           UUID PRIMARY KEY,
                             order_id     UUID          NOT NULL,
                             product_id   UUID          NOT NULL,
                             product_name VARCHAR(255)  NOT NULL,
                             quantity     INTEGER       NOT NULL CHECK (quantity > 0),
                             unit_price   NUMERIC(19,2) NOT NULL,
                             subtotal     NUMERIC(19,2) NOT NULL,
                             CONSTRAINT fk_order_items_order
                                 FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
);

CREATE INDEX idx_orders_customer_id ON orders (customer_id);
CREATE INDEX idx_orders_status      ON orders (status);
CREATE INDEX idx_orders_created_at  ON orders (created_at DESC);
CREATE INDEX idx_order_items_order  ON order_items (order_id);

COMMENT ON TABLE orders      IS 'Pedidos criados pelos clientes';
COMMENT ON TABLE order_items IS 'Itens que compõem cada pedido';