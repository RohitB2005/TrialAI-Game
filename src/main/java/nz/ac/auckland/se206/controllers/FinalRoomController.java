package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Ellipse;
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
  @FXML private Ellipse guiltyHoverShape;
  @FXML private Ellipse innocentHoverShape;
  @FXML private Button btnExit;

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

    if (verdict != "") {
      return;
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
    // hide input area and send button until a verdict is chosen
    if (areaInputText != null) {
      areaInputText.setVisible(false);
      areaInputText.setManaged(false);
      areaInputText.setDisable(true);
    }
    if (btnSend != null) {
      btnSend.setVisible(false);
      btnSend.setDisable(true);
    }
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
    verdictDecided();
    // initialise chat message from system and append message
    ChatMessage msg = new ChatMessage("system", "Give reasoning for why the AI is guilty");
    appendChatMessage(msg, true);
  }

  // selects not guilty verdict on innocent button click, sending message prompting for reasons
  @FXML
  private void onInnocentClicked() {
    verdict = "I find the Sentinel-12 innocent of all charges.";
    verdictDecided();
    ChatMessage msg = new ChatMessage("system", "Give reasoning for why the AI is innocent");
    appendChatMessage(msg, true);
  }

  private void verdictDecided() {
    btnInnocent.setDisable(true);
    btnGuilty.setDisable(true);
    btnGuilty.setVisible(false);
    btnInnocent.setVisible(false);
    btnGuilty.setMouseTransparent(true);
    btnInnocent.setMouseTransparent(true);
    // initialise chat message from system and append the message
    btnSend.setDisable(false);
    btnSend.setVisible(true);
    btnSend.setManaged(true);
    areaInputText.setDisable(false);
    areaInputText.setVisible(true);
    areaInputText.setManaged(true);
    areaInputText.requestFocus();
    btnReturn.setVisible(false);
    // make verdict regions non-interactive and show the chat popup
    if (btnGuilty != null) {
      btnGuilty.setMouseTransparent(true);
    }
    if (btnInnocent != null) {
      btnInnocent.setMouseTransparent(true);
    }
    showChatPopup();
    applyBackgroundBlur(true);
  }

  @FXML
  private void onVerdictExit(MouseEvent event) {
    Region region = (Region) event.getSource();

    if (region == btnGuilty) {
      animateHoverShape(guiltyHoverShape, false);
    } else if (region == btnInnocent) {
      animateHoverShape(innocentHoverShape, false);
    }
  }

  // hover effect animations for verdict buttons from the image
  @FXML
  private void onVerdictHover(MouseEvent event) {

    // get the source of the event before choice is made
    Region region = (Region) event.getSource();
    if (choiceMade) {
      return;
    }

    // identify region and show the intended colour on hover with animation
    if (region == btnGuilty) {
      animateHoverShape(guiltyHoverShape, true);
    } else if (region == btnInnocent) {
      animateHoverShape(innocentHoverShape, true);
    }
  }

  private void animateHoverShape(Node shape, boolean fadeIn) {
    if (shape == null) {
      return;
    }
    FadeTransition ft = new FadeTransition(Duration.millis(200), shape);
    ft.setToValue(fadeIn ? 0.4 : 0.0);
    ft.play();
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
    areaInputText.setVisible(false);
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
    // restore verdict regions to be clickable
    if (btnGuilty != null) {
      btnGuilty.setMouseTransparent(false);
    }
    if (btnInnocent != null) {
      btnInnocent.setMouseTransparent(false);
    }
    // hide input and send when resetting
    if (areaInputText != null) {
      areaInputText.setVisible(false);
      areaInputText.setManaged(false);
      areaInputText.setDisable(true);
    }
    if (btnSend != null) {
      btnSend.setVisible(false);
      btnSend.setDisable(true);
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

    // entrance animation: fade + slight scale pop for chat
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

    // animate input and send button coming in (fade + translate)
    if (areaInputText != null) {
      areaInputText.setOpacity(0);
      areaInputText.setTranslateY(8);
      FadeTransition fin = new FadeTransition(Duration.millis(220), areaInputText);
      fin.setFromValue(0);
      fin.setToValue(1);
      ScaleTransition sin = new ScaleTransition(Duration.millis(220), areaInputText);
      sin.setFromX(0.995);
      sin.setFromY(0.995);
      sin.setToX(1);
      sin.setToY(1);
      fin.play();
      sin.play();
    }

    if (btnSend != null) {
      btnSend.setOpacity(0);
      btnSend.setTranslateY(8);
      FadeTransition fbtn = new FadeTransition(Duration.millis(220), btnSend);
      fbtn.setFromValue(0);
      fbtn.setToValue(1);
      ScaleTransition sbtn = new ScaleTransition(Duration.millis(220), btnSend);
      sbtn.setFromX(0.995);
      sbtn.setFromY(0.995);
      sbtn.setToX(1);
      sbtn.setToY(1);
      fbtn.play();
      sbtn.play();
    }
    // ensure input visibility follows chat popup (if it was enabled)
    if (areaInputText != null && !areaInputText.isDisabled()) {
      areaInputText.requestFocus();
    }
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
    }

    blurTimeline = new Timeline();
    double target = enable ? 12.0 : 0.0;
    Duration duration = Duration.millis(360);

    for (Node child : rootPane.getChildren()) {
      // Don't blur the chat pane (we want it to stay crisp when showing)
      if (chatScrollPane != null && child == chatScrollPane) {
        child.setEffect(null);
        continue;
      }

      // Also avoid blurring UI elements that should remain readable while typing
      if (timerLabel != null && child == timerLabel) {
        child.setEffect(null);
        continue;
      }

      if (btnReplay != null && child == btnReplay) {
        child.setEffect(null);
        continue;
      }

      // Keep the return button unblurred as well (navigation controls should stay readable)
      if (btnReturn != null && child == btnReturn) {
        child.setEffect(null);
        continue;
      }

      // Keep the exit button unblurred so the user can always quit while typing
      if (btnExit != null && child == btnExit) {
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

    // Build animations (fade + slight scale) for input and send, run them in parallel,
    // then hide the controls and perform choice handling once finished.
    ParallelTransition pt = new ParallelTransition();

    if (areaInputText != null) {
      FadeTransition fout = new FadeTransition(Duration.millis(220), areaInputText);
      fout.setFromValue(areaInputText.getOpacity());
      fout.setToValue(0);
      ScaleTransition sout = new ScaleTransition(Duration.millis(220), areaInputText);
      sout.setFromX(areaInputText.getScaleX());
      sout.setFromY(areaInputText.getScaleY());
      sout.setToX(0.995);
      sout.setToY(0.995);
      pt.getChildren().addAll(fout, sout);
    }

    if (btnSend != null) {
      FadeTransition fbtn = new FadeTransition(Duration.millis(220), btnSend);
      fbtn.setFromValue(btnSend.getOpacity());
      fbtn.setToValue(0);
      ScaleTransition sbtn = new ScaleTransition(Duration.millis(220), btnSend);
      sbtn.setFromX(btnSend.getScaleX());
      sbtn.setFromY(btnSend.getScaleY());
      sbtn.setToX(0.995);
      sbtn.setToY(0.995);
      pt.getChildren().addAll(fbtn, sbtn);
    }

    if (pt.getChildren().isEmpty()) {
      onChoiceSelected();
    } else {
      pt.setOnFinished(
          e -> {
            if (areaInputText != null) {
              areaInputText.setVisible(false);
              areaInputText.setManaged(false);
              areaInputText.setDisable(true);
            }
            if (btnSend != null) {
              btnSend.setVisible(false);
              btnSend.setManaged(false);
              btnSend.setDisable(true);
            }
            onChoiceSelected();
          });
      pt.play();
    }
  }

  @FXML
  @Override
  protected void onEnterPressed(KeyEvent event) throws ApiProxyException, IOException {
    if (event.getCode() == KeyCode.ENTER && !btnSend.isDisabled()) {
      onSendMessage(null);
    }
  }

  private void submitRationale(String rationale) throws ApiProxyException {
    initializeChatCompletionRequest();
    ChatMessage prompt = new ChatMessage("system", getPrompt());
    chatCompletionRequest.addMessage(prompt);
    ChatMessage message = new ChatMessage("user", verdict + " " + rationale);
    appendChatMessage(message, true);
    runGpt(message);
  }

  // method creates a label object with specified parameters
  @Override
  protected Label createLabel() {
    Label messageLabel = new Label();

    // set specific styling, text wrapping and size of the label to return
    messageLabel.setWrapText(true);
    messageLabel.setMaxWidth(500);
    messageLabel.setPrefWidth(Label.USE_COMPUTED_SIZE);
    messageLabel.setStyle("-fx-padding: 8; -fx-font-size: 14px; -fx-background-radius: 10; -fx-font-family: 'Roboto mono';");
    return messageLabel;
  }

  @FXML
  private void onExitBtn() {
    javafx.application.Platform.exit();
  }
}
