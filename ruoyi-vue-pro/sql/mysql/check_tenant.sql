-- 检查租户数据

USE ch_ibms;

-- 1. 查询所有租户
SELECT 
    id, name, contact_name, contact_mobile, status, website,
    creator, create_time
FROM system_tenant
ORDER BY id;

-- 2. 查询"长辉信息"租户
SELECT * FROM system_tenant WHERE name = '长辉信息';

-- 3. 查询租户套餐
SELECT 
    t.id AS tenant_id,
    t.name AS tenant_name,
    tp.name AS package_name,
    t.status,
    t.website
FROM system_tenant t
LEFT JOIN system_tenant_package tp ON t.package_id = tp.id
ORDER BY t.id;

-- 4. 检查租户相关的管理员账号
SELECT 
    u.id, u.username, u.nickname, u.tenant_id,
    t.name AS tenant_name
FROM system_users u
LEFT JOIN system_tenant t ON u.tenant_id = t.id
WHERE u.username = 'admin'
ORDER BY u.tenant_id;

















