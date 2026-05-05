package com.atmbanksimulator;

// ===== 🧠 UIModel (Brain) =====

public class UIModel {
    // The ATM UIModel can be in one of three states:
    // 1. Waiting for an account number
    // 2. Waiting for a password
    // 3. Logged in (ready to process requests for the logged-in account)
    // We represent each state with a String constant.
    // The 'final' keyword ensures these values cannot be changed.
    private final String STATE_ACCOUNT_NO = "account_no";
    private final String STATE_PASSWORD = "password";
    private final String STATE_CREATE_NUMBER = "create_number";
    private final String STATE_CREATE_PASSWORD = "create_password";
    private final String STATE_LOGGED_IN = "logged_in";
    private final String STATE_WELCOME = "welcome";
    private final Bank bank; // The ATM communicates with this Bank
    private final int maxAttempts = 3;
    View view; // Reference to the View (part of the MVC setup)
    // Variables representing the state and data of the ATM UIModel
    // Current state of the ATM
    private String state = STATE_WELCOME;
    // Data
    private String accNumber = "";
    private String newAccNumber = "";
    private String accPasswd = "";
    private String message;
    private String numberPadInput;
    private String result;
    private int loginAttempts = 0;

    // UIModel constructor: pass a Bank object that the ATM interacts with
    public UIModel(Bank bank) {
        this.bank = bank;
    }

    public void initialise() {
        setState(STATE_WELCOME);
        numberPadInput = "";
        message = "Welcome to the ATM";
        result = "Press \"Ent\" to begin";
        update();
    }


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

    // Handle the Enter button.
    // This is a more complex method: pressing Enter causes the ATM to change state,
    // progressing from STATE_ACCOUNT_NO → STATE_PASSWORD → STATE_LOGGED_IN,
    // and back to STATE_ACCOUNT_NO when logging out.
    public void processEnter() {
        // The action depends on the current ATM state
        switch (state) {
            case STATE_WELCOME:
                setState(STATE_ACCOUNT_NO);
                message = "Enter your account number";
                result = "Followed by \"Ent\"";
                numberPadInput = "";
                break;

            case STATE_ACCOUNT_NO:
                if (numberPadInput.isEmpty()) {
                    message = "Invalid Account Number";
                    reset(message);
                } else {
                    // Save the entered number as accNumber, clear numberPadInput,
                    // update the state to expect password, and provide instructions
                    accNumber = numberPadInput;
                    numberPadInput = "";
                    setState(STATE_PASSWORD);
                    message = "Account Number Accepted";
                    result = "Now enter your password\nFollowed by \"Ent\"";
                }
                break;

            case STATE_PASSWORD:

                accPasswd = numberPadInput;
                numberPadInput = "";
                if (bank.login(accNumber, accPasswd)) {
                    loginAttempts = 0;
                    setState(STATE_LOGGED_IN);
                    message = "Logged In";
                    result = "Now enter the amount\nThen press transaction\n(Dep = Deposit, W/D = Withdraw)";
                } else {
                    loginAttempts++;

                    if (loginAttempts >= maxAttempts) {
                        reset("Too many failed attempts");
                        result = "ATM reset. Start again.";
                        loginAttempts = 0;
                    } else {
                        reset("Login failed");
                        result = "Attempts left: " + (maxAttempts - loginAttempts);
                    }
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
                    reset(message);
                    result = "Press ENT to continue";
                    setState(STATE_WELCOME);
                    numberPadInput = "";
                    initialise();

                } else {
                    reset("Invalid password");
                    reset(message);
                    setState(STATE_WELCOME);
                    numberPadInput = "";
                    initialise();
                }

                break;

            case STATE_LOGGED_IN:
                // Enter does nothing when logged in
                break;
        }

        update();
    }

    private int parseValidAmount(String number) {
        if (number.isEmpty()) return 0;
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

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


    // Handle the Withdraw button:
    // If the user is logged in, attempt to withdraw the amount entered;
    // otherwise, reset the ATM and display an error message.
    // Reads the amount from numberPadInput, validates it, and updates messages/results accordingly.
    public void processWithdraw() {
        if (state.equals(STATE_LOGGED_IN)) {
            int amount = parseValidAmount(numberPadInput);

            if (amount > 0) {
                if (bank.withdraw(amount)) {
                    message = "Withdraw Successful";
                    result = "Withdrawn: " + numberPadInput;
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


    public void processDeposit() {
        if (state.equals(STATE_LOGGED_IN)) {
            int amount = parseValidAmount(numberPadInput);

            if (amount > 0) {
                bank.deposit(amount);
                message = "Deposit Successful";
                result = "Deposited: " + numberPadInput;
            } else {
                message = "Invaild Amount";
                result = "Now enter the amount\nThen press transaction\n(Dep = Deposit, W/D = Withdraw)";
            }

            numberPadInput = "";
        } else {
            reset("You are not logged in");
        }
        update();
    }

    public void processFinish() {
        if (state.equals(STATE_LOGGED_IN)) {
            message = "Thank you for using the Bank ATM";
            result = "Goodbye!\nPress ENT to continue";
            bank.logout();

            setState(STATE_WELCOME);
            numberPadInput = "";
        } else {
            reset("You are not logged in");
        }
        update();
    }

    public void processTransfer() {

        if (!state.equals(STATE_LOGGED_IN)) {
            reset("You are not logged in");
            update();
            return;
        }

        int amount = parseValidAmount(numberPadInput);

        if (amount <= 0) {
            message = "Invalid Amount";
            result = "Enter amount first";
            update();
            return;
        }
        // decides where to send the money
        String targetAccount;

        if (accNumber.equals("10001")) {
            targetAccount = "10002";
        } else {
            targetAccount = "10001";
        }

        // performs the transfer between accounts
        if (bank.transfer(targetAccount, amount)) {
            message = "Transfer Successful";
            result = "Transferred £" + amount + " to " + targetAccount;
        } else {
            message = "Transfer Failed";
            result = "Not enough funds";
        }
        numberPadInput = "";
        update();
    }

    // Handle unknown or invalid buttons for the current state:
    public void processUnknownKey(String action) {
        reset("Invalid Command");
        update();
    }

    private void update() {
        view.update(message, numberPadInput, result);
    }
}
