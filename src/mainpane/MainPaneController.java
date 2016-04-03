package mainpane;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import mediacontrol.MediaControl;

public class MainPaneController implements Initializable
{
    public DoubleProperty lastX = new SimpleDoubleProperty(-1.0);
    public DoubleProperty lastY = new SimpleDoubleProperty(-1.0);
    public DoubleProperty lastH = new SimpleDoubleProperty(-1.0);
    public DoubleProperty lastW = new SimpleDoubleProperty(-1.0);
    public BooleanProperty isMaximized = new SimpleBooleanProperty(false);
    
    @FXML public BorderPane rootPane;
    @FXML public Label titleLabel;
    @FXML public Label subtitleLabel;
    @FXML public Label footerLabel;
    @FXML public ScrollPane bodyScroll;
    @FXML public VBox body;
    
    public boolean terminateAll()
    {
        boolean canIClose = true;
        
        for(Node node: body.getChildren())
        {
            if (node.getClass() == MediaControl.class)
            {
                if (((MediaControl) node).fullScreenMediaControl != null)
                {
                    canIClose = false;
                }
                else
                {
                    ((MediaControl) node).pauseMedia();
                }
            }                
        }
        return canIClose;
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        
    }    
    
}
