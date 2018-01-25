package io.opensphere.geopackage.export.ui;

import javax.swing.JOptionPane;

import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.util.swing.EventQueueUtilities;

/**
 * Asks the user a yes no question and waits for user response.
 */
public class UserAskerImpl implements UserAsker
{
    /**
     * Used to get the main frame.
     */
    private final UIRegistry myUIRegistry;

    /**
     * Constructs a new {@link UserAsker}.
     *
     * @param uiRegistry Used to get the main frame.
     */
    public UserAskerImpl(UIRegistry uiRegistry)
    {
        myUIRegistry = uiRegistry;
    }

    @Override
    public boolean askYesNo(String question, String title)
    {
        Integer option = EventQueueUtilities.happyOnEdt(() -> Integer.valueOf(JOptionPane
                .showConfirmDialog(myUIRegistry.getMainFrameProvider().get(), question, title, JOptionPane.YES_NO_OPTION)));
        return option.intValue() == JOptionPane.YES_OPTION;
    }
}
