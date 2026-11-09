# Rezervo — programări online la servicii locale

Rezervo leagă **furnizorii de servicii** (frizerii, service-uri auto, cabinete, saloane) de **clienți**:
furnizorul își publică serviciile, clientul vede orele libere și își face programare.

Proiectul are trei părți care vorbesc între ele:

```
 Site web (React)  ─┐
                    ├──►  API / backend (Java, Spring Boot)  ──►  Bază de date (PostgreSQL)
 Aplicație mobilă ──┘
 (React Native / Expo)
```

Site-ul și aplicația mobilă nu au date proprii: ele cer totul de la backend prin internet (cereri HTTP cu JSON).

---

## 1. Ce face aplicația

- **Conturi de utilizator**: cont nou, intrare în cont, ieșire. Două tipuri de cont: *Client* și *Furnizor*.
- **Furnizor**: adaugă și șterge servicii (nume, descriere, durată, preț), vede programările clienților, le poate anula.
- **Client**: caută servicii, alege ziua și ora liberă, face programare cu o notă, își vede și anulează programările.
- Program de lucru: luni–sâmbătă, 09:00–17:00 (se schimbă în `AppointmentService.java`).

---

## 2. Structura proiectului (ce fișiere și foldere sunt)

```
rezervo/
├── README.md                  ← acest ghid
├── render.yaml                ← rețeta de deploy pe Render (creează serverul, site-ul și baza de date)
├── .gitignore                 ← ce NU se urcă pe GitHub (secrete, fișiere generate)
├── .github/workflows/ci.yml   ← GitHub verifică automat că proiectul compilează la fiecare push
│
├── backend/                   ← SERVERUL (Java + Spring Boot)
│   ├── pom.xml                ← lista de biblioteci Java folosite (Maven le descarcă singur)
│   ├── Dockerfile             ← cum se construiește serverul pentru internet
│   └── src/
│       ├── main/java/ro/rezervo/
│       │   ├── auth/          ← cont nou, login, „cine sunt eu”
│       │   ├── user/          ← tabela de utilizatori
│       │   ├── offering/      ← serviciile oferite de furnizori
│       │   ├── appointment/   ← programările + regulile (ore libere, suprapuneri)
│       │   ├── security/      ← token-uri JWT, criptarea parolelor, limitare încercări
│       │   └── common/        ← tratarea erorilor, endpoint de „health”
│       ├── main/resources/
│       │   ├── application.properties       ← setări pentru rulare locală
│       │   └── application-prod.properties  ← setări pentru internet (citesc secretele din variabile de mediu)
│       └── test/              ← teste automate
│
├── web/                       ← SITE-UL (React + Vite)
│   ├── package.json           ← lista de biblioteci JavaScript
│   ├── .env.example           ← model pentru adresa API-ului
│   └── src/
│       ├── api.js             ← funcția care trimite cereri la backend
│       ├── auth.jsx           ← ține minte cine e logat
│       ├── App.jsx            ← meniul și paginile
│       └── pages/             ← fiecare pagină: servicii, detaliu, login, cont nou, programări
│
└── mobile/                    ← APLICAȚIA DE TELEFON (React Native + Expo)
    ├── App.js                 ← ecranul principal cu tab-urile de jos
    ├── app.json               ← numele aplicației, iconița, identificatorul pentru magazine
    ├── .env.example           ← model pentru adresa API-ului
    └── src/screens/           ← fiecare ecran
```

---

## 3. Tooluri de care ai nevoie

Instalează-le o singură dată, în ordinea asta:

| Tool | La ce folosește | De unde |
|---|---|---|
| **Git** | păstrează istoricul codului și îl urcă pe GitHub | git-scm.com |
| **Cont GitHub** | locul unde stă codul online; Render îl ia de acolo | github.com |
| **GitHub Desktop** *(opțional, mai ușor decât linia de comandă)* | urci codul cu click-uri | desktop.github.com |
| **Java JDK 21** | rulează backend-ul | adoptium.net (Temurin 21) |
| **IntelliJ IDEA Community** | editor pentru Java; are Maven inclus | jetbrains.com/idea |
| **Node.js 22 LTS** | rulează site-ul și aplicația mobilă (include `npm`) | nodejs.org |
| **Visual Studio Code** | editor pentru `web/` și `mobile/` | code.visualstudio.com |
| **Expo Go** (pe telefon) | rulează aplicația mobilă fără Android Studio/Xcode | Google Play / App Store |
| **Cont Render** | găzduiește serverul, site-ul și baza de date pe internet | render.com |

Verifică instalarea într-un terminal:

```bash
git --version
java -version     # trebuie să arate 21
node -v           # trebuie să arate v22 sau mai nou
```

---

## 4. Rulare pe calculatorul tău

Ai nevoie de **3 terminale** deschise (unul pentru fiecare parte).

### 4.1 Backend

