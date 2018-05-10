package io.opensphere.imagery;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.gdal.gdal.Dataset;
import org.gdal.osr.SpatialReference;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.imagery.gdal.GDALImageLayerData;
import io.opensphere.imagery.gdal.GDALInfo;
import io.opensphere.imagery.gdal.GDALTools;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.mantle.datasources.IDataSourceCreator;

/**
 * The Class AdvancedImageSourceWizard.
 */
@SuppressWarnings("PMD.GodClass")
public class ImagerySourceWizardPanel
{
    /** The Constant IMPORT_ADD_TO_GROUP. */
    private static final String IMPORT_ADD_TO_GROUP = "Import into existing group";

    /** The Constant IMPORT_AS_NEW_GROUP. */
    private static final String IMPORT_AS_NEW_GROUP = "Import as new group";

    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(ImagerySourceWizardPanel.class);

    /** The Caller. */
    private final IDataSourceCreator myCaller;

    /** The Cancel button. */
    private JButton myCancelButton;

    /** The Choosen files. */
    private final List<File> myChoosenFiles;

    /** The Error label. */
    private JLabel myErrorLabel;

    /** The File dir tf. */
    private JTextField myFileDirTF;

    /** The File info panel. */
    private JPanel myFileInfoPanel;

    /** The File list panel. */
    private JPanel myFileListPanel;

    /** The File preview ta. */
    private JTextArea myFilePreviewTA;

    /** The Ignore zeros toggle bt. */
    private JToggleButton myIgnoreZerosToggleBt;

    /** The Image info tile panels. */
    private final List<ImageInfoTilePanel> myImageInfoTilePanels;

    /** The Import cancel bt. */
    private JButton myImportCancelBt;

    /** The import choice panel. */
    private JPanel myImportChoicePanel;

    /** The Import group chooser combo box. */
    private JComboBox<String> myImportGroupChooserCombo;

    /** The Import group name. */
    private String myImportGroupName = "";

    /** The Import group name tf. */
    private JTextField myImportGroupNameTF;

    /** The import name error label. */
    private JLabel myImportNameErrorLb;

    /** The import name panel. */
    private JPanel myImportNamePanel;

    /** The Import ok bt. */
    private JButton myImportOkBt;

    /** The Import sources. */
    private final List<ImageryFileSource> myImportSources;

    /** The Import type. */
    private String myImportType = IMPORT_AS_NEW_GROUP;

    /** The Import type combo box. */
    private JComboBox<String> myImportTypeCombo;

    /** The Import type stage panel. */
    private JPanel myImportTypeStagePanel;

    /** The Inactive names in use list. */
    private final List<String> myInactiveNamesInUseList;

    /** The Names in use list. */
    private final List<String> myNamesInUseList;

    /** The OK button. */
    private JButton myOKButton;

    /** The Ov cancel bt. */
    private JButton myOvCancelBt;

    /** The Overview stage panel. */
    private JPanel myOverviewStagePanel;

    /** The Ov files to process prog bar. */
    private JProgressBar myOvFilesToProcessProgBar;

    /** The Ov files to proc note label. */
    private JLabel myOvFilesToProcNoteLB;

    /** The Ov generation note label. */
    private JLabel myOvGenerationNoteLB;

    /** The Ov generation prog bar. */
    private JProgressBar myOvGenerationProgBar;

    /** The Ov ok bt. */
    private JButton myOvOkBt;

    /** The Ov proc ta. */
    private JTextArea myOvProcTA;

    /** The Owner. */
    private final Container myOwner;

    /** The Read only dir. */
    private boolean myReadOnlyDir;

    /** The Read only dir label. */
    private JLabel myReadOnlyDirLB;

    /** The Sources in use. */
    private final Set<IDataSource> mySourcesInUse;

    /** The Source tile listener. */
    private ActionListener mySourceTileListener;

    /** The Source to dataset map. */
    private final IdentityHashMap<ImageryFileSource, Dataset> mySourceToDatasetMap;

    /** The Stage. */
    private final Stage myStage = Stage.SETTINGS_STAGE;

    /**
     * Helper function in creating panels with various parameters.
     *
     * @param c1 the c1
     * @param c2 the c2
     * @param width the width
     * @param height the height
     * @return the j panel
     */
    protected static JPanel createSubPanel(JComponent c1, JComponent c2, int width, int height)
    {
        return createSubPanel(c1, c2, width, height, null);
    }

    /**
     * Helper function in creating panels with various parameters.
     *
     * @param c1 the c1
     * @param c2 the c2
     * @param width the width
     * @param height the height
     * @param b the b
     * @return the j panel
     */
    protected static JPanel createSubPanel(JComponent c1, JComponent c2, int width, int height, Border b)
    {
        Dimension d = new Dimension(width, height);
        JPanel retPanel = new JPanel();
        retPanel.setPreferredSize(d);
        retPanel.setMinimumSize(d);
        retPanel.setMaximumSize(d);
        if (b != null)
        {
            retPanel.setBorder(b);
        }

        if (c2 != null)
        {
            retPanel.setLayout(new GridLayout(2, 1));
        }
        else
        {
            retPanel.setLayout(new BorderLayout());
        }

        retPanel.add(c1);
        if (c2 != null)
        {
            retPanel.add(c2);
        }

        return retPanel;
    }

