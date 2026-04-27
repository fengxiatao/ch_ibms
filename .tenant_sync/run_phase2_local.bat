@echo off
mysql -h127.0.0.1 -uroot -p123456 --default-character-set=utf8mb4 ch_ibms < e:\ch\.tenant_sync\phase2_sync.sql > e:\ch\.tenant_sync\phase2_local.out 2> e:\ch\.tenant_sync\phase2_local.err
echo EXITCODE=%ERRORLEVEL%
echo ----- ERR -----
type e:\ch\.tenant_sync\phase2_local.err
echo ----- OUT -----
type e:\ch\.tenant_sync\phase2_local.out
