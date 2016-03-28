package controls.controlpane;

import javafx.scene.text.Font;
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
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class ControlPaneController implements Initializable {
    
    public MainPaneController mainController;
    private Stage mainStage;
    private Timeline oneSecTimeline;
    public LocalDateTime targetDateTime;
    
    @FXML public TextField titleField;
    @FXML public TextField subtitleField;
    @FXML public DatePicker datePicker;
    @FXML public ComboBox minuteCombo;
    @FXML public ComboBox hourCombo;
    @FXML public TextField targetField;
    @FXML public TextArea textArea;
    @FXML public ComboBox fontStyleCombo;
    @FXML public ComboBox fontColorCombo;
    @FXML public ComboBox fontSizeCombo;
    @FXML private Button addTextButton;
    @FXML private Button resetButton;
    @FXML private ToggleButton mainPaneToggleButton;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Setup mainController e mainStage.
        try
        {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/mainpane/MainPane.fxml")
            );
            Parent root = (Parent) loader.load();
            MainPaneController controller = loader.getController();
            mainController = controller;
            
            mainStage = new Stage();
            mainStage.setTitle("OpenGrest");
            mainStage.setScene(new Scene(root));
            
            // Nascondi la finestra invece di chiuderla.
            mainStage.setOnCloseRequest(ev -> {
                ev.consume();
                mainStage.hide();
            });
            
            // Salva posizione e dimensioni quando viene chiuso il MainPane.
            mainStage.setOnHiding(ev -> {
                mainController.lastX = mainStage.getX();
                mainController.lastY = mainStage.getY();
                mainController.lastW = mainStage.getWidth();
                mainController.lastH = mainStage.getHeight();
            });
            
            // Ripristina posizione e dimensioni salvate quando viene riaperto.
            mainStage.setOnShowing(ev -> {
                // Se ci sono delle posizioni salvate...
                if (mainController.lastX > 0)
                {
                    mainStage.setX(mainController.lastX);
                    mainStage.setY(mainController.lastY);
                    mainStage.setWidth(mainController.lastW);
                    mainStage.setHeight(mainController.lastH);
                }
            });
            
            // Bind mostra/nascondi MainPane al pulsante nel Pannello di Controllo
            mainStage.showingProperty().addListener((obs, old_v, new_v) -> {
                mainPaneToggleButton.setSelected(new_v);
            });
        }
        catch (java.io.IOException ioEx)
        {
            System.err.println("Errore nel caricamenteo del file /mainpane/MainPane.fxml");
            ioEx.printStackTrace(System.err);
        }
        
        // Riempi i valori delle ComboBox
        for (int i = 0; i < 60; i++)
        {
            String val = i < 10 ? "0" + i : String.valueOf(i);
            minuteCombo.getItems().add(val);
            if (i < 24)
            {
                hourCombo.getItems().add(val);
            }
        }
        fontStyleCombo.getItems().addAll("Nessuno", "Grassetto", "Corsivo", "Grassetto corsivo");
        fontStyleCombo.getSelectionModel().select(0);
        fontColorCombo.getItems().addAll("Nero", "Rosso", "Blu", "Verde");
        fontColorCombo.getSelectionModel().select(0);
        for (int i = 10; i <= 80; i += 2)
        {
            fontSizeCombo.getItems().addAll(i);
        }
        fontSizeCombo.getSelectionModel().select(5);
        
        // Fornisce un modo per lanciare un evento al secondo.
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
    
    // Mostra o nascondi il MainPane con l'apposito pulsante nel ControlPane.
    @FXML void handleMainPanelToggleButton(ActionEvent ev)
    {
        if (mainPaneToggleButton.isSelected())
            mainStage.show();
        else
            mainStage.hide();
    }
    
    // Modifica titolo, sottotitolo e footer del MainPane.
    @FXML private void handleApply()
    {
        // Titolo
        mainController.titleLabel.setText(titleField.getText());
        // Sottotitolo
        mainController.subtitleLabel.setText(subtitleField.getText());
        
        // Footer (countdown).
        
        // Estrai ora e minuto dalle ComboBox (controllo validità numero).
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
        // Estrai la data dal DatePicker e controlla che non sia null.
        // Aggiungi alla data l'ora e il minuto appena ottenuti.
        targetDateTime = datePicker.getValue() == null ? null : datePicker.getValue().atTime(hour, min);
        // Imposta il footer con i valori adeguati.
        mainController.footerLabel.textProperty().bind(getDateDiff());
    }
    
    // Aggiungi del testo formattato al MainPane.
    @FXML private void handleAddText()
    {
        // Imposta il font dalle ComboBox nel COntrolPane.
        int size;
        try
        {
            size = Integer.parseInt(
                    fontSizeCombo.getSelectionModel().getSelectedItem().toString()
            );
        }
        catch (Exception ex)
        {
            size = 20;
        }
        int styleIndex = fontStyleCombo.getSelectionModel().getSelectedIndex();
        FontWeight fontWeight = (styleIndex == 1 || styleIndex == 3 ? FontWeight.BOLD : FontWeight.NORMAL);
        FontPosture fontStyle = (styleIndex == 2 || styleIndex == 3 ? FontPosture.ITALIC : FontPosture.REGULAR);
        Font newFont = Font.font("System", fontWeight, fontStyle, size);
        
        // Imposta il colore.
        Paint fontColor;
        switch (fontColorCombo.getSelectionModel().getSelectedIndex())
        {
            case 0:
                fontColor = Color.BLACK;
                break;
            case 1:
                fontColor = Color.RED;
                break;
            case 2:
                fontColor = Color.BLUE;
                break;
            case 3:
                fontColor = Color.GREEN;
                break;
            default:
                fontColor = Color.BLACK;
                break;
        }
        
        // Aggiungi una nuova Label al MainPane con questo testo, font e colore.
        Label newLbl = new Label();
        newLbl.setText(textArea.getText());
        newLbl.setFont(newFont);
        newLbl.setTextFill(fontColor);
        mainController.body.getChildren().add(newLbl);
        textArea.setText("");
    }
    
    @FXML private void handleResetButton()
    {
        mainController.body.getChildren().clear();
    }
    
    private StringBinding getDateDiff()
    {
        // Stringa vuota se la data è null.
        if (targetDateTime == null)
        {
            return Bindings.createStringBinding(() -> {return "";}, new SimpleStringProperty(""));
        }
        // Altrimenti restituisci uno StringBinding contenente il countdown che
        // viene aggiornato ogni secondo dalla oneSecTimeline.
        return Bindings.createStringBinding(
                () -> {
                    // Ottiene instante attuale.
                    LocalDateTime now = LocalDate.now().atTime(
                            Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                            Calendar.getInstance().get(Calendar.MINUTE),
                            Calendar.getInstance().get(Calendar.SECOND)
                    );
                    // Ottieni distanaza fra adesso e targetDateTime.
                    Duration dur = Duration.between(targetDateTime, now);

                    // Costruisci il messaggio da mostrare.
                    String message;
                    boolean needsSingular =
                            dur.abs().compareTo(Duration.ofDays(1)) >= 0
                            && dur.abs().compareTo(Duration.ofDays(2)) < 0;
                    // dur.isNegative() == true significa che la data è nel futuro.
                    // needsSingular == true significa 1 giornO invece che 0 giornI o 3 giornI etc.
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
                    
                    // Elimina eventuale segno meno.
                    dur = dur.abs();
                    // Calcola giorni, ore, minuti e secondi.
                    long s = dur.getSeconds();
                    long m = s / 60;
                    long h = m / 60;
                    long d = h / 24;
                    s = s % 60;
                    m = m % 60;
                    h = h % 24;
                    // Mostra il messaggio finale in formato "g e hh:mm:ss"
                    return String.format(message, d, h, m, s);
                },
                // Aggiorna ogni secondo.
                oneSecTimeline.currentTimeProperty()
        );
    }
}