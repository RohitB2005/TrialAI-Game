package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.util.Duration;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.prompts.PromptEngineering;

public class FinalRoomController extends ChatController {

  @FXML private Button btnReturn;
  @FXML private Button btnReplay;
  @FXML private Button btnSend;
  @FXML private Region btnGuilty;
  @FXML private Region btnInnocent;
  @FXML private Label timerLabel;
  @FXML private Label question;
  @FXML private Label cannotMakeVerdict;
  @FXML private Pane rootPane;
  @FXML private ScrollPane chatScrollPane;

  private boolean choiceMade = false;
  private String verdict = "";
  // Keep reference to any running timeline so we can stop it before starting another
  private Timeline blurTimeline;

  @Override
  public void updateTimer(int timeRemaining) {
    // Update the timer display in the UI
    timerLabel.setText(App.formatTime(timeRemaining));
    if (timeRemaining <= 0 && App.isFinalScene && !choiceMade) {
      onChoiceSelected();
    }
  }

  public void handleEntry() {
    // Disable return button if in final scene timer
    if (App.isFinalScene) {
      btnReturn.setVisible(false);
    }

    // Checks if player can make a verdict when time runs out
    boolean canMakeVerdict = allParticipantsContacted();
    timerLabel.setVisible(canMakeVerdict);
    question.setVisible(canMakeVerdict);
    cannotMakeVerdict.setVisible(!canMakeVerdict);
    btnSend.setVisible(canMakeVerdict);

    if (!canMakeVerdict) {
      btnInnocent.setDisable(true);
      btnGuilty.setDisable(true);
      areaInputText.setDisable(true);
      ChatMessage msg =
          new ChatMessage(
              "system",
              "You did not gather enough information to make a verdict. You can restart the game to"
                  + " try again.");
      appendChatMessage(msg, true);
    }

    // ensure chat is hidden on entry until a verdict is chosen
    if (chatScrollPane != null) {
      chatScrollPane.setVisible(false);
      chatScrollPane.setManaged(false);
    }
  }

  @Override
  public String getName() {
    return "Verdict";
  }

  @FXML
  private void onReplayClicked() {
    // clear blur and hide chat if present
    applyBackgroundBlur(false);
    if (chatScrollPane != null) {
      chatScrollPane.setVisible(false);
      chatScrollPane.setManaged(false);
      chatScrollPane.getStyleClass().remove("chat-floating");
    }
    App.resetGame();
  }

  // selects guilty verdict on guilty button click, and sends a message to prompt for reasoning
  @FXML
  private void onGuiltyClicked() {
    verdict = "I find the Sentinel-12 guilty of all charges.";
    btnGuilty.setDisable(true);
    btnInnocent.setVisible(false);

    // initialise chat message from system and append message
    ChatMessage msg = new ChatMessage("system", "Give reasoning for why the AI is guilty");
    appendChatMessage(msg, true);
    btnSend.setDisable(false);
    areaInputText.setDisable(false);
    btnReturn.setVisible(false);
    // show the chat popup and blur the background
    showChatPopup();
    applyBackgroundBlur(true);
  }

  // selects not guilty verdict on innocent button click, sending message prompting for reasons
  @FXML
  private void onInnocentClicked() {
    verdict = "I find the Sentinel-12 innocent of all charges.";
    btnInnocent.setDisable(true);
    btnGuilty.setVisible(false);

    // initialise chat message from system and append the message
    ChatMessage msg = new ChatMessage("system", "Give reasoning for why the AI is innocent");
    appendChatMessage(msg, true);
    btnSend.setDisable(false);
    areaInputText.setDisable(false);
    btnReturn.setVisible(false);
    // show the chat popup and blur the background
    showChatPopup();
    applyBackgroundBlur(true);
  }

