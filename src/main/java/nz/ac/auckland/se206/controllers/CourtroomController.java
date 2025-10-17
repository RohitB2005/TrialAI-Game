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
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.TextAlignment;
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

  @FXML private ImageView humanIndicator;
  @FXML private ImageView defendantIndicator;
  @FXML private ImageView aiIndicator;

  private TranslateTransition bounceAnimation;

  private Media media;
  private MediaPlayer mediaPlayerWelcome;

  // Tooltip label shown when hovering disabled Judge Now button
  private Label finalRoomTooltip;
  // Transparent overlay to capture hover events when the button is disabled
  private Region finalRoomOverlay;

  // loads and plays a stored tts file for the courtroom when the scene is opened for the first time
  @FXML
  public void initialize() throws URISyntaxException {
    btnFinalRoom.setDisable(true);

    // Play welcome audio on first entry
    playWelcomeAudio();
    playInstructionsPopup();

    // Initialize the tooltip label for the FinalRoom button
    initFinalRoomTooltip();
  }

  private void initFinalRoomTooltip() {
    finalRoomTooltip =
        new Label("You must chat to all 3 participants before you can make a verdict !!");
    finalRoomTooltip.getStyleClass().add("hud-display");
    finalRoomTooltip.setOpacity(0);
    finalRoomTooltip.setScaleX(0.9);
    finalRoomTooltip.setScaleY(0.9);
    finalRoomTooltip.setVisible(false);
    // Allow wrapping and set a reasonable default max width; adjusted before showing
    finalRoomTooltip.setWrapText(true);
    finalRoomTooltip.setMaxWidth(360);
    // Center text horizontally
    finalRoomTooltip.setAlignment(Pos.CENTER);
    finalRoomTooltip.setTextAlignment(TextAlignment.CENTER);
    // Add padding and slightly larger font for better readability
    // Use a red 'disabled' theme to indicate action is not allowed yet
    finalRoomTooltip.setStyle(
        "-fx-padding: 8px 12px; -fx-font-size: 14px; -fx-background-color: linear-gradient(#8B0000,"
            + " #B22222); -fx-text-fill: white; -fx-border-color: #FF6B6B; -fx-border-width: 1px;"
            + " -fx-border-radius: 8px; -fx-background-radius: 8px;");

    // Position the tooltip near the button (above it)
    // We'll update the layout when showing so it stays consistent
    rootPane.getChildren().add(finalRoomTooltip);

    // Create transparent overlay placed over btnFinalRoom to capture hover when disabled
    finalRoomOverlay = new Region();
    finalRoomOverlay.setStyle("-fx-background-color: transparent; -fx-cursor: not-allowed;");
    finalRoomOverlay.setVisible(btnFinalRoom.isDisabled());

    // Forward mouse events on overlay to tooltip show/hide handlers
    finalRoomOverlay.setOnMouseEntered(this::onFinalRoomHover);
    finalRoomOverlay.setOnMouseExited(this::onFinalRoomExit);

    // Ensure overlay and tooltip visibility follow the disabled state of the button
    btnFinalRoom
        .disabledProperty()
        .addListener(
            (obs2, wasDisabled, isDisabled) -> {
              finalRoomOverlay.setVisible(isDisabled);
              if (!isDisabled && finalRoomTooltip != null) {
                // hide tooltip immediately when button becomes enabled
                finalRoomTooltip.setVisible(false);
                finalRoomTooltip.setOpacity(0);
              }
            });

    // Keep overlay bounds synced with the button
    btnFinalRoom
        .boundsInParentProperty()
        .addListener(
            (obs, oldBounds, newBounds) -> {
              finalRoomOverlay.setLayoutX(newBounds.getMinX());
              finalRoomOverlay.setLayoutY(newBounds.getMinY());
              finalRoomOverlay.setPrefWidth(newBounds.getWidth());
              finalRoomOverlay.setPrefHeight(newBounds.getHeight());
            });

    // Initialize overlay bounds immediately if possible
    finalRoomOverlay.setLayoutX(btnFinalRoom.getLayoutX());
    finalRoomOverlay.setLayoutY(btnFinalRoom.getLayoutY());
    finalRoomOverlay.setPrefWidth(
        btnFinalRoom.getWidth() > 0 ? btnFinalRoom.getWidth() : btnFinalRoom.getPrefWidth());
    finalRoomOverlay.setPrefHeight(
        btnFinalRoom.getHeight() > 0 ? btnFinalRoom.getHeight() : btnFinalRoom.getPrefHeight());

    // Add overlay on top so it can capture events when needed
    rootPane.getChildren().add(finalRoomOverlay);
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

  @FXML
  private void onCharacterHover(MouseEvent event) {
    Region region = (Region) event.getSource();
    String regionId = region.getId();

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

    // Ensure CSS/layout is applied so width/height are valid
    finalRoomTooltip.applyCss();
    finalRoomTooltip.layout();

    // Use the overlay bounds for positioning
    double overlayX = finalRoomOverlay.getLayoutX();
    double overlayY = finalRoomOverlay.getLayoutY();
    double overlayW = finalRoomOverlay.getPrefWidth();

    // Adjust max width relative to available space in rootPane so it can wrap
    double maxAllowed = Math.max(150, rootPane.getWidth() - 40); // leave some margin
    finalRoomTooltip.setMaxWidth(Math.min(400, maxAllowed));
    finalRoomTooltip.applyCss();
    finalRoomTooltip.layout();
    double tooltipWidth = finalRoomTooltip.prefWidth(-1);

    double desiredX = overlayX + (overlayW - tooltipWidth) / 2;
    // If tooltip would go off right edge, shift it left
    double rightEdge = desiredX + tooltipWidth;
    if (rightEdge > rootPane.getWidth() - 10) {
      desiredX = rootPane.getWidth() - tooltipWidth - 10;
    }

    if (desiredX < 10) {
      desiredX = 10;
    }

    finalRoomTooltip.setLayoutX(desiredX);
    finalRoomTooltip.setLayoutY(overlayY - 70);
    finalRoomTooltip.setVisible(true);

    FadeTransition ft = new FadeTransition(Duration.millis(300), finalRoomTooltip);
    ft.setToValue(1.0);
    ScaleTransition st = new ScaleTransition(Duration.millis(300), finalRoomTooltip);
    st.setToX(1.05);
    st.setToY(1.05);
    ft.play();
    st.play();
  }

  @FXML
  private void onFinalRoomExit(MouseEvent event) {
    if (finalRoomTooltip == null || !finalRoomTooltip.isVisible()) {
      return;
    }

    FadeTransition ft = new FadeTransition(Duration.millis(200), finalRoomTooltip);
    ft.setToValue(0.0);
    ft.setOnFinished(e -> finalRoomTooltip.setVisible(false));
    ScaleTransition st = new ScaleTransition(Duration.millis(200), finalRoomTooltip);
    st.setToX(0.9);
    st.setToY(0.9);
    ft.play();
    st.play();
  }

  private void startBounceAnimation(ImageView indicator) {
    stopBounceAnimation();

    indicator.setVisible(true);

    bounceAnimation = new TranslateTransition(Duration.millis(600), indicator);
    bounceAnimation.setFromY(0);
    bounceAnimation.setToY(-10);
    bounceAnimation.setAutoReverse(true);
    bounceAnimation.setCycleCount(TranslateTransition.INDEFINITE);
    bounceAnimation.setInterpolator(Interpolator.EASE_BOTH);
    bounceAnimation.play();
  }

  private void stopBounceAnimation() {
    if (bounceAnimation != null) {
      bounceAnimation.stop();
      bounceAnimation.getNode().setTranslateY(0);
      bounceAnimation = null;
    }
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
