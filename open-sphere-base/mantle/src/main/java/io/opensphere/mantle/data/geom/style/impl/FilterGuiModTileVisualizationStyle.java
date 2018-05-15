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
 * The Class FilterGuiModTileVisualizationStyle.
 */
public class FilterGuiModTileVisualizationStyle extends AbstractTileVisualizationStyle
{
    /** The Constant ourPropertyKeyPrefix. */
    @SuppressWarnings("hiding")
    public static final String ourPropertyKeyPrefix = "FilterGuiModTileVisualizationStyle";

    /** The Constant ourScaleFactoryPropertyKey. */
    public static final String ourDriftFactoryPropertyKey = ourPropertyKeyPrefix + ".DriftFactor";

    /** The Constant ourDefaultColorProperty. */
    public static final VisualizationStyleParameter ourDefaultDriftFactorProperty = new VisualizationStyleParameter(
            ourDriftFactoryPropertyKey, "Drift Factor", Float.valueOf(0.0f), Float.class,
            new VisualizationStyleParameterFlags(false, false, true), ParameterHint.hint(true, false));

    /**
     * Instantiates a new filter gui mod tile visualization style.
     *
     * @param tb the {@link Toolbox}
     */
    public FilterGuiModTileVisualizationStyle(Toolbox tb)
    {
        super(tb);
    }

    @Override
    public FilterGuiModTileVisualizationStyle clone()
    {
        return (FilterGuiModTileVisualizationStyle)super.clone();
    }

    @Override
    public VisualizationStyle deriveForType(String dtiKey)
    {
        FilterGuiModTileVisualizationStyle clone = clone();
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
     * Gets the drift factor.
     *
     * @return the drift factor. 0.0f to 1.0f
     */
    public float getDriftFactor()
    {
        Float val = (Float)getStyleParameterValue(ourDriftFactoryPropertyKey);
        return val == null ? 0.0f : val.floatValue();
    }

    @Override
    public GroupedMiniStyleEditorPanel getMiniUIPanel()
    {
        GroupedMiniStyleEditorPanel panel = super.getMiniUIPanel();

        List<AbstractStyleParameterEditorPanel> paramList = New.list();
        MutableVisualizationStyle style = panel.getChangedStyle();

        VisualizationStyleParameter param = style.getStyleParameter(ourDriftFactoryPropertyKey);
        paramList.add(new FloatSliderStyleParameterEditorPanel(StyleUtils.createSliderMiniPanelBuilder(param.getName()), style,
                ourDriftFactoryPropertyKey, true, false, 0.0f, 1.0f, VisualizationStyleLabelConverters.BASIC_PERCENT));

        StyleParameterEditorGroupPanel paramGrp = new StyleParameterEditorGroupPanel(null, paramList, false, 1);
        panel.addGroupAtTop(paramGrp);

        return panel;
    }

    @Override
    public String getShaderResourceLocation()
    {
        return "/GLSL/GuiMod.glsl";
    }

    @Override
    public MapVisualizationStyleCategory getStyleCategory()
    {
        return MapVisualizationStyleCategory.IMAGE_TILE;
    }

    @Override
    public String getStyleDescription()
    {
        return "";
    }

    @Override
    public String getStyleName()
    {
        return "Filter: GuiMod";
    }

    @Override
    public GroupedStyleParameterEditorPanel getUIPanel()
    {
        GroupedStyleParameterEditorPanel panel = super.getUIPanel();

        List<AbstractStyleParameterEditorPanel> paramList = New.list();
        MutableVisualizationStyle style = panel.getChangedStyle();

        VisualizationStyleParameter param = style.getStyleParameter(ourDriftFactoryPropertyKey);
        paramList.add(new FloatSliderStyleParameterEditorPanel(PanelBuilder.get(param.getName()), style,
                ourDriftFactoryPropertyKey, true, false, 0.0f, 1.0f, VisualizationStyleLabelConverters.BASIC_PERCENT));

        StyleParameterEditorGroupPanel paramGrp = new StyleParameterEditorGroupPanel("GuiMod Tile Style", paramList);
        panel.addGroup(paramGrp);

        return panel;
    }

    @Override
    public void initialize()
    {
        super.initialize();
        setParameter(ourDefaultDriftFactorProperty);
    }

    @Override
    public VisualizationStyle newInstance(Toolbox tb)
    {
        VisualizationStyle vs = new FilterGuiModTileVisualizationStyle(tb);
        vs.initialize();
        return vs;
    }

    /**
     * Sets the drift factor. 0.0f to 1.0f, if outside range will be clamped to
     * range end.
     *
     * @param driftFactor the new drift factor ( range 0.0f to 1.0f )
     * @param source the source of the change.
     */
    public void setScaleFactor(float driftFactor, Object source)
    {
        setParameter(ourDriftFactoryPropertyKey, Float.valueOf(MathUtil.clamp(driftFactor, 0.0f, 1.0f)), source);
    }

    @Override
    public void setShaderParameters(ShaderPropertiesSet sps)
    {
        Collection<Pair<String, float[]>> fUnis = New.collection();
        fUnis.add(new Pair<String, float[]>("uDrift", new float[] { getDriftFactor() }));
        sps.setFloatUniforms(fUnis);
    }
}
