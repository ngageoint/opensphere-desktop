package io.opensphere.myplaces.specific.tracks;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import de.micromata.opengis.kml.v_2_2_0.ExtendedData;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.core.Toolbox;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.control.action.context.GeometryContextKey;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.units.InvalidUnitsException;
import io.opensphere.core.units.length.Length;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataTypeController;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.util.DataElementLookupUtils;
import io.opensphere.mantle.transformer.impl.DefaultMapDataElementTransformer;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.myplaces.constants.Constants;
import io.opensphere.myplaces.models.DataCouple;
import io.opensphere.myplaces.models.DataTypeInfoMyPlaceChangedEvent;
import io.opensphere.myplaces.models.MyPlacesDataTypeInfo;
import io.opensphere.myplaces.models.MyPlacesModel;
import io.opensphere.myplaces.util.ExtendedDataUtils;
import io.opensphere.myplaces.util.GroupUtils;
import io.opensphere.tracktool.model.Track;
import io.opensphere.tracktool.model.TrackNode;
import io.opensphere.tracktool.registry.TrackRegistry;

/**
 * Provides the context menu items for the context menu on the map.
 *
 */
public class TracksContextMenuProvider implements ContextMenuProvider<GeometryContextKey>
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(TracksContextMenuProvider.class);

    /**
     * The toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * The model.
     */
    private final MyPlacesModel myModel;

    /**
     * Constructs a new track context menu provider.
     *
     * @param toolbox The toolbox.
     * @param model The model.
     */
    public TracksContextMenuProvider(Toolbox toolbox, MyPlacesModel model)
    {
        myToolbox = toolbox;
        myModel = model;
    }

    @Override
    public List<JMenuItem> getMenuItems(String contextId, GeometryContextKey key)
    {
        if (!(key.getGeometry() instanceof PolylineGeometry))
        {
            return null;
        }

        final PolylineGeometry geom = (PolylineGeometry)key.getGeometry();
        List<JMenuItem> menuItems = null;
        final Track associatedTrack = getAssociatedTrack(geom);
        if (associatedTrack != null)
        {
            menuItems = New.list();

            JMenu unitsMenu = new JMenu("Change Units");
            addUnitsMenuItems(geom, unitsMenu);
            menuItems.add(unitsMenu);

            JMenuItem select = new JMenuItem("Remove Track");
            select.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    DataCouple couple = GroupUtils.getDataTypeAndParent(associatedTrack.getId(), myModel.getDataGroups());
                    if (couple.getDataType() != null)
                    {
                        int response = JOptionPane.showConfirmDialog(myToolbox.getUIRegistry().getMainFrameProvider().get(),
                                "Are you sure you want to delete " + couple.getDataType().getDisplayName() + "?",
                                "Delete Confirmation", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                        if (response == JOptionPane.OK_OPTION)
                        {
                            couple.getDataGroup().removeMember(couple.getDataType(), false, this);
                        }
                    }
                }
            });
            menuItems.add(select);

            JMenuItem bufferForTrack = new JMenuItem("Create Buffer for Track");
            bufferForTrack.addActionListener(e -> {
                DataCouple couple = GroupUtils.getDataTypeAndParent(associatedTrack.getId(), myModel.getDataGroups());
                if (couple.getDataType() != null)
                {
                    DataTypeInfo dataType = couple.getDataType();

                    DataElementLookupUtils dataElementLookupUtils = MantleToolboxUtils.getDataElementLookupUtils(myToolbox);
                    List<DataElement> dataElements = dataElementLookupUtils.getDataElements(dataType);

                    MantleToolbox mantleToolbox = MantleToolboxUtils.getMantleToolbox(myToolbox);
                    DataTypeController dataTypeController = mantleToolbox.getDataTypeController();
                    DefaultMapDataElementTransformer transformer = (DefaultMapDataElementTransformer)dataTypeController
                            .getTransformerForType(dataType.getTypeKey());

                    Set<Geometry> allGeometries = transformer.getGeometrySet();

                    transformer.getDataModelIdFromGeometryId(getPriority());

                    LOGGER.info("All Geometries retrieved.");
                }

            });

            menuItems.add(bufferForTrack);


            final boolean bubbleState = associatedTrack.isShowBubble();
            String menuLabel = bubbleState ? "Hide Labels" : "Show Labels";
            JMenuItem bubbles = new JMenuItem(menuLabel);
            bubbles.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    Placemark placemark = null;
                    DataCouple couple = GroupUtils.getDataTypeAndParent(associatedTrack.getId(), myModel.getDataGroups());
                    DataTypeInfo dataType = couple.getDataType();
                    if (dataType instanceof MyPlacesDataTypeInfo)
                    {
                        MyPlacesDataTypeInfo dataTypeInfo = (MyPlacesDataTypeInfo)dataType;
                        placemark = dataTypeInfo.getKmlPlacemark();
                        ExtendedData extendedData = placemark.getExtendedData();

                        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_ANNOHIDE_ID, bubbleState);

                        dataType.fireChangeEvent(new DataTypeInfoMyPlaceChangedEvent(dataType, this));
                    }
                }
            });
            menuItems.add(bubbles);

            JMenuItem selectEx = new JMenuItem("Clear Tracks");
            selectEx.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    int response = JOptionPane.showConfirmDialog(myToolbox.getUIRegistry().getMainFrameProvider().get(),
                            "Are you sure you want to remove all tracks?", "Delete Tracks Confirmation",
                            JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (response == JOptionPane.OK_OPTION)
                    {
                        Collection<Track> allTracks = TrackRegistry.getInstance().getTracks();
                        for (Track aTrack : allTracks)
                        {
                            DataCouple couple = GroupUtils.getDataTypeAndParent(aTrack.getId(), myModel.getDataGroups());
                            couple.getDataGroup().removeMember(couple.getDataType(), false, this);
                        }
                    }
                }
            });
            menuItems.add(selectEx);
        }

        return menuItems;
    }

    @Override
    public int getPriority()
    {
        return 11301;
    }

    /**
     * Add all of the available units for the length to the menu.
     *
     * @param geom The geometry.
     * @param unitsMenu The unit change sub-menu.
     */
    private void addUnitsMenuItems(final PolylineGeometry geom, JMenu unitsMenu)
    {
        Collection<Class<? extends Length>> availableUnits = myToolbox.getUnitsRegistry().getAvailableUnits(Length.class, true);
        availableUnits.add(null);
        for (final Class<? extends Length> lengthType : availableUnits)
        {
            JMenuItem unitsItem;
            try
            {
                String selectionLabel = lengthType == null
                        ? "System Default ("
                                + Length.getSelectionLabel(myToolbox.getUnitsRegistry().getPreferredUnits(Length.class)) + ")"
                        : Length.getSelectionLabel(lengthType);
                unitsItem = new JMenuItem(selectionLabel);
                unitsItem.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        handleUnitChange(geom, lengthType);
                    }
                });
                unitsMenu.add(unitsItem);
            }
            catch (InvalidUnitsException e)
            {
                LOGGER.warn("Could not use length type: " + e, e);
            }
        }
    }

    /**
     * Get the arc which owns the polyline.
     *
     * @param geom the polyline for which the owing arc is desired.
     * @return the arc which owns the polyline.
     */
    private Track getAssociatedTrack(PolylineGeometry geom)
    {
        Track track = null;
        if (geom.getVertices().size() == 2 && GeographicPosition.class.isAssignableFrom(geom.getPositionType()))
        {
            GeographicPosition start = (GeographicPosition)geom.getVertices().get(0);
            GeographicPosition end = (GeographicPosition)geom.getVertices().get(1);

            for (Track aTrack : TrackRegistry.getInstance().getTracks())
            {
                List<? extends TrackNode> nodes = aTrack.getNodes();
                for (int i = 0; i < nodes.size() - 1; ++i)
                {
                    if (nodes.get(i).getLocation().equals(start.getLatLonAlt())
                            && nodes.get(i + 1).getLocation().equals(end.getLatLonAlt()))
                    {
                        track = aTrack;
                        break;
                    }
                }

                if (track != null)
                {
                    break;
                }
            }
        }

        return track;
    }

    /**
     * Change the arc length label for the geometry to match the given units.
     *
     * @param geom The arc.
     * @param units The units in which to display length.
     */
    private void handleUnitChange(PolylineGeometry geom, Class<? extends Length> units)
    {
        Track track = getAssociatedTrack(geom);
        DataCouple couple = GroupUtils.getDataTypeAndParent(track.getId(), myModel.getDataGroups());
        DataTypeInfo dataType = couple.getDataType();
        if (dataType instanceof MyPlacesDataTypeInfo)
        {
            MyPlacesDataTypeInfo dataTypeInfo = (MyPlacesDataTypeInfo)dataType;
            Placemark placemark = dataTypeInfo.getKmlPlacemark();
            ExtendedData extendedData = placemark.getExtendedData();

            if (units == null)
            {
                ExtendedDataUtils.removeData(extendedData, Constants.UNITS_ID);
            }
            else
            {
                ExtendedDataUtils.putString(extendedData, Constants.UNITS_ID, units.getName());
            }

            dataType.fireChangeEvent(new DataTypeInfoMyPlaceChangedEvent(dataType, this));
        }
    }
}
