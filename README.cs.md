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
    - Heslo musí mít alespoň 6 znaků

#### Bezpečnostní pokyny
1. Požadavky na heslo:
    - Minimálně 6 znaků
    - Doporučena kombinace písmen a čísel
    - Doporučena pravidelná změna hesla
2. Ochrana účtu:
    - Nikdy nesdílejte své referenční číslo účtu
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
   - Minimální částka transakce: 0,01
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

#### Backend (Spring Boot)
- Spring Security s JWT
- Spring Data JPA
- PostgreSQL databáze
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
    "password": "password123"
  }
  ```
- POST `/api/auth/login`: Přihlášení uživatele
  ```json
  {
    "email": "user@example.com",
    "password": "password123"
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
    "isDemoMode": false,
    "recipientAccount": "ACC-12345678",  // Volitelné, pro převody
    "recipientName": "Jan Novák",        // Volitelné
    "paymentReference": "Faktura 123"     // Volitelné
  }
  ```
- POST `/api/wallet/transactions/{id}/process`: Zpracování čekající transakce

### Databázové schéma

#### Tabulka Users
- `id` (UUID): Primární klíč
- `email` (VARCHAR): Unikátní email uživatele
- `password` (VARCHAR): Hashované heslo
- `account_reference` (VARCHAR): Unikátní referenční číslo účtu
- `created_at` (TIMESTAMP): Datum vytvoření účtu

#### Tabulka Wallet Balances
- `id` (BIGSERIAL): Primární klíč
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
    - Java 17+
    - PostgreSQL 15+
    - Docker a Docker Compose

2. Instalace
   ```bash
   # Klonování repozitáře
   git clone [repository-url]

   # Instalace závislostí
   npm install

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
   npm run format      # Spustí Prettier
   npm run lint       # Spustí ESLint
   npm run typecheck  # Spustí TypeScript kontrolu
   npm run check-all  # Spustí vše v pořadí format -> lint -> typecheck

   # Backend
   npm run format:backend  # Formátuje Java kód
   ```

### Proměnné prostředí

#### Backend (`.env` nebo `application.properties`)
```properties
# Konfigurace databáze
spring.datasource.url=jdbc:postgresql://localhost:5432/wallet_db
spring.datasource.username=your-username-here
spring.datasource.password=your-password-here

# JWT Konfigurace
jwt.secret=your-secret-key-here
jwt.expiration=86400000

# Konfigurace serveru
server.port=8080
```

#### Frontend (`.env`)
```env
VITE_API_URL=http://localhost:8080/api
```

### Docker nasazení

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