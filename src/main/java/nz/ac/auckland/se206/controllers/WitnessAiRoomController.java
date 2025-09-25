package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.prompts.PromptEngineering;

public class WitnessAiRoomController extends ChatController {

  private String person = "witnessAi";
  private String name = "Alpha-Ai";
  private Media media;
  private MediaPlayer mediaPlayerWelcome;
  private int xVelocity = 0;
  private int yVelocity = 0;
  private List<Rectangle> platforms;

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
  private Image playerImage;
  private int tileSize = 16;
  private int restCount = 0;



  @FXML void initialize(){
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

    if (mediaPlayerWelcome != null) {
      mediaPlayerWelcome.stop();
    }
  }

  @FXML private void onStart() {
    btnStart.setVisible(false);
    areaInputText.requestFocus();
    if (timer != null && !timer.getStatus().equals(Timeline.Status.RUNNING)) {
      timer.play();
    }
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
    if (timer != null && !timer.getStatus().equals(Timeline.Status.RUNNING)) {
      timer.play();
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
    setImage();
    player.setX(player.getX() + xVelocity);
    player.setY(player.getY() + yVelocity);

    // Check for collisions with platforms
    if (player.getX() < 1 || player.getX() > 235 || player.getY() < 1 || player.getY() > 680) {
      player.setX(player.getX() - xVelocity);
      player.setY(player.getY() - yVelocity);
      System.out.println("Out of bounds X=" + player.getX() + " Y=" + player.getY());
    }

    for (Rectangle platform : platforms) {
      if (player.getBoundsInParent().intersects(platform.getBoundsInParent())) {
        player.setX(player.getX() - xVelocity);
        player.setY(player.getY() - yVelocity);
        System.out.println("Collision detected with platform");
        break;
      }
    }
  }

  private void setImage() {
    if (xVelocity == 0) {
      restCount = (restCount + 1) % 32;
    } else {
      restCount = 0;
    }
    if (restCount % 8 == 0) {
      WritableImage img = new WritableImage(
          playerImage.getPixelReader(), restCount/4 * tileSize + 8, 16, tileSize, tileSize);
      player.setImage(img);
    }
  }

  private Timeline timer =
      new Timeline(
          new KeyFrame(
              Duration.millis(16), // roughly 60 FPS
              event -> movePlayer()));
}
