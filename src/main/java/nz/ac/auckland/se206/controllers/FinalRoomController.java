package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.prompts.PromptEngineering;

public class FinalRoomController extends ChatController {

  @FXML private Button btnReturn;
  @FXML private Button btnReplay;
  @FXML private Button btnSend;
  @FXML private Label timerLabel;
  @FXML private Label question;
  @FXML private Label cannotMakeVerdict;

  private boolean choiceMade = false;

  public void updateTimer(int timeRemaining) {
    // Update the timer display in the UI
    timerLabel.setText(App.formatTime(timeRemaining));
    if (timeRemaining <= 0 && App.isFinalScene && !choiceMade) {
      onChoice();
    }
  }

  public void onEntry() {
    // Disable return button if in final scene timer
    if (App.isFinalScene) {
      btnReturn.setVisible(false);
    }

    // Checks if player can make a verdict when time runs out
    boolean canMakeVerdict = ChatController.allParticipantsContacted();
    timerLabel.setVisible(canMakeVerdict);
    question.setVisible(canMakeVerdict);
    cannotMakeVerdict.setVisible(!canMakeVerdict);
    btnSend.setVisible(canMakeVerdict);
    if (!canMakeVerdict) {
      ChatMessage msg = new ChatMessage("system", "You did not gather enough information to make a verdict. You can restart the game to try again.");
      appendChatMessage(msg);
    }
  }

  @FXML
  private void onReplay() {
    App.resetGame();
  }

  // logic for when the player has sent their final message or the timer has run out
  // disables the input box and send button and stops the timer
  private void onChoice() {
    timerLabel.setVisible(false);
    btnReturn.setVisible(false);
    btnSend.setVisible(false);
    btnSend.setDisable(true);
    choiceMade = true;
    App.stopTimer();
  }



  @Override
  public void reset() {
    timerLabel.setVisible(true);
    btnReturn.setVisible(true);
    btnSend.setVisible(true);
    btnSend.setDisable(false);
    choiceMade = false;
  }

  public String getSystemPrompt() {
    return PromptEngineering.getPrompt("FinalRoom.txt");
  }

  // logic for pushing return button
  @FXML
  protected void onReturn(ActionEvent event) throws IOException {
    Button clickedbtn = (Button) event.getSource();
    Scene scene = clickedbtn.getScene();
    App.openScene(scene, "courtroom");
  }

  @FXML
  protected void onSendMessage(ActionEvent event) throws ApiProxyException, IOException {
    String message = areaInputText.getText().trim();
    if (message.isEmpty()) {
      return;
    }
    areaInputText.clear();

    ChatMessage prompt = new ChatMessage("system", getSystemPrompt());
    chatCompletionRequest.addMessage(prompt);

    ChatMessage msg = new ChatMessage("user", message);
    appendChatMessage(msg);
    runGpt(msg);

    onChoice();
  }
}
