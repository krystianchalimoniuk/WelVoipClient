# Co to jest?
Aplikacja stworzona na potrzeby pracy inżynierskiej. Jej głównym założeniem było umożliwienie połączenie Voice over IP z adaptacją kodeka do aktualnie panujących warunków sieciowych pomiędzy dwoma użytkownikami w sieci lokalnej.
# Zasada działania
Aplikacja oparta jest o bibliotekę android.net.rtp oraz o architekturę klient-serwer protokołu TCP. Po nawiązaniu połączenia VoIP jeden z uczestników rozmowy inicjuje testowanie możliwości łącza metodą Round Trip Delay poprzez wysłanie porcji danych na drugie urządzenia. Metoda ta pozwala oszacować dostępna przepustość na podstawie, której podczas połączenia ustawiany jest jeden z czterech dostępnych kodeków audio:
* AMR
* GSM-FR
* GSM-EFR
* G.711u

Do każdego z kodeków przypisana jest  wartość przepustowości zgodna z [cisco](www.ciscopress.com/articles/article.asp?p=357102). Podczas gdy warunki sieciowe ulegną zmianie, ustawiany jest kodek, który oferuje lepszą jakość (gdy warunki zmieniają się na lepsze), bądź ma niższe wymagania na przepustowość (gdy warunki sieciowe pogarszają się).
Aplikacja ma w sobie zaimplementowaną bazę danych SugarCRM, która przechowuje kontakty (imię i nazwisko, adres IP).
# Uprawnienia
```java
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```
# Aktywności
![MainActivity](https://scontent-waw1-1.xx.fbcdn.net/v/t34.18173-12/15978312_1408727519160553_1092735164_n.png?_nc_cat=0&oh=2c30538d788aafd336f261f943e4e59b&oe=5BA344FB)
 ![SettingsActivity](https://scontent-waw1-1.xx.fbcdn.net/v/t34.18173-12/16117650_1415585075141464_1725654258_n.png?_nc_cat=0&oh=568ae60e68e9eeb787b1fa47feee4607&oe=5BA302D9) 
![](https://scontent-waw1-1.xx.fbcdn.net/v/t34.18173-12/16359051_1427128400653798_1326292315_n.png?_nc_cat=0&oh=4708e85073776d8a0b9fccad7cd203e3&oe=5BA306B5)
 ![CallActivity](https://scontent-waw1-1.xx.fbcdn.net/v/t34.18173-12/16401959_1427128450653793_112361856_n.png?_nc_cat=0&oh=0a73d6a995f9a4ee2eeab8329596a506&oe=5BA348E1) 
 ![CallActivity](https://scontent-waw1-1.xx.fbcdn.net/v/t34.18173-12/15942352_1408231062543532_1387954132_n.png?_nc_cat=0&oh=a00acbfa50604e26ece33e0250e98d7e&oe=5BA3227F) 
