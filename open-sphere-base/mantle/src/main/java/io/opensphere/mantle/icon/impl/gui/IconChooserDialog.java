package io.opensphere.mantle.icon.impl.gui;

import static io.opensphere.core.util.swing.SwingUtilities.newMenuItem;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.icon.impl.DefaultIconProvider;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * A icon chooser dialog that is has data from the IconRegistry.
 */
public class IconChooserDialog extends JDialog
{
    /** The Constant CANCELLED. */
    public static final String CANCELLED = "CANCELLED";

    /** The Constant DEFAULT_MESSAGE. */
    public static final String DEFAULT_MESSAGE = "Use the tree on the left to select the icon set"
            + ", then select an icon from the grid.";

    /** The Constant ICON_SELECTED. */
    public static final String ICON_SELECTED = "ICON_SELECTED";

    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The Action listeners. */
    private final Set<ActionListener> myActionListeners = New.set();

    /** The Chooser panel. */
    private final IconChooserPanel myChooserPanel;

    /** The Toolbox. */
    private final Toolbox myToolbox;

    /** Made a member to prevent garbage collection. */
    private final ActionListener mySelectionListener;

    /**
     * Instantiates a new icon chooser dialog with the default chooser message.
     *
     * @param owner the {@link Frame} that owns the dialog.
     * @param modal true for modal
     * @param tb the {@link Toolbox}
     */
    public IconChooserDialog(Frame owner, boolean modal, Toolbox tb)
    {
        this(owner, modal, DEFAULT_MESSAGE, tb);
    }

    /**
     * Instantiates a new icon chooser dialog.
     *
     * @param owner the {@link Frame} that owns this dialog.
     * @param modal true for modal
     * @param message the message to display to the user.
     * @param tb the {@link Toolbox}
     */
    public IconChooserDialog(Component owner, boolean modal, String message, Toolbox tb)
    {
        super(SwingUtilities.getWindowAncestor(owner), modal ? ModalityType.APPLICATION_MODAL : ModalityType.MODELESS);
        myToolbox = tb;
        setTitle("Icon Chooser");
        setSize(new Dimension(800, 600));

        JPopupMenu puMenu = new JPopupMenu();
        puMenu.add(newMenuItem("Add To Favorites", e -> addToFavorites()));
        puMenu.add(newMenuItem("Rotate Icon...", e -> showRotateDialog()));
        puMenu.add(newMenuItem("Delete Icon...", e -> deleteSelected()));

        myChooserPanel = new IconChooserPanel(tb, false, true, puMenu, null);

        JPanel msgPanel = new JPanel(new BorderLayout());
        msgPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 10));

        JTextArea jta = new JTextArea();
        jta.setLineWrap(true);
        jta.setWrapStyleWord(true);
        jta.setFont(jta.getFont().deriveFont(Font.BOLD, jta.getFont().getSize() + 5));
        jta.setText(message);
        jta.setEditable(false);
        jta.setBackground(msgPanel.getBackground());
        jta.setBorder(BorderFactory.createEmptyBorder());

        msgPanel.add(jta, BorderLayout.CENTER);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel closePanel = new JPanel();
        closePanel.setLayout(new BoxLayout(closePanel, BoxLayout.X_AXIS));
        JButton closeButton = new JButton("Cancel");
        closeButton.setMaximumSize(new Dimension(100, 30));
        closeButton.addActionListener(e -> close(CANCELLED));
        closePanel.add(Box.createHorizontalGlue());
        closePanel.add(closeButton);
        closePanel.add(Box.createHorizontalStrut(50));
        closePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        mySelectionListener = e -> close(ICON_SELECTED);
        myChooserPanel.addActionListener(mySelectionListener);

        mainPanel.add(msgPanel, BorderLayout.NORTH);
        mainPanel.add(myChooserPanel, BorderLayout.CENTER);
        mainPanel.add(closePanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
        setLocationRelativeTo(SwingUtilities.getWindowAncestor(owner));
    }

    /**
     * Closes the dialog and fires the given action command.
     *
     * @param actionCommand the action command
     */
    private void close(String actionCommand)
    {
        setVisible(false);
        fireActionPerformed(new ActionEvent(this, 0, actionCommand));
    }

    /**
     * Adds the selected icon to the favorites.
     */
    private void addToFavorites()
    {
        IconRecord rec = myChooserPanel.getLastPopupTriggerIconRecord();
        if (rec != null)
        {
            DefaultIconProvider provider = new DefaultIconProvider(rec.getImageURL(), IconRecord.FAVORITES_COLLECTION, null,
                    "User");
            MantleToolboxUtils.getMantleToolbox(myToolbox).getIconRegistry().addIcon(provider, this);
            myChooserPanel.refreshFromRegistry(IconRecord.FAVORITES_COLLECTION);
        }
    }

    /**
     * Shows the icon rotation dialog.
     */
    private void showRotateDialog()
    {
        IconRecord record = myChooserPanel.getLastPopupTriggerIconRecord();
        if (record != null)
        {
            IconRegistry iconRegistry = MantleToolboxUtils.getMantleToolbox(myToolbox).getIconRegistry();
            IconRotationDialog dialog = new IconRotationDialog(this, record, iconRegistry, myChooserPanel);
            dialog.setVisible(true);
        }
    }

    /**
     * Removes the selected icon from the registry.
     */
    private void deleteSelected()
    {
        IconRecord record = myChooserPanel.getLastPopupTriggerIconRecord();
        if (record != null)
        {
            int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the selected icon?\n"
                    + "The icon file on your computer will not be deleted \n" + "but the application will no longer remember it.",
                    "Delete Icon Confirmation", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.OK_OPTION)
            {
                MantleToolboxUtils.getMantleToolbox(myToolbox).getIconRegistry().removeIcon(record, this);
                myChooserPanel.refreshFromRegistry(record.getCollectionName());
            }
        }
    }

    /**
     * Adds the action listener.
     *
     * @param listener the listener
     */
    public void addActionListener(ActionListener listener)
    {
        synchronized (myActionListeners)
        {
            myActionListeners.add(listener);
        }
    }

    /**
     * Gets the selected icon.
     *
     * @return the selected icon
     */
    public IconRecord getSelectedIcon()
    {
        return myChooserPanel.getSelectedIcon();
    }

    /**
     * Removes the action listener.
     *
     * @param listener the listener
     */
    public void removeActionListener(ActionListener listener)
    {
        synchronized (myActionListeners)
        {
            myActionListeners.remove(listener);
        }
    }

    /**
     * Sets the selected icon URL.
     *
     * @param selectedUrl the icon URL
     */
    public void setSelectedUrl(String selectedUrl)
    {
        myChooserPanel.setSelectedUrl(selectedUrl);
    }

    /**
     * Fire action performed.
     *
     * @param e the e
     */
    private void fireActionPerformed(final ActionEvent e)
    {
        assert EventQueue.isDispatchThread();

        synchronized (myActionListeners)
        {
            for (ActionListener al : myActionListeners)
            {
                al.actionPerformed(e);
            }
        }
    }
}
