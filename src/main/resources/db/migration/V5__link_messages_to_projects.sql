ALTER TABLE messages
    ADD COLUMN project_id BIGINT;

UPDATE messages
SET project_id = (
    SELECT MIN(id)
    FROM projects
)
WHERE project_id IS NULL;

ALTER TABLE messages
    ADD CONSTRAINT fk_messages_project
        FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE RESTRICT;

ALTER TABLE messages
    ALTER COLUMN project_id SET NOT NULL;

CREATE INDEX idx_messages_project_pending_latest
    ON messages (project_id, proposal_status, created_at DESC, id DESC);
