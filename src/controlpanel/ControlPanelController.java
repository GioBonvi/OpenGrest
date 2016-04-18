package controlpanel;

import java.awt.Desktop;
import util.AlertBox;
import util.Lyric;
import java.io.File;
import java.net.URI;
import javafx.scene.text.Font;
import mainpanel.MainPanelController;
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
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.paint.Color;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import mediacontrol.MediaControl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import util.LyricBlock;
import util.LyricBlock.BlockType;

public class ControlPanelController implements Initializable {
    
    public MainPanelController mainController;
    public Stage mainStage;
    private Timeline oneSecTimeline;
    public LocalDateTime targetDateTime;
    private Media mediaFile = null;
    private Lyric lyricFile = null;
    
    // Opzioni principali.
    @FXML public TextField titleField;
    @FXML public TextField subtitleField;
    @FXML public DatePicker datePicker;
    @FXML public ComboBox<String> minuteCombo;
    @FXML public ComboBox<String> hourCombo;
    @FXML public TextField targetField;
    @FXML public ColorPicker backgroundColorPicker;
    @FXML public ImageView imgHelp;
    // Controllo testo.
    @FXML public TextArea textArea;
    @FXML public ComboBox<String> fontStyleCombo;
    @FXML public ColorPicker fontColorPicker;
    @FXML public ComboBox<String> fontSizeCombo;
    @FXML public Button addTextButton;
    // Controllo testi canzoni.
    @FXML private Label lyricsFileNameLabel;
    @FXML private Button addLyricsFileButton;
    @FXML public ComboBox<String> fontSizeLyricsCombo;
    // Controllo file media.
    @FXML private CheckBox autoplayCheckbox;
    @FXML private CheckBox loopCheckbox;
    @FXML private Label mediaFileNameLabel;
    @FXML private Button addMediaFileButton;
    // Controllo animazioni.
    @FXML public TextField animationTextField;
    // Controllo pannello principale.
    @FXML private Button mainPanelResetButton;
    @FXML private ToggleButton mainPanelToggleButton;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Setup mainController e mainStage.
        try
        {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/mainpanel/MainPanel.fxml")
            );
            Parent root = (Parent) loader.load();
            // mainController permette di gestire tutti i contenuti del pannello principale
            mainController = loader.getController();
            
