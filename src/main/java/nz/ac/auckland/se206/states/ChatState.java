package nz.ac.auckland.se206.states;

import nz.ac.auckland.se206.controllers.ChatController;
import nz.ac.auckland.se206.controllers.SceneManager;
import nz.ac.auckland.se206.controllers.SceneManager.AppUi;

public class ChatState implements GameState {

  private ChatController controller;

  public ChatState(AppUi ui) {
    controller = (ChatController) SceneManager.getController(ui);
  }

  @Override
  public void onEnter() {
    // Logic to execute when entering the Chat state
  }

  @Override
  public void onExit() {
    // Logic to execute when exiting the Chat state
  }

  @Override
  public void onPulse(int timeRemaining) {
    // Logic to execute on each pulse while in the Chat state
  }
  
}
