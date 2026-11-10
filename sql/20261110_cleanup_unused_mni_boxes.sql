-- Run against the intended cpck database. Only the two verified unused rows
-- from 2026-10-30 are eligible. Inventory and operation records are untouched.
-- Keep this backup table until the cleanup has been verified.
CREATE TABLE IF NOT EXISTS product_ids_backup_20261110_mni LIKE product_ids;

START TRANSACTION;

INSERT IGNORE INTO product_ids_backup_20261110_mni
    (box_text, box_number, quantity, depository_id, is_stocked, iter, creation_time)
SELECT p.box_text, p.box_number, p.quantity, p.depository_id,
       p.is_stocked, p.iter, p.creation_time
FROM product_ids p
WHERE p.depository_id = 2 AND p.iter = 1 AND p.quantity = 3000
  AND p.is_stocked IS NULL
  AND ((p.box_text = 'ZMNIA' AND p.box_number = '1002'
        AND p.creation_time = '2026-10-30 08:12:52')
    OR (p.box_text = 'ZMNIB' AND p.box_number = '1003'
        AND p.creation_time = '2026-10-30 08:13:05'))
  AND NOT EXISTS (
      SELECT 1 FROM bearing_records r
      WHERE REPLACE(r.box_text, 'Z', '') = REPLACE(p.box_text, 'Z', '')
        AND CAST(r.box_number AS UNSIGNED) = CAST(p.box_number AS UNSIGNED)
        AND r.iter = p.iter
  );

DELETE p FROM product_ids p
JOIN product_ids_backup_20261110_mni backup
  ON backup.box_text = p.box_text AND backup.box_number = p.box_number
 AND backup.depository_id = p.depository_id AND backup.iter = p.iter
 AND backup.quantity = p.quantity AND backup.creation_time = p.creation_time
 AND backup.is_stocked IS NULL
WHERE p.depository_id = 2 AND p.iter = 1 AND p.quantity = 3000
  AND p.is_stocked IS NULL
  AND ((p.box_text = 'ZMNIA' AND p.box_number = '1002'
        AND p.creation_time = '2026-10-30 08:12:52')
    OR (p.box_text = 'ZMNIB' AND p.box_number = '1003'
        AND p.creation_time = '2026-10-30 08:13:05'))
  AND NOT EXISTS (
      SELECT 1 FROM bearing_records r
      WHERE REPLACE(r.box_text, 'Z', '') = REPLACE(p.box_text, 'Z', '')
        AND CAST(r.box_number AS UNSIGNED) = CAST(p.box_number AS UNSIGNED)
        AND r.iter = p.iter
  );

SELECT ROW_COUNT() AS deleted_rows;
COMMIT;

SELECT * FROM product_ids_backup_20261110_mni ORDER BY box_text;

-- Recovery, if needed (fails safely if either original key already exists):
-- INSERT INTO product_ids
--     (box_text, box_number, quantity, depository_id, is_stocked, iter, creation_time)
-- SELECT box_text, box_number, quantity, depository_id, is_stocked, iter, creation_time
-- FROM product_ids_backup_20261110_mni;
