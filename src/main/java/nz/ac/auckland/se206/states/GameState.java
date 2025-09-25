package nz.ac.auckland.se206.states;

// State interface for the game, defines entering, exiting and pulse handling methods
public interface GameState {

  void onEnter();

  void onExit();

  void onPulse(int timeRemaining);
}
