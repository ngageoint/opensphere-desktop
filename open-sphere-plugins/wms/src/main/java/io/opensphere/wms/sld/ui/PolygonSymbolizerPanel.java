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
import io.opensphere.core.util.swing.VerticalSpacerForGridbag;
import io.opensphere.wms.sld.SldRegistry;
import net.opengis.sld._100.CssParameter;
import net.opengis.sld._100.Fill;
import net.opengis.sld._100.PolygonSymbolizer;
import net.opengis.sld._100.Stroke;
import net.opengis.sld._100.SymbolizerType;

/**
 * The Class PolygonSymbolizerPanel. This is the panel that will contain all
 * polygon symbolizer related UI elements.
 */
public final class PolygonSymbolizerPanel extends AbstractSymbolizer
{
    /** Serial. */
    private static final long serialVersionUID = 1L;

    /** The Main panel. */
    private JPanel myMainPanel;

    /** The Point color button. */
    private JButton myPolyColorButton;

    /** The Point color. */
    private Color myPolyColor = Color.WHITE;

//    /** The Point size. */
//    private int myPointSize;

    /** The Point opacity slider. */
    private DoubleSliderPanel myPolyOpacitySlider;

    /** The Point rotation slider. */
    private LinkedSliderTextField myPointRotationSlider;

//    /** The Point rotation degrees. */
//    private int myPointRotationDegrees;

//    /** The Point border panel. */
//    private JPanel myPointBorderPanel;

    /** The Point border color button. */
    private JButton myPolyBorderColorButton;

    /** The Point border color. */
    private Color myPointBorderColor = Color.WHITE;

    /** The Point border width spinner. */
    private JSpinner myPolyBorderWidthSpinner;

//    /** The Point border width. */
//    private int myPointBorderWidth;

    /** The Point border opacity slider. */
    private DoubleSliderPanel myPolyBorderOpacitySlider;

    /** The Original background color. */
    private Color myOriginalBackgroundColor;

//    /** The Sld registry. */
//    private final SldRegistry mySldRegistry;

