package io.opensphere.myplaces.editor.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.event.PopupMenuEvent;

import org.apache.commons.lang3.StringUtils;

import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.ChangeListener;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.DefaultValidatorSupport;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.Validatable;
import io.opensphere.core.util.swing.ComponentUtilities;
import io.opensphere.core.util.swing.FontComboBoxRenderer;
import io.opensphere.core.util.swing.FontWrapper;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.core.util.swing.PopupMenuAdapter;
import io.opensphere.core.util.swing.input.controller.AbstractController;
import io.opensphere.core.util.swing.input.controller.ControllerFactory;
import io.opensphere.core.util.swing.input.model.AbstractViewModel;
import io.opensphere.core.util.swing.input.model.ViewModel;
import io.opensphere.myplaces.editor.model.AnnotationModel;
import io.opensphere.myplaces.util.OptionsAccessor;

/** Panel for editing map point annotation style. */
public class AnnotationStyleEditorPanel extends GridBagPanel implements Validatable
{
    /** The double width style name. */
    private static final String DOUBLE = "double";

    /** The input style name. */
    private static final String INPUT = "input";

    /** The label style name. */
    private static final String LABEL = "label";

    /** The separator style name. */
    private static final String SEPARATOR = "separator";

    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The decimal check box. */
    private transient JComponent myDecimalCheckBox;

    /** The show distance check box. */
    private transient JComponent myDistanceCheckBox;

    /** The dms check box. */
    private transient JComponent myDmsCheckBox;

    /** The font preview label. */
    private final JTextField myFontPreviewLabel = new JTextField();

    /** The show heading check box. */
    private transient JComponent myHeadingCheckBox;

    /** Check box used to control the display state of the velocity field. */
    private transient JComponent myVelocityCheckBox;

    /** Check box used to control the display state of the duration field. */
    private transient JComponent myDurationCheckBox;

    /** The mgrs check box. */
    private transient JComponent myMgrsCheckBox;

    /** The altitude check box. */
    private transient JComponent myAltitudeCheckBox;

    /** The underlying model. */
    private final transient AnnotationModel myModel;

    /** Indicates that a placemark should be able to animate. */
    private JComponent myShouldAnimate;

    /** Indicates that a placemark should show in the timeline or not. */
    private JComponent myShowInTimeline;

    /** The title of the map point. */
    private String myTitle = "Example";

    /** The toolbox. */
    private final transient Toolbox myToolbox;

    /** The 'use default' button. */
    private JButton myUseDefaultButton;

    /** The validation support. */
    private final transient DefaultValidatorSupport myValidatorSupport = new DefaultValidatorSupport(this);

    /**
     * Constructor.
     *
     * @param toolbox The toolbox
     * @param showUseDefault Whether to show the 'use default' button.
     * @param showTimeOptions Indicates if the show in timeline and animate
     *            check boxes should be displayed.
     * @param showPolygonOptions A flag used to show polygon editor options. If
     *            False, these editor options are omitted.
     */
    public AnnotationStyleEditorPanel(Toolbox toolbox, boolean showUseDefault, boolean showTimeOptions,
            boolean showPolygonOptions)
    {
        super();
        myToolbox = toolbox;
        myModel = new AnnotationModel();

        myFontPreviewLabel.setOpaque(true);
        myFontPreviewLabel.setEditable(false);

        initialize(showUseDefault, showTimeOptions, showPolygonOptions);
        myModel.addListener(new ChangeListener<Placemark>()
        {
            @Override
            public void changed(ObservableValue<? extends Placemark> observable, Placemark oldValue, Placemark newValue)
            {
                updatePreviewLabel();
                getValidatorSupport().setValidationResult(getModel().getValidationStatus(), getModel().getErrorMessage());
            }
        });
    }

    /**
     * Getter for model.
     *
     * @return the model
     */
    public AnnotationModel getModel()
    {
        return myModel;
    }

    @Override
    public DefaultValidatorSupport getValidatorSupport()
    {
        return myValidatorSupport;
    }

    /**
     * Sets the enabled state of the panel.
     *
     * @param isEnabled Whether to enable it
     */
    public void setPanelEnabled(boolean isEnabled)
    {
        myModel.setEnabled(isEnabled);
        myUseDefaultButton.setEnabled(isEnabled);
    }

