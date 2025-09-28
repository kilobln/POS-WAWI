# POS-WAWI Android App

Diese App implementiert ein kombiniertes Kassensystem (POS) und eine Warenwirtschaft für ein Café. Sie ist in Kotlin mit Jetpack Compose erstellt und unterstützt Touch-Bedienung, Offline-Pufferung sowie mehrsprachige Oberflächen (Deutsch/Englisch).

## Features

- **POS Oberfläche** mit Produktkacheln, Rabattfunktionen, Zahlungsarten (Bar, Karte, Gutschein) sowie Bon parken/wiederaufnehmen.
- **Warenwirtschaft** inklusive Produktpflege, Lagerbestandsverwaltung, Inventur und Wareneingang.
- **Mitarbeiterverwaltung** mit PIN-Anlage, Check-in/Check-out und Rechteverwaltung.
- **Berichte** für Tages- und Monatsumsätze, Top-Seller und Mitarbeiterleistung mit CSV-Export.
- **Einstellungen** für Offline-Synchronisation, Loyalitätsprogramme und Sprachwahl.

## Projektstruktur

- `app/` – Android-App (Compose UI, Room-Datenbank, Repository).
- `gradle/` – Wrapper-Konfiguration.
- `gradlew`, `gradlew.bat` – Build-Skripte.

## Voraussetzungen

- Java 17 (kann per `JAVA_HOME` gesetzt werden).
- Android SDK (Umgebungsvariable `ANDROID_HOME` oder `local.properties`).

## Build

```bash
export JAVA_HOME=/path/to/jdk17
export ANDROID_HOME=/path/to/android-sdk
./gradlew assembleDebug
```

Beim ersten Aufruf lädt das `gradlew`-Skript automatisch die passende `gradle-wrapper.jar` herunter (Voraussetzung: `curl` und `unzip` vorhanden). Danach befindet sich die erzeugte APK unter `app/build/outputs/apk/debug/`.
