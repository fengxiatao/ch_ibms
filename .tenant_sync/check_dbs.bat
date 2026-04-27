@echo off
echo ===== 127.0.0.1 =====
mysql -h127.0.0.1 -uroot -p123456 -e "SELECT @@hostname h, @@port p, (SELECT COUNT(*) FROM ch_ibms.iot_subsystem WHERE tenant_id=162 AND deleted=0) t162_sub, (SELECT COUNT(*) FROM ch_ibms.iot_subsystem WHERE tenant_id=1 AND deleted=0) t1_sub;"
echo ===== 192.168.1.126 =====
mysql -h192.168.1.126 -uroot -p123456 -e "SELECT @@hostname h, @@port p, (SELECT COUNT(*) FROM ch_ibms.iot_subsystem WHERE tenant_id=162 AND deleted=0) t162_sub, (SELECT COUNT(*) FROM ch_ibms.iot_subsystem WHERE tenant_id=1 AND deleted=0) t1_sub;"
