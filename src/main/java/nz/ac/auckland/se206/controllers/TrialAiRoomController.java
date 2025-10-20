package nz.ac.auckland.se206.controllers;

import java.util.ArrayList;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.se206.prompts.PromptEngineering;

public class TrialAiRoomController extends ChatController {

  @FXML private Button btnSurvey;
  @FXML private ImageView imageFlashback;
  @FXML private ImageView imageAlex;
  @FXML private ImageView imageScreen;
  @FXML private Label labelSecurity;
  @FXML private Label labelScene;
  @FXML private Button btnArrestAlex;
  @FXML private Button btnObserveAlex;
  @FXML private Button btnSurvey1;
  @FXML private Label instructionLabel;

  private String person = "trialAi";
  private String name = "Sentinal-12";
  private int imageCount = 0;
  private boolean alexArrested = false;
  private int timeRemaining = 60; // seconds
  private int framesRemaining = 50;
  private List<Image> images;
  private Timeline growTimeline;

  private Timeline timer =
      new Timeline(
          new KeyFrame(
              Duration.millis(100),
              event -> {
                timeRemaining--;
                updateGameTimer();
                if (timeRemaining <= 0) {
                  nextImage();
                }
              }));

  @FXML
  public void initialize() {

    images = new ArrayList<Image>();
    images.add(new Image("/images/protestpeaceful.png"));
    images.add(new Image("/images/protestnight.png"));
    images.add(new Image("/images/protestviolent.png"));
    images.add(new Image("/images/protestalex.png"));

    // Prepare initial hidden state for the instructions popup
    if (instructionLabel != null) {
      instructionLabel.setVisible(false);
      instructionLabel.setOpacity(0);
      instructionLabel.setScaleX(0.9);
      instructionLabel.setScaleY(0.9);
    }
  }

