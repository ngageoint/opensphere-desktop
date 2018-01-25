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
 * The Class FilterSquareToothFaderTileVisualizationStyle.
 */
public class FilterSquareToothFaderTileVisualizationStyle extends AbstractTileVisualizationStyle
{
    /** The Constant TO_PERCENT_MULTIPLIER. */
    private static final double TO_PERCENT_MULTIPLIER = 100.0;

    /** The Constant ourPropertyKeyPrefix. */
    public static final String ourPropertyKeyPrefix = "FilterSquareToothFaderTileVisualizationStyle";

    /** The Constant ourReduceFactoryPropertyKey. */
    public static final String ourReduceFactoryPropertyKey = ourPropertyKeyPrefix + ".ReduceFactor";

    /** The Constant ourRangeBeginPropertyKey. */
    public static final String ourRangeBeginPropertyKey = ourPropertyKeyPrefix + ".RangeBegin";

    /** The Constant ourRangeNoiseChannelWidthPropertyKey. */
    public static final String ourRangeNoiseChannelWidthPropertyKey = ourPropertyKeyPrefix + ".RangeNoiseChannelWidth";

    /** The Constant ourDefaultReduceFactorProperty. */
    public static final VisualizationStyleParameter ourDefaultReduceFactorProperty = new VisualizationStyleParameter(
            ourReduceFactoryPropertyKey, "Reduce Factor", Float.valueOf(0.0f), Float.class,
            new VisualizationStyleParameterFlags(false, false, true), ParameterHint.hint(true, false));

    /** The Constant ourDefaultRangeBeginProperty. */
    public static final VisualizationStyleParameter ourDefaultRangeBeginProperty = new VisualizationStyleParameter(
            ourRangeBeginPropertyKey, "Range Begin", Float.valueOf(0.0f), Float.class,
            new VisualizationStyleParameterFlags(false, false, true), ParameterHint.hint(true, false));

    /** The Constant ourDefaultRangeNoiseChannelWidthProperty. */
    public static final VisualizationStyleParameter ourDefaultRangeNoiseChannelWidthProperty = new VisualizationStyleParameter(
            ourRangeNoiseChannelWidthPropertyKey, "Noise Channel Width", Float.valueOf(0.0f), Float.class,
            new VisualizationStyleParameterFlags(false, false, true), ParameterHint.hint(true, false));

    /**
     * Instantiates a new filter sharpen tile visualization style.
     *
     * @param tb the {@link Toolbox}
     */
    public FilterSquareToothFaderTileVisualizationStyle(Toolbox tb)
    {
        super(tb);
    }

    @Override
    public FilterSquareToothFaderTileVisualizationStyle clone()
    {
        return (FilterSquareToothFaderTileVisualizationStyle)super.clone();
    }

    @Override
    public VisualizationStyle deriveForType(String dtiKey)
    {
        FilterSquareToothFaderTileVisualizationStyle clone = clone();
        clone.setDTIKey(dtiKey);
        clone.initializeFromDataType();
        return clone;
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

        VisualizationStyleParameter param = style.getStyleParameter(ourRangeBeginPropertyKey);
        paramList.add(new FloatSliderStyleParameterEditorPanel(StyleUtils.createSliderMiniPanelBuilder(param.getName()), style,
                ourRangeBeginPropertyKey, true, false, 0.0f, 1.0f,
                new FloatSliderStyleParameterEditorPanel.BasicIntFloatConvertor(2, null)
                {
                    @Override
                    public String labelValue(double val)
                    {
                        double aVal = val * TO_PERCENT_MULTIPLIER;
                        return String.format(getStringFormat(), aVal) + "%";
                    }
                }));

        param = style.getStyleParameter(ourRangeNoiseChannelWidthPropertyKey);
        paramList.add(new FloatSliderStyleParameterEditorPanel(StyleUtils.createSliderMiniPanelBuilder(param.getName()), style,
                ourRangeNoiseChannelWidthPropertyKey, true, false, 0.0f, 0.5f,
                new FloatSliderStyleParameterEditorPanel.BasicIntFloatConvertor(2, null)
                {
                    @Override
                    public String labelValue(double val)
                    {
                        double aVal = val * TO_PERCENT_MULTIPLIER;
                        return String.format(getStringFormat(), aVal) + "%";
                    }
                }));

        param = style.getStyleParameter(ourReduceFactoryPropertyKey);
        paramList.add(new FloatSliderStyleParameterEditorPanel(StyleUtils.createSliderMiniPanelBuilder(param.getName()), style,
                ourReduceFactoryPropertyKey, true, false, 0.0f, 1.0f,
                new FloatSliderStyleParameterEditorPanel.BasicIntFloatConvertor(2, null)
                {
                    @Override
                    public String labelValue(double val)
                    {
                        double aVal = val * TO_PERCENT_MULTIPLIER;
                        return String.format(getStringFormat(), aVal) + "%";
                    }
                }));

        StyleParameterEditorGroupPanel styleParamGroup = new StyleParameterEditorGroupPanel(null, paramList, false, 1);
        panel.addGroupAtTop(styleParamGroup);

        return panel;
    }

    /**
     * Gets the range begin.
     *
     * @return the range begin. 0.0f to 1.0f
     */
    public float getRangeBegin()
    {
        Float val = (Float)getStyleParameterValue(ourRangeBeginPropertyKey);
        return val == null ? 0.0f : val.floatValue();
    }

    /**
     * Gets the range noise channel width begin.
     *
     * @return the range noise channel width. 0.0f to 0.5f
     */
    public float getRangeNoiseChannelWidth()
    {
        Float val = (Float)getStyleParameterValue(ourRangeNoiseChannelWidthPropertyKey);
        return val == null ? 0.0f : val.floatValue();
    }

