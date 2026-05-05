package com.atmbanksimulator;

// ===== 🧠 UIModel (Brain) =====

public class UIModel {
    // States
    private final String STATE_ACCOUNT_NO = "account_no";
    private final String STATE_PASSWORD = "password";
    private final String STATE_CREATE_NUMBER = "create_number";
    private final String STATE_CREATE_PASSWORD = "create_password";
    private final String STATE_LOGGED_IN = "logged_in";
    private final String STATE_WELCOME = "welcome";
    View view;
    private final Bank bank;
    // Current state
    private String state = STATE_WELCOME;

    // Data
    private String accNumber = "";
    private String newAccNumber = "";
    private String accPasswd = "";

    private String message;
    private String numberPadInput;
    private String result;

    public UIModel(Bank bank) {
        this.bank = bank;
    }

    // ===== WEEK 5: Welcome Page =====
    public void initialise() {
        setState(STATE_WELCOME);
        numberPadInput = "";
        message = "Welcome to the ATM";
        result = "Press \"Ent\" to begin";
        update();
    }

    // ===== Account Creation (Your Extension) =====
    public void newNumber() {
        setState(STATE_CREATE_NUMBER);
        numberPadInput = "";
        message = "Enter an account number";
        result = "Enter an account number\nWhich is five characters long\nFollowed by \"Ent\"";
        update();
    }

    public void newPassword() {
        setState(STATE_CREATE_PASSWORD);
        numberPadInput = "";
        message = "Enter a new password";
        result = "Enter a password\nWhich is five characters long\nFollowed by \"Ent\"";
        update();
    }

    // ===== FIXED RESET (Week 3 + Week 5 compatible) =====
    private void reset(String msg) {
        message = msg;
        numberPadInput = "";

        switch (state) {

            case STATE_ACCOUNT_NO:
                result = "Enter your account number\nFollowed by \"Ent\"";
                break;

            case STATE_PASSWORD:
                result = "Enter your password\nFollowed by \"Ent\"";
                break;

            case STATE_CREATE_NUMBER:
                result = "Enter an account number\nWhich is five characters long\nFollowed by \"Ent\"";
                break;

            case STATE_CREATE_PASSWORD:
                result = "Enter a password\nWhich is five characters long\nFollowed by \"Ent\"";
                break;

            default:
                setState(STATE_ACCOUNT_NO);
                result = "Enter your account number\nFollowed by \"Ent\"";
                break;
        }
    }

    private void setState(String newState) {
        if (!state.equals(newState)) {
            String oldState = state;
            state = newState;
            System.out.println("UIModel::setState: changed state from " + oldState + " to " + newState);
        }
    }

    // ===== Number Input =====
    public void processNumber(String numberOnButton) {
        numberPadInput += numberOnButton;
        message = "Beep! " + numberOnButton + " received";
        update();
    }

    public void processClear() {
        if (!numberPadInput.isEmpty()) {
            numberPadInput = "";
            message = "Input Cleared";
            update();
        }
    }

    // ===== ENTER BUTTON (Week 3 + Week 5 flow) =====
    public void processEnter() {
        switch (state) {

            // WEEK 5: Welcome → Account Number
            case STATE_WELCOME:
                setState(STATE_ACCOUNT_NO);
                message = "Enter your account number";
                result = "Followed by \"Ent\"";
                numberPadInput = "";
                break;

            // WEEK 3: Account Number → Password
            case STATE_ACCOUNT_NO:
                if (numberPadInput.isEmpty()) {
                    reset("Invalid Account Number");
                } else {
                    accNumber = numberPadInput;
                    numberPadInput = "";
                    setState(STATE_PASSWORD);
                    message = "Account Number Accepted";
                    result = "Now enter your password\nFollowed by \"Ent\"";
                }
                break;

            // WEEK 3: Password → Login
            case STATE_PASSWORD:
                accPasswd = numberPadInput;
                numberPadInput = "";

                if (bank.login(accNumber, accPasswd)) {
                    setState(STATE_LOGGED_IN);
                    message = "Logged In";
                    result = "Now enter the amount\nThen press transaction\n(Dep = Deposit, W/D = Withdraw)";
                } else {
                    reset("Login failed: Unknown Account/Password");
                }
                break;

            // Account creation (your extension)
            case STATE_CREATE_NUMBER:
                if (numberPadInput.length() == 5) {
                    newAccNumber = numberPadInput;
                    numberPadInput = "";
                    newPassword();
                } else {
                    reset("Invalid account number");
                }
                break;

            case STATE_CREATE_PASSWORD:
                String newPasswordNumber = numberPadInput;

                if (newPasswordNumber.length() == 5) {
                    bank.addBankAccount(newAccNumber, newPasswordNumber, 0);
                    reset("Account successfully created!");
                } else {
                    reset("Invalid password");
                }
                numberPadInput = "";
                break;

            case STATE_LOGGED_IN:
                // Enter does nothing when logged in
                break;
        }

        update();
    }

    // ===== Helper =====
    private int parseValidAmount(String number) {
        if (number.isEmpty()) return 0;
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ===== Balance =====
    public void processBalance() {
        if (state.equals(STATE_LOGGED_IN)) {
            numberPadInput = "";
            message = "Balance Available";
            result = "Your Balance is: " + bank.getBalance() +
                    "\n\nChoose: Dep, W/D, Bal, Fin";
        } else {
            reset("You are not logged in");
        }
        update();
    }

    // ===== Withdraw =====
    public void processWithdraw() {
        if (state.equals(STATE_LOGGED_IN)) {
            int amount = parseValidAmount(numberPadInput);

            if (amount > 0) {
                if (bank.withdraw(amount)) {
                    message = "Withdraw Successful";
                    result = "Withdrawn: " + amount;
                } else {
                    message = "Withdraw Failed: Insufficient Funds";
                    result = "Now enter the amount\nThen press transaction\n(Dep = Deposit, W/D = Withdraw)";
                }
            } else {
                message = "Invalid Amount";
                result = "Now enter the amount\nThen press transaction\n(Dep = Deposit, W/D = Withdraw)";
            }

            numberPadInput = "";
        } else {
            reset("You are not logged in");
        }
        update();
    }

    // ===== Deposit =====
    public void processDeposit() {
        if (state.equals(STATE_LOGGED_IN)) {
            int amount = parseValidAmount(numberPadInput);

            if (amount > 0) {
                bank.deposit(amount);
                message = "Deposit Successful";
                result = "Deposited: " + amount;
            } else {
                message = "Invalid Amount";
                result = "Now enter the amount\nThen press transaction\n(Dep = Deposit, W/D = Withdraw)";
            }

            numberPadInput = "";
        } else {
            reset("You are not logged in");
        }
        update();
    }

    // ===== Finish (Week 5 Goodbye Page) =====
    public void processFinish() {
        if (state.equals(STATE_LOGGED_IN)) {
            message = "Thank you for using the Bank ATM";
            result = "Goodbye!";
            bank.logout();

            setState(STATE_WELCOME);
            numberPadInput = "";
        } else {
            reset("You are not logged in");
        }
        update();
    }

    public void processUnknownKey(String action) {
        reset("Invalid Command");
        update();
    }

    private void update() {
        view.update(message, numberPadInput, result);
    }
}
