package mainpane;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class MainPaneController implements Initializable
{
    public double lastX = -1;
    public double lastY = -1;
    public double lastW = -1;
    public double lastH = -1;
    
    @FXML public Label titleLabel;
    @FXML public Label subtitleLabel;
    @FXML public Label footerLabel;
    @FXML public VBox body;
    
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        
    }    
    
}
