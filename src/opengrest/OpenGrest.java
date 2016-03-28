package opengrest;

import controls.controlpane.ControlPaneController;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class OpenGrest extends Application {
    
    private ControlPaneController controller;
    
    @Override
    public void start(Stage stage) throws Exception {
        try
        {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/controls/controlpane/ControlPane.fxml")
            );
            Parent root = (Parent) loader.load();
            controller = loader.getController();
            
            Path configFile = new File("settings.conf").toPath();
            if (Files.exists(configFile))
            {
                try
                {
                    System.out.println("File configurazione esistente");
                    // Leggi file di configurazione.
                    List<String> lines = Files.readAllLines(configFile);
                    controller.mainController.lastX = Float.parseFloat(lines.get(0));
                    controller.mainController.lastY = Float.parseFloat(lines.get(1));
                    controller.mainController.lastW = Float.parseFloat(lines.get(2));
                    controller.mainController.lastH = Float.parseFloat(lines.get(3));
                    controller.titleField.setText(lines.get(4));
                    controller.subtitleField.setText(lines.get(5));
                    if (! lines.get(6).equals("0"))
                    {
                        LocalDate ldt = LocalDate.of(
                            Integer.parseInt(lines.get(8)),
                            Integer.parseInt(lines.get(7)),
                            Integer.parseInt(lines.get(6))
                        );
                        controller.datePicker.setValue(ldt);
                    }
                    if (! lines.get(9).equals("null"))
                    {
                        controller.hourCombo.setValue(lines.get(9));
                    }
                    if (! lines.get(10).equals("null"))
                    {
                        controller.minuteCombo.setValue(lines.get(10));
                    }
                    controller.targetField.setText(lines.get(11));
                    controller.textArea.setText(lines.get(12));
                    controller.fontStyleCombo.getSelectionModel().select(Integer.parseInt(lines.get(13)));
                    controller.fontColorCombo.getSelectionModel().select(Integer.parseInt(lines.get(14)));
                    controller.fontSizeCombo.setValue(Integer.parseInt(lines.get(15)));
                }
                catch (IOException | NumberFormatException ex)
                {
                    System.err.println("Errore nella lettura delle preferenze.");
                    ex.printStackTrace(System.err);
                }
            }
            
            stage = new Stage();
            stage.setTitle("OpenGrest");
            stage.setScene(new Scene(root));
            stage.show();

            stage.setOnCloseRequest((e) -> handleClose());
        }
        catch (java.io.IOException ioEx)
        {
            System.err.println("Errore nel caricamento del file /mainpane/MainPane.fxml");
            ioEx.printStackTrace(System.err);
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    private void handleClose()
    {
        try (PrintWriter writer = new PrintWriter("settings.conf", "UTF-8"))
        {
            // Salva le dimensioni di MainPane.
            writer.println(controller.mainController.lastX);
            writer.println(controller.mainController.lastY);
            writer.println(controller.mainController.lastW);
            writer.println(controller.mainController.lastH);
            
            // Salva il contenuto di ControlPane.
            writer.println(controller.titleField.getText());
            writer.println(controller.subtitleField.getText());
            
            LocalDate ld = controller.datePicker.getValue();
            if (ld == null)
            {
                writer.println("0");
                writer.println("0");
                writer.println("0");
            }
            else
            {
                writer.println(controller.datePicker.getValue().getDayOfMonth());
                writer.println(controller.datePicker.getValue().getMonthValue());
                writer.println(controller.datePicker.getValue().getYear());
            }
            writer.println(controller.hourCombo.getValue());
            writer.println(controller.minuteCombo.getValue());
            
            writer.println(controller.targetField.getText());
            writer.println(controller.textArea.getText());
            
            writer.println(controller.fontStyleCombo.getSelectionModel().getSelectedIndex());
            writer.println(controller.fontColorCombo.getSelectionModel().getSelectedIndex());
            writer.println(controller.fontSizeCombo.getValue());
            writer.close();
        }
        catch (FileNotFoundException ex)
        {
            System.err.println("Errore nel caricamento del file di impostazioni settings.conf");
            ex.printStackTrace(System.err);
            
        }
        catch (UnsupportedEncodingException ex)
        {
            System.err.println("Errore nella gestione della codifica del file settings.conf");
            ex.printStackTrace(System.err);
        }
    }
    
}
