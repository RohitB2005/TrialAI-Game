package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.prompts.PromptEngineering;

public class FinalRoomController extends ChatController {

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
    timerLabel.setText(App.formatTime(timeRemaining));
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
    textFinal.setText(getVerdict() + "\n\n" + getPrompt());
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

  @Override
  public String getPrompt() {
    return PromptEngineering.getPrompt("finalroom.txt");
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
  protected void onReturn(ActionEvent event) throws IOException {
    Button clickedbtn = (Button) event.getSource();
    Scene scene = clickedbtn.getScene();
    App.openScene(scene, "courtroom");
    App.isFinalScene = false; // Set the final scene flag
  }

  @FXML
  protected void onSendMessage(ActionEvent event) throws ApiProxyException, IOException {
    String message = areaInputText.getText().trim();
    if (message.isEmpty()) {
      return;
    }
    areaInputText.clear();

    ChatMessage msg = new ChatMessage("user", message);
    appendChatMessage(msg);
    runGpt(msg);
  }

  @FXML
  private void onEnterPressed(KeyEvent event) throws ApiProxyException, IOException {
    if (event.getCode() == KeyCode.ENTER) {
      onSendMessage(null);
    }
  }

  @FXML
  // appends a chat message to the display area with appropriate formatting based on who sent it
  protected void appendChatMessage(ChatMessage msg) {
    if (msg.getRole().equals("user")) {
      areaDisplayText.appendText("You: " + msg.getContent() + "\n\n");
    } else if (msg.getRole().equals("assistant")) {
      areaDisplayText.appendText(msg.getContent() + "\n\n");
    }
  }
}
