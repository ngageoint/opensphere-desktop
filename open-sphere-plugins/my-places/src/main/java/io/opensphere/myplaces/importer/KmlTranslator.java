package io.opensphere.myplaces.importer;

import java.awt.Color;
import java.util.UUID;

import de.micromata.opengis.kml.v_2_2_0.BalloonStyle;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Style;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.myplaces.constants.Constants;
import io.opensphere.myplaces.models.MyPlacesDataGroupInfo;
import io.opensphere.myplaces.models.MyPlacesDataTypeInfo;
import io.opensphere.myplaces.specific.factory.TypeControllerFactory;
import io.opensphere.myplaces.util.ExtendedDataUtils;
import io.opensphere.myplaces.util.PlacemarkUtils;

/**
 * Translates to and from DataGroupInfo's to kml.
 *
 */
public class KmlTranslator
{
    /**
     * The toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * Constructs a new kml translator.
     *
     * @param toolbox the toolbox.
     */
    public KmlTranslator(Toolbox toolbox)
    {
        myToolbox = toolbox;
    }

    /**
     * Takes the kml tree and translates it into a data group info tree.
     *
     * @param kml The kml to translate.
     * @return The data group info tree.
     */
    public MyPlacesDataGroupInfo fromKml(Kml kml)
    {
        MyPlacesDataGroupInfo dataGroup = null;
        Feature feature = kml.getFeature();
        if (feature instanceof Document)
        {
            Document document = (Document)feature;
            for (Feature aFeature : document.getFeature())
            {
                if (aFeature instanceof Folder)
                {
                    dataGroup = parseFolder((Folder)aFeature, true);
                }
            }
        }

        return dataGroup;
    }

    /**
     * Parses a folder feature and create all necessary data group and type
     * infos.
     *
     * @param folder The folder to parse.
     * @param isRoot Indicates if this data group is the root.
     * @return The data group info representing the folder.
     */
    public MyPlacesDataGroupInfo parseFolder(Folder folder, boolean isRoot)
    {
        if (folder.getId() == null)
        {
            folder.setId(UUID.randomUUID().toString());
        }

        MyPlacesDataGroupInfo dataGroup = new MyPlacesDataGroupInfo(isRoot, myToolbox, folder);
        Boolean isVisibility = folder.isVisibility();
        if (!Boolean.TRUE.equals(isVisibility))
        {
            dataGroup.setGroupVisible(null, false, false, this);
        }

        for (Feature feature : folder.getFeature())
        {
            if (feature instanceof Placemark)
            {
                MyPlacesDataTypeInfo dataType = parsePlacemark((Placemark)feature);
                dataGroup.addMember(dataType, this);
            }
            else if (feature instanceof Folder)
            {
                MyPlacesDataGroupInfo childGroup = parseFolder((Folder)feature, false);
                dataGroup.addChild(childGroup, this);
            }
        }

        return dataGroup;
    }

    /**
     * Parses a placemark feature and creates the necessary data type info.
     *
     * @param placemark The placemark to parse.
     * @return The data type info representing the placemark.
     */
    public MyPlacesDataTypeInfo parsePlacemark(Placemark placemark)
    {
        MapVisualizationType visType = ExtendedDataUtils.getVisualizationType(placemark.getExtendedData());
        MyPlacesDataTypeInfo dataType = PlacemarkUtils.createDataType(placemark, myToolbox, this,
                TypeControllerFactory.getInstance().getController(visType));
        return dataType;
    }

    /**
     * Creates kml from the groups.
     *
     * @param folder The folder to add kml to.
     * @param group The group to translate to kml.
     */
    public void toKml(Folder folder, MyPlacesDataGroupInfo group)
    {
        for (DataTypeInfo member : group.getMembers(false))
        {
            if (member instanceof MyPlacesDataTypeInfo)
            {
                MyPlacesDataTypeInfo placesMember = (MyPlacesDataTypeInfo)member;
                Placemark existingPlacemark = placesMember.getKmlPlacemark();

                Placemark newPlacemark = folder.createAndAddPlacemark();
                newPlacemark.setName(existingPlacemark.getName());
                newPlacemark.setDescription(existingPlacemark.getDescription());
                newPlacemark.setId(existingPlacemark.getId());
                newPlacemark.setGeometry(existingPlacemark.getGeometry());
                newPlacemark.setExtendedData(existingPlacemark.getExtendedData());
                newPlacemark.setVisibility(existingPlacemark.isVisibility());
                newPlacemark.setTimePrimitive(existingPlacemark.getTimePrimitive());

                Color color = PlacemarkUtils.getPlacemarkColor(existingPlacemark);
                Style style = PlacemarkUtils.setPlacemarkColor(newPlacemark, color);

                Color textColor = PlacemarkUtils.getPlacemarkTextColor(existingPlacemark);

                BalloonStyle balloonStyle = new BalloonStyle();

                if (textColor != null)
                {
                    balloonStyle.setColor(style.getIconStyle().getColor());
                    balloonStyle.setTextColor(ColorUtilities.convertToHexString(textColor, 3, 2, 1, 0));
                }

                style.setBalloonStyle(balloonStyle);

                placesMember.setKmlPlacemark(newPlacemark);
            }
        }

        for (DataGroupInfo child : group.getChildren())
        {
            if (child instanceof MyPlacesDataGroupInfo)
            {
                MyPlacesDataGroupInfo childGroup = (MyPlacesDataGroupInfo)child;
                Folder existing = childGroup.getKmlFolder();
                Folder childFolder = folder.createAndAddFolder();
                childFolder.setName(existing.getName());
                childFolder.setId(existing.getId());
                childFolder.setVisibility(existing.isVisibility());
                childGroup.setKmlFolder(childFolder);

                toKml(childFolder, childGroup);
            }
        }
    }

    /**
     * Takes the my places root data group and translates the tree structure
     * into a kml tree.
     *
     * @param rootGroup The root group.
     * @return A kml tree representing the data group tree.
     */
    public Kml toKml(MyPlacesDataGroupInfo rootGroup)
    {
        Kml kml = new Kml();
        Document document = kml.createAndSetDocument();
        document.setName(Constants.MY_PLACES_LABEL);
        Style style = new Style();
        style.setId("unfilled");
        style.createAndSetPolyStyle().setFill(Boolean.FALSE);
        document.addToStyleSelector(style);

        Folder topFolder = document.createAndAddFolder();
        topFolder.setName(rootGroup.getKmlFolder().getName());
        topFolder.setId(rootGroup.getKmlFolder().getId());
        rootGroup.setKmlFolder(topFolder);

        toKml(topFolder, rootGroup);

        return kml;
    }
}
