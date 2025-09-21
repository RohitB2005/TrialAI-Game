package nz.ac.auckland.se206.states;

import java.io.IOException;
import javafx.scene.input.MouseEvent;

// State interface for the game, defines entering, exiting and pulse handling methods
public interface GameState {

  void onEnter();
  void onExit();
  void onPulse(int timeRemaining);
}
