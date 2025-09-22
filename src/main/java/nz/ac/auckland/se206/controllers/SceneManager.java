package nz.ac.auckland.se206.controllers;

import java.util.Collection;
import java.util.HashMap;
import javafx.scene.Parent;

public class SceneManager {

  public enum AppUi {
    COURTROOM,
    WITNESSAI,
    WITNESSHUMAN,
    TRIALAI,
    FINALROOM
  }

  // Maps to hold scene roots and their corresponding controllers
  private static HashMap<AppUi, Parent> sceneMap = new HashMap<AppUi, Parent>();
  private static HashMap<AppUi, ControllerInterface> controllerMap =
      new HashMap<AppUi, ControllerInterface>();

  public static void registerController(AppUi sceneName, ControllerInterface controller) {
    controllerMap.put(sceneName, controller);
  }

  public static Collection<ControllerInterface> getAllControllers() {
    return controllerMap.values();
  }

  public static void registerUi(AppUi sceneName, Parent uiRoot) {
    sceneMap.put(sceneName, uiRoot);
  }

  public static Parent getUiRoot(AppUi sceneName) {
    return sceneMap.get(sceneName);
  }

  public static ControllerInterface getController(AppUi currentScene) {
    return controllerMap.get(currentScene);
  }

  public static AppUi getUiName(String sceneName) {
    for (AppUi ui : AppUi.values()) {
      if (ui.name().equalsIgnoreCase(sceneName)) {
        return ui;
      }
    }
    throw new IllegalArgumentException("No AppUi found for name: " + sceneName);
  }

  // Converts AppUi enum to a designated Fxml file name
  public static String toFxmlString(AppUi appUi) {
    switch (appUi) {
      case COURTROOM:
        return "courtroom";
      case WITNESSAI:
        return "witnessAiRoom";
      case WITNESSHUMAN:
        return "witnessHumanRoom";
      case TRIALAI:
        return "trialAiRoom";
      case FINALROOM:
        return "finalRoom";
      // Add more cases as needed for other scenes
      default:
        throw new IllegalArgumentException("Unknown AppUi: " + appUi);
    }
  }
}
