package com.atmbanksimulator;

public class PrimeAccount extends BankAccount {

    private static final double OVERDRAFT_LIMIT = -500.0;

    public PrimeAccount(String accNumber, String accPasswd, int balance) {
        super(accNumber, accPasswd, balance);
    }

    @Override
    public boolean withdraw(int amount) {
        double newBalance = getBalance() - amount;


        if (newBalance < OVERDRAFT_LIMIT) {
            return false;
        }


        setBalance(newBalance);
        return true;
    }
}

