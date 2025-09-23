package nz.ac.auckland.se206.states;

import nz.ac.auckland.se206.controllers.FlashbackController;
import nz.ac.auckland.se206.controllers.SceneManager;
import nz.ac.auckland.se206.controllers.SceneManager.AppUi;

public class FlashbackState implements GameState {

  private FlashbackController controller;

  public FlashbackState() {
    this.controller = (FlashbackController) SceneManager.getController(AppUi.FLASHBACK);
  }

  @Override
  public void onEnter() {
    System.out.println("Entering Flashback State");
  }

  @Override
  public void onExit() {
    System.out.println("Exiting Flashback State");
  }

  @Override
  public void onPulse(int timeRemaining) {
    controller.updateTimer(timeRemaining);
  }
}
