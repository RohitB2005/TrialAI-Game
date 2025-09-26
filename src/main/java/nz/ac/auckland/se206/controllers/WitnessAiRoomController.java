package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.prompts.PromptEngineering;

public class WitnessAiRoomController extends ChatController {

  private String person = "witnessAi";
  private String name = "Alpha-Ai";
  private int horizontalVelocity = 0;
  private int verticalVelocity = 0;
  private List<Rectangle> platforms;
  private Image playerImage;
  private int tileSize = 16;
  private int restCount = 0;
  private boolean playerDead = false;
  private int deathCount = 0;
  private int platform1HorizontalVelocity = 1;
  private int platform2HorizontalVelocity = -1;
  private Boolean hasWon = false;

  @FXML private ImageView player;
  @FXML private Rectangle rectPlatform1;
  @FXML private Rectangle rectPlatform2;
  @FXML private Rectangle rect1;
  @FXML private Rectangle rect2;
  @FXML private Rectangle rect3;
  @FXML private Rectangle rect4;
  @FXML private Rectangle rect5;
  @FXML private Rectangle rect6;
  @FXML private Button btnStart;
  @FXML private Label labelFinal;
  @FXML private Label labelInstructions;

  private Timeline timer =
      new Timeline(
          new KeyFrame(
              Duration.millis(16), // roughly 60 FPS
              event -> {
                movePlayer();
                setImage();
                movePlatforms();
              }));

  // initialize sets all required fields for the scene when opened
  @FXML
  void initialize() {

    // create list to store all platforms in game, then add each of them
    platforms = new ArrayList<>();
    platforms.add(rectPlatform1);
    platforms.add(rectPlatform2);
    platforms.add(rect1);
    platforms.add(rect2);
    platforms.add(rect3);
    platforms.add(rect4);
    platforms.add(rect5);
    platforms.add(rect6);

    // sets player image and timer cycles logic
    areaInputText.requestFocus();
    playerImage = new Image(getClass().getResourceAsStream("/images/Player.png"));
    setImage();
    timer.setCycleCount(Timeline.INDEFINITE);
  }

