package io.opensphere.wms.sld.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.xml.bind.JAXBElement;

import com.bric.swing.ColorPicker;

import io.opensphere.core.util.swing.ColorCircleIcon;
import io.opensphere.core.util.swing.DoubleSliderPanel;
import io.opensphere.core.util.swing.LinkedSliderTextField;
import io.opensphere.core.util.swing.LinkedSliderTextField.PanelSizeParameters;
import io.opensphere.core.util.swing.VerticalSpacerForGridbag;
import io.opensphere.wms.sld.SldRegistry;
import net.opengis.sld._100.CssParameter;
import net.opengis.sld._100.Fill;
import net.opengis.sld._100.Graphic;
import net.opengis.sld._100.Mark;
import net.opengis.sld._100.ParameterValueType;
import net.opengis.sld._100.PointSymbolizer;
import net.opengis.sld._100.Stroke;
import net.opengis.sld._100.SymbolizerType;

/**
 * The Class PointSymbolizerPanel. This is the panel that will contain all point
 * symbolizer related UI elements.
 */
public final class PointSymbolizerPanel extends AbstractSymbolizer
{
    /** Serial. */
    private static final long serialVersionUID = 1L;

//    /** The SLD. */
//    private StyledLayerDescriptor mySLD;

//    /** The Rotation. */
//    private int myRotation;

    /** The Main panel. */
    private JPanel myMainPanel;

    /** The Point shape combo. */
    private JComboBox<WellKnownName> myPointShapeCombo;

    /** The Point color button. */
    private JButton myPointColorButton;

    /** The Point color. */
    private Color myPointColor = Color.WHITE;

    /** The Point size spinner. */
    private JSpinner myPointSizeSpinner;

//    /** The Point size. */
//    private int myPointSize;

    /** The Point opacity slider. */
    private DoubleSliderPanel myPointOpacitySlider;

    /** The Point rotation slider. */
    private LinkedSliderTextField myPointRotationSlider;

//    /** The Point rotation degrees. */
//    private int myPointRotationDegrees;

//    /** The Point border panel. */
//    private JPanel myPointBorderPanel;

    /** The Point border color button. */
    private JButton myPointBorderColorButton;

    /** The Point border color. */
    private Color myPointBorderColor = Color.WHITE;

    /** The Point border width spinner. */
    private JSpinner myPointBorderWidthSpinner;

//    /** The Point border width. */
//    private int myPointBorderWidth;

    /** The Point border opacity slider. */
    private DoubleSliderPanel myPointBorderOpacitySlider;

//    /** The Sld registry. */
//    private final SldRegistry mySldRegistry;

    /**
     * Instantiates a new point symbolizer panel.
     *
     * @param sldRegistry the sld registry
     */
    public PointSymbolizerPanel(SldRegistry sldRegistry)
    {
        super();
//        mySldRegistry = sldRegistry;
        inititalize();
    }

    /**
     * Gets the main panel.
     *
     * @return the main panel
     */
    public JPanel getMainPanel()
    {
        if (myMainPanel == null)
        {
            myMainPanel = new JPanel(new GridBagLayout());
            myMainPanel.setBorder(BorderFactory.createTitledBorder("Point"));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(0, 0, 0, 0);
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            myMainPanel.add(getPointTypeColorPanel());

            gbc.gridy = 1;
            gbc.insets = new Insets(5, 0, 5, 0);
            myMainPanel.add(getPointSizeOpacityPanel(), gbc);

            gbc.gridy = 2;
            gbc.insets = new Insets(0, 0, 0, 0);
            myMainPanel.add(getPointRotationPanel(), gbc);

            gbc.gridy = 3;
            gbc.insets = new Insets(5, 5, 5, 5);
            myMainPanel.add(getPointBorderPanel(), gbc);

            VerticalSpacerForGridbag vs = new VerticalSpacerForGridbag(0, 4);
            myMainPanel.add(vs, vs.getGBConst());
        }
        return myMainPanel;
    }

