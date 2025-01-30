# Digital Wallet

A modern digital wallet application built with Vue.js and Spring Boot.

## Features

- Multi-currency support (EUR and CZK)
- Secure user authentication with JWT
- Real-time balance tracking
- Multiple transaction types:
    - Instant deposits with demo mode
    - Bank withdrawals
    - Wallet-to-wallet transfers
- QR code generation for deposits
- Transaction history with filtering and pagination
- Profile management
- Multi-language support (EN, CS, SK, ES, DE)
- Responsive design with Bootstrap 5
- Comprehensive error handling
- Real-time notifications

## User Guide

### Getting Started
1. Create an account using your email and password
2. Log in to access your digital wallet
3. View your current balances in EUR and CZK

### Managing Your Wallet

#### Deposits
1. Click "Deposit Funds"
2. Enter the amount you want to deposit
3. Select the currency (EUR or CZK)
4. Choose deposit method:
    - Standard deposit:
        - Use the provided payment details or QR code to make the transfer
        - For EUR: Use IBAN and SWIFT
        - For CZK: Use local account number
    - Demo mode:
        - Enable demo mode checkbox
        - The amount will be credited instantly
5. The deposit will be processed and added to your balance

#### Withdrawals and Transfers
1. Click "Withdraw Funds"
2. Enter the withdrawal amount
3. Select the currency
4. For bank withdrawals:
    - Fill in the bank account details
    - Add recipient name
    - Optional payment reference
5. For wallet-to-wallet transfers:
    - Enter recipient's account reference (ACC-XXXXXXXX)
    - Funds will be transferred instantly
6. The amount will be deducted from your balance

#### Account Reference System
- Each user gets a unique account reference (e.g., ACC-12345678)
- Use this reference for receiving transfers from other users
- Account reference is displayed in your profile
- Transfers between wallet accounts are processed instantly

#### Transaction History
- View all your past transactions
- Each transaction shows:
    - Date and time
    - Type (Deposit/Withdrawal/Transfer)
    - Amount and currency
    - Status (Pending/Completed/Failed)
    - Payment reference
- Filter transactions by:
    - Amount range
    - Reference text
    - Transaction type
- Sort by date (newest first)
- Paginate through results

#### Profile Management
1. Access your profile settings
2. Update your personal information:
    - Full name
    - Bank account number
3. Change your password:
    - Enter current password
    - Enter and confirm new password
    - Password must be at least 6 characters

#### Security Guidelines
1. Password Requirements:
    - Minimum 6 characters
    - Mix of letters and numbers recommended
    - Regular password changes recommended
2. Account Protection:
    - Never share your account reference
    - Keep your login credentials secure
    - Log out when using shared devices
3. Transaction Safety:
    - Verify recipient details before transfers
    - Check transaction amounts carefully
    - Keep payment references for tracking

### Business Rules

1. Currency Support
   - EUR and CZK accounts are maintained separately
   - Each currency has its own balance
   - No automatic currency conversion

2. Balance Rules
   - Cannot withdraw more than available balance
   - Minimum transaction amount: 0.01
   - Balance cannot go negative

3. Transaction Processing
   - Standard deposits are marked as pending until confirmed
   - Demo deposits are processed instantly
   - Wallet-to-wallet transfers are processed instantly
   - All transactions are recorded with timestamps
   - Failed transactions don't affect balance

4. Security
   - Each user can only access their own wallet
   - All actions require authentication
   - Session expires after inactivity
   - Password changes require current password verification

# Technical Documentation

## Features

- Multi-currency support (EUR and CZK)
- Secure user authentication with JWT
- Real-time balance tracking
- Multiple transaction types:
    - Instant deposits with demo mode
    - Bank withdrawals
    - Wallet-to-wallet transfers
- QR code generation for deposits
- Transaction history with filtering and pagination
- Profile management
- Multi-language support (EN, CS, SK, ES, DE)
- Responsive design with Bootstrap 5
- Comprehensive error handling
- Real-time notifications

## Architecture