    /**
     * Instantiates a new imagery source wizard panel.
     *
     * @param parent the parent
     * @param tb the tb
     * @param group the group
     * @param sourcesInUse the sources in use
     * @param caller the caller
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public ImagerySourceWizardPanel(Container parent, Toolbox tb, ImagerySourceGroup group, Set<IDataSource> sourcesInUse,
            IDataSourceCreator caller)
    {
        myOwner = parent;
        myImportSources = new ArrayList<>(group.getImageSources());
        myImageInfoTilePanels = new ArrayList<>();
        mySourceToDatasetMap = new IdentityHashMap<>();
        myChoosenFiles = group.getFileList();
        mySourcesInUse = sourcesInUse;

        myInactiveNamesInUseList = new ArrayList<>();
        myNamesInUseList = new ArrayList<>();
        for (IDataSource val : mySourcesInUse)
        {
            if (!val.isActive() && !val.isFrozen())
            {
                myInactiveNamesInUseList.add(val.getName());
            }
            myNamesInUseList.add(val.getName());
        }

        Collections.sort(myNamesInUseList);

        myCaller = caller;

        createFileInfoPanel(false);
        createOverviewGenPanel();
        createImportTypePanel();
        if (!myChoosenFiles.isEmpty())
        {
            handleFirstCase(myChoosenFiles.get(0));
        }

        myImportGroupNameTF.setText(group.getName());
        myImportGroupName = group.getName();

        switchToPanel(myFileInfoPanel);
    }

    /**
     * Instantiates a new advanced image source wizard.
     *
     * @param parent the parent
     * @param tb the tb
     * @param chosenFiles the chosen files
     * @param sourcesInUse the sources in use
     * @param caller the caller
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public ImagerySourceWizardPanel(Container parent, Toolbox tb, List<File> chosenFiles, Set<IDataSource> sourcesInUse,
            IDataSourceCreator caller)
    {
        myOwner = parent;
        myImportSources = new ArrayList<>();
        myImageInfoTilePanels = new ArrayList<>();
        mySourceToDatasetMap = new IdentityHashMap<>();
        myChoosenFiles = chosenFiles;
        mySourcesInUse = sourcesInUse;
        myInactiveNamesInUseList = new ArrayList<>();
        myNamesInUseList = new ArrayList<>();
        for (IDataSource val : mySourcesInUse)
        {
            if (!val.isActive() && !val.isFrozen())
            {
                myInactiveNamesInUseList.add(val.getName());
            }
            myNamesInUseList.add(val.getName());
        }

        Collections.sort(myNamesInUseList);

        myCaller = caller;

        createFileInfoPanel(true);
        createOverviewGenPanel();
        createImportTypePanel();
        switchToPanel(myFileInfoPanel);
    }

    /**
     * Completes the wizard and returns the status and possible created sources
     * to the caller.
     *
     * @param success - true if succeeded
     */
    protected void completeWizard(boolean success)
    {
        if (success)
        {
            for (Map.Entry<ImageryFileSource, Dataset> entry : mySourceToDatasetMap.entrySet())
            {
                if (entry.getValue() != null)
                {
                    entry.getValue().delete();
                }
            }
            mySourceToDatasetMap.clear();

            if (EqualsHelper.equals(myImportType, IMPORT_AS_NEW_GROUP))
            {
                completeWizardReally(success, new ImagerySourceGroup(myImportGroupName));
            }
            else if (EqualsHelper.equals(myImportType, IMPORT_ADD_TO_GROUP))
            {
                ImagerySourceGroup group = null;
                for (IDataSource src : mySourcesInUse)
                {
                    if (src.getName().equals(myImportGroupName))
                    {
                        group = (ImagerySourceGroup)src;
                        break;
                    }
                }

                if (group == null)
                {
                    LOGGER.warn("Group not found: " + myImportGroupName);
                }
                else
                {
                    completeWizardReally(success, group);
                }
            }
            else
            {
                myCaller.sourceCreated(success, null);
            }
        }
        else
        {
            myCaller.sourceCreated(false, null);
        }
    }

    /**
     * Really completes the wizard and returns the status and possible created
     * sources to the caller.
     *
     * @param success - true if succeeded
     * @param group the group
     */
    private void completeWizardReally(boolean success, ImagerySourceGroup group)
    {
        for (ImageryFileSource src : myImportSources)
        {
            src.setGroupName(group.getName());
            src.setDescription("");
            group.getImageSources().add(src);
        }
        myCaller.sourceCreated(success, group);
    }

