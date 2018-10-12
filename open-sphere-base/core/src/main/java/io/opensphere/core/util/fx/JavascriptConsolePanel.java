package io.opensphere.core.util.fx;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.lang.StringUtilities;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;

/**
 * A Swing panel used to display a javascript console with JavaFX.
 */
public class JavascriptConsolePanel extends JFXPanel
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The engine used to execute the javascript. */
    private final WebEngine myEngine;

    /**
     * Constructor.
     *
     * @param engine The web engine that will execute the javascript.
     */
    public JavascriptConsolePanel(WebEngine engine)
    {
        myEngine = Utilities.checkNull(engine, "engine");
    }

    /**
     * Initialize the JavaFX components. This will fork to the FX thread if it
     * isn't already on the FX thread.
     */
    public void initFX()
    {
        FXUtilities.runOnFXThread(() ->
        {
            TextArea textArea = new TextArea();
            textArea.setWrapText(true);

            TextField textField = new TextField();
            textArea.setEditable(false);
            textField.setOnAction(e ->
            {
                Object result;
                try
                {
                    result = myEngine.executeScript(textField.getText());
                    textArea.appendText(">>   ");
                    textArea.appendText(textField.getText());
                    textArea.appendText(StringUtilities.LINE_SEP);
                    if (result instanceof JSObject)
                    {
                        textArea.appendText("type: ");
                        textArea.appendText(myEngine.executeScript("typeof " + textField.getText()).toString());
                        textArea.appendText(StringUtilities.LINE_SEP);
                        textArea.appendText("keys: ");
                        textArea.appendText(myEngine.executeScript("Object.keys(" + textField.getText() + ")").toString());
                        textArea.appendText(StringUtilities.LINE_SEP);
                    }
                    textArea.appendText(
                            result instanceof String ? StringUtilities.concat("\"", result, "\"") : String.valueOf(result));
                    textArea.appendText(StringUtilities.LINE_SEP);
                }
                catch (JSException e1)
                {
                    textArea.appendText(StringUtilities.concat(e1.getMessage(), StringUtilities.LINE_SEP));
                }
            });

            BorderPane root = new BorderPane(textArea);
            root.setBottom(textField);

            Scene scene = new Scene(root, 300, 400);
            FXUtilities.addDesktopStyle(scene);

            setScene(scene);
            textField.requestFocus();
        });
    }
}
