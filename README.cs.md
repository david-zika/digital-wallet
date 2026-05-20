# Digitální Peněženka

Moderní aplikace digitální peněženky postavená na Vue.js a Spring Boot.

## Funkce

- Podpora více měn (EUR a CZK)
- Bezpečná autentizace pomocí JWT
- Sledování zůstatku v reálném čase
- Různé typy transakcí:
    - Okamžité vklady s demo režimem
    - Bankovní výběry
    - Převody mezi peněženkami
- Generování QR kódů pro vklady
- Historie transakcí s filtrováním a stránkováním
- Správa profilu
- Podpora více jazyků (EN, CS, SK, ES, DE)
- Responzivní design s Bootstrap 5
- Komplexní zpracování chyb
- Notifikace v reálném čase

## Uživatelská příručka

### Začínáme
1. Vytvořte si účet pomocí emailu a hesla
2. Přihlaste se do své digitální peněženky
3. Zobrazte si aktuální zůstatky v EUR a CZK

### Správa peněženky

#### Vklady
1. Klikněte na "Vložit prostředky"
2. Zadejte částku, kterou chcete vložit
3. Vyberte měnu (EUR nebo CZK)
4. Zvolte způsob vkladu:
    - Standardní vklad:
        - Použijte zobrazené platební údaje nebo QR kód pro provedení převodu
        - Pro EUR: Použijte IBAN a SWIFT
        - Pro CZK: Použijte lokální číslo účtu
    - Demo režim:
        - Aktivujte zaškrtávací políčko demo režimu
        - Částka bude připsána okamžitě
5. Vklad bude zpracován a přidán k vašemu zůstatku

#### Výběry a převody
1. Klikněte na "Vybrat prostředky"
2. Zadejte částku pro výběr
3. Vyberte měnu
4. Pro bankovní výběr:
    - Vyplňte bankovní údaje
    - Zadejte jméno příjemce
    - Volitelná platební reference
5. Pro převod mezi peněženkami:
    - Zadejte referenční číslo účtu příjemce (ACC-XXXXXXXX)
    - Prostředky budou převedeny okamžitě
6. Částka bude odečtena z vašeho zůstatku

#### Systém referenčních čísel účtů
- Každý uživatel dostane unikátní referenční číslo účtu (např. ACC-12345678)
- Toto číslo slouží pro příjem převodů od jiných uživatelů
- Referenční číslo je zobrazeno ve vašem profilu
- Převody mezi peněženkami jsou zpracovány okamžitě

#### Historie transakcí
- Zobrazení všech vašich předchozích transakcí
- Každá transakce obsahuje:
    - Datum a čas
    - Typ (Vklad/Výběr/Převod)
    - Částku a měnu
    - Stav (Čeká/Dokončeno/Selhalo)
    - Platební referenci
- Filtrování transakcí podle:
    - Rozsahu částek
    - Textu reference
    - Typu transakce
- Řazení podle data (nejnovější první)
- Stránkování výsledků

#### Správa profilu
1. Přístup k nastavení profilu
2. Aktualizace osobních údajů:
    - Celé jméno
    - Číslo bankovního účtu
3. Změna hesla:
    - Zadání současného hesla
    - Zadání a potvrzení nového hesla
    - Heslo musí mít alespoň 8 znaků

#### Bezpečnostní pokyny
1. Požadavky na heslo:
    - Minimálně 8 znaků
    - Doporučena kombinace písmen a čísel
    - Doporučena pravidelná změna hesla
2. Ochrana účtu:
    - Sdílejte referenční číslo účtu pouze důvěryhodným osobám
    - Udržujte své přihlašovací údaje v bezpečí
    - Odhlaste se při používání sdílených zařízení
3. Bezpečnost transakcí:
    - Ověřte údaje příjemce před převodem
    - Pečlivě kontrolujte částky transakcí
    - Uchovávejte platební reference pro sledování

### Obchodní pravidla

1. Podpora měn
   - EUR a CZK účty jsou vedeny odděleně
   - Každá měna má vlastní zůstatek
   - Neprobíhá automatická konverze měn

2. Pravidla pro zůstatek
   - Nelze vybrat více než dostupný zůstatek
   - Minimální částka transakce: 5,00
   - Zůstatek nemůže být záporný