    /**
     * Checks if is file name in use.
     *
     * @param name the name
     * @param excludeThis the exclude this
     * @return true, if is file name in use
     */
    protected boolean isFileNameInUse(String name, IDataSource excludeThis)
    {
        boolean isInUse = false;
        if (!isInUse)
        {
            for (IDataSource src : myImportSources)
            {
                if (Utilities.sameInstance(src, excludeThis))
                {
                    continue;
                }

                if (name.equals(src.getName()))
                {
                    isInUse = true;
                    break;
                }
            }
        }

        // Check all the other groups for name conflicts
        if (!isInUse)
        {
            for (IDataSource ads : mySourcesInUse)
            {
                ImagerySourceGroup srcGrp = (ImagerySourceGroup)ads;
                if (srcGrp.getImageSources() != null && srcGrp.getImageSources().size() > 0)
                {
                    for (ImageryFileSource src : srcGrp.getImageSources())
                    {
                        if (name.equals(src.getName()))
                        {
                            isInUse = true;
                            break;
                        }
                    }
                }
                if (isInUse)
                {
                    break;
                }
            }
        }

        return isInUse;
    }

    /**
     * Unhides the overview OK button when overview generation is complete
     * allowing the user to move forward in the wizard.
     */
    protected void overViewGenerationComplete()
    {
        myOvOkBt.setVisible(true);
    }

    /**
     * Function to validate the state of the initial import stage and only
     * allows users to move forward if everything is properly configured.
     *
     * @return true if valid, false if not
     */
    protected boolean validateImportSettings()
    {
        String error = null;
        for (ImageInfoTilePanel tile : myImageInfoTilePanels)
        {
            String tError = tile.areSettingsValid();
            if (error == null)
            {
                error = tError;
            }
        }
        if (error != null)
        {
            myErrorLabel.setText(error);
            myErrorLabel.setForeground(Color.red);
        }
        else
        {
            myErrorLabel.setForeground(Color.green);
            myErrorLabel.setText("All Settings Valid");
        }
        myOKButton.setVisible(error == null);

        return error == null;
    }

    /**
     * Validates the state of the import type stage and shows errors and allows
     * the user to proceed with the import only when in a good state.
     */
    protected void validateImportTypeStage()
    {
        myImportType = (String)myImportTypeCombo.getSelectedItem();
        boolean valid = true;
        if (EqualsHelper.equals(myImportType, IMPORT_AS_NEW_GROUP))
        {
            myImportGroupName = myImportGroupNameTF.getText();

            if (myNamesInUseList.contains(myImportGroupNameTF.getText()))
            {
                myImportNameErrorLb.setText("NOT VALID: THIS GROUP NAME IS ALREADY IN USE");
                myImportNameErrorLb.setForeground(Color.RED);
                valid = false;
            }
            else if (StringUtils.isEmpty(myImportGroupNameTF.getText()))
            {
                myImportNameErrorLb.setText("NOT VALID: THE GROUP NAME CAN NOT BE EMPTY");
                myImportNameErrorLb.setForeground(Color.RED);
                valid = false;
            }
            else
            {
                myImportNameErrorLb.setText("VALID");
                myImportNameErrorLb.setForeground(Color.green);
            }
        }
        else
        {
            myImportGroupName = (String)myImportGroupChooserCombo.getSelectedItem();
            myImportOkBt.setVisible(true);
        }

        myImportOkBt.setVisible(valid);
    }

