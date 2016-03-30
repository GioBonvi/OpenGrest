package mediacontrol;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class MediaControl extends StackPane
{
    // Default time after which the controls are hidden if the mouse is not moved.
    private final int DEF_WAIT_TO_HIDE = 3;
    
    private Media media;
    private MediaPlayer mp;
    private javafx.util.Duration mediaDuration;
    private Boolean isAutoplay;
    private int loopNumber;
    private Timeline checkHideControlBar;
    private Instant lastMoved;
    private int waitHideControlBar;
    public Boolean isFullScreen = false;
    public Boolean hasFullScreen = false;
    public MediaControl fullScreenMediaControl = null;
    
    @FXML public MediaView mw;
    @FXML private HBox controlBar;
    @FXML private ImageView playImg;
    @FXML private ImageView fullScrImg;
    @FXML private ImageView resetImg;
    @FXML private Label displayTime;
    @FXML private Slider progressSlider;
    @FXML private ImageView volImg;
    @FXML private Slider volSlider;
    
    // Shorthand constructors.
    public MediaControl(Media mediaFile)
    {
        this(mediaFile, false, 1, 0,  null, 0, 0, 1);
    }    
    public MediaControl(
            Media mediaFile,
            boolean autoplay)
    {
        this(mediaFile, autoplay, 1, 0, null, 0, 0, 1);
    }
    public MediaControl(
            Media mediaFile,
            boolean autoplay,
            int loop)
    {
        this(mediaFile, autoplay, loop, 0, null, 0, 0, 1);
    }
    public MediaControl(
            Media mediaFile,
            boolean autoplay,
            int loop,
            int waitHideControlBar)
    {
        this(mediaFile, autoplay, loop, waitHideControlBar, null, 0, 0, 1);
    }
    public MediaControl(
            Media mediaFile,
            boolean autoplay,
            int loop,
            int waitHideControlBar,
            ReadOnlyDoubleProperty widthProp)
    {
        this(mediaFile, autoplay, loop, waitHideControlBar, widthProp, 0, 0, 1);
    }
    public MediaControl(
            Media mediaFile,
            Boolean autoplay,
            int loop,
            int waitHideControlBar,
            ReadOnlyDoubleProperty widthProp,
            double maxWidth)
    {
        this(mediaFile, autoplay, loop, waitHideControlBar, widthProp, maxWidth, 0, 1);
    }
    public MediaControl(
            Media mediaFile,
            Boolean autoplay,
            int loop,
            int waitHideControlBar,
            ReadOnlyDoubleProperty widthProp,
            double maxWidth,
            double progress)
    {
        this(mediaFile, autoplay, loop, waitHideControlBar, widthProp, maxWidth, progress, 1);
    }
    
    // Setup new instance of the MediaControl.
    public MediaControl(
            Media mediaFile,
            Boolean autoplay,
            int loop,
            int waitHideControlBar,
            ReadOnlyDoubleProperty widthProp,
            double maxWidth,
            double progress,
            double volume)
    {
        // Load the FXML structure.
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/mediacontrol/MediaControl.fxml")
        );
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try
        {
            fxmlLoader.load();
        }
        catch (IOException exception)
        {
            System.err.println("Error loading the /mediacontrol/MediaControl.fxml file!");
            throw new RuntimeException(exception);
        }
        
        // Setup the Media and MediaPlayer with the given parameters.
        media = mediaFile;
        mp = new MediaPlayer(media);
        isAutoplay = autoplay;
        mp.setAutoPlay(isAutoplay);
        mp.setVolume((volume >= 0 && volume <= 1) ? volume : 1);
        // If loop is negative the media is looped indefinetely
        loopNumber = loop >= 0 ? loop : MediaPlayer.INDEFINITE;
        mp.setCycleCount(loopNumber);
        // When the media ends take it back to the beginning, but check out for loops!
        mp.setOnEndOfMedia(() -> {
            if (mp.getCurrentCount() == mp.getCycleCount()
                    && mp.getCycleCount() != MediaPlayer.INDEFINITE)
            {
                mp.seek(javafx.util.Duration.ZERO);
                mp.pause();
                playImg.setImage(new Image(getClass().getResource("/mediacontrol/buttonimages/Play.png").toString()));
            }
        });
        
        mp.setOnReady(() -> {
             mediaDuration = mp.getCycleDuration();
            // Setup time label.
            displayTime.setText(secondsToTime((long) mp.getCurrentTime().toSeconds())
                    + "/"
                    + secondsToTime((long) mediaDuration.toSeconds()));
            // progressSlider moves while the video plays.
            mp.currentTimeProperty().addListener((Observable obs) -> {
                progressSlider.setValue(
                        mp.getCurrentTime().toSeconds() /
                                mediaDuration.toSeconds() * 100
                );
                displayTime.setText(secondsToTime((long) mp.getCurrentTime().toSeconds())
                        + "/"
                        + secondsToTime((long) mediaDuration.toSeconds()));
            });
            // Skip to given position.
            mp.seek(
                    (progress >= 0 && progress <= 1)
                    ? mediaDuration.multiply(progress)
                    : javafx.util.Duration.ZERO
            );
        });
        
        // Setup the size of MediaView (and then bind size of whole control to it).
        mw.setMediaPlayer(mp);
        
        // MediaView width.
        if (widthProp == null)
        {
            // Auto width.
        }
        else if (maxWidth == 0)
        {
            // Bind width of the control to the given width.
            mw.fitWidthProperty().bind(widthProp);
        }
        else
        {
            // Bind width of the control to the given width, but limiting it to the given limit.
            mw.fitWidthProperty().bind(
                Bindings.when(
                        widthProp.greaterThan(new SimpleDoubleProperty(maxWidth))
                    )
                    .then(new SimpleDoubleProperty(maxWidth))
                    .otherwise(widthProp)
            );
        }
        // Limit the height of the control to the height of the MediaView.
        this.maxHeightProperty().bind(mw.fitHeightProperty());
        
        // Add control bar (at the bottom of the MediaView).        
        // Bind the width of the control bar to the width of the MediaView.
        mw.boundsInLocalProperty().addListener((obs, old_v,  new_v) -> {
            controlBar.setMaxWidth(new_v.getWidth());
            controlBar.setMinWidth(new_v.getWidth());
        });
        
        // Setup the content of the control bar.
        
        // Play/pause button.
        playImg.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> handlePlayButton(e));
        String action = mp.isAutoPlay() ? "Pause.png" : "Play.png";
        playImg.setImage(new Image(
                getClass().getResource("/mediacontrol/buttonimages/" + action).toString()
        ));
        
        // Fullscreen button TODO: add logic
        fullScrImg.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> handleFullScrButton(e));
        fullScrImg.setImage(new Image(
                getClass().getResource("/mediacontrol/buttonimages/FullScr.png").toString()
        ));
        
        // Reset button.
        resetImg.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> handleResetButton(e));
        resetImg.setImage(new Image(
                getClass().getResource("/mediacontrol/buttonimages/Reset.png").toString()
        ));
        
        // Progress slider.
        progressSlider.setDisable(true);
        
        // Volume slider.
        volImg.setImage(new Image(
                getClass().getResource("/mediacontrol/buttonimages/Vol.png").toString()
        ));
        volSlider.valueProperty().bindBidirectional(mp.volumeProperty());
        
        // The control bar should be hidden if the mouse doesn't move for a certain amount of time.
        
        // If user value is 0 or less use the default value.
        this.waitHideControlBar = waitHideControlBar > 0 ? waitHideControlBar : DEF_WAIT_TO_HIDE;
        // Set the last time the mouse moved to now.
        lastMoved = Instant.now();
        // Check every 0.3 seconds if 3 seconds have passed since the mouse last moved.
        // mw.getFitHeight() > 0 is true if a video is shown. With music the control bar is alwas on.
        checkHideControlBar = new Timeline(new KeyFrame(javafx.util.Duration.millis(300), ev -> {
            if (Duration.between(lastMoved, Instant.now()).getSeconds() > this.waitHideControlBar
                    && mw.getFitHeight() > 0)
            {
                // Then hide the control bar.
                controlBar.setVisible(false);
                this.setCursor(Cursor.NONE);
            }
        }));
        checkHideControlBar.setCycleCount(Timeline.INDEFINITE);
        // Start checking.
        checkHideControlBar.play();        
    }
    
    // Play or pause the media and modify the button accordigly.
    @FXML private void handlePlayButton(MouseEvent e)
    {
        e.consume();
        Status status = mp.getStatus();
        if (status == Status.UNKNOWN || status == Status.HALTED)
        {
            playImg.setImage(new Image(
                    getClass().getResource("/mediacontrol/buttonimages/Play.png").toString()
            ));
            System.err.println("Unexpected media status!");
            return;
        }
        if (status == Status.PAUSED
                || status == Status.READY
                || status == Status.STOPPED)
        {
            playMedia();
        }
        else
        {
            pauseMedia();
        }
    }
    
    @FXML private void handleFullScrButton(MouseEvent e)
    {
        e.consume();
        if (isFullScreen)
        {
            // Close if already active.
            ((Stage) mw.getScene().getWindow()).close();
        }
        else
        {
            // Start if not active.
            
            // We need to recreate a perfect copy of our media.
            // Get the status.
            Status currentStatus = mp.getStatus();
            // Now stop (avoid reproducing again in the background).
            if (currentStatus == Status.PLAYING)
            {
                pauseMedia();
            }
            // Get te progress.
            double progress = mp.getCurrentTime().toMillis()
                    / mp.getCycleDuration().toMillis();
            Stage stage = new Stage();
            // Get all the user values and pass them on.
            fullScreenMediaControl = new MediaControl(media,
                    currentStatus == Status.PLAYING,
                    loopNumber,
                    waitHideControlBar,
                    stage.widthProperty(),
                    0,
                    progress,
                    mp.getVolume());
            // Set fullscreen.
            fullScreenMediaControl.isFullScreen = true;
            Scene scene = new Scene(fullScreenMediaControl);
            // Close on "Escape".
            scene.setOnKeyPressed(ke -> {
                if (ke.getCode() == KeyCode.ESCAPE) {
                    stage.close();
                }
            });
            // Start!
            stage.setScene(scene);
            // Mostra fullscreen sul secondo monitor (se esiste).
            if (Screen.getScreens().size() > 1)
            {
                Rectangle2D bounds = Screen.getScreens().get(1).getBounds();
                stage.setX(bounds.getMinX());
                stage.setY(bounds.getMinY());
                stage.setWidth(bounds.getWidth());
                stage.setHeight(bounds.getHeight());
            }
            else
            {
                stage.setFullScreen(true);
            }
            
            // Wait until user closes it.
            stage.showAndWait();
            
            // Whene it's closed we need to update our player with values form the closed one.
            MediaPlayer closedMp = fullScreenMediaControl.mw.getMediaPlayer();
            fullScreenMediaControl = null;
            // Recover status.
            currentStatus = closedMp.getStatus();
            closedMp.pause();
            // Recover progress.
            progress = closedMp.getCurrentTime().toMillis()
                    / closedMp.getCycleDuration().toMillis();
            // Apply status.
            if (currentStatus == Status.PLAYING)
            {
                playMedia();
            }
            // Apply progress.
            mp.seek(mediaDuration.multiply(progress));
            mp.setVolume(closedMp.getVolume());
        }
    }
    
    // Take the video to the beginning and pause it.
    @FXML private void handleResetButton(MouseEvent e)
    {
        e.consume();
        mp.seek(javafx.util.Duration.ZERO);
        pauseMedia();
    }
    
    // When the mouse is moved show the control bar and update the time.
    @FXML private void handleMouseMoved()
    {
        controlBar.setVisible(true);
        this.setCursor(Cursor.DEFAULT);
        lastMoved = Instant.now();
    }
    
    public void abortFullScreen()
    {
        if (fullScreenMediaControl != null)
        {
            fullScreenMediaControl.mw.getMediaPlayer().pause();
            ((Stage) fullScreenMediaControl.getScene().getWindow()).hide();
        }
    }
    
    public void pauseMedia()
    {
        mp.pause();
        playImg.setImage(new Image(
                getClass().getResource("/mediacontrol/buttonimages/Play.png").toString()
        ));
        if (fullScreenMediaControl != null)
        {
            fullScreenMediaControl.mw.getMediaPlayer().pause();
            fullScreenMediaControl.playImg.setImage(new Image(
                getClass().getResource("/mediacontrol/buttonimages/Play.png").toString()
            ));
        }
    }
    
    public void playMedia()
    {
        mp.play();
        playImg.setImage(new Image(
                getClass().getResource("/mediacontrol/buttonimages/Pause.png").toString()
        ));
    }
    
    private String secondsToTime(long secs)
    {
        if (secs >= 3600)
        {
            return String.format(
                    "%02d:%02d:%02d",
                    secs/3600,
                    (secs/60)%60,
                    secs%60);
        }
        else
        {
            return String.format(
                    "%02d:%02d",
                    (secs/60)%60,
                    secs%60);
        }
    }
}