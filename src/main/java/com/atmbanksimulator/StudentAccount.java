package com.atmbanksimulator;

public class StudentAccount extends BankAccount {

    private static final double WITHDRAW_LIMIT = 100.0;

    public StudentAccount(String accNumber, String accPasswd, int balance) {
        super(accNumber, accPasswd, balance);
    }

    @Override
    public boolean withdraw(int amount) {
        if (amount > WITHDRAW_LIMIT) {
            return false;
        }
        return super.withdraw(amount);
    }
}
