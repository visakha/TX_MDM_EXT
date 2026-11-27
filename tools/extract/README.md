# extract - Oracle tables to a zip, from the command line

Read-only. SELECT only, nothing is changed in Oracle.

Run from PowerShell in this folder:

    .\extract.ps1 -User <schema_user> -Tns <tns_alias>

- Asks for the password at the prompt. It is never written to disk or put on the command line.
- Default table: ZLV_MDM_EVENTS_STORE. More tables: -Tables ZLV_MDM_EVENTS_STORE,OTHER_TABLE
- Output: extract_out\<timestamp>\<TABLE>.dsv (pipe-delimited, header, no quotes) and extract_out\extract_<timestamp>.zip
- Needs sqlplus 12.2 or later on the PATH (SET MARKUP CSV).
- If PowerShell blocks the script: Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
