package io.opensphere.core.util.swing;

import java.awt.Component;
import java.util.Collection;

/**
 * An interface for a panel that can be dropped into and automatically wired
 * into an window such as an OptionDialog.
 */
public interface DialogPanel
{
    /**
     * Called when the user accepts the changes in the window.
     *
     * @return whether to allow the accept to complete
     */
    boolean accept();

    /**
     * Called when the user cancels the changes in the window.
     */
    void cancel();

    /**
     * Gets the content buttons.
     *
     * @return the content buttons
     */
    Collection<? extends Component> getContentButtons();

    /**
     * Gets the dialog button labels.
     *
     * @return the dialog button labels
     */
    Collection<String> getDialogButtonLabels();

    /**
     * Gets the title.
     *
     * @return the title
     */
    String getTitle();
}
