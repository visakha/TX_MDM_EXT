# zlv_guest lookups - allowed values (read live 2026-11-27 00:31 CST)

Source: Reference 360 value entities rdm.value.be._ent_<list>, read live from his IDMC org
through the tx-mdm backend (GET /api/b360/entities/rdm.value.be._ent_<list>/records, read only).
Field -> list mapping comes from the BE export zip (ext-docs). Lookups arrive in events as {Code, Name}.
This supersedes the 00:28 CST version, which had no values for most lists.

Note: for several lists Code and Name are the same text (e.g. PhoneType). Codes are case sensitive
as stored (e.g. 'VoIP', 'EMP ID' with a space, 'ParentDUNS').

Test data run 2026-11-27 used only: ph_typ Mobile, Fax; alt_id_typ TIN. No write errors.

## ShipmentCompletionType (rdm.value.be._ent_shipmentcompletiontype) - 2 values

Used by: touch_cd

| Code | Name |
|---|---|
| Upon delivery of goods to the final destination - customer address | Upon delivery of goods to the final destination - customer address |
| Upon delivery of goods to the shipping company | Upon delivery of goods to the shipping company |

## PhoneType (rdm.value.be._ent_phonetype) - 11 values

Used by: x_phone.ph_typ (mandatory)

| Code | Name |
|---|---|
| Cell | Cell |
| Contaminated | Contaminated |
| Fax | Fax |
| Freephone | Freephone |
| LandLine | LandLine |
| Marine | Marine |
| Mobile | Mobile |
| Pager | Pager |
| Special | Special |
| Telephone | Telephone |
| VoIP | VoIP |

## AddressType (rdm.value.be._ent_addresstype) - 31 values

Used by: Address.addressType, x_src_address.address_typ

| Code | Name |
|---|---|
| AA | Authorized Representative Address |
| AM1 | Alternate Mailing Address 1 |
| AM2 | Alternate Mailing Address 2 |
| AM3 | Alternate Mailing Address 3 |
| AM4 | Alternate Mailing Address 4 |
| AM5 | Alternate Mailing Address 5 |
| AP1 | Alternate Physical Address 1 |
| AP2 | Alternate Physical Address 2 |
| AP3 | Alternate Physical Address 3 |
| AP4 | Alternate Physical Address 4 |
| AP5 | Alternate Physical Address 5 |
| APOT | Other Alternate Physical |
| CE | Current Employer |
| CM | Long Term Care Contract Manager Address |
| CO | Contact |
| CR | Current Residence |
| EX | Long Term Care Executor Address |
| JA | Jail |
| LE | Last Known Employer |
| LK | Last Known Residence |
| MA | Mailing Address |
| OP | Other Cash Payee Address |
| OT | Other |
| PA | Physical Address |
| PH | Phone Number |
| PN | Personal Needs Allowance Address |
| SA | Service Address |
| SD | Long Term Care Service Delivery Address |
| SR | Self Reported |
| TA | Taxpayer Identification Number Address |
| WA | Long Term Care Work Address |

## AddressUsageType (rdm.value.be._ent_addressusagetype) - 3 values

Used by: Address.usageType

| Code | Name |
|---|---|
| Business | Business |
| Home | Home |
| Personal | Personal |

## AddressStatus (rdm.value.be._ent_addressstatus) - 2 values

Used by: Address.addressStatus, x_src_address.address_status_cd

| Code | Name |
|---|---|
| Active | Active |
| Inactive | Inactive |

## AddressEnrichedIndicator (rdm.value.be._ent_addressenrichedindicator) - 3 values

Used by: Address.enrichedIndicator

| Code | Name |
|---|---|
| ERROR | Error |
| NOT_VALIDATED | Not Validated |
| VALID | Valid |

## AlternateIdType (rdm.value.be._ent_alternateidtype) - 9 values

Used by: x_alt_id.alt_id_typ (mandatory)

| Code | Name |
|---|---|
| DL | Driver License Number |
| DUNS | D&B ID |
| EMP ID | Employee ID |
| FEIN | Federal Employer Identification Number |
| IRS | IRS |
| ParentDUNS | ParentDUNS |
| Passport | Passport Number |
| SSN | Social Security Number |
| TIN | Tax Id Number |

## SegmentValue (rdm.value.be._ent_segmentvalue) - 3 values

Used by: x_alt_id.pri_segment_typ

| Code | Name |
|---|---|
| LMC | Lower Middle Class |
| UHC | Upper High Class |
| UMC | Upper Middle Class |

## SegmentType (rdm.value.be._ent_segmenttype) - 3 values

Used by: x_alt_id.sec_segment_typ

