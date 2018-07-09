package io.opensphere.core.appl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.PluginAdapter;
import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.event.ApplicationLifecycleEvent;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.quantify.QuantifyToolboxUtils;
import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.javafx.WebPanel;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.GenericFontIcon;

/**
 * Plugin for the "What's New" dialog shown at initialization.
 */
public class WhatsNewPlugin extends PluginAdapter
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(WhatsNewPlugin.class);

    /** The index of the current message. */
    private int myCurrentIndex;

    /** The dialog. */
    private JDialog myDialog;

    /** The don't show again checkbox. */
    private JCheckBox myDontShowAgainCheckbox;

    /**
     * The checkbox used to inform the user of anonymous metrics collection, and
     * allow them to disable it.
     */
    private JCheckBox myMetricsCheckbox;

    /** The web panel. */
    private WebPanel myWebPanel;

    /** The toolbox. */
    private Toolbox myToolbox;

    /** Listener for lifecycle events. */
    private final EventListener<ApplicationLifecycleEvent> myLifeCycleEventListener = new EventListener<>()
    {
        @Override
        public void notify(ApplicationLifecycleEvent event)
        {
            if (event.getStage() == ApplicationLifecycleEvent.Stage.MAIN_FRAME_VISIBLE)
            {
                EventQueueUtilities.runOnEDT(() -> showWhatsNew(false));
            }
        }
    };

    /** The next button. */
    private JButton myNextButton;

    /** The parent component provider. */
    private Supplier<? extends Frame> myParentComponentProvider;

    /** The preferences. */
    private Preferences myPrefs;

    /** The previous button. */
    private JButton myPrevButton;

    /** The properties from the properties file. */
    private final List<Pair<String, String>> myProperties = New.list();

    @Override
    public void initialize(PluginLoaderData plugindata, final Toolbox toolbox)
    {
        myToolbox = toolbox;
        myParentComponentProvider = toolbox.getUIRegistry().getMainFrameProvider();
        myPrefs = toolbox.getPreferencesRegistry().getPreferences(WhatsNewPlugin.class);

        PropertiesLoader propertiesLoader = new PropertiesLoader(myProperties);
        try
        {
            for (URL url : Collections.list(WhatsNewPlugin.class.getClassLoader().getResources("whatsnew.properties")))
            {
                propertiesLoader.load(url.openStream());
            }
        }
        catch (IOException e)
        {
            LOGGER.warn("Failed to load whatsnew.properties: " + e, e);
            return;
        }

        if (!myProperties.isEmpty())
        {
            toolbox.getEventManager().subscribe(ApplicationLifecycleEvent.class, myLifeCycleEventListener);

            EventQueueUtilities.invokeLater(() ->
            {
                JMenuItem menuButton = new JMenuItem("What's New");
                menuButton.addActionListener(e -> showWhatsNew(true));

                JMenu menu = toolbox.getUIRegistry().getMenuBarRegistry().getMenu(MenuBarRegistry.MAIN_MENU_BAR,
                        MenuBarRegistry.HELP_MENU);
                menu.add(menuButton);
            });
        }
    }

    /**
     * Display the what's new dialog.
     *
     * @param force Show the dialog even if the preferences indicate that it's
     *            already been shown.
     */
    protected void showWhatsNew(boolean force)
    {
        for (int index = myProperties.size() - 1; index >= 0; --index)
        {
            Pair<String, String> pair = myProperties.get(index);
            if (force || !myPrefs.getBoolean(pair.getFirstObject(), false))
            {
                showWhatsNew(index);
                break;
            }
        }
    }

    /**
     * Show the dialog displaying the message for a certain index.
     *
     * @param index The index to display.
     */
    protected void showWhatsNew(final int index)
    {
        myCurrentIndex = index;

        if (myDialog == null)
        {
            createDialog();
        }

        updateDialog(myCurrentIndex);
        if (!myDialog.isVisible())
        {
            myDialog.setLocationRelativeTo(myParentComponentProvider.get());
        }
        myDialog.setVisible(true);
    }

    /**
     * Update the dialog with the version and message for a certain index.
     *
     * @param index The index.
     */
    protected void updateDialog(int index)
    {
        Pair<String, String> pair = myProperties.get(myCurrentIndex);
        String version = pair.getFirstObject();
        String message = StringUtilities.expandProperties(pair.getSecondObject(), System.getProperties());

        myWebPanel.loadContent(message);
        myWebPanel.setTitleListener(title -> setTitle(title, version));

        myPrevButton.setEnabled(index != 0);
        myNextButton.setEnabled(index != myProperties.size() - 1);
    }

    /**
     * If the "Don't show again" box is checked, update the preference for the
     * currently displayed message.
     */
    protected void updatePreference()
    {
        boolean selected = myDontShowAgainCheckbox.isSelected();
        for (Pair<String, String> pair : myProperties)
        {
            myPrefs.putBoolean(pair.getFirstObject(), selected, this);
        }

        QuantifyToolboxUtils.getQuantifyToolbox(myToolbox).getSettingsModel().enabledProperty()
                .set(myMetricsCheckbox.isSelected());
    }

    /**
     * Create the dialog.
     */
    private void createDialog()
    {
        myWebPanel = new WebPanel();

        myPrevButton = new JButton("Previous");
        myPrevButton.addActionListener(e -> updateDialog(--myCurrentIndex));

        final JButton autoProxyWizardButton = new JButton("Run Auto-Proxy Wizard");
        autoProxyWizardButton.addActionListener(e ->
        {
            myToolbox.getUIRegistry().getOptionsRegistry().requestShowTopic("Automatic Proxy");
            myDialog.setVisible(false);
        });

        final JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> myDialog.setVisible(false));

        myNextButton = new JButton("Next");
        myNextButton.addActionListener(e -> updateDialog(++myCurrentIndex));

        myDontShowAgainCheckbox = new JCheckBox("Don't show this again (access from Help menu)", true);
        myMetricsCheckbox = new JCheckBox("Enable anonymous usage metrics collection",
                QuantifyToolboxUtils.getQuantifyToolbox(myToolbox).getSettingsModel().enabledProperty().get());

        JLabel tooltipLabel = new JLabel(new GenericFontIcon(AwesomeIconSolid.QUESTION_CIRCLE, Color.WHITE, 10));
        tooltipLabel.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                myToolbox.getUIRegistry().getOptionsRegistry().requestShowTopic("Usage Statistics");
                myDialog.setVisible(false);
            }
        });

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        Box box = Box.createHorizontalBox();
        box.add(myMetricsCheckbox);
        box.add(tooltipLabel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = 3;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.WEST;
        buttonPanel.add(box, gbc);

        gbc.gridy++;
        gbc.gridwidth = 3;
        gbc.insets.bottom = 10;
        gbc.anchor = GridBagConstraints.WEST;
        buttonPanel.add(myDontShowAgainCheckbox, gbc);

        gbc.insets.bottom = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.weightx = 1.;
        gbc.anchor = GridBagConstraints.WEST;
        buttonPanel.add(myPrevButton, gbc);
        buttonPanel.add(autoProxyWizardButton, gbc);
        gbc.anchor = GridBagConstraints.CENTER;
        buttonPanel.add(closeButton, gbc);
        gbc.anchor = GridBagConstraints.EAST;
        buttonPanel.add(myNextButton, gbc);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setPreferredSize(new Dimension(700, 600));
        mainPanel.add(myWebPanel);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        myDialog = new JDialog(myParentComponentProvider.get());
        myDialog.setContentPane(mainPanel);
        myDialog.pack();
        myDialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(myParentComponentProvider.get()));
        myDialog.addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentHidden(ComponentEvent e)
            {
                updatePreference();
            }
        });
        myWebPanel.setBackground(buttonPanel.getBackground());
    }

    /**
     * Sets the title of the dialog from a non-Swing thread.
     *
     * @param title the title
     * @param version the version
     */
    private void setTitle(String title, String version)
    {
        myDialog.setTitle(StringUtils.isBlank(title) ? "What's new in " + version : title);
    }

    /**
     * Extension of {@link Properties} that loads the properties into a list.
     */
    @SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
    private static final class PropertiesLoader extends Properties
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /** The properties from the properties file. */
        @SuppressFBWarnings("SE_BAD_FIELD")
        private final List<Pair<String, String>> myProperties;

        /**
         * Construct the properties loader.
         *
         * @param properties The list to load the properties into.
         */
        public PropertiesLoader(List<Pair<String, String>> properties)
        {
            super();
            myProperties = properties;
        }

        @Override
        public synchronized Object put(Object key, Object value)
        {
            // Only add the property value to the list if it isn't blank and it
            // doesn't already exist. This allows a whatsnew.properties file in
            // the override.jar to disable content from the main
            // whatsnew.properties.
            if (!containsKey(key) && !StringUtils.isBlank((String)value))
            {
                myProperties.add(new Pair<>((String)key, (String)value));
            }
            return super.put(key, value);
        }
    }
}
