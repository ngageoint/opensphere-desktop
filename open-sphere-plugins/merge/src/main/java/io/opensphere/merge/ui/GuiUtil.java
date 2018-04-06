package io.opensphere.merge.ui;

import java.awt.Dimension;
import java.awt.Window;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.util.fx.JFXDialog;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;

/** A collection of GUI-related static utility functions. */
public final class GuiUtil
{
    /**
     * Private constructor to prevent instantiation.
     */
    private GuiUtil()
    {
        throw new UnsupportedOperationException("Instantiation of utility classes is not permitted.");
    }

    /**
     * Construct a JFXDialog instance subordinate to the main application frame
     * (which is supplied (indirectly) by the system Toolbox). The returned
     * dialog has a button bar showing only the "OK" button.
     *
     * @param tools the system Toolbox
     * @param title the window title
     * @return the JFXDialog
     */
    public static JFXDialog okDialog(Toolbox tools, String title)
    {
        return getDialog(tools.getUIRegistry().getMainFrameProvider().get(), title, false);
    }

    /**
     * Construct a JFXDialog instance subordinate to the main application frame
     * (which is supplied (indirectly) by the system Toolbox). The returned
     * dialog has a button bar showing "OK" and "Cancel" buttons.
     *
     * @param tools the system Toolbox
     * @param title the window title
     * @return the JFXDialog
     */
    public static JFXDialog okCancelDialog(Toolbox tools, String title)
    {
        return getDialog(tools.getUIRegistry().getMainFrameProvider().get(), title, true);
    }

    /**
     * Construct a JFXDialog instance subordinate to the specified AWT Window
     * instance. The returned dialog has a button bar showing only the "OK"
     * button.
     *
     * @param par the parent AWT Window
     * @param title the window title
     * @return the JFXDialog
     */
    public static JFXDialog okDialog(Window par, String title)
    {
        return getDialog(par, title, false);
    }

    /**
     * Construct a JFXDialog instance subordinate to the specified AWT Window
     * instance. The returned dialog has a button bar showing "OK" and "Cancel"
     * buttons.
     *
     * @param par the parent AWT Window
     * @param title the window title
     * @return the JFXDialog
     */
    public static JFXDialog okCancelDialog(Window par, String title)
    {
        return getDialog(par, title, true);
    }

    /**
     * Construct a JFXDialog instance subordinate to the specified AWT Window
     * instance.
     *
     * @param par the parent AWT Window
     * @param title the window title
     * @param showCancel if true, shows the "Cancel" button
     * @return the JFXDialog
     */
    private static JFXDialog getDialog(Window par, String title, boolean showCancel)
    {
        JFXDialog dialog = new JFXDialog(par, title, showCancel);
        dialog.setSize(new Dimension(450, 200));
        dialog.setResizable(true);
        dialog.setLocationRelativeTo(par);
        dialog.setModal(false);
        return dialog;
    }

    /**
     * Reduce Button creation to one method call.
     *
     * @param txt the Button label
     * @param ear callback for event handling
     * @return a Button
     */
    public static Button button(String txt, Runnable ear)
    {
        Button b = new Button(txt);
        if (ear != null)
        {
            b.setOnAction(e -> ear.run());
        }
        return b;
    }

    /**
     * Reduce boiler-plate in creating a JavaFX RadioButton. Also turns off the
     * heinous "mnemonic parsing" B.S. that screws up the text if an underscore
     * is present.
     *
     * @param txt the button label
     * @param group the group, if any, to which the RadioButton is added
     * @return the RadioButton
     */
    public static RadioButton radio(String txt, ToggleGroup group)
    {
        RadioButton rb = new RadioButton();
        rb.setMnemonicParsing(false);
        rb.setText(txt);
        if (group != null)
        {
            group.getToggles().add(rb);
        }
        return rb;
    }

    /**
     * Wrap a Node in a ScrollPane for vertical scrolling only.
     *
     * @param n the Node
     * @return the ScrollPane
     */
    public static ScrollPane vScroll(Node n)
    {
        ScrollPane scroll = new ScrollPane(n);
        scroll.setFitToWidth(true);
        return scroll;
    }

    /**
     * Factory method for creating an empty JavaFX Border.
     *
     * @param thick thickness of the Border
     * @return the Border
     */
    public static Border emptyBorder(double thick)
    {
        return new Border(new BorderStroke(null, null, null, null, new Insets(thick)));
    }

    /**
     * Add a menu item to a menu.
     *
     * @param m the menu
     * @param txt the text for the menu item
     * @param ear the listener for events
     */
    public static void addMenuItem(JMenu m, String txt, Runnable ear)
    {
        if (m == null)
        {
            return;
        }
        JMenuItem jmi = new JMenuItem(txt);
        if (ear != null)
        {
            jmi.addActionListener(e -> ear.run());
        }
        m.add(jmi);
    }

    /**
     * Get the menu registered to the main menu bar under the given key.
     *
     * @param tools the system Toolbox
     * @param key the registration key
     * @return the menu, which may be null
     */
    public static JMenu getMainMenu(Toolbox tools, String key)
    {
        MenuBarRegistry mbr = tools.getUIRegistry().getMenuBarRegistry();
        return mbr.getMenu(MenuBarRegistry.MAIN_MENU_BAR, key);
    }

    /**
     * Invoke the supplied Runnable on the JavaFX thread and wait for it to
     * return before proceeding. JavaFX fails to deliver this feature, probably
     * because they incorrectly believe thay have a good reason.
     *
     * @param r Runnable
     */
    public static void invokeJfx(Runnable r)
    {
        if (Platform.isFxApplicationThread())
        {
            r.run();
        }
        else
        {
            new JfxVehicle(r).go();
        }
    }

    /** Encapsulate a Runnable task to be executed on the JavaFX thread. */
    private static class JfxVehicle
    {
        /** The nested Runnable task. */
        private final Runnable task;

        /**
         * Create.
         *
         * @param r the task to execute
         */
        public JfxVehicle(Runnable r)
        {
            task = r;
        }

        /** Perform the task. */
        public void go()
        {
            synchronized (this)
            {
                Platform.runLater(() -> runJfx());
                try
                {
                    wait();
                }
                catch (Exception eek)
                {
                    // Do not log
                }
            }
        }

        /** Execute on JFX Thread. */
        private void runJfx()
        {
            synchronized (this)
            {
                try
                {
                    task.run();
                }
                finally
                {
                    notify();
                }
            }
        }
    }
}
