package io.opensphere.core.control.keybinding;

import java.awt.Dimension;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToggleButton;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.Binding;
import io.opensphere.core.control.BindingsToListener;
import io.opensphere.core.control.ControlContext;
import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.control.KeyBindingChangeEvent;
import io.opensphere.core.control.KeyBindingChangeListener;
import io.opensphere.core.control.SetBindingAction;

/**
 * The Class ButtonBinding. Helper class that associates a binding lister,
 * toggle button, and a reset button.
 */
public class ButtonBinding implements KeyBindingChangeListener
{
    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(ButtonBinding.class);

    /** The Bindings listener. */
    private final BindingsToListener myBindingsListener;

    /** The Key button. */
    private final JToggleButton myKeyButton;

    /** The Restore defaults button. */
    private JButton myRestoreDefaultsButton;

    /** The Binding action. */
    private final SetBindingAction myBindingAction;

    /**
     * Instantiates a new button binding.
     *
     * @param toolbox the toolbox
     * @param listener the listener
     */
    public ButtonBinding(Toolbox toolbox, BindingsToListener listener)
    {
        myBindingsListener = listener;
        Binding binding = listener.getBinding(0);
        String text = binding == null ? "" : binding.toString();
        ControlContext context = toolbox.getControlRegistry().getControlContext(ControlRegistry.GLOBE_CONTROL_CONTEXT);
        myBindingAction = new SetBindingAction(context, listener, 0, toolbox.getUIRegistry().getMainFrameProvider().get());
        myKeyButton = new JToggleButton(text);
        myKeyButton.setPreferredSize(new Dimension(150, 21));
        myKeyButton.setMaximumSize(myKeyButton.getSize());
        myKeyButton.setMinimumSize(myKeyButton.getSize());
        myKeyButton.addActionListener(myBindingAction);
    }

    /**
     * Adds the key binding change listener.
     *
     * @param listener the listener
     */
    public void addKeyBindingChangeListener(KeyBindingChangeListener listener)
    {
        myBindingAction.addKeyBindingChangeListener(listener);
    }

    /**
     * Gets the binding action.
     *
     * @return the binding action
     */
    public SetBindingAction getBindingAction()
    {
        return myBindingAction;
    }

    /**
     * Gets the bindings listener.
     *
     * @return the bindings listener
     */
    public BindingsToListener getBindingsListener()
    {
        return myBindingsListener;
    }

    /**
     * Gets the key button.
     *
     * @return the key button
     */
    public JToggleButton getKeyButton()
    {
        return myKeyButton;
    }

    /**
     * Creates the restore to default button.
     *
     * @return the j button
     */
    public JButton getRestoreToDefaultButton()
    {
        if (myRestoreDefaultsButton == null)
        {
            myRestoreDefaultsButton = new JButton();
            myRestoreDefaultsButton.setFocusPainted(false);
            myRestoreDefaultsButton.setContentAreaFilled(false);
            myRestoreDefaultsButton.setBorder(null);
            myRestoreDefaultsButton.setSize(new Dimension(18, 18));
            myRestoreDefaultsButton.setPreferredSize(myRestoreDefaultsButton.getSize());
            myRestoreDefaultsButton.setMinimumSize(myRestoreDefaultsButton.getSize());
            try
            {
                myRestoreDefaultsButton
                .setIcon(new ImageIcon(ImageIO.read(ButtonBinding.class.getResource("/images/icon-repeat-up.png"))));
                myRestoreDefaultsButton.setRolloverIcon(
                        new ImageIcon(ImageIO.read(ButtonBinding.class.getResource("/images/icon-repeat-over.png"))));
                myRestoreDefaultsButton.setPressedIcon(
                        new ImageIcon(ImageIO.read(ButtonBinding.class.getResource("/images/icon-repeat-down.png"))));
            }
            catch (IOException e)
            {
                LOGGER.error("IOException reading images.", e);
            }
            myRestoreDefaultsButton.addActionListener(e ->
            {
                myBindingAction.resetBinding(myKeyButton);
                myBindingsListener.commitBindingChanges();
            });
        }
        return myRestoreDefaultsButton;
    }

    @Override
    public void keyBindingChanged(KeyBindingChangeEvent evt)
    {
    }

    /**
     * Removes the key binding change listener.
     *
     * @param listener the listener
     */
    public void removeKeyBindingChangeListener(KeyBindingChangeListener listener)
    {
        myBindingAction.removeKeyBindingChangeListener(listener);
    }

    @Override
    public String toString()
    {
        return myKeyButton.getText();
    }
}
