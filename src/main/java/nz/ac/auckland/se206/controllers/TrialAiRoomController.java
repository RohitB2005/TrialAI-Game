package nz.ac.auckland.se206.controllers;

import java.util.ArrayList;
import java.util.List;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.prompts.PromptEngineering;

public class TrialAiRoomController extends ChatController {

  @FXML private Button btnSurvey;
  @FXML private ImageView imageFlashback;
  @FXML private ImageView imageFlashback1;
  @FXML private Button btnLeftArrest;
  @FXML private Button btnLeftObserve;
  @FXML private Button btnDownRightArrest;
  @FXML private Button btnDownRightObserve;
  @FXML private Button btnUpArrest;
  @FXML private Button btnUpObserve;
  @FXML private Button btnDownArrest;
  @FXML private Button btnDownObserve;
  @FXML private Label labelSecurity;
  @FXML private Label labelScene;

  private List<Image> flashbacks;
  private String person = "trialAi";
  private String name = "Sentinal-12";
  private Media media;
  private MediaPlayer mediaPlayerWelcome;
  private int imageCount = 0;
  private int score = 0;
  private boolean alexArrested = false;
  private int scene = 0;
  private List<Button> buttons;

  // initializes the linked list of images for the flashback sequence
  @FXML
  public void initialize() {

    // create list to store flashback images and create image objects
    flashbacks = new ArrayList<>();
    Image image1 = new Image(App.class.getResource("/images/Criminal_drunk.png").toExternalForm());
    Image image2 = new Image(App.class.getResource("/images/Criminal_Knife.png").toExternalForm());
    Image image3 =
        new Image(App.class.getResource("/images/Criminal_Activist.png").toExternalForm());

    // add images to flashbacks array
    flashbacks.add(image1);
    flashbacks.add(image2);
    flashbacks.add(image3);
    imageFlashback1.setImage(flashbacks.get(0));
    imageFlashback1.setVisible(false);

    // create buttons as an array list to select actions
    buttons = new ArrayList<>();
    buttons.add(btnLeftArrest);
    buttons.add(btnLeftObserve);
    buttons.add(btnDownRightArrest);
    buttons.add(btnDownRightObserve);
    buttons.add(btnUpArrest);
    buttons.add(btnUpObserve);
    buttons.add(btnDownArrest);
    buttons.add(btnDownObserve);
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

  // selects decision with event to record answer
  @FXML
  private void onDecisionBtnClick(Event event) {

    Button clickedbtn = (Button) event.getSource();
    String btnId = clickedbtn.getId();

    // switch statement to determine answer and set styles
    switch (btnId) {
      case "btnLeftArrest":
        btnLeftObserve.setVisible(false);
        updateButtonStyle(clickedbtn, true);
        break;
      case "btnLeftObserve":
        btnLeftArrest.setVisible(false);
        updateButtonStyle(clickedbtn, false);
        break;
      case "btnDownRightArrest":

        // update score for correct answer
        btnDownRightObserve.setVisible(false);
        updateButtonStyle(clickedbtn, true);
        score = score + 20;
        break;
      case "btnDownRightObserve":
        btnDownRightArrest.setVisible(false);
        updateButtonStyle(clickedbtn, false);
        break;
      case "btnDownArrest":
        btnDownObserve.setVisible(false);
        updateButtonStyle(clickedbtn, false);
        break;
      case "btnDownObserve":
        btnDownArrest.setVisible(false);
        updateButtonStyle(clickedbtn, true);
        break;
      case "btnUpArrest":
        minigame("arrest");
        break;
      case "btnUpObserve":
        minigame("observe");
        break;
      default:
        break;
    }

    // print text for scores and security level
    scene++;
    System.out.println("Score: " + score);
    labelSecurity.setText("City Security Level: " + score + "%");
    labelScene.setText(scene + "/7 situations judged");

    // special case for max security level obtained
    if (score >= 100) {
      labelSecurity.setText("City Security Level: MAXIMUM");
      labelSecurity.setTextFill(javafx.scene.paint.Color.RED);
    }
    if (scene == 7) {
      lastResponce();
    }
  }

  private void updateButtonStyle(Button clickedbtn, boolean answer) {
    clickedbtn.getStyleClass().remove("button3");
    if (answer) {
      clickedbtn.getStyleClass().add("button4");
    } else {
      clickedbtn.getStyleClass().add("button5");
    }
    clickedbtn.setDisable(true);
  }

  // changes the flashback image to the next in the linked list
  private void minigame(String action) {

    // choose flashback slide based on image count
    if (imageCount == 0) {
      imageFlashback1.setVisible(true);
    } else if (imageCount == 1) {
      imageFlashback1.setImage(flashbacks.get(1));
    } else if (imageCount == 2) {
      imageFlashback1.setImage(flashbacks.get(2));
    } else {

      // sets observe and arrest buttons based on choices
      if (action.equals("arrest")) {
        btnUpObserve.setVisible(false);
        updateButtonStyle(btnUpArrest, true);
        alexArrested = true;
      } else {
        btnUpArrest.setVisible(false);
        updateButtonStyle(btnUpObserve, false);
        alexArrested = false;
      }
    }

    // increment image count and score
    imageCount++;
    if (action.equals("arrest")) {
      score = score + 20;
    }
  }

  @FXML
  private void onSurvey() {
    btnSurvey.setVisible(false);
    for (Button btn : buttons) {
      btn.setVisible(true);
    }
    labelSecurity.setText("choose to arrest or observe the suspects");
    labelScene.setVisible(true);
  }

  // adjusts prompt based on users actions taken
  private void lastResponce() {
    String prompt;
    if (alexArrested) {
      prompt =
          "The interviewer has attempted to do your job observing crimes using your monitering"
              + " system, They achieved city security of "
              + score
              + " and decided to arrest alex ryder when viewing the image of him filling his duffel"
              + " bag with protestor equipment. You must now comment of this development.";
    } else {
      prompt =
          "The interviewer has attempted to do your job observing crimes using your monitering"
              + " system, They achieved city security of "
              + score
              + " and decided to let alex ryder go when viewing the image of him filling his duffel"
              + " bag with protestor equipment. You must now defend your decisions.";
    }

    // sends message to LLM
    ChatMessage msg = new ChatMessage("system", prompt);
    appendChatMessage(msg);
    try {
      runGpt(msg);
    } catch (ApiProxyException e) {
      e.printStackTrace();
    }
  }

  // reset includes the logic to reset all required parameters and buttons in this scene
  @Override
  public void reset() {
    super.reset();

    if (mediaPlayerWelcome != null) {
      mediaPlayerWelcome.stop();
    }
    imageFlashback1.setVisible(false);
    btnSurvey.setVisible(true);
    for (Button btn : buttons) {
      btn.setVisible(false);
      btn.setDisable(false);
      btn.getStyleClass().remove("button4");
      btn.getStyleClass().remove("button5");
      btn.getStyleClass().add("button3");
    }

    // reset values, scores and booleans here
    labelSecurity.setText("City Security Level: 0%");
    labelSecurity.setTextFill(javafx.scene.paint.Color.BLACK);
    labelScene.setVisible(false);
    imageCount = 0;
    score = 0;
    alexArrested = false;
    scene = 0;
  }
}
