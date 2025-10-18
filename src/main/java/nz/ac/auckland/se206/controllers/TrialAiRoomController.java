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
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
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

  private String person = "trialAi";
  private String name = "Sentinal-12";
  private int imageCount = 0;
  private boolean alexArrested = false;
  private int timeRemaining = 60; // seconds
  private int framesRemaining = 50;
  private List<Image> images;

  @FXML
  public void initialize() {

    images = new ArrayList<Image>();
    images.add(new Image("/images/peacefulprotest.png"));
    images.add(new Image("/images/nightprotest.png"));
    images.add(new Image("/images/violentprotest.png"));
    images.add(new Image("/images/alexprotest.png"));
  }

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

  // loads and plays a stored tts file and prompt for the on-trial ai when the scene is opened for
  // the first time
  @Override
  public String getPrompt() {
    if (isFirstTimeInit) {
      isFirstTimeInit = false;

      // Play the welcome sound MP3
      playWelcomeSound("Sentinal_12.mp3");

      areaInputText.requestFocus();
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

  // selects decision with event to record answer
  @FXML
  private void onDecisionBtnClick(Event event) {

    Button clickedButton = (Button) event.getSource();
    if (imageCount < 4) {
      updateGameTimer();
    } else if (!alexArrested) {
      if (clickedButton == btnArrestAlex) {
        btnObserveAlex.setVisible(false);
      } else {
        btnArrestAlex.setVisible(false);
      }
      alexArrested = true;
      clickedButton.setStyle("-fx-background-color: #00bfff; -fx-text-fill: black;");
      lastResponce();
    }
    nextImage();
  }

  @FXML
  private void onSurvey() {
    btnSurvey.setVisible(false);
    nextImage();
    updateGameTimer();
    labelSecurity.setText("choose to arrest or observe the suspects");
    labelScene.setVisible(true);
    btnSurvey1.setVisible(true);
  }

  @FXML
  private void onStopSurvey() {
    btnSurvey1.setVisible(false);
    btnSurvey.setVisible(true);
    timer.stop();
    imageAlex.setVisible(false);
    imageScreen.setVisible(false);
    labelScene.setVisible(false);
    btnArrestAlex.setVisible(false);
    btnObserveAlex.setVisible(false);
    btnArrestAlex.setStyle("");
    btnObserveAlex.setStyle("");
    labelSecurity.setText("try to protect the city");
    imageCount = 0;
    imageScreen.setFitWidth(300);
    imageScreen.setFitHeight(150);
    imageScreen.setX(0);
    imageScreen.setY(0);

  }

  @FXML
  void nextImage() {
    timer.stop();

    if (imageCount == 0) {
      imageScreen.setVisible(true);
      growImage(imageScreen);
    } else if (imageCount == 4) {
      return;
    }
    imageAlex.setImage(images.get(imageCount));
    timeRemaining = 120;
    updateGameTimer();
    timer.setCycleCount(timeRemaining);
    timer.play();
    imageCount++;
  }

  private void growImage(ImageView imageView) {

    double originalWidth = imageView.getFitWidth();
    double originalHeight = imageView.getFitHeight();

    framesRemaining = 55;

    Timeline growTimeline =
        new Timeline(
            new KeyFrame(
                Duration.millis(25),
                event -> {
                  double currentWidth = imageView.getFitWidth();
                  double currentHeight = imageView.getFitHeight();

                  imageView.setFitWidth(currentWidth + originalWidth * 0.05);
                  imageView.setFitHeight(currentHeight + originalHeight * 0.05);

                  imageView.setX(imageView.getX() - (originalWidth * 0.04) / 2);
                  imageView.setY(imageView.getY() - (originalHeight * 0.05) / 2);

                  framesRemaining--;

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

    if (alexArrested) {

      // human arrest case, shows agreement with defendant and prompt generated
      prompt =
          "The interviewer has reviewed your decisions using your monitering system, "
              + " and decided to arrest alex ryder when viewing the image of him filling his duffel"
              + " bag with protestor equipment. You must now comment of this development.";
    } else {

      // human not arrested case, disagreement with defendant and prompt generated
      prompt =
          "The interviewer has reviewed your decisions using your monitering system, and decided to"
              + " let alex ryder go when viewing the image of him filling his duffel bag with"
              + " protestor equipment. You must now defend your decisions.";
    }

    // sends message to LLM
    ChatMessage msg = new ChatMessage("system", prompt);
    appendChatMessage(msg);
    try {
      runGpt(msg);
    } catch (ApiProxyException e) {
      e.printStackTrace();
    }
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