Cel mai simplu: deschide folderul `backend` în IntelliJ → deschide `RezervoApplication.java` → butonul verde ▶ Run.

Sau din terminal (dacă ai Maven instalat separat):

```bash
cd backend
mvn spring-boot:run
```

Când vezi `Started RezervoApplication`, deschide http://localhost:8080/api/health — trebuie să apară `{"status":"ok"}`.

Local folosește o bază de date **H2** salvată în `backend/data/` — nu trebuie instalat nimic. Pentru a o goli, oprește serverul și șterge folderul `data`.

Testele automate:

```bash
cd backend
mvn test
```

### 4.2 Site-ul web

```bash
cd web
cp .env.example .env      # pe Windows: copy .env.example .env
npm install               # doar prima dată
npm run dev
```

Deschide http://localhost:5173. Fă un cont de **Furnizor**, adaugă un serviciu, apoi (în altă fereastră privată) un cont de **Client** și fă o programare.

### 4.3 Aplicația mobilă

1. Află IP-ul calculatorului în rețeaua Wi-Fi: Windows `ipconfig` (IPv4 Address), Mac `ipconfig getifaddr en0`.
2. Configurează adresa API-ului:
   ```bash
   cd mobile
   cp .env.example .env
   ```
   Editează `.env` și pune IP-ul tău: `EXPO_PUBLIC_API_URL=http://192.168.1.23:8080`
   (telefonul **nu** înțelege `localhost` — acolo `localhost` e chiar telefonul).
3. Pornește:
   ```bash
   npm install       # doar prima dată
   npx expo start
   ```
4. Scanează codul QR cu aplicația **Expo Go** (Android) sau cu camera (iPhone). Telefonul și calculatorul trebuie să fie pe același Wi-Fi.

Dacă telefonul nu se conectează la backend: permite portul 8080 în firewall-ul Windows sau încearcă `npx expo start --tunnel`.

---

## 5. Urcarea codului pe GitHub

**Da, ai nevoie de GitHub**: Render nu ia codul de pe calculatorul tău, ci dintr-un repository GitHub, și refă deploy-ul automat la fiecare push.

### Ce se urcă și ce NU se urcă

| Se urcă ✅ | NU se urcă ❌ (blocat deja de `.gitignore`) |
|---|---|
| tot codul din `backend/src`, `web/src`, `mobile/src` | `node_modules/` (se reinstalează cu `npm install`) |
| `pom.xml`, `package.json`, `package-lock.json` | `target/`, `dist/`, `.expo/` (generate automat) |
| `Dockerfile`, `render.yaml`, `.github/` | `.env` (adrese și setări locale) |
| `.env.example`, `.gitignore`, `README.md` | `backend/data/` (baza de date locală) |
| `app.json`, `App.js`, `mobile/assets/` | orice parolă, cheie sau token |

**Regulă de aur:** nicio parolă sau cheie secretă nu ajunge în Git. Secretele de producție se pun doar în dashboard-ul Render.

### Pașii

1. Pe github.com → **New repository** → nume `rezervo` → **Private** → *fără* README (îl ai deja) → Create.
2. Din folderul `rezervo`:
   ```bash
   git init
   git add .
   git status                      # verifică: NU trebuie să apară .env sau node_modules
   git commit -m "Prima versiune Rezervo"
   git branch -M main
   git remote add origin https://github.com/NUMELE-TAU/rezervo.git
   git push -u origin main
   ```
   La `push`, GitHub cere autentificare: cel mai simplu e să folosești **GitHub Desktop** (File → Add local repository → Publish), sau GitHub CLI cu `gh auth login`. Parola contului nu mai funcționează în terminal.
3. Pe GitHub, tab-ul **Actions**: în câteva minute trebuie să vezi bifă verde ✅ (backend compilat + teste trecute, site construit).

De acum, după fiecare modificare:

```bash
git add .
git commit -m "ce am schimbat"
git push
```

---

## 6. Publicarea pe internet (Render)

1. Intră pe render.com și autentifică-te **cu contul GitHub**.
2. **New → Blueprint** → alege repository-ul `rezervo`. Render citește `render.yaml` și propune 3 resurse: `rezervo-db`, `rezervo-api`, `rezervo-web`.
3. Îți cere valorile marcate `sync: false`. Prima dată nu știi încă adresele, așa că pune provizoriu:
   - `CORS_ALLOWED_ORIGINS` = `https://rezervo-web.onrender.com`
   - `VITE_API_URL` = `https://rezervo-api.onrender.com`
4. **Apply**. Backend-ul se construiește în ~5–10 minute (prima dată descarcă tot).
5. După ce pornesc, verifică adresele reale în dashboard (pot avea un sufix, ex. `rezervo-api-x7k2.onrender.com`).
   Dacă diferă de cele de la pasul 3:
   - `rezervo-api` → **Environment** → corectează `CORS_ALLOWED_ORIGINS` cu adresa exactă a site-ului (fără `/` la final) → Save.
   - `rezervo-web` → **Environment** → corectează `VITE_API_URL` → Save → **Manual Deploy → Clear build cache & deploy** (site-ul trebuie reconstruit ca să preia adresa).
