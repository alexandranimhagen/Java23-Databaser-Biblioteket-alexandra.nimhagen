
Riktlinjer för att köra projektet:
1. Förberedelser
- Installera senaste JDK
- Installera Maven
- Installera MySQL-servern och konfigurera den
- Ladda ner och installera JavaFX SDK

2. Hämta Källkoden
- Installera Git och klona projektet:
- git clone https://github.com/MH-GRIT/jd23-databases-biblioteket-alexandranimhagen.git
- cd https://github.com/MH-GRIT/jd23-databases-biblioteket-alexandranimhagen.git

3. Databasinställningar
- Importera Databasdump: mysql -u root -p < librarydb_dump.sql

5. Bygg och kör projektet
- Bygg med Maven: mvn clean install
- Kör med Maven: mvn javafx:run

7. Använd en utvecklingsmiljö (IDE)
- IntelliJ IDEA: Installera och importera projektet som ett Maven-projekt
- Konfigurera JavaFX:
- Lägg till JavaFX SDK som bibliotek.
- Under körkonfiguration, lägg till VM options: --module-path /path/to/javafx-sdk-16/lib --add-modules javafx.controls,javafx.fxml
- 
8. Kör projektet i IDE
- Högerklicka på MainApp.java och välj Run 'MainApp.main()'.
