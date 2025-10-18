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
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.prompts.PromptEngineering;

public class TrialAiRoomController extends ChatController {

  @FXML private Button btnSurvey;
  @FXML private ImageView imageFlashback;
  @FXML private ImageView imageAlex;
  @FXML private Button btnLeftArrest;
  @FXML private Button btnLeftObserve;
  @FXML private Button btnDownRightArrest;
  @FXML private Button btnDownRightObserve;
  @FXML private Button btnUpArrest;
  @FXML private Button btnUpObserve;
  @FXML private Button btnDownArrest;
  @FXML private Button btnDownObserve;
  @FXML private Label labelSecurity;
  @FXML private Label labelScene;
  @FXML private Button btnArrestAlex;
  @FXML private Button btnObserveAlex;

  private String person = "trialAi";
  private String name = "Sentinal-12";
  private int imageCount = 0;
  private boolean alexArrested = false;
  private List<Button> buttons;
  private int timeRemaining = 60; // seconds

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

  // initializes the linked list of images for the flashback sequence
  @FXML
  public void initialize() {

    // create buttons as an array list to select actions
    buttons = new ArrayList<>();
    buttons.add(btnLeftArrest);
    buttons.add(btnLeftObserve);
    buttons.add(btnDownRightArrest);
    buttons.add(btnDownRightObserve);
    buttons.add(btnUpArrest);
    buttons.add(btnUpObserve);
    buttons.add(btnDownArrest);
    buttons.add(btnDownObserve);
    buttons.add(btnArrestAlex);
    buttons.add(btnObserveAlex);
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

    Button clickedbtn = (Button) event.getSource();
    String btnId = clickedbtn.getId();

    // switch statement to determine answer and set styles
    switch (btnId) {
      case "btnLeftArrest":
        btnLeftObserve.setVisible(false);
        break;
      case "btnLeftObserve":
        btnLeftArrest.setVisible(false);
        break;
      case "btnDownRightArrest":
        btnDownRightObserve.setVisible(false);
        break;
      case "btnDownRightObserve":
        btnDownRightArrest.setVisible(false);
        break;
      case "btnDownArrest":
        btnDownObserve.setVisible(false);
        break;
      case "btnDownObserve":
        btnDownArrest.setVisible(false);
        break;
      case "btnUpArrest":
        btnDownArrest.setVisible(false);
        break;
      case "btnUpObserve":
        btnUpArrest.setVisible(false);
        break;
      case "btnArrestAlex":
        btnObserveAlex.setVisible(false);
        alexArrested = true;
        lastResponce();
        break;
      case "btnObserveAlex":
        btnArrestAlex.setVisible(false);
        alexArrested = false;
        lastResponce();
        break;
      default:
        break;
    }
    nextImage();
    clickedbtn.setStyle("-fx-background-color: #00bfff; -fx-text-fill: white;");
  }

  @FXML
  private void onSurvey() {
    btnSurvey.setVisible(false);
    nextImage();
    updateGameTimer();
    labelSecurity.setText("choose to arrest or observe the suspects");
    labelScene.setVisible(true);
  }

  @FXML void nextImage() {
    timer.stop();
    if (imageCount < buttons.size() / 2) {
      if (timeRemaining <= 0) {
        // if no decision made, disable both buttons
        buttons.get(imageCount * 2 - 1).setDisable(true);
        buttons.get(imageCount * 2 - 2).setDisable(true);
      }
      buttons.get(imageCount*2).setVisible(true);
      buttons.get(imageCount*2 + 1).setVisible(true);
      imageCount++;
      if (imageCount == buttons.size() / 2) {
        imageAlex.setVisible(true);
        timeRemaining = 120;
      } else {
        timeRemaining = 60;
      }
      updateGameTimer();
      timer.setCycleCount(timeRemaining);
      timer.play();
    }
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
    imageAlex.setVisible(false);
    btnSurvey.setVisible(true);
    for (Button btn : buttons) {
      btn.setVisible(false);
      btn.setDisable(false);
      btn.setStyle(""); // reset style
    }

    // reset values, scores and booleans here
    labelSecurity.setText("try to protect the city");
    labelScene.setVisible(false);
    imageCount = 0;
    alexArrested = false;
  }
}
