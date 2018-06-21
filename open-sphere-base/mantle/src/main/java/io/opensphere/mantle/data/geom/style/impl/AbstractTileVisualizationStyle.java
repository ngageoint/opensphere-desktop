package io.opensphere.mantle.data.geom.style.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.renderproperties.BlendingConfigGL;
import io.opensphere.core.geometry.renderproperties.FragmentShaderProperties;
import io.opensphere.core.geometry.renderproperties.FragmentShaderProperties.ShaderPropertiesSet;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.StreamReader;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.mantle.data.geom.style.TileVisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameter;
import io.opensphere.mantle.data.geom.style.impl.ui.GroupedMiniStyleEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.GroupedStyleParameterEditorPanel;

/**
 * The Class AbstractTileVisualizationStyle.
 */
public abstract class AbstractTileVisualizationStyle extends AbstractVisualizationStyle implements TileVisualizationStyle
{
    /** The Constant ourPropertyKeyPrefix. */
    public static final String ourPropertyKeyPrefix = "AbstractTileVisualizationStyle";

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(AbstractTileVisualizationStyle.class);

    /** The our shader source object pool. */
    private static Map<String, String> ourShaderSourceMap = New.map();

    /**
     * Gets the shader source pool object.
     *
     * @param style the style
     * @return the shader source pool object
     */
    protected static String getShaderSourcePoolInstance(TileVisualizationStyle style)
    {
        String result = null;
        synchronized (ourShaderSourceMap)
        {
            result = ourShaderSourceMap.get(style.getShaderResourceLocation());
            if (result == null)
            {
                result = readShaderResource(style.getShaderResourceLocation());
                if (!StringUtils.isBlank(result))
                {
                    ourShaderSourceMap.put(style.getShaderResourceLocation(), result);
                }
            }
        }
        return result;
    }

    /**
     * Read shader resource.
     *
     * @param shaderResourceLocation the shader resource location
     * @return the string
     */
    private static String readShaderResource(String shaderResourceLocation)
    {
        String shaderSource = null;
        if (!StringUtils.isBlank(shaderResourceLocation))
        {
            InputStream iStream = AbstractTileVisualizationStyle.class.getResourceAsStream(shaderResourceLocation);
            if (iStream != null)
            {
                try
                {
                    shaderSource = new StreamReader(iStream, 512, -1).readStreamIntoString(StringUtilities.DEFAULT_CHARSET);
                    if (StringUtils.isBlank(shaderSource))
                    {
                        shaderSource = null;
                    }
                }
                catch (IOException e)
                {
                    LOGGER.error("Failed to load shader resource: " + shaderResourceLocation);
                }
                finally
                {
                    Utilities.close(iStream);
                }
            }
        }
        return shaderSource;
    }

//    /** The Constant ourColorPropertyKey. */
//    public static final String ourTileColorPropertyKey = ourPropertyKeyPrefix + ".Color";
//
//    /** The Constant ourDefaultColorProperty. */
//    public static final VisualizationStyleParameter ourDefaultTileColorProperty = new VisualizationStyleParameter(
//            ourTileColorPropertyKey, "Tile Color", Color.white, Color.class, new VisualizationStyleParameterFlags(false, false,
//                    false), ParameterHint.hint(true, false));

//    /** The type change listener. */
//    @SuppressWarnings({ "PMD.SingularField" })
//    private EventListener<AbstractDataTypeInfoChangeEvent> myTypeChangeListener;

    /**
     * Instantiates a new abstract tile visualization style.
     *
     * @param tb the {@link Toolbox}
     */
    public AbstractTileVisualizationStyle(Toolbox tb)
    {
        super(tb);
    }

    /**
     * Instantiates a new abstract tile visualization style.
     *
     * @param toolbox the tb
     * @param dtiTypeKey the dti key
     */
    public AbstractTileVisualizationStyle(Toolbox toolbox, String dtiTypeKey)
    {
        super(toolbox, dtiTypeKey);
    }

