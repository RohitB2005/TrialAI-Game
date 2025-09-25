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
  private int xVelocity = 0;
  private int yVelocity = 0;
  private List<Rectangle> platforms;
  private Image playerImage;
  private int tileSize = 16;
  private int restCount = 0;
  private boolean playerDead = false;
  private int deathCount = 0;
  private int platform1XVelocity = 1;
  private int platform2XVelocity = -1;
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

  @FXML
  void initialize() {
    platforms = new ArrayList<>();
    platforms.add(rectPlatform1);
    platforms.add(rectPlatform2);
    platforms.add(rect1);
    platforms.add(rect2);
    platforms.add(rect3);
    platforms.add(rect4);
    platforms.add(rect5);
    platforms.add(rect6);
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
      // try {
      //   media = new Media(App.class.getResource("/sounds/Alpha.mp3").toURI().toString());
      //   mediaPlayerWelcome = new MediaPlayer(media);
      //   mediaPlayerWelcome.play();
      // } catch (URISyntaxException e) {
      //   e.printStackTrace();
      // }
      areaInputText.requestFocus();
      return PromptEngineering.getPrompt(person + ".txt");
    } else {
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

  @Override
  public void reset() {
    super.reset();

    btnStart.setVisible(true);
    btnStart.setText("Begin Infiltration");
    playerDead = false;
    player.setX(20);
    player.setY(675);
    player.setVisible(false);
    timer.stop();
    rectPlatform1.setX(5);
    rectPlatform2.setX(195);
    platform1XVelocity = 1;
    platform2XVelocity = -1;
    deathCount = 0;
    labelFinal.setText("Sentinel 12 Logic Centre");
    labelInstructions.setVisible(false);
    hasWon = false;
  }

  @FXML
  private void onStart() {
    btnStart.setVisible(false);
    areaInputText.requestFocus();
    if (timer != null && !timer.getStatus().equals(Timeline.Status.RUNNING)) {
      timer.play();
    }
    playerDead = false;
    player.setX(20);
    player.setY(675);
    player.setVisible(true);
    xVelocity = 0;
    yVelocity = 0;
    restCount = 0;
    labelFinal.setText("Sentinel 12 Logic Centre");
    labelInstructions.setVisible(true);
    setImage();
  }

  @Override
  @FXML
  protected void onEnterPressed(KeyEvent event) throws ApiProxyException, IOException {
    super.onEnterPressed(event);
    switch (event.getCode()) {
      case UP:
        yVelocity = -4;
        break;
      case DOWN:
        yVelocity = 4;
        break;
      case LEFT:
        xVelocity = -4;
        break;
      case RIGHT:
        xVelocity = 4;
        break;
      default:
        break;
    }
  }

  @FXML
  private void onKeyRelease(KeyEvent event) {
    switch (event.getCode()) {
      case UP:
      case DOWN:
        yVelocity = 0;
        break;
      case LEFT:
      case RIGHT:
        xVelocity = 0;
        break;
      default:
        break;
    }
  }

  private void movePlayer() {
    if (playerDead) {
      return;
    }
    player.setX(player.getX() + xVelocity);
    player.setY(player.getY() + yVelocity);

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
      player.setX(player.getX() - xVelocity);
      player.setY(player.getY() - yVelocity);
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

  private void setImage() {
    if (!playerDead) {
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
        player.setVisible(false);
        timer.stop();
        deathCount = 0;
      }
    }
  }

  private void movePlatforms() {
    // Move platform 1
    rectPlatform1.setX(rectPlatform1.getX() + platform1XVelocity);
    if (rectPlatform1.getX() > 195) {
      platform1XVelocity = -1;
    } else if (rectPlatform1.getX() < 5) {
      platform1XVelocity = 1;
    }

    // Move platform 2
    rectPlatform2.setX(rectPlatform2.getX() + platform2XVelocity);
    if (rectPlatform2.getX() > 195) {
      platform2XVelocity = -1;
    } else if (rectPlatform2.getX() < 5) {
      platform2XVelocity = 1;
    }
  }

  private Timeline timer =
      new Timeline(
          new KeyFrame(
              Duration.millis(16), // roughly 60 FPS
              event -> {
                movePlayer();
                setImage();
                movePlatforms();
              }));

  @FXML
  protected void onReturn(ActionEvent event) throws ApiProxyException, IOException {
    super.onReturn(event);
    timer.stop();
    btnStart.setVisible(true);
    btnStart.setText("Begin Infiltration");
    player.setVisible(false);
  }
}
