export interface Transaction {
  id: string;
  userId: string;
  amount: number;
  currency: 'EUR' | 'CZK';
  type: 'DEPOSIT' | 'WITHDRAWAL';
  status: 'PENDING' | 'COMPLETED' | 'FAILED';
  recipientAccount?: string;
  recipientName?: string;
  paymentReference?: string;
  createdAt: string;
}

export interface WalletBalance {
  userId: string;
  currency: 'EUR' | 'CZK';
  balance: number;
  lastUpdated: string;
}

export interface EURInstructions {
  IBAN: string;
  SWIFT: string;
  Bank: string;
}

export interface CZKInstructions {
  AccountNumber: string;
  Bank: string;
}

export type PaymentInstructions = {
  EUR: EURInstructions;
  CZK: CZKInstructions;
};