    /**
     * Gets the reduce factor.
     *
     * @return the reduce factor. 0.0f to 1.0f
     */
    public float getReduceFactor()
    {
        Float val = (Float)getStyleParameterValue(ourReduceFactoryPropertyKey);
        return val == null ? 0.0f : val.floatValue();
    }

    @Override
    public String getShaderResourceLocation()
    {
        return "/GLSL/SquareToothNoise.glsl";
    }

    @Override
    public MapVisualizationStyleCategory getStyleCategory()
    {
        return MapVisualizationStyleCategory.IMAGE_TILE;
    }

    @Override
    public String getStyleDescription()
    {
        return "Provides a notch filter for pixels based on intensity.";
    }

    @Override
    public String getStyleName()
    {
        return "Filter: Square Tooth Fader";
    }

    @Override
    public GroupedStyleParameterEditorPanel getUIPanel()
    {
        GroupedStyleParameterEditorPanel faderPanel = super.getUIPanel();

        List<AbstractStyleParameterEditorPanel> fParamList = New.list();
        MutableVisualizationStyle style = faderPanel.getChangedStyle();

        VisualizationStyleParameter param = style.getStyleParameter(ourRangeBeginPropertyKey);
        fParamList
                .add(new FloatSliderStyleParameterEditorPanel(PanelBuilder.get(param.getName()), style, ourRangeBeginPropertyKey,
                        true, false, 0.0f, 1.0f, new FloatSliderStyleParameterEditorPanel.BasicIntFloatConvertor(2, null)
                        {
                            @Override
                            public String labelValue(double val)
                            {
                                double aVal = val * TO_PERCENT_MULTIPLIER;
                                return String.format(getStringFormat(), aVal) + "%";
                            }
                        }));

        param = style.getStyleParameter(ourRangeNoiseChannelWidthPropertyKey);
        fParamList.add(new FloatSliderStyleParameterEditorPanel(PanelBuilder.get(param.getName()), style,
                ourRangeNoiseChannelWidthPropertyKey, true, false, 0.0f, 0.5f,
                new FloatSliderStyleParameterEditorPanel.BasicIntFloatConvertor(2, null)
                {
                    @Override
                    public String labelValue(double val)
                    {
                        double aVal = val * TO_PERCENT_MULTIPLIER;
                        return String.format(getStringFormat(), aVal) + "%";
                    }
                }));

        param = style.getStyleParameter(ourReduceFactoryPropertyKey);
        fParamList.add(
                new FloatSliderStyleParameterEditorPanel(PanelBuilder.get(param.getName()), style, ourReduceFactoryPropertyKey,
                        true, false, 0.0f, 1.0f, new FloatSliderStyleParameterEditorPanel.BasicIntFloatConvertor(2, null)
                        {
                            @Override
                            public String labelValue(double val)
                            {
                                double aVal = val * TO_PERCENT_MULTIPLIER;
                                return String.format(getStringFormat(), aVal) + "%";
                            }
                        }));

        StyleParameterEditorGroupPanel paramGrp = new StyleParameterEditorGroupPanel("Square Tooth Fader Tile Style", fParamList);
        faderPanel.addGroup(paramGrp);

        return faderPanel;
    }

    @Override
    public void initialize()
    {
        super.initialize();
        setParameter(ourDefaultReduceFactorProperty);
        setParameter(ourDefaultRangeNoiseChannelWidthProperty);
        setParameter(ourDefaultRangeBeginProperty);
    }

    @Override
    public VisualizationStyle newInstance(Toolbox tb)
    {
        VisualizationStyle vs = new FilterSquareToothFaderTileVisualizationStyle(tb);
        vs.initialize();
        return vs;
    }

    /**
     * Sets the range begin. 0.0f to 1.0f, if outside range will be clamped to
     * range end.
     *
     * @param begin the range begin ( range 0.0f to 1.0f )
     * @param source the source of the change.
     */
    public void setRangeBegin(float begin, Object source)
    {
        setParameter(ourRangeBeginPropertyKey, Float.valueOf(MathUtil.clamp(begin, 0.0f, 1.0f)), source);
    }

    /**
     * Sets the range noise channel width. 0.0f to 0.5f, if outside range will
     * be clamped to range end.
     *
     * @param channelWidth the range noise channel width ( range 0.0f to 0.5f )
     * @param source the source of the change.
     */
    public void setRangeNoiseChannelWidth(float channelWidth, Object source)
    {
        setParameter(ourRangeNoiseChannelWidthPropertyKey, Float.valueOf(MathUtil.clamp(channelWidth, 0.0f, 0.5f)), source);
    }

    /**
     * Sets the reduce factor. 0.0f to 1.0f, if outside range will be clamped to
     * range end.
     *
     * @param factor the new scale factor ( range 0.0f to 1.0f )
     * @param source the source of the change.
     */
    public void setReduceFactor(float factor, Object source)
    {
        setParameter(ourReduceFactoryPropertyKey, Float.valueOf(MathUtil.clamp(factor, 0.0f, 1.0f)), source);
    }

    @Override
    public void setShaderParameters(ShaderPropertiesSet sps)
    {
        Collection<Pair<String, float[]>> fUnis = New.collection();
        fUnis.add(new Pair<String, float[]>("uBeginVal", new float[] { getRangeBegin() }));
        fUnis.add(new Pair<String, float[]>("uEndVal", new float[] { getRangeBegin() + getRangeNoiseChannelWidth() }));
        fUnis.add(new Pair<String, float[]>("uReplaceVal", new float[] { getReduceFactor() }));
        sps.setFloatUniforms(fUnis);
    }
}
