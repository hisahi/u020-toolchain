
# Univtek 020 (U020) Toolchain

A full U020 toolchain, comprising an emulator with a GUI which will also include a debugger, assembler and disassembler. 

This project uses Java and Maven, including JUnit unit tests and some other tools to test and debug the code.

See the "doc" folder for a full documentation.

# Ohjelmistotekniikan menetelmät

Tämä repositorio ja sen alla oleva ohjelma on tehty kevätlukukauden 2018 (2017-2018 periodi IV) Ohjelmistotekniikan menetelmät -kurssin harjoitustyönä. Kaikki suoraan OTM-kirjanpitoon liittyvät asiakirjat löytyykin dokumentaatio-kansion alta.

* [Vaatimusmäärittely](https://github.com/hisahi/u020-toolchain/blob/master/dokumentaatio/vaatimusmaarittely.md)
* [Tuntikirjanpito](https://github.com/hisahi/u020-toolchain/blob/master/dokumentaatio/tuntikirjanpito.md)
* [Luokkakaavio](https://github.com/hisahi/u020-toolchain/blob/master/dokumentaatio/luokkakaaviot.md)
* [Sekvenssikaavioita](https://github.com/hisahi/u020-toolchain/blob/master/dokumentaatio/sekvenssikaaviot.md)
* [Arkkitehtuuri](https://github.com/hisahi/u020-toolchain/blob/master/dokumentaatio/arkkitehtuuri.md)
* [Testausdokumentti](https://github.com/hisahi/u020-toolchain/blob/master/dokumentaatio/testaus.md)
* [Lyhyt käyttöohje](https://github.com/hisahi/u020-toolchain/blob/master/dokumentaatio/kayttoohje.md) (täysi käyttöohje on englanniksi doc-kansiossa)

## Julkaisut

* [v0.7](https://github.com/hisahi/u020-toolchain/releases/tag/v0.7)
* [v0.6](https://github.com/hisahi/u020-toolchain/releases/tag/v0.6)
* [v0.5](https://github.com/hisahi/u020-toolchain/releases/tag/v0.5)

## Komentorivitoiminnot

### Testaus

Testaus suoritetaan komennolla `mvn test` ja testikattavuusraportin luonti komennolla `mvn test jacoco:report`. Raportti tulee tiedostoon `target/site/jacoco/index.html`.

### .JAR-tiedoston luominen

JAR-tiedosto luodaan komennolla `mvn package` ja tulee `target`-kansion alle.

### Javadoc

Javadoc luodaan komennolla `mvn javadoc:javadoc` ja löytyy tiedostona `target/site/apidocs/index.html`.

### Checkstyle

Checkstyle suoritetaan komennolla `mvn jxr:jxr checkstyle:checkstyle`. Se löytyy tiedostona `target/site/checkstyle.html`.