  // logic for when the player has sent their final message or the timer has run out
  // disables the input box and send button and stops the timer
  private void onChoiceSelected() {

    if (choiceMade) {
      return;
    }
    choiceMade = true;

    String text = areaInputText.getText();

    if (!btnSend.isDisable()) {
      try {
        submitRationale(text);
      } catch (ApiProxyException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    // disables visibility of select buttons and enables others after choice made
    timerLabel.setVisible(false);
    btnReturn.setVisible(false);
    btnSend.setVisible(false);
    btnSend.setDisable(true);
    areaInputText.setDisable(true);
    choiceMade = true;
    App.stopTimer();
  }

  // reset method to reset all specific buttons and labels in scene on replay
  @Override
  public void reset() {
    super.reset();
    timerLabel.setVisible(true);
    btnReturn.setVisible(true);
    btnSend.setVisible(true);

    // set button visibility and choice made status
    btnSend.setDisable(false);
    choiceMade = false;
    btnGuilty.setVisible(true);
    btnInnocent.setVisible(true);
    areaInputText.setDisable(true);
    btnGuilty.setDisable(false);
    btnInnocent.setDisable(false);
    // remove any blur when resetting
    applyBackgroundBlur(false);
    // hide chat on reset
    if (chatScrollPane != null) {
      chatScrollPane.setVisible(false);
      chatScrollPane.setManaged(false);
      chatScrollPane.getStyleClass().remove("chat-floating");
    }
  }

  /** Make the chat ScrollPane visible and play a small entrance animation. */
  private void showChatPopup() {
    if (chatScrollPane == null) {
      return;
    }

    // Make visible and managed so it occupies layout space
    chatScrollPane.setManaged(true);
    chatScrollPane.setVisible(true);

    // Add the stronger floating style if not present
    if (!chatScrollPane.getStyleClass().contains("chat-floating")) {
      chatScrollPane.getStyleClass().add("chat-floating");
    }

    // entrance animation: fade + slight scale pop
    chatScrollPane.setOpacity(0);
    ScaleTransition st = new ScaleTransition(Duration.millis(260), chatScrollPane);
    st.setFromX(0.98);
    st.setFromY(0.98);
    st.setToX(1.0);
    st.setToY(1.0);

    FadeTransition ft = new FadeTransition(Duration.millis(260), chatScrollPane);
    ft.setFromValue(0.0);
    ft.setToValue(1.0);

    st.play();
    ft.play();
  }

  /**
   * Animate Gaussian blur radius for all root children except the chat pane. Smoothly transitions
   * the blur in or out.
   */
  private void applyBackgroundBlur(boolean enable) {
    if (rootPane == null) {
      return;
    }

    // Cancel any previous blur animation
    if (blurTimeline != null) {
      blurTimeline.stop();
      blurTimeline = null;
    }

    blurTimeline = new Timeline();
    double target = enable ? 12.0 : 0.0;
    Duration duration = Duration.millis(360);

    for (Node child : rootPane.getChildren()) {
      if (chatScrollPane != null && child == chatScrollPane) {
        // ensure chat has no blur
        child.setEffect(null);
        continue;
      }

      // Ensure the node has a GaussianBlur effect instance to animate
      GaussianBlur gb;
      if (child.getEffect() instanceof GaussianBlur) {
        gb = (GaussianBlur) child.getEffect();
      } else if (child.getEffect() == null) {
        gb = new GaussianBlur(0);
        child.setEffect(gb);
      } else {
        // If there's an unrelated effect, replace it with GaussianBlur for our purposes
        gb = new GaussianBlur(0);
        child.setEffect(gb);
      }

      KeyValue kv = new KeyValue(gb.radiusProperty(), target);
      KeyFrame kf = new KeyFrame(duration, kv);
      blurTimeline.getKeyFrames().add(kf);
    }

    // When disabling blur, clear effects at the end to avoid lingering zero-radius effects
    blurTimeline.setOnFinished(
        e -> {
          if (!enable) {
            for (Node child : rootPane.getChildren()) {
              if (chatScrollPane != null && child == chatScrollPane) {
                continue;
              }
              child.setEffect(null);
            }
          }
        });

    blurTimeline.play();
  }

  @Override
  public String getPrompt() {
    return PromptEngineering.getPrompt("finalroom.txt");
  }

  // logic for pushing return button
  @FXML
  @Override
  protected void onReturn(ActionEvent event) throws IOException {
    Button clickedbtn = (Button) event.getSource();
    Scene scene = clickedbtn.getScene();
    App.openScene(scene, "courtroom");
  }

  // logic to send a chatCompletionRequest and run the GPT model with given prompt
  @FXML
  @Override
  protected void onSendMessage(ActionEvent event) throws ApiProxyException, IOException {
    String message = areaInputText.getText().trim();
    if (message.isEmpty()) {
      return;
    }
    areaInputText.clear();

    // adds messages for user and system to chat
    submitRationale(message);

    onChoiceSelected();
  }

  @FXML
  @Override
  protected void onEnterPressed(KeyEvent event) throws ApiProxyException, IOException {
    if (event.getCode() == KeyCode.ENTER && !btnSend.isDisabled()) {
      onSendMessage(null);
    }
  }

  private void submitRationale(String rationale) throws ApiProxyException {
    ChatMessage prompt = new ChatMessage("system", getPrompt());
    chatCompletionRequest.addMessage(prompt);
    ChatMessage message = new ChatMessage("user", verdict + " " + rationale);
    appendChatMessage(message, true);
    runGpt(message);
  }
}
