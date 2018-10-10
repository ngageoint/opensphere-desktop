package io.opensphere.mantle.data.geom.factory.impl;

import java.awt.Color;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.AbstractRenderableGeometry;
import io.opensphere.core.geometry.ImageManager;
import io.opensphere.core.geometry.PointSpriteGeometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.DefaultBaseAltitudeRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPointRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPointSizeRenderProperty;
import io.opensphere.core.geometry.renderproperties.PointRenderProperties;
import io.opensphere.core.geometry.renderproperties.PointScaleRenderProperty;
import io.opensphere.core.geometry.renderproperties.PointSizeRenderProperty;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.MapIconGeometrySupport;
import io.opensphere.mantle.data.geom.factory.RenderPropertyPool;
import io.opensphere.mantle.icon.IconImageProvider;
import io.opensphere.mantle.icon.IconProvider;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.icon.impl.IconProviderFactory;
import io.opensphere.mantle.util.MantleConstants;
import io.opensphere.mantle.util.MantleToolboxUtils;

/** Factory class to create geometry from geometry support class. */
public final class MapIconGeometryConverter extends AbstractGeometryConverter
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(MapIconGeometryConverter.class);

    /**
     * Convert.
     *
     * @param tb the tb
     * @param iconReg the icon reg
     * @param geomSupport the geom support
     * @param id the id
     * @param dti the dti
     * @param visState - the {@link VisualizationState}
     * @param renderPropertyPool the render property pool
     * @return the point geometry
     */
    public static PointSpriteGeometry convert(Toolbox tb, IconRegistry iconReg, MapIconGeometrySupport geomSupport, long id,
            DataTypeInfo dti, VisualizationState visState, RenderPropertyPool renderPropertyPool)
    {
        MapVisualizationInfo mapVisInfo = dti.getMapVisualizationInfo();
        BasicVisualizationInfo basicVisInfo = dti.getBasicVisualizationInfo();

        PointSpriteGeometry.Builder<GeographicPosition> iconBuilder = new PointSpriteGeometry.Builder<>();

        PointRenderProperties props = getIconSizeRenderPropertiesIfAvailable(visState, mapVisInfo, basicVisInfo,
                renderPropertyPool, geomSupport, visState.isSelected() ? MantleConstants.SELECT_COLOR : geomSupport.getColor());

        iconBuilder.setPosition(new GeographicPosition(
                LatLonAlt.createFromDegreesMeters(geomSupport.getLocation().getLatD(), geomSupport.getLocation().getLonD(),
                        geomSupport.getLocation().getAltM() + visState.getAltitudeAdjust(), geomSupport.followTerrain()
                                ? Altitude.ReferenceLevel.TERRAIN : geomSupport.getLocation().getAltitudeReference())));

        iconBuilder.setDataModelId(id);

        IconImageProvider iip = determineIconImageProvider(iconReg, geomSupport);
        iconBuilder.setImageManager(new ImageManager(null, iip));

        // Add a time constraint if in time line mode.
        Constraints constraints = null;
        if (mapVisInfo != null && basicVisInfo.getLoadsTo().isTimelineEnabled() && !geomSupport.getTimeSpan().isTimeless())
        {
            constraints = createTimeConstraints(tb, dti, geomSupport.getTimeSpan());
        }

        PointSpriteGeometry geom = new PointSpriteGeometry(iconBuilder, props, constraints);
        return geom;
    }

    /**
     * Determine icon image provider.
     *
     * @param iconReg the icon reg
     * @param geomSupport the geom support
     * @return the icon image provider
     */
    private static IconImageProvider determineIconImageProvider(IconRegistry iconReg, MapIconGeometrySupport geomSupport)
    {
        URL iconURL = null;

        try
        {
            iconURL = new URL(geomSupport.getIconURL());
        }
        catch (MalformedURLException e)
        {
            LOGGER.warn("Using default icon, failed to load icon url: " + geomSupport.getIconURL(), e);
            iconURL = IconRegistry.DEFAULT_ICON_URL;
        }

        IconRecord record = iconReg.getIconRecord(iconURL);
        if (record == null)
        {
            IconProvider ip = IconProviderFactory.create(iconURL, null, null, MapIconGeometryConverter.class.getName());
            record = iconReg.addIcon(ip, MapIconGeometryConverter.class);
        }

        return iconReg.getLoadedIconPool().getIconImageProvider(record, geomSupport.getImageProcessor());
    }

    /**
     * Gets the size render properties if available, if not creates a new one
     * and adds it to the share with the provided size.
     *
     * @param visState the vis state
     * @param mapVisInfo Data type level info relevant for rendering.
     * @param basicVisInfo Basic information for the data type.
     * @param renderPropertyPool the render property pool
     * @param geomSupport the geom support
     * @param c the c
     * @return the point size render properties if available
     */
    private static PointRenderProperties getIconSizeRenderPropertiesIfAvailable(VisualizationState visState,
            MapVisualizationInfo mapVisInfo, BasicVisualizationInfo basicVisInfo, RenderPropertyPool renderPropertyPool,
            MapIconGeometrySupport geomSupport, Color c)
    {
        PointSizeRenderProperty rp = getIconSizeRenderPropertiesIfAvailable(visState, renderPropertyPool, geomSupport);

        int zOrder = visState.isSelected() ? ZOrderRenderProperties.TOP_Z : mapVisInfo == null ? 1000 : mapVisInfo.getZOrder();
        boolean pickable = basicVisInfo != null && basicVisInfo.getLoadsTo().isPickable();

        PointRenderProperties prop = new DefaultPointRenderProperties(
                new DefaultBaseAltitudeRenderProperties(zOrder, true, pickable, false), rp);
        prop.setColor(c);
        prop.setRenderingOrder(visState.isSelected() ? 1 : 0);
        prop = renderPropertyPool.getPoolInstance(prop);
        return prop;
    }

    /**
     * Gets the size render properties if available, if not creates a new one
     * and adds it to the share with the provided size.
     *
     * @param visState the vis state
     * @param renderPropertyPool the render property pool
     * @param geomSupport the geom support
     * @return the point size render properties if available
     */
    private static PointSizeRenderProperty getIconSizeRenderPropertiesIfAvailable(VisualizationState visState,
            RenderPropertyPool renderPropertyPool, MapIconGeometrySupport geomSupport)
    {
        PointSizeRenderProperty sizeRP;
        float iconSize = geomSupport.getIconSize() < 1f ? MapIconGeometrySupport.DEFAULT_ICON_SIZE : geomSupport.getIconSize();
        float iconHighlightSize = geomSupport.getIconHighlightSize() < 1f ? MapIconGeometrySupport.DEFAULT_ICON_SIZE
                : geomSupport.getIconHighlightSize();
        if (geomSupport.getImageProcessor() != null)
        {
            /* There is an image processor, so let the image processor determine
             * the size of the image. Core will just leave it as is (scale it by
             * a factor of 1). The icon may be scaled down here to account for
             * over-scaling due to a large highlight size. */
            PointScaleRenderProperty scaleProperty = new PointScaleRenderProperty();
            float iconScale = iconHighlightSize > iconSize ? iconSize / iconHighlightSize : 1f;
            scaleProperty.setSize(iconScale);
            scaleProperty.setHighlightSize(1f);
            scaleProperty.setScaleFunction(geomSupport.getScaleFunction());
            sizeRP = scaleProperty;
        }
        else
        {
            sizeRP = new DefaultPointSizeRenderProperty();
            sizeRP.setSize(iconSize);
            sizeRP.setHighlightSize(iconHighlightSize);
        }
        sizeRP = renderPropertyPool.getPoolInstance(sizeRP);
        return sizeRP;
    }

    /**
     * Instantiates a new map point geometry factory.
     *
     * @param tb the {@link Toolbox}
     */
    public MapIconGeometryConverter(Toolbox tb)
    {
        super(tb);
    }

    @Override
    public AbstractRenderableGeometry createGeometry(MapGeometrySupport geomSupport, long id, DataTypeInfo dti,
            VisualizationState visState, RenderPropertyPool renderPropertyPool)
    {
        IconRegistry iconReg = MantleToolboxUtils.getMantleToolbox(getToolbox()).getIconRegistry();
        if (getConvertedClassType().isAssignableFrom(geomSupport.getClass()))
        {
            MapIconGeometrySupport localSupport = (MapIconGeometrySupport)geomSupport;
            return MapIconGeometryConverter.convert(getToolbox(), iconReg, localSupport, id, dti, visState, renderPropertyPool);
        }
        else
        {
            throw new IllegalArgumentException("MapGeometrySupport \"" + geomSupport.getClass().getName()
                    + "\" is not an instance of \"" + getConvertedClassType().getName() + "\"");
        }
    }

    @Override
    public Class<?> getConvertedClassType()
    {
        return MapIconGeometrySupport.class;
    }
}
