package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.prompts.PromptEngineering;

public class FinalRoomController implements ControllerInterface {

  @FXML private Button btnGuilty;
  @FXML private Button btnInnocent;
  @FXML private Button btnReturn;
  @FXML private Label timerLabel;
  @FXML private TextArea textFinal;
  
  private boolean isFirstTimeInit = true;
  private String verdict = "You have run out of time";

  @Override
  public void updateTimer(int timeRemaining) {
    // Update the timer display in the UI
    timerLabel.setText(timeRemaining + " seconds left");
  }

  // messy logic for when timer runs out in the final room

  public boolean isFirstTimeInit() {
    if (isFirstTimeInit) {
      isFirstTimeInit = false;
      btnReturn.setVisible(false);
    } else {
      textFinal.setText(getVerdict() + "\n\n" + getSystemPrompt());
      textFinal.setVisible(true);
      timerLabel.setVisible(false);
      App.stopTimer();
    }
    return true; // or implement logic to track first-time initialization
  }

  // unused methods from the interface
  public String getSystemPrompt() {
    // Return the system prompt for this scene
    return PromptEngineering.getPrompt("FinalRoom.txt");
  }

  @FXML
  private void onGuiltyButton() {
    verdict = "Your Verdict is Correct";
    if (isFirstTimeInit) {
      isFirstTimeInit();
    }
    isFirstTimeInit();
  }

  @FXML
  private void onInnocentButton() {
    verdict = "Your Verdict is Incorrect";
    if (isFirstTimeInit) {
      isFirstTimeInit();
    }
    isFirstTimeInit();
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

  @Override
  public void onEnter() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'onEnter'");
  }

  @Override
  public void onExit() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'onExit'");
  }
}