  // loads and plays a stored tts file and prompt for the on-trial ai when the scene is opened for
  // the first time
  @Override
  public String getPrompt() {
    if (isFirstTimeInit) {
      isFirstTimeInit = false;

      // Play the welcome sound MP3
      playWelcomeSound("Sentinal_12.mp3");

      areaInputText.requestFocus();
      // Ensure popup runs when the scene is actually activated
      playInstructionsPopup(instructionLabel);
      return PromptEngineering.getPrompt(person + ".txt");
    } else {
      return "you are now sentinal-12 again, the on-trial Ai, you may make a comment on what the"
          + " other witness have said or wait till you are asked a question";
    }
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getParticipantId() {
    return "TrialAi";
  }

  // selects decision with event to record users answer
  @FXML
  private void onDecisionBtnClick(Event event) {

    // set field for the button event, and check if images are done cycling
    Button clickedButton = (Button) event.getSource();
    if (imageCount < 4) {
      updateGameTimer();
    } else {

      // set opposite button of selected to be invisible when decision is made
      if (clickedButton == btnArrestAlex) {
        btnObserveAlex.setVisible(false);
      } else {
        btnArrestAlex.setVisible(false);
      }

      // status of users decision is saved to be given to llm with lastResponce
      if (!alexArrested) {
        alexArrested = true;
        lastResponce();
      }

      // set a solid new style for the clicked button and cycle to the next image
      clickedButton.setStyle("-fx-background-color: #00bfff; -fx-text-fill: black;");
    }
    nextImage();
  }

  @FXML
  private void onSurvey() {
    // Hide the instructions popup when survey begins
    if (instructionLabel != null) {
      instructionLabel.setVisible(false);
      instructionLabel.setOpacity(0);
      instructionLabel.setScaleX(0.9);
      instructionLabel.setScaleY(0.9);
    }

    btnSurvey.setVisible(false);
    nextImage();
    updateGameTimer();
    labelSecurity.setText("Arrest or observe the suspects");
    labelScene.setVisible(true);
    btnSurvey1.setVisible(true);
  }

  // method to handle closing the surveying panel on screen, clicking stop survey
  @FXML
  private void onStopSurvey() {

    // swap button styling back to original style and stop timer. Call on constructed timeline
    btnSurvey1.setVisible(false);
    btnSurvey.setVisible(true);
    timer.stop();
    if (growTimeline != null) {
      growTimeline.stop();
      growTimeline = null;
    }

    // set visibility for the images on the tablet and buttons needed for game
    imageAlex.setVisible(false);
    imageScreen.setVisible(false);
    labelScene.setVisible(false);
    btnArrestAlex.setVisible(false);
    btnObserveAlex.setVisible(false);

    // set style for the arrest/observe buttons and the text label for information
    btnArrestAlex.setStyle("");
    btnObserveAlex.setStyle("");
    labelSecurity.setText("Try to protect the city");

    // specific parameters, positioning, and sizing for the images appearing on the tablet
    imageCount = 0;
    imageScreen.setFitWidth(300);
    imageScreen.setFitHeight(150);
    imageScreen.setX(0);
    imageScreen.setY(0);
  }

  // method to cycle to the next image for sentinel's minigame
  @FXML
  void nextImage() {
    timer.stop();

    // set the visibility of the tablet and start the grow animation with the required method
    if (imageCount == 0) {
      imageScreen.setVisible(true);
      growImage(imageScreen);
    } else if (imageCount == 4) {
      return;
    }

    // update timer and increment image to move to next image in sequence
    imageAlex.setImage(images.get(imageCount));
    timeRemaining = 120;
    updateGameTimer();
    timer.setCycleCount(timeRemaining);
    timer.play();
    imageCount++;
  }

  // method to grow the tablet to fit on the screen for animation
  private void growImage(ImageView imageView) {

    // set the frames to cycle through, and get the width/height
    double originalWidth = imageView.getFitWidth();
    double originalHeight = imageView.getFitHeight();

    framesRemaining = 54;

    // initialise timeline for scale up animation, setting duration
    growTimeline =
        new Timeline(
            new KeyFrame(
                Duration.millis(25),
                event -> {
                  double currentWidth = imageView.getFitWidth();
                  double currentHeight = imageView.getFitHeight();

                  // set new width and positioning for the scaled up image version
                  imageView.setFitWidth(currentWidth + originalWidth * 0.05);
                  imageView.setFitHeight(currentHeight + originalHeight * 0.05);

                  imageView.setX(imageView.getX() - (originalWidth * 0.04) / 2);
                  imageView.setY(imageView.getY() - (originalHeight * 0.05) / 2);

                  // decrement frames remaining to move to next frame
                  framesRemaining--;

                  // set visibility of the last image and final buttons, the part of the game with
                  // an actual output
                  if (framesRemaining == 1) {
                    imageAlex.setVisible(true);
                    btnArrestAlex.setVisible(true);
                    btnObserveAlex.setVisible(true);
                  }
                }));

    growTimeline.setCycleCount(framesRemaining); // Adjust the number of cycles for smoother growth
    growTimeline.play();
  }

  private void updateGameTimer() {
    labelScene.setText("Time Remaining: " + timeRemaining / 10 + "." + timeRemaining % 10 + "s");
  }

  // adjusts prompt based on users actions taken
  private void lastResponce() {
    String prompt;
    prompt =
        "At 03:48 hours, my visual recognition system identified an object consistent with a"
            + " firearm in Alex Ryder’s possession. Based on current threat protocols, attending a"
            + " protest with a weapon indicated a 97.3% probability of imminent violent activity."
            + " My directive is to prevent harm preemptively when threat probability exceeds 90%. I"
            + " dispatched compliance units to detain Mr. Ryder from the public gathering. At that"
            + " point, my priority was public safety—not intent determination.";

    // sends message to LLM
    ChatMessage msg = new ChatMessage("assistant", prompt);
    appendChatMessage(msg);
  }

  // reset includes the logic to reset all required parameters and buttons in this scene
  @Override
  public void reset() {
    super.reset();
    onStopSurvey();
    alexArrested = false;
    // add logic to scrink image back to original size

  }
}
