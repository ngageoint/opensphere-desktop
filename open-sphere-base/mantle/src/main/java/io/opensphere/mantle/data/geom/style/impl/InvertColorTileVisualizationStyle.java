package io.opensphere.mantle.data.geom.style.impl;

import java.util.Collection;
import java.util.List;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.renderproperties.FragmentShaderProperties.ShaderPropertiesSet;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.mantle.data.MapVisualizationStyleCategory;
import io.opensphere.mantle.data.geom.style.MutableVisualizationStyle;
import io.opensphere.mantle.data.geom.style.ParameterHint;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameter;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameterFlags;
import io.opensphere.mantle.data.geom.style.impl.ui.AbstractStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.CheckBoxStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.GroupedMiniStyleEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.GroupedStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.StyleParameterEditorGroupPanel;
import io.opensphere.mantle.data.tile.TileVisualizationSupport;

/**
 * A simple style used to invert the color (essentially taking the compliment
 * within the color scale).
 */
public class InvertColorTileVisualizationStyle extends AbstractTileVisualizationStyle
{
    /** The Constant ourPropertyKeyPrefix. */
    public static final String PROPERTY_KEY_PREFIX = "InvertColorTileVisualizationStyle";

    /**
     * The Constant in which the name of the key used to configure the red
     * component is defined..
     */
    public static final String RED_PROPERTY_KEY = PROPERTY_KEY_PREFIX + ".Red";

    /**
     * The Constant in which the name of the key used to configure the green
     * component is defined.
     */
    public static final String GREEN_PROPERTY_KEY = PROPERTY_KEY_PREFIX + ".Green";

    /**
     * The Constant in which the name of the key used to configure the blue
     * component is defined.
     */
    public static final String BLUE_PROPERTY_KEY = PROPERTY_KEY_PREFIX + ".Blue";

    /** The Constant in which the red enabled property is defined. */
    public static final VisualizationStyleParameter RED_ENABLED_PROPERTY = new VisualizationStyleParameter(RED_PROPERTY_KEY,
            "Red", Boolean.TRUE, Boolean.class, new VisualizationStyleParameterFlags(false, false, true),
            ParameterHint.hint(true, false));

    /** The Constant in which the green enabled property is defined. */
    public static final VisualizationStyleParameter GREEN_ENABLED_PROPERTY = new VisualizationStyleParameter(GREEN_PROPERTY_KEY,
            "Green", Boolean.TRUE, Boolean.class, new VisualizationStyleParameterFlags(false, false, true),
            ParameterHint.hint(true, false));

    /** The Constant in which the blue enabled property is defined. */
    public static final VisualizationStyleParameter BLUE_ENABLED_PROPERTY = new VisualizationStyleParameter(BLUE_PROPERTY_KEY,
            "Blue", Boolean.TRUE, Boolean.class, new VisualizationStyleParameterFlags(false, false, true),
            ParameterHint.hint(true, false));

    /**
     * Instantiates a new tile visualization style.
     *
     * @param tb the {@link Toolbox}
     */
    public InvertColorTileVisualizationStyle(Toolbox tb)
    {
        super(tb);
    }

