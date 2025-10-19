package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionRequest;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionRequest.Model;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionResult;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.chat.openai.Choice;
import nz.ac.auckland.apiproxy.config.ApiProxyConfig;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;

public class ChatController implements ControllerInterface {

  // ChatController is a base class for all rooms that use chat gpt
  // it contains all the methods and fxml elements needed for chat gpt functionality

  // static so only one instance is created and shared across all rooms
  // this means the chat history is preserved when switching rooms
  protected static ChatCompletionRequest chatCompletionRequest;

  // Tracking which participants have been contacted
  // Static so it persists across room switches
  public static Set<String> contactedParticipants = new HashSet<>();

  // method to check if all participants have been contacted
  public static boolean allParticipantsContacted() {
    // return true;
    return contactedParticipants.contains("WitnessAi")
        && contactedParticipants.contains("WitnessHuman")
        && contactedParticipants.contains("TrialAi");
  }

  @FXML protected VBox chatVBox;
  @FXML protected ScrollPane chatScrollPane;
  @FXML protected TextField areaInputText;
  @FXML private Button btnSend;
  @FXML private Button btnReturn;
  @FXML private Label timerLabel;

  // timeline used to animate smooth scrolling to bottom
  private Timeline scrollTimeline;

  protected Media media;
  protected MediaPlayer mediaPlayerWelcome;
  protected boolean isFirstTimeInit = true;

  // initializes the chat controller, setting up the chat completion request if it is first time
  // temp and topP are double the usual values to make the ai more creative and random
  @FXML
  private void initialize() throws ApiProxyException {
    if (chatCompletionRequest == null) {
      initializeChatCompletionRequest();
    }
  }

  // initialize a chat completion request, contains the logic to set the specific LLM parameters
  public void initializeChatCompletionRequest() throws ApiProxyException {
    try {

      // generate request with specific tokens, TopP, model used, etc
      ApiProxyConfig config = ApiProxyConfig.readConfig();
      chatCompletionRequest =
          new ChatCompletionRequest(config)
              .setN(1)
              .setTemperature(1)
              .setTopP(0.5)
              .setModel(Model.GPT_4_1_MINI)
              .setMaxTokens(200);
    } catch (ApiProxyException e) {

      // debugging info for errors
      e.printStackTrace();
    }
  }

  @Override
  public void updateTimer(int timeRemaining) {
    // Update the timer display in the UI
    timerLabel.setText(App.formatTime(timeRemaining));
  }

  // reset logic for the chat controller with required parameters
  @Override
  public void reset() {

    // clears the text area with multiple checks
    if (chatVBox != null) {
      chatVBox.getChildren().clear();
    }
    if (areaInputText != null) {
      areaInputText.clear();
    }
    contactedParticipants.clear();
    isFirstTimeInit = true;
    try {

      // create chat completion request, exception for debugging
      initializeChatCompletionRequest();
    } catch (ApiProxyException e) {
      e.printStackTrace();
    }
  }

  // method to run the gpt chat completion request in a background thread
  // disables the send and return buttons while waiting for a response
  // called on enteriing a message or entering a flashback
  public void runGpt(ChatMessage msg) throws ApiProxyException {

    chatCompletionRequest.addMessage(msg);

    btnSend.setDisable(true);
    btnReturn.setDisable(true);

    // Execute the chat completion request in a background thread

    Task<Void> backgroundTask =
        new Task<>() {

          // logic to fetch message and send request, then append chat messages
          @Override
          protected Void call() {
            try {
              ChatCompletionResult chatCompletionResult = chatCompletionRequest.execute();
              Choice result = chatCompletionResult.getChoices().iterator().next();
              chatCompletionRequest.addMessage(result.getChatMessage());
              Platform.runLater(
                  () -> {
                    appendChatMessage(result.getChatMessage());

                    // disable buttons while waiting for message
                    btnSend.setDisable(false);
                    btnReturn.setDisable(false);
                  });
            } catch (ApiProxyException e) {
              e.printStackTrace();
              return null;
            }
            return null;
          }
        };

    Thread backgroundThread = new Thread(backgroundTask);
    backgroundThread.setDaemon(true); // Ensure the thread does not prevent JVM shutdown
    backgroundThread.start();
  }

  public String getName() {
    return "RoomController";
  }

  // overridden in flashback controllers to provide the appropriate prompt
  public String getPrompt() {
    return "No prompt set";
  }

  // method to get the current participant identifier
  // overridden in each specific room controller
  public String getParticipantId() {
    return "unknown";
  }

  @FXML
  protected void onEnterPressed(KeyEvent event) throws ApiProxyException, IOException {
    if (event.getCode() == KeyCode.ENTER && !btnSend.isDisabled()) {
      onSendMessage(null);
    }
  }

  // logic for when the player clicks the send button
  // needs logic for if player presses enter
  @FXML
  protected void onSendMessage(ActionEvent event) throws ApiProxyException, IOException {
    String message = areaInputText.getText().trim();
    if (message.isEmpty()) {
      return;
    }

    // clears text area and save participant contacted history
    // send user message to LLM and append to textarea
    areaInputText.clear();
    String participantId = getParticipantId();
    if (!participantId.equals("unknown")) {
      contactedParticipants.add(participantId);
    }
    ChatMessage msg = new ChatMessage("user", message);
    appendChatMessage(msg);
    runGpt(msg);
  }

