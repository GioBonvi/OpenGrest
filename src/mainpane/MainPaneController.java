package mainpane;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import mediacontrol.MediaControl;

public class MainPaneController implements Initializable
{
    public DoubleProperty lastX = new SimpleDoubleProperty(-1.0);
    public DoubleProperty lastY = new SimpleDoubleProperty(-1.0);
    public DoubleProperty lastH = new SimpleDoubleProperty(-1.0);
    public DoubleProperty lastW = new SimpleDoubleProperty(-1.0);
    
    @FXML public Label titleLabel;
    @FXML public Label subtitleLabel;
    @FXML public Label footerLabel;
    @FXML public VBox body;
    
    public void terminateAll()
    {
        body.getChildren().forEach((node) -> {
            if (node.getClass() == MediaControl.class)
            {
                ((MediaControl) node).abortFullScreen();
                ((MediaControl) node).mw.getMediaPlayer().stop();
            }
                
        });
    }
    
    public void terminateClearAll()
    {
        body.getChildren().forEach((node) -> {
            if (node.getClass() == MediaControl.class)
            {
                ((MediaControl) node).abortFullScreen();
                ((MediaControl) node).pauseMedia();
            }
                
        });
        body.getChildren().clear();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        
    }    
    
}
