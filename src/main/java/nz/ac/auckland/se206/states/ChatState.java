package nz.ac.auckland.se206.states;

import nz.ac.auckland.se206.controllers.ChatController;
import nz.ac.auckland.se206.controllers.SceneManager;
import nz.ac.auckland.se206.controllers.SceneManager.AppUi;

public class ChatState implements GameState {

  private ChatController controller;

  // finds and load the appropriate chat controller based on the ui passed in
  // used for all memories
  public ChatState(AppUi ui) {
    controller = (ChatController) SceneManager.getController(ui);
  }

  // logic to use prompt to send message to llm
  @Override
  public void onEnter() {
    return;
  }

  @Override
  public void onExit() {
    // Logic to execute when exiting the Chat state
  }

  @Override
  public void onPulse(int timeRemaining) {
    controller.updateTimer(timeRemaining);
  }
}
