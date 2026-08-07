-- Repairs UTF-8 text that was decoded as Windows-1252 before persistence.
-- Example: "YazÄ±lÄ±m" becomes "Yazılım".

CREATE OR REPLACE FUNCTION pg_temp.repair_utf8_mojibake(input_value TEXT)
RETURNS TEXT
LANGUAGE plpgsql
AS $$
DECLARE
    repaired TEXT := input_value;
    converted TEXT;
    pass INTEGER;
BEGIN
    IF repaired IS NULL THEN
        RETURN NULL;
    END IF;

    FOR pass IN 1..3 LOOP
        EXIT WHEN repaired !~ '[ÃÄÅÂâ]';
        BEGIN
            converted := convert_from(convert_to(repaired, 'WIN1252'), 'UTF8');
            EXIT WHEN converted = repaired;
            repaired := converted;
        EXCEPTION
            WHEN character_not_in_repertoire OR untranslatable_character THEN
                EXIT;
        END;
    END LOOP;

    RETURN repaired;
END;
$$;

DO $$
DECLARE
    column_record RECORD;
BEGIN
    FOR column_record IN
        SELECT table_schema, table_name, column_name
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND data_type IN ('character varying', 'character', 'text')
          AND table_name NOT IN ('flyway_schema_history', 'audit_logs')
    LOOP
        EXECUTE format(
            'UPDATE %I.%I SET %I = pg_temp.repair_utf8_mojibake(%I) '
            || 'WHERE %I IS NOT NULL AND %I ~ ''[ÃÄÅÂâ]''',
            column_record.table_schema,
            column_record.table_name,
            column_record.column_name,
            column_record.column_name,
            column_record.column_name,
            column_record.column_name
        );
    END LOOP;
END;
$$;