    @Override
    public InvertColorTileVisualizationStyle clone()
    {
        return (InvertColorTileVisualizationStyle)super.clone();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.geom.style.impl.AbstractTileVisualizationStyle#initialize()
     */
    @Override
    public void initialize()
    {
        super.initialize();
        setParameter(RED_ENABLED_PROPERTY);
        setParameter(GREEN_ENABLED_PROPERTY);
        setParameter(BLUE_ENABLED_PROPERTY);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.geom.style.impl.AbstractTileVisualizationStyle#getMiniUIPanel()
     */
    @Override
    public GroupedMiniStyleEditorPanel getMiniUIPanel()
    {
        GroupedMiniStyleEditorPanel panel = super.getMiniUIPanel();

        List<AbstractStyleParameterEditorPanel> paramList = New.list();
        MutableVisualizationStyle style = panel.getChangedStyle();

        VisualizationStyleParameter param = style.getStyleParameter(RED_PROPERTY_KEY);
        paramList.add(new CheckBoxStyleParameterEditorPanel(StyleUtils.createSliderMiniPanelBuilder(param.getName()), style,
                RED_PROPERTY_KEY, true));

        param = style.getStyleParameter(GREEN_PROPERTY_KEY);
        paramList.add(new CheckBoxStyleParameterEditorPanel(StyleUtils.createSliderMiniPanelBuilder(param.getName()), style,
                GREEN_PROPERTY_KEY, true));

        param = style.getStyleParameter(BLUE_PROPERTY_KEY);
        paramList.add(new CheckBoxStyleParameterEditorPanel(StyleUtils.createSliderMiniPanelBuilder(param.getName()), style,
                BLUE_PROPERTY_KEY, true));

        StyleParameterEditorGroupPanel styleParamGroup = new StyleParameterEditorGroupPanel(null, paramList, false, 1);
        panel.addGroupAtTop(styleParamGroup);

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
        GroupedStyleParameterEditorPanel faderPanel = super.getUIPanel();

        List<AbstractStyleParameterEditorPanel> fParamList = New.list();
        MutableVisualizationStyle style = faderPanel.getChangedStyle();

        VisualizationStyleParameter param = style.getStyleParameter(RED_PROPERTY_KEY);
        fParamList.add(new CheckBoxStyleParameterEditorPanel(StyleUtils.createSliderMiniPanelBuilder(param.getName()), style,
                RED_PROPERTY_KEY, true));

        param = style.getStyleParameter(GREEN_PROPERTY_KEY);
        fParamList.add(new CheckBoxStyleParameterEditorPanel(StyleUtils.createSliderMiniPanelBuilder(param.getName()), style,
                GREEN_PROPERTY_KEY, true));

        param = style.getStyleParameter(BLUE_PROPERTY_KEY);
        fParamList.add(new CheckBoxStyleParameterEditorPanel(StyleUtils.createSliderMiniPanelBuilder(param.getName()), style,
                BLUE_PROPERTY_KEY, true));

        StyleParameterEditorGroupPanel paramGrp = new StyleParameterEditorGroupPanel("Invert Colors Tile Style", fParamList);
        faderPanel.addGroup(paramGrp);

        return faderPanel;
    }

    @Override
    public VisualizationStyle deriveForType(String dtiKey)
    {
        InvertColorTileVisualizationStyle clone = clone();
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
    public String getShaderResourceLocation()
    {
        return "/GLSL/InvertColor.glsl";
    }

    @Override
    public MapVisualizationStyleCategory getStyleCategory()
    {
        return MapVisualizationStyleCategory.IMAGE_TILE;
    }

    @Override
    public String getStyleDescription()
    {
        return "The data pixels are displayed with inverted color values.";
    }

    @Override
    public String getStyleName()
    {
        return "Color: Invert";
    }

    @Override
    public VisualizationStyle newInstance(Toolbox tb)
    {
        VisualizationStyle vs = new InvertColorTileVisualizationStyle(tb);
        vs.initialize();
        return vs;
    }

    /**
     * Gets the enabled state of the inversion of the red channel.
     *
     * @return true to enable inversion of the red channel, false to disable.
     */
    public boolean getRedSeed()
    {
        Boolean val = (Boolean)getStyleParameterValue(RED_PROPERTY_KEY);
        return val == null ? false : val.booleanValue();
    }

    /**
     * Gets the enabled state of the inversion of the green channel.
     *
     * @return true to enable inversion of the green channel, false to disable.
     */
    public boolean getGreenSeed()
    {
        Boolean val = (Boolean)getStyleParameterValue(GREEN_PROPERTY_KEY);
        return val == null ? false : val.booleanValue();
    }

    /**
     * Gets the enabled state of the inversion of the blue channel.
     *
     * @return true to enable inversion of the blue channel, false to disable.
     */
    public boolean getBlueSeed()
    {
        Boolean val = (Boolean)getStyleParameterValue(BLUE_PROPERTY_KEY);
        return val == null ? false : val.booleanValue();
    }

    /**
     * Sets the enabled state of the inversion of the red channel.
     *
     * @param redSeed true to enable inversion of the red channel, false to
     *            disable.
     * @param source the source of the change.
     */
    public void setRedSeed(boolean redSeed, Object source)
    {
        setParameter(RED_PROPERTY_KEY, redSeed, source);
    }

    /**
     * Sets the enabled state of the inversion of the green channel.
     *
     * @param greenSeed true to enable inversion of the green channel, false to
     *            disable.
     * @param source the source of the change.
     */
    public void setGreenSeed(boolean greenSeed, Object source)
    {
        setParameter(GREEN_PROPERTY_KEY, greenSeed, source);
    }

    /**
     * Sets the enabled state of the inversion of the blue channel.
     *
     * @param blueSeed true to enable inversion of the blue channel, false to
     *            disable.
     * @param source the source of the change.
     */
    public void setBlueSeed(boolean blueSeed, Object source)
    {
        setParameter(BLUE_PROPERTY_KEY, blueSeed, source);
    }

    @Override
    public void setShaderParameters(ShaderPropertiesSet sps)
    {
        Collection<Pair<String, boolean[]>> fUnis = New.collection();
        fUnis.add(new Pair<>("uInvertRed", new boolean[] { getRedSeed() }));
        fUnis.add(new Pair<>("uInvertGreen", new boolean[] { getGreenSeed() }));
        fUnis.add(new Pair<>("uInvertBlue", new boolean[] { getBlueSeed() }));
        sps.setBooleanUniforms(fUnis);
    }
}
