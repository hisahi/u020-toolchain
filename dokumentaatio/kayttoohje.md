
[An English, more extensive version of the manual can be found here.](../doc/help.md)

# Käyttöohje

Emulaattorin käynnistämiseen riittää [.jar-tiedoston lataaminen](https://github.com/hisahi/u020-toolchain/releases), siirtäminen sopivaan kansioon ja sen ajaminen.

Ohjelma säilöö esimerkiksi asetustiedostot samaan kansioon kuin missä kyseinen .jar-tiedosto on, joten suositellaan sen siirtämistä johonkin omaan kansioonsa.

## Ohjelman käynnistäminen

.JAR-tiedoston voi avata suoraan kaksoisklikkaamalla tai komentotulkin kautta komennolla `java -jar u020-toolchain-versio.jar`. Ohjelma käyttää JavaFX-käyttöliittymiä, joten graafinen ympäristö vaaditaan.

## Emulaattorin käyttö

Emulaattoriohjelma käynnistyy itse emulaattorinäkymään, jossa on emuloitavan koneen virtuaalinäyttö sekä yläpuolella valikot.

## Huomioita

* Näppäimistö on suunniteltu yksinomaan yhdysvaltalaista näppäinasettelua varten. Kyseiseen näppäinasetteluun siirtymisen vaiheet riippuvat käyttöjärjestelmästä. Ohessa on ohjeet seuraaville käyttöjärjestelmille:
  * [Windows 7, Windows 8, Windows 8.1](https://support.microsoft.com/en-us/help/17424/windows-change-keyboard-layout). Lisättävä näppäinasettelu on "English (US)" tai "Englanti (Yhdysvallat)". Lisäämisen jälkeen voit käyttää kielivalikkoa.
  * [Windows 10](https://support.microsoft.com/en-us/help/4027670/windows-add-and-switch-input-and-display-language-preferences-in-windo). Lisättävä näppäinasettelu on "English (US)" tai "Englanti (Yhdysvallat)". Lisäämisen jälkeen voit käyttää kielivalikkoa.
  * [macOS](https://support.apple.com/kb/PH25311)
  * Linuxilla vaiheet riippuvat käytetystä työpöytäympäristöstä sekä ikkunointijärjestelmästä.

## BASIC-tulkki

Emulaattorin ensimmäisenä ohjelmana on aina BASIC-tulkki. Sitä voi käyttää muiden BASIC-tulkkien tapaan, joten sillä voi ohjelmoida suoraan yksinkertaisia ohjelmia tai käyttää laskimena. Tulkkina toimii Univtek BASIC, joka on vielä keskeneräinen BASIC-tulkki Univtek 020 -tietokoneelle. Esimerkiksi ohjelmien tallennus levykkeelle tai lataaminen levykkeeltä ovat toimintoja, joita tulkki ei vielä tue.

Kokeile esimerkiksi seuraavaa ohjelmaa, joka kysyy kahta lukua ja lisäävät ne yhteen:

```
10 REM SUMS TWO NUMBERS
20 PRINT "ENTER TWO NUMBERS"
30 PRINT "TO CALCULATE THEIR SUM"
40 INPUT A
50 INPUT B
60 PRINT "THE SUM IS",A+B
```

## Muiden ohjelmien suoritus

Jos sinulla on ohjelmakuva, voit suorittaa sen Tiedosto-valikon kautta klikkaamalla vaihtoehtoa "Avaa ja suorita". Kun tiedosto on valittu, se ladataan suorittimen muistiin automaattisesti ja suoritetaan.

Levykekuvalta lataaminen tapahtuu Tiedosto-valikon kautta syöttämällä levykekuva virtuaaliasemaan ja sitten BASIC-tulkin kautta komennolla `LOAD "*",0` (BASIC-tulkki ei vielä tue `LOAD`-komentoa!). 

