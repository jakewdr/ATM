package com.atmbanksimulator;

public class SavingAccount extends BankAccount {

    private static final double INTEREST_RATE = 0.02;

    public SavingAccount(String accNumber, String accPasswd, int balance) {
        super(accNumber, accPasswd, balance);
    }

    public void applyInterest() {
        double newBalance = getBalance() + (getBalance() * INTEREST_RATE);
        setBalance(newBalance);
    }
}
