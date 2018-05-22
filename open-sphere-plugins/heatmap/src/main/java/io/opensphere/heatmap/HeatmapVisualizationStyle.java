package io.opensphere.heatmap;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.MapVisualizationStyleCategory;
import io.opensphere.mantle.data.VisualizationSupport;
import io.opensphere.mantle.data.geom.style.InterpolatedTileVisualizationStyle;
import io.opensphere.mantle.data.geom.style.MutableVisualizationStyle;
import io.opensphere.mantle.data.geom.style.ParameterHint;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameter;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameterFlags;
import io.opensphere.mantle.data.geom.style.impl.AbstractVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.StyleUtils;
import io.opensphere.mantle.data.geom.style.impl.ui.AbstractStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.ComboBoxStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.GroupedMiniStyleEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.GroupedStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.IntegerSliderStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.PanelBuilder;
import io.opensphere.mantle.data.geom.style.impl.ui.StyleParameterEditorGroupPanel;
import io.opensphere.mantle.data.tile.InterpolatedTileVisualizationSupport;

/**
 * The visualization style used for heatmaps.
 */
public class HeatmapVisualizationStyle extends AbstractVisualizationStyle implements InterpolatedTileVisualizationStyle
{
    /** The prefix common to all heatmap style keys. */
    public static final String PROPERTY_KEY_PREFIX = "HeatmapVisualizationStyle";

    /** The key used for the heatmap color style. */
    public static final String COLOR_PALETTE_PROPERTY_KEY = PROPERTY_KEY_PREFIX + ".ColorPalette";

    /** The key used for the heatmap intensity property */
    public static final String INTENSITY_PROPERTY_KEY = PROPERTY_KEY_PREFIX + ".Intensity";

    /** The key used for the heatmap size property */
    public static final String SIZE_PROPERTY_KEY = PROPERTY_KEY_PREFIX + ".Size";

    /** The default value of the color palette parameter. */
    public static final VisualizationStyleParameter DEFAULT_COLOR_PALETTE_PARAMETER = new VisualizationStyleParameter(
            COLOR_PALETTE_PROPERTY_KEY, "Color Palette", HeatmapGradients.THERMAL, HeatmapGradients.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(false, false));

    /** The default value of the intensity parameter. */
    public static final VisualizationStyleParameter DEFAULT_INTENSITY_PARAMETER = new VisualizationStyleParameter(
            INTENSITY_PROPERTY_KEY, "Intensity", Integer.valueOf(6), Integer.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(false, false));

    /** The default value of the size parameter. */
    public static final VisualizationStyleParameter DEFAULT_SIZE_PARAMETER = new VisualizationStyleParameter(SIZE_PROPERTY_KEY,
            "Size", Integer.valueOf(50), Integer.class, new VisualizationStyleParameterFlags(false, false),
            ParameterHint.hint(false, false));

    /** The tooltip for the Intensity slider. */
    private static final String intensityTooltip = "Controls the number of points required for maximum intensity. "
            + "A higher number maps to a higher concentration of points.";

    /** The tooltip for the Size slider. */
    private static final String sizeTooltip = "Controls the size of the heat zone tiles.";

    /**
     * Creates a new instance of the {@link HeatmapVisualizationStyle} class.
     *
     * @param toolbox the toolbox through which application state is accessed.
     */
    public HeatmapVisualizationStyle(Toolbox toolbox)
    {
        super(toolbox);
    }

