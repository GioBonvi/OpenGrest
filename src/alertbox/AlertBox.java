/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package alertbox;

import java.io.PrintWriter;
import java.io.StringWriter;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 *
 * @author Giorgio
 */
public class AlertBox {
    
    // Constructor abbreviato
    public static void show(Alert.AlertType type, String title, String header, String content)
    {
        show(type, title, header, content, null);
    }
    
    public static void show(Alert.AlertType type, String title, String header, String content, Exception ex)
    {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        if (type == Alert.AlertType.ERROR)
        {
            alert.setContentText(content
                    + "\n\nSegnala questo errore allo sviluppatore fornendo tutti i dettagli possibili.");
            if (ex != null)
            {
                // Crea "Mostra dettagli" con dettagli sull'eccezione.
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ex.printStackTrace(pw);
                String exceptionText = sw.toString();

                Label label = new Label("Ecco i dettagli dell'errore:");

                TextArea errorTextArea = new TextArea(exceptionText);
                errorTextArea.setEditable(false);
                errorTextArea.setWrapText(true);

                errorTextArea.setMaxWidth(Double.MAX_VALUE);
                errorTextArea.setMaxHeight(Double.MAX_VALUE);
                GridPane.setVgrow(errorTextArea, Priority.ALWAYS);
                GridPane.setHgrow(errorTextArea, Priority.ALWAYS);

                GridPane expContent = new GridPane();
                expContent.setMaxWidth(Double.MAX_VALUE);
                expContent.add(label, 0, 0);
                expContent.add(errorTextArea, 0, 1);

                alert.getDialogPane().setExpandableContent(expContent);
            }
        }
        alert.showAndWait();
    }
}
