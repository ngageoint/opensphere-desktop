package io.opensphere.myplaces.specific.tracks;

import java.awt.Color;
import java.util.Collection;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import de.micromata.opengis.kml.v_2_2_0.ExtendedData;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.core.Toolbox;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.control.action.context.GeometryContextKey;
import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.GenericFontIcon;
import io.opensphere.mantle.data.DataTypeInfo;
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

/** Provides the context menu items for the context menu on the map. */
public class TracksContextMenuProvider implements ContextMenuProvider<GeometryContextKey>
{
    /** The toolbox through which application state is accessed. */
    private final Toolbox myToolbox;

    /** The model in which place data is stored. */
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

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.control.action.ContextMenuProvider#getMenuItems(java.lang.String,
     *      java.lang.Object)
     */
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

            JMenuItem select = new JMenuItem("Remove Track", new GenericFontIcon(AwesomeIconSolid.TIMES, Color.WHITE));
            select.addActionListener(e ->
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
            });
            menuItems.add(select);

            final boolean bubbleState = associatedTrack.isShowBubble();
            String menuLabel = bubbleState ? "Hide Labels" : "Show Labels";
            GenericFontIcon menuIcon = new GenericFontIcon(
                    bubbleState ? AwesomeIconSolid.COMMENT_SLASH : AwesomeIconSolid.COMMENT, Color.WHITE);
            JMenuItem bubbles = new JMenuItem(menuLabel, menuIcon);
            bubbles.addActionListener(e ->
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
            });
            menuItems.add(bubbles);

            JMenuItem selectEx = new JMenuItem("Clear Tracks", new GenericFontIcon(AwesomeIconSolid.TIMES, Color.WHITE));
            selectEx.addActionListener(e ->
            {
                int response = JOptionPane.showConfirmDialog(myToolbox.getUIRegistry().getMainFrameProvider().get(),
                        "Are you sure you want to remove all tracks?", "Delete Tracks Confirmation", JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.OK_OPTION)
                {
                    Collection<Track> allTracks = TrackRegistry.getInstance().getTracks();
                    for (Track aTrack : allTracks)
                    {
                        DataCouple couple = GroupUtils.getDataTypeAndParent(aTrack.getId(), myModel.getDataGroups());
                        couple.getDataGroup().removeMember(couple.getDataType(), false, this);
                    }
                }
            });
            menuItems.add(selectEx);
        }

        return menuItems;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.control.action.ContextMenuProvider#getPriority()
     */
    @Override
    public int getPriority()
    {
        return 11301;
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
}
