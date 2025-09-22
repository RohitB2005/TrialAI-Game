package nz.ac.auckland.se206.states;

import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
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

  @Override
  public void onEnter() {
    try {
      String prompt = controller.getPrompt();
      ChatMessage msg = new ChatMessage("system", prompt);
      controller.runGpt(msg);
    } catch (ApiProxyException e) {
      e.printStackTrace();
    }
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
