package io.opensphere.server.control;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;

import io.opensphere.core.event.EventManager;
import io.opensphere.core.options.impl.AbstractPreferencesOptionsProvider;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.quantify.QuantifyToolboxUtils;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.util.swing.DocumentListenerAdapter;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.server.services.ServerTimeoutChangeEvent;
import io.opensphere.server.source.OGCServerSource;
import io.opensphere.server.toolbox.ServerSourceController;
import io.opensphere.server.toolbox.ServerSourceController.ConfigChangeListener;
import io.opensphere.server.toolbox.ServerSourceControllerManager;
import io.opensphere.server.util.ServerConstants;

/**
 * The Class OGCServerPluginOptionsProvider.
 */
@SuppressWarnings("PMD.GodClass")
public class OGCServerTimeoutsOptionsProvider extends AbstractPreferencesOptionsProvider
        implements ConfigChangeListener, ServerSourceControllerManager.LoadListener
{
    /** The Constant TIMEOUT_HEADER. */
    private static final String TIMEOUT_HEADER = "Timeout (s)";

    /** The our original tf border. */
    private static Border ourOriginalTFBorder = new JTextField().getBorder();

    /** The Panel. */
    private final JPanel myPanel;

    /** The Default timeout panel. */
    private final ServerTimeoutPanel myDefaultTimeoutPanel;

    /** The Server list panel. */
    private JPanel myServerListPanel;

    /** The Server list scroll pane. */
    private JScrollPane myServerListScrollPane;

    /** The Manager for ServerSourceControllers. */
    private final ServerSourceControllerManager myCtrlMgr;

    /** The Server table panel. */
    private JPanel myServerTablePanel;

    /** The Server tile list lock. */
    private final ReentrantLock myServerTileListLock = new ReentrantLock();

    /** The Server timeout panel list. */
    private final List<ServerTimeoutPanel> myServerTimeoutPanelList = new ArrayList<>();

    /** The Error reporter. */
    private final ErrorReporter myErrorReporter;

    /** The event manager. */
    private final EventManager myEventManager;

    /**
     * Instantiates a new wFS plugin options provider.
     *
     * @param prefsRegistry The system preferences registry.
     * @param eventManager The event manager.
     * @param serverCtrlMgr the server controller manager
     */
    public OGCServerTimeoutsOptionsProvider(PreferencesRegistry prefsRegistry, EventManager eventManager,
            ServerSourceControllerManager serverCtrlMgr)
    {
        super(prefsRegistry, ServerConstants.OGC_SERVER_OPTIONS_PROVIDER_MAIN_TOPIC);
        myEventManager = eventManager;
        myCtrlMgr = serverCtrlMgr;
        myPanel = new JPanel();
        myPanel.setBackground(DEFAULT_BACKGROUND_COLOR);
        myPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
        myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.Y_AXIS));

        JLabel jl = new JLabel("Server Settings");
        jl.setHorizontalAlignment(JLabel.CENTER);
        jl.setFont(jl.getFont().deriveFont(Font.BOLD, jl.getFont().getSize() + 2));
        JPanel jp = new JPanel(new BorderLayout());
        jp.add(jl, BorderLayout.CENTER);
        myPanel.add(jp);
        myPanel.add(Box.createVerticalStrut(5));
        myPanel.add(new ServerTimeoutHeaderPanel("", "Activate", "Connect", "Read", "", ""));
