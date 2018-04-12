
# Luokkakaaviot
[Löytyy täältä](luokkakaaviot.md)

# Sekvenssikaaviot
[Löytyy täältä](sekvenssikaaviot.md)

# Pakkaukset
Pakkausten rakenne koostuu useasta eri logiikkapaketista, tiedostohallintapaketista ja käyttöliittymäpaketista, muttei kuitenkaan luultavammin noudata perinteistä kolmitasoista pakettiarkkitehtuuria.

* `com.github.hisahi.u020toolchain.ui` on käyttöliittymäpakkaus, joka sisältää kaikki käyttöliittymät sekä niihin liittyvät luokat. Paketin alla on myös kansainvälistämiseen keskittyvä I18n-luokka sekä asetusrajapintana toimiva Config-luokka.
* `com.github.hisahi.u020toolchain.file` on sekä kieli- että asetustiedostot käsittelevä pakkaus.
* Logiikkapakkaukset:
  * `com.github.hisahi.u020toolchain.logic` sisältää luokat, joita ei voitu jaotella muihin pakkauksiin. Kyseinen pakkaus sisältää muun muassa suorittimen kellona toimivan ajastinluokan, kääntäjäluokat sekä joitain rajapintoja.
  * `com.github.hisahi.u020toolchain.hardware` sisältää laitteistorajapinnan (interface) sekä toteutukset lisälaitteistoille.
  * `com.github.hisahi.u020toolchain.cpu` sisältää itse tietokoneen ja suorittimen toteutuksen. Sen ohessa ovat `addressing`- ja `instructions`-pakkaukset, jotka sisältävät toteutukset jokaiselle käskyn tiedonosoitusmoodille sekä operaatiolle.

# Käyttöliittymä
Käyttöliittymän viisi erilaista näkymää löytyvät jokainen omista ikkunoistaan.

Päänäkymä toimii emulaattorin pääasiallisena ikkuna, sisältäen emuloidun tietokoneen virtuaalisen näytön sekä sallien näppäimistön ja mahdollisesti hiiren kautta tehtävän tiedon syötön. Kyseinen ikkuna sisältää myös valikot, joiden kautta päästään toimintoihin ja muihin näkymiin.

Asetusikkuna on toinen näkymä ja sisältää asetukset, joista jokainen tallennetaan pysyvästi. Asetuksiin lukeutuu esimerkiksi emuloitavan suorittimen nopeus, mahdolliset lisälaitteet sekä näytön koon.

Virheenkorjaus- ja debug-konsoli sisältää komentoja, joilla voi tutkia laitteen tilaa reaaliajassa. Tämä ikkuna on suunniteltu pääosin kehittäjille, jotka haluavat kokeilla ohjelmiensa sekä itse emulaattorin toimintaa. 

Neljäs ja viides, konekielelle ja konekieleltä kääntäjät, ovat toisiaan muistuttavia, mutta silti erillisiä, ikkunoita. Kyseisillä ikkunoilla on käytössä `TabPane`-luokalla toteutetut välilehtinäkymät. Osa välilehdistä toimivat syötteinä ja toiset tulosteina.

Toteutukseltaan jokainen näkymä on oma `Stage`-olionsa, jotka sisältävät jokainen omat `Stage`-olionsa. Käyttöliittymän valikoiden ja nappien toiminto kutsuu allaolevia luokkia, mutta saattaa myös itse avata muita käyttöliittymiä, kuten tiedostonvalintaikkunoita. Logiikkaluokat myös voivat kutsua joitain käyttöliittymäpakkauksissa olevia metodeita päivittääkseen käyttöliittymän tilaa. Esimerkkinä tästä on tekstin kopiointivaihtoehto muokkausvalikossa, jota voi painaa ainoastaan, kun näyttö on tekstitilassa. Kun näytön tilaa vaihdetaan, näyttöluokka kutsuu käyttöliittymäluokan metodia, joka päivittää kopiointinapin tilan `setDisabled`-metodilla.

# Tiedostot
## Asetusten tallennus
Asetukset tallennetaan suorituskansioon tiedostona nimeltä `settings.json`, joka tiedostopäätteen tapaisesti on JSON-muodossa. Sovelluksen käynnistyessä asetukset ladataan kyseisestä tiedostosta, jos se on olemassa. Tiedoston käsittelystä vastaa luokka `ConfigFileHandler`, joka löytyy `com.github.hisahi.u020toolchain.file`-pakkauksesta. Asetukset tallennetaan automaattisesti niitä muokattaessa. 

## Ohjelmakuvat
Tiedosto-valikon alta löytyvä ohjelmakuvien lataus lataa binääritiedoston, siirtää sen virtuaalikoneen muistiin ja nollaa virtuaalikoneen tilan. Ladattavan binääritiedoston koko tavuissa on oltava parillinen, sillä virtuaalikoneen muisti koostuu 16-bittisistä sanoista. Näiden sanojen merkitsevämpi tavu säilötään tiedostossa ensin, eli muotona on niin sanottu _big endian_. Esimerkkinä tavu, jossa on neljä tavua `11 22 33 44`, käsiteltäisiin kahtena sanana `1122 3344`. Ohjelmakuvien suurin sallittu pituus on 64 (65 536) kilosanaa, eli 128 (131 072) kilotavua.

## Levykuvat
Levykuvien muoto on samankaltainen kuin ohjelmakuvien, mutta niiden pituus täytyy olla tasan 720 (737 280) kilosanaa, eli 1440 (1 474 560) kilotavua.

# Mahdolliset heikkoudet
Kaikki käyttöliittymän suunnittelu on tällä hetkellä Java-koodissa eikä esimerkiksi FXML-koodina.
