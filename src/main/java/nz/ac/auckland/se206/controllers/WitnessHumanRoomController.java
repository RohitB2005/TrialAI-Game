package nz.ac.auckland.se206.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.prompts.PromptEngineering;

public class WitnessHumanRoomController extends ChatController {

  private String person = "witnessHuman";
  private String name = "Alex Ryder";

  @FXML private ImageView viewSpraycan;
  @FXML private ImageView viewBandanna;
  @FXML private ImageView viewGun;
  @FXML private Region regionBag;
  @FXML private Label label;
  @FXML private Label labelCount;

  // loads and plays a stored tts file for the human witness when the scene is opened for the first
  // time
  @Override
  public String getPrompt() {
    if (isFirstTimeInit) {
      isFirstTimeInit = false;

      // try {
      //   media = new Media(App.class.getResource("/sounds/Alex_Ryder.mp3").toURI().toString());
      //   mediaPlayerWelcome = new MediaPlayer(media);
      //   mediaPlayerWelcome.play();
      // } catch (URISyntaxException e) {
      //   e.printStackTrace();
      // }
      return PromptEngineering.getPrompt(person + ".txt");
    } else {
      return "you are now Alex Ryder again, the human victim, you may make a comment on what the"
          + " other witness have said or wait till you are asked a question";
    }
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getParticipantId() {
    return "WitnessHuman";
  }

  // simple reset calls on interface and sets visibility of memoru components
  @Override
  public void reset() {
    super.reset();
    viewSpraycan.setVisible(false);
    viewBandanna.setVisible(false);
    viewGun.setVisible(false);
    regionBag.setCursor(javafx.scene.Cursor.HAND);
    label.setVisible(true);
    labelCount.setVisible(false);
  }

  // logic for clicking bag for items in memory
  @FXML
  public void onBagClick(MouseEvent event) throws ApiProxyException {
    double parentX =
        viewSpraycan.getParent().sceneToLocal(event.getSceneX(), event.getSceneY()).getX();
    double parentY =
        viewSpraycan.getParent().sceneToLocal(event.getSceneX(), event.getSceneY()).getY();

    // if statement to set position and visibility of spray can, increment indicator
    if (!viewSpraycan.isVisible()) {
      viewSpraycan.setVisible(true);
      viewSpraycan.setLayoutX(parentX - 30);
      viewSpraycan.setLayoutY(parentY - 40);
      labelCount.setVisible(true);
      labelCount.setText("1/3 found");
    } else if (!viewBandanna.isVisible()) {

      // set visibility and position of bandana next, increment count
      viewBandanna.setVisible(true);
      viewBandanna.setLayoutX(parentX - 30);
      viewBandanna.setLayoutY(parentY - 40);
      labelCount.setText("2/3 found");
    } else if (!viewGun.isVisible()) {

      // final item gun taken from bag, position and visibility set
      viewGun.setVisible(true);
      viewGun.setLayoutX(parentX - 30);
      viewGun.setLayoutY(parentY - 40);
      labelCount.setText("3/3 found");
      regionBag.setCursor(null);
      label.setVisible(false);

      // append new message to chat and send to LLM as prompt to provide further context
      ChatMessage msg =
          new ChatMessage(
              "system",
              "The interviewer has found a spray can, Bandanna and Airsoft gun in your bag, you"
                  + " must now defend yourself over why they were there on the day you wer"
                  + " areested");
      appendChatMessage(msg);
      runGpt(msg);
    }
  }

  // draggable animation for items in memory
  @FXML
  public void onDrag(MouseEvent event) {
    ImageView draggedImage = (ImageView) event.getSource();
    double parentX =
        viewSpraycan.getParent().sceneToLocal(event.getSceneX(), event.getSceneY()).getX();
    double parentY =
        viewSpraycan.getParent().sceneToLocal(event.getSceneX(), event.getSceneY()).getY();

    // set positions of separate images
    if (draggedImage == viewGun) {
      draggedImage.setLayoutX(parentX - 80);
      draggedImage.setLayoutY(parentY - 30);
      return;
    }
    draggedImage.setLayoutX(parentX - 50);
    draggedImage.setLayoutY(parentY - 80);
  }
}
