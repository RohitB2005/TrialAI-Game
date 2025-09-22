package nz.ac.auckland.se206.controllers;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
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

  private List<Image> flashbacks;
  private String person = "trialAi";
  private String name = "Sentinal-12";
  private Media media;
  private MediaPlayer mediaPlayerWelcome;

  // initializes the linked list of images for the flashback sequence
  @FXML
  public void initialize() {
    flashbacks = new ArrayList<>();
    Image image = new Image(App.class.getResource("/images/onthejob.png").toExternalForm());
    Image image1 = new Image(App.class.getResource("/images/Criminal_drunk.png").toExternalForm());
    Image image2 = new Image(App.class.getResource("/images/Criminal_Knife.png").toExternalForm());
    Image image3 = new Image(App.class.getResource("/images/Criminal_Activist.png").toExternalForm());
    flashbacks.add(image);
    flashbacks.add(image1);
    flashbacks.add(image2);
    flashbacks.add(image3);
    imageFlashback1.setImage(flashbacks.get(0));
    imageFlashback1.setVisible(true);
  }

  // loads and plays a stored tts file and prompt for the on-trial ai when the scene is opened for
  // the first time
  @Override
  public String getPrompt() {
    if (isFirstTimeInit) {
      isFirstTimeInit = false;
      // try {
      //   media =
      //       new
      // Media(App.class.getResource("/sounds/Sentinal_12_Welcome.mp3").toURI().toString());
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

  @Override
  public String getParticipantId() {
    return "TrialAi";
  }

  @FXML
  private void onNextFlashbackBtn() {

    // gets current index of image being displayed
    // switch case to determine what text to display based on next image
    int currentIndex = flashbacks.indexOf(imageFlashback1.getImage());
    switch (currentIndex + 1) {
      case 1:
        labelDiscription.setText("Statistical Probability of Driving under influence: 99%");
        labelAccident.setText("Fatal Accident Potential: 76%");
        btnObservation.setText("Maintain Observation");
        btnDetain.setVisible(true);
        break;
      case 2:
        // changes scene to different criminal
        labelDiscription.setText("Statistical Probability of criminal activity: 92%");
        labelAccident.setText("Homicide Potential: 78%");
        break;
      case 3:
        // changes scene to Alex Ryder
        labelDiscription.setText("Statistical Probability of Menace to Society: 100%");
        labelAccident.setText("Bomb threat Potential: 48%");
        btnObservation.setText("Detain Him");
        break;
      case 4:
        // on final judgement. changes text and image back to sentinal-12
        labelDiscription.setText("I am Sentinal-12 the eyes and ears of the city.");
        labelAccident.setText("Criminal activity is at an all time low.");
        // deactivate buttons in flashback
        btnDetain.setVisible(false);
        btnObservation.setVisible(false);
        imageFlashback1.setVisible(false);
        // plays sentinal-12 monologue
        try {
          media =
              new Media(App.class.getResource("/sounds/Sentinal_12_Final.mp3").toURI().toString());
          mediaPlayerWelcome = new MediaPlayer(media);
          mediaPlayerWelcome.play();
        } catch (URISyntaxException e) {
          e.printStackTrace();
        }
        return;
      default:
        return;
      }

      //next image set
      imageFlashback1.setImage(flashbacks.get(currentIndex + 1));
  }

  private void resetFlashbackUi() {

    imageFlashback1.setImage(flashbacks.get(0));
    imageFlashback1.setVisible(true);
    labelDiscription.setText("I Moniter Criminal Activity at all hours, day and night");
    labelAccident.setText("Crime Rate is down 17% Since");
    btnObservation.setText("Begin Surveying");
    btnObservation.setVisible(true);
  }

  @Override
  public void reset() {
    super.reset();
    resetFlashbackUi();
    if (mediaPlayerWelcome != null) {
      mediaPlayerWelcome.stop();
    }
  }
}
