package nz.ac.auckland.se206.states;

import nz.ac.auckland.se206.controllers.CourtroomController;
import nz.ac.auckland.se206.controllers.SceneManager;

public class CourtroomState implements GameState {

  private CourtroomController controller;

  public CourtroomState() {
    controller = (CourtroomController) SceneManager.getController(SceneManager.AppUi.COURTROOM);
  }

  @Override
  public void onEnter() {
    // Logic to execute when entering the Courtroom state
  }

  @Override
  public void onExit() {
    // Logic to execute when exiting the Courtroom state
  }

  @Override
  public void onPulse(int timeRemaining) {
    controller.updateTimer(timeRemaining);
  }
  
}