  // logic for pushing return button
  @FXML
  protected void onReturn(ActionEvent event) throws ApiProxyException, IOException {
    if (mediaPlayerWelcome != null) {
      mediaPlayerWelcome.stop();
    }
    Button btn = (Button) event.getSource();
    Scene scene = btn.getScene();
    App.openScene(scene, "courtRoom");
  }

  protected void appendChatMessage(ChatMessage msg) {
    appendChatMessage(msg, false);
  }

  // appends a chat message to the display area with appropriate formatting based on who sent it
  protected void appendChatMessage(ChatMessage msg, boolean showSystemMessage) {
    if ("system".equals(msg.getRole()) && !showSystemMessage) {
      return;
    }

    Label messageLabel = createLabel();

    javafx.scene.layout.HBox messageContainer = new javafx.scene.layout.HBox();

    switch (msg.getRole()) {
      case "user":
        messageLabel.setText("You: " + msg.getContent());
        messageLabel.setStyle(
            messageLabel.getStyle()
                + "-fx-background-color: linear-gradient(to right, #00bfff, #0077aa); "
                + "-fx-text-fill: white;");
        messageContainer.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        javafx.scene.layout.HBox.setMargin(messageLabel, new Insets(0, 0, 0, 40));
        break;
      case "assistant":
        messageLabel.setText(getName() + ": " + msg.getContent());
        messageLabel.setStyle(
            messageLabel.getStyle()
                + "-fx-background-color: rgba(0, 0, 0, 0.6); "
                + "-fx-text-fill: #00ffff;");
        messageContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        javafx.scene.layout.HBox.setMargin(messageLabel, new Insets(0, 40, 0, 0));
        break;
      case "system":
        messageLabel.setText("System: " + msg.getContent());
        messageLabel.setStyle(
            messageLabel.getStyle()
                + "-fx-background-color: #333333; -fx-text-fill: #ffcc00; "
                + "-fx-font-style: italic;");
        messageContainer.setAlignment(javafx.geometry.Pos.CENTER);
        javafx.scene.layout.HBox.setMargin(messageLabel, new Insets(0, 60, 0, 60));
        break;
    }

    messageContainer.getChildren().add(messageLabel);
    chatVBox.getChildren().add(messageContainer);
    // Smooth scroll to bottom after layout
    Platform.runLater(
        () -> {
          chatScrollPane.layout();

          // Cancel any running scroll animation
          if (scrollTimeline != null) {
            scrollTimeline.stop();
          }

          double start = chatScrollPane.getVvalue();
          double end = 1.0;
          // If already at bottom, just set and return
          if (Math.abs(end - start) < 1e-6) {
            chatScrollPane.setVvalue(end);
            return;
          }

          scrollTimeline = new Timeline();
          KeyValue kv = new KeyValue(chatScrollPane.vvalueProperty(), end);
          KeyFrame kf = new KeyFrame(Duration.millis(320), kv);
          scrollTimeline.getKeyFrames().add(kf);
          scrollTimeline.play();
        }); // animated auto-scroll
  }

  protected Label createLabel() {
    Label messageLabel = new Label();
    messageLabel.setWrapText(true);
    messageLabel.setMaxWidth(230);
    messageLabel.setPrefWidth(Label.USE_COMPUTED_SIZE);
    messageLabel.setStyle("-fx-padding: 8; -fx-font-size: 14px; -fx-background-radius: 10;");
    return messageLabel;
  }

  // helper method to be shared between characters for welcome sounds
  protected void playWelcomeSound(String soundFileName) {
    try {
      // constucts filepath for sound names
      String soundPath = "/sounds/" + soundFileName;
      media = new Media(App.class.getResource(soundPath).toURI().toString());
      mediaPlayerWelcome = new MediaPlayer(media);

      // wait for the MediaPlayer to be ready before playing
      mediaPlayerWelcome.setOnReady(
          () -> {
            mediaPlayerWelcome.play();
          });

    } catch (Exception e) {
      // debug and error case accounted for
      System.err.println("Error loading " + soundFileName + ": " + e.getMessage());
    }
  }

  protected void playInstructionsPopup(Label instructionLabel) {
    if (instructionLabel == null) {
      return;
    }
    // Ensure the node is visible and initial values are applied before the animation starts
    instructionLabel.setVisible(true);
    instructionLabel.setOpacity(0);
    instructionLabel.setScaleX(0.9);
    instructionLabel.setScaleY(0.9);

    PauseTransition delay = new PauseTransition(Duration.millis(1500));
    delay.setOnFinished(
        event -> {
          FadeTransition ft = new FadeTransition(Duration.millis(300), instructionLabel);
          ft.setFromValue(0);
          ft.setToValue(1.0);
          ScaleTransition st = new ScaleTransition(Duration.millis(300), instructionLabel);
          st.setToX(1.0);
          st.setToY(1.0);
          ft.play();
          st.play();
        });

    // Start the initial delay
    delay.play();
  }
}
