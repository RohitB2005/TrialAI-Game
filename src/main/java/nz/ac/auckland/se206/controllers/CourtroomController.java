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

  // loads and plays a stored tts file for the courtroom when the scene is opened for the first time
  @FXML
  public void initialize() throws URISyntaxException {
    btnFinalRoom.setDisable(true);
  }

  public void updateTimer(int timeRemaining) {
    // Update the timer display in the UI
    timerLabel.setText(App.formatTime(timeRemaining));
    // Check if button should be enabled whenever timer updates
    updateFinalRoomButton();
  }

  // logic for when the player clicks on a charcter region to go to a flashback
  @FXML
  private void handleRegionClicked(MouseEvent event) throws IOException {
    Region clickedRegion = (Region) event.getSource();
    String participantId = clickedRegion.getId();
    Scene scene = clickedRegion.getScene();

    if (ChatController.getContactedParticipants().contains(participantId)) {
      App.openScene(scene, participantId);
    } else {
      ChatController.contactedParticipants.add(participantId);
      FlashbackController flashbackController =
          (FlashbackController) SceneManager.getController(SceneManager.AppUi.FLASHBACK);
      flashbackController.setupFlashback(participantId);
      App.openScene(scene, "flashback");
    }
  }

  public void onEntry() {
    if (isFirstTimeInit) {
      // media = new Media(App.class.getResource("/sounds/Welcome.mp3").toURI().toString());
      // mediaPlayerWelcome = new MediaPlayer(media);
      // mediaPlayerWelcome.play();
      isFirstTimeInit = false;
      btnFinalRoom.setDisable(true);
    }
  }

  @FXML
  private void onExitBtn() {
    javafx.application.Platform.exit();
  }

  @FXML
  private void onFinalRoom(ActionEvent event) throws IOException {
    Button clickedbtn = (Button) event.getSource();
    Scene scene = clickedbtn.getScene();
    App.openScene(scene, "finalRoom");
  }

  public void updateFinalRoomButton() {
    if (ChatController.allParticipantsContacted()) {
      btnFinalRoom.setDisable(false);
    }
  }

  @Override
  public void reset() {
    btnFinalRoom.setDisable(true);
    isFirstTimeInit = true;
  }
}
