package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import nz.ac.auckland.se206.App;

public class FlashbackController implements ControllerInterface {

  private static class FlashbackSlide {
    final Image image;
    final String text;

    FlashbackSlide(Image image, String text) {
      this.image = image;
      this.text = text;
    }
  }

  @FXML private ImageView flashbackImageView;
  @FXML private Label flashbackText;
  @FXML private Button btnNext;
  @FXML private Button btnContinue;

  private List<FlashbackSlide> slides = new ArrayList<>();
  private int currentSlideIndex = 0;
  private String participantId;

  public void setupFlashback(String participantId) {
    this.participantId = participantId;
    slides.clear();
    currentSlideIndex = 0;
    switch (participantId) {
      case "witnessHuman":
        slides.add(
            new FlashbackSlide(
                new Image(App.class.getResource("/images/Alex1.png").toExternalForm()),
                "As an avid activist, I attended a protest a few days ago and to my shock, the AI"
                    + " detained me."));
        slides.add(
            new FlashbackSlide(
                new Image(App.class.getResource("/images/Alex2.png").toExternalForm()),
                "Little did I know, due to news of my detainment, my boss fired me from my job."));
        slides.add(
            new FlashbackSlide(
                new Image(App.class.getResource("/images/Alex3.png").toExternalForm()),
                "I'll never trust one of those things again, it ruined my life!"));
        break;
      case "witnessAi":
        slides.add(
            new FlashbackSlide(
                new Image(App.class.getResource("/images/Alpha1.png").toExternalForm()),
                "My sensor logs from the incident are clear. I recorded all relevant data, showing"
                    + " an Anomaly in Sentinel's programming."));
        slides.add(
            new FlashbackSlide(
                new Image(App.class.getResource("/images/Alpha2.png").toExternalForm()),
                "Investigating further, an error was discovered in the source code for the ethics"
                    + " measuring function."));
        slides.add(
            new FlashbackSlide(
                new Image(App.class.getResource("/images/Alpha3.png").toExternalForm()),
                "I determined that a clear ethics violation was detected in Sentinel's actions."));
        break;
      case "trialAi":
        slides.add(
            new FlashbackSlide(
                new Image(App.class.getResource("/images/Alpha1.png").toExternalForm()),
                "I analyzed 4.7 million data points in 0.02 seconds."));
        slides.add(
            new FlashbackSlide(
                new Image(App.class.getResource("/images/Alpha1.png").toExternalForm()),
                "My decision was the most logical outcome to minimize overall risk."));
        break;
    }
    updateSlide();
  }

  @FXML
  private void onNext() {
    currentSlideIndex++;
    updateSlide();
  }

  @FXML
  private void onContinue(ActionEvent event) throws IOException {
    Button btn = (Button) event.getSource();
    Scene scene = btn.getScene();
    App.openScene(scene, this.participantId);
  }

  private void updateSlide() {
    flashbackImageView.setImage(slides.get(currentSlideIndex).image);
    flashbackText.setText(slides.get(currentSlideIndex).text);

    if (currentSlideIndex == slides.size() - 1) {
      btnNext.setVisible(false);
      btnContinue.setVisible(true);
    } else {
      btnNext.setVisible(true);
      btnContinue.setVisible(false);
    }
  }

  @Override
  public void reset() {
    currentSlideIndex = 0;
    slides.clear();
    btnNext.setVisible(true);
    btnContinue.setVisible(false);
  }

  @Override
  public void updateTimer(int timeRemaining) {}
}
