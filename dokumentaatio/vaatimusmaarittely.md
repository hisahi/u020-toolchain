
# Vaatimusmäärittely

## Sovelluksen tarkoitus

Sovelluksen tarkoitus on toteuttaa täysimittainen emulaattori debug-työkaluineen ja konekielikääntäjineen kaikkineen Univtek U020-alustalle (fiktiivinen 80-luvun mikrotietokone).

## Käyttäjät

Sovelluksella ei ole sinänsä käyttäjärooleja. Peruskäyttäjä voi kuitenkin käyttää vain itse emulaatiotoimintoja, kun taas kehittäjä voi käyttää debuggaukseen ja ohjelmointiin suuntautuvia lisätoimintoja.

# Käyttöliittymäluonnos

Tähän ei ole kuvaa, mutta pääasiallinen käyttöliittymä tulee olemaan yksinkertainen ikkuna virtuaalinäytöllä sekä valikolla. Tämän lisäksi muita ikkunoita tulee olemaan asetusikkuna, debuggauskonsoli sekä erityiset ikkunat (kuin omanlaiset pienemmät ohjelmat) konekielelle ja konekielestä kääntämiselle.

# Toiminnallisuus

## Peruskäyttäjälle

* Ikkunassa tulee olla näytön ulostulo graafisessa muodossa alkuperäisellä resoluutiolla tai sen monikerralla
* Käyttäjän tulee pystyä kirjoittamaan näppäimistöllä niin, että emuloitu laitteisto saa näppäinpainallukset ja käsittelee ne
* Laitteen suorituksen pitää pystyä pysäyttämään väliaikaisesti ja jatkamaan sitä myöhemmin
* Käyttäjän tulee voida pystyä lisäämään tiedostoja virtuaalisiin levykeasemiin, jos käyttäjä on päättänyt lisätä laitteeseen niitä
* Asetusikkuna, josta voi valita mm. ikkunan koon (näytön resoluution monikerran), emulaationopeuden ja muita vaihtoehtoja
* Sovelluksen tulee tilakaappauksia eli _save stateja_, joilla laitteen tilan voi tallentaa tiedostoon ja palauttaa myöhemmin
* Sovelluksen tulee pystyä ainakin pyörittämään Univtek BASIC -tulkkia (tämä on merkittävä testi sovellukselle, etenkin CPU-tuelle)

## Arkkitehtuuri

* Tukea [UCPU-16](https://github.com/hisahi/u020-toolchain/blob/master/doc/specs/UCPU16.txt)-suoritinarkkitehtuuria, jonka ympärille tietokone rakentuu
* Tukea 256 KW muistia, vaikka kerrallaan  olevaa muistia on vain 64 KW (lisämuisti, bank switching -tekniikalla)
* Tukea [UNCD321](https://github.com/hisahi/u020-toolchain/blob/master/doc/specs/UNCD321.txt)-näyttöä
* Tukea [näppäimistöä](https://github.com/hisahi/u020-toolchain/blob/master/doc/specs/keyboard.txt)
* Tukea [kellolaitetta](https://github.com/hisahi/u020-toolchain/blob/master/doc/specs/clock.txt)
* Tukea [UNTM200-ajastinta](https://github.com/hisahi/u020-toolchain/blob/master/doc/specs/UNTM200.txt)
* Tukea 0-2 kopiota [M35FD-levykeasemasta](https://github.com/hisahi/u020-toolchain/blob/master/doc/specs/M35FD.txt) (käyttäjän päätettävissä)
* Tukea valinnaista [UNMS001-hiirtä](https://github.com/hisahi/u020-toolchain/blob/master/doc/specs/UNMS001.txt)
* Tukea valinnaista [UNAC810-PCM-äänikorttia](https://github.com/hisahi/u020-toolchain/blob/master/doc/specs/UNAC810.txt)

## Kehittäjälle

* Tarjota toiminnot UCPU-16-suoritinarkkitehtuurin symbolisesta konekielestä varsinaiselle konekielelle kääntämiseksi ja toisinpäin
* Tarjota debug-konsoli, jolla voi tutkia ohjelman suoritusta käsky kerrallaan, katsoen samalla rekistereiden ja muistin tilaa
    * Konsolista voi suorittaa myös käskyjä, muokata ohjelmaa, tutkia ohjelman koodia (kääntäminen symboliseksi lennossa...)

# Jatkokehitysideoita

* Paranna konekielikääntäjää (molempiin suuntiin). Suorita optimointeja? Tue lisää esikääntäjätoimintoja?
* Lisätä tuki koko näytön tilalle
* Lisätä tuki valinnaiselle [K8581-äänikortille](https://github.com/hisahi/u020-toolchain/blob/master/doc/specs/K8581.txt)
* Lisätä valinnaiselle [UNHD210-kovalevyasemalle](https://github.com/hisahi/u020-toolchain/blob/master/doc/specs/UNHD210.txt)
* Lisätä tuki mahdollisille muille lisälaitteille
* Optimoida emulaattoria itseään nopeammaksi






