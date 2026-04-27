SELECT t.id AS tenant_id, t.name AS tenant_name, t.package_id, p.name AS pkg_name, JSON_LENGTH(p.menu_ids) AS pkg_cnt
FROM system_tenant t LEFT JOIN system_tenant_package p ON t.package_id = p.id
WHERE t.name LIKE '%长辉%';

SELECT id, name, code, tenant_id FROM system_role
WHERE tenant_id IN (SELECT id FROM system_tenant WHERE name LIKE '%长辉%IBMS%');
