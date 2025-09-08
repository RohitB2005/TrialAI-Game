package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.net.URISyntaxException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;

/**
 * Controller class for the room view. Handles user interactions within the room where the user can
 * chat with customers and guess their profession.
 */
public class CourtroomController implements ControllerInterface {

  private static boolean isFirstTimeInit = true;

  @FXML private Button btnGuess;
  @FXML private Button btnExit;
  @FXML private Button btnFinalRoom;
  @FXML private Label timerLabel;
  @FXML private Region trialAi;
  @FXML private Region witnessAi;
  @FXML private Region witnessHuman;


  private Media media;
  private MediaPlayer mediaPlayerWelcome;

  /**
   * Initializes the room view. If it's the first time initialization, it will provide instructions
   * via text-to-speech.
   *
   * @throws URISyntaxException
   */
  // loads and plays a stored tts file for the courtroom when the scene is opened for the first time
  @FXML
  public void initialize() throws URISyntaxException {
    if (isFirstTimeInit) {
      //media = new Media(App.class.getResource("/sounds/Welcome.mp3").toURI().toString());
      //mediaPlayerWelcome = new MediaPlayer(media);
      //mediaPlayerWelcome.play();
      isFirstTimeInit = false;
    }
  }

  @Override
  public void updateTimer(int timeRemaining) {
    // Update the timer display in the UI
    timerLabel.setText(timeRemaining + " seconds left");
  }

  // logic for when the player clicks on a charcter region to go to a flashback
  @FXML
  private void handleRegionClicked(MouseEvent event) throws IOException {
    Region clickedRegion = (Region) event.getSource();
    Scene scene = clickedRegion.getScene();
    App.openScene(scene, clickedRegion.getId());
  }

  // unused methods from the interface
  @Override
  public void runGpt(ChatMessage msg) throws ApiProxyException {
    // This method is not implemented in this controller, but it can be overridden by subclasses
    throw new UnsupportedOperationException("runGpt is not implemented in CourtroomController");
  }

  @Override
  public boolean isFirstTimeInit() {
    return isFirstTimeInit;
  }

  @FXML
  private void onExit() {
    javafx.application.Platform.exit();
  }

  @FXML
  private void onFinalRoom(ActionEvent event) throws IOException {
    Button clickedbtn = (Button) event.getSource();
    Scene scene = clickedbtn.getScene();
    App.openScene(scene, "finalRoom");
    App.isFinalScene = true; // Set the final scene flag
  }

  @Override
  public String getSystemPrompt() {
    // This method is not implemented in this controller, but it can be overridden by subclasses
    throw new UnsupportedOperationException(
        "getSystemPrompt is not implemented in CourtroomController");
  }

  @Override
  public String getReturnPrompt() {
    // This method is not implemented in this controller, but it can be overridden by subclasses
    throw new UnsupportedOperationException(
        "getReturnPrompt is not implemented in CourtroomController");
  }
}
