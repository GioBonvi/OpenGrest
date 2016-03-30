package controlpane;

import alertexception.AlertException;
import java.io.File;
import javafx.scene.text.Font;
import mainpane.MainPaneController;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.media.Media;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import mediacontrol.MediaControl;

public class ControlPaneController implements Initializable {
    
    public MainPaneController mainController;
    public Stage mainStage;
    private Timeline oneSecTimeline;
    public LocalDateTime targetDateTime;
    private Media mediaFile;
    
    // Opzioni principali.
    @FXML public TextField titleField;
    @FXML public TextField subtitleField;
    @FXML public DatePicker datePicker;
    @FXML public ComboBox minuteCombo;
    @FXML public ComboBox hourCombo;
    @FXML public TextField targetField;
    // Controllo testo.
    @FXML public TextArea textArea;
    @FXML public ComboBox fontStyleCombo;
    @FXML public ComboBox fontColorCombo;
    @FXML public ComboBox fontSizeCombo;
    @FXML public Button addTextButton;
    // Controllo file media.
    @FXML private CheckBox autoplayCheckbox;
    @FXML private CheckBox loopCheckbox;
    @FXML private Label mediaFileNameLabel;
    @FXML private Button addMediaFileButton;
    // Controllo MainPane.
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
                        
            // Chiudi tutti i Media prima di nascondere.
            mainStage.setOnHiding(ev -> {
                if (! mainController.terminateAll())
                {
                    System.out.println("Chiudi i file multimediali a tutto schermo!");
                }
            });
            
            mainStage.setOnShowing(ev -> {
                // Ripristina posizione e dimensioni salvate quando viene riaperto.
                if (mainController.lastX.doubleValue() != -1.0)
                {
                    mainStage.setX(mainController.lastX.doubleValue());
                    mainStage.setY(mainController.lastY.doubleValue());
                    mainStage.setWidth(mainController.lastW.doubleValue());
                    mainStage.setHeight(mainController.lastH.doubleValue());
                }
                // Salva costantemente dimensioni e posizione del MainPane.
                mainController.lastX.bind( mainStage.xProperty());
                mainController.lastY.bind( mainStage.yProperty());
                mainController.lastW.bind( mainStage.widthProperty());
                mainController.lastH.bind( mainStage.heightProperty());
            });
            
            // Bind mostra/nascondi MainPane al pulsante nel Pannello di Controllo
            mainStage.showingProperty().addListener((obs, old_v, new_v) -> {
                mainPaneToggleButton.setSelected(new_v);
            });
        }
        catch (java.io.IOException ioEx)
        {
            AlertException.show(
                    "Errore nel caricamento del file!",
                    "Errore interno al programma.",
                    "Errore nel caricamenteo del file /mainpane/MainPane.fxml",
                    ioEx
            );
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
        
        // Permetti di aggiungere testo solo se textArea contiene qualcosa.
        addTextButton.disableProperty().bind(textArea.textProperty().isEqualTo(""));
        
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
        {
            mainStage.show();
        }
        else
        {
            if (mainController.terminateAll())
            {
                mainStage.hide();
            }
            else
            {
                ev.consume();
                mainPaneToggleButton.setSelected(true);
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Impossibile proseguire!");
                alert.setHeaderText("Impossibile nascondere il pannello principale!");
                alert.setContentText("Sembra che ci sia un file multimediale in riproduzione a tutto schermo.\nChiudilo per continuare.");
                alert.showAndWait();
            }
        }
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
        // Imposta il font dalle ComboBox nel ControlPane.
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
    
    // Apri la schermata per scegliere un file multimediale.
    @FXML private void handleChooseMediaFileButton()
    {
        FileChooser fc = new FileChooser();
        Stage fcStage = new Stage();
        fcStage.setTitle("Scegli un file multimediale");
        // Apri la cartella ./Media se esiste.
        if (Files.exists(Paths.get("./Media")))
        {
            fc.setInitialDirectory(
                new File("./Media")
            );
        }
        // Imposta i filtri per file multimediali supportati da Media.
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Tutti i file multimediali",
                        "*.aif", "*.aiff", "*.flv", "*.fxm", "*.mp4", "*.m4a",
                        "*.m4v", "*.mp3", "*.wav", "*.m3u8"),
                new FileChooser.ExtensionFilter("File AIF", "*.aif", "*.aiff"),
                new FileChooser.ExtensionFilter("File FLV", "*.flv", "*.fxm"),
                new FileChooser.ExtensionFilter("File MP4", "*.mp4", "*.m4a", "*.m4v"),
                new FileChooser.ExtensionFilter("File MP3", "*.mp3"),
                new FileChooser.ExtensionFilter("File WAV", "*.wav"),
                new FileChooser.ExtensionFilter("File HLS", "*.m3u8")
            );
        File file = fc.showOpenDialog(fcStage);
        if (file != null)
        {
            // Controllo che il file sia effettivamente supportato.
            try
            {
                mediaFile = new Media(file.toURI().toString());
                mediaFileNameLabel.setText(file.getName());
                addMediaFileButton.setDisable(false);
            }
            catch (Exception ex)
            {
                AlertException.show(
                        "Errore nel caricamento del file!",
                        "Formato non supportato",
                        "Il formato del media \"" + file.getName() + "\" non supportato!",
                        ex
                );
                mediaFileNameLabel.setText("Nessun file selezionato");
                addMediaFileButton.setDisable(true);
            }
        }
    }
    
    @FXML private void handleAddMediaFileButton()
    {
        if (mediaFile != null)
        {
            mainController.body.getChildren().add(new MediaControl(
                    mediaFile,
                    autoplayCheckbox.isSelected(),
                    (loopCheckbox.isSelected() ? -1 : 1)
            ));
            // Reset impostazioni media.
            autoplayCheckbox.setSelected(false);
            loopCheckbox.setSelected(false);
            mediaFileNameLabel.setText("Nessun file selezionato");
            mediaFile = null;
        }
        addMediaFileButton.setDisable(true);
    }
    
    @FXML private void handleResetButton()
    {
        if (mainController.terminateAll())
        {
            mainController.body.getChildren().clear();
        }
        else
        {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Impossibile proseguire!");
            alert.setHeaderText("Impossibile resettare il pannello principale!");
            alert.setContentText("Sembra che ci sia un file multimediale in riproduzione a tutto schermo.\nChiudilo per continuare.");
            alert.showAndWait();
        }
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