| Code | Name |
|---|---|
| Income | Income |
| Region | Region |
| Supplier Criticality | Supplier Criticality |

## State (rdm.value.be._ent_state) - 57 values

Used by: Address.state (depends on country), x_src_address.state_cd

| Code | Name |
|---|---|
| AK | Alaska |
| AL | Alabama |
| AR | Arkansas |
| AS | American Samoa |
| AZ | Arizona |
| CA | California |
| CO | Colorado |
| CT | Connecticut |
| DC | District of Columbia |
| DE | Delaware |
| FL | Florida |
| GA | Georgia |
| GU | Guam |
| HI | Hawaii |
| IA | Iowa |
| ID | Idaho |
| IL | Illinois |
| IN | Indiana |
| KS | Kansas |
| KY | Kentucky |
| LA | Louisiana |
| MA | Massachusetts |
| MD | Maryland |
| ME | Maine |
| MI | Michigan |
| MN | Minnesota |
| MO | Missouri |
| MP | Northern Mariana Islands |
| MS | Mississippi |
| MT | Montana |
| NC | North Carolina |
| ND | North Dakota |
| NE | Nebraska |
| NH | New Hampshire |
| NJ | New Jersey |
| NM | New Mexico |
| NV | Nevada |
| NY | New York |
| OH | Ohio |
| OK | Oklahoma |
| OR | Oregon |
| PA | Pennsylvania |
| PR | Puerto Rico |
| RI | Rhode Island |
| SC | South Carolina |
| SD | South Dakota |
| TN | Tennessee |
| TX | Texas |
| UM | United States Minor Outlying Islands |
| UT | Utah |
| VA | Virginia |
| VI | Virgin Islands |
| VT | Vermont |
| WA | Washington |
| WI | Wisconsin |
| WV | West Virginia |
| WY | Wyoming |

## Country (rdm.value.be._ent_country) - 251 values

Used by: Address.country (mandatory), x_src_address.country_cd

