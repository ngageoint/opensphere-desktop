package io.opensphere.myplaces.importer;

import java.awt.Color;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import io.opensphere.core.control.action.MenuOption;
import io.opensphere.core.export.ExportException;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.swing.GenericFontIcon;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.geom.MapLocationGeometrySupport;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.AbstractFeatureVisualizationStyle;
import io.opensphere.myplaces.models.MyPlacesEditListener;
import io.opensphere.myplaces.specific.factory.TypeControllerFactory;
import io.opensphere.tracktool.model.TrackNode;
import io.opensphere.tracktool.model.impl.DefaultTrackNode;
import io.opensphere.tracktool.registry.TrackRegistry;

/**
 * An exporter that creates tracks from {@link DataElement}s.
 */
public class DataElementTrackExporter extends AbstractDataElementExporter
{
    /** The name of the command / menu option to create track from selection. */
    private static final String CREATE_TRACK_COMMAND = "Create Track From Selection";

    @Override
    public MenuOption getMenuOption()
    {
        return new MenuOption(CREATE_TRACK_COMMAND, CREATE_TRACK_COMMAND,
                "Creates a new track by linking selected features in time order",
                new GenericFontIcon(AwesomeIconSolid.SHARE_ALT, Color.WHITE));
    }

    @Override
    protected void export(DataTypeInfo dataType, Collection<? extends MapDataElement> elements) throws ExportException
    {
        final AbstractFeatureVisualizationStyle style = getStyle(dataType);

        List<TrackNode> trackNodes = elements.stream()
                .filter(e -> e.getMapGeometrySupport() instanceof MapLocationGeometrySupport).sorted(new TimeComparator())
                .map(e -> createTrackNode(e, style)).collect(Collectors.toList());

        if (!trackNodes.isEmpty())
        {
            MyPlacesEditListener listener = TypeControllerFactory.getInstance()
                    .getController(MapVisualizationType.USER_TRACK_ELEMENTS);
            TrackRegistry.getInstance().createNewTrackFromNodes(trackNodes, getToolbox(), getParentGroup(), listener);
        }
        else
        {
            throw new ExportException("The selected data do not contain any points.");
        }
    }

    /**
     * Gets the style for the data type.
     *
     * @param dataType the data type
     * @return the style
     */
    private AbstractFeatureVisualizationStyle getStyle(DataTypeInfo dataType)
    {
        AbstractFeatureVisualizationStyle style = null;
        MantleToolbox mantleToolbox = getToolbox().getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class);
        VisualizationStyle visStyle = mantleToolbox.getVisualizationStyleRegistry().getStyle(MapLocationGeometrySupport.class,
                dataType.getTypeKey(), true);
        if (visStyle instanceof AbstractFeatureVisualizationStyle)
        {
            style = (AbstractFeatureVisualizationStyle)visStyle;
        }
        return style;
    }

    /**
     * Creates a track node from the data element.
     *
     * @param dataElement the data element
     * @param style the style
     * @return the track node
     */
    private static DefaultTrackNode createTrackNode(MapDataElement dataElement, AbstractFeatureVisualizationStyle style)
    {
        LatLonAlt loc = ((MapLocationGeometrySupport)dataElement.getMapGeometrySupport()).getLocation();
        if (style != null && style.isUseAltitude())
        {
            double alt = 0.;
            Double altitudeColumnValueM = style.getAltitudeColumnValueM(dataElement.getMetaData());
            if (altitudeColumnValueM != null)
            {
                alt = altitudeColumnValueM.doubleValue();
            }
            loc = LatLonAlt.createFromDegreesMeters(loc.getLatD(), loc.getLonD(), alt, loc.getAltitudeReference());
        }

        return new DefaultTrackNode(loc, dataElement.getTimeSpan(), Long.valueOf(dataElement.getIdInCache()));
    }

    /** A comparator for sorting data elements by time. */
    private static class TimeComparator implements Comparator<DataElement>, Serializable
    {
        /** Serial. */
        private static final long serialVersionUID = 1L;

        @Override
        public int compare(DataElement o1, DataElement o2)
        {
            if (o1.getTimeSpan() != null)
            {
                return o2.getTimeSpan() != null ? o1.getTimeSpan().compareTo(o2.getTimeSpan()) : -1;
            }
            else if (o2.getTimeSpan() != null)
            {
                return 1;
            }
            return o1.hashCode() < o2.hashCode() ? -1 : o1.hashCode() > o2.hashCode() ? 1 : 0;
        }
    }
}
