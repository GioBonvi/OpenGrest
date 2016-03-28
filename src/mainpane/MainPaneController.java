package mainpane;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

public class MainPaneController implements Initializable
{
    public double lastX = -1;
    public double lastY = -1;
    public double lastW = -1;
    public double lastH = -1;
    
    @FXML public Label titleLabel;
    @FXML public Label subtitleLabel;
    @FXML public Label footerLabel;
    
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        
    }    
    
}
