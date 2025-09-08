package nz.ac.auckland.se206.controllers;

import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;

public interface ControllerInterface {

  // interface used to controll all rooms using CurrentSceneContext class
  // thinking of adding game states which load a specific controller so controllers do not have to implement all methods

  void updateTimer(int timeRemaining);

  void runGpt(ChatMessage msg) throws ApiProxyException;

  boolean isFirstTimeInit();

  String getSystemPrompt();

  String getReturnPrompt();
}
