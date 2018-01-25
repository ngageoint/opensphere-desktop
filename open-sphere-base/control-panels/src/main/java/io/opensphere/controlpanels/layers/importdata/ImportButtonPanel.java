package io.opensphere.controlpanels.layers.importdata;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JComponent;

import io.opensphere.controlpanels.layers.event.ShowAvailableDataEvent;
import io.opensphere.core.control.action.ContextActionManager;
import io.opensphere.core.control.action.context.ContextIdentifiers;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.core.util.swing.misc.ControlMenuOptionContextSplitButton;

/**
 * The Class ImportButtonPanel.
 */
public class ImportButtonPanel extends JComponent
{
    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The split button. */
    private final transient ImportSplitButton mySplitButton;

    /**
     * Instantiates a new import button panel.
     *
     * @param contextActionManager The context action manager.
     * @param eventManager The event manager.
     * @param buttonText the button text
     */
    public ImportButtonPanel(ContextActionManager contextActionManager, final EventManager eventManager, String buttonText)
    {
        setBackground(new Color(0, 0, 0, 0));
        setLayout(new BorderLayout());

        mySplitButton = new ImportSplitButton(contextActionManager, buttonText, null);
        IconUtil.setIcons(mySplitButton, IconType.PLUS, Color.GREEN);
        mySplitButton.setToolTipText("Add a layer to the map.");
        mySplitButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                eventManager.publishEvent(new ShowAvailableDataEvent());
            }
        });

        add(mySplitButton, BorderLayout.CENTER);
    }

    /**
     * Gets the import file button.
     *
     * @return the import file button
     */
    public ImportSplitButton getSplitButton()
    {
        return mySplitButton;
    }

    /**
     * The Class ImportSplitButton.
     */
    private static class ImportSplitButton extends ControlMenuOptionContextSplitButton<Void>
    {
        /** serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /**
         * Instantiates a new delete split button.
         *
         * @param contextActionManager the context action manager
         * @param text the text
         * @param icon the icon
         */
        public ImportSplitButton(ContextActionManager contextActionManager, String text, Icon icon)
        {
            super(contextActionManager, text, icon, ContextIdentifiers.IMPORT_CONTEXT, Void.class);
        }
    }
}
