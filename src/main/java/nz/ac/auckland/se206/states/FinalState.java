package nz.ac.auckland.se206.states;

import nz.ac.auckland.se206.controllers.FinalRoomController;
import nz.ac.auckland.se206.controllers.SceneManager;

public class FinalState implements GameState {

  private FinalRoomController controller;

  public FinalState() {
    controller = (FinalRoomController) SceneManager.getController(SceneManager.AppUi.FINALROOM);
  }

  @Override
  public void onEnter() {
    controller.handleEntry();
  }

  @Override
  public void onExit() {
    // Logic to execute when exiting the Final state
  }

  @Override
  public void onPulse(int timeRemaining) {
    controller.updateTimer(timeRemaining);
  }
}