    /**
     * Sets the point.
     *
     * @param placemark the placemark
     */
    public void setPlacemark(Placemark placemark)
    {
        myTitle = placemark == null || StringUtils.isBlank(placemark.getName()) ? "Example" : placemark.getName();
        myModel.set(placemark);
        if (!myModel.isDistanceHeadingCapable())
        {
            myDistanceCheckBox.setVisible(false);
            myHeadingCheckBox.setVisible(false);
        }

        if (!myModel.isLocationCapable())
        {
            myMgrsCheckBox.setVisible(false);
            myDmsCheckBox.setVisible(false);
            myDecimalCheckBox.setVisible(false);
        }

        if (!myModel.isLocationCapable() || !(myModel.get().getGeometry() instanceof Point))
        {
            myAltitudeCheckBox.setVisible(false);
        }

        if (!myModel.isVelocityCapable())
        {
            myVelocityCheckBox.setVisible(false);
        }

        if (!myModel.isDurationCapable())
        {
            myDurationCheckBox.setVisible(false);
        }
    }

    /**
     * Update preview text.
     *
     * @param text the text
     */
    public void updatePreviewText(String text)
    {
        myTitle = text;
        myFontPreviewLabel.setText(myTitle);
    }

    /**
     * Adds a model.
     *
     * @param model The model
     * @return The component that was added
     */
    private JComponent addModel(AbstractViewModel<?> model)
    {
        AbstractController<?, ? extends ViewModel<?>, ? extends JComponent> controller = ControllerFactory.createController(model,
                null, null);
        JComponent view = controller.getView();
        if (view instanceof JCheckBox)
        {
            JCheckBox box = (JCheckBox)view;
            box.setText(null);
        }
        style(LABEL, INPUT).addRow(controller.getLabel(), controller.getView());
        return controller.getView();
    }

    /**
     * Adds a horizontal separator with a label.
     *
     * @param text The label text
     */
    private void addSeparator(String text)
    {
        GridBagPanel panel = new GridBagPanel();
        panel.setInsets(6, 0, 3, 3);
        panel.add(new JLabel(text));
        panel.fillHorizontal();
        panel.setInsets(6, 0, 3, 0);
        panel.add(new JSeparator());

        style(SEPARATOR).addRow(panel);
    }

    /**
     * Creates a checkbox with null border.
     *
     * @param model The model
     * @return The checkbox
     */
    private JComponent createCheckBox(AbstractViewModel<?> model)
    {
        JComponent checkBox = ControllerFactory.createComponent(model);
        checkBox.setBorder(null);
        return checkBox;
    }

    /**
     * Gets the bubble contents panel.
     *
     * @return The bubble contents panel
     */
    private JPanel getBubbleContentsPanel()
    {
        GridBagPanel panel = new GridBagPanel();
        panel.init0();
        panel.anchorWest();
        panel.setInsets(0, 0, 0, 10);
        myDistanceCheckBox = createCheckBox(myModel.getShowDistance());
        myHeadingCheckBox = createCheckBox(myModel.getShowHeading());
        myVelocityCheckBox = createCheckBox(myModel.getShowVelocity());
        myDurationCheckBox = createCheckBox(myModel.getShowDuration());
        myMgrsCheckBox = createCheckBox(myModel.getShowMGRSLatLon());
        myDmsCheckBox = createCheckBox(myModel.getShowDMSLatLon());
        myDecimalCheckBox = createCheckBox(myModel.getShowDecimalLatLon());
        myAltitudeCheckBox = createCheckBox(myModel.getShowAltitude());
        panel.addRow(createCheckBox(myModel.getShowTitle()), createCheckBox(myModel.getShowDescription()),
                createCheckBox(myModel.getShowFieldTitles()));
        panel.addRow(myDistanceCheckBox, myHeadingCheckBox, myVelocityCheckBox);
        panel.addRow(myMgrsCheckBox, myDmsCheckBox, myDecimalCheckBox);
        panel.addRow(myDurationCheckBox);
        panel.addRow(myAltitudeCheckBox);
        return panel;
    }

    /**
     * Gets the font style panel.
     *
     * @return The font style panel
     */
    private JPanel getFontStylePanel()
    {
        GridBagPanel panel = new GridBagPanel();
        panel.anchorWest();
        panel.add(ControllerFactory.createComponent(myModel.getTextStyleModel().getBold()));
        panel.add(ControllerFactory.createComponent(myModel.getTextStyleModel().getItalic()));
        return panel;
    }

    /**
     * Gets the panel with the various time options a user can set on a my
     * place.
     *
     * @return The time options panel.
     */
    private JPanel getTimePanel()
    {
        myShowInTimeline = createCheckBox(myModel.getShowInTimeline());
        myShouldAnimate = createCheckBox(myModel.getAnimate());
        GridBagPanel visibilityPanel = new GridBagPanel();
        visibilityPanel.setGridx(0).setGridy(1).setInsets(0, 25, 0, 5).add(myShowInTimeline);
        visibilityPanel.setGridx(1).setInsets(0, 0, 0, 0).add(myShouldAnimate);

        return visibilityPanel;
    }

