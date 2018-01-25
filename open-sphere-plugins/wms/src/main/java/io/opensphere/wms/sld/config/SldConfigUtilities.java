package io.opensphere.wms.sld.config;

import javax.xml.bind.JAXBElement;

import org.apache.commons.lang3.StringUtils;

import net.opengis.sld._100.FeatureTypeStyle;
import net.opengis.sld._100.NamedLayer;
import net.opengis.sld._100.NamedStyle;
import net.opengis.sld._100.Rule;
import net.opengis.sld._100.StyledLayerDescriptor;
import net.opengis.sld._100.SymbolizerType;
import net.opengis.sld._100.UserLayer;
import net.opengis.sld._100.UserStyle;

/**
 * Utility methods for dealing with SLD Configurations.
 */
public final class SldConfigUtilities
{
    /**
     * Gets the name from a {@link StyledLayerDescriptor}.
     *
     * @param sld the {@link StyledLayerDescriptor}
     * @return the name from the SLD
     */
    public static String getNameFromSld(StyledLayerDescriptor sld)
    {
        for (Object obj : sld.getNamedLayerOrUserLayer())
        {
            if (obj instanceof UserLayer)
            {
                for (UserStyle style : ((UserLayer)obj).getUserStyle())
                {
                    if (StringUtils.isNotEmpty(style.getName()))
                    {
                        return style.getName();
                    }
                }
            }
            else if (obj instanceof NamedLayer)
            {
                for (Object namedObj : ((NamedLayer)obj).getNamedStyleOrUserStyle())
                {
                    if (namedObj instanceof NamedStyle)
                    {
                        return ((NamedStyle)namedObj).getName();
                    }
                    else if (namedObj instanceof UserStyle)
                    {
                        return ((UserStyle)namedObj).getName();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Prints a {@link StyledLayerDescriptor} for debug logging.
     *
     * @param sld the {@link StyledLayerDescriptor}
     * @return the printed version of the SLD
     */
    public static String printSld(StyledLayerDescriptor sld)
    {
        StringBuilder sb = new StringBuilder("StyledLayerDescriptor[" + " Version[").append(sld.getVersion()).append(']');
        if (StringUtils.isNotEmpty(sld.getName()))
        {
            sb.append(" Name[").append(sld.getName()).append(']');
        }
        if (StringUtils.isNotEmpty(sld.getTitle()))
        {
            sb.append(" Title[").append(sld.getTitle()).append(']');
        }
        if (StringUtils.isNotEmpty(sld.getAbstract()))
        {
            sb.append(" Abstract[").append(sld.getAbstract()).append(']');
        }
        for (Object layer : sld.getNamedLayerOrUserLayer())
        {
            if (layer instanceof UserLayer)
            {
                sb.append(printSldUserLayer((UserLayer)layer));
            }
            else if (layer instanceof NamedLayer)
            {
                sb.append(printSldNamedLayer((NamedLayer)layer));
            }
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Prints a {@link NamedLayer} for debug logging.
     *
     * @param layer the {@link NamedLayer}
     * @return the printed version of the NamedLayer
     */
    private static String printSldNamedLayer(NamedLayer layer)
    {
        StringBuilder sb = new StringBuilder(" NamedLayer[");
        if (StringUtils.isNotEmpty(layer.getName()))
        {
            sb.append(" Name[").append(layer.getName()).append(']');
        }
        for (Object style : layer.getNamedStyleOrUserStyle())
        {
            if (style instanceof UserStyle)
            {
                sb.append(printSldUserStyle((UserStyle)style));
            }
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Prints a {@link Rule} for debug logging.
     *
     * @param rule the {@link Rule}
     * @return the printed version of the Rule
     */
    private static String printSldRule(Rule rule)
    {
        StringBuilder sb = new StringBuilder(" Rule[");
        for (JAXBElement<? extends SymbolizerType> jaxbSymbol : rule.getSymbolizer())
        {
            sb.append(" Symbolizer[").append(jaxbSymbol.getValue().toString()).append(']');
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Prints a {@link UserLayer} for debug logging.
     *
     * @param layer the {@link UserLayer}
     * @return the printed version of the UserLayer
     */
    private static String printSldUserLayer(UserLayer layer)
    {
        StringBuilder sb = new StringBuilder(" UserLayer[");
        if (StringUtils.isNotEmpty(layer.getName()))
        {
            sb.append(" Name[").append(layer.getName()).append(']');
        }
        for (UserStyle style : layer.getUserStyle())
        {
            sb.append(printSldUserStyle(style));
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Prints a {@link UserStyle} for debug logging.
     *
     * @param style the {@link UserStyle}
     * @return the printed version of the UserStyle
     */
    private static String printSldUserStyle(UserStyle style)
    {
        StringBuilder sb = new StringBuilder(64).append(" UserStyle[");
        if (StringUtils.isNotEmpty(style.getName()))
        {
            sb.append(" Name[").append(style.getName()).append(']');
        }
        if (StringUtils.isNotEmpty(style.getTitle()))
        {
            sb.append(" Title[").append(style.getTitle()).append(']');
        }
        for (FeatureTypeStyle type : style.getFeatureTypeStyle())
        {
            sb.append(" FeatureTypeStyle[");
            if (StringUtils.isNotEmpty(type.getName()))
            {
                sb.append(" Name[").append(type.getName()).append(']');
            }
            if (StringUtils.isNotEmpty(type.getTitle()))
            {
                sb.append(" Title[").append(type.getTitle()).append(']');
            }
            for (Rule rule : type.getRule())
            {
                sb.append(printSldRule(rule));
            }
            sb.append(']');
        }
        sb.append(']');
        return sb.toString();
    }

    /** Forbid instantiation of utility class. */
    private SldConfigUtilities()
    {
    }
}
