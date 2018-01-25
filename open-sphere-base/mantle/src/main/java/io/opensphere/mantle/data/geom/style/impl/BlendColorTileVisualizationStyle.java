package io.opensphere.mantle.data.geom.style.impl;

import java.awt.Color;
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
import io.opensphere.mantle.data.geom.style.impl.ui.ColorChooserStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.GroupedMiniStyleEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.GroupedStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.PanelBuilder;
import io.opensphere.mantle.data.geom.style.impl.ui.StyleParameterEditorGroupPanel;
import io.opensphere.mantle.data.tile.TileVisualizationSupport;

/**
 * The Class BlendColorTileVisualizationStyle.
 */
public class BlendColorTileVisualizationStyle extends AbstractTileVisualizationStyle
{
    /** The Constant ourPropertyKeyPrefix. */
    public static final String ourPropertyKeyPrefix = "BlendColorTileVisualizationStyle";

    /** The Constant ourColorPropertyKey. */
    public static final String ourBlendColorPropertyKey = ourPropertyKeyPrefix + ".Color";

    /** The Constant ourDefaultColorProperty. */
    public static final VisualizationStyleParameter ourBlendColorProperty = new VisualizationStyleParameter(
            ourBlendColorPropertyKey, "Blend Color", Color.white, Color.class,
            new VisualizationStyleParameterFlags(false, false, true), ParameterHint.hint(true, false));

    /**
     * Instantiates a new blend color tile visualization style.
     *
     * @param tb the {@link Toolbox}
     */
    public BlendColorTileVisualizationStyle(Toolbox tb)
    {
        super(tb);
    }

    @Override
    public BlendColorTileVisualizationStyle clone()
    {
        return (BlendColorTileVisualizationStyle)super.clone();
    }

    @Override
    public VisualizationStyle deriveForType(String dtiKey)
    {
        BlendColorTileVisualizationStyle clone = clone();
        clone.setDTIKey(dtiKey);
        clone.initializeFromDataType();
        return clone;
    }

    /**
     * Gets the blend color.
     *
     * @return the color
     */
    public Color getBlendColor()
    {
        return (Color)getStyleParameterValue(ourBlendColorPropertyKey);
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
        paramList.add(new ColorChooserStyleParameterEditorPanel(StyleUtils.createBasicMiniPanelBuilder("Color"), style,
                ourBlendColorPropertyKey, true, true));

        StyleParameterEditorGroupPanel paramGrp = new StyleParameterEditorGroupPanel(null, paramList, false, 1);
        panel.addGroupAtTop(paramGrp);

        return panel;
    }

    @Override
    public String getShaderResourceLocation()
    {
        return "/GLSL/BlendWithColor.glsl";
    }

    @Override
    public MapVisualizationStyleCategory getStyleCategory()
    {
        return MapVisualizationStyleCategory.IMAGE_TILE;
    }

    @Override
    public String getStyleDescription()
    {
        return "Tint the tile with a given color. The opacity of the selected color will determine the amount of tinting.";
    }

    @Override
    public String getStyleName()
    {
        return "Blend: Color";
    }

    @Override
    public GroupedStyleParameterEditorPanel getUIPanel()
    {
        GroupedStyleParameterEditorPanel panel = super.getUIPanel();

        List<AbstractStyleParameterEditorPanel> paramList = New.list();
        MutableVisualizationStyle style = panel.getChangedStyle();
        paramList.add(new ColorChooserStyleParameterEditorPanel(PanelBuilder.get("Blend Color"), style, ourBlendColorPropertyKey,
                true, true));

        StyleParameterEditorGroupPanel paramGrp = new StyleParameterEditorGroupPanel("Blend Color Tile Style", paramList);
        panel.addGroup(paramGrp);

        return panel;
    }

    @Override
    public void initialize()
    {
        super.initialize();
        setParameter(ourBlendColorProperty);
    }

    @Override
    public VisualizationStyle newInstance(Toolbox tb)
    {
        VisualizationStyle vs = new BlendColorTileVisualizationStyle(tb);
        vs.initialize();
        return vs;
    }

    /**
     * Sets the blend color.
     *
     * @param c the {@link Color}
     * @param source the source
     */
    public void setBlendColor(Color c, Object source)
    {
        setParameter(ourBlendColorPropertyKey, c, source);
    }

    @Override
    public void setShaderParameters(ShaderPropertiesSet sps)
    {
        Collection<Pair<String, float[]>> fUnis = New.collection();
        fUnis.add(new Pair<String, float[]>("uBlendColor", getBlendColor().getComponents(null)));
        sps.setFloatUniforms(fUnis);
    }
}
