USE plant_care;
SET NAMES utf8mb4;

START TRANSACTION;

UPDATE plant_category
SET
    name = CONVERT(BINARY CONVERT(name USING latin1) USING utf8mb4),
    category = CASE WHEN category IS NULL OR category = '' THEN category ELSE CONVERT(BINARY CONVERT(category USING latin1) USING utf8mb4) END,
    light_requirement = CASE WHEN light_requirement IS NULL OR light_requirement = '' THEN light_requirement ELSE CONVERT(BINARY CONVERT(light_requirement USING latin1) USING utf8mb4) END,
    water_requirement = CASE WHEN water_requirement IS NULL OR water_requirement = '' THEN water_requirement ELSE CONVERT(BINARY CONVERT(water_requirement USING latin1) USING utf8mb4) END,
    temperature_range = CASE WHEN temperature_range IS NULL OR temperature_range = '' THEN temperature_range ELSE CONVERT(BINARY CONVERT(temperature_range USING latin1) USING utf8mb4) END,
    humidity = CASE WHEN humidity IS NULL OR humidity = '' THEN humidity ELSE CONVERT(BINARY CONVERT(humidity USING latin1) USING utf8mb4) END,
    fertilization = CASE WHEN fertilization IS NULL OR fertilization = '' THEN fertilization ELSE CONVERT(BINARY CONVERT(fertilization USING latin1) USING utf8mb4) END,
    pruning = CASE WHEN pruning IS NULL OR pruning = '' THEN pruning ELSE CONVERT(BINARY CONVERT(pruning USING latin1) USING utf8mb4) END,
    common_diseases = CASE WHEN common_diseases IS NULL OR common_diseases = '' THEN common_diseases ELSE CONVERT(BINARY CONVERT(common_diseases USING latin1) USING utf8mb4) END,
    description = CASE WHEN description IS NULL OR description = '' THEN description ELSE CONVERT(BINARY CONVERT(description USING latin1) USING utf8mb4) END
WHERE name REGEXP 'å|æ|ç|è|é|ï|â';

UPDATE user
SET nickname = CONVERT(BINARY CONVERT(nickname USING latin1) USING utf8mb4)
WHERE nickname REGEXP 'å|æ|ç|è|é|ï|â';

COMMIT;