6. Test: `https://ADRESA-API/api/health` → `{"status":"ok"}`, apoi deschide adresa site-ului.

`JWT_SECRET` și parola bazei de date sunt generate de Render și nu le vezi niciodată în cod.

### Limitele planului gratuit Render

- Serverul gratuit **adoarme după 15 minute fără vizitatori**; prima cerere după aceea durează ~1 minut.
- Baza de date gratuită **expiră după 30 de zile** (apoi mai ai 14 zile să faci upgrade înainte să fie ștearsă) și are 1 GB.
- Pentru utilizatori reali: trece baza de date pe un plan plătit înainte de expirare.

---

## 7. Aplicația mobilă cu serverul de pe internet

În `mobile/.env` pune adresa de pe Render:

```
EXPO_PUBLIC_API_URL=https://rezervo-api.onrender.com
```

Repornește `npx expo start`. Acum aplicația merge de oriunde, nu doar din Wi-Fi-ul tău.

### Publicarea în magazine (opțional, mai târziu)

Se face cu **EAS** (serviciul Expo de build în cloud) — nu ai nevoie de Android Studio sau Mac:

```bash
cd mobile
npx eas-cli@latest login
npx eas-cli@latest build:configure
npx eas-cli@latest build --platform android     # sau ios
npx eas-cli@latest submit --platform android
```

Cont Google Play Developer: taxă unică (~25 USD). Apple Developer: abonament anual (~99 USD). La build, pune adresa API-ului de producție ca variabilă de mediu în EAS (`EXPO_PUBLIC_API_URL`).

---

## 8. Securitate — ce e deja făcut

- **Parolele** sunt salvate doar ca hash **BCrypt** (nici tu ca administrator nu le poți citi).
- **Autentificare cu token JWT** semnat, valabil 24h; cheia vine din variabila `JWT_SECRET`. În producție, fără cheie serverul refuză să pornească.
- **Limitare încercări**: maximum 10 încercări de login / cont nou per IP la 15 minute (contra ghicirii parolelor).
- Mesajul de login greșit e același indiferent dacă emailul există sau nu.
- **Validare** pe server pentru toate datele (lungimi, email, parolă cu literă + cifră, preț, durată).
- **Permisiuni**: doar furnizorii adaugă servicii; un utilizator vede și anulează doar programările lui sau ale serviciilor lui.
- **Fără suprapuneri**: rezervările simultane pe aceeași oră sunt blocate în baza de date.
- **CORS** restrâns: doar site-ul tău poate apela API-ul din browser.
- Interogări SQL doar prin JPA cu parametri (fără risc de SQL injection); React escapează textul afișat (protecție XSS).
- Erorile nu trimit detalii interne (stacktrace, SQL) către utilizator.
- Pe telefon, token-ul e păstrat în **SecureStore** (criptat de sistem). Containerul serverului rulează ca utilizator fără drepturi de root.

**Ce să nu faci:** nu pune `JWT_SECRET` sau parole în cod sau în Git; nu dezactiva HTTPS; nu pune `*` în `CORS_ALLOWED_ORIGINS`.

---

## 9. Referință API (pentru curioși)

| Metodă | Adresă | Cine | Ce face |
|---|---|---|---|
| POST | `/api/auth/register` | oricine | cont nou → `{token, user}` |
| POST | `/api/auth/login` | oricine | login → `{token, user}` |
| GET | `/api/auth/me` | logat | datele contului |
| GET | `/api/services` | oricine | lista serviciilor |
| GET | `/api/services/{id}` | oricine | un serviciu |
| GET | `/api/services/{id}/slots?date=2026-11-16` | oricine | orele libere dintr-o zi |
| GET | `/api/services/mine` | furnizor | serviciile mele |
| POST | `/api/services` | furnizor | adaugă serviciu |
| DELETE | `/api/services/{id}` | furnizorul lui | șterge serviciu |
| POST | `/api/appointments` | client | programare nouă |
| GET | `/api/appointments/mine` | logat | programările mele |
| POST | `/api/appointments/{id}/cancel` | clientul sau furnizorul | anulează |

Cererile care cer cont trimit header-ul `Authorization: Bearer <token>`.

---

## 10. Idei de pași următori

- Program de lucru configurabil per furnizor.
- Email de confirmare la programare (ex. cu SendGrid / Resend).
- Resetare parolă prin email.
- Migrări de bază de date cu Flyway (în loc de `ddl-auto=update`) când aplicația are utilizatori reali.
- Notificări push pe telefon (Expo Notifications).
