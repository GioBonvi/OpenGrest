package mainpanel;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import lyriccontrol.LyricControl;
import mediacontrol.MediaControl;

public class MainPanelController implements Initializable
{
    public DoubleProperty lastX = new SimpleDoubleProperty(-1.0);
    public DoubleProperty lastY = new SimpleDoubleProperty(-1.0);
    public DoubleProperty lastH = new SimpleDoubleProperty(-1.0);
    public DoubleProperty lastW = new SimpleDoubleProperty(-1.0);
    public BooleanProperty isMaximized = new SimpleBooleanProperty(false);
    
    @FXML public BorderPane rootPanel;
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
        // Quando viene premuto SHIFT o CONTROL tutti i "lyricControl" del
        // pannello vanno avanti o indietro di una strofa/ritornello.
        // Non uso le frecce perché servono per scorrere nel pannello esterno.
        rootPanel.setOnKeyReleased((event) -> {
            int jump = 0;
            switch (event.getCode()) {
                case SHIFT:
                    jump = -1;
                    break;
                case CONTROL:
                    jump = 1;
                    break;
            }
            if (jump != 0)
            {
                for (Node node: body.getChildren())
                {
                    if (node.getClass() == LyricControl.class)
                    {
                        ((LyricControl) node).jump(jump);
                    }
                }
            }
        });
    }    
    
}
