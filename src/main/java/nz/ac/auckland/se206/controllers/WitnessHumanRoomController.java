package nz.ac.auckland.se206.controllers;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.util.Duration;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.prompts.PromptEngineering;

public class WitnessHumanRoomController extends ChatController {

  private String person = "witnessHuman";
  private String name = "Alex Ryder";
  private TranslateTransition leftBounceAnimation;
  private TranslateTransition rightBounceAnimation;
  private int bagClickCount = 0;
  private int itemStoredCount = 0;

  @FXML private ImageView viewSpraycan;
  @FXML private ImageView viewBandanna;
  @FXML private ImageView viewGun;
  @FXML private Region regionBag;
  @FXML private ImageView rightArrow;
  @FXML private ImageView leftArrow;
  @FXML private Label counterLabel;

  // loads and plays a stored tts file for the human witness when the scene is opened for the first
  // time
  @Override
  public String getPrompt() {
    if (isFirstTimeInit) {
      isFirstTimeInit = false;

      // call the helper method to play mp3
      playWelcomeSound("Alex_Ryder.mp3");
      areaInputText.requestFocus();
      return PromptEngineering.getPrompt(person + ".txt");
    } else {

      // initialise with prompt once more
      return "you are now Alex Ryder again, the human victim, you may make a comment on what the"
          + " other witness have said or wait till you are asked a question";
    }
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getParticipantId() {
    return "WitnessHuman";
  }

  // simple reset calls on interface and sets visibility of memoru components
  @Override
  public void reset() {

    // calls on interface to implement reset, then sets visibility of needed components
    super.reset();
    viewSpraycan.setVisible(false);
    viewBandanna.setVisible(false);
    viewGun.setVisible(false);
    bagClickCount = 0;
    itemStoredCount = 0;
    counterLabel.setText("0/3");

    // set a custom cursor on hover and labels visibility
    regionBag.setCursor(javafx.scene.Cursor.HAND);
  }

  // logic for clicking bag for items in memory
  @FXML
  public void onBagClick(MouseEvent event) throws ApiProxyException {
    double parentX =
        viewSpraycan.getParent().sceneToLocal(event.getSceneX(), event.getSceneY()).getX();
    double parentY =
        viewSpraycan.getParent().sceneToLocal(event.getSceneX(), event.getSceneY()).getY();

    // if statement to set position and visibility of spray can, increment indicator
    if (bagClickCount == 0) {
      viewSpraycan.setVisible(true);
      viewSpraycan.setLayoutX(parentX - 30);
      viewSpraycan.setLayoutY(parentY - 40);
    } else if (bagClickCount == 1) {

      // set visibility and position of bandana next, increment count
      viewBandanna.setVisible(true);
      viewBandanna.setLayoutX(parentX - 30);
      viewBandanna.setLayoutY(parentY - 40);
    } else if (bagClickCount == 2) {

      // final item gun taken from bag, position and visibility set
      viewGun.setVisible(true);
      viewGun.setLayoutX(parentX - 30);
      viewGun.setLayoutY(parentY - 40);
      regionBag.setCursor(null);

      // append new message to chat and send to LLM as prompt to provide further context
      ChatMessage msg =
          new ChatMessage(
              "system",
              "The interviewer has found a spray can, Bandanna and Airsoft gun in your bag, you"
                  + " must now defend yourself over why they were there on the day you wer"
                  + " areested");
      appendChatMessage(msg);
      runGpt(msg);
    }
    bagClickCount++;
  }

  @FXML
  public void onHover() {
    startBounceAnimation();
  }

    @FXML
  private void onMouseExit(MouseEvent event) {
    stopBounceAnimation();
  }

  private void startBounceAnimation() {
    if (bagClickCount >= 3) {
      return;
    }

    leftArrow.setVisible(true);
    rightArrow.setVisible(true);

    leftBounceAnimation = new TranslateTransition(Duration.millis(600), leftArrow);
    leftBounceAnimation.setFromX(0);
    leftBounceAnimation.setToX(10);
    leftBounceAnimation.setAutoReverse(true);
    leftBounceAnimation.setCycleCount(TranslateTransition.INDEFINITE);
    leftBounceAnimation.setInterpolator(Interpolator.EASE_BOTH);

    rightBounceAnimation = new TranslateTransition(Duration.millis(600), rightArrow);
    rightBounceAnimation.setFromX(0);
    rightBounceAnimation.setToX(-10);
    rightBounceAnimation.setAutoReverse(true);
    rightBounceAnimation.setCycleCount(TranslateTransition.INDEFINITE);
    rightBounceAnimation.setInterpolator(Interpolator.EASE_BOTH);
    
    leftBounceAnimation.play();
    rightBounceAnimation.play();

  }

  private void stopBounceAnimation() {
    if (leftBounceAnimation != null) {
      leftBounceAnimation.stop();
      leftBounceAnimation.getNode().setTranslateX(0);
      leftBounceAnimation = null;
    }
    if (rightBounceAnimation != null) {
      rightBounceAnimation.stop();
      rightBounceAnimation.getNode().setTranslateX(0);
      rightBounceAnimation = null;
    }
    leftArrow.setVisible(false);
    rightArrow.setVisible(false);
  }

  // draggable animation for items in memory
  @FXML
  public void onDrag(MouseEvent event) {
    ImageView draggedImage = (ImageView) event.getSource();
    double parentX =
        viewSpraycan.getParent().sceneToLocal(event.getSceneX(), event.getSceneY()).getX();
    double parentY =
        viewSpraycan.getParent().sceneToLocal(event.getSceneX(), event.getSceneY()).getY();

    
    
    // set positions of separate images
    if (draggedImage == viewGun) {
      draggedImage.setLayoutX(parentX - 80);
      draggedImage.setLayoutY(parentY - 30);
    } else {
      draggedImage.setLayoutX(parentX - 50);
      draggedImage.setLayoutY(parentY - 80);
    }

    if (draggedImage.getBoundsInParent().intersects(counterLabel.getBoundsInParent()) && draggedImage.isVisible()) {
      draggedImage.setVisible(false);
      itemStoredCount++;
      counterLabel.setText(itemStoredCount + "/3");
    }
  }
}