    /**
     * Instantiates a new point symbolizer panel.
     *
     * @param sldRegistry the sld registry
     */
    public PolygonSymbolizerPanel(SldRegistry sldRegistry)
    {
        super();
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
            myMainPanel.setBorder(BorderFactory.createTitledBorder("Polygon"));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(0, 0, 0, 0);
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            myMainPanel.add(getPolyColorOpacityPanel());

            gbc.gridy = 3;
            gbc.insets = new Insets(5, 5, 5, 5);
            myMainPanel.add(getPolyBorderPanel(), gbc);

            VerticalSpacerForGridbag vs = new VerticalSpacerForGridbag(0, 4);
            myMainPanel.add(vs, vs.getGBConst());
        }
        return myMainPanel;
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
            myPointRotationSlider = new LinkedSliderTextField("Rotation:", 0, 180, 5,
                    new LinkedSliderTextField.PanelSizeParameters(35, 24, 0));
            myPointRotationSlider.setValues(0);
            myPointRotationSlider.setOpaque(true);
            myPointRotationSlider.setBackground(myOriginalBackgroundColor);
        }
        return myPointRotationSlider;
    }

    /**
     * Gets the poly border color button.
     *
     * @return the poly border color button
     */
    public JButton getPolyBorderColorButton()
    {
        if (myPolyBorderColorButton == null)
        {
            myPolyBorderColorButton = new JButton();
            myPolyBorderColorButton.setMaximumSize(new Dimension(20, 20));
            myPolyBorderColorButton.setIcon(new ColorCircleIcon(Color.WHITE));
            myPolyBorderColorButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    Color c = ColorPicker.showDialog(SwingUtilities.getWindowAncestor(PolygonSymbolizerPanel.this),
                            "Select Color", ((ColorCircleIcon)myPolyBorderColorButton.getIcon()).getColor(), false);
                    if (c != null)
                    {
                        myPointBorderColor = c;
                        getPolyBorderColorButton().setIcon(new ColorCircleIcon(c));
                    }
                }
            });
        }
        return myPolyBorderColorButton;
    }

    /**
     * Gets the poly border opacity slider.
     *
     * @return the poly border opacity slider
     */
    public DoubleSliderPanel getPolyBorderOpacitySlider()
    {
        if (myPolyBorderOpacitySlider == null)
        {
            myPolyBorderOpacitySlider = new DoubleSliderPanel("Opacity:", 0, 1, 3, 150);
            myPolyBorderOpacitySlider.setOpaque(true);
            myPolyBorderOpacitySlider.setBackground(myOriginalBackgroundColor);
        }
        return myPolyBorderOpacitySlider;
    }

    /**
     * Gets the poly border width spinner.
     *
     * @return the poly border width spinner
     */
    public JSpinner getPolyBorderWidthSpinner()
    {
        if (myPolyBorderWidthSpinner == null)
        {
            SpinnerNumberModel model = new SpinnerNumberModel(0, 0, 180, 1);
            myPolyBorderWidthSpinner = new JSpinner(model);
        }
        return myPolyBorderWidthSpinner;
    }

    /**
     * Gets the poly color button.
     *
     * @return the poly color button
     */
    public JButton getPolyColorButton()
    {
        if (myPolyColorButton == null)
        {
            myPolyColorButton = new JButton();
            myPolyColorButton.setMaximumSize(new Dimension(20, 20));
            myPolyColorButton.setIcon(new ColorCircleIcon(Color.WHITE));
            myPolyColorButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    Color c = ColorPicker.showDialog(SwingUtilities.getWindowAncestor(PolygonSymbolizerPanel.this),
                            "Select Color", ((ColorCircleIcon)myPolyColorButton.getIcon()).getColor(), false);
                    if (c != null)
                    {
                        myPolyColor = c;
                        getPolyColorButton().setIcon(new ColorCircleIcon(c));
                    }
                }
            });
        }
        return myPolyColorButton;
    }

    /**
     * Gets the poly opacity slider.
     *
     * @return the poly opacity slider
     */
    public DoubleSliderPanel getPolyOpacitySlider()
    {
        if (myPolyOpacitySlider == null)
        {
            myPolyOpacitySlider = new DoubleSliderPanel("Opacity:", 0, 1, 3, 150);
            myPolyOpacitySlider.setOpaque(true);
            myPolyOpacitySlider.setBackground(myOriginalBackgroundColor);
        }
        return myPolyOpacitySlider;
    }

    @Override
    public JAXBElement<? extends SymbolizerType> validateInputs()
    {
        return getSymbolizer();
    }

    /**
     * Gets the poly border opacity panel.
     *
     * @return the poly border opacity panel
     */
    private JPanel getPolyBorderOpacityPanel()
    {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(getPolyBorderOpacitySlider(), gbc);

        return panel;
    }

    /**
     * Gets the point border panel.
     *
     * @return the point border panel
     */
    private JPanel getPolyBorderPanel()
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
        aPanel.add(getPolyBorderColorButton(), aPanelGBC);

        aPanelGBC.gridx = 2;
        aPanelGBC.insets = new Insets(0, 10, 0, 0);
        aPanel.add(new JLabel("Width:"), aPanelGBC);

        aPanelGBC.gridx = 3;
        aPanelGBC.insets = new Insets(0, 0, 0, 0);
        aPanel.add(getPolyBorderWidthSpinner(), aPanelGBC);

        panel.add(aPanel, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        panel.add(getPolyBorderOpacityPanel(), gbc);

        return panel;
    }

    /**
     * Gets the point size opacity panel.
     *
     * @return the point size opacity panel
     */
    private JPanel getPolyColorOpacityPanel()
    {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(new JLabel("Color:"), gbc);

        gbc.gridx = 1;
        panel.add(getPolyColorButton(), gbc);

        gbc.gridx = 2;
        gbc.insets = new Insets(0, 10, 0, 0);
        panel.add(getPolyOpacitySlider(), gbc);

        return panel;
    }

    /**
     * Creates a new point SymbolizerType from UI inputs.
     *
     * @return the symbolizer
     */
    private JAXBElement<? extends SymbolizerType> getSymbolizer()
    {
        Fill fill = new Fill();
        CssParameter cssParam = new CssParameter();
        cssParam.setName("fill");
        cssParam.getContent().add(getRGBtoHexColor(myPolyColor.getRGB()));
        fill.getCssParameter().add(cssParam);

        CssParameter opacityParam = new CssParameter();
        opacityParam.setName("fill-opacity");
        double polyOpacity = getPolyOpacitySlider().getDoubleValue();
        opacityParam.getContent().add(Double.toString(polyOpacity));
        fill.getCssParameter().add(opacityParam);

        CssParameter strokeColor = new CssParameter();
        strokeColor.setName("stroke");
        strokeColor.getContent().add(getRGBtoHexColor(myPointBorderColor.getRGB()));

        CssParameter strokeWidth = new CssParameter();
        strokeWidth.setName("stroke-width");
        strokeWidth.getContent().add(getPolyBorderWidthSpinner().getValue().toString());

        Stroke stroke = new Stroke();
        stroke.getCssParameter().add(strokeColor);
        stroke.getCssParameter().add(strokeWidth);

        PolygonSymbolizer symbolizer = new PolygonSymbolizer();
        symbolizer.setFill(fill);
        symbolizer.setStroke(stroke);

        net.opengis.sld._100.ObjectFactory factory = new net.opengis.sld._100.ObjectFactory();

        return factory.createPolygonSymbolizer(symbolizer);
    }

    /**
     * Inititalize.
     */
    private void inititalize()
    {
        myOriginalBackgroundColor = getBackground();
        setLayout(new BorderLayout());
        add(getMainPanel(), BorderLayout.CENTER);
    }
}
