package io.opensphere.mantle.data.geom.style.impl;

import java.util.Collection;
import java.util.List;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.renderproperties.FragmentShaderProperties.ShaderPropertiesSet;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.mantle.data.MapVisualizationStyleCategory;
import io.opensphere.mantle.data.geom.style.MutableVisualizationStyle;
import io.opensphere.mantle.data.geom.style.ParameterHint;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameter;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameterFlags;
import io.opensphere.mantle.data.geom.style.impl.ui.AbstractStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.FloatSliderStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.GroupedMiniStyleEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.GroupedStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.PanelBuilder;
import io.opensphere.mantle.data.geom.style.impl.ui.StyleParameterEditorGroupPanel;
import io.opensphere.mantle.data.tile.TileVisualizationSupport;

/**
 * The Class FilterDarkenBrightenTileVisualizationStyle.
 */
public class FilterDarkenBrightenTileVisualizationStyle extends AbstractTileVisualizationStyle
{
    /** The Constant MIN_BRIGHTNESS. */
    private static final float MIN_BRIGHTNESS = 0.0f;

    /** The Constant MAX_BRIGHTNESS. */
    private static final float MAX_BRIGHTNESS = 8.0f;

    /** The Constant ourPropertyKeyPrefix. */
    public static final String ourPropertyKeyPrefix = "FilterDarkenBrightenTileVisualizationStyle";

    /** The Constant ourScaleFactoryPropertyKey. */
    public static final String ourBrightnessFactoryPropertyKey = ourPropertyKeyPrefix + ".BrightnessFactor";

    /** The Constant ourDefaultColorProperty. */
    public static final VisualizationStyleParameter ourDefaultBrightnessFactorProperty = new VisualizationStyleParameter(
            ourBrightnessFactoryPropertyKey, "Brightness", Float.valueOf(1.0f), Float.class,
            new VisualizationStyleParameterFlags(false, false, true), ParameterHint.hint(true, false));

    /**
     * Instantiates a new filter darken/brighten tile visualization style.
     *
     * @param tb the {@link Toolbox}
     */
    public FilterDarkenBrightenTileVisualizationStyle(Toolbox tb)
    {
        super(tb);
    }

    @Override
    public FilterDarkenBrightenTileVisualizationStyle clone()
    {
        return (FilterDarkenBrightenTileVisualizationStyle)super.clone();
    }

    @Override
    public VisualizationStyle deriveForType(String dtiKey)
    {
        FilterDarkenBrightenTileVisualizationStyle clone = clone();
        clone.setDTIKey(dtiKey);
        clone.initializeFromDataType();
        return clone;
    }

    /**
     * Gets the brightness factor.
     *
     * @return the scale factor. 0.0f to 8.0f
     */
    public float getBrightnessFactor()
    {
        Float val = (Float)getStyleParameterValue(ourBrightnessFactoryPropertyKey);
        return val == null ? MIN_BRIGHTNESS : val.floatValue();
    }

    @Override
    public Class<? extends TileVisualizationSupport> getConvertedClassType()
    {
        return TileVisualizationSupport.class;
    }

    @Override
    public GroupedMiniStyleEditorPanel getMiniUIPanel()
    {
        GroupedMiniStyleEditorPanel panel = super.getMiniUIPanel();

        List<AbstractStyleParameterEditorPanel> paramList = New.list();
        MutableVisualizationStyle style = panel.getChangedStyle();

        createBrightnessStyleParameterEditorPanel(StyleUtils.createSliderMiniPanelBuilder("Brightness"), paramList, style);

        StyleParameterEditorGroupPanel paramGrp = new StyleParameterEditorGroupPanel(null, paramList, false, 1);
        panel.addGroup(paramGrp);

        return panel;
    }

    @Override
    public String getShaderResourceLocation()
    {
        return "/GLSL/DarkenBrighten.glsl";
    }

    @Override
    public MapVisualizationStyleCategory getStyleCategory()
    {
        return MapVisualizationStyleCategory.IMAGE_TILE;
    }

    @Override
    public String getStyleDescription()
    {
        return "Increases or decreases the brightness for data tile pixels.";
    }

    @Override
    public String getStyleName()
    {
        return "Filter: Darken/Brighten";
    }

    @Override
    public GroupedStyleParameterEditorPanel getUIPanel()
    {
        GroupedStyleParameterEditorPanel panel = super.getUIPanel();

        List<AbstractStyleParameterEditorPanel> paramList = New.list();
        MutableVisualizationStyle style = panel.getChangedStyle();
        createBrightnessStyleParameterEditorPanel(null, paramList, style);

        StyleParameterEditorGroupPanel paramGrp = new StyleParameterEditorGroupPanel("Darken/Brighten Tile Style", paramList);
        panel.addGroup(paramGrp);

        return panel;
    }

    @Override
    public void initialize()
    {
        super.initialize();
        setParameter(ourDefaultBrightnessFactorProperty);
    }

    @Override
    public VisualizationStyle newInstance(Toolbox tb)
    {
        VisualizationStyle vs = new FilterDarkenBrightenTileVisualizationStyle(tb);
        vs.initialize();
        return vs;
    }

    /**
     * Sets the brightness factor. 0.0f to 8.0f, if outside range will be
     * clamped to range end.
     *
     * @param brightnessFactor the new scale factor ( range 0.0f to 8.0f )
     * @param source the source of the change.
     */
    public void setBrightnessFactor(float brightnessFactor, Object source)
    {
        setParameter(ourBrightnessFactoryPropertyKey,
                Float.valueOf(MathUtil.clamp(brightnessFactor, MIN_BRIGHTNESS, MAX_BRIGHTNESS)), source);
    }

    @Override
    public void setShaderParameters(ShaderPropertiesSet sps)
    {
        Collection<Pair<String, float[]>> fUnis = New.collection();
        fUnis.add(new Pair<String, float[]>("uScaleFactor", new float[] { getBrightnessFactor() }));
        sps.setFloatUniforms(fUnis);
    }

    /**
     * Creates the brightness style parameter editor panel.
     *
     * @param pb the pb
     * @param paramList the param list
     * @param style the style
     */
    private void createBrightnessStyleParameterEditorPanel(PanelBuilder pb, List<AbstractStyleParameterEditorPanel> paramList,
            MutableVisualizationStyle style)
    {
        VisualizationStyleParameter param = style.getStyleParameter(ourBrightnessFactoryPropertyKey);
        paramList.add(new FloatSliderStyleParameterEditorPanel(pb == null ? PanelBuilder.get(param.getName()) : pb, style,
                ourBrightnessFactoryPropertyKey, true, false, MIN_BRIGHTNESS, MAX_BRIGHTNESS,
                new FloatSliderStyleParameterEditorPanel.BasicIntFloatConvertor(2, null)
                {
                    @Override
                    public String labelValue(double val)
                    {
                        double aVal = val * 10;
                        return String.format(getStringFormat(), aVal);
                    }
                }));
    }
}
