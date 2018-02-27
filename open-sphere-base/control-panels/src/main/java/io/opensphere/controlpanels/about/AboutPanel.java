package io.opensphere.controlpanels.about;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.ProgressMonitor;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.h2.command.dml.Set;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.util.Colors;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.swing.AbstractHUDPanel;
import io.opensphere.core.util.swing.ButtonPanel;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.taskactivity.TaskActivity;
import io.opensphere.core.util.zip.Zip;
import io.opensphere.core.util.zip.ZipFileInputAdapter;
import io.opensphere.core.util.zip.ZipInputAdapter;
import io.opensphere.mantle.util.ProgressDialog;
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

    /** The About frame. */
    private final About myAboutFrame;

    /** The System properties map. */
    private final Map<String, String> mySystemPropertiesMap;
    
    /** The Toolbox. */
    private final transient Toolbox myToolbox;
    
    /** The Menu Bar Registry. */
    private MenuBarRegistry menuBarReg;

    /** The Close button. */
    private JButton myCloseButton;
    
    /** The Export Logs button. */
    private JButton myExportButton;

    /** The More button. */
    private JButton myMoreButton;
    
    /** The file browser command. */
    private String command;
    
    /** The OS working directory. */
    private String workingDirectory;


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
        menuBarReg = myToolbox.getUIRegistry().getMenuBarRegistry();
        myAboutFrame = frame;
        this.setSize(getTopLevelPanelDim());
        setMinimumSize(getSize());
        setPreferredSize(getSize());
        setLayout(new BorderLayout());
        setBackground(getBackgroundColor());
        mySystemPropertiesMap = New.map();
        // add(getTabbedPane(), BorderLayout.CENTER);

        buildPropertyMap();
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
        Collections.sort(keys, new Comparator<Object>()
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
     * Initializes the file browser application and working directory
     * based on the user's operating system.
     */
    private void initializeFileBrowser()
    {
        if (System.getProperty("os.name").contains("Windows"))
        {
            workingDirectory = null;
            command = "explorer";
        }
        else if (new File("/usr/bin/xdg-open").canExecute())
        {
            workingDirectory = "/";
            command = "/usr/bin/xdg-open";
        }
        else if (new File("/usr/bin/gnome-open").canExecute())
        {
            workingDirectory = "/";
            command = "/usr/bin/gnome-open";
        }
        else
        {
            workingDirectory = null;
            command = null;
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
        lb.setHorizontalAlignment(JLabel.RIGHT);
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

        if (command != null)
        {
            final File file = new File(value);
            if (file.isDirectory())
            {
                final JButton openBtn = new JButton("Open");
                openBtn.setMargin(ButtonPanel.INSETS_MEDIUM);
                openBtn.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        openFolder(file);
                    }
                });
                p.add(openBtn, BorderLayout.EAST);
            }
        }

        return p;
    }
    
    /**
     * Opens a given directory.
     *
     * @param file the directory
     */
    private void openFolder(File file)
    {
        try
        {
            ProcessBuilder pb = new ProcessBuilder(command, file.getCanonicalPath());
            if (workingDirectory != null)
            {
                pb.directory(new File(workingDirectory));
            }
            pb.start();
        }
        catch (final IOException e1)
        {
            LOGGER.error("Failed to open folder: " + e1, e1);
        }
    }

    /**
     * Gets the about panel.
     *
     * @return the about panel
     */
    private JPanel getAboutPanel()
    {
        final JLabel lb = new JLabel("About");
        lb.setHorizontalAlignment(JLabel.CENTER);
        lb.setHorizontalTextPosition(JLabel.CENTER);
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
            lb.setHorizontalAlignment(JLabel.CENTER);
            lb.setHorizontalTextPosition(JLabel.CENTER);
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
     * Creates the export button.
     *
     * @return the export button
     */
    private JButton createExportButton()
    {
        myExportButton = new JButton("Export Logs");
        myExportButton.setMargin(ButtonPanel.INSETS_MEDIUM);
        myExportButton.addActionListener(new ActionListener()
        {
            final String dbPath = mySystemPropertiesMap.get("opensphere.db.path");
            final String logPath = mySystemPropertiesMap.get("log.path");
            final String runPath = mySystemPropertiesMap.get("opensphere.path.runtime");
            
            @Override
            public void actionPerformed(ActionEvent e)
            {                
                final File zipPath = new File(runPath);
                final File zipFile = Paths.get(runPath, "debug_info.zip").toFile();
                
                final ArrayList<File> files = new ArrayList<>();
                Collections.addAll(files, new File(dbPath).listFiles());
                Collections.addAll(files, new File(logPath).listFiles());
                Collections.addAll(files, Paths.get(runPath, "prefs").toFile().listFiles());
                
                final ArrayList<ZipInputAdapter> inputAdapters = new ArrayList<>();
                
                long totalBytes = 0L;
                
                for (File file : files)
                {
                    totalBytes += file.getTotalSpace();

                    URI parentFolder = URI.create(file.getParent());
                    URI relativeLocation = zipPath.toURI().relativize(parentFolder);

                    inputAdapters.add(new ZipFileInputAdapter(relativeLocation.toString(), file, ZipEntry.DEFLATED));
                    
                }
                
                final ProgressMonitor progressMon = new ProgressMonitor(myAboutFrame,
                        "Writing Debug Export File: " + zipFile.getName(), "Writing", 0, (int)totalBytes);
                
                Thread t = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        TaskActivity ta = new TaskActivity();
                        ta.setLabelValue("Exporting log files...");
                        ta.setActive(true);
                        menuBarReg.addTaskActivity(ta);
                        progressMon.setMillisToPopup(0);
                        
                        try
                        {                        
                            Zip.zipfiles(zipFile, inputAdapters, progressMon, true);
                        }
                        catch (IOException e)
                        {
                            if (!zipFile.delete() && LOGGER.isTraceEnabled())
                            {
                                LOGGER.trace("Failed to delete file: " + zipFile.getAbsolutePath(), e);
                            }
                            JOptionPane.showMessageDialog(myAboutFrame,
                                    "Error encountered while saving export file", "File Save Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                        
                        ta.setActive(false);
                        progressMon.close();
                        openFolder(new File(runPath));
                    }
                });
                t.start();
            }
        });
        
        return myExportButton;
    }

    /**
     * Gets the locations panel.
     *
     * @return the locations panel
     */
    private JPanel getLocationsPanel()
    {
        final JLabel lb3 = new JLabel("Locations");
        lb3.setHorizontalAlignment(JLabel.CENTER);
        lb3.setHorizontalTextPosition(JLabel.CENTER);
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
        lb2.setHorizontalAlignment(JLabel.CENTER);
        lb2.setHorizontalTextPosition(JLabel.CENTER);
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

        final TextViewDialog dvd = new TextViewDialog(myToolbox.getUIRegistry().getMainFrameProvider().get(),
                title + " System Properties", sb.toString(), false, myToolbox.getPreferencesRegistry());
        dvd.setLocationRelativeTo(myToolbox.getUIRegistry().getMainFrameProvider().get());
        dvd.setVisible(true);
    }
}