    /**
     * Gets the point border color button.
     *
     * @return the point border color button
     */
    public JButton getPointBorderColorButton()
    {
        if (myPointBorderColorButton == null)
        {
            myPointBorderColorButton = new JButton();
            myPointBorderColorButton.setMaximumSize(new Dimension(20, 20));
            myPointBorderColorButton.setIcon(new ColorCircleIcon(Color.WHITE));
            myPointBorderColorButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    Color c = ColorPicker.showDialog(SwingUtilities.getWindowAncestor(PointSymbolizerPanel.this), "Select Color",
                            ((ColorCircleIcon)myPointBorderColorButton.getIcon()).getColor(), false);
                    if (c != null)
                    {
                        myPointBorderColor = c;
                        getPointBorderColorButton().setIcon(new ColorCircleIcon(c));
                    }
                }
            });
        }
        return myPointBorderColorButton;
    }

    /**
     * Gets the point border opacity slider.
     *
     * @return the point border opacity slider
     */
    public DoubleSliderPanel getPointBorderOpacitySlider()
    {
        if (myPointBorderOpacitySlider == null)
        {
            myPointBorderOpacitySlider = new DoubleSliderPanel("Opacity:", 0, 1, 3, 150);
            myPointBorderOpacitySlider.setOpaque(true);
            myPointBorderOpacitySlider.setBackground(getPointSizeSpinner().getBackground());
        }
        return myPointBorderOpacitySlider;
    }

    /**
     * Gets the point border width spinner.
     *
     * @return the point border width spinner
     */
    public JSpinner getPointBorderWidthSpinner()
    {
        if (myPointBorderWidthSpinner == null)
        {
            SpinnerNumberModel model = new SpinnerNumberModel(0, 0, 180, 1);
            myPointBorderWidthSpinner = new JSpinner(model);
        }
        return myPointBorderWidthSpinner;
    }

    /**
     * Gets the point color button.
     *
     * @return the point color button
     */
    public JButton getPointColorButton()
    {
        if (myPointColorButton == null)
        {
            myPointColorButton = new JButton();
            myPointColorButton.setMaximumSize(new Dimension(20, 20));
            myPointColorButton.setIcon(new ColorCircleIcon(Color.WHITE));
            myPointColorButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    Color c = ColorPicker.showDialog(SwingUtilities.getWindowAncestor(PointSymbolizerPanel.this), "Select Color",
                            ((ColorCircleIcon)myPointColorButton.getIcon()).getColor(), false);
                    if (c != null)
                    {
                        myPointColor = c;
                        getPointColorButton().setIcon(new ColorCircleIcon(c));
                    }
                }
            });
        }
        return myPointColorButton;
    }

    /**
     * Gets the point opacity slider.
     *
     * @return the point opacity slider
     */
    public DoubleSliderPanel getPointOpacitySlider()
    {
        if (myPointOpacitySlider == null)
        {
            myPointOpacitySlider = new DoubleSliderPanel("Opacity:", 0, 1, 3, 150);
            myPointOpacitySlider.setOpaque(true);
            myPointOpacitySlider.setBackground(getPointSizeSpinner().getBackground());
        }
        return myPointOpacitySlider;
    }

    /**
     * Gets the point rotation slider.
     *
     * @return the point rotation slider
     */
    public LinkedSliderTextField getPointRotationSlider()
    {
        if (myPointRotationSlider == null)
        {
            myPointRotationSlider = new LinkedSliderTextField("Rotation:", 0, 180, 5, new PanelSizeParameters(35, 24, 0));
            myPointRotationSlider.setValues(0);
            myPointRotationSlider.setOpaque(true);
            myPointRotationSlider.setBackground(getPointSizeSpinner().getBackground());
        }
        return myPointRotationSlider;
    }

    /**
     * Gets the point size spinner.
     *
     * @return the point size spinner
     */
    public JSpinner getPointSizeSpinner()
    {
        if (myPointSizeSpinner == null)
        {
            SpinnerNumberModel model = new SpinnerNumberModel(3, 1, 20, 1);
            myPointSizeSpinner = new JSpinner(model);
        }
        return myPointSizeSpinner;
    }

    @Override
    public JAXBElement<? extends SymbolizerType> validateInputs()
    {
        return getSymbolizer();
    }

    /**
     * Gets the point border opacity panel.
     *
     * @return the point border opacity panel
     */
    private JPanel getPointBorderOpacityPanel()
    {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(getPointBorderOpacitySlider(), gbc);

        return panel;
    }

    /**
     * Gets the point border panel.
     *
     * @return the point border panel
     */
    private JPanel getPointBorderPanel()
    {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.setBorder(BorderFactory.createTitledBorder("Border"));

        JPanel aPanel = new JPanel(new GridBagLayout());
        GridBagConstraints aPanelGBC = new GridBagConstraints();
        aPanelGBC.insets = new Insets(0, 0, 0, 0);
        aPanelGBC.gridx = 0;
        aPanelGBC.gridy = 0;
        aPanel.add(new JLabel("Color:"), aPanelGBC);

        aPanelGBC.gridx = 1;
        aPanel.add(getPointBorderColorButton(), aPanelGBC);

        aPanelGBC.gridx = 2;
        aPanelGBC.insets = new Insets(0, 10, 0, 0);
        aPanel.add(new JLabel("Width:"), aPanelGBC);

        aPanelGBC.gridx = 3;
        aPanelGBC.insets = new Insets(0, 0, 0, 0);
        aPanel.add(getPointBorderWidthSpinner(), aPanelGBC);

        panel.add(aPanel, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        panel.add(getPointBorderOpacityPanel(), gbc);

        return panel;
    }

    /**
     * Gets the point rotation panel.
     *
     * @return the point rotation panel
     */
    private JPanel getPointRotationPanel()
    {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(getPointRotationSlider(), gbc);

        return panel;
    }

    /**
     * Gets the point shape combo.
     *
     * @return the point shape combo
     */
    private JComboBox<WellKnownName> getPointShapeCombo()
    {
        if (myPointShapeCombo == null)
        {
            myPointShapeCombo = new JComboBox<WellKnownName>(WellKnownName.values());
        }
        return myPointShapeCombo;
    }

    /**
     * Gets the point size opacity panel.
     *
     * @return the point size opacity panel
     */
    private JPanel getPointSizeOpacityPanel()
    {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(new JLabel("Size:"), gbc);

        gbc.gridx = 1;
        panel.add(getPointSizeSpinner(), gbc);

        gbc.gridx = 2;
        gbc.insets = new Insets(0, 10, 0, 0);
        panel.add(getPointOpacitySlider(), gbc);

        return panel;
    }

    /**
     * Gets the point type color panel.
     *
     * @return the point type color panel
     */
    private JPanel getPointTypeColorPanel()
    {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Shape:"), gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 0, 10);
        panel.add(getPointShapeCombo(), gbc);

        gbc.gridx = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(new JLabel("Color:"), gbc);

        gbc.gridx = 3;
        panel.add(getPointColorButton(), gbc);

        return panel;
    }

    /**
     * Creates a new point SymbolizerType from UI inputs.
     *
     * @return the symbolizer
     */
    private JAXBElement<? extends SymbolizerType> getSymbolizer()
    {
        Mark mark = new Mark();
        mark.setWellKnownName(getPointShapeCombo().getSelectedItem().toString());
        Fill fill = new Fill();
        CssParameter cssParam = new CssParameter();
        cssParam.setName("fill");
        cssParam.getContent().add(getRGBtoHexColor(myPointColor.getRGB()));
        fill.getCssParameter().add(cssParam);

        CssParameter opacityParam = new CssParameter();
        opacityParam.setName("fill-opacity");
        opacityParam.getContent().add("1");
        fill.getCssParameter().add(opacityParam);

        mark.setFill(fill);

        CssParameter strokeColor = new CssParameter();
        strokeColor.setName("stroke");
        strokeColor.getContent().add(getRGBtoHexColor(myPointBorderColor.getRGB()));

        CssParameter strokeWidth = new CssParameter();
        strokeWidth.setName("stroke-width");
        strokeWidth.getContent().add(getPointBorderWidthSpinner().getValue().toString());

        Stroke stroke = new Stroke();
        stroke.getCssParameter().add(strokeColor);
        stroke.getCssParameter().add(strokeWidth);
        mark.setStroke(stroke);

        Graphic graphic = new Graphic();
        graphic.getExternalGraphicOrMark().add(mark);
        ParameterValueType size = new ParameterValueType();
        size.getContent().add(getPointSizeSpinner().getValue().toString());
        graphic.setSize(size);
        ParameterValueType rotation = new ParameterValueType();
        rotation.getContent().add(Integer.toString(getPointRotationSlider().getValue()));
        graphic.setRotation(rotation);

        ParameterValueType opacity = new ParameterValueType();
        opacity.getContent().add("1");
        graphic.setOpacity(opacity);

        PointSymbolizer symbolizer = new PointSymbolizer();
        symbolizer.setGraphic(graphic);

        net.opengis.sld._100.ObjectFactory factory = new net.opengis.sld._100.ObjectFactory();

        return factory.createPointSymbolizer(symbolizer);
    }

    /**
     * Inititalize.
     */
    private void inititalize()
    {
        setLayout(new BorderLayout());
        add(getMainPanel(), BorderLayout.CENTER);
    }
}
