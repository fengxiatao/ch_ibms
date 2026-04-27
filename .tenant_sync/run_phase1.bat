@echo off
mysql -h192.168.1.126 -uroot -p123456 --default-character-set=utf8mb4 ch_ibms < e:\ch\.tenant_sync\phase1_sync.sql > e:\ch\.tenant_sync\phase1_run.out 2> e:\ch\.tenant_sync\phase1_run.err
echo EXITCODE=%ERRORLEVEL%