//        myPanel.add(new ServerTimeoutHeaderPanel("", TIMEOUT_HEADER, TIMEOUT_HEADER, TIMEOUT_HEADER, ""));
        myErrorReporter = new ErrorReporter();
        myDefaultTimeoutPanel = new ServerTimeoutPanel(prefsRegistry, eventManager, null, null, myErrorReporter, true);
        myPanel.add(myDefaultTimeoutPanel);
        myPanel.add(Box.createVerticalStrut(2));
        myPanel.add(myErrorReporter);
        myPanel.add(Box.createVerticalStrut(2));
        myPanel.add(getServerListPanel());
        myPanel.add(Box.createVerticalGlue());
        JPanel emptyPanel = new JPanel();
        emptyPanel.setBackground(TRANSPARENT_COLOR);
        myPanel.add(emptyPanel);

        myCtrlMgr.addLoadListener(this);
    }

    @Override
    public void applyChanges()
    {
        QuantifyToolboxUtils.collectMetric("mist3d.settings.servers.apply-button");
        myErrorReporter.clearErrors();
        boolean isValid = myDefaultTimeoutPanel.arePanelInputsValid();
        if (isValid)
        {
            myServerTileListLock.lock();
            try
            {
                for (ServerTimeoutPanel panel : myServerTimeoutPanelList)
                {
                    isValid = panel.arePanelInputsValid();
                    if (!isValid)
                    {
                        break;
                    }
                }

                if (isValid)
                {
                    boolean defaultHasChanges = myDefaultTimeoutPanel.hasChangesFromSource();
                    if (defaultHasChanges)
                    {
                        myDefaultTimeoutPanel.saveChangesToSource();
                    }
                    for (ServerTimeoutPanel panel : myServerTimeoutPanelList)
                    {
                        if (panel.hasChangesFromSource())
                        {
                            panel.saveChangesToSource();
                        }
                        else if (panel.sourceIsSetToDefault() && defaultHasChanges)
                        {
                            panel.restoreDefaults();
                        }
                    }
                }
            }
            finally
            {
                myServerTileListLock.unlock();
            }
        }
    }

    @Override
    public void configChanged()
    {
        rebuildSpecifcServerTimeoutPanel();
    }

    @Override
    public JPanel getOptionsPanel()
    {
        return myPanel;
    }

    @Override
    public void loadComplete()
    {
        for (ServerSourceController ctrl : myCtrlMgr.getControllers())
        {
            ctrl.addConfigChangeListener(this);
        }
        rebuildSpecifcServerTimeoutPanel();
    }

    @Override
    public void restoreDefaults()
    {
        myErrorReporter.clearErrors();
        myDefaultTimeoutPanel.restoreDefaults();
        myServerTileListLock.lock();
        try
        {
            for (ServerTimeoutPanel panel : myServerTimeoutPanelList)
            {
                panel.restoreDefaults();
            }
        }
        finally
        {
            myServerTileListLock.unlock();
        }
    }

    /**
     * Gets the server list panel.
     *
     * @return the server list panel
     */
    private Component getServerListPanel()
    {
        if (myServerListPanel == null)
        {
            myServerListPanel = new JPanel(new BorderLayout());
            myServerListPanel.setBackground(TRANSPARENT_COLOR);
            myServerListPanel.add(getServerListScrollPane(), BorderLayout.CENTER);
        }
        return myServerListPanel;
    }

    /**
     * Gets the server list scroll pane.
     *
     *
     * @return the server list scroll pane
     */
    private Component getServerListScrollPane()
    {
        if (myServerListScrollPane == null)
        {
            myServerListScrollPane = new JScrollPane(getServerTablePanel());
            JPanel headerPanel = new JPanel();
            headerPanel.setBackground(TRANSPARENT_COLOR);
            headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
            headerPanel.add(new ServerTimeoutHeaderPanel("Server", "Activate", "Connect", "Read", "Permalink", "Use"));
            headerPanel
                    .add(new ServerTimeoutHeaderPanel("Name", TIMEOUT_HEADER, TIMEOUT_HEADER, TIMEOUT_HEADER, "URL", "Default"));
            myServerListScrollPane.setColumnHeaderView(headerPanel);
            myServerListScrollPane.setBackground(TRANSPARENT_COLOR);
        }
        return myServerListScrollPane;
    }

    /**
     * Gets the server table panel.
     *
     *
     * @return the server table panel
     */
    private Component getServerTablePanel()
    {
        if (myServerTablePanel == null)
        {
            myServerTablePanel = new JPanel();
            myServerTablePanel.setLayout(new BoxLayout(myServerTablePanel, BoxLayout.Y_AXIS));
            myServerTablePanel.setBackground(TRANSPARENT_COLOR);
        }
        return myServerTablePanel;
    }

    /**
     * Gets a list of all server sources.
     *
     * @param ctrlMgr the manager for {@link ServerSourceController}s
     * @return list of sources and the controllers responsible for them
     */
    private List<Pair<ServerSourceController, IDataSource>> getSources(ServerSourceControllerManager ctrlMgr)
    {
        List<Pair<ServerSourceController, IDataSource>> sourceList = New.list();
        for (ServerSourceController ctrl : ctrlMgr.getControllers())
        {
            for (IDataSource source : ctrl.getSourceList())
            {
                sourceList.add(new Pair<ServerSourceController, IDataSource>(ctrl, source));
            }
        }
        return sourceList;
    }

    /**
     * Rebuild specifc server timeout panel.
     */
    private void rebuildSpecifcServerTimeoutPanel()
    {
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                myErrorReporter.clearErrors();
                myServerTablePanel.removeAll();

                List<Pair<ServerSourceController, IDataSource>> sourceList = getSources(myCtrlMgr);
                Collections.sort(sourceList, new Comparator<Pair<ServerSourceController, IDataSource>>()
                {
                    @Override
                    public int compare(Pair<ServerSourceController, IDataSource> pair1,
                            Pair<ServerSourceController, IDataSource> pair2)
                    {
                        return pair1.getSecondObject().getName().compareTo(pair2.getSecondObject().getName());
                    }
                });

                myServerTileListLock.lock();
                try
                {
                    myServerTimeoutPanelList.clear();
                    for (Pair<ServerSourceController, IDataSource> src : sourceList)
                    {
                        if (src.getSecondObject() instanceof OGCServerSource)
                        {
                            ServerTimeoutPanel pnl = new ServerTimeoutPanel(getPreferencesRegistry(), myEventManager,
                                    src.getFirstObject(), (OGCServerSource)src.getSecondObject(), myErrorReporter, false);
                            myServerTimeoutPanelList.add(pnl);
                            myServerTablePanel.add(pnl);
                            myServerTablePanel.add(Box.createVerticalStrut(2));
                        }
                    }
                }
                finally
                {
                    myServerTileListLock.unlock();
                }

                myServerTablePanel.add(Box.createVerticalGlue());
                JPanel emptyPanel = new JPanel();
                emptyPanel.setBackground(TRANSPARENT_COLOR);
                myServerTablePanel.add(emptyPanel);
                myServerTablePanel.revalidate();
                myServerTablePanel.repaint();
            }
        });
    }

    /**
     * The Interface ErrorReporter.
     */
    @SuppressWarnings("serial")
    private static class ErrorReporter extends JPanel
    {
        /** The Label. */
        private final JLabel myLabel;

        /**
         * Instantiates a new error reporter.
         */
        public ErrorReporter()
        {
            super(new BorderLayout());
            setMaximumSize(new Dimension(5000, 40));
            setMinimumSize(new Dimension(400, 30));
            setPreferredSize(new Dimension(400, 30));
            setBackground(TRANSPARENT_COLOR);
            myLabel = new JLabel("VALID");
            myLabel.setHorizontalAlignment(JLabel.CENTER);
            add(myLabel, BorderLayout.CENTER);
            myLabel.setVisible(false);
        }

        /**
         * Clear errors.
         */
        public void clearErrors()
        {
            myLabel.setVisible(false);
            myLabel.setForeground(Color.GREEN);
            myLabel.setText("VALID");
        }

        /**
         * Report error.
         *
         * @param error the error
         */
        public void reportError(String error)
        {
            myLabel.setVisible(true);
            myLabel.setForeground(Color.red);
            myLabel.setText(error);
        }
    }

    /**
     * The Class ServerTimeoutHeaderPanel.
     */
    @SuppressWarnings("serial")
    private static class ServerTimeoutHeaderPanel extends JPanel
    {
        /**
         * Instantiates a new server timeout header panel.
         *
         *
         * @param c1Val the c1 val
         * @param c2Val the c2 val
         * @param c3Val the c3 val
         * @param c4Val the c4 val
         * @param c5Val the c5 val
         * @param c6Val the c6 val
         */
        public ServerTimeoutHeaderPanel(String c1Val, String c2Val, String c3Val, String c4Val, String c5Val, String c6Val)
        {
            super();
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setMaximumSize(new Dimension(5000, 20));
            setMinimumSize(new Dimension(400, 20));
            setPreferredSize(new Dimension(400, 20));

            JPanel namePanel = new JPanel(new BorderLayout());
            namePanel.setBackground(DEFAULT_BACKGROUND_COLOR);
            namePanel.setMaximumSize(new Dimension(200, 20));
            namePanel.setMinimumSize(new Dimension(200, 20));
            namePanel.setPreferredSize(new Dimension(200, 20));
            namePanel.add(createLabel(c1Val), BorderLayout.CENTER);
            add(namePanel);

            add(Box.createHorizontalStrut(5));

            JPanel activateTimeoutPanel = new JPanel(new BorderLayout());
            activateTimeoutPanel.setBackground(DEFAULT_BACKGROUND_COLOR);
            activateTimeoutPanel.setMaximumSize(new Dimension(70, 30));
            activateTimeoutPanel.setMinimumSize(new Dimension(70, 20));
            activateTimeoutPanel.setPreferredSize(new Dimension(70, 30));
            activateTimeoutPanel.add(
                    createLabel(c2Val,
                            "Activate timeout (in seconds) is how long the application will wait to set up the initial server services."),
                    BorderLayout.CENTER);
            add(activateTimeoutPanel);

            JPanel connectTimeoutPanel = new JPanel(new BorderLayout());
            connectTimeoutPanel.setBackground(DEFAULT_BACKGROUND_COLOR);
            connectTimeoutPanel.setMaximumSize(new Dimension(70, 30));
            connectTimeoutPanel.setMinimumSize(new Dimension(70, 20));
            connectTimeoutPanel.setPreferredSize(new Dimension(70, 30));
            connectTimeoutPanel.add(
                    createLabel(c3Val,
                            "Connect timeout (in seconds) is how long the application will wait to establish a connection with the server for each request."),
                    BorderLayout.CENTER);
            add(Box.createHorizontalStrut(5));
            add(connectTimeoutPanel);

            JPanel readTimeoutPanel = new JPanel(new BorderLayout());
            readTimeoutPanel.setBackground(DEFAULT_BACKGROUND_COLOR);
            readTimeoutPanel.setMaximumSize(new Dimension(70, 30));
            readTimeoutPanel.setMinimumSize(new Dimension(70, 20));
            readTimeoutPanel.setPreferredSize(new Dimension(70, 30));
            readTimeoutPanel.add(
                    createLabel(c4Val,
                            "Read timeout (in seconds) is how long the application will wait to get a single item from the server"),
                    BorderLayout.CENTER);
            add(Box.createHorizontalStrut(5));
            add(readTimeoutPanel);

            createPermalinkPanel(c5Val);

            JPanel cbPanel = new JPanel(new BorderLayout());
            cbPanel.setBackground(DEFAULT_BACKGROUND_COLOR);
            cbPanel.setMaximumSize(new Dimension(40, 30));
            cbPanel.setMinimumSize(new Dimension(40, 20));
            cbPanel.setPreferredSize(new Dimension(40, 30));
            cbPanel.add(createLabel(c6Val), BorderLayout.CENTER);
            add(Box.createHorizontalStrut(8));
            add(cbPanel);
        }

        /**
         * Creates the label.
         *
         *
         * @param labelValue the label value
         * @return the {@link JLabel}
         */
        private JLabel createLabel(String labelValue)
        {
            return createLabel(labelValue, null);
        }

        /**
         * Creates the label.
         *
         * @param labelValue the label value
         * @param toolTip the tool tip
         * @return the {@link JLabel}
         */
        private JLabel createLabel(String labelValue, String toolTip)
        {
            JLabel label = new JLabel(labelValue);
            label.setHorizontalAlignment(JLabel.CENTER);
            if (toolTip != null)
            {
                label.setToolTipText(toolTip);
            }
            return label;
        }

        /**
         * Creates a permalink header panel.
         *
         * @param label The label for the header.
         */
        private void createPermalinkPanel(String label)
        {
            JPanel permalinkPanel = new JPanel(new BorderLayout());
            permalinkPanel.setBackground(DEFAULT_BACKGROUND_COLOR);
            permalinkPanel.setMaximumSize(new Dimension(140, 30));
            permalinkPanel.setMinimumSize(new Dimension(140, 20));
            permalinkPanel.setPreferredSize(new Dimension(140, 30));
            permalinkPanel.add(
                    createLabel(label,
                            "The URL to append to the server URL that points to the permalink service for the server."),
                    BorderLayout.CENTER);
            add(Box.createHorizontalStrut(5));
            add(permalinkPanel);
        }
    }

    /**
     * The Class ServerTimeoutPanel.
     */
    @SuppressWarnings("serial")
    private static class ServerTimeoutPanel extends JPanel
    {
        /** The Source. */
        private final OGCServerSource mySource;

        /** The Controller for my source. */
        private final ServerSourceController mySourceController;

        /** The Server name label. */
        private JLabel myServerNameLabel;

        /** The Read timeout textfield. */
        private JTextField myReadTimeoutTF;

        /** The Connect timeout textfield. */
        private JTextField myConnectTimeoutTF;

        /** The Activate timeout textfield. */
        private JTextField myActivateTimeoutTF;

        /**
         * The permalink url textfield.
         */
        private JTextField myPermalinkUrlTF;

        /** Checkbox used to set this source's timeouts to the defaults. */
        private JCheckBox myDefaultCheckBox;

        /** Flag that gets set if this panel lists the default timeouts. */
        private final boolean myIsDefaultPanel;

        /** The Error reporter. */
        private final ErrorReporter myErrorReporter;

        /** The system preferences registry. */
        private final PreferencesRegistry myPreferencesRegistry;

        /** The event manager. */
        private final EventManager myEventManager;

        /**
         * Instantiates a new server timeout panel.
         *
         * @param prefsRegistry The system preferences registry.
         * @param eventManager The event manager.
         * @param ctrl the controller for the specified source
         * @param source the source
         * @param errorReporter the error reporter
         * @param isDefaultPanel the is default panel
         */
        public ServerTimeoutPanel(PreferencesRegistry prefsRegistry, EventManager eventManager, ServerSourceController ctrl,
                OGCServerSource source, ErrorReporter errorReporter, boolean isDefaultPanel)
        {
            myErrorReporter = errorReporter;
            myPreferencesRegistry = prefsRegistry;
            myEventManager = eventManager;
            myIsDefaultPanel = isDefaultPanel;
            mySource = source;
            mySourceController = ctrl;
            setMaximumSize(new Dimension(5000, 40));
            setMinimumSize(new Dimension(400, 30));
            setPreferredSize(new Dimension(400, 30));
            setBackground(TRANSPARENT_COLOR);
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            add(getServerNamePanel());

            add(Box.createHorizontalStrut(5));

            JPanel activateTimeoutPanel = new JPanel(new BorderLayout());
            activateTimeoutPanel.setBackground(TRANSPARENT_COLOR);
            activateTimeoutPanel.setMaximumSize(new Dimension(70, 30));
            activateTimeoutPanel.setMinimumSize(new Dimension(70, 20));
            activateTimeoutPanel.setPreferredSize(new Dimension(70, 30));
            activateTimeoutPanel.add(getActivateTimeoutTextField(), BorderLayout.CENTER);
            add(activateTimeoutPanel);

            JPanel connectTimeoutPanel = new JPanel(new BorderLayout());
            connectTimeoutPanel.setBackground(TRANSPARENT_COLOR);
            connectTimeoutPanel.setMaximumSize(new Dimension(70, 30));
            connectTimeoutPanel.setMinimumSize(new Dimension(70, 20));
            connectTimeoutPanel.setPreferredSize(new Dimension(70, 30));
            connectTimeoutPanel.add(getConnectTimeoutTextField(), BorderLayout.CENTER);
            add(Box.createHorizontalStrut(5));
            add(connectTimeoutPanel);

            JPanel readTimeoutPanel = new JPanel(new BorderLayout());
            readTimeoutPanel.setBackground(TRANSPARENT_COLOR);
            readTimeoutPanel.setMaximumSize(new Dimension(70, 30));
            readTimeoutPanel.setMinimumSize(new Dimension(70, 20));
            readTimeoutPanel.setPreferredSize(new Dimension(70, 30));
            readTimeoutPanel.add(getReadTimeoutTextField(), BorderLayout.CENTER);
            add(Box.createHorizontalStrut(5));
            add(readTimeoutPanel);

            createPermalinkPanel(isDefaultPanel);

            JPanel cbPanel = new JPanel();
            cbPanel.setLayout(new BoxLayout(cbPanel, BoxLayout.X_AXIS));
            cbPanel.setBackground(TRANSPARENT_COLOR);
            cbPanel.setMaximumSize(new Dimension(40, 30));
            cbPanel.setMinimumSize(new Dimension(40, 20));
            cbPanel.setPreferredSize(new Dimension(40, 30));
            cbPanel.add(Box.createHorizontalGlue());
            cbPanel.add(getDefaultCheckBox(), BorderLayout.CENTER);
            cbPanel.add(Box.createHorizontalGlue());
            add(Box.createHorizontalStrut(5));
            add(cbPanel);
            updateValuesFromSource();
        }

        /**
         * Validate panel inputs.
         *
         * @return true, if succes
         */
        public boolean arePanelInputsValid()
        {
            boolean isValid = false;
            if (!myIsDefaultPanel && myDefaultCheckBox.isSelected())
            {
                isValid = true;
            }
            else
            {
                isValid = validateTimeoutInput(myActivateTimeoutTF, "Activate timeout", true)
                        && validateTimeoutInput(myConnectTimeoutTF, "Connect timeout", true)
                        && validateTimeoutInput(myReadTimeoutTF, "Read timeout", true);

//                int internalReadTimeout = getTimeoutValueMilliseconds(myReadTimeoutTF);
//                int internalConnectTimeout = getTimeoutValueMilliseconds(myConnectTimeoutTF);
//                int internalActivateTimeout = getTimeoutValueMilliseconds(myActivateTimeoutTF);

//                if (isValid)
//                {
//
//                    if (internalActivateTimeout < internalReadTimeout + internalConnectTimeout)
//                    {
//                        isValid = false;
//                        myActivateTimeoutTF.setBorder(BorderFactory.createLineBorder(Color.red));
//                        String errorMessage = "Activate timeout must be at least the sum of CONNECT and READ timeouts.";
//                        myErrorReporter.reportError(errorMessage);
//                        JOptionPane.showMessageDialog(getParent(), errorMessage, "Invalid Activate Timeout",
//                                JOptionPane.ERROR_MESSAGE);
//                    }
//                }
            }
            return isValid;
        }

        /**
         * Checks for changes from source.
         *
         * @return true, if successful
         */
        public boolean hasChangesFromSource()
        {
            boolean hasChanges = false;
            int intReadTO = getTimeoutValueMilliseconds(myReadTimeoutTF);
            int intConTO = getTimeoutValueMilliseconds(myConnectTimeoutTF);
            int intActTO = getTimeoutValueMilliseconds(myActivateTimeoutTF);
            String permalinkUrl = myPermalinkUrlTF.getText();
            if (myIsDefaultPanel)
            {
                int defaultReadTimeout = ServerConstants.getDefaultServerReadTimeoutFromPrefs(myPreferencesRegistry);
                int defaultConnectTimeout = ServerConstants.getDefaultServerConnectTimeoutFromPrefs(myPreferencesRegistry);
                int defaultActivateTimeout = ServerConstants.getDefaultServerActivateTimeoutFromPrefs(myPreferencesRegistry);
                String defaultPermalinkUrl = ServerConstants.DEFAULT_PERMALINK_URL;
                hasChanges = intReadTO != defaultReadTimeout || intConTO != defaultConnectTimeout
                        || intActTO != defaultActivateTimeout || !defaultPermalinkUrl.equals(permalinkUrl);
            }
            else
            {
                int srcReadTO = mySource.getReadTimeoutMillis();
                int srcConTO = mySource.getConnectTimeoutMillis();
                int srcActTO = mySource.getActivateTimeoutMillis();
                String srcPermalinkUrl = mySource.getPermalinkUrl();
                boolean setToDefault = sourceIsSetToDefault();
                hasChanges = myDefaultCheckBox.isSelected() != setToDefault || !setToDefault && (intReadTO != srcReadTO
                        || intConTO != srcConTO || intActTO != srcActTO || !srcPermalinkUrl.equals(permalinkUrl));
            }
            return hasChanges;
        }

        /**
         * Restore defaults.
         */
        public void restoreDefaults()
        {
            if (myIsDefaultPanel)
            {
                ServerConstants.setDefaultServerConnectTimeoutToPrefs(myPreferencesRegistry,
                        ServerConstants.DEFAULT_SERVER_CONNECT_TIMEOUT, this);
                ServerConstants.setDefaultServerReadTimeoutToPrefs(myPreferencesRegistry,
                        ServerConstants.DEFAULT_SERVER_READ_TIMEOUT, this);
                ServerConstants.setDefaultServerActivateTimeoutToPrefs(myPreferencesRegistry,
                        ServerConstants.DEFAULT_SERVER_ACTIVATE_TIMEOUT, this);
            }
            else
            {
                mySource.setActivateTimeoutMillis(-1);
                mySource.setReadTimeoutMillis(-1);
                mySource.setConnectTimeoutMillis(-1);
                int defaultReadTimeout = ServerConstants.getDefaultServerReadTimeoutFromPrefs(myPreferencesRegistry);
                int defaultConnectTimeout = ServerConstants.getDefaultServerConnectTimeoutFromPrefs(myPreferencesRegistry);
                myEventManager.publishEvent(new ServerTimeoutChangeEvent(mySource.getName(), mySource.getSessionUniqueId(),
                        defaultConnectTimeout, defaultReadTimeout, true, this));
                mySourceController.saveConfigState();
            }
            updateValuesFromSource();
        }

        /**
         * Save changes to source.
         */
        public void saveChangesToSource()
        {
            int internalReadTimeout = getTimeoutValueMilliseconds(myReadTimeoutTF);
            int internalConnectTimeout = getTimeoutValueMilliseconds(myConnectTimeoutTF);
            int internalActivateTimeout = getTimeoutValueMilliseconds(myActivateTimeoutTF);
            String permalinkUrl = myPermalinkUrlTF.getText();
            if (myIsDefaultPanel)
            {
                ServerConstants.setDefaultServerConnectTimeoutToPrefs(myPreferencesRegistry, internalConnectTimeout, this);
                ServerConstants.setDefaultServerReadTimeoutToPrefs(myPreferencesRegistry, internalReadTimeout, this);
                ServerConstants.setDefaultServerActivateTimeoutToPrefs(myPreferencesRegistry, internalActivateTimeout, this);
            }
            else
            {
                if (myDefaultCheckBox.isSelected())
                {
                    mySource.setActivateTimeoutMillis(-1);
                    mySource.setReadTimeoutMillis(-1);
                    mySource.setConnectTimeoutMillis(-1);
                    mySource.setPermalinkUrl(ServerConstants.DEFAULT_PERMALINK_URL);
                    int defaultReadTimeout = ServerConstants.getDefaultServerReadTimeoutFromPrefs(myPreferencesRegistry);
                    int defaultConnectTimeout = ServerConstants.getDefaultServerConnectTimeoutFromPrefs(myPreferencesRegistry);
                    myEventManager.publishEvent(new ServerTimeoutChangeEvent(mySource.getName(), mySource.getSessionUniqueId(),
                            defaultConnectTimeout, defaultReadTimeout, true, this));
                }
                else
                {
                    mySource.setActivateTimeoutMillis(internalActivateTimeout);
                    mySource.setReadTimeoutMillis(internalReadTimeout);
                    mySource.setConnectTimeoutMillis(internalConnectTimeout);
                    mySource.setPermalinkUrl(permalinkUrl);
                    myEventManager.publishEvent(new ServerTimeoutChangeEvent(mySource.getName(), mySource.getSessionUniqueId(),
                            internalConnectTimeout, internalReadTimeout, false, this));
                }
                mySourceController.saveConfigState();
            }
        }

        /**
         * Source is set to default.
         *
         *
         * true, if successful
         *
         * @return true, if successful
         */
        public boolean sourceIsSetToDefault()
        {
            return mySource.getReadTimeoutMillis() <= 0 || mySource.getConnectTimeoutMillis() <= 0
                    || mySource.getActivateTimeoutMillis() <= 0;
        }

        /**
         * Update values from source.
         */
        public final void updateValuesFromSource()
        {
            int defaultReadTimeoutMS = ServerConstants.getDefaultServerReadTimeoutFromPrefs(myPreferencesRegistry);
            int defaultConnectTimeoutMS = ServerConstants.getDefaultServerConnectTimeoutFromPrefs(myPreferencesRegistry);
            int defaultActivateTimeoutMS = ServerConstants.getDefaultServerActivateTimeoutFromPrefs(myPreferencesRegistry);
            String defaultPermalink = ServerConstants.DEFAULT_PERMALINK_URL;
            if (myIsDefaultPanel)
            {
                myReadTimeoutTF.setText(Integer.toString(toSeconds(defaultReadTimeoutMS)));
                myConnectTimeoutTF.setText(Integer.toString(toSeconds(defaultConnectTimeoutMS)));
                myActivateTimeoutTF.setText(Integer.toString(toSeconds(defaultActivateTimeoutMS)));
                myPermalinkUrlTF.setText(defaultPermalink);
            }
            else
            {
                int sourceReadTimeoutMS = mySource.getReadTimeoutMillis();
                int sourceConnectTimeoutMS = mySource.getConnectTimeoutMillis();
                int sourceActivateTimeoutMS = mySource.getActivateTimeoutMillis();

                if (sourceReadTimeoutMS > 0 || sourceConnectTimeoutMS > 0 || sourceActivateTimeoutMS > 0)
                {
                    myReadTimeoutTF.setEnabled(true);
                    myReadTimeoutTF.setText(Integer.toString(toSeconds(sourceReadTimeoutMS)));
                    myConnectTimeoutTF.setEnabled(true);
                    myConnectTimeoutTF.setText(Integer.toString(toSeconds(sourceConnectTimeoutMS)));
                    myActivateTimeoutTF.setEnabled(true);
                    myActivateTimeoutTF.setText(Integer.toString(toSeconds(sourceActivateTimeoutMS)));
                    myPermalinkUrlTF.setEnabled(true);
                    myPermalinkUrlTF.setText(mySource.getPermalinkUrl());
                    myDefaultCheckBox.setSelected(false);
                }
                else
                {
                    myReadTimeoutTF.setEnabled(false);
                    myReadTimeoutTF.setText(Integer.toString(toSeconds(defaultReadTimeoutMS)));
                    myConnectTimeoutTF.setEnabled(false);
                    myConnectTimeoutTF.setText(Integer.toString(toSeconds(defaultConnectTimeoutMS)));
                    myActivateTimeoutTF.setEnabled(false);
                    myActivateTimeoutTF.setText(Integer.toString(toSeconds(defaultActivateTimeoutMS)));
                    myPermalinkUrlTF.setEnabled(false);
                    myPermalinkUrlTF.setText(defaultPermalink);
                    myDefaultCheckBox.setSelected(true);
                }
            }
        }

        /**
         * Creates the permalink panel.
         *
         * @param isDefault True if this is the default panel, false otherwise.
         */
        private void createPermalinkPanel(boolean isDefault)
        {
            JTextField permalinkTF = getPermalinkUrlTextField();

            if (!isDefault)
            {
                JPanel permalinkPanel = new JPanel(new BorderLayout());
                permalinkPanel.setBackground(TRANSPARENT_COLOR);
                permalinkPanel.setMaximumSize(new Dimension(140, 30));
                permalinkPanel.setMinimumSize(new Dimension(140, 20));
                permalinkPanel.setPreferredSize(new Dimension(140, 30));
                permalinkPanel.add(permalinkTF, BorderLayout.CENTER);
                add(Box.createHorizontalStrut(5));
                add(permalinkPanel);
            }
        }

        /**
         * Gets the default server activate timeout text field.
         *
         * @return the default activate timeout text field
         */
        private JTextField getActivateTimeoutTextField()
        {
            if (myActivateTimeoutTF == null)
            {
                myActivateTimeoutTF = new JTextField();
                myActivateTimeoutTF.getDocument().addDocumentListener(new DocumentListenerAdapter()
                {
                    @Override
                    protected void updateAction(DocumentEvent e)
                    {
                        validateTimeoutInput(myActivateTimeoutTF, "Activate timeout", false);
                    }
                });
            }
            return myActivateTimeoutTF;
        }

        /**
         * Gets the default server connect timeout text field.
         *
         * @return the default connect timeout text field
         */
        private JTextField getConnectTimeoutTextField()
        {
            if (myConnectTimeoutTF == null)
            {
                myConnectTimeoutTF = new JTextField();
                myConnectTimeoutTF.getDocument().addDocumentListener(new DocumentListenerAdapter()
                {
                    @Override
                    protected void updateAction(DocumentEvent e)
                    {
                        validateTimeoutInput(myConnectTimeoutTF, "Connect timeout", false);
                    }
                });
            }
            return myConnectTimeoutTF;
        }

        /**
         * Gets the default check box.
         *
         * @return the default check box
         */
        private JCheckBox getDefaultCheckBox()
        {
            if (myDefaultCheckBox == null)
            {
                myDefaultCheckBox = new JCheckBox();
                if (myIsDefaultPanel)
                {
                    myDefaultCheckBox.setVisible(false);
                }
                else
                {
                    myDefaultCheckBox.addActionListener(new ActionListener()
                    {
                        @Override
                        public void actionPerformed(ActionEvent e)
                        {
                            int defaultReadTimeout = ServerConstants.getDefaultServerReadTimeoutFromPrefs(myPreferencesRegistry);
                            int defaultConnectTimeout = ServerConstants
                                    .getDefaultServerConnectTimeoutFromPrefs(myPreferencesRegistry);
                            int defaultActivateTimeout = ServerConstants
                                    .getDefaultServerActivateTimeoutFromPrefs(myPreferencesRegistry);
                            String defaultPermalinkUrl = ServerConstants.DEFAULT_PERMALINK_URL;
                            myActivateTimeoutTF.setText(Integer.toString(toSeconds(defaultActivateTimeout)));
                            myConnectTimeoutTF.setText(Integer.toString(toSeconds(defaultConnectTimeout)));
                            myReadTimeoutTF.setText(Integer.toString(toSeconds(defaultReadTimeout)));
                            myPermalinkUrlTF.setText(defaultPermalinkUrl);
                            myActivateTimeoutTF.setEnabled(!myDefaultCheckBox.isSelected());
                            myConnectTimeoutTF.setEnabled(!myDefaultCheckBox.isSelected());
                            myReadTimeoutTF.setEnabled(!myDefaultCheckBox.isSelected());
                            myPermalinkUrlTF.setEnabled(!myDefaultCheckBox.isSelected());
                        }
                    });
                }
            }
            return myDefaultCheckBox;
        }

        /**
         * Gets the server's permalink url text field.
         *
         * @return The permalink url text field.
         */
        private JTextField getPermalinkUrlTextField()
        {
            if (myPermalinkUrlTF == null)
            {
                myPermalinkUrlTF = new JTextField();
            }

            return myPermalinkUrlTF;
        }

        /**
         * Gets the read timeout text field.
         *
         * @return the read timeout text field
         */
        private JTextField getReadTimeoutTextField()
        {
            if (myReadTimeoutTF == null)
            {
                myReadTimeoutTF = new JTextField();
                myReadTimeoutTF.getDocument().addDocumentListener(new DocumentListenerAdapter()
                {
                    @Override
                    protected void updateAction(DocumentEvent e)
                    {
                        validateTimeoutInput(myReadTimeoutTF, "Read timeout", false);
                    }
                });
            }
            return myReadTimeoutTF;
        }

        /**
         * Gets the server name panel.
         *
         * @return the server name panel
         */
        private JPanel getServerNamePanel()
        {
            JPanel serverNamePanel = new JPanel(new BorderLayout());
            serverNamePanel.setBackground(TRANSPARENT_COLOR);
            serverNamePanel.setMaximumSize(new Dimension(200, 30));
            serverNamePanel.setMinimumSize(new Dimension(200, 20));
            serverNamePanel.setPreferredSize(new Dimension(200, 30));
            myServerNameLabel = new JLabel(myIsDefaultPanel ? "Default Timeouts (s)" : mySource.getName());
            if (myIsDefaultPanel)
            {
                myServerNameLabel.setHorizontalAlignment(JLabel.RIGHT);
            }
            serverNamePanel.add(myServerNameLabel, BorderLayout.CENTER);
            return serverNamePanel;
        }

        /**
         * Gets the timeout value milliseconds.
         *
         * @param timeOutTF the time out tf
         * @return the timeout value milliseconds
         */
        private int getTimeoutValueMilliseconds(JTextField timeOutTF)
        {
            int timeout = -1;
            try
            {
                int value = Integer.parseInt(timeOutTF.getText());
                if (value < 0)
                {
                    timeout = -1;
                }
                else
                {
                    timeout = value * 1000;
                }
            }
            catch (NumberFormatException e)
            {
                timeout = -1;
            }
            return timeout;
        }

        /**
         * Convert input milliseconds to seconds.
         *
         * @param milliseconds the
         *
         * @return the int
         */
        private int toSeconds(int milliseconds)
        {
            return milliseconds / Constants.MILLI_PER_UNIT;
        }

        /**
         * Validate timeout input.
         *
         * @param timeOutTF the timeout text field to validate
         * @param name the name
         * @param showErrorMessages the show error messages
         * @return true, if successful
         */
        private boolean validateTimeoutInput(JTextField timeOutTF, String name, boolean showErrorMessages)
        {
            myErrorReporter.clearErrors();
            String errorMessage = null;
            boolean valid = true;
            try
            {
                int value = Integer.parseInt(timeOutTF.getText());
                if (value < 1)
                {
                    valid = false;
                    errorMessage = name + " must be at least 1 second.";
                    if (showErrorMessages)
                    {
                        JOptionPane.showMessageDialog(getParent(), errorMessage, "Invalid " + name + " Value",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            catch (NumberFormatException e)
            {
                errorMessage = name + " must be an integer number of at least 1 second.";
                if (showErrorMessages)
                {
                    JOptionPane.showMessageDialog(getParent(), errorMessage, "Invalid " + name + " Value",
                            JOptionPane.ERROR_MESSAGE);
                }
                valid = false;
            }
            if (valid)
            {
                timeOutTF.setBorder(ourOriginalTFBorder);
            }
            else
            {
                myErrorReporter.reportError(errorMessage);
                timeOutTF.setBorder(BorderFactory.createLineBorder(Color.red));
            }
            return valid;
        }
    }
}
