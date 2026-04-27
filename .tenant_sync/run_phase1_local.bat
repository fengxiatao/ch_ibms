@echo off
mysql -h127.0.0.1 -uroot -p123456 --default-character-set=utf8mb4 ch_ibms < e:\ch\.tenant_sync\phase1_sync.sql > e:\ch\.tenant_sync\phase1_local.out 2> e:\ch\.tenant_sync\phase1_local.err
echo EXITCODE=%ERRORLEVEL%
type e:\ch\.tenant_sync\phase1_local.err
echo ----- OUT -----
type e:\ch\.tenant_sync\phase1_local.out
