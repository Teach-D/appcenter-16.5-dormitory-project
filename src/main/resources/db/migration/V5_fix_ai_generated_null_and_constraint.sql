UPDATE calender
SET ai_generated = false
WHERE ai_generated IS NULL;

ALTER TABLE calender
    MODIFY ai_generated BOOLEAN NOT NULL DEFAULT false;