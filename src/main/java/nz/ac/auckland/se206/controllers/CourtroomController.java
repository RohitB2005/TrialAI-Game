package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import nz.ac.auckland.se206.App;

/**
 * Controller class for the room view. Handles user interactions within the room where the user can
 * chat with customers and guess their profession.
 */
public class CourtroomController implements ControllerInterface {

  private static Set<String> viewedFlashbacks = new HashSet<>();
  private static boolean isFirstTimeInit = true;

  @FXML private Button btnGuess;
  @FXML private Button btnExit;
  @FXML private Button btnFinalRoom;
  @FXML private Label timerLabel;
  @FXML private Region trialAi;
  @FXML private Region witnessAi;
  @FXML private Region witnessHuman;

  @FXML private Label instructionLabel;
  @FXML private Pane rootPane;
  @FXML private Label finalRoomTooltip;
  @FXML private Region finalRoomOverlay;
  @FXML private ImageView humanIndicator;
  @FXML private ImageView defendantIndicator;
  @FXML private ImageView aiIndicator;

  private TranslateTransition bounceAnimation;

  private Media media;
  private MediaPlayer mediaPlayerWelcome;

  // Tooltip label shown when hovering disabled Judge Now button
  // (now injected from FXML)

  // loads and plays a stored tts file for the courtroom when the scene is opened for the first time
  @FXML
  public void initialize() throws URISyntaxException {
    btnFinalRoom.setDisable(true);

    // Play welcome audio on first entry
    playWelcomeAudio();
    playInstructionsPopup();

    // Tooltip and overlay are defined in FXML; ensure overlay visibility follows button state
    if (finalRoomOverlay != null) {
      finalRoomOverlay.setVisible(btnFinalRoom.isDisabled());
    }
    if (finalRoomTooltip != null) {
      finalRoomTooltip.setVisible(false);
      finalRoomTooltip.setOpacity(0);
    }
  }

