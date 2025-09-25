package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
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

  @FXML protected TextArea areaDisplayText;
  @FXML protected TextField areaInputText;
  @FXML private Button btnSend;
  @FXML private Button btnReturn;
  @FXML private Label timerLabel;

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

  @Override
  public void reset() {
    if (areaDisplayText != null) {
      areaDisplayText.clear();
    }
    if (areaInputText != null) {
      areaInputText.clear();
    }
    contactedParticipants.clear();
    isFirstTimeInit = true;
    try {
      initializeChatCompletionRequest();
    } catch (ApiProxyException e) {
      // TODO Auto-generated catch block
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

  // appends a chat message to the display area with appropriate formatting based on who sent it
  protected void appendChatMessage(ChatMessage msg) {
    if (msg.getRole().equals("user")) {
      areaDisplayText.appendText("You: " + msg.getContent() + "\n\n");
    } else if (msg.getRole().equals("assistant")) {
      areaDisplayText.appendText(getName() + ": " + msg.getContent() + "\n\n");
    }
    areaDisplayText.setScrollTop(Double.MAX_VALUE); // scrolls to bottom
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

  // method to check if all participants have been contacted
  public static boolean allParticipantsContacted() {
    return contactedParticipants.contains("WitnessAi")
        && contactedParticipants.contains("WitnessHuman")
        && contactedParticipants.contains("TrialAi");
  }

  // method to get list of contacted participants (for debugging/UI purposes)
  public static Set<String> getContactedParticipants() {
    return new HashSet<>(contactedParticipants);
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
}
