package nz.ac.auckland.se206.controllers;

import java.net.URISyntaxException;
import java.util.LinkedList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.prompts.PromptEngineering;

public class TrialAiRoomController extends ChatController {

  @FXML private Label labelDiscription;
  @FXML private Label labelAccident;
  @FXML private Button btnDetain;
  @FXML private Button btnObservation;
  @FXML private ImageView imageFlashback;
  @FXML private ImageView imageFlashback1;
  @FXML private ImageView imageFlashback2;
  @FXML private ImageView imageFlashback3;
  @FXML private ImageView imageFlashback4;

  private LinkedList<ImageView> flashbacks;
  private String person = "trialAi";
  private String name = "Sentinal-12";
  private Media media;
  private MediaPlayer mediaPlayerWelcome;

  // initializes the linked list of images for the flashback sequence
  @FXML
  public void initialize() {
    flashbacks = new LinkedList<>();
    flashbacks.add(imageFlashback);
    flashbacks.add(imageFlashback1);
    flashbacks.add(imageFlashback2);
    flashbacks.add(imageFlashback3);
    flashbacks.add(imageFlashback4);
  }

 
  // loads and plays a stored tts file and prompt for the on-trial ai when the scene is opened for the first time
  @Override
  public String getPrompt() {
    if (isFirstTimeInit) {
      isFirstTimeInit = false;
    // try {
    //   media =
    //       new Media(App.class.getResource("/sounds/Sentinal_12_Welcome.mp3").toURI().toString());
    //   mediaPlayerWelcome = new MediaPlayer(media);
    //   mediaPlayerWelcome.play();
    // } catch (URISyntaxException e) {
    //   e.printStackTrace();
    // }
    
      return PromptEngineering.getPrompt(person + ".txt");
    } else {
      return "you are now sentinal-12 again, the on-trial Ai, you may make a comment on what the"
        + " other witness have said or wait till you are asked a question";
    }
  }

  @Override
  public String getName() {
    return name;
  }

  @FXML
  private void onNextFlashbackBtn() {
    // checks the amount of images left in the Linked list and makes a decision when a button is
    // pushed based on how many images are left in the stack
    switch (flashbacks.size()) {
      case 1:
        // on final image
        return;
      case 2:
        // on final judgement. changes text and image back to sentinal-12
        labelDiscription.setText("I am Sentinal-12 the eyes and ears of the city.");
        labelAccident.setText("Criminal activity is at an all time low.");
        // deactivate buttons in flashback
        btnDetain.setVisible(false);
        btnObservation.setVisible(false);
        // plays sentinal-12 monolog
        try {
          media =
              new Media(App.class.getResource("/sounds/Sentinal_12_Final.mp3").toURI().toString());
          mediaPlayerWelcome = new MediaPlayer(media);
          mediaPlayerWelcome.play();
        } catch (URISyntaxException e) {
          e.printStackTrace();
        }
        break;
      case 3:
        // changes scene to Alex Ryder
        labelDiscription.setText("Statistical Probability of Menace to Society: 100%");
        labelAccident.setText("Bomb threat Potential: 48%");
        btnObservation.setText("Detain Him");
        break;
      case 4:
        // changes scene to different criminal
        labelDiscription.setText("Statistical Probability of criminal activity: 92%");
        labelAccident.setText("Homicide Potential: 78%");
        break;
      case 5:
        // changes scene to first criminal
        labelDiscription.setText("Statistical Probability of Driving under influence: 99%");
        labelAccident.setText("Fatal Accident Potential: 76%");
        btnObservation.setText("Maintain Observation");
        btnDetain.setVisible(true);
        break;
      default:
        break;
    }
    // pop's the first image on the list and sets it to not visable
    flashbacks.pop().setVisible(false);
    // sets the new first image to visable
    flashbacks.getFirst().setVisible(true);
  }
}