    /**
     * Instantiates a new polygon feature visualization style.
     *
     * @param toolbox the toolbox through which application state is accessed.
     * @param dtiKey the data type key
     */
    public HeatmapVisualizationStyle(Toolbox toolbox, String dtiKey)
    {
        super(toolbox, dtiKey);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.geom.style.impl.AbstractTileVisualizationStyle#initialize()
     */
    @Override
    public void initialize()
    {
        setParameter(DEFAULT_COLOR_PALETTE_PARAMETER);
        setParameter(DEFAULT_INTENSITY_PARAMETER);
        setParameter(DEFAULT_SIZE_PARAMETER);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.geom.style.impl.AbstractTileVisualizationStyle#initialize(java.util.Set)
     */
    @Override
    public void initialize(Set<VisualizationStyleParameter> parameterSet)
    {
        for (VisualizationStyleParameter parameter : parameterSet)
        {
            if (parameter.getKey() != null && parameter.getKey().startsWith(PROPERTY_KEY_PREFIX))
            {
                setParameter(parameter);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.geom.style.impl.AbstractTileVisualizationStyle#clone()
     */
    @Override
    public HeatmapVisualizationStyle clone()
    {
        return (HeatmapVisualizationStyle)super.clone();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.geom.style.VisualizationStyle#deriveForType(java.lang.String)
     */
    @Override
    public VisualizationStyle deriveForType(String dtiKey)
    {
        HeatmapVisualizationStyle zClone = clone();
        zClone.setDTIKey(dtiKey);
        zClone.initializeFromDataType();
        return zClone;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.geom.style.VisualizationStyle#getConvertedClassType()
     */
    @Override
    public Class<? extends VisualizationSupport> getConvertedClassType()
    {
        return InterpolatedTileVisualizationSupport.class;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.geom.style.VisualizationStyle#getStyleCategory()
     */
    @Override
    public MapVisualizationStyleCategory getStyleCategory()
    {
        return MapVisualizationStyleCategory.IMAGE_TILE;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.geom.style.VisualizationStyle#getStyleDescription()
     */
    @Override
    public String getStyleDescription()
    {
        return "Heatmap tile styles.\n\nIntensity: " + intensityTooltip + "\nSize: " + sizeTooltip;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.geom.style.VisualizationStyle#getStyleName()
     */
    @Override
    public String getStyleName()
    {
        return "Heatmap";
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.geom.style.VisualizationStyle#newInstance(io.opensphere.core.Toolbox)
     */
    @Override
    public VisualizationStyle newInstance(Toolbox tb)
    {
        HeatmapVisualizationStyle style = new HeatmapVisualizationStyle(tb);
        style.initialize();

        return style;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.geom.style.impl.AbstractTileVisualizationStyle#getMiniUIPanel()
     */
    @Override
    public GroupedMiniStyleEditorPanel getMiniUIPanel()
    {
        GroupedMiniStyleEditorPanel panel = new GroupedMiniStyleEditorPanel(this);

        List<AbstractStyleParameterEditorPanel> paramList = New.list();
        MutableVisualizationStyle style = panel.getChangedStyle();

        VisualizationStyleParameter colorPaletteParameter = style.getStyleParameter(COLOR_PALETTE_PROPERTY_KEY);
        paramList.add(
                new ComboBoxStyleParameterEditorPanel(StyleUtils.createComboBoxMiniPanelBuilder(colorPaletteParameter.getName()),
                        style, COLOR_PALETTE_PROPERTY_KEY, false, false, false, Arrays.asList(HeatmapGradients.values())));

        IntegerSliderStyleParameterEditorPanel intensityPanel = new IntegerSliderStyleParameterEditorPanel(
                StyleUtils.createSliderMiniPanelBuilder(style.getStyleParameter(INTENSITY_PROPERTY_KEY).getName()), style,
                INTENSITY_PROPERTY_KEY, true, false, 5, 50, null);
        intensityPanel.setToolTipText(intensityTooltip);
        paramList.add(intensityPanel);

        VisualizationStyleParameter sizeParameter = style.getStyleParameter(SIZE_PROPERTY_KEY);
        IntegerSliderStyleParameterEditorPanel sizePanel = new IntegerSliderStyleParameterEditorPanel(
                StyleUtils.createSliderMiniPanelBuilder(sizeParameter.getName()), style, SIZE_PROPERTY_KEY, true, false, 1, 150,
                null);
        sizePanel.setToolTipText(sizeTooltip);
        paramList.add(sizePanel);

        StyleParameterEditorGroupPanel parameterGroup = new StyleParameterEditorGroupPanel("Heatmap", paramList, false, 1);
        panel.addGroupAtTop(parameterGroup);
        return panel;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.geom.style.impl.AbstractTileVisualizationStyle#getUIPanel()
     */
    @Override
    public GroupedStyleParameterEditorPanel getUIPanel()
    {
        GroupedStyleParameterEditorPanel panel = new GroupedStyleParameterEditorPanel(this, true);

        List<AbstractStyleParameterEditorPanel> paramList = New.list();
        MutableVisualizationStyle style = panel.getChangedStyle();

        VisualizationStyleParameter colorPaletteParameter = style.getStyleParameter(COLOR_PALETTE_PROPERTY_KEY);
        paramList.add(new ComboBoxStyleParameterEditorPanel(PanelBuilder.get(colorPaletteParameter.getName()), style,
                COLOR_PALETTE_PROPERTY_KEY, false, false, false, Arrays.asList(HeatmapGradients.values())));

        VisualizationStyleParameter intensityParameter = style.getStyleParameter(INTENSITY_PROPERTY_KEY);
        paramList.add(new IntegerSliderStyleParameterEditorPanel(PanelBuilder.get(intensityParameter.getName()), style,
                INTENSITY_PROPERTY_KEY, true, false, 5, 50, null));

        VisualizationStyleParameter sizeParameter = style.getStyleParameter(SIZE_PROPERTY_KEY);
        paramList.add(new IntegerSliderStyleParameterEditorPanel(PanelBuilder.get(sizeParameter.getName()), style,
                SIZE_PROPERTY_KEY, true, false, 1, 150, null));

        StyleParameterEditorGroupPanel parameterGroup = new StyleParameterEditorGroupPanel("Heatmap", paramList, false, 1);
        panel.addGroup(parameterGroup);

        return panel;
    }

    /**
     * Sets the value of the style parameter associated with the
     * {@link #COLOR_PALETTE_PROPERTY_KEY} field.
     *
     * @param colorPalette the value of the style parameter associated with the
     *            {@link #COLOR_PALETTE_PROPERTY_KEY} field.
     */
    public void setColorPalette(HeatmapGradients colorPalette)
    {
        setParameter(getStyleParameter(COLOR_PALETTE_PROPERTY_KEY).deriveWithNewValue(colorPalette));
    }

    /**
     * Sets the value of the style parameter associated with the
     * {@link #INTENSITY_PROPERTY_KEY} field.
     *
     * @param intensity the value of the style parameter associated with the
     *            {@link #INTENSITY_PROPERTY_KEY} field.
     */
    public void setIntensity(int intensity)
    {
        setParameter(getStyleParameter(INTENSITY_PROPERTY_KEY).deriveWithNewValue(Integer.valueOf(intensity)));
    }

    /**
     * Sets the value of the style parameter associated with the
     * {@link #SIZE_PROPERTY_KEY} field.
     *
     * @param size the value of the style parameter associated with
     *            the{@link #SIZE_PROPERTY_KEY} field.
     */
    public void setSize(int size)
    {
        setParameter(getStyleParameter(SIZE_PROPERTY_KEY).deriveWithNewValue(Integer.valueOf(size)));
    }

    /**
     * Gets the value of the style parameter associated with the
     * {@link #COLOR_PALETTE_PROPERTY_KEY} field.
     *
     * @return the value of the style parameter associated with the
     *         {@link #COLOR_PALETTE_PROPERTY_KEY} field.
     */
    public HeatmapGradients getColorPalette()
    {
        return (HeatmapGradients)getStyleParameter(COLOR_PALETTE_PROPERTY_KEY).getValue();
    }

    /**
     * Gets the value of the style parameter associated with the
     * {@link #INTENSITY_PROPERTY_KEY} field.
     *
     * @return the value of the style parameter associated with the
     *         {@link #INTENSITY_PROPERTY_KEY} field.
     */
    public int getIntensity()
    {
        return ((Integer)getStyleParameter(INTENSITY_PROPERTY_KEY).getValue()).intValue();
    }

    /**
     * Gets the value of the style parameter associated with the
     * {@link #SIZE_PROPERTY_KEY} field.
     *
     * @return the value of the style parameter associated with
     *         the{@link #SIZE_PROPERTY_KEY} field.
     */
    public int getSize()
    {
        return ((Integer)getStyleParameter(SIZE_PROPERTY_KEY).getValue()).intValue();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.geom.style.VisualizationStyle#initializeFromDataType()
     */
    @Override
    public void initializeFromDataType()
    {
        /* intentionally blank */
    }
}
