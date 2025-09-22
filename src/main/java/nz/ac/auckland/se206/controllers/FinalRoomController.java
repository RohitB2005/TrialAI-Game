package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.prompts.PromptEngineering;

public class FinalRoomController implements ControllerInterface {

  @FXML private Button btnGuilty;
  @FXML private Button btnInnocent;
  @FXML private Button btnReturn;
  @FXML private Button btnReplay;
  @FXML private Label timerLabel;
  @FXML private TextArea textFinal;
  @FXML private Label question;
  @FXML private Label cannotMakeVerdict;

  private String verdict = "You have run out of time";
  private boolean choiceMade = false;

  public void updateTimer(int timeRemaining) {
    // Update the timer display in the UI
    timerLabel.setText(timeRemaining + " seconds left");
    if (timeRemaining <= 0 && App.isFinalScene && !choiceMade) {
      onChoice();
    }
  }

  public void onEntry() {
    if (App.isFinalScene) {
      textFinal.setVisible(false);
      timerLabel.setVisible(true);
      btnGuilty.setVisible(true);
      btnInnocent.setVisible(true);
      btnReturn.setVisible(false);
      btnReplay.setVisible(true);
      question.setVisible(true);
    }

    boolean canMakeVerdict = ChatController.allParticipantsContacted();
    btnGuilty.setDisable(!canMakeVerdict);
    btnInnocent.setDisable(!canMakeVerdict);
    timerLabel.setVisible(canMakeVerdict);
    question.setVisible(canMakeVerdict);
    cannotMakeVerdict.setVisible(!canMakeVerdict);
  }

  @FXML
  private void onReplay() {
    App.resetGame();
  }

  private void onChoice() {
    textFinal.setText(getVerdict() + "\n\n" + getSystemPrompt());
    textFinal.setVisible(true);
    timerLabel.setVisible(false);
    btnGuilty.setVisible(false);
    btnInnocent.setVisible(false);
    btnReturn.setVisible(false);
    btnReplay.setVisible(true);
    choiceMade = true;
    App.stopTimer();
  }

  @Override
  public void reset() {
    btnGuilty.setVisible(true);
    btnInnocent.setVisible(true);
    timerLabel.setVisible(true);
    textFinal.setVisible(false);
    btnReplay.setVisible(true);
    verdict = "You have run out of time";
    choiceMade = false;
  }

  public String getSystemPrompt() {
    return PromptEngineering.getPrompt("FinalRoom.txt");
  }

  @FXML
  private void onGuiltyButton() {
    verdict = "Your Verdict is Correct";
    onChoice();
  }

  @FXML
  private void onInnocentButton() {
    verdict = "Your Verdict is Incorrect";
    onChoice();
  }

  private String getVerdict() {
    return verdict;
  }

  // logicfor pushing return button
  @FXML
  private void onReturn(ActionEvent event) throws IOException {
    Button clickedbtn = (Button) event.getSource();
    Scene scene = clickedbtn.getScene();
    App.openScene(scene, "courtroom");
    App.isFinalScene = false; // Set the final scene flag
  }
}
