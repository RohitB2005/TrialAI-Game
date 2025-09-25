package nz.ac.auckland.se206;

import java.io.IOException;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import nz.ac.auckland.se206.controllers.ControllerInterface;
import nz.ac.auckland.se206.controllers.SceneManager;
import nz.ac.auckland.se206.controllers.SceneManager.AppUi;

/**
 * This is the entry point of the JavaFX application. This class initializes and runs the JavaFX
 * application.
 */
public class App extends Application {

  private static Scene scene;
  private static CurrentSceneContext context;
  private static int timeRemaining; // seconds
  private static Timeline timer;
  public static boolean isFinalScene = false;

  /**
   * The main method that launches the JavaFX application.
   *
   * @param args the command line arguments
   */
  public static void main(final String[] args) {
    launch();
  }

  // starts a countdown timer
  // on finishing the countdown will change the scene to the final room and start a 10 second timer
  // if the timer runs out in the final room the game will end
  public static void startTimer(int initialTime) {
    timeRemaining = initialTime;
    timer =
        new Timeline(
            new KeyFrame(
                Duration.seconds(1),
                event -> {
                  timeRemaining--;
                  context.updateTimer(timeRemaining);
                  // System.out.println("Time left: " + timeRemaining + "s");
                  if (timeRemaining <= 0) {
                    if (isFinalScene) {
                      stopTimer();
                      return;
                    }
                    timer.stop();
                    startTimer(60);
                    try {
                      isFinalScene = true;
                      openScene(scene, "finalRoom");
                    } catch (IOException e) {
                      e.printStackTrace();
                    }
                  }
                }));
    timer.setCycleCount(initialTime); // 120 seconds
    timer.play();
  }

  // sets currentscene in the context class and updates the timer before opening the new scene
  // if the scene is a flashback will prompt gpt to generate a response anything said in other
  // scenes.
  public static void openScene(Scene newScene, String regionId) throws IOException {
    context.setCurrentScene(regionId);
    context.updateTimer(timeRemaining);
    scene = newScene;
    scene.setRoot(SceneManager.getUiRoot(SceneManager.getUiName(regionId)));
  }

  // loads all scenes and controllers into SceneManager class
  // opens Courtroom by default and starts the timer
  @Override
  public void start(final Stage stage) throws IOException {
    for (AppUi ui : AppUi.values()) {
      FXMLLoader loader =
          new FXMLLoader(App.class.getResource("/fxml/" + SceneManager.toFxmlString(ui) + ".fxml"));
      SceneManager.registerUi(ui, loader.load());
      SceneManager.registerController(ui, (ControllerInterface) loader.getController());
    }
    context = new CurrentSceneContext();
    scene = new Scene(SceneManager.getUiRoot(AppUi.COURTROOM));
    stage.setScene(scene);
    stage.show();
    startTimer(300);
  }

  // stops the timer if it is running
  public static void stopTimer() {
    if (timer != null) {
      timer.stop();
      timer = null;
    }
  }

  public static String formatTime(int totalSeconds) {
    int minutes = totalSeconds / 60;
    int seconds = totalSeconds % 60;
    return String.format("%02d:%02d", minutes, seconds);
  }

  // master game reset command logic
  public static void resetGame() {
    System.out.println("DEBUG: MASTER RESET INITIATED IN APP.JAVA");
    for (ControllerInterface controller : SceneManager.getAllControllers()) {
      controller.reset();
    }

    // resets context and timer
    isFinalScene = false;
    context.reset();
    stopTimer();

    // set new scene, context and restart timer
    scene.setRoot(SceneManager.getUiRoot(AppUi.COURTROOM));
    context.setCurrentScene("courtRoom");
    startTimer(300);
  }
}
