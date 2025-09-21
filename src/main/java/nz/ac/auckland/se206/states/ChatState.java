package nz.ac.auckland.se206.states;

import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
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
    String prompt = controller.getPrompt();
    ChatMessage msg = new ChatMessage("system", prompt);
    try {
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