            /*
             * Cliccando sull'immagine del punto interrogativo sul pannello di
             * controllo si apre la guida online (il file README.md di GitHub).
             */
            imgHelp.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE))
                {
                    try
                    {
                        desktop.browse(URI.create("https://github.com/GioBonvi/OpenGrest#guida"));
                    }
                    catch (Exception ex)
                    {
                        AlertBox.show(
                            javafx.scene.control.Alert.AlertType.ERROR,
                            "Impossibile proseguire!",
                            "Impossibile aprire la pagina di aiuto.",
                            "Non è stato possibile aprire la pagina di aiuto:"
                                    + " puoi provare ad aprirla manualmente "
                                    + "inserendo questo link nel tuo browser:\n\n" +
                                    "https://github.com/GioBonvi/OpenGrest#guida",
                            ex
                        );
                    }
                }
            });
            
            /*
             * mainStage è la finestra del pannello principale, necessaria per gestire gli
             * eventi come caricamento, chiusura etc.
             */
            mainStage = new Stage();
            mainStage.setTitle("OpenGrest");
            mainStage.setScene(new Scene(root));
            
            /*
             * Il pannello principale contiene un pannello di scorrimento all'interno del
             * quale vengono aggiunti i contenuti.
             *
             * Espandi orizzontalmente per tutto lo spazio possibile.
             */
            mainController.body.minWidthProperty().bind(
                    mainController.bodyScroll.widthProperty().subtract(20)
            );
            mainController.body.maxWidthProperty().bind(
                    mainController.bodyScroll.widthProperty().subtract(20)
            );
            // Scorri fino in fondo ogni volta che vengono aggiunti elementi.
            mainController.body.heightProperty().addListener( (ov, t, t1) -> {
                 mainController.bodyScroll.setVvalue(1); 
            }) ;
            
            // Nascondi la finestra del pannello principale invece di chiuderla.
            mainStage.setOnCloseRequest(ev -> {
                ev.consume();
                if (mainController.terminateAll())
                {
                    mainStage.hide();
                }
                else
                {
                    AlertBox.show(
                        javafx.scene.control.Alert.AlertType.WARNING,
                        "Impossibile proseguire!",
                        "Impossibile nascondere il pannello principale.",
                        "Sembra che ci sia un file multimediale in riproduzione a tutto schermo.\nChiudilo per continuare."
                    );
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
                    mainStage.setMaximized(mainController.isMaximized.get());
                }
                // Salva costantemente dimensioni e posizione del pannello principale.
                mainController.lastX.bind( mainStage.xProperty());
                mainController.lastY.bind( mainStage.yProperty());
                mainController.lastW.bind( mainStage.widthProperty());
                mainController.lastH.bind( mainStage.heightProperty());
                mainController.isMaximized.bind(mainStage.maximizedProperty());
            });
            
            // Collega mostra/nascondi pannello principale al pulsante nel pannello di controllo.
            mainStage.showingProperty().addListener((obs, old_v, new_v) -> {
                mainPanelToggleButton.setSelected(new_v);
            });
            
            // Permetti di resettare il pannello principale solo mentre è visibile.
            mainPanelResetButton.disableProperty().bind(mainStage.showingProperty().not());
        }
        catch (java.io.IOException ioEx)
        {
            AlertBox.show(
                    javafx.scene.control.Alert.AlertType.ERROR,
                    "Errore nel caricamento del file!",
                    "Errore interno al programma.",
                    "Errore nel caricamenteo del file /mainpanel/MainPanel.fxml",
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
        for (int i = 20; i <= 80; i += 2)
        {
            fontSizeCombo.getItems().addAll(String.valueOf(i));
            fontSizeLyricsCombo.getItems().addAll(String.valueOf(i));
        }
        fontSizeCombo.getSelectionModel().select(5);
        fontSizeCombo.getSelectionModel().select(5);
        
        // Impedisci di aggiungere testo al pannello principale se textArea è vuota.
        addTextButton.disableProperty().bind(textArea.textProperty().isEqualTo(""));
        
        /*
         * oneSecTimeline fornisce un modo per lanciare un evento al secondo.
         * È usato per aggiornare il countdown.
         */
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
    
    // Mostra o nascondi il pannello principale con l'apposito pulsante nel pannello di controllo.
    @FXML void handleMainPanelToggleButton(ActionEvent ev)
    {
        if (mainPanelToggleButton.isSelected())
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
                mainPanelToggleButton.setSelected(true);
                AlertBox.show(
                    javafx.scene.control.Alert.AlertType.WARNING,
                    "Impossibile proseguire!",
                    "Impossibile nascondere il pannello principale!",
                    "Sembra che ci sia un file multimediale in riproduzione a tutto schermo.\nChiudilo per continuare."
                );
            }
        }
    }
    
    // Modifica titolo, sottotitolo e footer del pannello principale.
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
            min = Integer.parseInt(minuteCombo.getValue());
        }
        catch (Exception e)
        {
            min = 0;
        }
        try
        {
            hour = Integer.parseInt(hourCombo.getValue());
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
        
        // Imposta il colore di sfondo.
        Color newColor = backgroundColorPicker.getValue();
        if (newColor != null)
        {
            String newStyle = "-fx-background: rgb("
                    + (int) (newColor.getRed() * 255) + ","
                    + (int) (newColor.getGreen() * 255) + ","
                    + (int) (newColor.getBlue() * 255) + ")";
            
            mainController.rootPanel.setStyle(newStyle);
            mainController.rootPanel.getChildren().forEach(child -> {
                child.setStyle(newStyle);
            });
        }
    }
    
    // Aggiungi del testo formattato al pannello principale.
    @FXML private void handleAddText()
    {
        // Imposta il font dalle ComboBox nel pannello di controllo.
        int size;
        try
        {
            size = Integer.parseInt(fontSizeCombo.getValue());
        }
        catch (Exception ex)
        {
            size = 20;
            fontSizeCombo.setValue(String.valueOf(20));
        }
        int styleIndex = fontStyleCombo.getSelectionModel().getSelectedIndex();
        FontWeight fontWeight = (styleIndex == 1 || styleIndex == 3 ? FontWeight.BOLD : FontWeight.NORMAL);
        FontPosture fontStyle = (styleIndex == 2 || styleIndex == 3 ? FontPosture.ITALIC : FontPosture.REGULAR);
        Font newFont = Font.font("System", fontWeight, fontStyle, size);
                
        // Aggiungi una nuova Label al pannello di controllo con questo testo, font e colore.
        Text text = new Text();
        text.setText(textArea.getText());
        text.setFont(newFont);
        text.setFill(fontColorPicker.getValue());
        TextFlow newTF = new TextFlow(text);
        mainController.body.getChildren().add(newTF);
        textArea.setText("");
        
        // Vai al fondo dello scroll.
        mainController.bodyScroll.setVvalue(1);
    }
    
    // Apri la schermata per scegliere un file con un testo musicale.
    @FXML private void handleChooseLyricsFileButton()
    {
        FileChooser fc = new FileChooser();
        Stage fcStage = new Stage();
        fcStage.setTitle("Scegli un testo musicale");
        // Apri la cartella ./Testi se esiste.
        if (Files.exists(Paths.get("./Testi")))
        {
            fc.setInitialDirectory(
                new File("./Testi")
            );
        }
        // Imposta il filtro per file *.txt.
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("File di testo", "*.txt")
        );
        File file = fc.showOpenDialog(fcStage);
        // Carica in lyricFile il nuovo testo.
        if (file != null)
        {
            try
            {
                // XML parsing.
                DocumentBuilderFactory dbFactory =
                        DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(file);
                doc.getDocumentElement().normalize();
                Element root = doc.getDocumentElement();
                // Imposta il titolo.
                lyricFile = new Lyric(root.getAttribute("titolo"));
                NodeList children = root.getChildNodes();
                // Imposta strofe e ritornelli.
                for (int i = 0; i < children.getLength(); i++)
                {
                    Node node = children.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE)
                    {
                        Element elem = (Element) node;
                        String text = elem.getTextContent();
                        BlockType type = (
                                elem.getAttribute("tipo").equals("strofa")
                                ? BlockType.STROFA
                                : BlockType.RIT
                        );
                        lyricFile.blocks.add(new LyricBlock(text, type));
                    }
                }
                lyricsFileNameLabel.setText(file.getName());
                addLyricsFileButton.setDisable(false);
            }
            catch (Exception ex)
            {
                AlertBox.show(
                        Alert.AlertType.ERROR,
                        "Errore nella lettura del file.",
                        "È avvenuto un errore nella lettura del file " + file.getName(),
                        "L'erore non è stato previsto...",
                        ex
                );
                // Reset se è avvenuto un errore.
                lyricFile = null;
                addLyricsFileButton.setDisable(true);
                lyricsFileNameLabel.setText("Nessun file selezionato");
            }
        }
    }
    
    /*
     * Carica un nuovo testo musicale nel pannello principale.
     * Viene inserito un pannello a scorrimento all'interno del pannello principale.
     * Il testo viene formatttato secondo titolo, strofa o ritornello.
     */
    @FXML private void handleAddLyricsFileButton()
    {
        if (lyricFile != null)
        {
            // Dimensione base del testo della canzone (il titolo sarà baseSize * 1.5).
            int baseSize;
            try 
            {
                baseSize = Integer.parseInt(fontSizeLyricsCombo.getValue());
            }
            catch (Exception ex)
            {
                baseSize = 30;
                fontSizeLyricsCombo.setValue("30");
            }
            // Qui viene inserita la canzone.
            TextFlow newTF = new TextFlow();
            // Titotlo: grasssetto, 1.5 * baseSize.
            Text title = new Text(lyricFile.getTitle());
            title.setFont(Font.font("System", FontWeight.BOLD, (int) (baseSize * 1.5)));
            newTF.getChildren().add(title);
            // Strofe e ritornelli.
            for (LyricBlock block: lyricFile.blocks)
            {
                // Strofa.
                Text blockText = new Text("\n" + block.getText());
                blockText.setFont(Font.font("System", baseSize));
                // Ritornello.
                if (block.getType() == BlockType.RIT)
                {
                    blockText.setFont(Font.font("System", FontPosture.ITALIC, baseSize));
                    blockText.setText("\n\n" + block.getText() + "\n");
                }
                newTF.getChildren().addAll(new Text(" "), blockText, new Text(" "));
            }
            // inserisci il testo in una ScrollPane leggermente più piccola dello spazio disponibile.
            ScrollPane scrPane = new ScrollPane(newTF);
            scrPane.setMinHeight(mainController.bodyScroll.getHeight() * 85 / 100);
            scrPane.setMaxHeight(mainController.bodyScroll.getHeight() * 85 / 100);
            scrPane.setMinWidth(mainController.bodyScroll.getWidth() * 85 / 100);
            scrPane.setMaxWidth(mainController.bodyScroll.getWidth() * 85 / 100);
            mainController.body.getChildren().add(scrPane);
            // Reset controllo.
            lyricsFileNameLabel.setText("Nessun file selezionato");
            lyricFile = null;
        }
        addLyricsFileButton.setDisable(true);
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
                AlertBox.show(
                        javafx.scene.control.Alert.AlertType.ERROR,
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
    
    // Aggiungi un file multimediale al pannello principale.
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
    
    // Mostra una semplice animazione con un testo e della neve che cade.
    @FXML private void handleSnowButton()
    {
        // Elimina tutti i contenuti per mostrare l'animazione.
        if (mainController.terminateAll())
        {
            mainController.body.getChildren().clear();
        }
        else
        {
            AlertBox.show(
                javafx.scene.control.Alert.AlertType.WARNING,
                "Impossibile proseguire!",
                "Impossibile resettare il pannello principale!",
                "Sembra che ci sia un file multimediale in riproduzione a tutto schermo.\nChiudilo per continuare."
            );
            return;
        }
        
        // Testo personalizzato.
        String text = animationTextField.getText();
        
        WebView wv = new WebView();
        wv.minHeightProperty().bind(mainController.bodyScroll.heightProperty());
        wv.minWidthProperty().bind(mainController.body.widthProperty());
        WebEngine we = wv.getEngine();
        we.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                // Setup animazione.
                we.executeScript("document.getElementById(\"text\").innerHTML = \"" + text + "\"");
                we.executeScript("snowStorm.flakesMaxActive = 128");
            }
        });
        we.load(getClass().getResource("/util/snow/index.html").toString());
        
        mainController.body.getChildren().add(wv);
        wv.requestFocus();        
    }
    
    // Mostra un testo animato interattivo.
    @FXML private void handleLogoButton()
    {
        // Elimina tutti i contenuti per mostrare l'animazione.
        if (mainController.terminateAll())
        {
            mainController.body.getChildren().clear();
        }
        else
        {
            AlertBox.show(
                javafx.scene.control.Alert.AlertType.WARNING,
                "Impossibile proseguire!",
                "Impossibile resettare il pannello principale!",
                "Sembra che ci sia un file multimediale in riproduzione a tutto schermo.\nChiudilo per continuare."
            );
            return;
        }
        
        // Testo personalizzato.
        String text = animationTextField.getText().replace(" ", "").equals("") ? "Grest" : animationTextField.getText();
        
        WebView wv = new WebView();
        wv.minHeightProperty().bind(mainController.bodyScroll.heightProperty());
        wv.minWidthProperty().bind(mainController.body.widthProperty());
        WebEngine we = wv.getEngine();
        we.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                we.executeScript("drawName(\"" + text + "\", [red, orange, green, blue, purple])");
            }
        });
        we.load(getClass().getResource("/util/logo/index.html").toString());
        
        mainController.body.getChildren().add(wv);
        wv.requestFocus();   
    }
    
    // Elimina tutto il contenuto del pannello principale.
    @FXML private void handleResetButton()
    {
        if (mainController.terminateAll())
        {
            mainController.body.getChildren().clear();
        }
        else
        {
            AlertBox.show(
                javafx.scene.control.Alert.AlertType.WARNING,
                "Impossibile proseguire!",
                "Impossibile resettare il pannello principale!",
                "Sembra che ci sia un file multimediale in riproduzione a tutto schermo.\nChiudilo per continuare."
            );
        }
    }
    
    // Restituisce una StringProperty legata alla distanza fra targetDateTime e la data attuale.
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