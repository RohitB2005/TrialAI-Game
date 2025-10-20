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
  private String name = "Alex Ryder: ";
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
  @FXML private Label instructionLabel;

  @FXML
  public void initialize() {
    // Prepare initial hidden state; popup is triggered from getPrompt() when the scene is active
    if (instructionLabel != null) {
      instructionLabel.setVisible(false);
      instructionLabel.setOpacity(0);
      instructionLabel.setScaleX(0.9);
      instructionLabel.setScaleY(0.9);
    }
  }

  // loads and plays a stored tts file for the human witness when the scene is opened for the first
  // time
  @Override
  public String getPrompt() {
    if (isFirstTimeInit) {
      isFirstTimeInit = false;

      // call the helper method to play mp3
      playWelcomeSound("Alex_Ryder.mp3");
      areaInputText.requestFocus();
      // ensure popup runs when the scene is actually activated
      playInstructionsPopup(instructionLabel);
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

  // method to play the bounce animation for arrows around bag
  private void startBounceAnimation() {
    if (bagClickCount >= 3) {
      return;
    }

    // set visibility of arrows(initially invisible)
    leftArrow.setVisible(true);
    rightArrow.setVisible(true);

    // create new transition type, set x values and infinite cycling, and animation styling
    leftBounceAnimation = new TranslateTransition(Duration.millis(600), leftArrow);
    leftBounceAnimation.setFromX(0);
    leftBounceAnimation.setToX(10);
    leftBounceAnimation.setAutoReverse(true);
    leftBounceAnimation.setCycleCount(TranslateTransition.INDEFINITE);
    leftBounceAnimation.setInterpolator(Interpolator.EASE_BOTH);

    // create transition type for right arrow animation, setting same parameters for cycling, x
    // values, and animations
    rightBounceAnimation = new TranslateTransition(Duration.millis(600), rightArrow);
    rightBounceAnimation.setFromX(0);
    rightBounceAnimation.setToX(-10);
    rightBounceAnimation.setAutoReverse(true);
    rightBounceAnimation.setCycleCount(TranslateTransition.INDEFINITE);
    rightBounceAnimation.setInterpolator(Interpolator.EASE_BOTH);

    // play bounce animation for arrows
    leftBounceAnimation.play();
    rightBounceAnimation.play();
  }

  // method to stop the bounce animation from cycling
  private void stopBounceAnimation() {
    // when not hovered, disable the animation and set position
    if (leftBounceAnimation != null) {
      leftBounceAnimation.stop();
      leftBounceAnimation.getNode().setTranslateX(0);
      leftBounceAnimation = null;
    }

    // same check for the right arrow, disable animation and set position
    if (rightBounceAnimation != null) {
      rightBounceAnimation.stop();
      rightBounceAnimation.getNode().setTranslateX(0);
      rightBounceAnimation = null;
    }

    // disable visibility of arrows once animations stop
    leftArrow.setVisible(false);
    rightArrow.setVisible(false);
  }

  // draggable animation for items in memory
  @FXML
  public void onDrag(MouseEvent event) throws ApiProxyException {
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

    if (draggedImage.getBoundsInParent().intersects(counterLabel.getBoundsInParent())
        && draggedImage.isVisible()) {
      draggedImage.setVisible(false);
      itemStoredCount++;
      counterLabel.setText(itemStoredCount + "/3");
      if (itemStoredCount == 3) {
        // append new message to chat and send to LLM as prompt to provide further context
        ChatMessage msg =
            new ChatMessage(
                "assistant",
                "The spray can was for making protest signs nothing more. The bandanna was to cover"
                    + " my face from dust and paint fumes, not to hide my identity.\n"
                    + "And the airsoft gun? It was a prop for a street performance piece, part of"
                    + " the protest’s message about police overreach.\n"
                    + "None of it was meant to harm anyone. Sentinel-12 saw what it wanted to see,"
                    + " not the truth.\n"
                    + "I was exercising my right to protest, not planning a crime.");
        appendChatMessage(msg);
        chatCompletionRequest.addMessage(msg);
      }
    }
  }
}
