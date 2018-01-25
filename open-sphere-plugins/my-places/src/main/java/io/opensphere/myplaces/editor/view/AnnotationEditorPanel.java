package io.opensphere.myplaces.editor.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.micromata.opengis.kml.v_2_2_0.ExtendedData;
import de.micromata.opengis.kml.v_2_2_0.Geometry;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
import de.micromata.opengis.kml.v_2_2_0.Polygon;
import io.opensphere.core.control.action.context.ContextIdentifiers;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.swing.ComponentTitledBorder;
import io.opensphere.core.util.swing.DynamicEtchedBorder;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.core.util.swing.ListComboBoxModel;
import io.opensphere.core.util.swing.SlavedDateTimePicker;
import io.opensphere.core.util.swing.ToStringProxy;
import io.opensphere.core.viewbookmark.ViewBookmark;
import io.opensphere.kml.common.util.KMLSpatialTemporalUtils;
import io.opensphere.myplaces.constants.Constants;
import io.opensphere.myplaces.editor.controller.AnnotationEditController;
import io.opensphere.myplaces.util.ExtendedDataUtils;
import io.opensphere.myplaces.util.PlacemarkUtils;

/** The annotation point editor class. */
@SuppressWarnings("PMD.GodClass")
public class AnnotationEditorPanel extends JDialog
{
    /** The Constant CANCEL. */
    public static final String RESULT_CANCEL = "CANCEL";

    /** The Constant SUCCESS. */
    public static final String RESULT_SUCCESS = "SUCCESS";

    /** Serial. */
    private static final long serialVersionUID = 1L;

    /** The Associated view ComboBox. */
    private final JComboBox<Object> myAssociatedViewComboBox = new JComboBox<>();

    /** The Associated view label. */
    private JLabel myAssociatedViewLabel;

    /** The Associated view panel. */
    private JPanel myAssociatedViewPanel;

    /** The cancel button. */
    private JButton myCancelButton;

    /** The Slaved date time picker. */
    private transient SlavedDateTimePicker myDateTimePicker;

    /** The Decimal degrees precision spinner. */
    private JSpinner myDecimalDegreesPrecisionSpinner;

    /** The decimal latitude field. */
    protected JTextField decLatField = new JTextField();

    /** The decimal longitude field. */
    protected JTextField decLonField = new JTextField();

    /** The DMS latitude field. */
    protected JTextField dmsLatField = new JTextField();

    /** The DMS longitude field. */
    protected JTextField dmsLonField = new JTextField();

    /** Input field for latitude in degrees + decimal minutes. */
    protected JTextField ddmLatField = new JTextField();

    /** Input field for longitude in degrees + decimal minutes. */
    protected JTextField ddmLonField = new JTextField();

    /** The description area. */
    private final JTextArea myDescArea = new JTextArea();

    /** The description panel. */
    private JPanel myDescPanel;

    /** The field in which altitude is entered. */
    private final JTextField myAltitudeField = new JTextField();

    /** The field in which altitude is enabled / disabled. */
    private final JCheckBox myEnableAltitudeField = new JCheckBox();

    /** The end time panel. */
    private GridBagPanel myEndTimePanel;

    /** Indicates if the placemark as an end time. */
    private JCheckBox myHasEndDate;

    /** True if the point has a time, false if not. */
    private JCheckBox myHasTimeCheckBox;

    /** Indicates if the panel has been initialized. */
    private boolean myIsInitialized;

    /** The mgrs field. */
    protected JTextField mgrsField = new JTextField();
    {
        mgrsField.getDocument().addDocumentListener(new DocumentListener()
        {
            @Override
            public void changedUpdate(DocumentEvent e)
            {
                toUpperMGRS();
            }

            @Override
            public void insertUpdate(DocumentEvent e)
            {
                toUpperMGRS();
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                // no need to check for uppercase when removing.
            }
        });
    }

    /** The MGRS precision combo box. */
    private JComboBox<String> myMGRSPrecisionComboBox;

