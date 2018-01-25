package io.opensphere.controlpanels.layers.layerpopout.view;

import java.awt.Color;
import java.awt.Container;
import java.awt.Rectangle;

import io.opensphere.controlpanels.layers.layerpopout.controller.PopoutController;
import io.opensphere.controlpanels.layers.layerpopout.model.v1.PopoutModel;
import io.opensphere.core.Toolbox;
import io.opensphere.core.hud.awt.AbstractInternalFrame;
import io.opensphere.core.preferences.PreferenceChangeEvent;
import io.opensphere.core.preferences.PreferenceChangeListener;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.swing.AbstractHUDPanel;
import io.opensphere.core.util.swing.EventQueueUtilities;

/**
 * The layer popout hud view.
 */
public class LayerPopoutView extends AbstractInternalFrame implements PreferenceChangeListener
{
    /**
     * The serial version id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The background color.
     */
    private Color myBackgroundColor;

    /**
     * The pop out controller.
     */
    private final PopoutController myController;

    /**
     * The Active data view.
     */
    private final Container myDataPanel;

    /**
     * The pop out model.
     */
    private final PopoutModel myModel;

    /**
     * The preference registry.
     */
    private final PreferencesRegistry myPreferenceRegistry;

    /**
     * Constructs a new layer popout view.
     *
     * @param toolbox The system toolbox.
     * @param dataPanel The active data view.
     * @param controller The controller for the view.
     */
    public LayerPopoutView(Toolbox toolbox, Container dataPanel, PopoutController controller)
    {
        super(controller.getModel().getTitle(), true, true, false);
        myDataPanel = dataPanel;
        myController = controller;
        myModel = controller.getModel();

        int x = myModel.getX();
        int y = myModel.getY();

        myBackgroundColor = AbstractHUDPanel.ourDefaultHUDBackgroundColor;

        myPreferenceRegistry = toolbox.getPreferencesRegistry();
        myPreferenceRegistry.getPreferences(AbstractHUDPanel.class)
                .addPreferenceChangeListener(AbstractHUDPanel.ourHUDBackgroundColorKey, this);

        setContentPane(myDataPanel);

        setColor();

        if (myModel.getWidth() == 0 || myModel.getHeight() == 0)
        {
            pack();
        }
        else
        {
            this.setSize(myModel.getWidth(), myModel.getHeight());
        }

        this.setLocation(x, y);
    }

    @Override
    public void dispose()
    {
        myPreferenceRegistry.getPreferences(AbstractHUDPanel.class)
                .removePreferenceChangeListener(AbstractHUDPanel.ourHUDBackgroundColorKey, this);
        super.dispose();
    }

    @Override
    public void doDefaultCloseAction()
    {
        myController.closed();
        super.doDefaultCloseAction();
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt)
    {
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                setColor();
            }
        });
    }

    @Override
    public void setFrameBoundsOnly(Rectangle bounds)
    {
        super.setFrameBoundsOnly(bounds);
        myController.boundsSet(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    /**
     * Sets the color for the view.
     */
    private void setColor()
    {
        myBackgroundColor = new Color(myPreferenceRegistry.getPreferences(AbstractHUDPanel.class)
                .getInt(AbstractHUDPanel.ourHUDBackgroundColorKey, myBackgroundColor.getRGB()), true);
        myDataPanel.setBackground(myBackgroundColor);
    }
}