| Code | Name |
|---|---|
| AC | Ascension |
| AD | Andorra |
| AE | United Arab Emirates |
| AF | Afghanistan |
| AG | Antigua and Barbuda |
| AI | Anguilla |
| AL | Albania |
| AM | Armenia |
| AN | Netherlands Antilles |
| AO | Angola |
| AQ | Antarctica |
| AR | Argentina |
| AS | American Samoa |
| AT | Austria |
| AU | Australia |
| AW | Aruba |
| AX | Aland |
| AZ | Azerbaijan |
| BA | Bosnia and Herzegovina |
| BB | Barbados |
| BD | Bangladesh |
| BE | Belgium |
| BF | Burkina Faso |
| BG | Bulgaria |
| BH | Bahrain |
| BI | Burundi |
| BJ | Benin |
| BL | Saint Barthelemy |
| BM | Bermuda |
| BN | Brunei Darussalam |
| BO | Bolivia |
| BR | Brazil |
| BS | Bahamas |
| BT | Bhutan |
| BV | Bouvet Island |
| BW | Botswana |
| BY | Belarus |
| BZ | Belize |
| CA | Canada |
| CC | Cocos (Keeling) Islands |
| CD | Congo, the Democratic Republic |
| CF | Central African Republic |
| CG | Congo |
| CH | Switzerland |
| CI | Cote d'Ivoire |
| CK | Cook Islands |
| CL | Chile |
| CM | Cameroon |
| CN | China |
| CO | Colombia |
| CR | Costa Rica |
| CU | Cuba |
| CV | Cape Verde |
| CW | Curacao |
| CX | Christmas Island |
| CY | Cyprus |
| CZ | Czech Republic |
| DE | Germany |
| DJ | Djibouti |
| DK | Denmark |
| DM | Dominica |
| DO | Dominican Republic |
| DZ | Algeria |
| EC | Ecuador |
| EE | Estonia |
| EG | Egypt |
| EH | Western Sahara |
| ER | Eritrea |
| ES | Spain |
| ET | Ethiopia |
| FI | Finland |
| FJ | Fiji |
| FK | Falkland Islands(Islas Malvinas) |
| FM | Micronesia, Federated States |
| FO | Faroe Islands |
| FR | France |
| GA | Gabon |
| GB | United Kingdom |
| GD | Grenada |
| GE | Georgia |
| GF | French Guiana |
| GG | Guernsey |
| GH | Ghana |
| GI | Gibraltar |
| GL | Greenland |
| GM | Gambia |
| GN | Guinea |
| GP | Guadeloupe |
| GQ | Equatorial Guinea |
| GR | Greece |
| GS | South Georgia and the South Sandwich Islands |
| GT | Guatemala |
| GU | Guam |
| GW | Guinea-Bissau |
| GY | Guyana |
| HK | Hong Kong |
| HM | Heard Island and McDonald Islands |
| HN | Honduras |
| HR | Croatia |
| HT | Haiti |
| HU | Hungary |
| ID | Indonesia |
| IE | Ireland |
| IL | Israel |
| IM | Isle of Man |
| IN | India |
| IO | British Indian Ocean Territory |
| IQ | Iraq |
| IR | Iran, Islamic Republic of |
| IS | Iceland |
| IT | Italy |
| JE | Jersey |
| JM | Jamaica |
| JO | Jordan |
| JP | Japan |
| KE | Kenya |
| KG | Kyrgyzstan |
| KH | Cambodia |
| KI | Kiribati |
| KM | Comoros |
| KN | Saint Kitts and Nevis |
| KP | Korea,North |
| KR | Korea,South |
| KW | Kuwait |
| KY | Cayman Islands |
| KZ | Kazakhstan |
| LA | Lao People's Democratic Republic |
| LB | Lebanon |
| LC | Saint Lucia |
| LI | Liechtenstein |
| LK | Sri Lanka |
| LR | Liberia |
| LS | Lesotho |
| LT | Lithuania |
| LU | Luxembourg |
| LV | Latvia |
| LY | Libyan Arab Jamahiriya |
| MA | Morocco |
| MC | Monaco |
| MD | Moldova |
| ME | Montenegro |
| MF | Saint Martin |
| MG | Madagascar |
| MH | Marshall Islands |
| MK | Macedonia, the former Yugoslav Republic of |
| ML | Mali |
| MM | Myanmar |
| MN | Mongolia |
| MO | Macao |
| MP | Northern Mariana Islands |
| MQ | Martinique |
| MR | Mauritania |
| MS | Montserrat |
| MT | Malta |
| MU | Mauritius |
| MV | Maldives |
| MW | Malawi |
| MX | Mexico |
| MY | Malaysia |
| MZ | Mozambique |
| NA | Namibia |
| NC | New Caledonia |
| NE | Niger |
| NF | Norfolk Island |
| NG | Nigeria |
| NI | Nicaragua |
| NL | Netherlands |
| NO | Norway |
| NP | Nepal |
| NR | Nauru |
| NU | Niue |
| NZ | New Zealand |
| OM | Oman |
| PA | Panama |
| PE | Peru |
| PF | French Polynesia |
| PG | Papua New Guinea |
| PH | Philippines |
| PK | Pakistan |
| PL | Poland |
| PM | Saint Pierre and Miquelon |
| PN | Pitcairn |
| PR | Puerto Rico |
| PT | Portugal |
| PW | Palau |
| PY | Paraguay |
| QA | Qatar |
| RE | Reunion |
| RO | Romania |
| RS | Serbia |
| RU | Russian Federation |
| RW | Rwanda |
| SA | Saudi Arabia |
| SB | Solomon Islands |
| SC | Seychelles |
| SD | Sudan |
| SE | Sweden |
| SG | Singapore |
| SH | Saint Helena |
| SI | Slovenia |
| SJ | Svalbard and Jan Mayen |
| SK | Slovakia |
| SL | Sierra Leone |
| SM | San Marino |
| SN | Senegal |
| SO | Somalia |
| SR | Suriname |
| SS | South Sudan |
| ST | Sao Tome and Principe |
| SV | Ei Salvador |
| SX | Sint Maarten |
| SY | Syrian Arab Republic |
| SZ | Swaziland |
| TA | Tristan Da Cunha |
| TC | Turks and Caicos Islands |
| TD | Chad |
| TF | French Southern Territories |
| TG | Togo |
| TH | Thailand |
| TJ | Tajikistan |
| TK | Tokelau |
| TL | Timor-Leste |
| TM | Turkmenistan |
| TN | Tunisia |
| TO | Tonga |
| TR | Turkey |
| TT | Trinidad and Tobago |
| TV | Tuvalu |
| TW | Taiwan, Province of China |
| TZ | Tanzania, United Republic |
| UA | Ukraine |
| UG | Uganda |
| UM | United States Minor Outlying Islands |
| US | United States |
| UY | Uruguay |
| UZ | Uzbekistan |
| VA | Holy See (Vatican City State) |
| VC | Saint Vincent and the Grenadines |
| VE | Venezuela, Bolivian Republic of |
| VG | British Virgin Islands |
| VI | U.S. Virgin Islands |
| VN | Vietnam |
| VU | Vanuatu |
| WF | Wallis and Futuna |
| WS | Samoa |
| YE | Yemen |
| YT | Mayotte |
| YU | Yugoslavia |
| ZA | South Africa |
| ZM | Zambia |
| ZW | Zimbabwe |
