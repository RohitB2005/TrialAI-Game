package nz.ac.auckland.se206.controllers;

public interface ControllerInterface {

  void onEnter();
  void onExit();
  void updateTimer(int timeRemaining);
}