3. Zpracování transakcí
   - Standardní vklady jsou označeny jako čekající do potvrzení
   - Demo vklady jsou zpracovány okamžitě
   - Převody mezi peněženkami jsou zpracovány okamžitě
   - Všechny transakce jsou zaznamenány s časovým razítkem
   - Neúspěšné transakce neovlivňují zůstatek

4. Bezpečnost
   - Každý uživatel má přístup pouze ke své peněžence
   - Všechny akce vyžadují autentizaci
   - Relace vyprší po neaktivitě
   - Změna hesla vyžaduje ověření současného hesla


## Technická dokumentace

### Architektura

#### Frontend (Vue.js)
- Vue 3 s Composition API
- Pinia pro správu stavu
- Vue I18n pro internacionalizaci
- Bootstrap 5 pro UI komponenty
- Axios pro komunikaci s API
- TypeScript pro typovou bezpečnost
- Generování QR kódů pro platby
- Notifikace v reálném čase
- Biome pro linting a formátování

#### Backend (Spring Boot)
- Spring Security s JWT (přístupový + refresh token)
- Spring Data JPA
- PostgreSQL databáze
- Redis pro ukládání refresh tokenů
- Flyway pro databázové migrace
- OpenAPI/Swagger pro dokumentaci API

### API Dokumentace

#### Přístup k Swagger UI
- Dostupné na: `http://localhost:8080/swagger-ui.html`
- OpenAPI specifikace: `http://localhost:8080/v3/api-docs`

#### Autentizační endpointy
- POST `/api/auth/register`: Vytvoření nového účtu
  ```json
  {
    "email": "user@example.com",
    "password": "password123",
    "fullName": "Jan Novák",
    "bankAccount": "1234567890/0100"
  }
  ```
  Odpověď: `{ "token": "...", "refreshToken": "..." }`
- POST `/api/auth/login`: Přihlášení uživatele
  ```json
  {
    "email": "user@example.com",
    "password": "password123"
  }
  ```
  Odpověď: `{ "token": "...", "refreshToken": "..." }`
- POST `/api/auth/refresh`: Obnova přístupového tokenu
  ```json
  {
    "refreshToken": "<refresh-token>"
  }
  ```
- POST `/api/auth/logout`: Odhlášení (zneplatnění refresh tokenu)
  ```json
  {
    "refreshToken": "<refresh-token>"
  }
  ```
- POST `/api/auth/change-password`: Změna hesla
  ```json
  {
    "currentPassword": "oldPassword",
    "newPassword": "newPassword123"
  }
  ```

#### Operace s peněženkou
- GET `/api/wallet/balances`: Získání zůstatků
- GET `/api/wallet/transactions`: Získání historie transakcí
- POST `/api/wallet/transactions`: Vytvoření nové transakce
  ```json
  {
    "amount": 100.00,
    "currency": "EUR",
    "type": "DEPOSIT",
    "recipientAccount": "ACC-12345678",  // Volitelné, pro převody
    "recipientName": "Jan Novák",        // Volitelné
    "paymentReference": "Faktura 123"    // Volitelné
  }
  ```
  > **Poznámka:** Demo režim (okamžité zpracování transakcí) je řízen serverovou konfigurací `app.demo-mode`, nikoli polem v požadavku.

### Databázové schéma

#### Tabulka Users
- `id` (UUID): Primární klíč
- `email` (VARCHAR): Unikátní email uživatele
- `password` (VARCHAR): Hashované heslo
- `account_reference` (VARCHAR): Unikátní referenční číslo účtu
- `full_name` (VARCHAR): Celé jméno
- `bank_account` (VARCHAR): Číslo bankovního účtu
- `created_at` (TIMESTAMP): Datum vytvoření účtu

#### Tabulka Wallet Balances
- `id` (UUID): Primární klíč
- `user_id` (UUID): Cizí klíč na users
- `currency` (VARCHAR): EUR nebo CZK
- `balance` (DECIMAL): Aktuální zůstatek
- `last_updated` (TIMESTAMP): Časové razítko poslední aktualizace

#### Tabulka Transactions
- `id` (UUID): Primární klíč
- `user_id` (UUID): Cizí klíč na users
- `amount` (DECIMAL): Částka transakce
- `currency` (VARCHAR): EUR nebo CZK
- `type` (VARCHAR): DEPOSIT nebo WITHDRAWAL
- `status` (VARCHAR): PENDING, COMPLETED nebo FAILED
- `recipient_account` (VARCHAR): Pro výběry/převody
- `recipient_name` (VARCHAR): Pro výběry
- `payment_reference` (VARCHAR): Volitelná reference
- `created_at` (TIMESTAMP): Časové razítko transakce

