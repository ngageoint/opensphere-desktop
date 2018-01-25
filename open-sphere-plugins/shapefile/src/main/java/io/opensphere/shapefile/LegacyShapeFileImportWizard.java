package io.opensphere.shapefile;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXTable;

import io.opensphere.core.Toolbox;
import io.opensphere.core.common.configuration.date.DateFormat;
import io.opensphere.core.common.configuration.date.DateFormatsConfig;
import io.opensphere.core.common.shapefile.v2.ESRIShapefile;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.util.DateTimeUtilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.javafx.WebDialog;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.datasources.IDataSourceCreator;
import io.opensphere.mantle.util.MantleConstants;
import io.opensphere.shapefile.LegacyImportWizardUtil.ColumnType;
import io.opensphere.shapefile.config.v1.ShapeFileSource;

/** The Class ShapeFileImportWizard. */
@SuppressWarnings({ "unchecked", "rawtypes", "PMD.GodClass" })
class LegacyShapeFileImportWizard implements ActionListener
{
    /** Tool tip text. */
    private static final String APPLY_ENTERED_CUSTOM_FORMAT = "Apply the entered custom format";

    /** The Constant APPLY_STRING. */
    private static final String APPLY_STRING = "Apply";

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(LegacyShapeFileImportWizard.class);

    /** The Constant PREVIEW_STRING. */
    private static final String PREVIEW_STRING = "Preview: ";

    /** Error text. */
    private static final String SPECIFIED_FORMAT_UNABLE_TO_PROPERLY_PARSE_SAMPLE_FROM_FILE = "Specified Format Unable to Properly Parse Sample From File.";

    /** The caller. */
    private final IDataSourceCreator myCaller;

    /** The cancel bt. */
    private final JButton myCancelBT;

    /** The column names lb. */
    private final JLabel myColumnNamesLB;

    /** The column types. */
    private final List<ColumnType> myColumnTypes = New.list();

    /** The current wizard panel. */
    private JPanel myCurrentWizardPanel;

    /** The data table. */
    private JXTable myDataTable;

    /** The date formats config. */
    private final DateFormatsConfig myDateFormatsConfig;

    /** The existing name set. */
    private final Set<String> myExistingNameSet;

    /** The file analysis line lb. */
    private final JLabel myFileAnalysisLineLB;

    /** The import config. */
    private final ShapeFileSource myImportConfig;

    /** The intro line lb. */
    private final JLabel myIntroLineLB;

    /** The intro panel. */
    private final JPanel myIntroPanel;

    /** The main panel. */
    private final JPanel myMainPanel;

    /** The next bt. */
    private final JButton myNextBT;

    /** The owner. */
    private final Container myOwner;

    /** The preferences. */
    private final Preferences myPreferences;

    /** The prev bt. */
    private final JButton myPrevBT;

    /** The properties lb. */
    private final JLabel myPropertiesLB;

    /** The a sdf browser. */
    private WebDialog myWebDialog;

    /** The state. */
    private ImportState myState = ImportState.INTRODUCTION;

    /** The time format lb. */
    private final JLabel myTimeFormatLB;

    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The type resolution lb. */
    private final JLabel myTypeResolutionLB;

    /** The user date formats config. */
    private final DateFormatsConfig myUserDateFormatsConfig;

    /** The uses time. */
    private boolean myUsesTime = true;

    /** The values set. */
    private final List<List<String>> myValuesSet = New.linkedList();

