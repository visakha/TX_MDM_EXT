# extract - Oracle tables to a zip, from the command line

Read-only. SELECT only, nothing is changed in Oracle.

Run from PowerShell in this folder:

    .\extract.ps1 -User <schema_user> -Tns <tns_alias>

- Asks for the password at the prompt. It is never written to disk or put on the command line.
- Default table: ZLV_MDM_EVENTS_STORE. More tables: -Tables ZLV_MDM_EVENTS_STORE,OTHER_TABLE
- Output: extract_out\<timestamp>\<TABLE>.dsv (pipe-delimited, header, no quotes) and extract_out\extract_<timestamp>.zip
- Needs sqlplus 12.2 or later on the PATH (SET MARKUP CSV).
- If PowerShell blocks the script: Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass

## Getting sqlplus going on Windows

Option A - Oracle Instant Client (smallest):
1. Download "Basic" (or "Basic Light") and "SQL*Plus" packages, same version, 19c or 21c+, from oracle.com Instant Client for Windows x64. Unzip both into the same folder, e.g. C:\oracle\instantclient_21_15
2. Set these for the current PowerShell window:

       $env:PATH = "C:\oracle\instantclient_21_15;" + $env:PATH
       $env:TNS_ADMIN = "C:\oracle\network\admin"      # folder holding tnsnames.ora (only if you use a TNS alias)
       $env:NLS_LANG = "AMERICAN_AMERICA.AL32UTF8"      # keeps JSON payload characters intact

   To make them permanent: setx PATH / setx TNS_ADMIN / setx NLS_LANG (new windows only).
3. Check: sqlplus -V

Option B - full Oracle client already installed: set ORACLE_HOME to the client folder and put %ORACLE_HOME%\bin first on PATH; TNS_ADMIN defaults to %ORACLE_HOME%\network\admin.

No tnsnames.ora? Use EZConnect for -Tns instead of an alias:

    .\extract.ps1 -User <schema_user> -Tns "<db_host>:1521/<service_name>"

Quick manual test (prompts for the password):

    sqlplus <schema_user>@<tns_alias_or_ezconnect>
    SQL> select count(*) from ZLV_MDM_EVENTS_STORE;
    SQL> exit

Common errors:
- ORA-12154 / ORA-12162: TNS alias not found -> check TNS_ADMIN or use EZConnect.
- ORA-12541 / ORA-12170: cannot reach host/port -> VPN or firewall.
- 'sqlplus' is not recognized -> PATH does not include the client folder.
- SP2-0158 unknown SET option "CSV" -> sqlplus older than 12.2; upgrade the client.

## Extract including the history tables (after ddl\V001 is run)

    .\extract.ps1 -User <schema_user> -Tns <tns_alias> -Tables ZLV_MDM_EVENTS_STORE,EVT_MASTER_HIST,EVT_MASTER_PHONE_HIST,EVT_MASTER_SRC_ADDR_HIST,EVT_MASTER_ADDR_HIST,EVT_MASTER_ALT_ID_HIST,EVT_FIELD_CHANGE,EVT_MEMBER_HIST,EVT_XREF_CHANGE,EVT_SOURCE_HIST
