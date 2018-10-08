package io.opensphere.core.util.fx;

import java.awt.EventQueue;

import javax.swing.JDialog;

import io.opensphere.core.util.Utilities;
import javafx.scene.web.WebEngine;

/**
 * A dialog that contains a javascript console that uses JavaFX.
 */
public class JavascriptConsoleDialog
{
    /** The wrapped JDialog. */
    private final JDialog myDialog;

    /** The javascript engine. */
    private final WebEngine myEngine;

    /**
     * Constructor.
     *
     * @param dialog The dialog to populate.
     * @param engine The web engine used to execute the javascript.
     */
    public JavascriptConsoleDialog(JDialog dialog, WebEngine engine)
    {
        myDialog = Utilities.checkNull(dialog, "dialog");
        myEngine = Utilities.checkNull(engine, "engine");
    }

    /**
     * Initialize the dialog.
     */
    public void initFX()
    {
        FXUtilities.runOnFXThread(() ->
        {
            JavascriptConsolePanel panel = new JavascriptConsolePanel(myEngine);
            panel.initFX();
            EventQueue.invokeLater(() ->
            {
                myDialog.getContentPane().add(panel);
                myDialog.pack();
                myDialog.setLocationRelativeTo(myDialog.getParent());
                myDialog.setVisible(true);
            });
        });
    }
}
