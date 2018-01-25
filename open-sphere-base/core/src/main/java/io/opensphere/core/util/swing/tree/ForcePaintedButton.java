package io.opensphere.core.util.swing.tree;

import java.awt.Graphics;

import javax.swing.ImageIcon;

import io.opensphere.core.util.swing.QuadStateIconButton;

/**
 * A button which paints the background even when the button is transparent. If
 * the button is opaque, the background will be painted twice.
 */
public class ForcePaintedButton extends QuadStateIconButton
{
    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The state updater. */
    private final ButtonStateUpdater myStateUpdater;

    /**
     * Constructor.
     *
     * @param defaultIcon The default icon.
     * @param rolloverIcon The the rollover icon.
     * @param pressedIcon The pressed icon.
     * @param selectedIcon the selected icon
     * @param updator the updater
     */
    public ForcePaintedButton(ImageIcon defaultIcon, ImageIcon rolloverIcon, ImageIcon pressedIcon, ImageIcon selectedIcon,
            ButtonStateUpdater updator)
    {
        super(defaultIcon, rolloverIcon, pressedIcon, selectedIcon);
        myStateUpdater = updator;
    }

    /**
     * Gets the state updater.
     *
     * @return the state updater
     */
    public ButtonStateUpdater getStateUpdater()
    {
        return myStateUpdater;
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        // Paint the background color even if the button is transparent.
        // If the button is opaque, this color will be painted over again.
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        if (!isHidden())
        {
            super.paintComponent(g);
        }
    }
}