    /** The current placemark. */
    private transient Placemark myPlacemark;

    /** The annotation point controller. */
    private final transient AnnotationEditController myPointPanelController;

    /** The Result listener. */
    private ActionListener myResultListener;

    /** The save button. */
    private JButton mySaveButton;

    /** The save cancel panel. */
    private JPanel mySaveCancelPanel;

    /** Indicates if the placemark should be able to animate on the map. */
    private JCheckBox myShouldAnimate;

    /** Indicates if the placemark should show in the timeline or not. */
    private JCheckBox myShowInTimeline;

    /** Allows the user to set a time for a my point. */
    private GridBagPanel myStartTimePanel;

    /** The style panel. */
    private AnnotationStyleEditorPanel myStylePanel;

    /** The time panel. */
    private GridBagPanel myTimePanel;

    /** The title field. */
    private JTextField titleField = new JTextField();
    {
        titleField.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyReleased(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    doSaveAction();
                }
                else
                {
                    myStylePanel.updatePreviewText(titleField.getText());
                }
            }
        });
    }

    /** The title panel. */
    private JPanel myTitlePanel;

    /** The use call out panel checkbox. */
    private JCheckBox myUseCalloutPanelCheckBox;

    /**
     * Creates a horizontal separator with a label.
     *
     * @param text The label text
     * @return The separator panel
     */
    private static JPanel createSeparator(String text)
    {
        GridBagPanel panel = new GridBagPanel();
        panel.setInsets(6, 0, 3, 3);
        panel.add(new JLabel(text));
        panel.fillHorizontal();
        panel.setInsets(6, 0, 3, 0);
        panel.add(new JSeparator());
        return panel;
    }

    /**
     * Instantiates a new map point editor panel2.
     *
     * @param pointPanelController the point panel controller
     */
    public AnnotationEditorPanel(AnnotationEditController pointPanelController)
    {
        super(pointPanelController.getToolbox().getUIRegistry().getMainFrameProvider().get(), "My Places Editor",
                ModalityType.APPLICATION_MODAL);
        myPointPanelController = pointPanelController;
    }

    /**
     * Gets the associated view panel.
     *
     * @return the associated view panel
     */
    public JPanel getAssociatedViewPanel()
    {
        if (myAssociatedViewPanel == null)
        {
            myAssociatedViewPanel = new JPanel(new BorderLayout());
            myAssociatedViewLabel = new JLabel("View:");
            JPanel labelPanel = new JPanel();
            labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
            labelPanel.add(Box.createHorizontalStrut(30));
            labelPanel.add(myAssociatedViewLabel);
            labelPanel.add(Box.createHorizontalStrut(5));
            myAssociatedViewPanel.add(createSeparator("Associated Look Angle"), BorderLayout.NORTH);
            myAssociatedViewPanel.add(labelPanel, BorderLayout.WEST);
            myAssociatedViewPanel.add(myAssociatedViewComboBox);
        }
        return myAssociatedViewPanel;
    }

    /**
     * Gets the cancel button.
     *
     * @return the cancel button
     */
    public JButton getCancelButton()
    {
        if (myCancelButton == null)
        {
            myCancelButton = new JButton("Cancel");
            myCancelButton.setPreferredSize(new Dimension(75, 20));
            myCancelButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    myPointPanelController.getToolbox().getUIRegistry().getContextActionManager()
                            .clearContextSingleActionProvider(ContextIdentifiers.DEFAULT_MOUSE_CONTEXT, MouseEvent.class);

                    AnnotationEditorPanel.this.setVisible(false);
                    fireResultToListener(RESULT_CANCEL);
                }
            });
        }
        return myCancelButton;
    }

    /**
     * Gets the decimal degrees precision spinner.
     *
     * @return the decimal degrees precision spinner
     */
    public JSpinner getDecimalDegreesPrecisionSpinner()
    {
        if (myDecimalDegreesPrecisionSpinner == null)
        {
            SpinnerModel model = new SpinnerNumberModel(10, 1, 10, 1);
            myDecimalDegreesPrecisionSpinner = new JSpinner(model);
            myDecimalDegreesPrecisionSpinner.setSize(60, 24);
            myDecimalDegreesPrecisionSpinner.setPreferredSize(myDecimalDegreesPrecisionSpinner.getSize());
            myDecimalDegreesPrecisionSpinner.setMinimumSize(myDecimalDegreesPrecisionSpinner.getSize());
        }
        return myDecimalDegreesPrecisionSpinner;
    }

    /**
     * Gets the field in which altitude is entered.
     *
     * @return the field in which altitude is entered.
     */
    public JTextField getAltitudeField()
    {
        return myAltitudeField;
    }

    /**
     * Gets the field in which altitude is enabled / disabled.
     *
     * @return the field in which altitude is enabled / disabled.
     */
    public JCheckBox getEnableAltitudeField()
    {
        return myEnableAltitudeField;
    }

    /**
     * Gets the MGRS precision combo box.
     *
     * @return the mGRS precision combo box
     */
    public JComboBox<String> getMGRSPrecisionComboBox()
    {
        if (myMGRSPrecisionComboBox == null)
        {
            myMGRSPrecisionComboBox = new JComboBox<>(new String[] { "10", "8", "6", "4" });
            myMGRSPrecisionComboBox.setSize(60, 24);
            myMGRSPrecisionComboBox.setPreferredSize(myMGRSPrecisionComboBox.getSize());
            myMGRSPrecisionComboBox.setMinimumSize(myMGRSPrecisionComboBox.getSize());
        }
        return myMGRSPrecisionComboBox;
    }

    /**
     * Gets the save button.
     *
     * @return the save button
     */
    public JButton getSaveButton()
    {
        if (mySaveButton == null)
        {
            mySaveButton = new JButton("OK");
            mySaveButton.setPreferredSize(new Dimension(75, 20));
            mySaveButton.addActionListener(e -> doSaveAction());
        }
        return mySaveButton;
    }

    /**
     * Sets the point.
     *
     * @param placemark the placemark
     * @param resultListener the result listener
     */
    public void setPlace(Placemark placemark, ActionListener resultListener)
    {
        initialize(placemark != null ? placemark.getGeometry().getClass() : Point.class);
        myResultListener = resultListener;
        myPlacemark = placemark;
        titleField.setText("");
        decLatField.setText("");
        decLonField.setText("");
        dmsLatField.setText("");
        dmsLonField.setText("");
        ddmLatField.setText("");
        ddmLonField.setText("");
        mgrsField.setText("");
        myDescArea.setText("");
        myStylePanel.setPlacemark(placemark);
        updateAssociatedViewComboBoxValues(null);
        if (placemark != null)
        {
            setAnnotationPointParams(placemark);
        }
    }

    /**
     * Show editor and populate the fields with values from the mappoint.
     *
     * @param parent the parent
     */
    public void showEditor(Component parent)
    {
        initialize(myPlacemark != null ? myPlacemark.getGeometry().getClass() : Point.class);
        Window w = parent == null ? myPointPanelController.getToolbox().getUIRegistry().getMainFrameProvider().get()
                : SwingUtilities.getWindowAncestor(parent) == null && parent instanceof Window ? (Window)parent
                        : SwingUtilities.getWindowAncestor(parent);
        setLocationRelativeTo(w);

        if (myPlacemark != null)
        {
            titleField.setText(myPlacemark.getName());
            titleField.selectAll();
            setVisible(true);
        }
    }

    /**
     * Check inputs valid.
     *
     * @return true, if successful
     */
    protected boolean checkInputsValid()
    {
        boolean valid = myPlacemark != null;
        String invalidMessage = null;
        if (valid && titleField.getText().isEmpty())
        {
            invalidMessage = "Title can not be empty.";
            valid = false;
        }

        String positionErrorMessage = positionsValid();
        if (valid && positionErrorMessage != null)
        {
            invalidMessage = positionErrorMessage;
            valid = false;
        }

        if (valid && myStylePanel.getModel().getValidationStatus() != ValidationStatus.VALID)
        {
            invalidMessage = myStylePanel.getModel().getErrorMessage();
            valid = false;
        }

        if (!valid)
        {
            JOptionPane.showMessageDialog(this, invalidMessage, "Input Validation Error", JOptionPane.ERROR_MESSAGE);
        }
        return valid;
    }

    /**
     * Extract position.
     *
     * @param pt the pt
     */
    protected void extractPosition(Placemark pt)
    {
    }

    /**
     * Gets the edits the loc panel.
     *
     * @return the edits the loc panel
     */
    protected JPanel getEditLocPanel()
    {
        return null;
    }

    /**
     * Gets the controller.
     *
     * @return The controller.
     */
    protected AnnotationEditController getPointPanelController()
    {
        return myPointPanelController;
    }

    /**
     * Gets the show hide point checkbox.
     *
     * @return the show hide point checkbox
     */
    protected JCheckBox getShowHideFeatureCheckbox()
    {
        return null;
    }

    /**
     * Validates the positions.
     *
     * @return The positions to validate.
     */
    protected String positionsValid()
    {
        return null;
    }

    /**
     * Save inputs.
     */
    protected void saveInputs()
    {
        if (myPlacemark != null)
        {
            Placemark pt = myPlacemark.clone();
            ExtendedData extendedData = pt.getExtendedData();

            pt.setName(titleField.getText());
            pt.setDescription(myDescArea.getText());

            myStylePanel.getModel().saveInputs(pt);

            extractPosition(pt);

            boolean isFeatureOn = true;

            JCheckBox isFeatureOnCheckBox = getShowHideFeatureCheckbox();
            if (isFeatureOnCheckBox != null)
            {
                isFeatureOn = isFeatureOnCheckBox.isSelected();
            }

            ExtendedDataUtils.putBoolean(extendedData, Constants.IS_FEATURE_ON_ID, isFeatureOn);
            ExtendedDataUtils.putBoolean(extendedData, Constants.IS_ANNOHIDE_ID, !getCalloutPanelCheckbox().isSelected());
            Object item = myAssociatedViewComboBox.getSelectedItem();
            if (item instanceof BookmarkProxy)
            {
                BookmarkProxy proxy = (BookmarkProxy)item;
                if (proxy.getItem() != null)
                {
                    ExtendedDataUtils.putString(extendedData, Constants.ASSOCIATED_VIEW_ID, proxy.getItem().getViewName());
                }
            }
            else
            {
                ExtendedDataUtils.putString(extendedData, Constants.ASSOCIATED_VIEW_ID, "");
            }

            if (myHasTimeCheckBox.isSelected())
            {
                TimeSpan span = null;

                if (myHasEndDate.isSelected())
                {
                    span = TimeSpan.get(myDateTimePicker.getBeginDateTimePicker().getCurrentPickerDate(),
                            myDateTimePicker.getEndDateTimePicker().getCurrentPickerDate());
                }
                else
                {
                    span = TimeSpan.get(myDateTimePicker.getBeginDateTimePicker().getCurrentPickerDate());
                }

                pt.setTimePrimitive(KMLSpatialTemporalUtils.timeSpanToTimePrimitive(span));
            }
            else
            {
                pt.setTimePrimitive(null);
            }

            ExtendedDataUtils.putBoolean(extendedData, Constants.IS_ANIMATE, myShouldAnimate.isSelected());
            ExtendedDataUtils.putBoolean(extendedData, Constants.IS_ALTITUDE_ID, myEnableAltitudeField.isSelected());
            ExtendedDataUtils.putBoolean(extendedData, Constants.IS_SHOW_IN_TIMELINE, myShowInTimeline.isSelected());

            PlacemarkUtils.copyPlacemark(pt, myPlacemark);
        }
    }

    /**
     * Sets the annotation point params.
     *
     * @param placemark the placemark to retrieve settings from
     */
    protected void setAnnotationPointParams(Placemark placemark)
    {
        titleField.setText(placemark.getName());

        ExtendedData extendedData = placemark.getExtendedData();

        JCheckBox showHideFeature = getShowHideFeatureCheckbox();
        if (showHideFeature != null)
        {
            showHideFeature.setSelected(ExtendedDataUtils.getBoolean(extendedData, Constants.IS_FEATURE_ON_ID, true));
        }

        boolean showCallout = !ExtendedDataUtils.getBoolean(extendedData, Constants.IS_ANNOHIDE_ID, false);
        getCalloutPanelCheckbox().setSelected(showCallout);
        setCalloutParamsEnabled(showCallout);

        myDescArea.setText(placemark.getDescription());
        String assocView = ExtendedDataUtils.getString(extendedData, Constants.ASSOCIATED_VIEW_ID);
        if (assocView != null && !assocView.isEmpty() && myAssociatedViewComboBox.getItemCount() > 0)
        {
            int foundIndex = -1;
            for (int i = 0; i < myAssociatedViewComboBox.getItemCount(); i++)
            {
                Object o = myAssociatedViewComboBox.getItemAt(i);
                if (o instanceof BookmarkProxy)
                {
                    BookmarkProxy proxy = (BookmarkProxy)o;
                    if (proxy.getItem() != null && proxy.getItem().getViewName().equals(assocView))
                    {
                        foundIndex = i;
                        break;
                    }
                }
            }
            if (foundIndex != -1)
            {
                myAssociatedViewComboBox.setSelectedIndex(foundIndex);
            }
        }

        if (placemark.getTimePrimitive() != null)
        {
            TimeSpan timeSpan = KMLSpatialTemporalUtils.timeSpanFromTimePrimitive(placemark.getTimePrimitive());
            myDateTimePicker.getBeginDateTimePicker().setCurrentPickerDate(timeSpan.getStartDate());
            myDateTimePicker.getEndDateTimePicker().setCurrentPickerDate(timeSpan.getEndDate());
            myHasTimeCheckBox.setSelected(true);
            myShouldAnimate.setEnabled(true);
            myShowInTimeline.setEnabled(true);
            myHasEndDate.setSelected(!timeSpan.isInstantaneous());
        }
        else
        {
            myHasTimeCheckBox.setSelected(false);
            myHasEndDate.setSelected(false);
            myHasEndDate.setSelected(false);
        }

        enableDisableTimePanel();

        myShouldAnimate.setSelected(ExtendedDataUtils.getBoolean(extendedData, Constants.IS_ANIMATE, true));
        myEnableAltitudeField.setSelected(ExtendedDataUtils.getBoolean(extendedData, Constants.IS_ALTITUDE_ID, false));
        myShowInTimeline.setSelected(ExtendedDataUtils.getBoolean(extendedData, Constants.IS_SHOW_IN_TIMELINE, true));

        enableDisableAltitudeField();
    }

    /**
     * Do save action.
     */
    private void doSaveAction()
    {
        if (checkInputsValid())
        {
            myPointPanelController.getToolbox().getUIRegistry().getContextActionManager()
                    .clearContextSingleActionProvider(ContextIdentifiers.DEFAULT_MOUSE_CONTEXT, MouseEvent.class);

            saveInputs();
            setVisible(false);
            fireResultToListener(RESULT_SUCCESS);
        }
    }

    /**
     * Enables or disables specific portions of the time panel based on what is
     * checked.
     */
    private void enableDisableTimePanel()
    {
        myDateTimePicker.getBeginDateTimePicker().setEnabled(myHasTimeCheckBox.isSelected());
        myDateTimePicker.getEndDateTimePicker().setEnabled(myHasTimeCheckBox.isSelected() && myHasEndDate.isSelected());
        myHasEndDate.setEnabled(myHasTimeCheckBox.isSelected());
        myShowInTimeline.setEnabled(myHasTimeCheckBox.isSelected());
        myShouldAnimate.setEnabled(myHasTimeCheckBox.isSelected());
    }

    /**
     * Updates the enabled state of the altitude field.
     */
    private void enableDisableAltitudeField()
    {
        myAltitudeField.setEnabled(myEnableAltitudeField.isSelected());
    }

    /**
     * Fire result to listener.
     *
     * @param resultType the result type
     */
    private void fireResultToListener(final String resultType)
    {
        if (myResultListener != null)
        {
            final ActionListener resultListener = myResultListener;
            myResultListener = null;
            EventQueueUtilities.runOnEDT(() -> resultListener.actionPerformed(new ActionEvent(this, 0, resultType)));
        }
    }

    /**
     * Gets the annotation style panel.
     *
     * @param editClass the geometry class of the underlying placemark (useful
     *            for determining which editor options to display).
     * @return the annotation style panel
     */
    private JPanel getAnnotationStylePanel(Class<? extends Geometry> editClass)
    {
        myStylePanel = new AnnotationStyleEditorPanel(myPointPanelController.getToolbox(), true, false,
                Polygon.class.isAssignableFrom(editClass));
        DynamicEtchedBorder calloutBorder = new DynamicEtchedBorder(null, getCalloutPanelCheckbox());
        myStylePanel.setBorder(new ComponentTitledBorder(getCalloutPanelCheckbox(), myStylePanel, calloutBorder));
        return myStylePanel;
    }

    /**
     * Gets the callout panel checkbox.
     *
     * @return the callout panel checkbox
     */
    private JCheckBox getCalloutPanelCheckbox()
    {
        if (myUseCalloutPanelCheckBox == null)
        {
            myUseCalloutPanelCheckBox = new JCheckBox("Show Bubble");
            myUseCalloutPanelCheckBox.setBorder(null);
            myUseCalloutPanelCheckBox.setToolTipText("Check to show the bubble, uncheck to hide.");
            myUseCalloutPanelCheckBox.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent evt)
                {
                    if (evt.getSource().equals(myUseCalloutPanelCheckBox))
                    {
                        setCalloutParamsEnabled(myUseCalloutPanelCheckBox.isSelected());
                    }
                }
            });
            setCalloutParamsEnabled(false);
        }
        return myUseCalloutPanelCheckBox;
    }

    /**
     * Gets the content panel.
     *
     * @param editClass the geometry class of the underlying placemark (useful
     *            for determining which editor options to display).
     * @return the content panel
     */
    private JPanel getContentPanel(Class<? extends Geometry> editClass)
    {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel mainPanel = getMainPanel(editClass);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 0, 15));
        panel.add(mainPanel, BorderLayout.CENTER);
        panel.add(getSaveCancelPanel(), BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Gets the desc panel.
     *
     * @return the desc panel
     */
    private JPanel getDescPanel()
    {
        if (myDescPanel == null)
        {
            myDescPanel = new JPanel(new BorderLayout());
            myDescPanel.add(createSeparator("Description"), BorderLayout.NORTH);
            myDescArea.setLineWrap(true);
            myDescPanel.add(new JScrollPane(myDescArea), BorderLayout.CENTER);
        }
        return myDescPanel;
    }

    /**
     * Gets the left panel.
     *
     * @param editClass the geometry class of the underlying placemark (useful
     *            for determining which editor options to display).
     * @return the left panel
     */
    private JPanel getLeftPanel(Class<? extends Geometry> editClass)
    {
        GridBagPanel panel = new GridBagPanel();
        panel.init0();
        panel.fillHorizontal();
        panel.setInsets(0, 0, 5, 0);
        panel.addRow(getTitlePanel());
        panel.addRow(getEditLocPanel());
        panel.addRow(getTimePanel());
        panel.addRow(getAssociatedViewPanel());
        panel.fillBoth();
        panel.setInsets(0, 0, 0, 0);
        panel.addRow(getDescPanel());
        return panel;
    }

    /**
     * Gets the main panel.
     *
     * @param editClass the geometry class of the underlying placemark (useful
     *            for determining which editor options to display).
     * @return the main panel
     */
    private JPanel getMainPanel(Class<? extends Geometry> editClass)
    {
        JPanel panel = new JPanel(new GridLayout(1, 2));
        panel.add(getLeftPanel(editClass));
        JPanel rightPanel = getRightPanel(editClass);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        panel.add(rightPanel);
        return panel;
    }

    /**
     * Gets the right panel.
     *
     * @param editClass the geometry class of the underlying placemark (useful
     *            for determining which editor options to display).
     * @return the right panel
     */
    private JPanel getRightPanel(Class<? extends Geometry> editClass)
    {
        GridBagPanel panel = new GridBagPanel();
        panel.init0();
        panel.anchorWest();
        panel.setInsets(0, 5, 5, 0);
        panel.addRow(getShowHideFeatureCheckbox());
        panel.fillBoth();
        panel.setInsets(0, 0, 0, 0);
        panel.addRow(getAnnotationStylePanel(editClass));
        return panel;
    }

    /**
     * Gets the save cancel panel.
     *
     * @return the save cancel panel
     */
    private JPanel getSaveCancelPanel()
    {
        if (mySaveCancelPanel == null)
        {
            mySaveCancelPanel = new JPanel();
            mySaveCancelPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            mySaveCancelPanel.add(getSaveButton());
            mySaveCancelPanel.add(getCancelButton());
        }
        return mySaveCancelPanel;
    }

    /**
     * Gets the start time panel.
     *
     * @return the start time panel.
     */
    private GridBagPanel getTimePanel()
    {
        if (myTimePanel == null)
        {
            myDateTimePicker = new SlavedDateTimePicker(true);
            myDateTimePicker.getEndDateTimePicker().setEnabled(false);

            myStartTimePanel = new GridBagPanel();
            myStartTimePanel.setGridx(0).setGridy(0).setInsets(0, 55, 0, 5).add(new JLabel("Start Date:"));
            myStartTimePanel.setGridx(1).setInsets(0, 0, 0, 0).add(myDateTimePicker.getBeginDateTimePicker());
            myStartTimePanel.setGridx(2).fillHorizontalSpace();

            myEndTimePanel = new GridBagPanel();
            myHasEndDate = new JCheckBox("End Date:");
            myHasEndDate.addActionListener((evt) ->
            {
                myDateTimePicker.getEndDateTimePicker().setEnabled(myHasEndDate.isSelected());
            });
            myEndTimePanel.setGridx(0).setGridy(1).setInsets(0, 25, 0, 5).add(myHasEndDate);
            myEndTimePanel.setGridx(1).setInsets(0, 0, 0, 0).add(myDateTimePicker.getEndDateTimePicker());
            myEndTimePanel.setGridx(2).fillHorizontalSpace();

            myShowInTimeline = new JCheckBox("Show on timeline");
            myShowInTimeline.setSelected(true);
            myShouldAnimate = new JCheckBox("Animate");
            myShouldAnimate.setSelected(true);
            GridBagPanel visibilityPanel = new GridBagPanel();
            visibilityPanel.setGridx(0).setGridy(1).setInsets(0, 25, 0, 5).add(myShowInTimeline);
            visibilityPanel.setGridx(1).setInsets(0, 0, 0, 0).add(myShouldAnimate);

            myTimePanel = new GridBagPanel();
            myTimePanel.setGridx(0).setGridy(0).fillHorizontal().add(myStartTimePanel);
            myTimePanel.setGridy(1).add(myEndTimePanel);
            myTimePanel.setGridy(2).add(visibilityPanel);

            myHasTimeCheckBox = new JCheckBox("Has Time");
            myHasTimeCheckBox.addActionListener(evt -> enableDisableTimePanel());
            DynamicEtchedBorder calloutBorder = new DynamicEtchedBorder(null, myHasTimeCheckBox);
            myTimePanel.setBorder(new ComponentTitledBorder(myHasTimeCheckBox, myTimePanel, calloutBorder));
        }

        return myTimePanel;
    }

    /**
     * Gets the title panel.
     *
     * @return the title panel
     */
    private JPanel getTitlePanel()
    {
        if (myTitlePanel == null)
        {
            myTitlePanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();

            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(0, 0, 0, 5);
            myTitlePanel.add(new JLabel("Title:"), gbc);

            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 1;
            gbc.insets = new Insets(0, 0, 0, 0);
            gbc.weightx = 1.0;
            myTitlePanel.add(titleField, gbc);
        }
        return myTitlePanel;
    }

    /**
     * Initialize.
     *
     * @param editClass the geometry class of the underlying placemark (useful
     *            for determining which editor options to display).
     */
    private void initialize(Class<? extends Geometry> editClass)
    {
        if (!myIsInitialized)
        {
            setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
            getContentPane().setLayout(new BorderLayout());
            getContentPane().add(getContentPanel(editClass), BorderLayout.CENTER);
            pack();
            addComponentListener(new ComponentAdapter()
            {
                @Override
                public void componentHidden(ComponentEvent e)
                {
                    fireResultToListener(RESULT_CANCEL);
                }
            });
            myIsInitialized = true;

            getEnableAltitudeField().addActionListener(e -> getAltitudeField().setEnabled(getEnableAltitudeField().isSelected()));
        }
    }

    /**
     * Enables/disables the call-out parameters.
     *
     * @param selected true enable
     */
    private void setCalloutParamsEnabled(boolean selected)
    {
        myStylePanel.setPanelEnabled(selected);
    }

    /** If the MGRS text is not already uppercase, change it to uppercase. */
    private void toUpperMGRS()
    {
        final String upper = mgrsField.getText().toUpperCase();
        if (!upper.equals(mgrsField.getText()))
        {
            EventQueueUtilities.invokeLater(() ->
            {
                int carrot = mgrsField.getCaretPosition();
                mgrsField.setText(upper);
                mgrsField.setCaretPosition(carrot);
            });
        }
    }

    /**
     * Update associated view combo box values.
     *
     * @param selectedName the selected name
     */
    private void updateAssociatedViewComboBoxValues(String selectedName)
    {
        List<ViewBookmark> views = myPointPanelController.getToolbox().getMapManager().getViewBookmarkRegistry()
                .getViewBookmarks(new ViewBookmark.TypeAndNameComparator());
        List<Object> proxies = new LinkedList<>();
        BookmarkProxy selected = null;
        proxies.add("NONE");
        if (!views.isEmpty())
        {
            for (ViewBookmark vbm : views)
            {
                BookmarkProxy proxy = new BookmarkProxy(vbm);
                if (selectedName != null && selectedName.equals(vbm.getViewName()))
                {
                    selected = proxy;
                }

                proxies.add(proxy);
            }
        }
        ListComboBoxModel<Object> model = new ListComboBoxModel<>(proxies);
        myAssociatedViewComboBox.setModel(model);
        if (selected != null)
        {
            myAssociatedViewComboBox.setSelectedItem(selected);
        }
    }

    /**
     * The Class BookmarkProxy.
     */
    private static class BookmarkProxy extends ToStringProxy<ViewBookmark>
    {
        /**
         * Instantiates a new view book mark proxy.
         *
         * @param itemToProxy the item to proxy
         */
        public BookmarkProxy(ViewBookmark itemToProxy)
        {
            super(itemToProxy);
        }

        @Override
        public String toString()
        {
            return getItem() == null ? "NONE" : (getItem().is3D() ? "3D: " : "2D: ") + getItem().getViewName();
        }
    }
}
