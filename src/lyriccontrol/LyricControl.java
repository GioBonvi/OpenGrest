package lyriccontrol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.Text;

public class LyricControl extends ScrollPane {
    @FXML private Text titleText;
    @FXML private Text bodyText;
    
    private final List<Text> textBlocks;
    
    private final IntegerProperty currentIndex;
    
    public LyricControl()
    {
        // Load the FXML structure.
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/lyriccontrol/LyricControl.fxml")
        );
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try
        {
            fxmlLoader.load();
        }
        catch (IOException exception)
        {
            System.err.println("Error loading the lyriccontrol/LyricControl.fxml file!");
            throw new RuntimeException(exception);
        }
                
        // Lista di strofe e ritornelli.
        textBlocks = new ArrayList<>();
        currentIndex = new SimpleIntegerProperty(-1);
        // Quando cambia l'indice mostra la strofa/rit corrispondente.
        currentIndex.addListener((o, oldVal, newVal) -> {
            Text newText = textBlocks.get(newVal.intValue());
            bodyText.setText(newText.getText());
            bodyText.setFont(newText.getFont());
        });
    }
    
    public void setTitle(Text newTitle)
    {
        titleText.setText(newTitle.getText());
        titleText.setFont(newTitle.getFont());
    }
    
    // Aggiungi una nuova strofa/rit all'elenco.
    public void appendText(Text newText)
    {
        textBlocks.add(newText);
        currentIndex.set(0);
    }
    
    // Vai avanti o indietro di jump strofe/ritornelli (negativo = indietro).
    public void jump(int jump)
    {
        // Non uscire dall'elenco (minimo = 0, massimo = size() - 1).
        if (currentIndex.get() + jump < textBlocks.size() && currentIndex.get() + jump >= 0)
        {
            currentIndex.set(currentIndex.add(jump).get());
        }
    }
}
