package io.opensphere.controlpanels.about;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.quantify.Quantify;
import io.opensphere.core.util.Colors;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.swing.AbstractHUDPanel;
import io.opensphere.core.util.swing.ButtonPanel;
import io.opensphere.mantle.util.TextViewDialog;

/**
 * The Class AboutPanel.
 */
public class AboutPanel extends AbstractHUDPanel
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(AboutPanel.class);

    /** Serial. */
    private static final long serialVersionUID = 1L;

    /** The Toolbox. */
    private final transient Toolbox myToolbox;

    /** The UI Registry. */
    private final transient UIRegistry myUiRegistry;

    /** The Preferences Registry. */
    private final transient PreferencesRegistry myPreferencesRegistry;

    /** The About frame. */
    private final About myAboutFrame;

    /** The About utility functions. */
    private final AboutUtil myUtil;

    /** The System properties map. */
    private final Map<String, String> mySystemPropertiesMap;

    /** The Close button. */
    private JButton myCloseButton;

    /** The Export Logs button. */
    private JButton myExportButton;

    /** The More button. */
    private JButton myMoreButton;

    /** The file browser command. */
    private String myCommand;

    /** The OS working directory. */
    private String myWorkingDirectory;

    /** The Delete Vortex button. */
    private JButton myDeleteVortexButton;

    /**
     * Instantiates a new layer manager panel.
     *
     * @param toolbox the toolbox
     * @param frame the options
     */
    public AboutPanel(Toolbox toolbox, About frame)
    {
        super(toolbox.getPreferencesRegistry());

        myToolbox = toolbox;
        myPreferencesRegistry = toolbox.getPreferencesRegistry();
        myUiRegistry = toolbox.getUIRegistry();

        myAboutFrame = frame;
        mySystemPropertiesMap = New.map();

        setSize(getTopLevelPanelDim());
        setMinimumSize(getSize());
        setPreferredSize(getSize());
        setLayout(new BorderLayout());
        setBackground(getBackgroundColor());

        buildPropertyMap();

        myUtil = new AboutUtil(myToolbox, frame, mySystemPropertiesMap);

        initializeFileBrowser();
        initialize();
    }

    /**
     * Builds the property map.
     */
    private void buildPropertyMap()
    {
        mySystemPropertiesMap.clear();
        final Properties p = System.getProperties();
        final List<Object> keys = New.list(p.keySet());
        Collections.sort(keys, new Comparator<>()
        {
            @Override
            public int compare(Object o1, Object o2)
            {
                return o1.toString().compareTo(o2.toString());
            }
        });
        for (final Object key : keys)
        {
            final String keyVal = key.toString();
            if (!keyVal.startsWith("password") && !keyVal.endsWith("Warning") && !"line.separator".equals(keyVal))
            {
                final String value = StringUtilities.expandProperties(p.getProperty(key.toString()), p);
                mySystemPropertiesMap.put(key.toString(), value);
            }
        }
    }

    /**
     * Initializes the file browser application and working directory based on
     * the user's operating system.
     */
    private void initializeFileBrowser()
    {
        if (System.getProperty("os.name").contains("Windows"))
        {
            myWorkingDirectory = null;
            myCommand = "explorer";
        }
        else if (new File("/usr/bin/xdg-open").canExecute())
        {
            myWorkingDirectory = "/";
            myCommand = "/usr/bin/xdg-open";
        }
        else if (new File("/usr/bin/gnome-open").canExecute())
        {
            myWorkingDirectory = "/";
            myCommand = "/usr/bin/gnome-open";
        }
        else
        {
            myWorkingDirectory = null;
            myCommand = null;
        }
    }

    /**
     * Opens a given directory.
     *
     * @param file the directory
     */
    private void openFolder(File file)
    {
        if (myCommand == null)
        {
            return;
        }

        try
        {
            ProcessBuilder pb = new ProcessBuilder(myCommand, file.getCanonicalPath());
            if (myWorkingDirectory != null)
            {
                pb.directory(new File(myWorkingDirectory));
            }
            pb.start();
        }
        catch (final IOException e1)
        {
            LOGGER.error("Failed to open folder: " + e1, e1);
        }
    }

    /**
     * Creates the label panel.
     *
     * @param string the string
     * @param value The label text value.
     * @return the j panel
     */
    @SuppressFBWarnings("DMI_HARDCODED_ABSOLUTE_FILENAME")
    private JPanel createLabelPanel(String string, String value)
    {
        final JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.setBackground(Colors.TRANSPARENT_BLACK);

        final Dimension dim = new Dimension(400, 26);
        p.setMinimumSize(dim);
        p.setPreferredSize(dim);
        p.setMaximumSize(new Dimension(1000, 26));

        final JLabel lb = new JLabel(string);
        lb.setHorizontalAlignment(SwingConstants.RIGHT);
        final JPanel lbPnl = new JPanel(new BorderLayout());
        lbPnl.setBackground(Colors.TRANSPARENT_BLACK);
        final Dimension d = new Dimension(160, 20);
        lbPnl.setMinimumSize(d);
        lbPnl.setPreferredSize(d);
        lbPnl.setMaximumSize(new Dimension(1000, 20));
        lbPnl.add(lb, BorderLayout.CENTER);

        final JPanel valPnl = new JPanel(new BorderLayout());
        valPnl.setBackground(Colors.TRANSPARENT_BLACK);
        final Dimension d2 = new Dimension(200, 20);
        valPnl.setMinimumSize(d2);
        valPnl.setPreferredSize(d2);
        valPnl.setMaximumSize(new Dimension(1000, 20));

        final JTextField jtf = new JTextField();
        jtf.setBackground(Colors.TRANSPARENT_BLACK);
        jtf.setEditable(false);
        jtf.setText(value);
        valPnl.add(jtf, BorderLayout.CENTER);

        p.add(lbPnl, BorderLayout.WEST);
        p.add(valPnl, BorderLayout.CENTER);

        if (myCommand != null)
        {
            final File file = new File(value);
            if (file.isDirectory())
            {
                final JButton openBtn = new JButton("Open");
                openBtn.setMargin(ButtonPanel.INSETS_MEDIUM);
                openBtn.addActionListener(e ->
                {
                    Quantify.collectMetric("mist3d.help.about.open-location." + string);
                    openFolder(file);
                });
                p.add(openBtn, BorderLayout.EAST);
            }
        }

        return p;
    }

    /**
     * Gets the about panel.
     *
     * @return the about panel
     */
    private JPanel getAboutPanel()
    {
        final JLabel lb = new JLabel("About");
        lb.setHorizontalAlignment(SwingConstants.CENTER);
        lb.setHorizontalTextPosition(SwingConstants.CENTER);
        final JPanel lbPanel1 = new JPanel(new BorderLayout());
        lbPanel1.setBackground(Colors.TRANSPARENT_BLACK);
        lbPanel1.setMaximumSize(new Dimension(1000, 20));
        lbPanel1.add(lb, BorderLayout.CENTER);
        return lbPanel1;
    }

    /**
     * Gets the about panel.
     *
     * @return the about panel
     */
    private JPanel getAttributionPanel()
    {
        JPanel lbPanel1 = null;
        final String labelText = System.getProperty("opensphere.about");

        if (StringUtils.isNotEmpty(labelText))
        {
            final JLabel lb = new JLabel(labelText);
            lb.setHorizontalAlignment(SwingConstants.CENTER);
            lb.setHorizontalTextPosition(SwingConstants.CENTER);
            lbPanel1 = new JPanel(new BorderLayout());
            lbPanel1.setBackground(Colors.TRANSPARENT_BLACK);
            lbPanel1.setMaximumSize(new Dimension(1000, 20));
            lbPanel1.add(lb, BorderLayout.CENTER);
        }

        return lbPanel1;
    }

    /**
     * Gets the close button.
     *
     * @return the close button
     */
    private JButton getCloseButton()
    {
        if (myCloseButton == null)
        {
            myCloseButton = new JButton("Close");
            myCloseButton.setMargin(ButtonPanel.INSETS_MEDIUM);
            myCloseButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    myAboutFrame.setVisible(false);
                }
            });
        }
        return myCloseButton;
    }

    /**
     * Creates the export button. Exports all logs, preferences and db files.
     * <p>
     * After being clicked the button will initiate a Save-As dialog, then save
     * the resulting file as a ZIP archive.
     *
     * @return the export button
     */
    private JButton createExportButton()
    {
        myExportButton = new JButton("Export Logs");
        myExportButton.setMargin(ButtonPanel.INSETS_MEDIUM);
        myExportButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                File saveFile = myUtil.initSaveAs();
                if (saveFile == null)
                {
                    return;
                }

                myUtil.performZipAction(saveFile, false, evt ->
                {
                    if (evt.getActionCommand() == AboutUtil.SUCCESS)
                    {
                        File saveDir = saveFile.getParentFile();
                        openFolder(saveDir);
                    }
                });
            }
        });

        return myExportButton;
    }

    /**
     * Creates the delete vortex button. Deletes all files under the vortex runtime path.
     * After being clicked, will initiate a dialog asking the user if they are sure about
     * doing this. If so, the vortex files will be deleted and the application will restart.
     *
     * @return the delete vortex button
     */
    private JButton createDeleteVortexButton()
    {
        myDeleteVortexButton = new JButton("Delete Vortex");
        myDeleteVortexButton.setMargin(ButtonPanel.INSETS_MEDIUM);
        myDeleteVortexButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int result = JOptionPane.showConfirmDialog(myAboutFrame, "This action will delete all Vortex files.\n"
                        + "Data, security settings, servers, and other application state information will be removed, "
                        + "reverting this application to a clean state.\nThis application will then restart. Do you wish to proceed?",
                        "WARNING", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION)
                {
                    restart();
                }
            }

            private boolean deleteDirectory(File file)
            {
                if (file.isDirectory())
                {
                    File[] subfiles = file.listFiles();
                    if (subfiles != null)
                    {
                        for (File child : subfiles)
                        {
                            deleteDirectory(child);
                        }
                    }
                }
                return file.delete();
            }

            private void restart()
            {
                final String installPath = new File(System.getProperty("user.dir")).getParentFile().getAbsolutePath();
                String command;
                if (System.getProperty("os.name").contains("Windows"))
                {
                    command = "\"" + Paths.get(installPath, "launch.bat").normalize().toString() + "\"";
                }
                else
                {
                    command = Paths.get(installPath, "launch.sh").normalize().toString();
                }
                Runtime.getRuntime().addShutdownHook(new Thread()
                {
                    @Override
                    public void run()
                    {
                        deleteDirectory(new File(mySystemPropertiesMap.get("opensphere.path.runtime")));
                        try
                        {
                            Runtime.getRuntime().exec(command);
                        }
                        catch (IOException e)
                        {
                            LOGGER.error("Failed to execute new instance of application command.", e);
                        }
                    }
                });

                System.exit(0);
            }
        });
        return myDeleteVortexButton;
    }

    /**
     * Gets the locations panel.
     *
     * @return the locations panel
     */
    private JPanel getLocationsPanel()
    {
        final JLabel lb3 = new JLabel("Locations");
        lb3.setHorizontalAlignment(SwingConstants.CENTER);
        lb3.setHorizontalTextPosition(SwingConstants.CENTER);
        final JPanel lbPanel3 = new JPanel(new BorderLayout());
        lbPanel3.setBackground(Colors.TRANSPARENT_BLACK);
        lbPanel3.setMaximumSize(new Dimension(1000, 20));
        lbPanel3.add(lb3, BorderLayout.CENTER);
        return lbPanel3;
    }

    /**
     * Gets the OpenSphere panel.
     *
     * @return the OpenSphere panel
     */
    private JPanel getOpenSpherePanel()
    {
        final JLabel lb2 = new JLabel(mySystemPropertiesMap.get("opensphere.title"));
        lb2.setFont(lb2.getFont().deriveFont(Font.BOLD, lb2.getFont().getSize() + 2));
        lb2.setHorizontalAlignment(SwingConstants.CENTER);
        lb2.setHorizontalTextPosition(SwingConstants.CENTER);
        final JPanel lbPanel2 = new JPanel(new BorderLayout());
        lbPanel2.setBackground(Colors.TRANSPARENT_BLACK);
        lbPanel2.setMaximumSize(new Dimension(1000, 25));
        lbPanel2.add(lb2, BorderLayout.CENTER);
        return lbPanel2;
    }

    /**
     * Gets the more button.
     *
     * @return the more button
     */
    private JButton getMoreButton()
    {
        if (myMoreButton == null)
        {
            myMoreButton = new JButton("More");
            myMoreButton.setMargin(ButtonPanel.INSETS_MEDIUM);
            myMoreButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    printPropertiesToTextEditor();
                }
            });
        }
        return myMoreButton;
    }

    /**
     * Initialize.
     */
    private void initialize()
    {
        final JPanel toPanel = new JPanel();
        toPanel.setLayout(new BoxLayout(toPanel, BoxLayout.Y_AXIS));
        toPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        toPanel.add(Box.createVerticalStrut(5));
        toPanel.add(getAboutPanel());

        toPanel.add(Box.createVerticalStrut(10));
        toPanel.add(getOpenSpherePanel());

        final JPanel attributionPanel = getAttributionPanel();

        if (attributionPanel != null)
        {
            toPanel.add(Box.createVerticalStrut(5));
            toPanel.add(getAttributionPanel());
        }

        toPanel.add(Box.createVerticalStrut(20));
        toPanel.add(createLabelPanel("Version: ", mySystemPropertiesMap.get("opensphere.version")));
        toPanel.add(createLabelPanel("Deployment Name: ", mySystemPropertiesMap.get("opensphere.deployment.name")));
        toPanel.add(createLabelPanel("Java Version: ", mySystemPropertiesMap.get("java.version")));
        toPanel.add(createLabelPanel("OpenGL Renderer: ",
                myToolbox.getGeometryRegistry().getRenderingCapabilities().getRendererIdentifier()));

        toPanel.add(Box.createVerticalStrut(10));
        toPanel.add(getLocationsPanel());

        toPanel.add(createLabelPanel("User Home: ", mySystemPropertiesMap.get("user.home")));
        toPanel.add(createLabelPanel("User Directory: ", mySystemPropertiesMap.get("user.dir")));
        toPanel.add(createLabelPanel("Runtime Path: ", mySystemPropertiesMap.get("opensphere.path.runtime")));
        toPanel.add(createLabelPanel("DB Path: ", mySystemPropertiesMap.get("opensphere.db.path")));
        toPanel.add(createLabelPanel("Log Path: ", mySystemPropertiesMap.get("log.path")));

        toPanel.add(Box.createVerticalStrut(10));

        final JPanel rtPanel = new JPanel();
        rtPanel.setBackground(Colors.TRANSPARENT_BLACK);
        rtPanel.setLayout(new BoxLayout(rtPanel, BoxLayout.X_AXIS));
        rtPanel.add(Box.createHorizontalGlue());
        rtPanel.add(getMoreButton());
        rtPanel.add(Box.createHorizontalStrut(5));
        rtPanel.add(createExportButton());
        rtPanel.add(Box.createHorizontalStrut(5));
        rtPanel.add(createDeleteVortexButton());
        rtPanel.add(Box.createHorizontalStrut(5));
        rtPanel.add(getCloseButton());
        rtPanel.add(Box.createHorizontalGlue());

        final JPanel morePanel = new JPanel(new BorderLayout());
        morePanel.add(rtPanel, BorderLayout.CENTER);
        morePanel.setBackground(Colors.TRANSPARENT_BLACK);

        final JPanel b = new JPanel();
        b.setBackground(Colors.TRANSPARENT_BLACK);
        toPanel.add(Box.createVerticalGlue());
        toPanel.add(b);
        toPanel.add(Box.createVerticalGlue());

        toPanel.add(morePanel);

        add(toPanel, BorderLayout.CENTER);
    }

    /**
     * Prints the properties to text editor.
     */
    private void printPropertiesToTextEditor()
    {
        final String title = mySystemPropertiesMap.get("opensphere.title");
        final StringBuilder sb = new StringBuilder(title);
        sb.append(" System Properties\n\n");

        final List<String> paths = New.list();

        final List<String> keys = New.list(mySystemPropertiesMap.keySet());
        Collections.sort(keys);
        for (final String key : keys)
        {
            if (key.endsWith("path"))
            {
                paths.add(key);
            }
            else
            {
                sb.append(String.format("%-50s   %s%n", key, mySystemPropertiesMap.get(key)));
            }
        }

        if (!paths.isEmpty())
        {
            sb.append("\n\nPATHS:\n\n");
            for (final String key : paths)
            {
                sb.append(key).append("\n\n");
                sb.append(mySystemPropertiesMap.get(key));
                sb.append("\n\n");
            }
        }

        Supplier<? extends JFrame> mainFrameProvider = myUiRegistry.getMainFrameProvider();

        final TextViewDialog dvd = new TextViewDialog(mainFrameProvider.get(), title + " System Properties", sb.toString(), false,
                myPreferencesRegistry);
        dvd.setLocationRelativeTo(mainFrameProvider.get());
        dvd.setVisible(true);
    }
}
