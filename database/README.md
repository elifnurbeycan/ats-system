# Development Database

`ats_system_seed.dump` is an anonymized PostgreSQL 17 development database.
It contains demonstration records only and does not contain refresh tokens,
audit request/response bodies, CV metadata, or real candidate contact details.

Create an empty UTF-8 database with `template0` and restore the dump:

```powershell
& "C:\Program Files\PostgreSQL\17\bin\createdb.exe" `
  -h localhost -p 5432 -U postgres `
  -T template0 -E UTF8 ats_system

& "C:\Program Files\PostgreSQL\17\bin\pg_restore.exe" `
  -h localhost -p 5432 -U postgres `
  -d ats_system --no-owner --no-privileges -v `
  ".\database\ats_system_seed.dump"
```
