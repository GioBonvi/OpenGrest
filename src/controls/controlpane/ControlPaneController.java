package controls.controlpane;

import mainpane.MainPaneController;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.ResourceBundle;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;

public class ControlPaneController implements Initializable {
    
    private MainPaneController mainController;
    private Stage mainStage;
    private Timeline oneSecTimeline;
    private LocalDateTime targetDateTime;
    
    @FXML private TextField titleField;
    @FXML private TextField subtitleField;
    @FXML private ToggleButton mainPaneToggleButton;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox minuteCombo;
    @FXML private ComboBox hourCombo;
    @FXML private TextField targetField;
    @FXML private ChoiceBox fontStyleChoice;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Setup mainController and mainStage.
        try
        {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/mainpane/MainPane.fxml")
            );
            Parent root = (Parent) loader.load();
            MainPaneController controller = loader.getController();
            mainController = controller;
            
            mainStage = new Stage();
            mainStage.setScene(new Scene(root));
            
            // Hide instead of closing
            mainStage.setOnCloseRequest(ev -> {
                ev.consume();
                mainStage.hide();
            });
            
            // Save the position and dimensions when hiding.
            mainStage.setOnHiding(ev -> {
                mainController.lastX = mainStage.getX();
                mainController.lastY = mainStage.getY();
                mainController.lastW = mainStage.getWidth();
                mainController.lastH = mainStage.getHeight();
            });
            
            // Restore saved position and dimensions when showing.
            mainStage.setOnShowing(ev -> {
                // If any position is saved...
                if (mainController.lastX > 0)
                {
                    mainStage.setX(mainController.lastX);
                    mainStage.setY(mainController.lastY);
                    mainStage.setWidth(mainController.lastW);
                    mainStage.setHeight(mainController.lastH);
                }
            });
            
            // Bind showing/hiding to ToggleButton on the ControlPanel.
            mainStage.showingProperty().addListener((obs, old_v, new_v) -> {
                mainPaneToggleButton.setSelected(new_v);
            });
        }
        catch (java.io.IOException ioEx)
        {
            System.err.println("Error loading the file /mainpane/MainPane.fxml");
            ioEx.printStackTrace(System.err);
        }
        
        // Fill the value of the combo boxes.
        for (int i = 0; i < 60; i++)
        {
            String val = i < 10 ? "0" + i : String.valueOf(i);
            minuteCombo.getItems().add(val);
            if (i < 24)
            {
                hourCombo.getItems().add(val);
            }
        }
        fontStyleChoice.getItems().addAll("None", "Bold", "Italic", "Bold Italic");
        fontStyleChoice.getSelectionModel().select(0);
        
        // Provide a way to fire an event every second.
        oneSecTimeline = new Timeline(
            new KeyFrame(
                javafx.util.Duration.seconds(0),
                (ActionEvent ev) -> {
                    
                }
            ),
            new KeyFrame(javafx.util.Duration.seconds(1))
        );
        oneSecTimeline.setCycleCount(Animation.INDEFINITE);
        oneSecTimeline.play();
    }
    
    // Hide and show the main panel with the ToggleButton on the ControlPanel.
    @FXML void handleMainPanelToggleButton(ActionEvent ev)
    {
        if (mainPaneToggleButton.isSelected())
            mainStage.show();
        else
            mainStage.hide();
    }
    
    // Edit the title and subtitle of the main panel.
    @FXML private void handleApply()
    {
        // Apply title
        mainController.titleLabel.setText(titleField.getText());
        // Apply subtitle
        mainController.subtitleLabel.setText(subtitleField.getText());
        
        // Apply footer (countdown).
        
        // Extract minute and hour of the target time from the two comboboxes.
        int min, hour;
        try
        {
            min = Integer.parseInt(minuteCombo.getValue().toString());
        }
        catch (Exception e)
        {
            min = 0;
        }
        try
        {
            hour = Integer.parseInt(hourCombo.getValue().toString());
        }
        catch (Exception e)
        {
            hour = 0;
        }
        // Extract the value of the date from the datepicker.
        // Watch out for null.
        // Add the hour and minute previously obtained.
        targetDateTime = datePicker.getValue() == null ? null : datePicker.getValue().atTime(hour, min);
        // Bind to the new values.
        mainController.footerLabel.textProperty().bind(getDateDiff());
    }
    
    private StringBinding getDateDiff()
    {
        // Return a static empty String if there is no date.
        if (targetDateTime == null)
        {
            return Bindings.createStringBinding(() -> {return "";}, new SimpleStringProperty(""));
        }
        // Otherwise return a StringBinding which contains the countdown and
        // gets updated every second via the oneSecTimeline.
        return Bindings.createStringBinding(
                () -> {
                    // Get current time.
                    LocalDateTime now = LocalDate.now().atTime(
                            Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                            Calendar.getInstance().get(Calendar.MINUTE),
                            Calendar.getInstance().get(Calendar.SECOND)
                    );
                    // Get duration between now and the target time.
                    Duration dur = Duration.between(targetDateTime, now);

                    // Build the message to be displayed.
                    String message;
                    boolean needsSingular =
                            dur.abs().compareTo(Duration.ofDays(1)) >= 0
                            && dur.abs().compareTo(Duration.ofDays(2)) < 0;
                    // dur.isNegative() == true means the date is in the future.
                    // needsSingular == true means 1 day (instead of 0 dayS or 3 dayS).
                    if (dur.isNegative() && needsSingular)
                    {
                        message = targetField.getText().equals("") ? "" : targetField.getText() + ": ";
                        message += "Manca %d giorno e %02d:%02d:%02d.";
                    }
                    else if(dur.isNegative())
                    {
                        message = targetField.getText().equals("") ? "" : targetField.getText() + ": ";
                        message += "Mancano %d giorni e %02d:%02d:%02d.";
                    }
                    else if (needsSingular)
                    {
                        message = targetField.getText().equals("") ? "" : targetField.getText() + ": ";
                        message += "È passato %d giorno e %02d:%02d:%02d.";
                    }
                    else
                    {
                        message = targetField.getText().equals("") ? "" : targetField.getText() + ": ";
                        message += "Sono passati %d giorni e %02d:%02d:%02d.";
                    }
                    
                    // Take out the minus sign if present.
                    dur = dur.abs();
                    // Calculate days, hours, minutes and seconds.
                    long s = dur.getSeconds();
                    long m = s / 60;
                    long h = m / 60;
                    long d = h / 24;
                    s = s % 60;
                    m = m % 60;
                    h = h % 24;
                    // Display the final string with leading zeroes for time.
                    return String.format(message, d, h, m, s);
                },
                // Update every second.
                oneSecTimeline.currentTimeProperty()
        );
    }
}