    /**
     * Builds the assist1.
     *
     * @param topLabel the top label
     * @param fileDirPanel the file dir panel
     * @param fileListPanel the file list panel
     * @param btPanel the bt panel
     */
    private void buildAssist1(JLabel topLabel, JPanel fileDirPanel, JPanel fileListPanel, JPanel btPanel)
    {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2, 10, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 10, 20));
        myOKButton = new JButton("OK");
        myOKButton.addActionListener(createOKButtonActionListener());

        myCancelButton = new JButton("Cancel");
        myCancelButton.addActionListener(actionevent -> completeWizard(false));
        buttonPanel.add(myOKButton);
        buttonPanel.add(myCancelButton);

        JPanel warningPanel = new JPanel(new BorderLayout());
        warningPanel.setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 160));

        JLabel warningLabel1 = new JLabel("Warning: Very slow performance may result from files larger than 1 GB   ");
        warningLabel1.setForeground(Color.yellow);
        warningPanel.add(warningLabel1, BorderLayout.WEST);

        JPanel compositePanel = new JPanel(new BorderLayout());

        compositePanel.add(buttonPanel, BorderLayout.EAST);
        JPanel warnBtPanel = new JPanel(new BorderLayout());
        warnBtPanel.add(warningPanel, BorderLayout.CENTER);
        warnBtPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        compositePanel.add(warnBtPanel, BorderLayout.CENTER);

        JButton warnDetailsBt = new JButton("Details");
        warnDetailsBt.setForeground(Color.yellow);
        warnDetailsBt.setMargin(new Insets(5, 3, 5, 3));
        warnDetailsBt.setFocusable(false);
        warningPanel.add(warnDetailsBt, BorderLayout.CENTER);

        myFileInfoPanel.setLayout(new BorderLayout());
        myFileInfoPanel.add(topLabel, BorderLayout.NORTH);
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(fileDirPanel, BorderLayout.NORTH);
        contentPanel.add(fileListPanel, BorderLayout.CENTER);
        contentPanel.add(btPanel, BorderLayout.SOUTH);
        myFileInfoPanel.add(contentPanel, BorderLayout.CENTER);
        myFileInfoPanel.add(compositePanel, BorderLayout.SOUTH);

        mySourceTileListener = createSourceTileListener();
    }

    /**
     * Builds the file dir panel.
     *
     * @return the j panel
     */
    private JPanel buildFileDirPanel()
    {
        JPanel fileDirPanel = new JPanel(new BorderLayout());
        fileDirPanel.add(new JLabel("File Directory "), BorderLayout.WEST);
        fileDirPanel.add(myFileDirTF, BorderLayout.CENTER);
        fileDirPanel.add(myReadOnlyDirLB, BorderLayout.EAST);
        fileDirPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 200));
        return fileDirPanel;
    }

    /**
     * Builds the file list panel.
     *
     * @return the j panel
     */
    private JPanel buildFileListPanel()
    {
        JPanel fileListPanel = new JPanel(new BorderLayout());
        fileListPanel.add(new ImageInfoHeaderTilePanel(), BorderLayout.NORTH);
        fileListPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        myFileListPanel = new JPanel();
        myFileListPanel.setLayout(new BoxLayout(myFileListPanel, BoxLayout.Y_AXIS));

        JScrollPane jsp1 = new JScrollPane(myFileListPanel);
        fileListPanel.add(jsp1, BorderLayout.CENTER);
        fileListPanel.setPreferredSize(new Dimension(800, 250));
        fileListPanel.setMaximumSize(new Dimension(800, 250));
        return fileListPanel;
    }

    /**
     * Builds the import choice panel.
     *
     * @param mainPanel the main panel
     */
    private void buildImportChoicePanel(JPanel mainPanel)
    {
        myImportChoicePanel = new JPanel(new BorderLayout());
        myImportChoicePanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        myImportChoicePanel.setPreferredSize(new Dimension(900, 80));
        myImportChoicePanel.setMinimumSize(new Dimension(900, 80));
        myImportChoicePanel.setMaximumSize(new Dimension(4000, 880));
        JPanel importChoiceInnerPanel = new JPanel(new BorderLayout());
        importChoiceInnerPanel.setBorder(BorderFactory.createEmptyBorder());
        myImportGroupChooserCombo = new JComboBox<>(New.array(myInactiveNamesInUseList, String.class));
        myImportGroupChooserCombo.addActionListener(arg0 -> validateImportTypeStage());
        importChoiceInnerPanel.add(new JLabel("Group"), BorderLayout.WEST);
        importChoiceInnerPanel.add(myImportGroupChooserCombo, BorderLayout.CENTER);
        myImportChoicePanel.add(importChoiceInnerPanel, BorderLayout.CENTER);
        myImportChoicePanel.setVisible(false);
        mainPanel.add(myImportChoicePanel);
    }

    /**
     * Builds the import name panel.
     *
     * @return the j panel
     */
    private JPanel buildImportNamePanel()
    {
        myImportNamePanel = new JPanel(new BorderLayout());
        myImportNamePanel.setBorder(BorderFactory.createEmptyBorder(20, 150, 20, 150));
        myImportNamePanel.setPreferredSize(new Dimension(900, 100));
        myImportNamePanel.setMinimumSize(new Dimension(900, 100));
        myImportNamePanel.setMaximumSize(new Dimension(4000, 100));
        JPanel innerNamePanel = new JPanel(new BorderLayout());
        myImportNameErrorLb = new JLabel("VALID");
        myImportNameErrorLb.setForeground(Color.green);
        myImportNameErrorLb.setHorizontalAlignment(JLabel.CENTER);
        myImportNameErrorLb.setHorizontalTextPosition(JLabel.CENTER);
        myImportGroupNameTF = new JTextField();
        myImportGroupNameTF.setText(getGroupName());
        myImportGroupNameTF.getDocument().addDocumentListener(new DocumentListener()
        {
            @Override
            public void changedUpdate(DocumentEvent e)
            {
            }

            @Override
            public void insertUpdate(DocumentEvent e)
            {
                validateImportTypeStage();
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                validateImportTypeStage();
            }
        });
        return innerNamePanel;
    }

    /**
     * Gets the initial group name with which to populate the text field.
     *
     * @return the group name
     */
    private String getGroupName()
    {
        return myImportSources.size() == 1 ? myImportSources.get(0).getName() : new File(myFileDirTF.getText()).getName();
    }

    /**
     * Builds the import type panel.
     *
     * @return the j panel
     */
    private JPanel buildImportTypePanel()
    {
        JPanel importTypePanel = new JPanel(new BorderLayout());
        importTypePanel.setBorder(BorderFactory.createEmptyBorder(20, 250, 20, 250));
        importTypePanel.setPreferredSize(new Dimension(900, 80));
        importTypePanel.setMinimumSize(new Dimension(900, 80));
        importTypePanel.setMaximumSize(new Dimension(4000, 80));
        importTypePanel.add(myImportTypeCombo, BorderLayout.CENTER);
        return importTypePanel;
    }

    /**
     * Builds the pb panel1.
     *
     * @return the j panel
     */
    private JPanel buildPbPanel1()
    {
        JPanel pbPanel1 = new JPanel(new BorderLayout());
        myOvFilesToProcNoteLB = new JLabel("Processing File X of Y");
        myOvFilesToProcessProgBar = new JProgressBar();
        myOvFilesToProcessProgBar.setPreferredSize(new Dimension(800, 30));
        myOvFilesToProcessProgBar.setMinimumSize(new Dimension(800, 30));
        myOvFilesToProcessProgBar.setMaximumSize(new Dimension(4000, 30));
        pbPanel1.add(myOvFilesToProcNoteLB, BorderLayout.NORTH);
        JPanel pbPanel1Inner = new JPanel(new BorderLayout());
        pbPanel1Inner.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        pbPanel1Inner.add(myOvFilesToProcessProgBar, BorderLayout.CENTER);
        pbPanel1.add(pbPanel1Inner, BorderLayout.CENTER);
        return pbPanel1;
    }

    /**
     * Build pb panel2.
     *
     * @return the j panel
     */
    private JPanel buildpbPanel2()
    {
        JPanel pbPanel2 = new JPanel(new BorderLayout());
        myOvGenerationNoteLB = new JLabel("Generating Overview for File");
        myOvGenerationProgBar = new JProgressBar();
        myOvGenerationProgBar.setPreferredSize(new Dimension(800, 30));
        myOvGenerationProgBar.setMinimumSize(new Dimension(800, 30));
        myOvGenerationProgBar.setMaximumSize(new Dimension(4000, 30));
        pbPanel2.add(myOvGenerationNoteLB, BorderLayout.NORTH);
        JPanel pbPanel2Inner = new JPanel(new BorderLayout());
        pbPanel2Inner.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        pbPanel2Inner.add(myOvGenerationProgBar, BorderLayout.CENTER);
        pbPanel2.add(pbPanel2Inner, BorderLayout.CENTER);
        return pbPanel2;
    }

    /**
     * Builds the top label.
     *
     * @param mainPanel the main panel
     */
    private void buildTopLabel(JPanel mainPanel)
    {
        JLabel topLabel = new JLabel("Import Group Options...");
        topLabel.setFont(topLabel.getFont().deriveFont(Font.BOLD, 20));
        topLabel.setHorizontalTextPosition(JLabel.CENTER);
        topLabel.setHorizontalAlignment(JLabel.CENTER);
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setPreferredSize(new Dimension(900, 80));
        topPanel.setMinimumSize(new Dimension(900, 80));
        topPanel.setMaximumSize(new Dimension(4000, 80));
        topPanel.add(topLabel, BorderLayout.CENTER);
        mainPanel.add(topPanel);
    }

    /**
     * Creates the file info wizard panel.
     *
     * @param setupWithFiles the setup with files
     * @return the j panel
     */
    private JPanel createFileInfoPanel(boolean setupWithFiles)
    {
        myFileInfoPanel = new JPanel();
        myFileInfoPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JLabel topLabel = new JLabel("Image File Import Settings");
        topLabel.setFont(topLabel.getFont().deriveFont(Font.BOLD, 20));
        topLabel.setHorizontalTextPosition(JLabel.CENTER);
        topLabel.setHorizontalAlignment(JLabel.CENTER);
        myFileDirTF = new JTextField();
        myFileDirTF.setEditable(false);

        myReadOnlyDirLB = new JLabel("  READ ONLY");
        myReadOnlyDirLB.setForeground(Color.yellow);
        myReadOnlyDirLB.setVisible(false);

        JPanel fileDirPanel = buildFileDirPanel();
        JPanel fileListPanel = buildFileListPanel();

        JPanel previewAreaPanel = new JPanel();
        previewAreaPanel.setLayout(new BorderLayout());

        myErrorLabel = new JLabel("All Settings Valid");
        // myErrorLabel.setVisible(false);
        myErrorLabel.setFont(myErrorLabel.getFont().deriveFont(Font.BOLD));
        myErrorLabel.setForeground(Color.green);
        myErrorLabel.setHorizontalTextPosition(JLabel.CENTER);
        myErrorLabel.setHorizontalAlignment(JLabel.CENTER);

        myIgnoreZerosToggleBt = new JToggleButton("Ignore 0's", false);
        myIgnoreZerosToggleBt.setFocusable(false);
        myIgnoreZerosToggleBt.setToolTipText("Toggles all images to ignore zeros");
        myIgnoreZerosToggleBt.setMargin(new Insets(2, 5, 2, 5));
        myIgnoreZerosToggleBt.addActionListener(createIgnoreZerosToggleButtonActionListener());
        JPanel togglePanel = new JPanel(new BorderLayout());
        togglePanel.add(myIgnoreZerosToggleBt, BorderLayout.CENTER);
        togglePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));

        JPanel errorPanel = new JPanel(new BorderLayout());
        errorPanel.add(myErrorLabel, BorderLayout.CENTER);
        errorPanel.add(togglePanel, BorderLayout.EAST);

        JPanel btPanel = new JPanel(new BorderLayout());
        btPanel.add(errorPanel, BorderLayout.NORTH);

        myFilePreviewTA = new JTextArea();
        myFilePreviewTA.setEditable(false);
        JScrollPane jsp = new JScrollPane(myFilePreviewTA);
        previewAreaPanel.add(jsp, BorderLayout.CENTER);
        previewAreaPanel.setPreferredSize(new Dimension(800, 200));
        previewAreaPanel.setMaximumSize(new Dimension(800, 200));
        previewAreaPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20),
                BorderFactory.createTitledBorder("File Info")));

        btPanel.add(previewAreaPanel, BorderLayout.CENTER);

        buildAssist1(topLabel, fileDirPanel, fileListPanel, btPanel);
        if (setupWithFiles)
        {
            setupWithChosenFiles();
        }
        rebuildSourceListPanel();
        return myFileInfoPanel;
    }

    /**
     * Creates the ignore zeros toggle button action listener.
     *
     * @return the action listener
     */
    private ActionListener createIgnoreZerosToggleButtonActionListener()
    {
        return new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (myFileListPanel.getComponents() != null && myFileListPanel.getComponents().length > 0)
                {
                    for (Component comp : myFileListPanel.getComponents())
                    {
                        if (comp instanceof ImageInfoTilePanel)
                        {
                            ((ImageInfoTilePanel)comp).setIgnoreZeros(myIgnoreZerosToggleBt.isSelected());
                            ((ImageInfoTilePanel)comp).saveState();
                        }
                    }
                }
            }
        };
    }

    /**
     * Creates the import type stage panel for the wizard.
     */
    private void createImportTypePanel()
    {
        myImportTypeStagePanel = new JPanel(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        buildTopLabel(mainPanel);

        myImportTypeCombo = new JComboBox<>(new String[] { IMPORT_AS_NEW_GROUP, IMPORT_ADD_TO_GROUP, });
        myImportTypeCombo.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                myImportNamePanel.setVisible(myImportTypeCombo.getSelectedItem() == IMPORT_AS_NEW_GROUP);
                myImportChoicePanel.setVisible(myImportTypeCombo.getSelectedItem() != IMPORT_AS_NEW_GROUP);
                validateImportTypeStage();
            }
        });
        if (myInactiveNamesInUseList.isEmpty())
        {
            myImportTypeCombo.setEnabled(false);
        }
        JPanel importTypePanel = buildImportTypePanel();
        mainPanel.add(importTypePanel);

        JPanel innerNamePanel = buildImportNamePanel();
        JLabel grpNameLb = new JLabel("New Group Name");
        innerNamePanel.add(grpNameLb, BorderLayout.NORTH);
        innerNamePanel.add(myImportGroupNameTF, BorderLayout.CENTER);
        innerNamePanel.add(myImportNameErrorLb, BorderLayout.SOUTH);
        myImportNamePanel.add(innerNamePanel, BorderLayout.CENTER);
        mainPanel.add(myImportNamePanel);

        buildImportChoicePanel(mainPanel);

        mainPanel.add(Box.createVerticalGlue());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2, 10, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 550, 10, 20));
        myImportOkBt = new JButton("OK");
        myImportOkBt.setVisible(false);
        myImportOkBt.addActionListener(e -> completeWizard(true));

        myImportCancelBt = new JButton("Cancel");
        myImportCancelBt.addActionListener(actionevent -> completeWizard(false));
        buttonPanel.add(myImportOkBt);
        buttonPanel.add(myImportCancelBt);

        myImportTypeStagePanel.add(mainPanel, BorderLayout.CENTER);
        myImportTypeStagePanel.add(buttonPanel, BorderLayout.SOUTH);
        validateImportTypeStage();
    }

    /**
     * Creates the ok button action listener.
     *
     * @return the action listener
     */
    private ActionListener createOKButtonActionListener()
    {
        return new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (myStage == Stage.SETTINGS_STAGE)
                {
                    boolean needsOverviewImport = false;
                    for (ImageryFileSource src : myImportSources)
                    {
                        if (src.isCreateOverviews())
                        {
                            needsOverviewImport = true;
                            break;
                        }
                    }

                    if (needsOverviewImport)
                    {
                        switchToPanel(myOverviewStagePanel);

                        CreateOverviewsWorker worker = new CreateOverviewsWorker(ImagerySourceWizardPanel.this,
                                myOvFilesToProcessProgBar, myOvGenerationProgBar, myOvFilesToProcNoteLB, myOvGenerationNoteLB,
                                myOvProcTA, myImportSources, mySourceToDatasetMap);

                        Thread t = new Thread(worker);
                        t.start();
                    }
                    else
                    {
                        myImportGroupNameTF.setText(getGroupName());
                        switchToPanel(myImportTypeStagePanel);
                    }
                }
            }
        };
    }

    /**
     * Creates the overview stage panel for the wizard.
     */
    private void createOverviewGenPanel()
    {
        myOverviewStagePanel = new JPanel();
        myOverviewStagePanel.setLayout(new BorderLayout());

        JLabel topLabel = new JLabel("Creating Overviews...");
        topLabel.setFont(topLabel.getFont().deriveFont(Font.BOLD, 20));
        topLabel.setHorizontalTextPosition(JLabel.CENTER);
        topLabel.setHorizontalAlignment(JLabel.CENTER);
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(topLabel, BorderLayout.CENTER);

        JPanel progBarPanel = new JPanel();
        progBarPanel.setPreferredSize(new Dimension(900, 180));
        progBarPanel.setMinimumSize(new Dimension(900, 180));
        progBarPanel.setMaximumSize(new Dimension(4000, 180));
        progBarPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JPanel pbPanel1 = buildPbPanel1();

        JPanel pbPanel2 = buildpbPanel2();

        progBarPanel.setLayout(new GridLayout(2, 1, 0, 10));
        progBarPanel.add(pbPanel1);
        progBarPanel.add(pbPanel2);

        JPanel procOutputPanel = new JPanel(new BorderLayout());
        myOvProcTA = new JTextArea();
        myOvProcTA.setEditable(false);
        JScrollPane jsp = new JScrollPane(myOvProcTA);
        procOutputPanel.add(jsp, BorderLayout.CENTER);
        procOutputPanel.setPreferredSize(new Dimension(800, 400));
        procOutputPanel.setMaximumSize(new Dimension(800, 500));
        procOutputPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20),
                BorderFactory.createTitledBorder("Output")));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2, 10, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 550, 10, 20));
        myOvOkBt = new JButton("OK");
        myOvOkBt.setVisible(false);
        myOvOkBt.addActionListener(e ->
        {
            myImportGroupNameTF.setText(getGroupName());
            switchToPanel(myImportTypeStagePanel);
        });

        myOvCancelBt = new JButton("Cancel");
        myOvCancelBt.addActionListener(actionevent -> completeWizard(false));
        buttonPanel.add(myOvOkBt);
        buttonPanel.add(myOvCancelBt);

        JPanel mainLayoutPanel = new JPanel();
        mainLayoutPanel.setLayout(new BoxLayout(mainLayoutPanel, BoxLayout.Y_AXIS));
        mainLayoutPanel.add(topPanel);
        mainLayoutPanel.add(progBarPanel);
        mainLayoutPanel.add(procOutputPanel);

        myOverviewStagePanel.add(mainLayoutPanel, BorderLayout.CENTER);
        myOverviewStagePanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Creates the source tile listener.
     *
     * @return the action listener
     */
    private ActionListener createSourceTileListener()
    {
        return new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                if (evt.getActionCommand() == Constants.REFRESH_FILE_INFO_AREA)
                {
                    myFilePreviewTA.setText(evt.getSource().toString());
                    myFilePreviewTA.setCaretPosition(0);
                }
                else if (evt.getActionCommand() == Constants.REMOVE_SELECTED_IMPORT_FILE)
                {
                    myImportSources.remove(((ImageInfoTilePanel)evt.getSource()).getSource());
                    rebuildSourceListPanel();
                    validateImportSettings();
                }
                else if (evt.getActionCommand() == Constants.REVALIDATE_TILE_CONTENT)
                {
                    validateImportSettings();
                }
            }
        };
    }

    /**
     * Handle first case.
     *
     * @param aFile the a file
     */
    private void handleFirstCase(File aFile)
    {
        myFileDirTF.setText(aFile.getParentFile().getAbsolutePath());

        // Test to see if we can write into the directory from which we are
        // reading files. If not we need to know because in that case we won't
        // be able to create overview files.
        StringBuilder path = new StringBuilder();
        path.append(aFile.getParentFile().getAbsolutePath()).append(File.separator).append("testFile")
                .append(System.currentTimeMillis()).append(".tst");
        File tmpFile = new File(path.toString());
        try
        {
            tmpFile.createNewFile();
        }
        catch (IOException e1)
        {
            myReadOnlyDirLB.setVisible(true);
            myReadOnlyDir = true;
        }

        // If we created the temp file then we can write to the directory so
        // clean it up and set the flags for our read only warning.
        if (tmpFile.exists())
        {
            if (!tmpFile.delete())
            {
                LOGGER.error("Failed to delete temp file: " + tmpFile.getAbsolutePath());
            }
            myReadOnlyDirLB.setVisible(false);
            myReadOnlyDir = false;
        }
    }

    /**
     * Rebuilds the list of sources for the first import stage.
     */
    private void rebuildSourceListPanel()
    {
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                myImageInfoTilePanels.clear();
                myFileListPanel.removeAll();

                for (ImageryFileSource src : myImportSources)
                {
                    ImageInfoTilePanel tile = new ImageInfoTilePanel(src, ImagerySourceWizardPanel.this, mySourceTileListener,
                            myImportSources.size() > 1, myReadOnlyDir);
                    myFileListPanel.add(tile);
                    myImageInfoTilePanels.add(tile);
                }

                myFileListPanel.add(Box.createVerticalGlue());
                myFileListPanel.revalidate();
                myFileListPanel.repaint();
                validateImportSettings();
            }
        });
    }

    /**
     * Setup with chosen files.
     */
    private void setupWithChosenFiles()
    {
        boolean first = true;
        for (File aFile : myChoosenFiles)
        {
            String name = aFile.getName();
            int dotIndex = name.lastIndexOf('.');
            if (dotIndex != -1)
            {
                name = name.substring(0, dotIndex);
            }

            ImageryFileSource src = new ImageryFileSource(aFile);
            src.setName(name);

            Dataset ds = GDALTools.getDataSet(aFile);

            SpatialReference hSRS;
            String pszProjection;

            if (ds != null)
            {
                mySourceToDatasetMap.put(src, ds);
                pszProjection = ds.GetProjectionRef();
                if (pszProjection == null || pszProjection.length() == 0)
                {
                    pszProjection = ds.GetGCPProjection();
                }
                hSRS = new SpatialReference(pszProjection);

                if (pszProjection.length() != 0)
                {
                    src.setProjection(hSRS.GetAttrValue("GEOGCS"));
                    src.setDatum(hSRS.GetAttrValue("DATUM"));
                    hSRS.delete();
                }
                else
                {
                    src.setDescription("Coordinate System is `" + ds.GetProjectionRef() + "'");
                }

                GDALTools gt = new GDALTools(aFile, null);
                GDALImageLayerData layerData = gt.retrieveGDALImageLayerDataOriginal(aFile, 512);

                src.setBands(ds.getRasterCount());
                src.setDescription(GDALInfo.getInfoPrint(ds));
                double[] adfGeoTransform = new double[6];
                ds.GetGeoTransform(adfGeoTransform);
                src.setPixelSizeLat(adfGeoTransform[1]);
                src.setPixelSizeLon(adfGeoTransform[5]);
                src.setXResolution(ds.getRasterXSize());
                src.setYResolution(ds.getRasterYSize());

                int ovCount = 0;
                for (int i = 0; i < ds.getRasterCount(); i++)
                {
                    ovCount += ds.GetRasterBand(i + 1).GetOverviewCount();
                }
                src.setHasOverviews(ovCount > 0);

                src.setUpperLeftLat(layerData.getSector().getUpperLeft().getLatLonAlt().getLatD());
                src.setUpperLeftLon(layerData.getSector().getUpperLeft().getLatLonAlt().getLonD());
                src.setLowerRightLat(layerData.getSector().getLowerRight().getLatLonAlt().getLatD());
                src.setLowerRightLon(layerData.getSector().getLowerRight().getLatLonAlt().getLonD());
                ds = null;
            }

            if (first)
            {
                handleFirstCase(aFile);
            }
            first = false;

            myImportSources.add(src);
        }
    }

    /**
     * Switches the current wizard panel to the desired panel.
     *
     * @param panel the panel
     */
    private void switchToPanel(final JPanel panel)
    {
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                myOwner.removeAll();
                myOwner.add(panel, BorderLayout.CENTER);
                ((JPanel)myOwner).revalidate();
                myOwner.repaint();
            }
        });
    }

    /**
     * The Enum Stage.
     */
    private enum Stage
    {
        /** The COMPLETE_STAGE. */
        COMPLETE_STAGE,

        /** The CREATE_OVERVIEWS_STAGE. */
        CREATE_OVERVIEWS_STAGE,

        /** The SETTINGS_STAGE. */
        SETTINGS_STAGE
    }
}
