# grabb.ch Mobile App

Native iOS & Android App für grabb.ch - Lokale News für die Region Bülach.

## Architektur

```
www/                    ← Statische Web-App
├── index.html          ← Haupt-HTML
├── css/app.css         ← Styles
├── js/
│   ├── api.js          ← WordPress REST API Client
│   └── app.js          ← App-Logik
├── img/                ← Icons & Bilder
└── manifest.json       ← PWA Manifest

ios/                    ← Xcode Projekt (generiert)
android/                ← Android Studio Projekt (generiert)
```

## Funktionen

- 📰 **News-Feed** - Lädt Posts via WordPress REST API
- 🗺️ **Karte** - Leaflet mit POIs und Artikel-Markern
- 🏘️ **Gemeinde-Filter** - Bülach, Höri, Hochfelden, Winkel, Bachenbülach
- 📱 **Native Features** - Splash Screen, Status Bar, Offline-fähig

## Voraussetzungen

### Für Android
- [Android Studio](https://developer.android.com/studio) (gratis)
- JDK 17+
- Google Play Developer Account ($25 einmalig)

### Für iOS
- Mac mit macOS
- [Xcode](https://apps.apple.com/app/xcode/id497799835) (gratis)
- Apple Developer Account ($99/Jahr)

## Development

### Web-App lokal testen
```bash
# Einfacher HTTP Server
npx serve www

# Oder mit Python
cd www && python3 -m http.server 8080
```

Dann http://localhost:8080 öffnen.

### Änderungen synchronisieren
Nach jeder Änderung in `www/`:
```bash
npm run sync
# Oder nur für eine Plattform:
npm run sync:ios
npm run sync:android
```

## Build Android

### 1. Android Studio öffnen
```bash
npm run open:android
```

### 2. In Android Studio
1. Warten bis Gradle sync fertig
2. `Build` → `Generate Signed Bundle / APK`
3. `Android App Bundle` wählen (für Play Store)
4. Keystore erstellen oder auswählen
5. `Release` Build wählen
6. `Finish`

### 3. Output
- AAB: `android/app/build/outputs/bundle/release/app-release.aab`
- APK: `android/app/build/outputs/apk/release/app-release.apk`

### 4. Play Store
1. https://play.google.com/console
2. App erstellen
3. AAB hochladen
4. Store-Eintrag ausfüllen
5. Zur Prüfung einreichen

## Build iOS

### 1. Xcode öffnen
```bash
npm run open:ios
```

### 2. In Xcode
1. `App` Projekt auswählen
2. Signing & Capabilities:
   - Team: Dein Apple Developer Account
   - Bundle Identifier: `ch.grabb.app`
3. Scheme auf "Any iOS Device" setzen
4. `Product` → `Archive`

### 3. App Store Connect
1. Window → Organizer
2. Archive auswählen
3. `Distribute App` → `App Store Connect`
4. Upload

### 4. App Store
1. https://appstoreconnect.apple.com
2. Neue App erstellen
3. Build auswählen
4. Metadaten ausfüllen
5. Zur Prüfung einreichen

## App Icons

Icons müssen noch erstellt werden! Benötigt:

### Android (in `android/app/src/main/res/`)
- mipmap-mdpi: 48x48
- mipmap-hdpi: 72x72
- mipmap-xhdpi: 96x96
- mipmap-xxhdpi: 144x144
- mipmap-xxxhdpi: 192x192

### iOS (in `ios/App/App/Assets.xcassets/AppIcon.appiconset/`)
- 20x20, 29x29, 40x40, 60x60, 76x76, 83.5x83.5, 1024x1024
- Jeweils @1x, @2x, @3x Varianten

**Tool:** https://www.appicon.co/ (generiert alle Größen)

## Splash Screen

Standard grün (#10b981). Anpassbar in:
- iOS: `ios/App/App/Assets.xcassets/Splash.imageset/`
- Android: `android/app/src/main/res/drawable/splash.png`

## API Endpoints (grabb.ch)

Die App nutzt diese REST-API Endpoints:

| Endpoint | Beschreibung |
|----------|--------------|
| `/wp-json/wp/v2/posts?_embed` | News-Posts mit Featured Images |
| `/wp-json/grabb/v1/regionen` | Verfügbare Regionen |
| `/wp-json/grabb/v1/map-points` | Kartenpunkte (POIs) |
| `/wp-json/grabb/v1/article-points` | Artikel mit Geo-Daten |

## Troubleshooting

### Android: "SDK not found"
In Android Studio: File → Project Structure → SDK Location

### iOS: "Signing requires a development team"
In Xcode: Signing & Capabilities → Team auswählen

### Änderungen erscheinen nicht
```bash
npm run sync
```

### API-Fehler
CORS muss auf grabb.ch erlaubt sein (ist es bereits).

## Nächste Schritte

- [ ] App Icons erstellen
- [ ] Screenshots für Stores
- [ ] Store-Beschreibungen schreiben
- [ ] Datenschutzerklärung URL
- [ ] Push Notifications einrichten (@capacitor/push-notifications)
