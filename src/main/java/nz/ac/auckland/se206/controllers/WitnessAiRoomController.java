package nz.ac.auckland.se206.controllers;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import nz.ac.auckland.se206.prompts.PromptEngineering;

public class WitnessAiRoomController extends ChatController {

  private String person = "witnessAi";
  private String name = "Alpha-Ai";
  private Media media;
  private MediaPlayer mediaPlayerWelcome;
  private static boolean isFirstTimeInit = true;

  // loads and plays a stored tts file for the ai witness when the scene is opened for the first
  // time
  @Override
  public String getPrompt() {
    if (WitnessAiRoomController.isFirstTimeInit) {
      WitnessAiRoomController.isFirstTimeInit = false;
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

  @Override
  public String getParticipantId() {
    return "WitnessAi";
  }

  public static void resetState() {
    isFirstTimeInit = true;
  }

  @Override
  public void reset() {
    super.reset();
    WitnessAiRoomController.resetState();
  }
}
