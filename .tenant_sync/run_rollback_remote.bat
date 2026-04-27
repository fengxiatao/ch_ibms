@echo off
mysql -h192.168.1.126 -uroot -p123456 --default-character-set=utf8mb4 ch_ibms < e:\ch\.tenant_sync\rollback_phase1_remote.sql > e:\ch\.tenant_sync\rollback_remote.out 2> e:\ch\.tenant_sync\rollback_remote.err
echo EXITCODE=%ERRORLEVEL%
type e:\ch\.tenant_sync\rollback_remote.out
type e:\ch\.tenant_sync\rollback_remote.err