#### Tabulka Refresh Tokens
- `id` (UUID): Primární klíč
- `user_id` (UUID): Cizí klíč na users
- `token` (VARCHAR): Unikátní refresh token
- `expires_at` (TIMESTAMP): Platnost tokenu
- `created_at` (TIMESTAMP): Datum vytvoření

### Implementace zabezpečení

#### JWT Autentizace
- Autentizace založená na tokenech
- Bezestavová správa relací
- Správa expirace tokenů
- Bezpečné hashování hesel pomocí BCrypt

#### Zabezpečení databáze
- Bezpečnost na úrovni řádků
- Prepared statements
- Validace vstupů
- Prevence SQL injection

### Vývojové prostředí

1. Předpoklady
    - Node.js 20+
    - Java 21+
    - PostgreSQL 16+
    - Redis 7+
    - Docker a Docker Compose

2. Instalace
   ```bash
   # Klonování repozitáře
   git clone [repository-url]

   # Instalace závislostí
   npm install

   # Nastavení frontendu – zkopírujte .env.example
   cp packages/frontend/.env.example packages/frontend/.env

   # Spuštění vývojových serverů
   npm run dev
   ```

3. Sestavení
   ```bash
   # Sestavení frontendu
   npm run build

   # Sestavení backendu
   ./mvnw package

   # Sestavení Docker obrazů
   docker-compose build
   ```

4. Kontrola kódu
   ```bash
   # Frontend
   npm run format      # Spustí Biome formátování
   npm run lint       # Spustí Biome lint
   npm run typecheck  # Spustí TypeScript kontrolu
   npm run test       # Spustí testy
   npm run check-all  # Spustí vše v pořadí format -> lint -> typecheck -> test

   # Backend
   ./mvnw verify      # Spustí všechny kontroly a testy
   ```

### Proměnné prostředí

#### Backend (`.env` nebo `application.properties`)
```properties
# Konfigurace databáze
spring.datasource.url=jdbc:postgresql://localhost:5432/wallet_db
spring.datasource.username=your-username-here
spring.datasource.password=your-password-here

# JWT Konfigurace
jwt.secret=your-secret-key-min-64-chars-here
jwt.expiration=900000
jwt.refresh-expiration=604800000
jwt.issuer=digital-wallet-api
jwt.audience=digital-wallet-client

# Konfigurace aplikace
app.demo-mode=false
app.cors-allowed-origins=http://localhost:5173

# Konfigurace serveru
server.port=8080

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

#### Frontend (`.env`)
```env
VITE_API_URL=http://localhost:8080/api
```

### Docker nasazení

0. Nastavte proměnné prostředí v docker-compose.yml:

*  POSTGRES_DB
*  POSTGRES_USER
*  POSTGRES_PASSWORD

*  SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/DB_NAME
*  SPRING_DATASOURCE_USERNAME
*  SPRING_DATASOURCE_PASSWORD
*  SPRING_DATA_REDIS_HOST: redis
*  JWT_SECRET
*  APP_DEMO_MODE: false (výchozí)
*  APP_CORS_ALLOWED_ORIGINS: http://localhost:5173 (výchozí)

1. Sestavení obrazů:
   ```bash
   docker-compose build
   ```

2. Spuštění služeb:
   ```bash
   docker-compose up -d
   ```

3. Přístup k aplikaci:
    - Frontend: `http://localhost:5173`
    - Backend API: `http://localhost:8080`
    - Swagger UI: `http://localhost:8080/swagger-ui.html`
    - OpenAPI v3: http://localhost:8080/v3/api-docs

### Přispívání

1. Standardy kódování
    - ESLint konfigurace
    - Prettier formátování
    - Java code style
    - Git commit zprávy

2. Proces Pull Requestů
    - Feature branch workflow
    - Požadavky na code review
    - Požadavky na testování
    - Aktualizace dokumentace

3. Proces vydání
    - Číslování verzí
    - Údržba changelogu
    - Checklist pro nasazení

## Licence

Tento projekt je licencován pod MIT licencí - viz soubor LICENSE pro detaily.