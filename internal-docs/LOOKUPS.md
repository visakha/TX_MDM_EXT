# zlv_guest lookups (checked 2026-11-27 00:28 CST)

Source: BE export zip (ext-docs) for the field -> Reference 360 code list mapping;
tx-mdm backend form-schema (live IDMC) for values. Lookups arrive in events as {Code, Name}.

The BE zip carries only the code-list DEFINITIONS (name + id), not the values.

| Field | Reference 360 code list | Values we have |
|---|---|---|
| touch_cd | ShipmentCompletionType (_ent_shipmentcompletiontype) | none |
| x_phone.ph_typ (mandatory) | PhoneType (_ent_phonetype) | none from live read (earlier note says 11 codes) |
| Address.addressType | AddressType (_ent_addresstype) | none |
| Address.usageType | AddressUsageType (_ent_addressusagetype) | none |
| Address.state (depends on country) | State (_ent_state) | 57, guessed from R360 |
| Address.country (mandatory) | Country (_ent_country) | 200, guessed from R360 |
| Address.addressStatus | AddressStatus (_ent_addressstatus) | none |
| Address.enrichedIndicator | AddressEnrichedIndicator | ERROR, NOT_VALIDATED, VALID (guessed) |
| x_src_address.state_cd | State | AZ only (seen in records) |
| x_src_address.address_status_cd | AddressStatus | none |
| x_src_address.address_typ | AddressType | AM2 = Alternate Mailing Address 2 (seen) |
| x_src_address.country_cd | Country | none |
| x_alt_id.alt_id_typ (mandatory) | AlternateIdType | TIN = Tax Id Number (seen) |
| x_alt_id.pri_segment_typ | SegmentValue | none |
| x_alt_id.sec_segment_typ | SegmentType | none |

Gap: full allowed values need an export of the Reference 360 code lists (code + name),
or a working R360 code-value API read. Until then the history tables store whatever
Code/Name the event carries; no validation against a list.
