package io.opensphere.imagery;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.gdal.gdal.Dataset;
import org.jdesktop.swingx.JXTable;

import io.opensphere.controlpanels.layers.event.ShowGroupLayerDetailsEvent;
import io.opensphere.controlpanels.layers.layerdetail.LayerDetailPanel;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.util.lang.PhasedTaskCanceller;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.imagery.gdal.GDALInfo;
import io.opensphere.imagery.gdal.GDALTools;
import io.opensphere.mantle.data.AbstractActivationListener;
import io.opensphere.mantle.data.ActivationListener;
import io.opensphere.mantle.data.ActivationState;
import io.opensphere.mantle.data.DataGroupActivationProperty;
import io.opensphere.mantle.datasources.DataSourceChangeEvent;
import io.opensphere.mantle.datasources.DataSourceChangeListener;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.mantle.util.TextViewDialog;

/**
 * The Class ImageryGroupSourceEditorPanel.
 */
@SuppressWarnings({ "serial", "PMD.GodClass" })
public class ImageryGroupImageManagerPanel extends JPanel
        implements ActionListener, DataSourceChangeListener, ListSelectionListener
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ImageryGroupImageManagerPanel.class);

    /** The Accept button. */
    private JButton myAcceptButton;

    /** The Cancel button. */
    private JButton myCancelButton;

    /** The Controller. */
    private final transient ImageryFileSourceController myController;

    /** The Current file source. */
    private transient ImageryFileSource myCurrentFileSource;

    /** The Deactivate to change label. */
    private JLabel myDeactivateToChangeLabel;

    /** The Disable selected button. */
    private JButton myDisableSelectedBT;

    /** The Enable selected button. */
    private JButton myEnableSelectedBT;

    /** The File table. */
    private JXTable myFileTable;

    /** The File table model. */
    private ImageFileTableModel myFileTableModel;

    /** The Has name error. */
    private boolean myHasNameError;

    /** The Id to last selected row map. */
    private final Map<String, Integer> myIdToLastSelectedRowMap;

    /** The Imagery settings panel. */
    private final ImagerySettingsPanel myImagerySettingsPanel;

    /** The LL panel change listener. */
    private transient ChangeListener myLLPanelChangeListener;

    /** The Name field. */
    private JTextField myNameField;

    /** The Name label. */
    private JLabel myNameLabel;

    /** The Original source. */
    private transient ImagerySourceGroup myOriginalSource;

    /** The Other file sources. */
    private final transient Map<String, ImagerySourceGroup> myOtherFileSources;

    /** The Owner. */
    private final JDialog myOwner;

    /** The Remove selected button. */
    private JButton myRemoveSelectedBT;

    /** The Source center on button. */
    private JButton mySourceCenterOnBT;

    /** The Source editor panel. */
    private JPanel mySourceEditorPanel;

    /** The Source error message. */
    private JLabel mySourceErrorMessage;

    /** The Source file text field. */
    private JTextField mySourceFileTF;

    /** The Source ignore zeros check box. */
    private JCheckBox mySourceIgnoreZerosCheckBox;

    /** The Source info button. */
    private JButton mySourceInfoBT;

    /** The Source lat lon panel. */
    private LatLongCornerPanel mySourceLLPanel;

    /** The Source name doc listener. */
    private transient DocumentListener mySourceNameDocListener;

    /** The Source name panel. */
    private JPanel mySourceNamePanel;

    /** The Source name text field. */
    private JTextField mySourceNameTF;

    /** The Source on check box. */
    private JCheckBox mySourceOnCheckBox;

    /** The Source zoom to button. */
    private JButton mySourceZoomToBT;

    /** The Starting name. */
    private String myStartingName;

    /** The Has changed. */
    private boolean myStateHasChanged;

    /** The Toolbox. */
    private final transient Toolbox myToolbox;

    /** The activation listener. */
    private final transient ActivationListener myActivationListener = new AbstractActivationListener()
    {
        @Override
        public void commit(DataGroupActivationProperty property, ActivationState state, PhasedTaskCanceller canceller)
        {
            updateBasedOnActivity();
        }
    };

    /**
     * Clean cache for source.
     *
     * @param source the source
     * @param ignoreZerosChange the ignore zeros change
     * @param tb the tb
     */
    private static void cleanCacheForSource(ImageryFileSource source, boolean ignoreZerosChange, Toolbox tb)
    {
        ImagerySourceGroup group = source.getGroup();
        if (group != null)
        {
            ImageryEnvoy envoy = group.getImageryEnvoy();
            if (envoy != null)
            {
                envoy.clearImageCache(Collections.singleton(source.generateTypeKey()));
            }
            else
            {
                group.cleanCache(tb);
            }
            LOGGER.info("Cache clean is complete.");
        }
    }

    /**
     * Clean cache for sources.
     *
     * @param c the c
     * @param srcList the src list
     * @param tb the tb
     */
    private static void cleanCacheForSources(Component c, List<ImageryFileSource> srcList, Toolbox tb)
    {
        ImagerySourceGroup group = null;
        Set<String> keySet = New.set();
        for (ImageryFileSource src : srcList)
        {
            if (group == null)
            {
                group = src.getGroup();
            }
            keySet.add(src.generateTypeKey());
        }

        if (keySet.isEmpty())
        {
            JOptionPane.showMessageDialog(SwingUtilities.getRootPane(c), "You must select at least one image.",
                    "No Image Selected", JOptionPane.ERROR_MESSAGE);
        }
        else
        {
            if (group != null)
            {
                ImageryEnvoy iEnvoy = group.getImageryEnvoy();
                if (iEnvoy != null)
                {
                    iEnvoy.clearImageCache(keySet);
                }
                else
                {
                    group.cleanCache(tb);
                }
                LOGGER.info("Cache clean is complete for selected images.");
            }
        }
    }

    /**
     * Instantiates a new advanced image group source editor panel.
     *
     * @param settingsPanel the settings panel
     * @param controller the controller
     * @param owner the owner
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public ImageryGroupImageManagerPanel(ImagerySettingsPanel settingsPanel, ImageryFileSourceController controller,
            JDialog owner)
    {
        super();
        myImagerySettingsPanel = settingsPanel;
        myController = controller;
        myOwner = owner;
        myToolbox = myController.getToolbox();
        myOtherFileSources = new HashMap<>();
        myIdToLastSelectedRowMap = new HashMap<>();
        init();
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        myStateHasChanged = true;
        fireChanged();
    }

    /**
     * Helper function to create fixed width panels with a right-justified
     * label.
     *
     * @param label the label
     * @param width the width of the label panel
     * @return the panel.
     */
    public JPanel createLabelPanel(JLabel label, int width)
    {
        JPanel aPanel = new JPanel();
        aPanel.setLayout(new BorderLayout());
        label.setText(label.getText() + " ");
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        aPanel.add(label, BorderLayout.CENTER);
        aPanel.setMinimumSize(new Dimension(width, 20));
        aPanel.setMaximumSize(new Dimension(width, 20));
        aPanel.setPreferredSize(new Dimension(width, 20));
        return aPanel;
    }

    @Override
    public void dataSourceChanged(DataSourceChangeEvent evt)
    {
        EventQueueUtilities.runOnEDT(this::updateBasedOnActivity);
    }

    /**
     * Fire changed.
     */
    public void fireChanged()
    {
        myAcceptButton.setEnabled(isEditorValid());
    }

    /**
     * Checks for changed.
     *
     * @return true, if successful
     */
    public boolean hasChanged()
    {
        return myStateHasChanged;
    }

    /**
     * Checks if is editor valid.
     *
     * @return true, if is editor valid
     */
    public boolean isEditorValid()
    {
        boolean error = false;

        for (ImageryFileSource src : myFileTableModel.getSources())
        {
            if (!error && src.isEnabled())
            {
                if (src.getLowerRightLat() == src.getUpperLeftLat())
                {
                    int row = myFileTableModel.getRow(src);
                    if (row != -1)
                    {
                        row = myFileTable.convertRowIndexToView(row);
                        myFileTable.setRowSelectionInterval(row, row);
                    }
                    error = true;
                    JOptionPane.showMessageDialog(SwingUtilities.getRootPane(this),
                            "ERROR: The upper left and lower right latitudes for \n the image \"" + src.getName()
                                    + "\" are identical.\n\nPlease correct the images corner points or disable the image.\n",
                            "Image Configuration Validation Error", JOptionPane.ERROR_MESSAGE);

                    break;
                }
                else if (src.getLowerRightLon() == src.getUpperLeftLon())
                {
                    int row = myFileTableModel.getRow(src);
                    if (row != -1)
                    {
                        row = myFileTable.convertRowIndexToView(row);
                        myFileTable.setRowSelectionInterval(row, row);
                    }
                    error = true;
                    JOptionPane.showMessageDialog(SwingUtilities.getRootPane(this),
                            "ERROR: The upper left and lower right longitudes for \n the image \"" + src.getName()
                                    + "\" are identical.\n\nPlease correct the images corner points or disable the image.\n",
                            "Image Configuration Validation Error", JOptionPane.ERROR_MESSAGE);
                    break;
                }
            }
        }

        if (!error && myHasNameError)
        {
            error = true;
            JOptionPane.showMessageDialog(SwingUtilities.getRootPane(this),
                    "One or more file names are in conflict\n\nPlease resolve the conflict.", "Name Conflict Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        if (!error)
        {
            // Do some validation checking on the name and file field
            if (StringUtils.isBlank(myNameField.getText()))
            {
                JOptionPane.showMessageDialog(SwingUtilities.getRootPane(this), "You must enter a name for your image group.",
                        "Input Validation Error", JOptionPane.ERROR_MESSAGE);
                error = true;
            }
            else
            {
                String name = myNameField.getText();
                boolean nameInUse = myOtherFileSources.containsKey(name);
                boolean nameIsAlreadyMine = name.equals(myStartingName);

                if (nameInUse && !nameIsAlreadyMine)
                {
                    JOptionPane.showMessageDialog(SwingUtilities.getRootPane(this),
                            "You must input a unique name for your image group.\n\"" + name + "\" is already in use.",
                            "Duplicate Name Error", JOptionPane.ERROR_MESSAGE);
                    error = true;
                }
            }
        }

        return !error;
    }

    /**
     * Sets the changed.
     */
    public void setChanged()
    {
        myStateHasChanged = true;
        fireChanged();
    }

    /**
     * Sets this editor's values provided an {@link ImagerySourceGroup}.
     *
     * @param source the source
     */
    public void setFromSource(ImagerySourceGroup source)
    {
        myOriginalSource = source;
        source.getDataGroupInfo().activationProperty().addListener(myActivationListener);
        myStartingName = source.getName();
        myNameField.setText(source.getName());
        myNameField.setEditable(false);
        myRemoveSelectedBT.setVisible(!source.isActive());
        myEnableSelectedBT.setVisible(!source.isActive());
        myDisableSelectedBT.setVisible(!source.isActive());
        mySourceErrorMessage.setVisible(false);
        mySourceNamePanel.setBorder(BorderFactory.createEmptyBorder());
        myHasNameError = false;
        myFileTableModel.setSources(source.getImageSources());
        myFileTable.packAll();

        if (myIdToLastSelectedRowMap.get(source.getId()) != null && myIdToLastSelectedRowMap.get(source.getId()).intValue() != -1)
        {
            int row = myIdToLastSelectedRowMap.get(source.getId()).intValue();
            myFileTable.getSelectionModel().setValueIsAdjusting(true);
            myFileTable.getSelectionModel().setSelectionInterval(row, row);
            myFileTable.getSelectionModel().setValueIsAdjusting(false);
        }

        updateBasedOnActivity();
    }

    /**
     * Update and save source.
     */
    public void updateAndSaveSource()
    {
        LayerDetailPanel ldp = findLayerDetailsPanel(myImagerySettingsPanel);
        if (ldp != null)
        {
            ldp.setNullAndHide();
        }

        myController.removeSource(myOriginalSource, true, this);

        ImagerySourceGroup changedSource = new ImagerySourceGroup();

        // Set equal to the original to get everything needed
        changedSource.setEqualTo(myOriginalSource);

        List<ImageryFileSource> srcList = myFileTableModel.getSources();
        List<ImageryFileSource> destList = new ArrayList<>();
        for (ImageryFileSource toCpySrc : srcList)
        {
            ImageryFileSource copy = new ImageryFileSource(toCpySrc);
            copy.setDataTypeInfo(toCpySrc.getDataTypeInfo());
            copy.setGroup(toCpySrc.getGroup());
            destList.add(copy);
        }
        changedSource.updateSourceGroupReferences();
        changedSource.getImageSources().clear();
        changedSource.getImageSources().addAll(destList);
        changedSource.setName(myNameField.getText());
        myOriginalSource.setEqualTo(changedSource);
        myOriginalSource.getImageSources().clear();
        myOriginalSource.getImageSources().addAll(destList);
        myController.addSource(myOriginalSource);

        myToolbox.getEventManager().publishEvent(
                new ShowGroupLayerDetailsEvent(myOriginalSource.getDataGroupInfo().getId(), LayerDetailPanel.SETTINGS_TAB));
    }

    @Override
    public void valueChanged(ListSelectionEvent arg0)
    {
        if (!arg0.getValueIsAdjusting())
        {
            ImageryFileSource srcToEdit = null;
            int[] rowsSelected = myFileTable.getSelectedRows();
            int index = myFileTable.getSelectedRow();
            // myCurrentFileSource = null;
            if (index != -1 && rowsSelected != null && rowsSelected.length == 1)
            {
                int modelRowIndex = myFileTable.convertRowIndexToModel(index);
                srcToEdit = myFileTableModel.getSourceAtRow(modelRowIndex);
            }
            if (!Utilities.sameInstance(myCurrentFileSource, srcToEdit))
            {
                myCurrentFileSource = srcToEdit;

                if (myCurrentFileSource != null)
                {
                    myIdToLastSelectedRowMap.put(myOriginalSource.getId(), Integer.valueOf(index));
                }

                updateSourcePanel();
            }
        }
    }

    /**
     * Enables/disables the images selected in the table or shows an error if
     * nothing is selected.
     *
     * @param enable the enable
     */
    protected void enableDisableSelectedImages(boolean enable)
    {
        int[] rowsSelected = myFileTable.getSelectedRows();
        if (rowsSelected != null && rowsSelected.length > 0)
        {
            for (int row : rowsSelected)
            {
                ImageryFileSource src = myFileTableModel.getSourceAtRow(myFileTable.convertRowIndexToModel(row));
                if (src != null)
                {
                    src.setEnabled(enable);
                }
            }

            myIdToLastSelectedRowMap.put(myOriginalSource.getId(), Integer.valueOf(-1));
            myCurrentFileSource = null;
            myFileTableModel.fireTableDataChanged();
            myStateHasChanged = true;
            fireChanged();
        }
        else
        {
            String val = enable ? "enable" : "disable";
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(myRemoveSelectedBT),
                    "You must first select at least one image to " + val, "Selection Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Checks to see if the name for a source is already in use in this group or
     * other groups.
     *
     * @param name - the name to check
     * @param excludeThis - the source to exclude from checking
     * @return null if not in use, error message if in use
     */
    protected String isSourceNameInUse(String name, IDataSource excludeThis)
    {
        String inUseMessage = null;

        if (myFileTableModel != null && myFileTableModel.getSources() != null)
        {
            for (IDataSource src : myFileTableModel.getSources())
            {
                if (Utilities.sameInstance(src, excludeThis))
                {
                    continue;
                }

                if (name.equals(src.getName()))
                {
                    inUseMessage = "The name \"" + name + "\" is already in use in this group.";
                    break;
                }
            }
        }

        // Check all the other groups for name conflicts
        if (inUseMessage == null && myOtherFileSources != null)
        {
            for (Map.Entry<String, ImagerySourceGroup> entry : myOtherFileSources.entrySet())
            {
                ImagerySourceGroup srcGrp = entry.getValue();

                if (!EqualsHelper.equals(srcGrp, myOriginalSource) && srcGrp.getImageSources() != null
                        && srcGrp.getImageSources().size() > 0)
                {
                    for (ImageryFileSource src : srcGrp.getImageSources())
                    {
                        if (name.equals(src.getName()))
                        {
                            inUseMessage = "The name \"" + name + "\" is already in use in another group: " + srcGrp.getName();
                            break;
                        }
                    }
                }
                if (inUseMessage != null)
                {
                    break;
                }
            }
        }

        return inUseMessage;
    }

    /**
     * updates the individual source panel editor with the current editor values
     * or hides/enables/disables based on the state of the source and selection.
     */
    protected void updateSourcePanel()
    {
        mySourceEditorPanel.setVisible(myCurrentFileSource != null);
        mySourceLLPanel.removeChangeListener(myLLPanelChangeListener);
        mySourceNameTF.getDocument().removeDocumentListener(mySourceNameDocListener);

        if (myCurrentFileSource != null)
        {
            mySourceLLPanel.setULLat(myCurrentFileSource.getUpperLeftLat());
            mySourceLLPanel.setLRLong(myCurrentFileSource.getLowerRightLon());
            mySourceLLPanel.setLRLat(myCurrentFileSource.getLowerRightLat());
            mySourceLLPanel.setULLong(myCurrentFileSource.getUpperLeftLon());
            mySourceOnCheckBox.setSelected(myCurrentFileSource.isEnabled());
            mySourceNameTF.setText(myCurrentFileSource.getName());
            mySourceFileTF.setText(myCurrentFileSource.getPath());
            mySourceIgnoreZerosCheckBox.setSelected(myCurrentFileSource.ignoreZeros());
        }
        else
        {
            mySourceLLPanel.setULLat(0.0);
            mySourceLLPanel.setLRLong(0.0);
            mySourceLLPanel.setLRLat(0.0);
            mySourceLLPanel.setULLong(0.0);
            mySourceOnCheckBox.setSelected(false);
            mySourceIgnoreZerosCheckBox.setSelected(false);
            mySourceNameTF.setText("");
            mySourceFileTF.setText("");
        }
        if (myOriginalSource != null)
        {
            boolean active = myOriginalSource.getDataGroupInfo().activationProperty().isActiveOrActivating();
            mySourceNameTF.setEditable(!active);
            mySourceLLPanel.setEnabled(!active);
            mySourceOnCheckBox.setEnabled(!active);
            mySourceIgnoreZerosCheckBox.setEnabled(!active);
        }

        String newName = mySourceNameTF.getText();
        String inUseMessage = isSourceNameInUse(newName, myCurrentFileSource);
        if (inUseMessage != null)
        {
            mySourceNamePanel.setBorder(BorderFactory.createLineBorder(Color.red));
            mySourceErrorMessage.setVisible(true);
            mySourceErrorMessage.setText(inUseMessage);
            mySourceErrorMessage.setForeground(Color.RED);
            myHasNameError = true;
        }
        else
        {
            myHasNameError = false;
            mySourceErrorMessage.setVisible(false);
            mySourceNamePanel.setBorder(BorderFactory.createEmptyBorder());
        }

        mySourceLLPanel.addChangeListener(myLLPanelChangeListener);
        mySourceNameTF.getDocument().addDocumentListener(mySourceNameDocListener);
    }

    /**
     * Builds the source center on button.
     */
    private void buildSourceCenterOnButton()
    {
        mySourceCenterOnBT = new JButton("Center On");
        mySourceCenterOnBT.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                if (myCurrentFileSource != null)
                {
                    myCurrentFileSource.centerOnImage(myToolbox, false);
                }
            }
        });
    }

    /**
     * Builds the source ignore zeros button.
     */
    private void buildSourceIgnoreZerosButton()
    {
        mySourceIgnoreZerosCheckBox = new JCheckBox("Ignore 0's", false);
        mySourceIgnoreZerosCheckBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (myCurrentFileSource != null)
                {
                    ImageryGroupImageManagerPanel.cleanCacheForSource(myCurrentFileSource, true, myToolbox);
                    myCurrentFileSource.setIgnoreZeros(mySourceIgnoreZerosCheckBox.isSelected());
                    myFileTableModel.updateRowForSource(myCurrentFileSource);
                    myStateHasChanged = true;
                    fireChanged();
                }
            }
        });
    }

    /**
     * Builds the source on button.
     */
    private void buildSourceOnButton()
    {
        mySourceOnCheckBox = new JCheckBox("Enabled", true);
        mySourceOnCheckBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (myCurrentFileSource != null)
                {
                    myCurrentFileSource.setEnabled(mySourceOnCheckBox.isSelected());
                    myFileTableModel.updateRowForSource(myCurrentFileSource);
                    myStateHasChanged = true;
                    fireChanged();
                }
            }
        });
    }

    /**
     * Builds the source zoom to button.
     */
    private void buildSourceZoomToButton()
    {
        mySourceZoomToBT = new JButton("Zoom To");
        mySourceZoomToBT.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                if (myCurrentFileSource != null)
                {
                    myCurrentFileSource.zoomToImage(myToolbox);
                }
            }
        });
    }

    /**
     * Creates the accept cancel panel.
     *
     * @return the component
     */
    private Component createAcceptCancelPanel()
    {
        Box aBox = Box.createHorizontalBox();
        aBox.add(Box.createHorizontalGlue());
        myAcceptButton = new JButton("Apply");
        myAcceptButton.setEnabled(false);
        myAcceptButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                updateAndSaveSource();
                myOwner.setVisible(false);
                myOwner.dispose();
            }
        });
        aBox.add(myAcceptButton);
        aBox.add(Box.createHorizontalStrut(10));
        myCancelButton = new JButton("Cancel");
        myCancelButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                myOwner.setVisible(false);
                myOwner.dispose();
            }
        });
        aBox.add(myCancelButton);
        aBox.add(Box.createHorizontalGlue());
        return aBox;
    }

    /**
     * Creates the and setup name panel.
     *
     * @param fieldPanel the field panel
     */
    private void createAndSetupNamePanel(JPanel fieldPanel)
    {
        myNameLabel = new JLabel("Group Name");
        myNameField = createNameField();
        JPanel namePanel = new JPanel(new BorderLayout());
        namePanel.add(myNameLabel, BorderLayout.WEST);
        namePanel.setMaximumSize(new Dimension(1000, 28));
        namePanel.setMinimumSize(new Dimension(300, 28));
        namePanel.setPreferredSize(new Dimension(300, 28));
        namePanel.add(myNameField, BorderLayout.CENTER);
        namePanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        fieldPanel.add(namePanel);
    }

    /**
     * Creates the and setup source panel.
     *
     * @param fieldPanel the field panel
     */
    private void createAndSetupSourcePanel(JPanel fieldPanel)
    {
        mySourceLLPanel = new LatLongCornerPanel();
        mySourceLLPanel.setMaximumSize(new Dimension(1000, 60));
        mySourceLLPanel.setMinimumSize(new Dimension(300, 60));
        mySourceLLPanel.setPreferredSize(new Dimension(300, 60));

        mySourceNameTF = new JTextField();
        createSourceNamePanel();
        mySourceErrorMessage = new JLabel("NO ERRORS");
        mySourceErrorMessage.setHorizontalAlignment(JLabel.CENTER);
        mySourceErrorMessage.setForeground(Color.GREEN);
        mySourceErrorMessage.setVisible(false);

        createSourceNameDocListener();
        mySourceNameTF.getDocument().addDocumentListener(mySourceNameDocListener);

        JPanel sourceFilePanel = createSourceFilePanel();

        JPanel sourcePanel1 = new JPanel(new GridLayout(2, 1, 4, 0));
        sourcePanel1.add(mySourceNamePanel);
        sourcePanel1.add(sourceFilePanel);

        buildSourceOnButton();
        buildSourceIgnoreZerosButton();
        buildSourceZoomToButton();
        buildSourceCenterOnButton();
        createSourceInfoButton();

        JPanel sourceButtonPanel = new JPanel(new GridLayout(5, 1, 0, 5));
        sourceButtonPanel.add(mySourceOnCheckBox);
        sourceButtonPanel.add(mySourceIgnoreZerosCheckBox);
        sourceButtonPanel.add(mySourceZoomToBT);
        sourceButtonPanel.add(mySourceCenterOnBT);
        sourceButtonPanel.add(mySourceInfoBT);
        sourceButtonPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 2));

        JPanel llOuterPanel = new JPanel(new BorderLayout());
        llOuterPanel.setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 0));
        llOuterPanel.add(mySourceLLPanel, BorderLayout.CENTER);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(sourcePanel1, BorderLayout.NORTH);
        leftPanel.add(llOuterPanel, BorderLayout.CENTER);

        mySourceEditorPanel = new JPanel(new BorderLayout());
        mySourceEditorPanel.add(leftPanel, BorderLayout.CENTER);
        mySourceEditorPanel.add(sourceButtonPanel, BorderLayout.EAST);
        mySourceEditorPanel.add(mySourceErrorMessage, BorderLayout.SOUTH);
        mySourceEditorPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0),
                BorderFactory.createTitledBorder("Selected Image Properties:")));

        fieldPanel.add(mySourceEditorPanel);
    }

    /**
     * Creates the name field.
     *
     * @return the j text field
     */
    private JTextField createNameField()
    {
        JTextField nameField = new JTextField("New Image");
        nameField.setEditable(true);
        return nameField;
    }

    /**
     * Creatend setup file table panel.
     *
     * @param fieldPanel the field panel
     */
    private void createndSetupFileTablePanel(JPanel fieldPanel)
    {
        myFileTableModel = new ImageFileTableModel();
        myFileTable = new JXTable(myFileTableModel);
        JPanel fileTablePanel = new JPanel(new BorderLayout());
        fileTablePanel.setMaximumSize(new Dimension(1000, 200));
        fileTablePanel.setMinimumSize(new Dimension(400, 200));
        fileTablePanel.setPreferredSize(new Dimension(400, 200));
        fileTablePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0),
                BorderFactory.createTitledBorder("Image Files:")));
        JScrollPane fileTableSP = new JScrollPane(myFileTable);
        fileTablePanel.add(fileTableSP, BorderLayout.CENTER);
        fieldPanel.add(fileTablePanel);
        myFileTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        myFileTable.getSelectionModel().addListSelectionListener(this);

        Box btBox = Box.createHorizontalBox();
        btBox.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
        createRemoveSelectedButton();
        myEnableSelectedBT = new JButton("Enable");
        myEnableSelectedBT.setMargin(new Insets(2, 2, 2, 2));
        myEnableSelectedBT.setToolTipText("Enable Selected Images");
        myEnableSelectedBT.setFocusable(false);
        myEnableSelectedBT.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                enableDisableSelectedImages(true);
            }
        });

        myDisableSelectedBT = new JButton("Disable");
        myDisableSelectedBT.setMargin(new Insets(2, 2, 2, 2));
        myDisableSelectedBT.setToolTipText("Disable Selected Images");
        myDisableSelectedBT.setFocusable(false);
        myDisableSelectedBT.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                enableDisableSelectedImages(false);
            }
        });

        btBox.add(Box.createHorizontalGlue());
        btBox.add(myEnableSelectedBT);
        btBox.add(Box.createHorizontalStrut(3));
        btBox.add(myDisableSelectedBT);
        btBox.add(Box.createHorizontalStrut(3));
        btBox.add(myRemoveSelectedBT);
        fileTablePanel.add(btBox, BorderLayout.SOUTH);

        myLLPanelChangeListener = new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                if (myCurrentFileSource != null)
                {
                    myCurrentFileSource.setUpperLeftLat(mySourceLLPanel.getULLat());
                    myCurrentFileSource.setLowerRightLon(mySourceLLPanel.getLRLong());
                    myCurrentFileSource.setLowerRightLat(mySourceLLPanel.getLRLat());
                    myCurrentFileSource.setUpperLeftLon(mySourceLLPanel.getULLong());
                    myStateHasChanged = true;
                    fireChanged();
                }
            }
        };
    }

    /**
     * Creates the remove selected button.
     */
    private void createRemoveSelectedButton()
    {
        myRemoveSelectedBT = new JButton("Remove");
        myRemoveSelectedBT.setMargin(new Insets(2, 2, 2, 2));
        myRemoveSelectedBT.setToolTipText("Remove Selected Images");
        myRemoveSelectedBT.setFocusable(false);
        myRemoveSelectedBT.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int[] rowsSelected = myFileTable.getSelectedRows();
                if (rowsSelected != null && rowsSelected.length > 0)
                {
                    List<ImageryFileSource> srcList = new ArrayList<>();
                    for (int row : rowsSelected)
                    {
                        ImageryFileSource src = myFileTableModel.getSourceAtRow(myFileTable.convertRowIndexToModel(row));
                        if (src != null)
                        {
                            srcList.add(src);
                        }
                    }

                    if (srcList.size() == myFileTableModel.getRowCount())
                    {
                        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(myRemoveSelectedBT),
                                "You cannot remove all images, at least one must remain!", "Remove All Error",
                                JOptionPane.WARNING_MESSAGE);
                    }
                    else
                    {
                        int val = JOptionPane.showConfirmDialog(SwingUtilities.getWindowAncestor(myRemoveSelectedBT),
                                "Are you sure you want to remove the selected images?", "Remove Confiramtion",
                                JOptionPane.OK_CANCEL_OPTION);

                        if (val == JOptionPane.OK_OPTION)
                        {
                            myIdToLastSelectedRowMap.put(myOriginalSource.getId(), Integer.valueOf(-1));
                            myFileTableModel.removeSources(srcList);
                            myCurrentFileSource = null;
                            myStateHasChanged = true;
                            fireChanged();
                        }
                    }
                }
                else
                {
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(myRemoveSelectedBT),
                            "You must first select an image to remove", "Selection Error", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
    }

    /**
     * Creates the source file panel.
     *
     * @return the j panel
     */
    private JPanel createSourceFilePanel()
    {
        mySourceFileTF = new JTextField();
        JPanel sourceFilePanel = new JPanel(new BorderLayout());
        sourceFilePanel.setMaximumSize(new Dimension(1000, 30));
        sourceFilePanel.setMinimumSize(new Dimension(300, 30));
        sourceFilePanel.setPreferredSize(new Dimension(300, 30));
        sourceFilePanel.add(createLabelPanel(new JLabel("File"), 40), BorderLayout.WEST);
        sourceFilePanel.add(mySourceFileTF);
        mySourceFileTF.setEditable(false);
        return sourceFilePanel;
    }

    /**
     * Creates the source info button.
     */
    private void createSourceInfoButton()
    {
        mySourceInfoBT = new JButton("Info");
        mySourceInfoBT.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (myCurrentFileSource != null)
                {
                    Dataset ds = GDALTools.getDataSet(new File(myCurrentFileSource.getFilePath()));
                    if (ds != null)
                    {
                        String info = GDALInfo.getInfoPrint(ds);
                        TextViewDialog tv = new TextViewDialog(ImageryGroupImageManagerPanel.this,
                                "GDAL File Info: " + myCurrentFileSource.getFilePath(), info, false,
                                myToolbox.getPreferencesRegistry());
                        tv.setLocationRelativeTo(SwingUtilities.getWindowAncestor(ImageryGroupImageManagerPanel.this));
                        tv.setVisible(true);
                        ds.delete();
                        ds = null;
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(ImageryGroupImageManagerPanel.this, "Could not get image info.",
                                "Image Info Retrieval Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
    }

    /**
     * Creates the source name doc listener.
     */
    private void createSourceNameDocListener()
    {
        mySourceNameDocListener = new DocumentListener()
        {
            @Override
            public void changedUpdate(DocumentEvent e)
            {
            }

            @Override
            public void insertUpdate(DocumentEvent e)
            {
                if (myCurrentFileSource != null)
                {
                    String newName = mySourceNameTF.getText();
                    String inUseMessage = isSourceNameInUse(newName, myCurrentFileSource);
                    if (inUseMessage != null)
                    {
                        mySourceNamePanel.setBorder(BorderFactory.createLineBorder(Color.red));
                        mySourceErrorMessage.setVisible(true);
                        mySourceErrorMessage.setText(inUseMessage);
                        mySourceErrorMessage.setForeground(Color.RED);
                        myHasNameError = true;
                    }
                    else
                    {
                        mySourceErrorMessage.setVisible(false);
                        mySourceNamePanel.setBorder(BorderFactory.createEmptyBorder());
                        myCurrentFileSource.setName(mySourceNameTF.getText());
                        myFileTableModel.updateRowForSource(myCurrentFileSource);
                        myHasNameError = false;
                        myStateHasChanged = true;
                        fireChanged();
                    }
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                if (myCurrentFileSource != null)
                {
                    String newName = mySourceNameTF.getText();
                    String inUseMessage = isSourceNameInUse(newName, myCurrentFileSource);
                    if (inUseMessage != null)
                    {
                        mySourceNamePanel.setBorder(BorderFactory.createLineBorder(Color.red));
                        mySourceErrorMessage.setVisible(true);
                        mySourceErrorMessage.setText(inUseMessage);
                        mySourceErrorMessage.setForeground(Color.RED);
                        myHasNameError = true;
                    }
                    else
                    {
                        myHasNameError = false;
                        mySourceErrorMessage.setVisible(false);
                        mySourceNamePanel.setBorder(BorderFactory.createEmptyBorder());
                        myCurrentFileSource.setName(mySourceNameTF.getText());
                        myFileTableModel.updateRowForSource(myCurrentFileSource);
                        myStateHasChanged = true;
                        fireChanged();
                    }
                }
            }
        };
    }

    /**
     * Creates the source name panel.
     */
    private void createSourceNamePanel()
    {
        mySourceNamePanel = new JPanel(new BorderLayout());
        mySourceNamePanel.setMaximumSize(new Dimension(1000, 30));
        mySourceNamePanel.setMinimumSize(new Dimension(300, 30));
        mySourceNamePanel.setPreferredSize(new Dimension(300, 30));
        mySourceNamePanel.add(createLabelPanel(new JLabel("Name"), 40), BorderLayout.WEST);
        mySourceNamePanel.add(mySourceNameTF);
    }

    /**
     * Find internal frame parent.
     *
     * @param c the c
     * @return the j internal frame
     */
    private LayerDetailPanel findLayerDetailsPanel(Component c)
    {
        if (c.getParent() == null)
        {
            return null;
        }
        else if (c.getParent() instanceof LayerDetailPanel)
        {
            return (LayerDetailPanel)c.getParent();
        }
        else
        {
            return findLayerDetailsPanel(c.getParent());
        }
    }

    /**
     * Inits the.
     */
    private void init()
    {
        setMaximumSize(new Dimension(1000, 800));
        setPreferredSize(new Dimension(1000, 400));
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel fieldPanel = new JPanel();
        BoxLayout fl = new BoxLayout(fieldPanel, BoxLayout.Y_AXIS);
        fieldPanel.setLayout(fl);
        fieldPanel.setAlignmentY(LEFT_ALIGNMENT);
        fieldPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 10));

        myDeactivateToChangeLabel = new JLabel("DEACTIVATE LAYER TO MAKE CHANGES");
        myDeactivateToChangeLabel.setForeground(Color.yellow);
        myDeactivateToChangeLabel.setFont(myDeactivateToChangeLabel.getFont().deriveFont(Font.BOLD));
        Box aBox = Box.createHorizontalBox();
        aBox.add(Box.createHorizontalGlue());
        aBox.add(myDeactivateToChangeLabel);
        aBox.add(Box.createHorizontalGlue());
        fieldPanel.add(aBox);

        createAndSetupNamePanel(fieldPanel);
        fieldPanel.add(Box.createVerticalStrut(5));
        createndSetupFileTablePanel(fieldPanel);
        createAndSetupSourcePanel(fieldPanel);
        fieldPanel.add(Box.createVerticalGlue());

        add(fieldPanel, BorderLayout.CENTER);

        add(createAcceptCancelPanel(), BorderLayout.SOUTH);

        updateSourcePanel();
    }

    /**
     * Update based on activity.
     */
    private void updateBasedOnActivity()
    {
        if (myOriginalSource != null)
        {
            boolean active = myOriginalSource.getDataGroupInfo().activationProperty().isActive();

            myDeactivateToChangeLabel.setVisible(active);
            myAcceptButton.setVisible(!active);
            myCancelButton.setText(active ? "Close" : "Cancel");
            mySourceLLPanel.setEnabled(!active);
            mySourceOnCheckBox.setEnabled(!active);
            mySourceIgnoreZerosCheckBox.setEnabled(!active);
            myRemoveSelectedBT.setVisible(!active);
            myEnableSelectedBT.setVisible(!active);
            myDisableSelectedBT.setVisible(!active);
        }
    }

    /**
     * The Class ImageFileTableModel.
     */
    protected class ImageFileTableModel extends DefaultTableModel
    {
        /** The Sources. */
        private List<ImageryFileSource> mySources;

        /**
         * Instantiates a new image file table model.
         */
        public ImageFileTableModel()
        {
            super();
        }

        @Override
        public int getColumnCount()
        {
            return 7;
        }

        @Override
        public String getColumnName(int columnIndex)
        {
            String colName = null;
            switch (columnIndex)
            {
                case 0:
                    colName = "#";
                    break;
                case 1:
                    colName = "Name";
                    break;
                case 2:
                    colName = "File";
                    break;
                case 3:
                    colName = "Enabled";
                    break;
                case 4:
                    colName = "Projection";
                    break;
                case 5:
                    colName = "Datum";
                    break;
                case 6:
                    colName = "Ignore 0's";
                    break;
                default:
                    break;
            }
            return colName;
        }

        /**
         * Gets the row.
         *
         * @param src the src
         * @return the row
         */
        public int getRow(ImageryFileSource src)
        {
            if (mySources != null)
            {
                for (int i = 0; i < mySources.size(); i++)
                {
                    if (mySources.get(i) == src)
                    {
                        return i;
                    }
                }
            }
            return -1;
        }

        @Override
        public int getRowCount()
        {
            if (mySources != null)
            {
                return mySources.size();
            }
            else
            {
                return 0;
            }
        }

        /**
         * Gets the row of source.
         *
         * @param src the src
         * @return the row of source
         */
        public int getRowOfSource(ImageryFileSource src)
        {
            return mySources.indexOf(src);
        }

        /**
         * Gets the source at row.
         *
         * @param row the row
         * @return the source at row
         */
        public ImageryFileSource getSourceAtRow(int row)
        {
            if (mySources != null)
            {
                return mySources.get(row);
            }
            else
            {
                return null;
            }
        }

        /**
         * Gets the sources.
         *
         * @return the sources
         */
        public List<ImageryFileSource> getSources()
        {
            return mySources;
        }

        @Override
        public Object getValueAt(int row, int col)
        {
            try
            {
                if (mySources != null)
                {
                    ImageryFileSource source = mySources.get(row);
                    if (source != null)
                    {
                        String colVal = null;
                        switch (col)
                        {
                            case 0:
                                colVal = Integer.toString(row + 1);
                                break;
                            case 1:
                                colVal = source.getName();
                                break;
                            case 2:
                                File aFile = new File(source.getPath());
                                colVal = aFile.getName();
                                break;
                            case 3:
                                colVal = source.isEnabled() ? "Yes" : "No";
                                break;
                            case 4:
                                colVal = source.getProjection();
                                break;
                            case 5:
                                colVal = source.getDatum();
                                break;
                            case 6:
                                colVal = source.ignoreZeros() ? "Yes" : "No";
                                break;
                            default:
                                break;
                        }
                        return colVal;
                    }
                    else
                    {
                        return "";
                    }
                }
                else
                {
                    return "";
                }
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
                return "";
            }
        }

        @Override
        public boolean isCellEditable(int row, int column)
        {
            return false;
        }

        /**
         * Removes the source.
         *
         * @param source the source
         */
        public void removeSource(ImageryFileSource source)
        {
            ImageryGroupImageManagerPanel.cleanCacheForSource(source, false, myToolbox);
            mySources.remove(source);
            fireTableDataChanged();
        }

        /**
         * Removes the sources.
         *
         * @param srcList the src list
         */
        public void removeSources(List<ImageryFileSource> srcList)
        {
            ImageryGroupImageManagerPanel.cleanCacheForSources(ImageryGroupImageManagerPanel.this, srcList, myToolbox);
            mySources.removeAll(srcList);
            fireTableDataChanged();
        }

        /**
         * Sets the sources.
         *
         * @param sources the new sources
         */
        public void setSources(List<ImageryFileSource> sources)
        {
            mySources = new ArrayList<>();
            if (sources != null)
            {
                for (ImageryFileSource src : sources)
                {
                    ImageryFileSource copy = new ImageryFileSource(src);
                    copy.setDataTypeInfo(src.getDataTypeInfo());
                    copy.setGroup(src.getGroup());
                    mySources.add(copy);
                }
            }
            fireTableDataChanged();
        }

        @Override
        public void setValueAt(Object aValue, int row, int column)
        {
            // Intentionally not implemented as cell should not be edited.
        }

        /**
         * Update row for source.
         *
         * @param src the src
         */
        public void updateRowForSource(ImageryFileSource src)
        {
            int row = mySources.indexOf(src);
            if (row != -1)
            {
                fireTableRowsUpdated(row, row);
            }
        }
    }
}
