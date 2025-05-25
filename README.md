# Tier List Creator (Tasemeloendilooja)

**Rühmaliikmed:**

- Anri Sokolov - [anriwv](https://github.com/anriwv)
- Aleksei Ganyukov - [gan-alex](https://github.com/gan-alex)

## Kirjeldus

Tier list'i kasutatakse elementide järjestamiseks (näiteks tegelased, meeskonnad, esemed) nende tugevuse, populaarsuse või kvaliteedi järgi. Tulemuseks on nimekiri, kus elemendid on jaotatud tasemetesse (tiers), näiteks S, A, B, C jne, kus S-tase tähistab parimat ja järgnevad tasemed näitavad järkjärgulist langust.

TLC on Gradle abil ehitatud Java Swingi põhine töölaua rakendus. Rakendus võimaldab luua, hallata ja salvestada mitmeid tasemeloendeid. Iga tasemeloend sisaldab kohandatavaid tasemeid: kasutaja saab valida tasemete arvu (3 kuni 6) ja muuta nende nimesid. Tasemetel on vaikimisi fikseeritud värvid (S=Punane, A=Oranž jne) ning rakendus toetab hetkel tekstielementide lisamist, liigutamist (taseme sees, teise tasemesse, tagasi "Pakki") ja eemaldamist. Andmed salvestatakse kohalikku JSON-faili (`tierlists.json`), kasutades Gson'i serialiseerimiseks.

On plaanis migreerida andmete salvestamine tugevama andmebaasilahenduse poole ning lisada täiendavaid funktsionaalsusi nagu pilditugi.



## Funktsionaalsus

### Põhifunktsionaalsus:

1.  [X] Kasutaja saab luua ja kohandada erinevaid tasemeid (valida arv 3-6, muuta nimesid, vaikimisi värvid).
2.  [X] Kasutaja saab lisada uusi (teksti)elemente tasemeloendisse "Paki" kaudu.
3.  [X] Elemente saab liigutada (valikmenüü kaudu):
   *   Sama taseme sees vasakule/paremale.
   *   Ühest tasemest teise.
   *   Tasemelt tagasi "Pakki" (vt Märkused).
4.  [X] Graafiline kasutajaliides (Java Swing).
5.  [X] Elementidele saab lisada pilte või logosid (`images.dat` faili kaudu + arendamisel).
6.  [X] Andmete salvestamine ja laadimine (`tierlists.json` faili kaudu).

### Lisafunktsionaalsus:

-   [X] Mitme erineva tasemeloendi loomine ja haldamine.
-   [X] Värvide ja stiilide kohandamine (tasemetel on fikseeritud värvid, nimed on muudetavad).
-   [X] Double-click'i kaudu tasemeloendi avamine
-   [ ] Tagasivõtmise (undo) funktsioon
-   [ ] Elementide lohistamine (drag-and-drop) tasemete vahel
-   [ ] Koostöö võimaldamine lokaalses võrgus

### Tulevased Plaanid:

-   **Pildielementide tugi:** Piltide loendisse lisamise võimalus ja kuvamine.
-   **Andmebaasi migreerimine:** JSON asemel robustsema andmebaasi (nt SQLite) kasutamine.
-   **Lohistamine (Drag-and-Drop):** Elementide mugavam liigutamine hiirega.
-   **Versiooni ajaloo jälgimine:** Võimalus vaadata/taastada varasemaid versioone (mini-git sarnane).
-   **Täiendav kohandamine:** Võimalus muuta tasemete värve, fonte jms.


## Näidis

<p align="center">
  <img src="https://upload.wikimedia.org/wikipedia/commons/0/02/Tier_list_fruits.jpg" width="440">
</p>


## Projekti struktuur

```
TierListCreator/             // tlc
├── build.gradle             // dependencies, JVM arguments,configuration.
├── settings.gradle          // project name
├── README.md                
├── tierlists.json           // saves tier list data
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── tlc/
│   │               ├── Main.java                 
│   │               ├── model/
│   │               │   ├── TierList.java           
│   │               │   ├── Tier.java               // tier (e.g., S, A, B)
│   │               │   ├── TierItem.java           // abstraktne
│   │               │   ├── TextTierItem.java       
│   │               │   └── ImageTierItem.java      // arendamisel
│   │               ├── repository/
│   │               │   └── TierListRepository.java // Haldab kõiki TierList'e mälus
│   │               ├── service/
│   │               │   └── TierListService.java      
│   │               ├── ui/
│   │               │   ├── MainFrame.java            // gui
│   │               │   ├── TierListManagerPanel.java 
│   │               │   ├── TierListPanel.java        
│   │               │   ├── TierPanel.java            // ühe taseme rida
│   │               │   ├── TierHeaderPanel.java      //  Värviline taseme päis nime ja nupuga    
│   │               │   └── DeckPanel.java            // pakk, esemed lisamiseks
│   │               └── util/
│   │                   ├── PersistenceUtil.java     // JSON salvestamine/laadimine (Gson)
│   │                   └── RuntimeTypeAdapterFactory.java  // Gson abiklass polümorfismiks
│   └── test/
│       └── java/
│           └── com/
│               └── tlc/        // projekt näeb nii korralik välja, nagu AKT puu
└── build/                    
```

## Põhitöö selgtus

### Tier List Manager (Tasemeloendi Haldur)

Esialgne ekraan kuvab nimekirja kõigist salvestatud tasemeloenditest (laetud failist `tierlists.json`). Kasutaja saab luua uue tasemeloendi (valides ka soovitud tasemete arvu 3-6) või avada olemasoleva.

### Detailne Tasemeloendi Vaade

Kui tasemeloend on avatud, saab kasutaja seda muuta. See vaade kuvab iga taseme eraldi reana. Rida algab värvilise päisega, mis näitab taseme nime (mida saab muuta) ja sellele järgnevad tasemel olevad elemendid (hetkel tekst). Akna allosas on "Pakk" (Deck), kuhu saab lisada uusi tekstielemente ning kust neid saab tasemetele paigutada. Kasutaja saab elemente liigutada: sama taseme sees, erinevate tasemete vahel või tagasi "Pakki". Samuti saab lisada uusi tasemeridu (kuni 6 kokku) "+ Add Tier" nupuga.

### Andmete Püsivus

Rakendus kasutab Gson teeki, et serialiseerida ja deserialiseerida tasemeloendi andmed (sh tasemete nimed, värvid ja elemendid) kohalikku JSON-faili (`tierlists.json`). **Märkus:** "Paki" (DeckPanel) sisu *ei salvestata* JSON faili.

## Eeltingimused (olid testitud)

- **JDK:** 21
- **Gradle:** 8.13

## Rakenduse Ehitamine ja Käivitamine

### Kasuta Gradle Wrapperit

1. Ava terminal projekti juurkaustas.
2. Ehita projekt käsuga:
    ```bash
    ./gradlew clean build
    ```
3. Käivita rakendus käsuga:
    ```bash
    ./gradlew run
    ```

## Troubleshooting

- **Andmeformaadi Probleemid:** Kustuta fail `tierlists.json`, et rakendus saaks luua uue õige formaadiga faili.

## Gradle Ehituskonfiguratsiooni Olulised Punktid

Fail `build.gradle` sisaldab sõltuvusi (nt Gson) ning seab JVM argumendid mooduli ligipääsu haldamiseks. Plugin `application` kasutab põhiklassi (`com.tlc.Main`).