    /**
     * Gets the use default button.
     *
     * @return The use default button
     */
    private JButton getUseDefaultButton()
    {
        myUseDefaultButton = new JButton("Use Default");
        myUseDefaultButton.setMargin(new Insets(3, 6, 3, 6));
        myUseDefaultButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                OptionsAccessor options = new OptionsAccessor(myToolbox);
                Placemark userDefaultPlacemark = options.getDefaultPlacemark();
                getModel().set(userDefaultPlacemark);
                getModel().setChanged(true);
            }
        });
        return myUseDefaultButton;
    }

    /**
     * Initializes the panel.
     *
     * @param showUseDefault Whether to show the 'use default' button
     * @param showTimeOptions Indicates if the show in timeline and animate
     *            check boxes should be displayed.
     * @param showPolygonOptions indicates that polygon editor fields should be displayed.
     */
    private void initialize(boolean showUseDefault, boolean showTimeOptions, boolean showPolygonOptions)
    {
        init0();

        // Define the styles
        style(SEPARATOR).anchorWest().fillHorizontal().setGridwidth(2).setInsets(0, 9, 0, 5);
        style(LABEL).anchorWest().fillNone().setGridwidth(1).setInsets(0, 23, 0, 3);
        style(INPUT).anchorWest().fillNone().setGridwidth(1).setInsets(0, 0, 0, 5);
        style(DOUBLE).anchorWest().fillNone().setGridwidth(2).setInsets(0, 23, 0, 5);

        addSeparator("Bubble Contents");

        style(DOUBLE).addRow(getBubbleContentsPanel());

        if (showTimeOptions)
        {
            addSeparator("Time Options");

            style(DOUBLE).addRow(getTimePanel());
        }

        addSeparator("Text Style");

        @SuppressWarnings("unchecked")
        JComboBox<FontWrapper> fontCombo = (JComboBox<FontWrapper>)addModel(myModel.getTextStyleModel().getFont());

        // Don't set the renderer until the popup is going to be shown, because
        // rendering all the fonts is slow the first time.
        fontCombo.addPopupMenuListener(new PopupMenuAdapter()
        {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e)
            {
                fontCombo.setRenderer(new FontComboBoxRenderer());
            }
        });

        fontCombo.setMaximumRowCount(10);
        ComponentUtilities.setPreferredWidth(fontCombo, 190);

        addModel(myModel.getTextStyleModel().getFontSize());

        JLabel fontStyleLabel = new JLabel("Style:");
        fontStyleLabel.setEnabled(myModel.getTextStyleModel().getBold().isEnabled());
        JPanel fontStylePanel = getFontStylePanel();
        fontStyleLabel.setLabelFor(fontStylePanel.getComponent(0));
        style(LABEL, INPUT).addRow(fontStyleLabel, fontStylePanel);

        addModel(myModel.getTextStyleModel().getFontColor());

        addSeparator("Bubble Style");

        addModel(myModel.getBorderColor());

        JCheckBox fillCheckBox = (JCheckBox)addModel(myModel.getBubbleFilled());
        fillCheckBox.setText(null);

        if (showPolygonOptions)
        {
            addSeparator("Polygon Style");
            addModel(myModel.getPolygonFillColor());

            JCheckBox polygonFillCheckBox = (JCheckBox)addModel(myModel.getPolygonFilled());
            polygonFillCheckBox.setText(null);
        }

        if (showUseDefault)
        {
            addSeparator("Use Default");

            style(LABEL, INPUT).addRow(null, getUseDefaultButton());
        }

        addSeparator("Preview");

        JScrollPane scroll = new JScrollPane(myFontPreviewLabel);
        scroll.setPreferredSize(new Dimension(0, 30));
        style(DOUBLE).fillBoth().setInsets(0, 23, 5, 5);
        addRow(scroll);
    }

    /** Updates the preview label. */
    void updatePreviewLabel()
    {
        Color bgColor;
        if (myModel.getBubbleFilled().get() != null && myModel.getBubbleFilled().get().booleanValue())
        {
            bgColor = ColorUtilities.blendColors(myModel.getBorderColor().get(), getBackground());
        }
        else
        {
            bgColor = getBackground();
        }

        myFontPreviewLabel.setText(myTitle);
        myFontPreviewLabel.setFont(myModel.getTextStyleModel().getSelectedFont());
        myFontPreviewLabel.setForeground(myModel.getTextStyleModel().getFontColor().get());
        myFontPreviewLabel.setBackground(bgColor);
        repaint();
    }
}
