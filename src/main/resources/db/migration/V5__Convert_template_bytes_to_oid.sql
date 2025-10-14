-- V5__Convert_template_bytes_to_oid.sql
-- Converte a coluna template_bytes para OID para alinhar com o mapeamento Hibernate
-- Mantém dados existentes convertendo BYTEA para Large Object quando necessário

-- Cria função auxiliar para liberar LOs antigos antes de reatribuir ids (apenas se coluna já for OID)
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'user_face_templates'
          AND column_name = 'template_bytes'
          AND udt_name = 'oid'
    ) THEN
        -- Nenhuma conversão necessária
        RAISE NOTICE 'Coluna template_bytes já está em OID, nenhuma alteração aplicada.';
    ELSE
        ALTER TABLE user_face_templates
            ALTER COLUMN template_bytes DROP NOT NULL;

        ALTER TABLE user_face_templates
            ALTER COLUMN template_bytes TYPE OID
            USING CASE
                WHEN template_bytes IS NULL THEN NULL
                ELSE lo_from_bytea(0, template_bytes)
            END;

        ALTER TABLE user_face_templates
            ALTER COLUMN template_bytes SET NOT NULL;
    END IF;
END $$;
