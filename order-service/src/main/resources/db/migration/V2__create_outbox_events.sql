CREATE TABLE outbox_events (
                               id             UUID PRIMARY KEY,
                               aggregate_type VARCHAR(100)  NOT NULL,
                               aggregate_id   UUID          NOT NULL,
                               event_type     VARCHAR(100)  NOT NULL,
                               topic          VARCHAR(200)  NOT NULL,
                               payload        TEXT          NOT NULL,
                               occurred_at    TIMESTAMPTZ   NOT NULL,
                               published_at   TIMESTAMPTZ,
                               attempts       INTEGER       NOT NULL DEFAULT 0,
                               last_error     TEXT,
                               created_at     TIMESTAMPTZ   NOT NULL,
                               updated_at     TIMESTAMPTZ   NOT NULL
);

CREATE INDEX idx_outbox_pending ON outbox_events (created_at)
    WHERE published_at IS NULL;