  private void playInstructionsPopup() {
    instructionLabel.setOpacity(0);
    instructionLabel.setScaleX(0.9);
    instructionLabel.setScaleY(0.9);
    PauseTransition delay = new PauseTransition(Duration.millis(1500));
    delay.setOnFinished(
        event -> {
          FadeTransition ft = new FadeTransition(Duration.millis(300), instructionLabel);
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

  @Override
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

    // initializes flashback scene and opens it for each character
    if (viewedFlashbacks.contains(participantId)) {
      App.openScene(scene, participantId);
    } else {
      viewedFlashbacks.add(participantId);
      FlashbackController flashbackController =
          (FlashbackController) SceneManager.getController(SceneManager.AppUi.FLASHBACK);
      flashbackController.initializeFlashback(participantId);
      App.openScene(scene, "flashback");
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

  // hover effect indicator for each character utilises the arrow bounce animation
  @FXML
  private void onCharacterHover(MouseEvent event) {

    // set the regions and their ids
    Region region = (Region) event.getSource();
    String regionId = region.getId();

    // switch statement plays the arrow bounce animation above the specified character hovered
    switch (regionId) {
      case "witnessHuman":
        startBounceAnimation(humanIndicator);
        break;
      case "trialAi":
        startBounceAnimation(defendantIndicator);
        break;
      case "witnessAi":
        startBounceAnimation(aiIndicator);
        break;
    }
  }

  @FXML
  private void onCharacterExit(MouseEvent event) {
    stopBounceAnimation();
  }

  // Hover handler added in FXML for btnFinalRoom
  @FXML
  private void onFinalRoomHover(MouseEvent event) {
    // Only show tooltip when the button is disabled (overlay should only be active when disabled)
    if (!btnFinalRoom.isDisabled()) {
      return;
    }

    // Adjust max width relative to available space in rootPane so it can wrap
    double maxAllowed = Math.max(150, rootPane.getWidth() - 40); // leave some margin
    finalRoomTooltip.setMaxWidth(Math.min(400, maxAllowed));
    finalRoomTooltip.applyCss();
    finalRoomTooltip.layout();

    finalRoomTooltip.setVisible(true);

    FadeTransition ft = new FadeTransition(Duration.millis(300), finalRoomTooltip);
    ft.setToValue(1.0);
    ScaleTransition st = new ScaleTransition(Duration.millis(300), finalRoomTooltip);
    st.setToX(1.05);
    st.setToY(1.05);
    ft.play();
    st.play();
  }

  // method to create a transition animation for exiting the final room
  @FXML
  private void onFinalRoomExit(MouseEvent event) {

    // return if requirements not properly met
    if (finalRoomTooltip == null || !finalRoomTooltip.isVisible()) {
      return;
    }

    // initialise fade and scale transitions, showing popup animation for judge button
    FadeTransition ft = new FadeTransition(Duration.millis(200), finalRoomTooltip);
    ft.setToValue(0.0);
    ft.setOnFinished(e -> finalRoomTooltip.setVisible(false));
    ScaleTransition st = new ScaleTransition(Duration.millis(200), finalRoomTooltip);
    st.setToX(0.9);
    st.setToY(0.9);

    // play both animations
    ft.play();
    st.play();
  }

  // method to play the bounce animation for arrows on the courtroom screen
  private void startBounceAnimation(ImageView indicator) {

    // stop any existing animation and set the arrows visibility
    stopBounceAnimation();
    indicator.setVisible(true);

    // create new transition type, set y values and infinite cycling, and animation styling
    bounceAnimation = new TranslateTransition(Duration.millis(600), indicator);
    bounceAnimation.setFromY(0);
    bounceAnimation.setToY(-10);
    bounceAnimation.setAutoReverse(true);
    bounceAnimation.setCycleCount(TranslateTransition.INDEFINITE);
    bounceAnimation.setInterpolator(Interpolator.EASE_BOTH);

    // play the animation for the arrow above the characters head
    bounceAnimation.play();
  }

  // method to end the bouncing animation when not hovered
  private void stopBounceAnimation() {

    // if animation is playing, then stop it and reset its positioning
    if (bounceAnimation != null) {
      bounceAnimation.stop();
      bounceAnimation.getNode().setTranslateY(0);
      bounceAnimation = null;
    }

    // disable indicators for characters on end of animation
    humanIndicator.setVisible(false);
    defendantIndicator.setVisible(false);
    aiIndicator.setVisible(false);
  }

  public void updateFinalRoomButton() {
    if (ChatController.allParticipantsContacted()) {
      btnFinalRoom.setDisable(false);
      // hide overlay and tooltip when button becomes enabled
      if (finalRoomOverlay != null) {
        finalRoomOverlay.setVisible(false);
      }
      if (finalRoomTooltip != null) {
        finalRoomTooltip.setVisible(false);
        finalRoomTooltip.setOpacity(0);
      }
    }
  }

  @Override
  public void reset() {
    btnFinalRoom.setDisable(true);
    if (finalRoomOverlay != null) {
      finalRoomOverlay.setVisible(true);
    }
    isFirstTimeInit = true; // This ensures audio will play again after reset
    viewedFlashbacks.clear();

    // Stop current audio if playing
    if (mediaPlayerWelcome != null) {
      mediaPlayerWelcome.stop();
    }

    // Play welcome audio after reset
    playWelcomeAudio();
    playInstructionsPopup();
  }

  // Method to play welcome audio
  public void playWelcomeAudio() {
    if (isFirstTimeInit) {
      try {
        media = new Media(App.class.getResource("/sounds/Welcome.mp3").toURI().toString());
        mediaPlayerWelcome = new MediaPlayer(media);

        // Wait for MediaPlayer to be ready before playing
        mediaPlayerWelcome.setOnReady(
            () -> {
              mediaPlayerWelcome.play();
            });

        isFirstTimeInit = false;

      } catch (Exception e) {
        System.err.println("Error loading Welcome.mp3: " + e.getMessage());
        e.printStackTrace();
      }
    }
  }
}
