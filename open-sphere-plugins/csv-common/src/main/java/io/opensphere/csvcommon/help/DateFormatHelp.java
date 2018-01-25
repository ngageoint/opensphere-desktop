package io.opensphere.csvcommon.help;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.util.javafx.WebDialog;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.util.MantleConstants;

/**
 * Shows date format help.
 *
 */
public class DateFormatHelp implements ActionListener
{
    /**
     * The browser to show the format help.
     */
    private WebDialog myWebDialog;

    /**
     * The system toolbox.
     */
    private Toolbox myToolbox;

    @Override
    public void actionPerformed(ActionEvent e)
    {
        showHelp(myToolbox.getUIRegistry().getMainFrameProvider().get());
    }

    /**
     * Initializes the date format helper.
     *
     * @param toolbox The system toolbox.
     */
    public void initializeHelpMenu(Toolbox toolbox)
    {
        myToolbox = toolbox;
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                JMenuItem menuButton = new JMenuItem("Date Format Help");
                menuButton.addActionListener(DateFormatHelp.this);

                myToolbox.getUIRegistry().getMenuBarRegistry().getMenu(MenuBarRegistry.MAIN_MENU_BAR, MenuBarRegistry.HELP_MENU)
                        .add(menuButton);
            }
        });
    }

    /**
     * Shows the date format help page.
     *
     * @param parent The parent of the browser.
     */
    public void showHelp(Window parent)
    {
        if (myWebDialog == null)
        {
            myWebDialog = new WebDialog(parent);
            myWebDialog.setTitle("Date Format Help");
            myWebDialog.load(MantleConstants.SDF_HELP_FILE_URL.toExternalForm());
        }

        myWebDialog.setVisible(true);
        myWebDialog.toFront();
    }
}
