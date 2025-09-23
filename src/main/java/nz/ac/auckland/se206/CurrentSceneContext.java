package nz.ac.auckland.se206;

import nz.ac.auckland.se206.controllers.SceneManager.AppUi;
import nz.ac.auckland.se206.states.ChatState;
import nz.ac.auckland.se206.states.CourtroomState;
import nz.ac.auckland.se206.states.FinalState;
import nz.ac.auckland.se206.states.FlashbackState;
import nz.ac.auckland.se206.states.GameState;

/**
 * Context class for managing the state of the game. Handles transitions between different game
 * scenes and maintains game data such as the SceneControllers and currentScene.
 */
public class CurrentSceneContext {

  private GameState currentState;
  private GameState previousState;
  private GameState courtroom;
  private GameState finalRoom;
  private GameState chatWitnessAi;
  private GameState chatWitnessHuman;
  private GameState chatTrialAi;
  private GameState flashback;

  /** Loads all the game states for the different scenes and sets courtroom to default. */
  public CurrentSceneContext() {
    this.courtroom = new CourtroomState();
    this.finalRoom = new FinalState();
    this.chatWitnessAi = new ChatState(AppUi.WITNESSAI);
    this.chatWitnessHuman = new ChatState(AppUi.WITNESSHUMAN);
    this.chatTrialAi = new ChatState(AppUi.TRIALAI);
    this.flashback = new FlashbackState();
    this.currentState = courtroom; // Initial state
    this.previousState = null;
    currentState.onEnter();
  }

  public GameState getCurrentState() {
    return currentState;
  }

  public void reset() {
    this.currentState = courtroom;
    this.previousState = null;
    currentState.onEnter();
  }

  // sets the current scene and updates the controller
  // if the new scene is courtroom it stores the previous scene
  // triggers the onExit and onEnter methods for the states
  public void setCurrentScene(String scene) {
    if (currentState != null) {
      currentState.onExit();
    }
    if (currentState instanceof ChatState) {
      this.previousState = currentState; // Store the previous scene
    }
    this.currentState = getState(scene);
    if (currentState != previousState) {
      currentState.onEnter();
    }
  }

  // triggers the pulse method for the current state
  public void updateTimer(int timeRemaining) {
    currentState.onPulse(timeRemaining);
  }

  private GameState getState(String scene) {
    scene = scene.toUpperCase();
    switch (scene) {
      case "COURTROOM":
        return courtroom;
      case "FINALROOM":
        return finalRoom;
      case "WITNESSAI":
        return chatWitnessAi;
      case "WITNESSHUMAN":
        return chatWitnessHuman;
      case "TRIALAI":
        return chatTrialAi;
      case "FLASHBACK":
        return flashback;
      default:
        throw new IllegalArgumentException("Unknown scene: " + scene);
    }
  }
}