    /**
     * Instantiates a new shape file import wizard.
     *
     * @param owner the owner
     * @param tb the toolbox
     * @param source the source
     * @param nameSet the name set
     * @param caller the caller
     */
    public LegacyShapeFileImportWizard(Container owner, Toolbox tb, ShapeFileSource source, Set<String> nameSet,
            IDataSourceCreator caller)
    {
        if (source == null || caller == null)
        {
            throw new IllegalArgumentException("source and caller can't be null");
        }

        myOwner = owner;
        myToolbox = tb;
        myCaller = caller;
        myExistingNameSet = nameSet;
        myImportConfig = source;

        InputStream is = LegacyShapeFileImportWizard.class.getClassLoader()
                .getResourceAsStream(MantleConstants.getDefaultDateFormatConfigFile().getName());
        myDateFormatsConfig = DateFormatsConfig.loadPatternConfig(is);

        myPreferences = tb.getPreferencesRegistry().getPreferences(MantleConstants.USER_DATE_FORMAT_CONFIG_FILE_TOPIC);
        myUserDateFormatsConfig = myPreferences.getJAXBObject(DateFormatsConfig.class,
                MantleConstants.USER_DATE_FORMAT_CONFIG_FILE_KEY, null);

        int width = 900;
        int height = 600;

        myMainPanel = new JPanel(new BorderLayout());
        myMainPanel.setMinimumSize(new Dimension(width, height));
        myMainPanel.setPreferredSize(new Dimension(width, height));

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3));
        myCancelBT = new JButton("Cancel");
        myCancelBT.addActionListener(this);
        myPrevBT = new JButton("Previous");
        myPrevBT.addActionListener(this);
        myPrevBT.setVisible(false);
        myNextBT = new JButton("Next");
        myNextBT.addActionListener(this);

        JPanel b1 = new JPanel(new BorderLayout());
        b1.setBorder(BorderFactory.createEmptyBorder(3, 20, 3, 20));
        b1.add(myCancelBT, BorderLayout.CENTER);

        JPanel b3 = new JPanel(new BorderLayout());
        b3.setBorder(BorderFactory.createEmptyBorder(3, 50, 3, 5));
        b3.add(myPrevBT, BorderLayout.CENTER);

        JPanel b2 = new JPanel(new BorderLayout());
        b2.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 50));
        b2.add(myNextBT, BorderLayout.CENTER);

        buttonPanel.add(b1);
        buttonPanel.add(b3);
        buttonPanel.add(b2);

        JPanel stepPanel = new JPanel();
        stepPanel.setLayout(new BoxLayout(stepPanel, BoxLayout.Y_AXIS));

        stepPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(10, 10, 10, 5)));
        stepPanel.setPreferredSize(new Dimension(170, 50));
        stepPanel.setMinimumSize(new Dimension(5, 50));

        JLabel lb = new JLabel("IMPORT STEPS");
        Font itemFont = lb.getFont().deriveFont(Font.BOLD, lb.getFont().getSize() + 2);
        lb.setFont(lb.getFont().deriveFont(Font.BOLD, lb.getFont().getSize() + 5));
        stepPanel.add(lb);

        stepPanel.add(LegacyImportWizardUtil.createSpacer(5, null));
        stepPanel.add(LegacyImportWizardUtil.createSpacer(5, lb.getForeground()));
        int spacerHeight = 20;
        stepPanel.add(LegacyImportWizardUtil.createSpacer(spacerHeight, null));

        myIntroLineLB = new JLabel("Introduction <");
        myIntroLineLB.setEnabled(true);
        myIntroLineLB.setFont(itemFont);
        stepPanel.add(myIntroLineLB);
        stepPanel.add(LegacyImportWizardUtil.createSpacer(spacerHeight, null));

        myFileAnalysisLineLB = new JLabel("File Analysis");
        myFileAnalysisLineLB.setEnabled(true);
        myFileAnalysisLineLB.setFont(itemFont);
        stepPanel.add(myFileAnalysisLineLB);
        stepPanel.add(LegacyImportWizardUtil.createSpacer(spacerHeight, null));

        myColumnNamesLB = new JLabel("Column Names");
        myColumnNamesLB.setEnabled(true);
        myColumnNamesLB.setFont(itemFont);
        stepPanel.add(myColumnNamesLB);
        stepPanel.add(LegacyImportWizardUtil.createSpacer(spacerHeight, null));

        myTypeResolutionLB = new JLabel("Type Resolution");
        myTypeResolutionLB.setEnabled(true);
        myTypeResolutionLB.setFont(itemFont);
        stepPanel.add(myTypeResolutionLB);
        stepPanel.add(LegacyImportWizardUtil.createSpacer(spacerHeight, null));

        myTimeFormatLB = new JLabel("Time Format");
        myTimeFormatLB.setEnabled(true);
        myTimeFormatLB.setFont(itemFont);
        stepPanel.add(myTimeFormatLB);
        stepPanel.add(LegacyImportWizardUtil.createSpacer(spacerHeight, null));

        myPropertiesLB = new JLabel("File Properties");
        myPropertiesLB.setEnabled(true);
        myPropertiesLB.setFont(itemFont);
        stepPanel.add(myPropertiesLB);
        stepPanel.add(LegacyImportWizardUtil.createSpacer(spacerHeight, null));

        myMainPanel.add(stepPanel, BorderLayout.WEST);
        myMainPanel.add(buttonPanel, BorderLayout.SOUTH);

        myIntroPanel = LegacyImportWizardUtil.createInfoPanel(itemFont,
                "Introduction\n\nThis ESRI Shape File Import Wizard will attempt to"
                        + " assist\nyou in importing your shape file.\n\nWhen you are ready please click \"Next\" below.");

        switchToWizardPanel(myIntroPanel);

        if (myOwner instanceof JPanel)
        {
            EventQueueUtilities.runOnEDT(new Runnable()
            {
                @Override
                public void run()
                {
                    myOwner.removeAll();
                    myOwner.add(myMainPanel, BorderLayout.CENTER);
                    ((JPanel)myOwner).revalidate();
                    myOwner.repaint();
                }
            });
        }
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == myCancelBT)
        {
            if (myCaller != null)
            {
                new Thread(() -> myCaller.sourceCreated(false, null), "ShapeFileImportWizard:WizardComplete").start();
            }
        }
        else if (e.getSource() == myNextBT)
        {
            doNextAction();
        }
        else if (e.getSource() == myPrevBT)
        {
            doPrevAction();
        }
    }

    /**
     * Change state.
     *
     * @param state the new state
     */
    public void changeState(ImportState state)
    {
        myState = state;
        changeState();
        myPrevBT.setVisible(false);
    }

    /** Change state. */
    private void changeState()
    {
        switch (myState)
        {
            case INTRODUCTION:
                LegacyImportWizardUtil.startLabel(myIntroLineLB);
                switchToWizardPanel(myIntroPanel);
                myNextBT.setVisible(true);
                myPrevBT.setVisible(false);
                break;
            case ANALYSIS:
                LegacyImportWizardUtil.completeLabel(myIntroLineLB);
                LegacyImportWizardUtil.startLabel(myFileAnalysisLineLB);
                AnalyzeFilePanel analyzeFilePanel = new AnalyzeFilePanel();
                switchToWizardPanel(analyzeFilePanel);
                ESRIShapefile shapefile = ShapeFileReadUtilities.readOnWorker(
                        myImportConfig.getShapeFileAbsolutePath(),
                        myToolbox.getServerProviderRegistry().getProvider(HttpServer.class));
                analyzeFilePanel.analyzeFile(shapefile);
                myNextBT.setVisible(true);
                myPrevBT.setVisible(true);
                break;
            case COLUMN_NAMES:
                LegacyImportWizardUtil.completeLabel(myFileAnalysisLineLB);
                LegacyImportWizardUtil.startLabel(myColumnNamesLB);
                switchToWizardPanel(new ColumnNameResolver(myImportConfig, myValuesSet));
                myPrevBT.setVisible(true);
                myNextBT.setVisible(true);
                break;
            case TYPE_RESOLUTION:
                LegacyImportWizardUtil.completeLabel(myColumnNamesLB);
                LegacyImportWizardUtil.startLabel(myTypeResolutionLB);
                myPrevBT.setVisible(true);
                myNextBT.setVisible(false);
                switchToWizardPanel(new ResolveTypesPanel());

                break;
            case TIME_FORMAT:
                LegacyImportWizardUtil.completeLabel(myTypeResolutionLB);
                LegacyImportWizardUtil.startLabel(myTimeFormatLB);
                myPrevBT.setVisible(true);
                myNextBT.setVisible(false);
                switchToWizardPanel(new TimeFormatPanel());
                break;
            case PROPERTIES:
                LegacyImportWizardUtil.completeLabel(myUsesTime ? myTimeFormatLB : myTypeResolutionLB);
                LegacyImportWizardUtil.startLabel(myPropertiesLB);
                myPrevBT.setVisible(true);
                myNextBT.setText("Done");
                myNextBT.setVisible(false);
                switchToWizardPanel(new PropertiesPanel(myImportConfig, myExistingNameSet, myNextBT));
                break;
            case IMPORT:
                if (myCaller != null)
                {
                    new Thread(() -> myCaller.sourceCreated(true, myImportConfig), "ShapeFileImportWizard:WizardComplete")
                            .start();
                }
                break;
            case ERROR:
                if (myCaller != null)
                {
                    new Thread(() -> myCaller.sourceCreated(false, null), "ShapeFileImportWizard:WizardComplete").start();
                }
                break;
            default:
        }
    }

    /** Do the action for the next button. */
    private void doNextAction()
    {
        switch (myState)
        {
            case INTRODUCTION:
                myState = ImportState.ANALYSIS;
                changeState();
                break;
            case ANALYSIS:
                myState = ImportState.COLUMN_NAMES;
                changeState();
                break;
            case COLUMN_NAMES:
                myState = ImportState.TYPE_RESOLUTION;
                changeState();
                break;
            case TYPE_RESOLUTION:
                if (myUsesTime)
                {
                    myState = ImportState.TIME_FORMAT;
                }
                else
                {
                    LegacyImportWizardUtil.startLabel(myTimeFormatLB);
                    LegacyImportWizardUtil.completeLabel(myTimeFormatLB);
                    myState = ImportState.PROPERTIES;
                }
                changeState();
                break;
            case TIME_FORMAT:
                myState = ImportState.PROPERTIES;
                changeState();
                break;
            case PROPERTIES:
                myState = ImportState.IMPORT;
                changeState();
                break;
            case ERROR:
            default:
                break;
        }
    }

    /** Do the action for the previous button. */
    private void doPrevAction()
    {
        switch (myState)
        {
            case ANALYSIS:
            case COLUMN_NAMES:
                LegacyImportWizardUtil.restoreLabel(myColumnNamesLB);
                LegacyImportWizardUtil.restoreLabel(myFileAnalysisLineLB);
                LegacyImportWizardUtil.restoreLabel(myIntroLineLB);
                myState = ImportState.INTRODUCTION;
                changeState();
                break;
            case TYPE_RESOLUTION:
                LegacyImportWizardUtil.restoreLabel(myColumnNamesLB);
                LegacyImportWizardUtil.restoreLabel(myTypeResolutionLB);
                myState = ImportState.COLUMN_NAMES;
                changeState();
                break;
            case TIME_FORMAT:
                LegacyImportWizardUtil.restoreLabel(myTypeResolutionLB);
                LegacyImportWizardUtil.restoreLabel(myTimeFormatLB);
                myState = ImportState.TYPE_RESOLUTION;
                changeState();
                break;
            case PROPERTIES:
                if (myUsesTime)
                {
                    LegacyImportWizardUtil.restoreLabel(myTimeFormatLB);
                    myState = ImportState.TIME_FORMAT;
                }
                else
                {
                    LegacyImportWizardUtil.restoreLabel(myTypeResolutionLB);
                    LegacyImportWizardUtil.restoreLabel(myTimeFormatLB);
                    myState = ImportState.TYPE_RESOLUTION;
                }
                LegacyImportWizardUtil.restoreLabel(myPropertiesLB);
                changeState();
                break;
            case IMPORT:

                break;
            default:
        }
    }

    /** Show sdf browser. */
    private void showSDFBrowser()
    {
        if (myWebDialog == null)
        {
            myWebDialog = new WebDialog(SwingUtilities.windowForComponent(myOwner));
            myWebDialog.setTitle("Date Format Help");
            myWebDialog.load(MantleConstants.SDF_HELP_FILE_URL.toExternalForm());
        }
        myWebDialog.setVisible(true);
        myWebDialog.toFront();
    }

    /**
     * Helper function to swap the current wizard panel.
     *
     * @param next the next
     */
    private void switchToWizardPanel(JPanel next)
    {
        if (myCurrentWizardPanel != null)
        {
            myMainPanel.remove(myCurrentWizardPanel);
        }
        myCurrentWizardPanel = next;
        myMainPanel.add(next, BorderLayout.CENTER);
        myMainPanel.revalidate();
        myMainPanel.repaint();
    }

    /** The Class AnalyzeFilePanel. */
    private class AnalyzeFilePanel extends JPanel
    {
        /** serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The info label. */
        private final JLabel myInfoLabel;

        /** The progress bar. */
        private final JProgressBar myProgressBar;

        /** Instantiates a new analyze file panel. */
        public AnalyzeFilePanel()
        {
            super(new BorderLayout());

            JLabel lb = new JLabel("Analyzing Import File");
            Font itemFont = lb.getFont().deriveFont(Font.BOLD, lb.getFont().getSize() + 3);
            lb.setFont(itemFont);
            add(lb, BorderLayout.NORTH);
            setBorder(BorderFactory.createEmptyBorder(150, 100, 10, 100));

            myProgressBar = new JProgressBar(0, 100);
            myProgressBar.setIndeterminate(true);
            myProgressBar.setMaximumSize(new Dimension(5000, 30));
            JPanel tPanel = new JPanel(new BorderLayout());
            myInfoLabel = new JLabel("Reading File...");
            tPanel.setBorder(BorderFactory.createEmptyBorder(50, 0, 0, 0));
            tPanel.add(myProgressBar, BorderLayout.NORTH);

            JPanel bPanel = new JPanel(new BorderLayout());
            bPanel.add(myInfoLabel, BorderLayout.NORTH);

            tPanel.add(bPanel, BorderLayout.CENTER);
            add(tPanel, BorderLayout.CENTER);
        }

        /**
         * Analyze file.
         *
         * @param aFile the a file
         */
        private void analyzeFile(ESRIShapefile aFile)
        {
            myColumnTypes.clear();
            myImportConfig.getColumnNames().clear();
            myValuesSet.clear();

            List<String> columnNames = ShapeFileReadUtilities.getHeader(aFile);

            if (!CollectionUtilities.hasContent(columnNames))
            {
                LOGGER.error("Shapefile header is empty.");
            }

            myValuesSet.addAll(ShapeFileReadUtilities.getDataSample(aFile, 200));

            myImportConfig.getColumnNames().clear();
            myImportConfig.getColumnNames().addAll(columnNames);

            int numPreviewLines = myValuesSet.size();
            boolean hadError = numPreviewLines == 0;
            int lineIndex = -1;
            if (numPreviewLines == 0)
            {
                JOptionPane.showMessageDialog(myMainPanel,
                        "Error loading: " + aFile.getFilePath() + "\n\nShapefile contains no data", "Shapefile Loading Error",
                        JOptionPane.ERROR_MESSAGE);
            }
            if (hadError)
            {
                myState = ImportState.ERROR;
                changeState();
            }
            else
            {
                myInfoLabel.setText("Read " + lineIndex + " Lines From File. Analyzing...");
                if (myImportConfig.getColumnNames().size() > 0)
                {
                    for (int i = 0; i < myImportConfig.getColumnNames().size(); i++)
                    {
                        myColumnTypes.add(ColumnType.OTHER);
                    }

                    myImportConfig.setDateColumn(-1);
                    myImportConfig.setTimeColumn(-1);
                    myImportConfig.setLobColumn(-1);

                    LegacyImportWizardUtil.determineColumns(myImportConfig);

                    /* Now go through again and try to guess on any columns that
                     * weren't determined more liberally */
                    LegacyImportWizardUtil.determineColumnsLiberal(myImportConfig);

                    /* Now go through again and try to guess on any columns that
                     * weren't determined even more liberally. */
                    LegacyImportWizardUtil.graspAtStraws(myImportConfig);

                    LegacyImportWizardUtil.setupColumnTypes(myImportConfig, myColumnTypes);
                }

                myProgressBar.setValue(100);
                myProgressBar.setIndeterminate(false);
                myInfoLabel.setText("Analysis Complete");
            }
        }
    }

    /** Panel to resolve the column types. */
    private class ResolveTypesPanel extends JPanel implements ActionListener
    {
        /** serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The current extras panel. */
        private JPanel myCurrentExtrasPanel;

        /** The date and time rb. */
        private JRadioButton myDateAndTimeRB;

        /** The date db. */
        private final JComboBox myDateDB;

        /** The date time panel. */
        private JPanel myDateTimePanel;

        /** The ellipse column panel. */
        private JPanel myEllipseColumnPanel;

        /** The ellipse rb. */
        private final JRadioButton myEllipseRB;

        /** The error msg label. */
        private final JLabel myErrorMsgLabel;

        /** The extras panel. */
        private final JPanel myExtrasPanel;

        /** The lob combo box. */
        private final JComboBox myLobComboBox;

        /** The lob column panel. */
        private final JPanel myLobColumnPanel;

        /** The lob rb. */
        private final JRadioButton myLobRB;

        /** The no extras panel. */
        private final JPanel myNoExtrasPanel;

        /** The no extras rb. */
        private final JRadioButton myNoExtrasRB;

        /** The no time rb. */
        private JRadioButton myNoTimeRB;

        /** The orient combo box. */
        private JComboBox myOrientComboBox;

        /** The smaj combo box. */
        private JComboBox mySmajComboBox;

        /** The smin combo box. */
        private JComboBox mySminComboBox;

        /** The time db. */
        private final JComboBox myTimeDB;

        /** The time panel. */
        private JPanel myTimePanel;

        /** The timestamp combo box. */
        private final JComboBox myTimestampComboBox;

        /** The timestamp panel. */
        private JPanel myTimestampPanel;

        /** The timestamp rb. */
        private JRadioButton myTimestampRB;

        /** Instantiates a new resolve types panel. */
        public ResolveTypesPanel()
        {
            super(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            myDataTable = new JXTable();
            myDataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            myDataTable.setSortable(false);

            Object[] colNames = myImportConfig.getColumnNames().toArray();
            myTimestampComboBox = new JComboBox(colNames);
            myDateDB = new JComboBox(colNames);
            myTimeDB = new JComboBox(colNames);

            JScrollPane jsp = new JScrollPane(myDataTable);
            jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
            JPanel tablePanel = new JPanel(new BorderLayout());
            tablePanel.add(jsp, BorderLayout.CENTER);
            tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            tablePanel.add(new JLabel("Data Preview ( not all data is shown )"), BorderLayout.NORTH);

            JPanel workPanel = new JPanel(new BorderLayout());

            JTextArea jta = new JTextArea();
            jta.setEditable(false);
            jta.setBackground(workPanel.getBackground());
            Font itemFont = jta.getFont().deriveFont(Font.BOLD);
            jta.setFont(itemFont);
            jta.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
            jta.setText("Before we can import we must determine a columns for TIMESTAMP ( or TIME and DATE )\n"
                    + " and Line of Bearing (LOB) from the table.\n\n"
                    + "A TIMESTAMP is a single column that contains both date and time together.\n\n"
                    + "        Please verify that the following selections are correct:");
            workPanel.add(jta, BorderLayout.NORTH);

            setupTimeComponents();

            myExtrasPanel = new JPanel(new BorderLayout());
            myNoExtrasRB = new JRadioButton("No Extras");
            myLobRB = new JRadioButton("LOB");
            myEllipseRB = new JRadioButton("Ellipse");
            myLobComboBox = new JComboBox(colNames);
            ButtonGroup bg3 = new ButtonGroup();
            bg3.add(myNoExtrasRB);
            bg3.add(myLobRB);
            bg3.add(myEllipseRB);

            myLobColumnPanel = new JPanel(new BorderLayout());
            myLobColumnPanel.add(myLobComboBox, BorderLayout.CENTER);

            setupEllipseComponents(colNames);

            JPanel extrasTypePanel = new JPanel(new GridLayout(3, 1));
            extrasTypePanel.add(myNoExtrasRB);
            extrasTypePanel.add(myLobRB);
            extrasTypePanel.add(myEllipseRB);
            extrasTypePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

            myExtrasPanel.add(extrasTypePanel, BorderLayout.WEST);

            myNoExtrasPanel = new JPanel();

            // Pre-Setup the columns for found lob/ellipse types
            if (myColumnTypes.contains(ColumnType.LOB))
            {
                myLobComboBox.setSelectedIndex(myColumnTypes.indexOf(ColumnType.LOB));
            }

            if (myColumnTypes.contains(ColumnType.SEMIMAJOR))
            {
                mySmajComboBox.setSelectedIndex(myColumnTypes.indexOf(ColumnType.SEMIMAJOR));
            }

            if (myColumnTypes.contains(ColumnType.SEMIMINOR))
            {
                mySminComboBox.setSelectedIndex(myColumnTypes.indexOf(ColumnType.SEMIMINOR));
            }

            if (myColumnTypes.contains(ColumnType.ORIENTATION))
            {
                myOrientComboBox.setSelectedIndex(myColumnTypes.indexOf(ColumnType.ORIENTATION));
            }

            // Set the initial state of the extras panel
            if (myColumnTypes.contains(ColumnType.LOB))
            {
                myCurrentExtrasPanel = myLobColumnPanel;
                myExtrasPanel.add(myLobColumnPanel, BorderLayout.CENTER);
                myLobRB.setSelected(true);
            }
            else if (myColumnTypes.contains(ColumnType.SEMIMAJOR) || myColumnTypes.contains(ColumnType.SEMIMINOR)
                    || myColumnTypes.contains(ColumnType.ORIENTATION))
            {
                myCurrentExtrasPanel = myEllipseColumnPanel;
                myExtrasPanel.add(myEllipseColumnPanel, BorderLayout.CENTER);
                myEllipseRB.setSelected(true);
            }
            else
            {
                myCurrentExtrasPanel = myNoExtrasPanel;
                myExtrasPanel.add(myNoExtrasPanel, BorderLayout.CENTER);
                myNoExtrasRB.setSelected(true);
            }

            myExtrasPanel.setMinimumSize(new Dimension(150, 60));
            myExtrasPanel.setPreferredSize(new Dimension(150, 60));
            myExtrasPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(),
                    BorderFactory.createTitledBorder("Extra Type Definitions: Line-Of-Bearing or Ellipse Columns")));

            JPanel selectorPanels = new JPanel(new GridLayout(2, 1));
            selectorPanels.add(myTimePanel);
            selectorPanels.add(myExtrasPanel);
            selectorPanels.setBorder(BorderFactory.createEmptyBorder(10, 40, 0, 40));

            workPanel.add(selectorPanels, BorderLayout.CENTER);
            workPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
            workPanel.setMaximumSize(new Dimension(5000, 300));
            workPanel.setPreferredSize(new Dimension(5000, 300));

            myErrorMsgLabel = new JLabel("SELECTIONS VALID");
            myErrorMsgLabel.setForeground(Color.GREEN);
            myErrorMsgLabel.setHorizontalAlignment(SwingConstants.CENTER);
            workPanel.add(myErrorMsgLabel, BorderLayout.SOUTH);

            setLayout(new BorderLayout());
            add(workPanel, BorderLayout.NORTH);
            add(tablePanel, BorderLayout.CENTER);

            CSVDataPreviewTableModel model = new CSVDataPreviewTableModel(myImportConfig, myValuesSet);
            myDataTable.setModel(model);
            myDataTable.packAll();

            myNoExtrasRB.addActionListener(this);
            myLobRB.addActionListener(this);
            myLobComboBox.addActionListener(this);
            myEllipseRB.addActionListener(this);
            mySmajComboBox.addActionListener(this);
            mySminComboBox.addActionListener(this);
            myOrientComboBox.addActionListener(this);

            checkForValidity(false);
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (e.getSource() instanceof JComboBox)
            {
                myNextBT.setVisible(false);
                checkForValidity(true);
            }
            else if (e.getSource() == myTimestampRB && myTimestampRB.isSelected())
            {
                myTimePanel.remove(myDateTimePanel);
                myTimePanel.add(myTimestampPanel, BorderLayout.CENTER);
                myTimePanel.revalidate();
                myTimePanel.repaint();
                myTimestampPanel.setVisible(true);

                myNextBT.setVisible(false);

                checkForValidity(false);
            }
            else if (e.getSource() == myDateAndTimeRB && myDateAndTimeRB.isSelected())
            {
                myTimePanel.remove(myTimestampPanel);
                myTimePanel.add(myDateTimePanel, BorderLayout.CENTER);
                myDateTimePanel.setVisible(true);
                myTimePanel.revalidate();
                myTimePanel.repaint();
                myNextBT.setVisible(false);
                checkForValidity(false);
            }
            else if (e.getSource() == myNoTimeRB)
            {
                myTimestampPanel.setVisible(false);
                myDateTimePanel.setVisible(false);
                checkForValidity(false);
            }
            else if (e.getSource() == myNoExtrasRB || e.getSource() == myLobRB || e.getSource() == myEllipseRB)
            {
                myExtrasPanel.remove(myCurrentExtrasPanel);
                if (myLobRB.isSelected())
                {
                    myCurrentExtrasPanel = myLobColumnPanel;
                }
                else if (myEllipseRB.isSelected())
                {
                    myCurrentExtrasPanel = myEllipseColumnPanel;
                }
                else
                {
                    myCurrentExtrasPanel = myNoExtrasPanel;
                }
                myExtrasPanel.add(myCurrentExtrasPanel, BorderLayout.CENTER);
                myExtrasPanel.revalidate();
                myExtrasPanel.repaint();

                checkForValidity(false);
            }
        }

        /**
         * Check for validity.
         *
         * @param showPopups the show pop-ups
         */
        private void checkForValidity(boolean showPopups)
        {
            boolean valid = true;

            myUsesTime = !myNoTimeRB.isSelected();
            myImportConfig.setUsesTimestamp(myTimestampRB.isSelected());

            int tsIndex = myTimestampComboBox.getSelectedIndex();
            int dateIndex = myDateDB.getSelectedIndex();
            int timeIndex = myTimeDB.getSelectedIndex();
            int lobIndex = myLobComboBox.getSelectedIndex();
            int smajIndex = mySmajComboBox.getSelectedIndex();
            int sminIndex = mySminComboBox.getSelectedIndex();
            int orntIndex = myOrientComboBox.getSelectedIndex();

            // Figure out if any of the columns are used more than once
            int numColumns = myImportConfig.getColumnNames().size();
            int[] useCount = new int[numColumns];

            if (!myNoTimeRB.isSelected())
            {
                if (myImportConfig.usesTimestamp())
                {
                    useCount[tsIndex]++;
                }
                else
                {
                    useCount[dateIndex]++;
                    useCount[timeIndex]++;
                }
            }

            if (myLobRB.isSelected())
            {
                useCount[lobIndex]++;
            }

            if (myEllipseRB.isSelected())
            {
                useCount[smajIndex]++;
                useCount[sminIndex]++;
                useCount[orntIndex]++;
            }

            boolean sameColumnMultipleTypes = false;
            for (int i = 0; i < numColumns; i++)
            {
                if (useCount[i] > 1)
                {
                    sameColumnMultipleTypes = true;
                    break;
                }
            }

            // If one or more column was used more than once then error
            if (sameColumnMultipleTypes)
            {
                myErrorMsgLabel.setText("SELECTIONS NOT VALID: ONE OR MORE COLUMNS IS USED MORE THAN ONCE");
                myErrorMsgLabel.setForeground(Color.RED);
                valid = false;
            }

            if (valid)
            {
                doOnValid(tsIndex, dateIndex, timeIndex, lobIndex);
            }
        }

        /** Perform more required operations on successful validity check. */
        private void doMoreOnValid()
        {
            // Make sure settings are preserved in to the config
            if (myUsesTime)
            {
                if (myImportConfig.usesTimestamp())
                {
                    myImportConfig.setTimeColumn(myColumnTypes.indexOf(ColumnType.TIMESTAMP));
                }
                else
                {
                    myImportConfig.setDateColumn(myColumnTypes.indexOf(ColumnType.DATE));
                    myImportConfig.setTimeColumn(myColumnTypes.indexOf(ColumnType.TIME));
                }
            }

            if (myLobRB.isSelected())
            {
                myImportConfig.setLobColumn(myColumnTypes.indexOf(ColumnType.LOB));
            }

            if (myEllipseRB.isSelected())
            {
                myImportConfig.setSmajColumn(myColumnTypes.indexOf(ColumnType.SEMIMAJOR));
                myImportConfig.setSminColumn(myColumnTypes.indexOf(ColumnType.SEMIMINOR));
                myImportConfig.setOrientColumn(myColumnTypes.indexOf(ColumnType.ORIENTATION));
            }

            myErrorMsgLabel.setText("SELECTIONS VALID");
            myErrorMsgLabel.setForeground(Color.GREEN);
            myNextBT.setVisible(true);
        }

        /**
         * Perform required operations on successful validity check.
         *
         * @param tsIndex timestamp index.
         * @param dateIndex date index.
         * @param timeIndex time index.
         * @param lobIndex lob index.
         */
        private void doOnValid(int tsIndex, int dateIndex, int timeIndex, int lobIndex)
        {
            // Rebuild column types based on selections
            myColumnTypes.clear();
            myImportConfig.setDateColumn(-1);
            myImportConfig.setTimeColumn(-1);
            myImportConfig.setLobColumn(-1);
            myImportConfig.setSmajColumn(-1);
            myImportConfig.setSminColumn(-1);
            myImportConfig.setOrientColumn(-1);

            for (int i = 0; i < myImportConfig.getColumnNames().size(); i++)
            {
                if (myEllipseRB.isSelected())
                {
                    if (mySmajComboBox.getSelectedIndex() == i)
                    {
                        myColumnTypes.add(ColumnType.SEMIMAJOR);
                        continue;
                    }

                    if (mySminComboBox.getSelectedIndex() == i)
                    {
                        myColumnTypes.add(ColumnType.SEMIMINOR);
                        continue;
                    }

                    if (myOrientComboBox.getSelectedIndex() == i)
                    {
                        myColumnTypes.add(ColumnType.ORIENTATION);
                        continue;
                    }
                }

                if (i == lobIndex && myLobRB.isSelected())
                {
                    myColumnTypes.add(ColumnType.LOB);
                }
                else if (myImportConfig.usesTimestamp())
                {
                    if (i == tsIndex)
                    {
                        if (myUsesTime)
                        {
                            myColumnTypes.add(ColumnType.TIMESTAMP);
                        }
                    }
                    else
                    {
                        myColumnTypes.add(ColumnType.OTHER);
                    }
                }
                else
                {
                    if (i == timeIndex)
                    {
                        if (myUsesTime)
                        {
                            myColumnTypes.add(ColumnType.TIME);
                        }
                        else
                        {
                            myColumnTypes.add(ColumnType.OTHER);
                        }
                    }
                    else if (i == dateIndex)
                    {
                        if (myUsesTime)
                        {
                            myColumnTypes.add(ColumnType.DATE);
                        }
                        else
                        {
                            myColumnTypes.add(ColumnType.OTHER);
                        }
                    }
                    else
                    {
                        myColumnTypes.add(ColumnType.OTHER);
                    }
                }
            }

            doMoreOnValid();
        }

        /**
         * Initialize the GUI components for the error ellipse.
         *
         * @param colNames the names of the columns.
         */
        private void setupEllipseComponents(Object[] colNames)
        {
            myEllipseColumnPanel = new JPanel(new GridLayout(1, 3));
            JPanel smajPanel = new JPanel(new BorderLayout());
            smajPanel.add(new JLabel("Semi-Major"), BorderLayout.NORTH);
            mySmajComboBox = new JComboBox(colNames);
            smajPanel.add(mySmajComboBox, BorderLayout.CENTER);
            myEllipseColumnPanel.add(smajPanel);

            JPanel sminPanel = new JPanel(new BorderLayout());
            sminPanel.add(new JLabel("Semi-Minor"), BorderLayout.NORTH);
            mySminComboBox = new JComboBox(colNames);
            sminPanel.add(mySminComboBox, BorderLayout.CENTER);
            myEllipseColumnPanel.add(sminPanel);

            JPanel orientPanel = new JPanel(new BorderLayout());
            orientPanel.add(new JLabel("Orientation"), BorderLayout.NORTH);
            myOrientComboBox = new JComboBox(colNames);
            orientPanel.add(myOrientComboBox, BorderLayout.CENTER);
            myEllipseColumnPanel.add(orientPanel);
        }

        /** Initialize the time based GUI components. */
        private void setupTimeComponents()
        {
            myTimestampRB = new JRadioButton("Timestamp");
            myTimestampRB.setToolTipText("Time and Date are in the same column");

            myDateAndTimeRB = new JRadioButton("Date/Time");
            myDateAndTimeRB.setToolTipText("Time and Date are in separate columns");

            myNoTimeRB = new JRadioButton("No Date/Time");
            myNoTimeRB.setToolTipText("No Date and/or Time column is available");

            ButtonGroup bg1 = new ButtonGroup();
            bg1.add(myTimestampRB);
            bg1.add(myDateAndTimeRB);
            bg1.add(myNoTimeRB);

            myTimePanel = new JPanel(new BorderLayout());
            JPanel timeTypeCBPanel = new JPanel(new GridLayout(3, 1));
            timeTypeCBPanel.add(myTimestampRB);
            timeTypeCBPanel.add(myDateAndTimeRB);
            timeTypeCBPanel.add(myNoTimeRB);
            timeTypeCBPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
            myTimePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(),
                    BorderFactory.createTitledBorder("Timestamp Column or Date/Time Column")));
            myTimePanel.add(timeTypeCBPanel, BorderLayout.WEST);

            myDateTimePanel = new JPanel(new GridLayout(1, 2, 5, 0));
            JPanel tPanel1 = new JPanel(new BorderLayout());
            tPanel1.add(new JLabel(" Date "), BorderLayout.WEST);
            tPanel1.add(myDateDB, BorderLayout.CENTER);
            myDateTimePanel.add(tPanel1);

            JPanel tPanel2 = new JPanel(new BorderLayout());
            tPanel2.add(new JLabel(" Time "), BorderLayout.WEST);
            tPanel2.add(myTimeDB, BorderLayout.CENTER);
            myDateTimePanel.add(tPanel2);

            myTimestampPanel = new JPanel(new BorderLayout());
            myTimestampPanel.add(myTimestampComboBox, BorderLayout.CENTER);

            if (myColumnTypes.contains(ColumnType.TIMESTAMP))
            {
                myTimestampComboBox.setSelectedIndex(myColumnTypes.indexOf(ColumnType.TIMESTAMP));
                myTimePanel.add(myTimestampPanel, BorderLayout.CENTER);
                myTimestampRB.setSelected(true);
            }
            else if (myColumnTypes.contains(ColumnType.DATE) || myColumnTypes.contains(ColumnType.TIME))
            {
                if (myColumnTypes.contains(ColumnType.DATE))
                {
                    myDateDB.setSelectedIndex(myColumnTypes.indexOf(ColumnType.DATE));
                }

                if (myColumnTypes.contains(ColumnType.TIME))
                {
                    myTimeDB.setSelectedIndex(myColumnTypes.indexOf(ColumnType.TIME));
                }

                myTimePanel.add(myDateTimePanel, BorderLayout.CENTER);
                myDateAndTimeRB.setSelected(true);
            }
            else
            {
                myTimePanel.add(myTimestampPanel, BorderLayout.CENTER);
                myNoTimeRB.setSelected(true);
                myTimestampPanel.setVisible(false);
                myDateTimePanel.setVisible(false);
            }

            myTimePanel.setMaximumSize(new Dimension(5000, 60));
            myTimePanel.setPreferredSize(new Dimension(5000, 60));

            myTimestampRB.addActionListener(this);
            myDateAndTimeRB.addActionListener(this);
            myNoTimeRB.addActionListener(this);
            myTimestampComboBox.addActionListener(this);
            myDateDB.addActionListener(this);
            myTimeDB.addActionListener(this);
        }
    }

    /** The Class TimeFormatPanel. */
    private class TimeFormatPanel extends JPanel implements ActionListener
    {
        /** serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The apply custom date bt. */
        private JButton myApplyCustomDateBT;

        /** The apply custom time bt. */
        private JButton myApplyCustomTimeBT;

        /** The cur date sdf. */
        private SimpleDateFormat myCurDateSDF;

        /** The cur time sdf. */
        private SimpleDateFormat myCurTimeSDF;

        /** The custom date preview lb. */
        private JLabel myCustomDatePreviewLB;

        /** The custom date tf. */
        private JTextField myCustomDateTF;

        /** The custom format date rb. */
        private JRadioButton myCustomFormatDateRB;

        /** The custom format time rb. */
        private JRadioButton myCustomFormatTimeRB;

        /** The custom time preview lb. */
        private final JLabel myCustomTimePreviewLB;

        /** The custom time tf. */
        private final JTextField myCustomTimeTF;

        /** The date combo box. */
        private JComboBox myDateComboBox;

        /** The date col idx. */
        private int myDateColIdx = -1;

        /** The date format error. */
        private String myDateFormatError;

        /** The date format list. */
        private List<DateFormat> myDateFormatList;

        /** The date panel. */
        private JPanel myDatePanel;

        /** The date valid. */
        private boolean myDateValid;

        /** The error msg label. */
        private final JLabel myErrorMsgLabel;

        /** The sample date value. */
        private String mySampleDateValue;

        /** The sample time value. */
        private String mySampleTimeValue;

        /** The sdf help bt. */
        private final JButton mySdfHelpBT;

        /** The selected format date rb. */
        private JRadioButton mySelectedFormatDateRB;

        /** The selected format time rb. */
        private JRadioButton mySelectedFormatTimeRB;

        /** The time combo box. */
        private JComboBox myTimeComboBox;

        /** The time col idx. */
        private int myTimeColIdx = -1;

        /** The time format error. */
        private String myTimeFormatError;

        /** The time format list. */
        private List<DateFormat> myTimeFormatList;

        /** The time panel. */
        private final JPanel myTimePanel;

        /** The timestamp col idx. */
        private int myTimestampColIdx = -1;

        /** The time valid. */
        private boolean myTimeValid;

        /** Instantiates a new time format panel. */
        public TimeFormatPanel()
        {
            super(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            myImportConfig.setUsesTimestamp(myColumnTypes.contains(ColumnType.TIMESTAMP));
            findValidDateTimeTypes();

            myDataTable = new JXTable();
            myDataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            myDataTable.setSortable(false);

            JScrollPane jsp = new JScrollPane(myDataTable);
            jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
            JPanel tablePanel = new JPanel(new BorderLayout());
            tablePanel.add(jsp, BorderLayout.CENTER);
            tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            tablePanel.add(new JLabel("Format Preview ( not all data is shown, original and converted should match )"),
                    BorderLayout.NORTH);
            tablePanel.setMaximumSize(new Dimension(5000, 260));
            tablePanel.setPreferredSize(new Dimension(5000, 260));

            mySdfHelpBT = new JButton("Format Help");
            mySdfHelpBT.addActionListener(this);

            JPanel workPanel = new JPanel(new BorderLayout());

            JTextArea jta = new JTextArea();
            jta.setEditable(false);
            jta.setBackground(workPanel.getBackground());
            Font itemFont = jta.getFont().deriveFont(Font.BOLD);
            jta.setFont(itemFont);
            jta.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
            jta.setText("We now need to determine the format of the TIMESTAMP or ( DATE and TIME )\n"
                    + "columns.  Please select or enter the correct format(s) below:");
            workPanel.add(jta, BorderLayout.NORTH);

            // Build the time panel

            myTimePanel = new JPanel(new BorderLayout());
            myTimePanel.setBorder(BorderFactory.createTitledBorder("Time Format Pattern"));
            myCustomTimeTF = new JTextField();
            if (!myTimeFormatList.isEmpty())
            {
                setupWithTimeFormats();
            }
            else
            {
                myTimePanel.add(new JLabel("Time Format"), BorderLayout.WEST);
                myTimePanel.add(myCustomTimeTF, BorderLayout.CENTER);

                myApplyCustomTimeBT = new JButton(APPLY_STRING);
                myApplyCustomTimeBT.setToolTipText(APPLY_ENTERED_CUSTOM_FORMAT);
                myApplyCustomTimeBT.addActionListener(this);
                myTimePanel.add(myApplyCustomTimeBT, BorderLayout.EAST);
            }

            myCustomTimePreviewLB = new JLabel("Preview: ERROR");
            if (myImportConfig.getTimeFormat() != null)
            {
                myCustomTimePreviewLB.setText(PREVIEW_STRING + myImportConfig.getTimeFormat().getSample());
            }
            myTimePanel.add(myCustomTimePreviewLB, BorderLayout.SOUTH);

            if (myImportConfig.usesTimestamp())
            {
                workPanel.add(myTimePanel, BorderLayout.CENTER);
            }
            else
            {
                // Build date panel

                myDatePanel = new JPanel(new BorderLayout());
                myDatePanel.setBorder(BorderFactory.createTitledBorder("Date Format Pattern"));
                myCustomDateTF = new JTextField();
                if (!myDateFormatList.isEmpty())
                {
                    setupWithDateFormats();
                }
                else
                {
                    myDatePanel.add(new JLabel("Date Format"), BorderLayout.WEST);
                    myDatePanel.add(myCustomDateTF, BorderLayout.CENTER);

                    myApplyCustomDateBT = new JButton(APPLY_STRING);
                    myApplyCustomDateBT.setToolTipText(APPLY_ENTERED_CUSTOM_FORMAT);
                    myApplyCustomDateBT.addActionListener(this);
                    myDatePanel.add(myApplyCustomDateBT, BorderLayout.EAST);
                }

                myCustomDatePreviewLB = new JLabel("Preview: ERROR");
                if (myImportConfig.getDateFormat() != null)
                {
                    myCustomDatePreviewLB.setText(PREVIEW_STRING + myImportConfig.getDateFormat().getSample());
                }
                myDatePanel.add(myCustomDatePreviewLB, BorderLayout.SOUTH);

                JPanel twoPartPanel = new JPanel(new GridLayout(2, 1));
                twoPartPanel.add(myTimePanel);
                twoPartPanel.add(myDatePanel);

                workPanel.add(twoPartPanel, BorderLayout.CENTER);
            }

            workPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

            myErrorMsgLabel = new JLabel("SELECTIONS VALID");
            myErrorMsgLabel.setForeground(Color.GREEN);
            myErrorMsgLabel.setHorizontalAlignment(SwingConstants.CENTER);

            JPanel errorPanel = new JPanel(new BorderLayout());
            errorPanel.add(myErrorMsgLabel, BorderLayout.CENTER);
            errorPanel.add(mySdfHelpBT, BorderLayout.EAST);

            workPanel.add(errorPanel, BorderLayout.SOUTH);
            workPanel.setMaximumSize(new Dimension(5000, 200));
            workPanel.setPreferredSize(new Dimension(5000, 200));

            setLayout(new BorderLayout());
            add(workPanel, BorderLayout.NORTH);
            add(tablePanel, BorderLayout.CENTER);

            DateTimeFormatPreviewTableModel model = new DateTimeFormatPreviewTableModel();
            myDataTable.setModel(model);
            myDataTable.packAll();

            refreshDatePreview();
            refreshTimePreview();
            checkForValidity();
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (e.getSource() == mySdfHelpBT)
            {
                showSDFBrowser();
            }
            else if (e.getSource() == mySelectedFormatTimeRB && mySelectedFormatTimeRB.isSelected())
            {
                myCustomTimeTF.setEnabled(false);
                myApplyCustomTimeBT.setEnabled(false);
                myTimeComboBox.setEnabled(true);
                myImportConfig.setTimeFormat((DateFormat)myTimeComboBox.getSelectedItem());
                myCurTimeSDF = myImportConfig.getTimeFormat().getFormat();
                refreshTimePreview();
            }
            else if (e.getSource() == myCustomFormatTimeRB && myCustomFormatTimeRB.isSelected())
            {
                myCustomTimeTF.setEnabled(true);
                myApplyCustomTimeBT.setEnabled(true);
                myTimeComboBox.setEnabled(false);
                processCustomTimeFormat();
            }
            else if (e.getSource() == myApplyCustomTimeBT)
            {
                processCustomTimeFormat();
            }
            else if (e.getSource() == mySelectedFormatDateRB && mySelectedFormatDateRB.isSelected())
            {
                myCustomDateTF.setEnabled(false);
                myApplyCustomDateBT.setEnabled(false);
                myDateComboBox.setEnabled(true);
                myImportConfig.setDateFormat((DateFormat)myDateComboBox.getSelectedItem());
                myCurDateSDF = myImportConfig.getDateFormat().getFormat();
                refreshDatePreview();
            }
            else if (e.getSource() == myCustomFormatDateRB && myCustomFormatDateRB.isSelected())
            {
                myCustomDateTF.setEnabled(true);
                myApplyCustomDateBT.setEnabled(true);
                myDateComboBox.setEnabled(false);
                processCustomDateFormat();
            }
            else if (e.getSource() == myApplyCustomDateBT)
            {
                processCustomDateFormat();
            }
            else if (e.getSource() == myTimeComboBox)
            {
                myImportConfig.setTimeFormat((DateFormat)myTimeComboBox.getSelectedItem());
                myCurTimeSDF = myImportConfig.getTimeFormat().getFormat();
                refreshTimePreview();
            }
            else if (e.getSource() == myDateComboBox)
            {
                myImportConfig.setDateFormat((DateFormat)myDateComboBox.getSelectedItem());
                myCurDateSDF = myImportConfig.getDateFormat().getFormat();
                refreshDatePreview();
            }
        }

        /** Check for validity. */
        private void checkForValidity()
        {
            boolean valid = false;

            if (myImportConfig.usesTimestamp())
            {
                valid = myTimeValid;
            }
            else
            {
                valid = myTimeValid && myDateValid;
            }

            if (valid)
            {
                myErrorMsgLabel.setText("SETTINGS VALID");
                myErrorMsgLabel.setForeground(Color.GREEN);
                myNextBT.setVisible(true);
            }
            else
            {
                myErrorMsgLabel.setText("SETTINGS IN-VALID");
                myErrorMsgLabel.setForeground(Color.RED);
                myNextBT.setVisible(false);
            }
        }

        /** Find valid date time types. */
        private void findValidDateTimeTypes()
        {
            if (myImportConfig.usesTimestamp())
            {
                myTimestampColIdx = myColumnTypes.indexOf(ColumnType.TIMESTAMP);
                mySampleTimeValue = myValuesSet.get(0).get(myTimestampColIdx);
                myTimeFormatList = myDateFormatsConfig.findMatchingPatterns(mySampleTimeValue);
                myUserDateFormatsConfig.findMatchingPatterns(myTimeFormatList, mySampleTimeValue.trim());
                if (!myTimeFormatList.isEmpty())
                {
                    myImportConfig.setTimeFormat(myTimeFormatList.get(0));
                    myCurTimeSDF = myImportConfig.getTimeFormat().getFormat();
                }
            }
            else
            {
                myTimeColIdx = myColumnTypes.indexOf(ColumnType.TIME);
                myDateColIdx = myColumnTypes.indexOf(ColumnType.DATE);
                mySampleTimeValue = myValuesSet.get(0).get(myTimeColIdx);
                mySampleDateValue = myValuesSet.get(0).get(myDateColIdx);
                myTimeFormatList = myDateFormatsConfig.findMatchingPatterns(mySampleTimeValue);
                myUserDateFormatsConfig.findMatchingPatterns(myTimeFormatList, mySampleTimeValue.trim());

                myDateFormatList = myDateFormatsConfig.findMatchingPatterns(mySampleDateValue);
                myUserDateFormatsConfig.findMatchingPatterns(myDateFormatList, mySampleDateValue.trim());

                if (!myTimeFormatList.isEmpty())
                {
                    myImportConfig.setTimeFormat(myTimeFormatList.get(0));
                    myCurTimeSDF = myImportConfig.getTimeFormat().getFormat();
                }

                if (!myDateFormatList.isEmpty())
                {
                    myImportConfig.setDateFormat(myDateFormatList.get(0));
                    myCurDateSDF = myImportConfig.getDateFormat().getFormat();
                }
            }
            ((DefaultTableModel)myDataTable.getModel()).fireTableDataChanged();
        }

        /** Process custom date format. */
        private void processCustomDateFormat()
        {
            myImportConfig.setDateFormat(null);
            myCurDateSDF = null;

            String customFormat = myCustomDateTF.getText();
            myDateFormatError = "No Format Specified";
            if (StringUtils.isNotEmpty(customFormat))
            {
                SimpleDateFormat sdf = null;
                try
                {
                    sdf = new SimpleDateFormat(customFormat);
                }
                catch (IllegalArgumentException exc)
                {
                    sdf = null;
                    myDateFormatError = "Format is not a valid Simple Date Format";
                }
                if (sdf != null)
                {
                    DateFormat.Type dft = DateFormat.Type.DATE;
                    myImportConfig.setDateFormat(new DateFormat(dft, customFormat));
                    myCurDateSDF = myImportConfig.getDateFormat().getFormat();

                    try
                    {
                        DateTimeUtilities.parse(myImportConfig.getDateFormat().getFormat(), mySampleDateValue);
                        if (myUserDateFormatsConfig.addFormat(new DateFormat(dft, customFormat)))
                        {
                            myPreferences.putJAXBObject(MantleConstants.USER_DATE_FORMAT_CONFIG_FILE_KEY, myUserDateFormatsConfig,
                                    false, this);
                        }
                    }
                    catch (ParseException e)
                    {
                        myDateFormatError = SPECIFIED_FORMAT_UNABLE_TO_PROPERLY_PARSE_SAMPLE_FROM_FILE;
                    }
                }
            }

            refreshDatePreview();
        }

        /** Process custom time format. */
        private void processCustomTimeFormat()
        {
            myImportConfig.setTimeFormat(null);
            myCurTimeSDF = null;

            String customFormat = myCustomTimeTF.getText();
            myTimeFormatError = "No Format Specified";
            if (StringUtils.isNotEmpty(customFormat))
            {
                SimpleDateFormat sdf = null;
                try
                {
                    sdf = new SimpleDateFormat(customFormat);
                }
                catch (IllegalArgumentException exc)
                {
                    sdf = null;
                    myTimeFormatError = "Format is not a valid Simple Date Format";
                }
                if (sdf != null)
                {
                    DateFormat.Type dft = DateFormat.Type.TIMESTAMP;
                    if (!myImportConfig.usesTimestamp())
                    {
                        dft = DateFormat.Type.TIME;
                    }

                    myImportConfig.setTimeFormat(new DateFormat(dft, customFormat));
                    myCurTimeSDF = myImportConfig.getTimeFormat().getFormat();

                    try
                    {
                        DateTimeUtilities.parse(myImportConfig.getTimeFormat().getFormat(), mySampleTimeValue);
                        if (myUserDateFormatsConfig.addFormat(new DateFormat(dft, customFormat)))
                        {
                            myPreferences.putJAXBObject(MantleConstants.USER_DATE_FORMAT_CONFIG_FILE_KEY, myUserDateFormatsConfig,
                                    false, this);
                        }
                    }
                    catch (ParseException e)
                    {
                        myTimeFormatError = SPECIFIED_FORMAT_UNABLE_TO_PROPERLY_PARSE_SAMPLE_FROM_FILE;
                    }
                }
            }

            refreshTimePreview();
        }

        /** Refresh date preview. */
        private void refreshDatePreview()
        {
            myDateValid = false;
            if (myCustomDatePreviewLB != null)
            {
                if (myImportConfig.getDateFormat() != null)
                {
                    try
                    {
                        myCustomDatePreviewLB.setForeground(Color.GREEN);
                        Date date = DateTimeUtilities.parse(myImportConfig.getDateFormat().getFormat(), mySampleDateValue);
                        String reformatted = myImportConfig.getDateFormat().getFormat().format(date);
                        Date date2 = DateTimeUtilities.parse(myImportConfig.getDateFormat().getFormat(), reformatted);
                        myCustomDatePreviewLB.setText(PREVIEW_STRING + reformatted);

                        if (date.equals(date2))
                        {
                            myCustomDatePreviewLB.setText(PREVIEW_STRING + reformatted);
                            myDateValid = true;
                        }
                        else
                        {
                            myCustomDatePreviewLB.setText("Preview: ERROR - Reformatted [" + reformatted
                                    + "] Does Not Match Original [" + mySampleDateValue + "]");
                            myCustomDatePreviewLB.setForeground(Color.RED);
                        }
                    }
                    catch (ParseException e)
                    {
                        myCustomDatePreviewLB.setText("Preview: ERROR - Format does not match first sample from file");
                        myCustomDatePreviewLB.setForeground(Color.RED);
                    }
                }
                else
                {
                    myCustomDatePreviewLB.setText("Preview: ERROR - " + myDateFormatError);
                    myCustomDatePreviewLB.setForeground(Color.RED);
                }
            }
            ((DefaultTableModel)myDataTable.getModel()).fireTableDataChanged();
            checkForValidity();
        }

        /** Refresh time preview. */
        private void refreshTimePreview()
        {
            myTimeValid = false;
            if (myImportConfig.getTimeFormat() != null)
            {
                try
                {
                    myCustomTimePreviewLB.setForeground(Color.GREEN);

                    Date date = DateTimeUtilities.parse(myCurTimeSDF, mySampleTimeValue);
                    String reformatted = myCurTimeSDF.format(date);
                    Date date2 = DateTimeUtilities.parse(myCurTimeSDF, reformatted);
                    if (date.equals(date2))
                    {
                        myCustomTimePreviewLB.setText(PREVIEW_STRING + reformatted);
                        myTimeValid = true;
                    }
                    else
                    {
                        myCustomTimePreviewLB.setText("Preview: ERROR - Reformatted [" + reformatted
                                + "] Does Not Match Original [" + mySampleTimeValue + "]");
                        myCustomTimePreviewLB.setForeground(Color.RED);
                    }
                }
                catch (ParseException e)
                {
                    myCustomTimePreviewLB.setText("Preview: ERROR - Could Not Parse Time");
                    myCustomTimePreviewLB.setForeground(Color.RED);
                }
            }
            else
            {
                myCustomTimePreviewLB.setText("Preview: ERROR - " + myTimeFormatError);
                myCustomTimePreviewLB.setForeground(Color.RED);
            }
            ((DefaultTableModel)myDataTable.getModel()).fireTableDataChanged();
            checkForValidity();
        }

        /** Setup GUI components when there are valid date formats available. */
        private void setupWithDateFormats()
        {
            JPanel a = new JPanel(new GridLayout(1, 2));
            mySelectedFormatDateRB = new JRadioButton("Pre-Defined");
            mySelectedFormatDateRB.setToolTipText("Use a pre-defined time pattern");
            mySelectedFormatDateRB.setSelected(true);

            myCustomFormatDateRB = new JRadioButton("Custom");
            myCustomFormatDateRB.setToolTipText("Use a custom date pattern");
            myCustomDateTF.setEnabled(false);

            myApplyCustomDateBT = new JButton(APPLY_STRING);
            myApplyCustomDateBT.setToolTipText(APPLY_ENTERED_CUSTOM_FORMAT);
            myApplyCustomDateBT.addActionListener(this);
            myApplyCustomDateBT.setEnabled(false);

            ButtonGroup bg1 = new ButtonGroup();
            bg1.add(mySelectedFormatDateRB);
            bg1.add(myCustomFormatDateRB);

            myDateComboBox = new JComboBox(myDateFormatList.toArray());
            myDateComboBox.setSelectedItem(myImportConfig.getDateFormat());

            JPanel selPanel = new JPanel(new BorderLayout());
            selPanel.add(mySelectedFormatDateRB, BorderLayout.NORTH);
            selPanel.add(myDateComboBox, BorderLayout.CENTER);

            JPanel customPanel = new JPanel(new BorderLayout());
            customPanel.add(myCustomFormatDateRB, BorderLayout.NORTH);
            customPanel.add(myCustomDateTF, BorderLayout.CENTER);
            customPanel.add(myApplyCustomDateBT, BorderLayout.EAST);

            a.add(selPanel);
            a.add(customPanel);
            myDatePanel.add(a, BorderLayout.CENTER);

            myDateComboBox.addActionListener(this);
            mySelectedFormatDateRB.addActionListener(this);
            myCustomFormatDateRB.addActionListener(this);
        }

        /** Setup GUI components when there are valid time formats available. */
        private void setupWithTimeFormats()
        {
            JPanel a = new JPanel(new GridLayout(1, 2));
            mySelectedFormatTimeRB = new JRadioButton("Pre-Defined");
            mySelectedFormatTimeRB.setToolTipText("Use a pre-defined time pattern");
            mySelectedFormatTimeRB.setSelected(true);

            myCustomFormatTimeRB = new JRadioButton("Custom");
            myCustomFormatTimeRB.setToolTipText("Use a custom time pattern");
            myCustomTimeTF.setEnabled(false);

            myApplyCustomTimeBT = new JButton(APPLY_STRING);
            myApplyCustomTimeBT.setToolTipText(APPLY_ENTERED_CUSTOM_FORMAT);
            myApplyCustomTimeBT.addActionListener(this);
            myApplyCustomTimeBT.setEnabled(false);

            ButtonGroup bg1 = new ButtonGroup();
            bg1.add(mySelectedFormatTimeRB);
            bg1.add(myCustomFormatTimeRB);

            myTimeComboBox = new JComboBox(myTimeFormatList.toArray());
            myTimeComboBox.setSelectedItem(myImportConfig.getTimeFormat());

            JPanel selPanel = new JPanel(new BorderLayout());
            selPanel.add(mySelectedFormatTimeRB, BorderLayout.NORTH);
            selPanel.add(myTimeComboBox, BorderLayout.CENTER);

            JPanel customPanel = new JPanel(new BorderLayout());
            customPanel.add(myCustomFormatTimeRB, BorderLayout.NORTH);
            customPanel.add(myCustomTimeTF, BorderLayout.CENTER);
            customPanel.add(myApplyCustomTimeBT, BorderLayout.EAST);

            a.add(selPanel);
            a.add(customPanel);
            myTimePanel.add(a, BorderLayout.CENTER);

            myTimeComboBox.addActionListener(this);
            mySelectedFormatTimeRB.addActionListener(this);
            myCustomFormatTimeRB.addActionListener(this);
        }

        /** The Class DateTimeFormatPreviewTableModel. */
        private class DateTimeFormatPreviewTableModel extends DefaultTableModel
        {
            /** serialVersionUID. */
            private static final long serialVersionUID = 1L;

            /** Instantiates a new date time format preview table model. */
            public DateTimeFormatPreviewTableModel()
            {
                super();
            }

            @Override
            public int getColumnCount()
            {
                return myImportConfig.usesTimestamp() ? 2 : 4;
            }

            @Override
            public String getColumnName(int columnIndex)
            {
                if (myImportConfig.usesTimestamp())
                {
                    if (columnIndex == 0)
                    {
                        return "Timestamp";
                    }
                    else
                    {
                        return "Converted";
                    }
                }
                else
                {
                    if (columnIndex == 0)
                    {
                        return "Date";
                    }
                    else if (columnIndex == 1)
                    {
                        return "Converted Date";
                    }
                    else if (columnIndex == 2)
                    {
                        return "Time";
                    }
                    else
                    {
                        return "Converted Time";
                    }
                }
            }

            @Override
            public int getRowCount()
            {
                return myValuesSet != null ? myValuesSet.size() : 0;
            }

            @Override
            public Object getValueAt(int row, int col)
            {
                try
                {
                    if (myValuesSet != null)
                    {
                        if (col == 0)
                        {
                            if (myImportConfig.usesTimestamp())
                            {
                                return myValuesSet.get(row).get(myTimestampColIdx);
                            }
                            else
                            {
                                return myValuesSet.get(row).get(myDateColIdx);
                            }
                        }
                        else if (col == 1)
                        {
                            if (myImportConfig.usesTimestamp())
                            {
                                if (myCurTimeSDF != null)
                                {
                                    return myCurTimeSDF.format(
                                            DateTimeUtilities.parse(myCurTimeSDF, myValuesSet.get(row).get(myTimestampColIdx)));
                                }
                                else
                                {
                                    return "ERROR";
                                }
                            }
                            else
                            {
                                if (myCurDateSDF != null)
                                {
                                    return myCurDateSDF.format(
                                            DateTimeUtilities.parse(myCurDateSDF, myValuesSet.get(row).get(myDateColIdx)));
                                }
                                else
                                {
                                    return "ERROR";
                                }
                            }
                        }
                        else if (col == 2)
                        {
                            return myValuesSet.get(row).get(myTimeColIdx);
                        }
                        else
                        {
                            if (myCurTimeSDF != null)
                            {
                                return myCurTimeSDF
                                        .format(DateTimeUtilities.parse(myCurTimeSDF, myValuesSet.get(row).get(myTimeColIdx)));
                            }
                            else
                            {
                                return "ERROR";
                            }
                        }
                    }
                    else
                    {
                        return "";
                    }
                }
                catch (IndexOutOfBoundsException e)
                {
                    return "";
                }
                catch (IllegalArgumentException e)
                {
                    return "ERROR - Round Trip Conversion Error";
                }
                catch (ParseException e)
                {
                    return "ERROR - Format Invalid";
                }
            }

            @Override
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }

            @Override
            public void setValueAt(Object aValue, int row, int column)
            {
                // Intentionally not implemented as cell should not be edited.
            }
        }
    }
}
