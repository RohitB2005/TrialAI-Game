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
import javafx.util.Duration;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.prompts.PromptEngineering;

public class WitnessAiRoomController extends ChatController {

  private String person = "witnessAi";
  private String name = "Alpha-Ai";
  private int horizontalVelocity = 0;
  private int verticalVelocity = 0;
  private List<Label> labels;
  private Image playerImage;
  private int tileSize = 16;
  private int restCount = 0;
  private boolean playerDead = false;
  private int deathCount = 0;
  private Boolean hasWon = false;
  private int[] textPos;
  private String text;
  private int textMove;

  @FXML private ImageView player;
  @FXML private Label gameLabel1;
  @FXML private Label gameLabel2;
  @FXML private Label gameLabel3;
  @FXML private Label gameLabel4;
  @FXML private Label gameLabel5;
  @FXML private Button btnStart;
  @FXML private Label labelFinal;
  @FXML private Label labelInstructions;
  @FXML private Label gameLabelStatic1;
  @FXML private Label gameLabelStatic2;

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
    labels = new ArrayList<>();
    labels.add(gameLabel1);
    labels.add(gameLabel2);
    labels.add(gameLabel3);
    labels.add(gameLabel4);
    labels.add(gameLabel5);
    labels.add(gameLabelStatic1);
    labels.add(gameLabelStatic2);

    textPos = new int[] {1, 5, 9, 13, 17, 0, 0};
    text =
        "        Intruder Detected. Initiating Security Protocols. All Systems Operational. Access Denied. "
            + "         Unauthorized Access Attempt Logged. Deploying Countermeasures. System"
            + " Integrity at 100%. Analyzing Threat Level. Activating Defense Mechanisms. "
            + "         Security Breach Contained. Monitoring Intruder Movements. Engaging       ";
    textMove = 0;

    movePlatforms();

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
    resetLabels();

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

    resetLabels();

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
                    + " algorithm used is out of date and due to bias against potential rioters."
                    + " Unlawfully used his home camera's to survey him while monitering");
        appendChatMessage(systemMessage);
        chatCompletionRequest.addMessage(systemMessage);
      }
      hasWon = true;
      return;
    }

    // Check for collisions with platforms
    if (player.getX() < 1 || player.getX() > 260 || player.getY() < 1 || player.getY() > 680) {
      player.setX(player.getX() - horizontalVelocity);
      player.setY(player.getY() - verticalVelocity);
    }

    for (Label label : labels) {
      if (player.getBoundsInParent().intersects(label.getBoundsInParent())) {
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
    int labelcount = 0;
    Label label;
    for (int i = 0; i < labels.size() - 2; i++) {
      label = labels.get(i);
      if (textMove % 10 == 0) {
        label.setText(text.substring(textPos[labelcount], textPos[labelcount] + 10));
        textPos[labelcount]++;
        if (textPos[labelcount] + 25 > text.length()) {
          textPos[labelcount] = 0;
        }
        if (label.getLayoutY() > 600) {
          label.setLayoutY(-100 * Math.random() * 7);
          label.setLayoutX(Math.random() * 50 + 50 * labelcount);
        }
      }
      label.setLayoutY(label.getLayoutY() + 3);
      labelcount++;
    }
    if (textMove % 10 == 0) {
      for (int i = labels.size() - 2; i < labels.size(); i++) {
        label = labels.get(i);
        label.setText(text.substring(textPos[0], textPos[0] + 25));
      }
    }
    textMove++;
  }

  private void resetLabels() {
    int count = 0;
    Label label;
    for (int i = 0; i < labels.size() - 2; i++) {
      label = labels.get(i);
      label.setLayoutY(-100 * Math.random() * 10);
      label.setLayoutX(Math.random() * 50 + 50 * count);
      count++;
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
