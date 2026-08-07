-- İK, şirketin işe alım operasyonlarının sahibidir. Yeni ve mevcut şirketlerde
-- aktif tüm uygulama yetkilerini alır. ON CONFLICT migration'ı tekrar çalıştırmaya karşı korur.
INSERT INTO role_permissions (role_id, permission_id)
SELECT role.id, permission.id
FROM roles role
CROSS JOIN permissions permission
WHERE role.code = 'HR'
  AND role.active = TRUE
  AND permission.active = TRUE
ON CONFLICT (role_id, permission_id) DO NOTHING;