  // loads and plays a stored tts file for the ai witness when the scene is opened for the first
  // time
  @Override
  public String getPrompt() {
    if (isFirstTimeInit) {
      isFirstTimeInit = false;

      // call the helper method to play mp3
      playWelcomeSound("Alpha.mp3");
      areaInputText.requestFocus();
      return PromptEngineering.getPrompt(person + ".txt");
    } else {

      // initialise with prompt once more
      return "you are now Alpha-Ai again, the expert witness Ai, you may make a comment on what the"
          + " other witness have said or wait till you are asked a question";
    }
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getParticipantId() {
    return "WitnessAi";
  }

  // custom reset logic for this specific scene with relevant parameters
  @Override
  public void reset() {

    // call on interface reset
    super.reset();

    // reset visibility of buttons and positions of platforms and character, similar to initialise
    // but for replays
    btnStart.setVisible(true);
    btnStart.setText("Begin Infiltration");
    playerDead = false;
    player.setX(20);
    player.setY(675);
    player.setVisible(false);
    timer.stop();
    rectPlatform1.setX(5);
    rectPlatform2.setX(195);
    platform1HorizontalVelocity = 1;
    platform2HorizontalVelocity = -1;

    // reset deaths and label text, as well as boolean statuses associated with game
    deathCount = 0;
    labelFinal.setText("Sentinel 12 Logic Centre");
    labelInstructions.setVisible(false);
    hasWon = false;
  }

  // logic for starting the memory game and initialising fields needed
  @FXML
  private void onStart() {
    btnStart.setVisible(false);
    areaInputText.requestFocus();
    if (timer != null && !timer.getStatus().equals(Timeline.Status.RUNNING)) {
      timer.play();
    }

    // set position, health status, as well as velocity and other parameters to start game
    playerDead = false;
    player.setX(20);
    player.setY(675);
    player.setVisible(true);
    horizontalVelocity = 0;
    verticalVelocity = 0;
    restCount = 0;

    // text and image included in memory
    labelFinal.setText("Sentinel 12 Logic Centre");
    labelInstructions.setVisible(true);
    setImage();
  }

  // set logic for controls in the memory game
  @Override
  @FXML
  protected void onEnterPressed(KeyEvent event) throws ApiProxyException, IOException {
    super.onEnterPressed(event);
    switch (event.getCode()) {

      // up sets positive vertical velocity, down is negative, right is positive horizontal
      // velocity, left is negative
      case UP:
        verticalVelocity = -4;
        break;
      case DOWN:
        verticalVelocity = 4;
        break;
      case LEFT:
        horizontalVelocity = -4;
        break;
      case RIGHT:
        horizontalVelocity = 4;
        break;
      default:
        break;
    }
  }

  // tracks key pressed for memory game and sets velocity
  @FXML
  private void onKeyRelease(KeyEvent event) {

    // initialise horizontal velocity for X directions, vertical velocity for Y
    switch (event.getCode()) {
      case UP:
      case DOWN:
        verticalVelocity = 0;
        break;
      case LEFT:
      case RIGHT:
        horizontalVelocity = 0;
        break;
      default:
        break;
    }
  }

  private void movePlayer() {
    if (playerDead) {
      return;
    }
    player.setX(player.getX() + horizontalVelocity);
    player.setY(player.getY() + verticalVelocity);

    if (player.getY() < 20) {
      // Player reached the top, win condition
      timer.stop();
      btnStart.setVisible(true);
      labelFinal.setText("Infiltration Successful!!");
      btnStart.setText("Restart?");
      playerDead = true; // Prevent further movement
      if (!hasWon) {
        ChatMessage systemMessage =
            new ChatMessage(
                "assistant",
                "Information recovered regarding Alex Ryder. Using current law enforcement"
                    + " protocols there is no reason to believe he would act outside peaceful"
                    + " protesting parameters. Compared to Sentinel 12 logic it is obvious the"
                    + " algorithm used is out of date and using pre AI revolution data. Alex Ryder"
                    + " is not a threat to national security and should be released immediately.");
        appendChatMessage(systemMessage);
        chatCompletionRequest.addMessage(systemMessage);
      }
      hasWon = true;
      return;
    }

    // Check for collisions with platforms
    if (player.getX() < 1 || player.getX() > 235 || player.getY() < 1 || player.getY() > 680) {
      player.setX(player.getX() - horizontalVelocity);
      player.setY(player.getY() - verticalVelocity);
    }

    for (Rectangle platform : platforms) {
      if (player.getBoundsInParent().intersects(platform.getBoundsInParent())) {
        playerDead = true;
        btnStart.setVisible(true);
        btnStart.setText("You Died! Restart?");
        break;
      }
    }
  }

  // method to set the image based on its pixel position
  private void setImage() {
    if (!playerDead) {

      // use pixel reader when a player is alive to set image
      restCount = (restCount + 1) % 32;
      if (restCount % 8 == 0) {
        WritableImage img =
            new WritableImage(
                playerImage.getPixelReader(),
                restCount / 4 * tileSize + 9,
                16,
                tileSize - 2,
                tileSize);
        player.setImage(img);
      }
    } else {

      // check number of deaths and set image and fields based on that
      deathCount++;
      if (deathCount == 1 || deathCount == 11) {
        WritableImage img =
            new WritableImage(
                playerImage.getPixelReader(),
                deathCount / 10 * 2 * tileSize + 8,
                206,
                tileSize,
                tileSize);
        player.setImage(img);
      } else if (deathCount > 20) {

        // reset logic for game
        player.setVisible(false);
        timer.stop();
        deathCount = 0;
      }
    }
  }

  private void movePlatforms() {
    // Move platform 1
    rectPlatform1.setX(rectPlatform1.getX() + platform1HorizontalVelocity);
    if (rectPlatform1.getX() > 195) {
      platform1HorizontalVelocity = -1;
    } else if (rectPlatform1.getX() < 5) {
      platform1HorizontalVelocity = 1;
    }

    // Move platform 2
    rectPlatform2.setX(rectPlatform2.getX() + platform2HorizontalVelocity);
    if (rectPlatform2.getX() > 195) {
      platform2HorizontalVelocity = -1;
    } else if (rectPlatform2.getX() < 5) {
      platform2HorizontalVelocity = 1;
    }
  }

  // method handles logic for returning to room
  @FXML
  @Override
  protected void onReturn(ActionEvent event) throws ApiProxyException, IOException {

    // call interface with master return method, then set required components' visibility
    super.onReturn(event);
    timer.stop();
    btnStart.setVisible(true);
    btnStart.setText("Begin Infiltration");
    player.setVisible(false);
  }
}
