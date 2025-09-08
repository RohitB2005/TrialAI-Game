package nz.ac.auckland.se206.controllers;

import java.net.URISyntaxException;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.prompts.PromptEngineering;

public class WitnessHumanRoomController extends ChatController {

  private String person = "witnessHuman";
  private String name = "Alex Ryder";
  private Media media;
  private MediaPlayer mediaPlayerWelcome;

  /**
   * Generates the system prompt based on the profession.
   *
   * @return the system prompt string
   */
  // loads and plays a stored tts file for the human witness when the scene is opened for the first time
  @Override
  public String getSystemPrompt() {
    // try {
    //   media = new Media(App.class.getResource("/sounds/Alex_Ryder.mp3").toURI().toString());
    //   mediaPlayerWelcome = new MediaPlayer(media);
    //   mediaPlayerWelcome.play();
    // } catch (URISyntaxException e) {
    //   e.printStackTrace();
    // }
    return PromptEngineering.getPrompt(person + ".txt");
  }

  @Override
  public String getName() {
    return name;
  }

  // logic for when the player returns to this scene after visiting another flashback
  @Override
  public String getReturnPrompt() {
    return "you are now Alex Ryder again, the human victim, you may make a comment on what the"
        + " other witness have said or wait till you are asked a question";
  }
}
