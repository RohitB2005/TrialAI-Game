package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
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

  @FXML private VBox contentVbox;
  @FXML private Pane whiteFlashPane;
  @FXML private ImageView flashbackImageView;
  @FXML private Text flashbackText;
  @FXML private Button btnNext;
  @FXML private Button btnContinue;
  @FXML private Label timerLabel;

  private Timeline typingAnimation;
  private List<FlashbackSlide> slides = new ArrayList<>();
  private int currentSlideIndex = 0;
  private String participantId;

  // method begins flashback slideshow sequence
  public void initializeFlashback(String participantId) {
    this.participantId = participantId;
    slides.clear();
    currentSlideIndex = 0;

    // setting the introductory transition logic
    contentVbox.setOpacity(0.0);
    whiteFlashPane.setOpacity(1.0);
    whiteFlashPane.setMouseTransparent(false);

    // switch statement creating flashback slides with custom images and captions
    switch (participantId) {
      case "witnessHuman":

        // human witness 3 slides showing flashback, including captions
        slides.add(
            new FlashbackSlide(
                new Image(App.class.getResource("/images/Alex1.png").toExternalForm()),
                "The protest was passionate, things getting tense. We were only exercising our"
                    + " rights, when out of nowhere, I was detained and singled out."));
        slides.add(
            new FlashbackSlide(
                new Image(App.class.getResource("/images/Alex2.png").toExternalForm()),
                "It flagged me as a 'high-threat instigator.' That label is a permanent mark on my"
                    + " record. Because of that, I lost my job despite committing no crime."));
        slides.add(
            new FlashbackSlide(
                new Image(App.class.getResource("/images/Alex3.png").toExternalForm()),
                "Its decision ruined my life, I shouldn't be reprimanded for what some machine"
                    + " thinks I was gonna do!"));
        break;
      case "witnessAi":

        // ai witness 3 slides with flashback, including custom subtitles
        slides.add(
            new FlashbackSlide(
                new Image(App.class.getResource("/images/Alpha1.png").toExternalForm()),
                "After careful analysis, I've determined Sentinel AI cross-referenced the subject's"
                    + " elevated heart rate and stress indicators with crowd data showing rising"
                    + " tension."));
        slides.add(
            new FlashbackSlide(
                new Image(App.class.getResource("/images/Alpha2.png").toExternalForm()),
                "The issue lay in its predictive model — it marked the subject as a 'high-threat"
                    + " instigator' using outdated, biased data from riots, not peaceful"
                    + " protests."));
        slides.add(
            new FlashbackSlide(
                new Image(App.class.getResource("/images/Alpha3.png").toExternalForm()),
                "Therefore, while the defendant acted consistently with its programming, its"
                    + " programming was based on a corrupted premise. The action was logical, but"
                    + " the data was not impartial."));
        break;
      case "trialAi":

        // alpha flashback with 3 slides and personalised text
        slides.add(
            new FlashbackSlide(
                new Image(App.class.getResource("/images/Sentinel1.png").toExternalForm()),
                "TRAINING DATA LOG: Ingesting historical data from Case File 7B: Urban Riot."
                    + " Objective: Identify reliable precursors to civic violence and property"
                    + " destruction."));
        slides.add(
            new FlashbackSlide(
                new Image(App.class.getResource("/images/Sentinel2.png").toExternalForm()),
                "Pattern identified: Rapid forward movement combined with agitated vocalizations"
                    + " and rigid, sign-like object correlates with a 94% probability of assault"
                    + " on law enforcement."));
        slides.add(
            new FlashbackSlide(
                new Image(App.class.getResource("/images/Sentinel3.png").toExternalForm()),
                "PATTERN MATCH CONFIRMED: Subject 'Alex Ryder' displays three key"
                    + " precursors. Threat probability elevated to 92%. Action required to uphold"
                    + " primary safety directive. Detainment imminent."));
        break;
    }

    // update to next slide with transition animation
    updateSlide();
    playIntroAnimation();
  }

  // plays introductory animation for flashback start
  private void playIntroAnimation() {
    FadeTransition ftFlash = new FadeTransition(Duration.millis(500), whiteFlashPane);
    ftFlash.setFromValue(1.0);
    ftFlash.setToValue(0.0);

    // set and play white flash event for animation transition
    ftFlash.setOnFinished(
        event -> {
          whiteFlashPane.setMouseTransparent(true);
          FadeTransition ftContent = new FadeTransition(Duration.millis(500), contentVbox);
          ftContent.setFromValue(0.0);
          ftContent.setToValue(1.0);
          ftContent.play();
        });
    ftFlash.play();
  }

  @FXML
  private void onNextClicked() {
    btnNext.setDisable(true);
    playContentFadeTransition(false);
  }

  // plays transition animation for slide switching and opening slides
  private void playContentFadeTransition(boolean fadeIn) {
    FadeTransition ft = new FadeTransition(Duration.millis(300), contentVbox);
    ft.setFromValue(fadeIn ? 0.0 : 1.0);
    ft.setToValue(fadeIn ? 1.0 : 0.0);

    // increment slide index and update slide with transition
    ft.setOnFinished(
        event -> {
          if (!fadeIn) {
            currentSlideIndex++;
            updateSlide();
            playContentFadeTransition(true);
          } else {
            btnNext.setDisable(false);
          }
        });
    ft.play();
  }

  // animation for text to appear as a typing transition
  private void animateText(String text) {
    if (typingAnimation != null) {
      typingAnimation.stop();
    }
    flashbackText.setText("");
    typingAnimation = new Timeline();
    typingAnimation.setCycleCount(text.length());

    // sets keyframe delay for text appearing
    KeyFrame keyFrame =
        new KeyFrame(
            Duration.millis(25),
            event -> {
              flashbackText.setText(
                  flashbackText.getText() + text.charAt(flashbackText.getText().length()));
            });

    typingAnimation.getKeyFrames().add(keyFrame);
    typingAnimation.play();
  }

  @FXML
  private void onContinue(ActionEvent event) throws IOException {
    Button btn = (Button) event.getSource();
    Scene scene = btn.getScene();
    App.openScene(scene, this.participantId);
  }

  // updates flashback image with included transition
  private void updateSlide() {
    flashbackImageView.setImage(slides.get(currentSlideIndex).image);
    animateText(slides.get(currentSlideIndex).text);

    // sets visibility of button based on current slides index
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
  public void updateTimer(int timeRemaining) {
    if (timerLabel != null) {
      timerLabel.setText(App.formatTime(timeRemaining));
    }
  }
}
