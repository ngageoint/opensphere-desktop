package io.opensphere.server.manager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXBusyLabel;

import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.quantify.Quantify;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.swing.ButtonPanel;
import io.opensphere.core.util.swing.DialogPanel;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.IconButton;
import io.opensphere.core.util.swing.OptionDialog;
import io.opensphere.core.util.swing.VerticalSpacerForGridbag;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.server.control.AbstractServerSourceController;
import io.opensphere.server.services.ServerConfigEvent;
import io.opensphere.server.services.ServerConfigEvent.ServerEventAction;
import io.opensphere.server.toolbox.ServerSourceController;
import io.opensphere.server.toolbox.ServerSourceControllerManager;
import io.opensphere.server.toolbox.ServerToolboxUtils;
import io.opensphere.server.util.GridBagConstraintsBuilder;

/**
 * The Server Manager panel.
 */
public class ServerManagerPanel extends JPanel implements DialogPanel, AbstractServerSourceController.ConfigChangeListener
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ServerManagerPanel.class);

    /** Default serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Sort data sources alphabetically by name. */
    private static final Comparator<IDataSource> SOURCE_ALPHA_COMPARATOR = new Comparator<IDataSource>()
    {
        @Override
        public int compare(IDataSource o1, IDataSource o2)
        {
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    };

    /** Default background color for all Swing components. */
    private final Color myBackgroundColor;

    /** Background brightness used to set table colors and text. */
    private final int myBgBrightness;

    /** Panel that displays the table of servers. */
    private final JPanel myServerListPanel;

    /** Parent component used to locate warning dialogs. */
    private final Component myParentComponent;

    /** The server source controller manager. */
    private final ServerSourceControllerManager myControllerManager;

    /** The edit server panel. */
    private EditServerPanel myEditServerPanel;

    /** The edit server dialog. */
    private OptionDialog myEditServerDialog;

    /** The system event manager; used to publish server remove events. */
    private final EventManager myEventManager;

    /** The toolbox through which application state is accessed. */
    private final Toolbox myToolbox;

    /**
     * Determine alternate color.
     *
     * @param color the base color
     * @param brightness the brightness of the incoming color. This could be calculated on the fly, but computing it once and just
     *            passing it in saves many CPU cycles
     * @return the alternate color
     */
    private static Color determineAlternateColor(Color color, int brightness)
    {
        final double factor = 0.8;
        return brightness < 130 ? ColorUtilities.brighten(color, factor) : ColorUtilities.darken(color, factor);
    }

    /**
     * Instantiates a new OGC server configuration GUI.
     *
     * @param toolbox the Core toolbox
     * @param parent this panel's parent component used close the parent and to center confirmation dialogs
     */
    public ServerManagerPanel(Toolbox toolbox, Component parent)
    {
        super(new BorderLayout());
        myToolbox = toolbox;
        myParentComponent = parent;
        myControllerManager = ServerToolboxUtils.getServerSourceControllerManager(toolbox);
        myEventManager = toolbox.getEventManager();
        for (final ServerSourceController controller : myControllerManager.getControllers())
        {
            controller.addConfigChangeListener(this);
        }

        myBackgroundColor = getBackground();
        myBgBrightness = ColorUtilities.getBrightness(myBackgroundColor);

        myServerListPanel = new JPanel(new GridBagLayout());
        JScrollPane scrollPane = new JScrollPane(myServerListPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        add(scrollPane, BorderLayout.CENTER);

        rebuildTable();
    }

    @Override
    public boolean accept()
    {
        return true;
    }

    @Override
    public void cancel()
    {
    }

    @Override
    public void configChanged()
    {
        assert !SwingUtilities.isEventDispatchThread();
        EventQueueUtilities.invokeLater(this::rebuildTable);
    }

    @Override
    public Collection<? extends Component> getContentButtons()
    {
        return Collections.singletonList(getAddServerButton());
    }

    @Override
    public Collection<String> getDialogButtonLabels()
    {
        return Collections.singletonList(ButtonPanel.OK);
    }

    @Override
    public String getTitle()
    {
        return "Servers";
    }

    /**
     * Creates a button that is used to launch the "new server" configuration display.
     *
     * @return Button that launches the "new server" configuration
     */
    private JButton getAddServerButton()
    {
        JButton addButton = new IconButton("Add Server", IconType.PLUS, Color.GREEN);
        addButton.setToolTipText("Add a new server");
        addButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                Quantify.collectMetric("mist3d.server-panel.add-server");
                getEditServerPanel().setServerSource(null, null);
                getEditServerDialog().setTitle("Add Server");
                getEditServerDialog().showDialog();
            }
        });
        return addButton;
    }

    /**
     * Gets a busy spinner.
     *
     * @return the busy spinner
     */
    private JXBusyLabel getBusySpinner()
    {
        JXBusyLabel spinner = new JXBusyLabel(new Dimension(15, 15));
        spinner.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        spinner.setBusy(true);
        spinner.setOpaque(true);
        spinner.setHorizontalAlignment(SwingConstants.CENTER);
        return spinner;
    }

    /**
     * Build a button that allows a user to edit the given source.
     *
     * @param source the source to edit
     * @param controller the server source controller
     * @return an icon button to edit the source
     */
    private JButton getEditIconButton(final IDataSource source, final ServerSourceController controller)
    {
        JButton editButton = new JButton();
        IconUtil.setIcons(editButton, source.isActive() ? IconType.VIEW : IconType.EDIT);
        editButton.setToolTipText(StringUtilities.concat(source.isActive() ? "View " : "Edit ", source.getName()));
        editButton.setMargin(new Insets(0, 0, 0, 0));
        editButton.setBorderPainted(false);
        editButton.setContentAreaFilled(false);
        editButton.setFocusPainted(false);
        editButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Quantify.collectMetric("mist3d.server-panel." + (source.isActive() ? "view" : "edit"));
                getEditServerPanel().setServerSource(source, controller.getTypeName(source));
                getEditServerDialog().setTitle("Edit Server");
                getEditServerDialog().showDialog();
            }
        });
        editButton.setEnabled(!source.isBusy());
        return editButton;
    }

    /**
     * Build a panel with a button in it that allows a user to edit the given source.
     *
     * @param source the source to edit
     * @param controller the server source controller
     * @return a panel with a button to edit the source
     */
    private JPanel getEditIconButtonPanel(final IDataSource source, final ServerSourceController controller)
    {
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(true);
        JButton editButton = getEditIconButton(source, controller);
        buttonPanel.add(editButton);
        return buttonPanel;
    }

    /**
     * Gets the edit server dialog.
     *
     * @return the edit server dialog
     */
    private OptionDialog getEditServerDialog()
    {
        if (myEditServerDialog == null)
        {
            myEditServerDialog = new OptionDialog(myParentComponent, getEditServerPanel());
            myEditServerDialog.build(520, 520);
        }
        return myEditServerDialog;
    }

    /**
     * Gets the edit server panel.
     *
     * @return the edit server panel
     */
    private EditServerPanel getEditServerPanel()
    {
        if (myEditServerPanel == null)
        {
            myEditServerPanel = new EditServerPanel(myToolbox, myControllerManager);
        }
        return myEditServerPanel;
    }

    /**
     * Build a button that allows a user to remove the given source.
     *
     * @param source the source to remove
     * @param controller the server source controller
     * @return a button to remove the source
     */
    private JButton getRemoveButton(final IDataSource source, final ServerSourceController controller)
    {
        JButton deleteButton = new JButton();
        IconUtil.setIcons(deleteButton, IconType.CLOSE, Color.RED);
        deleteButton.setToolTipText(StringUtilities.concat("Remove ", source.getName()));
        deleteButton.setMargin(new Insets(0, 0, 0, 0));
        deleteButton.setBorderPainted(false);
        deleteButton.setContentAreaFilled(false);
        deleteButton.setFocusPainted(false);
        deleteButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Quantify.collectMetric("mist3d.server-panel.delete");
                String warningMessage = "Are you sure you want to remove\n\n\"" + source.getName() + "\"?";
                int opt = JOptionPane.showConfirmDialog(myParentComponent, warningMessage, "Confirm Remove Server",
                        JOptionPane.OK_CANCEL_OPTION);
                if (opt == JOptionPane.OK_OPTION)
                {
                    LOGGER.warn("Removing source " + source.getName());
                    controller.removeSource(source);
                    sendServerConfigEvent(source);
                    ServerManagerPanel.this.repaint();
                }
            }
        });
        deleteButton.setEnabled(!source.isBusy());
        return deleteButton;
    }

    /**
     * Build a panel with a button in it that allows a user to remove the given source.
     *
     * @param source the source to remove
     * @param controller the server source controller
     * @return a panel with a button to remove the source
     */
    private JPanel getRemoveButtonPanel(final IDataSource source, final ServerSourceController controller)
    {
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(true);
        if (!source.isActive())
        {
            JButton deleteButton = getRemoveButton(source, controller);
            buttonPanel.add(deleteButton, BorderLayout.CENTER);
        }
        return buttonPanel;
    }

    /**
     * Build a checkbox for activating/deactivating a given source.
     *
     * @param source the source
     * @param controller the server source controller
     * @return the source checkbox
     */
    private JCheckBox getSourceCheckbox(final IDataSource source, final ServerSourceController controller)
    {
        JCheckBox box = new JCheckBox();
        box.setOpaque(true);
        box.setHorizontalAlignment(SwingConstants.CENTER);
        box.setSelected(source.isActive());
        box.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                JCheckBox eventBox = (JCheckBox)e.getSource();
                LOGGER.info((eventBox.isSelected() ? "Activating " : "Deactivating ") + source.getName());
                Quantify.collectEnableDisableMetric("mist3d.server-panel.status", eventBox.isSelected());
                if (eventBox.isSelected())
                {
                    controller.activateSource(source);
                }
                else
                {
                    controller.deactivateSource(source);
                }
                rebuildTable();
            }
        });
        return box;
    }

    /**
     * Build a button that allows a user to cancel a source's activation.
     *
     * @param source the source
     * @param controller the server source controller
     * @return the cancel activation button
     */
    private JButton getCancelActivationButton(final IDataSource source, final ServerSourceController controller)
    {
        JButton cancelButton = new JButton();
        IconUtil.setIcons(cancelButton, IconType.CANCEL, Color.RED);
        cancelButton.setToolTipText(StringUtilities.concat("Cancel activaion of ", source.getName()));
        cancelButton.setMargin(new Insets(0, 0, 0, 0));
        cancelButton.setBorderPainted(false);
        cancelButton.setContentAreaFilled(false);
        cancelButton.setFocusPainted(false);
        cancelButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                LOGGER.info("Canceling loading " + source.getName());
                Quantify.collectMetric("mist3d.server-panel.cancel");
                controller.deactivateSource(source);
                rebuildTable();
            }
        });
        cancelButton.setEnabled(source.isBusy());
        return cancelButton;
    }

    /**
     * Build a panel with a button in it that allows a user to cancel activation of a given source.
     *
     * @param source the source
     * @param controller the server source controller
     * @return the panel with the button to cancel activation of the source
     */
    private JPanel getCancelActivationButtonPanel(final IDataSource source, final ServerSourceController controller)
    {
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(true);
        if (source.isBusy())
        {
            JButton cancelButton = getCancelActivationButton(source, controller);
            buttonPanel.add(cancelButton, BorderLayout.CENTER);
        }
        return buttonPanel;
    }

    /**
     * Build a label for a table header.
     *
     * @param text the name of the column
     * @param textColor the color of the column label's text
     * @return the label for the table header
     */
    private JLabel getTableHeaderLabel(String text, Color textColor)
    {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setForeground(textColor);
        label.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
        return label;
    }

    /**
     * Build up a table of servers and display them in the Layout Manager.
     */
    private void rebuildTable()
    {
        assert SwingUtilities.isEventDispatchThread();

        Color alternateColor = determineAlternateColor(myBackgroundColor, myBgBrightness);
        Color textColor = myBgBrightness < 130 ? Color.WHITE : Color.BLACK;
        myServerListPanel.removeAll();
        GridBagConstraints gbc = new GridBagConstraintsBuilder().gridy(0).fill(GridBagConstraints.BOTH).build();

        myServerListPanel.add(getTableHeaderLabel("Active", textColor), gbc);
        gbc.weightx = 1.0;
        myServerListPanel.add(getTableHeaderLabel("Title", textColor), gbc);
        gbc.weightx = 0.0;

        boolean isAlternate = false;
        for (ServerSourceController controller : myControllerManager.getControllers())
        {
            if (CollectionUtilities.hasContent(controller.getSourceList()))
            {
                List<IDataSource> srcList = New.list(controller.getSourceList());
                Collections.sort(srcList, SOURCE_ALPHA_COMPARATOR);
                for (IDataSource source : srcList)
                {
                    isAlternate = !isAlternate;
                    Color bgColor = isAlternate ? alternateColor : myBackgroundColor;
                    gbc.gridy++;
                    JComponent leftSymbol = source.isBusy() ? getBusySpinner() : getSourceCheckbox(source, controller);
                    leftSymbol.setBackground(bgColor);
                    myServerListPanel.add(leftSymbol, gbc);

                    gbc.weightx = 1.0;
                    JLabel sourceLabel = new JLabel(source.getName());
                    sourceLabel.setToolTipText(controller.getSourceDescription(source));
                    sourceLabel.setOpaque(true);
                    sourceLabel.setForeground(source.loadError() ? Color.RED : textColor);
                    sourceLabel.setBackground(bgColor);
                    myServerListPanel.add(sourceLabel, gbc);

                    gbc.weightx = 0.0;
                    JPanel cancelButtonPanel = getCancelActivationButtonPanel(source, controller);
                    cancelButtonPanel.setBackground(bgColor);
                    myServerListPanel.add(cancelButtonPanel, gbc);

                    JPanel editButtonPanel = getEditIconButtonPanel(source, controller);
                    editButtonPanel.setBackground(bgColor);
                    myServerListPanel.add(editButtonPanel, gbc);

                    JPanel removeButtonPanel = getRemoveButtonPanel(source, controller);
                    removeButtonPanel.setBackground(bgColor);
                    myServerListPanel.add(removeButtonPanel, gbc);
                }
            }
        }
        gbc.gridy++;
        gbc.weighty = 1.0;
        myServerListPanel.add(new VerticalSpacerForGridbag(), gbc);
        myServerListPanel.revalidate();
    }

    /**
     * Send server config event.
     *
     * @param source The data source.
     */
    private void sendServerConfigEvent(IDataSource source)
    {
        String serverName = source.getName();
        ServerConfigEvent event = new ServerConfigEvent(serverName, null, ServerEventAction.REMOVE);
        myEventManager.publishEvent(event);
    }
}
