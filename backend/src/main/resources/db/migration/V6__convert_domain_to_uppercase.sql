-- Convert all lowercase domain values to uppercase to align with EnumType.STRING mappings
UPDATE symbols SET domain = UPPER(domain);
UPDATE synonyms SET domain = UPPER(domain);
UPDATE projects SET domain = UPPER(domain);