    @Override
    public AbstractTileVisualizationStyle clone()
    {
        final AbstractTileVisualizationStyle aClone = (AbstractTileVisualizationStyle)super.clone();
//        if (getDTIKey() != null)
//        {
//            aClone.myTypeChangeListener = createDataTypeInfoChangeListener();
//            getToolbox().getEventManager().subscribe(AbstractDataTypeInfoChangeEvent.class, aClone.myTypeChangeListener);
//        }
        return aClone;
    }

//    /**
//     * Creates the data type change listener.
//     *
//     * @return the event listener
//     */
//    private EventListener<AbstractDataTypeInfoChangeEvent> createDataTypeInfoChangeListener()
//    {
//        return new EventListener<AbstractDataTypeInfoChangeEvent>()
//        {
//            @Override
//            public void notify(AbstractDataTypeInfoChangeEvent event)
//            {
//                if (getDTIKey() != null && EqualsHelper.equals(getDTIKey(), event.getDataTypeKey()))
//                {
//                    handleDataTypeInfoChangeEvent(event);
//                }
//            }
//        };
//    }

//    /**
//     * Handle a data type info change event. If called will be for this data
//     * type, having already been checked in the change listener that it matches
//     * this type.
//     *
//     * @param event the AbstractDataTypeInfoChangeEvent to handle.
//     */
//    protected void handleDataTypeInfoChangeEvent(AbstractDataTypeInfoChangeEvent event)
//    {
//        if (event instanceof DataTypeInfoColorChangeEvent)
//        {
//            DataTypeInfoColorChangeEvent ccEvt = (DataTypeInfoColorChangeEvent)event;
//            if (ccEvt.isOpacityChangeOnly())
//            {
//                setParameter(ourTileColorPropertyKey, ccEvt.getColor(), event.getSource());
//            }
//        }
//    }

//    /**
//     * Gets the color.
//     *
//     * @return the color
//     */
//    public Color getColor()
//    {
//        return (Color)getStyleParameterValue(ourTileColorPropertyKey);
//    }
//
//    /**
//     * Sets the color.
//     *
//     * @param c the {@link Color}
//     * @param source the source
//     */
//    public void setColor(Color c, Object source)
//    {
//        setParameter(ourTileColorPropertyKey, c, source);
//    }

//    /**
//     * Gets the opacity.
//     *
//     * @return the opacity
//     */
//    public int getOpacity()
//    {
//        Color baseColor = getColor();
//        return baseColor == null ? 0 : baseColor.getAlpha();
//    }
//
//    /**
//     * Sets the opacity.
//     *
//     * @param alpha the alpha ( 0 to 255 )
//     * @param source the source making the change
//     */
//    public void setOpacity(int alpha, Object source)
//    {
//        Color baseColor = getColor();
//        if (baseColor != null && alpha != baseColor.getAlpha())
//        {
//            int r = baseColor.getRed();
//            int g = baseColor.getGreen();
//            int b = baseColor.getBlue();
//            setColor(new Color(r, g, b, alpha), source);
//        }
//    }

    @Override
    @NonNull
    public GroupedMiniStyleEditorPanel getMiniUIPanel()
    {
        GroupedMiniStyleEditorPanel panel = new GroupedMiniStyleEditorPanel(this);
        return panel;
    }

    @Override
    @NonNull
    public GroupedStyleParameterEditorPanel getUIPanel()
    {
        GroupedStyleParameterEditorPanel aPanel = new GroupedStyleParameterEditorPanel(this, true);
//        List<AbstractStyleParameterEditorPanel> paramList = New.list();
//        MutableVisualizationStyle style = aPanel.getChangedStyle();
//        paramList.add(new ColorChooserStyleParameterEditorPanel("Tile Color", style, ourTileColorPropertyKey, true));
//
//        StyleParameterEditorGroupPanel paramGrp = new StyleParameterEditorGroupPanel("Basic Tile Style", paramList);
//        aPanel.addGroup(paramGrp);
        return aPanel;
    }

    @Override
    public void initialize()
    {
//        setParameter(ourDefaultTileColorProperty);
    }

    @Override
    public void initialize(Set<VisualizationStyleParameter> paramSet)
    {
        for (VisualizationStyleParameter param : paramSet)
        {
            if (param.getKey() != null && param.getKey().startsWith(ourPropertyKeyPrefix))
            {
                setParameter(param);
            }
        }
    }

    @Override
    public void initializeFromDataType()
    {
//        if (getDTIKey() != null)
//        {
//            DataTypeInfo type = AbstractVisualizationStyle.getDataTypeInfoFromKey(getToolbox(), getDTIKey());
//            if (type != null)
//            {
//                if (type.getBasicVisualizationInfo() != null)
//                {
//                    setParameter(ourTileColorPropertyKey, type.getBasicVisualizationInfo().getTypeColor(), null);
//                }
//                myTypeChangeListener = createDataTypeInfoChangeListener();
//                getToolbox().getEventManager().subscribe(AbstractDataTypeInfoChangeEvent.class, myTypeChangeListener);
//            }
//        }
    }

    @Override
    public boolean requiresShaders()
    {
        return !StringUtils.isBlank(getShaderResourceLocation());
    }

    /**
     * Sets the blending parameters.
     *
     * @param trp the new blending parameters
     */
    public void setBlendingParameters(TileRenderProperties trp)
    {
        // Don't do anything by default.
    }

    /**
     * Sets the shader parameters.
     *
     * @param sps the new shader parameters
     */
    public abstract void setShaderParameters(ShaderPropertiesSet sps);

    @Override
    public void updateTileRenderProperties(TileRenderProperties trp)
    {
        if (trp != null)
        {
            trp.resetShaderPropertiesToDefault();
            trp.setBlending(BlendingConfigGL.getDefaultBlending());

            if (!StringUtils.isBlank(getShaderResourceLocation()))
            {
                FragmentShaderProperties props = trp.getShaderProperties();
                if (props != null)
                {
                    ShaderPropertiesSet sps = new ShaderPropertiesSet();
                    sps.setShaderCode(getShaderSourcePoolInstance(this));
                    setShaderParameters(sps);
                    props.setupShader(sps);
                }
            }

            trp.setBlending(BlendingConfigGL.getDefaultBlending());
            setBlendingParameters(trp);
        }
    }
}
