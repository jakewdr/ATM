package com.atmbanksimulator;

// ===== ⚡ Controller (Nerves) =====

import javafx.scene.media.AudioClip;

import java.util.Objects;

// The Controller receives user actions from the View and delegates the appropriate tasks to the UIModel.
// Its main job is to decide what to do based on the user input.
public class Controller {

    private final AudioClip clickSound = new AudioClip(
            Objects.requireNonNull(getClass().getResource("/sounds/click.mp3")).toExternalForm()
    );

    private final AudioClip errorSound = new AudioClip(
            Objects.requireNonNull(getClass().getResource("/sounds/error.mp3")).toExternalForm()
    );
    UIModel UIModel; // Reference to the UIModel (part of the MVC setup)

    // The process method is called by the View in response to user interface events.
    // It uses a switch statement to determine which UIModel method should be called,
    // and delegates the task accordingly.
    void process(String action) {

        switch (action) {
            case "1":
            case "2":
            case "3":
            case "4":
            case "5":
            case "6":
            case "7":
            case "8":
            case "9":
            case "0":
                clickSound.play();
                UIModel.processNumber(action);
                break;

            case "CLR":
                clickSound.play();
                UIModel.processClear();
                break;

            case "Ent":
                clickSound.play(); // Different sound for Enter
                UIModel.processEnter();
                break;

            case "W/D":
                clickSound.play();
                UIModel.processWithdraw();
                break;

            case "Dep":
                clickSound.play();
                UIModel.processDeposit();
                break;

            case "Bal":
                clickSound.play();
                UIModel.processBalance();
                break;
            case "TRF":
                UIModel.processTransfer();
                break;
            case "Fin":
                clickSound.play();
                UIModel.processFinish();
                break;
            case "New":
                clickSound.play();
                UIModel.newNumber();
                break;

            default:
                errorSound.play();
                UIModel.processUnknownKey(action);
                break;
        }
    }
}