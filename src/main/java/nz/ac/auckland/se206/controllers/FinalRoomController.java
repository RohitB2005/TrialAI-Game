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
  @FXML private Label timerLabel;
  @FXML private TextArea textFinal;
  
  private String verdict = "You have run out of time";


  public void updateTimer(int timeRemaining) {
    // Update the timer display in the UI
    timerLabel.setText(timeRemaining + " seconds left");
    if (timeRemaining <= 0 && App.isFinalScene) {
      onChoice();
    }
  }

  // messy logic for when timer runs out in the final room

  public void onEntry() {
    if (App.isFinalScene) {
      btnReturn.setVisible(false);
    }
  }

  private void onChoice() {
    textFinal.setText(getVerdict() + "\n\n" + getSystemPrompt());
    textFinal.setVisible(true);
    timerLabel.setVisible(false);
    btnGuilty.setVisible(false);
    btnInnocent.setVisible(false);
    App.stopTimer();
  }

  public String getSystemPrompt() {
    // Return the system prompt for this scene
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
    App.openScene(scene, "courtRoom");
    App.isFinalScene = false; // Set the final scene flag
  }

}
