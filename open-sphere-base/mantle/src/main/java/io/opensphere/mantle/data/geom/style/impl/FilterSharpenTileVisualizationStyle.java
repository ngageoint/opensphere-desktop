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
import io.opensphere.mantle.data.geom.style.impl.ui.GroupedStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.PanelBuilder;
import io.opensphere.mantle.data.geom.style.impl.ui.StyleParameterEditorGroupPanel;
import io.opensphere.mantle.data.tile.TileVisualizationSupport;

/**
 * The Class FilterSharpenTileVisualizationStyle.
 */
public class FilterSharpenTileVisualizationStyle extends AbstractTileVisualizationStyle
{
    /** The Constant TO_PERCENT_MULTIPLIER. */
    private static final double TO_PERCENT_MULTIPLIER = 100.0;

    /** The Constant ourPropertyKeyPrefix. */
    public static final String ourPropertyKeyPrefix = "FilterSharpenTileVisualizationStyle";

    /** The Constant ourScaleFactoryPropertyKey. */
    public static final String ourScaleFactoryPropertyKey = ourPropertyKeyPrefix + ".ScaleFactor";

    /** The Constant ourDefaultColorProperty. */
    public static final VisualizationStyleParameter ourDefaultScaleFactorProperty = new VisualizationStyleParameter(
            ourScaleFactoryPropertyKey, "Scale Factor", Float.valueOf(0.0f), Float.class,
            new VisualizationStyleParameterFlags(false, false, true), ParameterHint.hint(true, false));

    /**
     * Instantiates a new filter sharpen tile visualization style.
     *
     * @param tb the {@link Toolbox}
     */
    public FilterSharpenTileVisualizationStyle(Toolbox tb)
    {
        super(tb);
    }

    @Override
    public FilterSharpenTileVisualizationStyle clone()
    {
        return (FilterSharpenTileVisualizationStyle)super.clone();
    }

    @Override
    public VisualizationStyle deriveForType(String dtiKey)
    {
        FilterSharpenTileVisualizationStyle clone = clone();
        clone.setDTIKey(dtiKey);
        clone.initializeFromDataType();
        return clone;
    }

    @Override
    public Class<? extends TileVisualizationSupport> getConvertedClassType()
    {
        return TileVisualizationSupport.class;
    }

    /**
     * Gets the scale factor.
     *
     * @return the scale factor. 0.0f to 1.0f
     */
    public float getScaleFactor()
    {
        Float val = (Float)getStyleParameterValue(ourScaleFactoryPropertyKey);
        return val == null ? 0.0f : val.floatValue();
    }

    @Override
    public String getShaderResourceLocation()
    {
        return "/GLSL/Sharpen.glsl";
    }

    @Override
    public MapVisualizationStyleCategory getStyleCategory()
    {
        return MapVisualizationStyleCategory.IMAGE_TILE;
    }

    @Override
    public String getStyleDescription()
    {
        return "Sharpens the pixels in the tile.";
    }

    @Override
    public String getStyleName()
    {
        return "Filter: Sharpen";
    }

    @Override
    public GroupedStyleParameterEditorPanel getUIPanel()
    {
        GroupedStyleParameterEditorPanel panel = super.getUIPanel();

        List<AbstractStyleParameterEditorPanel> paramList = New.list();
        MutableVisualizationStyle style = panel.getChangedStyle();

        VisualizationStyleParameter param = style.getStyleParameter(ourScaleFactoryPropertyKey);
        paramList.add(
                new FloatSliderStyleParameterEditorPanel(PanelBuilder.get(param.getName()), style, ourScaleFactoryPropertyKey,
                        true, false, 0.0f, 1.0f, new FloatSliderStyleParameterEditorPanel.BasicIntFloatConvertor(2, null)
                        {
                            @Override
                            public String labelValue(double val)
                            {
                                double aVal = val * TO_PERCENT_MULTIPLIER;
                                return String.format(getStringFormat(), aVal) + "%";
                            }
                        }));

        StyleParameterEditorGroupPanel paramGrp = new StyleParameterEditorGroupPanel("Sharpen Tile Style", paramList);
        panel.addGroup(paramGrp);

        return panel;
    }

    @Override
    public void initialize()
    {
        super.initialize();
        setParameter(ourDefaultScaleFactorProperty);
    }

    @Override
    public VisualizationStyle newInstance(Toolbox tb)
    {
        VisualizationStyle vs = new FilterSharpenTileVisualizationStyle(tb);
        vs.initialize();
        return vs;
    }

    /**
     * Sets the scale factor. 0.0f to 1.0f, if outside range will be clamped to
     * range end.
     *
     * @param scaleFactor the new scale factor ( range 0.0f to 1.0f )
     * @param source the source of the change.
     */
    public void setScaleFactor(float scaleFactor, Object source)
    {
        setParameter(ourScaleFactoryPropertyKey, Float.valueOf(MathUtil.clamp(scaleFactor, 0.0f, 1.0f)), source);
    }

    @Override
    public void setShaderParameters(ShaderPropertiesSet sps)
    {
        Collection<Pair<String, float[]>> fUnis = New.collection();
        fUnis.add(new Pair<String, float[]>("uScaleFactor", new float[] { getScaleFactor() }));
        sps.setFloatUniforms(fUnis);
    }
}