### Frontend (Vue.js)
- Vue 3 with Composition API
- Pinia for state management
- Vue I18n for internationalization
- Bootstrap 5 for UI components
- TypeScript for type safety
- Axios for API communication
- QR code generation for payments
- Real-time notifications system

### Backend (Spring Boot)
- Spring Security with JWT authentication
- Spring Data JPA for database access
- PostgreSQL database
- Swagger/OpenAPI documentation
- Comprehensive error handling
- Transaction management
- Data validation

## API Documentation

### Authentication Endpoints

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "fullName": "John Doe",
  "bankAccount": "1234567890/0100"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

#### Change Password
```http
POST /api/auth/change-password
Authorization: Bearer <token>
Content-Type: application/json

{
  "currentPassword": "oldPassword",
  "newPassword": "newPassword123"
}
```

### Profile Endpoints

#### Get Profile
```http
GET /api/auth/profile
Authorization: Bearer <token>
```

#### Update Profile
```http
PUT /api/auth/profile
Authorization: Bearer <token>
Content-Type: application/json

{
  "fullName": "John Doe",
  "bankAccount": "1234567890/0100"
}
```

### Wallet Endpoints

#### Get Balances
```http
GET /api/wallet/balances
Authorization: Bearer <token>
```

#### Get Transactions
```http
GET /api/wallet/transactions
Authorization: Bearer <token>
```

Query parameters:
- `page` (default: 1)
- `size` (default: 10)
- `amountFrom`
- `amountTo`
- `reference`
- `type` (TRANSFER/EXTERNAL)

#### Create Transaction
```http
POST /api/wallet/transactions
Authorization: Bearer <token>
Content-Type: application/json

{
  "amount": 100.00,
  "currency": "EUR",
  "type": "DEPOSIT",
  "recipientAccount": "ACC-12345678",
  "recipientName": "John Doe",
  "paymentReference": "Invoice 123",
  "isDemoMode": false
}
```

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    account_reference VARCHAR(20) UNIQUE NOT NULL,
    full_name VARCHAR(255),
    bank_account VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE
);
```

### Wallet Balances Table
```sql
CREATE TABLE wallet_balances (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    currency VARCHAR(3) NOT NULL,
    balance DECIMAL(20, 2) NOT NULL,
    last_updated TIMESTAMP WITH TIME ZONE,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Transactions Table
```sql
CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    amount DECIMAL(20, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    type VARCHAR(10) NOT NULL,
    status VARCHAR(10) NOT NULL,
    recipient_account VARCHAR(255),
    recipient_name VARCHAR(255),
    payment_reference VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

## Development Setup

### Prerequisites
- Node.js 20+
- Java 17+
- PostgreSQL 15+
- Docker and Docker Compose (optional)

### Installation

1. Clone the repository:
```bash
git clone [repository-url]
cd digital-wallet
```

2. Install dependencies:
```bash
npm install
```

3. Configure environment variables:

Frontend (.env):
```env
VITE_API_URL=http://localhost:8080/api
```

Backend (application.properties):
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/wallet_db
spring.datasource.username=your-username-here
spring.datasource.password=your-password-here
jwt.secret=your-secret-key-here
jwt.expiration=86400000
```

4. Start development servers:
```bash
# Frontend
npm run dev

# Backend
./mvnw spring-boot:run
```

### Docker Deployment

Set env variables in the docker-compose.yml:

 *  POSTGRES_DB
 *  POSTGRES_USER
 *  POSTGRES_PASSWORD

 *  SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/DB_NAME
 *  SPRING_DATASOURCE_USERNAME
 *  SPRING_DATASOURCE_PASSWORD
 
Build images:
```bash
docker-compose build
```

2. Start services:
```bash
docker-compose up -d
```

3. Access the application:
- Frontend: http://localhost:5173
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI v3: http://localhost:8080/v3/api-docs 

## Code Quality

```bash
# Frontend
npm run format      # Run Prettier
npm run lint       # Run ESLint
npm run typecheck  # Run TypeScript check

# Backend
./mvnw verify      # Run all checks
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.