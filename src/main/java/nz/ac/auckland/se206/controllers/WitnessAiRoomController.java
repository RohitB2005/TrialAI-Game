package nz.ac.auckland.se206.controllers;

import java.net.URISyntaxException;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.prompts.PromptEngineering;

public class WitnessAiRoomController extends ChatController {

  private String person = "witnessAi";
  private String name = "Alpha-Ai";
  private Media media;
  private MediaPlayer mediaPlayerWelcome;


  // loads and plays a stored tts file for the ai witness when the scene is opened for the first time
  @Override
  public String getPrompt() {
    if (isFirstTimeInit) {
      isFirstTimeInit = false;
    // try {
    //   media = new Media(App.class.getResource("/sounds/Alpha.mp3").toURI().toString());
    //   mediaPlayerWelcome = new MediaPlayer(media);
    //   mediaPlayerWelcome.play();
    // } catch (URISyntaxException e) {
    //   e.printStackTrace();
    // }
    return PromptEngineering.getPrompt(person + ".txt");
    } else {
      return "you are now Alpha-Ai again, the expert witness Ai, you may make a comment on what the"
          + " other witness have said or wait till you are asked a question";
    }
  }

  @Override
  public String getName() {
    return name;
  }
}
