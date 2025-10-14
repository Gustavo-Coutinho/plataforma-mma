-- V4__Adjust_quality_score_type.sql
-- Ajusta o tipo da coluna quality_score para double precision

ALTER TABLE user_face_templates
    ALTER COLUMN quality_score TYPE DOUBLE PRECISION
    USING quality_score::DOUBLE PRECISION;
