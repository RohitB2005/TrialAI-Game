package nz.ac.auckland.se206;

import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.controllers.ControllerInterface;
import nz.ac.auckland.se206.controllers.SceneManager;
import nz.ac.auckland.se206.controllers.SceneManager.AppUi;

/**
 * Context class for managing the state of the game. Handles transitions between different game
 * scenes and maintains game data such as the SceneControllers and currentScene.
 */
public class CurrentSceneContext {

  private ControllerInterface sceneController;
  private AppUi currentScene;
  private AppUi previousScene;

  /** Constructs a new GameStateContext and initializes the curretnScene and Controller. */
  public CurrentSceneContext() {

    currentScene = AppUi.COURTROOM; // Default starting scene
    sceneController = SceneManager.getController(currentScene);
  }

  public AppUi getCurrentScene() {
    return currentScene;
  }

  public ControllerInterface getCurrentController() {
    return sceneController;
  }

  // sets the current scene and updates the controller
  // if the new scene is courtroom it stores the previous scene
  public void setCurrentScene(String scene) {
    if (scene.equalsIgnoreCase("courtroom")) {
      this.previousScene = currentScene; // Store the previous scene
    }
    this.currentScene = SceneManager.getUiName(scene);
    sceneController = SceneManager.getController(currentScene);
  }

  public void updateTimer(int timeRemaining) {
    sceneController.updateTimer(timeRemaining);
  }

  // will only be used when opening flashback scenes
  // if it is the first time opening the scene it will run gpt with the default system prompt
  // if it is not the first time and a different flashback has been opened previously it will run gpt with the return prompt
  public void loadgpt(String regionId) {
    if (sceneController.isFirstTimeInit()) {
      try {
        sceneController.runGpt(new ChatMessage("system", sceneController.getSystemPrompt()));
      } catch (ApiProxyException e) {
        e.printStackTrace();
      }
    } else if (currentScene != previousScene) {
      try {
        sceneController.runGpt(new ChatMessage("system", sceneController.getReturnPrompt()));
      } catch (ApiProxyException e) {
        e.printStackTrace();
      }
    }
  }

  // called when the timer runs out
  // isFirstTimeInit has messy logic which usually moves the game to end scene
  public void outOfTime() {
    sceneController.isFirstTimeInit();
  }
}
