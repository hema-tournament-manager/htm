package nl.malienkolders.htm.lib

import model._

object CountryImporter {

  def doImport {
    if (Country.count == 0) {
      // code generated from http://en.wikipedia.org/wiki/ISO_3166-1
      // hasViewerFlag field manually set from flags in htm-viewer-jme
      val noCountry = Country.create.code2("").code3("").name("Unknown").hasFlag(false).hasViewerFlag(false)
      noCountry.save
      noCountry.reload
      Country.create.code2("AF").code3("AFG").name("Afghanistan").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("AX").code3("ALA").name("Åland Islands").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("AL").code3("ALB").name("Albania").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("DZ").code3("DZA").name("Algeria").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("AS").code3("ASM").name("American Samoa").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("AD").code3("AND").name("Andorra").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("AO").code3("AGO").name("Angola").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("AI").code3("AIA").name("Anguilla").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("AQ").code3("ATA").name("Antarctica").hasFlag(false).hasViewerFlag(false).save
      Country.create.code2("AG").code3("ATG").name("Antigua and Barbuda").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("AR").code3("ARG").name("Argentina").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("AM").code3("ARM").name("Armenia").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("AW").code3("ABW").name("Aruba").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("AU").code3("AUS").name("Australia").hasFlag(true).hasViewerFlag(true).save
      Country.create.code2("AT").code3("AUT").name("Austria").hasFlag(true).hasViewerFlag(true).save
      Country.create.code2("AZ").code3("AZE").name("Azerbaijan").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("BS").code3("BHS").name("Bahamas").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("BH").code3("BHR").name("Bahrain").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("BD").code3("BGD").name("Bangladesh").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("BB").code3("BRB").name("Barbados").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("BY").code3("BLR").name("Belarus").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("BE").code3("BEL").name("Belgium").hasFlag(true).hasViewerFlag(true).save
      Country.create.code2("BZ").code3("BLZ").name("Belize").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("BJ").code3("BEN").name("Benin").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("BM").code3("BMU").name("Bermuda").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("BT").code3("BTN").name("Bhutan").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("BO").code3("BOL").name("Bolivia, Plurinational State of").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("BQ").code3("BES").name("Bonaire, Sint Eustatius and Saba").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("BA").code3("BIH").name("Bosnia and Herzegovina").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("BW").code3("BWA").name("Botswana").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("BV").code3("BVT").name("Bouvet Island").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("BR").code3("BRA").name("Brazil").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("IO").code3("IOT").name("British Indian Ocean Territory").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("BN").code3("BRN").name("Brunei Darussalam").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("BG").code3("BGR").name("Bulgaria").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("BF").code3("BFA").name("Burkina Faso").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("BI").code3("BDI").name("Burundi").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("KH").code3("KHM").name("Cambodia").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("CM").code3("CMR").name("Cameroon").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("CA").code3("CAN").name("Canada").hasFlag(true).hasViewerFlag(true).save
      Country.create.code2("CV").code3("CPV").name("Cape Verde").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("KY").code3("CYM").name("Cayman Islands").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("CF").code3("CAF").name("Central African Republic").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("TD").code3("TCD").name("Chad").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("CL").code3("CHL").name("Chile").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("CN").code3("CHN").name("China").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("CX").code3("CXR").name("Christmas Island").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("CC").code3("CCK").name("Cocos (Keeling) Islands").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("CO").code3("COL").name("Colombia").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("KM").code3("COM").name("Comoros").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("CG").code3("COG").name("Congo").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("CD").code3("COD").name("Congo, the Democratic Republic of the").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("CK").code3("COK").name("Cook Islands").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("CR").code3("CRI").name("Costa Rica").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("CI").code3("CIV").name("Côte d'Ivoire").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("HR").code3("HRV").name("Croatia").hasFlag(true).hasViewerFlag(true).save
      Country.create.code2("CU").code3("CUB").name("Cuba").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("CW").code3("CUW").name("Curaçao").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("CY").code3("CYP").name("Cyprus").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("CZ").code3("CZE").name("Czech Republic").hasFlag(true).hasViewerFlag(true).save
      Country.create.code2("DK").code3("DNK").name("Denmark").hasFlag(true).hasViewerFlag(true).save
      Country.create.code2("DJ").code3("DJI").name("Djibouti").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("DM").code3("DMA").name("Dominica").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("DO").code3("DOM").name("Dominican Republic").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("EC").code3("ECU").name("Ecuador").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("EG").code3("EGY").name("Egypt").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("SV").code3("SLV").name("El Salvador").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("GQ").code3("GNQ").name("Equatorial Guinea").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("ER").code3("ERI").name("Eritrea").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("EE").code3("EST").name("Estonia").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("ET").code3("ETH").name("Ethiopia").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("FK").code3("FLK").name("Falkland Islands (Malvinas)").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("FO").code3("FRO").name("Faroe Islands").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("FJ").code3("FJI").name("Fiji").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("FI").code3("FIN").name("Finland").hasFlag(true).hasViewerFlag(true).save
      Country.create.code2("FR").code3("FRA").name("France").hasFlag(true).hasViewerFlag(true).save
      Country.create.code2("GF").code3("GUF").name("French Guiana").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("PF").code3("PYF").name("French Polynesia").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("TF").code3("ATF").name("French Southern Territories").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("GA").code3("GAB").name("Gabon").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("GM").code3("GMB").name("Gambia").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("GE").code3("GEO").name("Georgia").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("DE").code3("DEU").name("Germany").hasFlag(true).hasViewerFlag(true).save
      Country.create.code2("GH").code3("GHA").name("Ghana").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("GI").code3("GIB").name("Gibraltar").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("GR").code3("GRC").name("Greece").hasFlag(true).hasViewerFlag(true).save
      Country.create.code2("GL").code3("GRL").name("Greenland").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("GD").code3("GRD").name("Grenada").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("GP").code3("GLP").name("Guadeloupe").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("GU").code3("GUM").name("Guam").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("GT").code3("GTM").name("Guatemala").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("GG").code3("GGY").name("Guernsey").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("GN").code3("GIN").name("Guinea").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("GW").code3("GNB").name("Guinea-Bissau").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("GY").code3("GUY").name("Guyana").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("HT").code3("HTI").name("Haiti").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("HM").code3("HMD").name("Heard Island and McDonald Islands").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("VA").code3("VAT").name("Holy See (Vatican City State)").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("HN").code3("HND").name("Honduras").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("HK").code3("HKG").name("Hong Kong").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("HU").code3("HUN").name("Hungary").hasFlag(true).hasViewerFlag(true).save
      Country.create.code2("IS").code3("ISL").name("Iceland").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("IN").code3("IND").name("India").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("ID").code3("IDN").name("Indonesia").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("IR").code3("IRN").name("Iran, Islamic Republic of").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("IQ").code3("IRQ").name("Iraq").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("IE").code3("IRL").name("Ireland").hasFlag(true).hasViewerFlag(true).save
      Country.create.code2("IM").code3("IMN").name("Isle of Man").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("IL").code3("ISR").name("Israel").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("IT").code3("ITA").name("Italy").hasFlag(true).hasViewerFlag(true).save
      Country.create.code2("JM").code3("JAM").name("Jamaica").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("JP").code3("JPN").name("Japan").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("JE").code3("JEY").name("Jersey").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("JO").code3("JOR").name("Jordan").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("KZ").code3("KAZ").name("Kazakhstan").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("KE").code3("KEN").name("Kenya").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("KI").code3("KIR").name("Kiribati").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("KP").code3("PRK").name("Korea, Democratic People's Republic of").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("KR").code3("KOR").name("Korea, Republic of").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("KW").code3("KWT").name("Kuwait").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("KG").code3("KGZ").name("Kyrgyzstan").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("LA").code3("LAO").name("Lao People's Democratic Republic").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("LV").code3("LVA").name("Latvia").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("LB").code3("LBN").name("Lebanon").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("LS").code3("LSO").name("Lesotho").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("LR").code3("LBR").name("Liberia").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("LY").code3("LBY").name("Libya").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("LI").code3("LIE").name("Liechtenstein").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("LT").code3("LTU").name("Lithuania").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("LU").code3("LUX").name("Luxembourg").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("MO").code3("MAC").name("Macao").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("MK").code3("MKD").name("Macedonia, the former Yugoslav Republic of").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("MG").code3("MDG").name("Madagascar").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("MW").code3("MWI").name("Malawi").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("MY").code3("MYS").name("Malaysia").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("MV").code3("MDV").name("Maldives").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("ML").code3("MLI").name("Mali").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("MT").code3("MLT").name("Malta").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("MH").code3("MHL").name("Marshall Islands").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("MQ").code3("MTQ").name("Martinique").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("MR").code3("MRT").name("Mauritania").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("MU").code3("MUS").name("Mauritius").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("YT").code3("MYT").name("Mayotte").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("MX").code3("MEX").name("Mexico").hasFlag(true).hasViewerFlag(true).save
      Country.create.code2("FM").code3("FSM").name("Micronesia, Federated States of").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("MD").code3("MDA").name("Moldova, Republic of").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("MC").code3("MCO").name("Monaco").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("MN").code3("MNG").name("Mongolia").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("ME").code3("MNE").name("Montenegro").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("MS").code3("MSR").name("Montserrat").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("MA").code3("MAR").name("Morocco").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("MZ").code3("MOZ").name("Mozambique").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("MM").code3("MMR").name("Myanmar").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("NA").code3("NAM").name("Namibia").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("NR").code3("NRU").name("Nauru").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("NP").code3("NPL").name("Nepal").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("NL").code3("NLD").name("Netherlands").hasFlag(true).hasViewerFlag(true).save
      Country.create.code2("NC").code3("NCL").name("New Caledonia").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("NZ").code3("NZL").name("New Zealand").hasFlag(true).hasViewerFlag(true).save
      Country.create.code2("NI").code3("NIC").name("Nicaragua").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("NE").code3("NER").name("Niger").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("NG").code3("NGA").name("Nigeria").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("NU").code3("NIU").name("Niue").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("NF").code3("NFK").name("Norfolk Island").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("MP").code3("MNP").name("Northern Mariana Islands").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("NO").code3("NOR").name("Norway").hasFlag(true).hasViewerFlag(true).save
      Country.create.code2("OM").code3("OMN").name("Oman").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("PK").code3("PAK").name("Pakistan").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("PW").code3("PLW").name("Palau").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("PS").code3("PSE").name("Palestinian Territory, Occupied").hasFlag(false).hasViewerFlag(false).save
      Country.create.code2("PA").code3("PAN").name("Panama").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("PG").code3("PNG").name("Papua New Guinea").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("PY").code3("PRY").name("Paraguay").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("PE").code3("PER").name("Peru").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("PH").code3("PHL").name("Philippines").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("PN").code3("PCN").name("Pitcairn").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("PL").code3("POL").name("Poland").hasFlag(true).hasViewerFlag(true).save
      Country.create.code2("PT").code3("PRT").name("Portugal").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("PR").code3("PRI").name("Puerto Rico").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("QA").code3("QAT").name("Qatar").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("RE").code3("REU").name("Réunion").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("RO").code3("ROU").name("Romania").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("RU").code3("RUS").name("Russian Federation").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("RW").code3("RWA").name("Rwanda").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("BL").code3("BLM").name("Saint Barthélemy").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("SH").code3("SHN").name("Saint Helena, Ascension and Tristan da Cunha").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("KN").code3("KNA").name("Saint Kitts and Nevis").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("LC").code3("LCA").name("Saint Lucia").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("MF").code3("MAF").name("Saint Martin (French part)").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("PM").code3("SPM").name("Saint Pierre and Miquelon").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("VC").code3("VCT").name("Saint Vincent and the Grenadines").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("WS").code3("WSM").name("Samoa").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("SM").code3("SMR").name("San Marino").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("ST").code3("STP").name("Sao Tome and Principe").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("SA").code3("SAU").name("Saudi Arabia").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("SN").code3("SEN").name("Senegal").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("RS").code3("SRB").name("Serbia").hasFlag(true).hasViewerFlag(true).save
      Country.create.code2("SC").code3("SYC").name("Seychelles").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("SL").code3("SLE").name("Sierra Leone").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("SG").code3("SGP").name("Singapore").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("SX").code3("SXM").name("Sint Maarten (Dutch part)").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("SK").code3("SVK").name("Slovakia").hasFlag(true).hasViewerFlag(true).save
      Country.create.code2("SI").code3("SVN").name("Slovenia").hasFlag(true).hasViewerFlag(true).save
      Country.create.code2("SB").code3("SLB").name("Solomon Islands").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("SO").code3("SOM").name("Somalia").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("ZA").code3("ZAF").name("South Africa").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("GS").code3("SGS").name("South Georgia and the South Sandwich Islands").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("SS").code3("SSD").name("South Sudan").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("ES").code3("ESP").name("Spain").hasFlag(true).hasViewerFlag(true).save
      Country.create.code2("LK").code3("LKA").name("Sri Lanka").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("SD").code3("SDN").name("Sudan").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("SR").code3("SUR").name("Suriname").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("SJ").code3("SJM").name("Svalbard and Jan Mayen").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("SZ").code3("SWZ").name("Swaziland").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("SE").code3("SWE").name("Sweden").hasFlag(true).hasViewerFlag(true).save
      Country.create.code2("CH").code3("CHE").name("Switzerland").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("SY").code3("SYR").name("Syrian Arab Republic").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("TW").code3("TWN").name("Taiwan, Province of China").hasFlag(false).hasViewerFlag(false).save
      Country.create.code2("TJ").code3("TJK").name("Tajikistan").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("TZ").code3("TZA").name("Tanzania, United Republic of").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("TH").code3("THA").name("Thailand").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("TL").code3("TLS").name("Timor-Leste").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("TG").code3("TGO").name("Togo").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("TK").code3("TKL").name("Tokelau").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("TO").code3("TON").name("Tonga").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("TT").code3("TTO").name("Trinidad and Tobago").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("TN").code3("TUN").name("Tunisia").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("TR").code3("TUR").name("Turkey").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("TM").code3("TKM").name("Turkmenistan").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("TC").code3("TCA").name("Turks and Caicos Islands").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("TV").code3("TUV").name("Tuvalu").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("UG").code3("UGA").name("Uganda").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("UA").code3("UKR").name("Ukraine").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("AE").code3("ARE").name("United Arab Emirates").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("GB").code3("GBR").name("United Kingdom").hasFlag(true).hasViewerFlag(true).save
      Country.create.code2("US").code3("USA").name("United States").hasFlag(true).hasViewerFlag(true).save
      Country.create.code2("UM").code3("UMI").name("United States Minor Outlying Islands").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("UY").code3("URY").name("Uruguay").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("UZ").code3("UZB").name("Uzbekistan").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("VU").code3("VUT").name("Vanuatu").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("VE").code3("VEN").name("Venezuela, Bolivarian Republic of").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("VN").code3("VNM").name("Viet Nam").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("VG").code3("VGB").name("Virgin Islands, British").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("VI").code3("VIR").name("Virgin Islands, U.S.").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("WF").code3("WLF").name("Wallis and Futuna").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("EH").code3("ESH").name("Western Sahara").hasFlag(false).hasViewerFlag(false).save
      Country.create.code2("YE").code3("YEM").name("Yemen").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("ZM").code3("ZMB").name("Zambia").hasFlag(true).hasViewerFlag(false).save
      Country.create.code2("ZW").code3("ZWE").name("Zimbabwe").hasFlag(true).hasViewerFlag(false).save

      Participant.findAll.foreach(_.country(noCountry).save)
    }
  }

}