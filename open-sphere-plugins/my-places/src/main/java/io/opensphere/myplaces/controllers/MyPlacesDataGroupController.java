package io.opensphere.myplaces.controllers;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventListener;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.event.DataTypeVisibilityChangeEvent;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.myplaces.constants.Constants;
import io.opensphere.myplaces.importer.KmlTranslator;
import io.opensphere.myplaces.models.MyPlacesDataGroupInfo;
import io.opensphere.myplaces.models.MyPlacesDataTypeInfo;
import io.opensphere.myplaces.models.MyPlacesModel;

/**
 * The controller for the data groups or active layer view portion of my
 * places..
 */
public class MyPlacesDataGroupController implements EventListener<DataTypeVisibilityChangeEvent>
{
    /**
     * Listens for child removed events.
     */
    private final DataGroupInfoChildRemovedListener myChildRemovedListener;

    /**
     * The color change listener.
     */
    private final DataTypeInfoColorChangeListener myColorListener;

    /**
     * Listens for group creations.
     */
    private final DataGroupInfoChildAddedListener myGroupAddedListener;

    /**
     * Translates the kml model into data groups.
     */
    private final KmlTranslator myKmlTranslator;

    /**
     * Listens for child add events.
     */
    private final DataGroupInfoMemberAddedListener myMemberAddedListener;

    /**
     * Listens for active data group changes.
     */
    private final DataGroupInfoMemberRemovedListener myMemberRemovedListener;

    /**
     * The my places model.
     */
    private final MyPlacesModel myModel;

    /**
     * The toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * Listens for changes of a data type info.
     */
    private final DataTypeInfoChangedListener myTypeChangedListener;

    /**
     * Constructs a new data group controller.
     *
     * @param toolbox The toolbox.
     * @param model The my places model.
     */
    public MyPlacesDataGroupController(Toolbox toolbox, MyPlacesModel model)
    {
        myToolbox = toolbox;
        myKmlTranslator = new KmlTranslator(myToolbox);
        myModel = model;
        myToolbox.getEventManager().subscribe(DataTypeVisibilityChangeEvent.class, this);
        myColorListener = new DataTypeInfoColorChangeListener(myToolbox, myModel);
        myMemberRemovedListener = new DataGroupInfoMemberRemovedListener(myToolbox, myModel, myKmlTranslator);
        myChildRemovedListener = new DataGroupInfoChildRemovedListener(myToolbox, myModel, myKmlTranslator);
        myMemberAddedListener = new DataGroupInfoMemberAddedListener(myToolbox, myModel);
        myTypeChangedListener = new DataTypeInfoChangedListener(myToolbox, myModel);
        myGroupAddedListener = new DataGroupInfoChildAddedListener(myToolbox, myModel);
    }

    /**
     * Closes open resources.
     */
    public void close()
    {
        myToolbox.getEventManager().unsubscribe(DataTypeVisibilityChangeEvent.class, this);
        myColorListener.close();
        myMemberRemovedListener.close();
        myChildRemovedListener.close();
        myMemberAddedListener.close();
        myTypeChangedListener.close();
        myGroupAddedListener.close();
    }

    /**
     * Loads the data groups and adds them to the layers tree.
     */
    public void loadDataGroups()
    {
        Kml kml = myModel.getMyPlaces();

        if (kml != null)
        {
            MyPlacesDataGroupInfo dataGroup = myKmlTranslator.fromKml(kml);

            if (dataGroup == null)
            {
                Document document = kml.createAndSetDocument();
                Folder folder = document.createAndAddFolder();
                folder.setName(Constants.MY_PLACES_LABEL);
                folder.setId(Constants.MY_PLACES_ID);
                folder.setVisibility(Boolean.TRUE);

                dataGroup = new MyPlacesDataGroupInfo(true, myToolbox, folder);
                dataGroup.setIsFlattenable(true);
            }

            DataGroupController controller = MantleToolboxUtils.getMantleToolbox(myToolbox).getDataGroupController();
            controller.addRootDataGroupInfo(dataGroup, this);
            setActive(dataGroup);

            myModel.setDataGroups(dataGroup);
        }
    }

    @Override
    public void notify(DataTypeVisibilityChangeEvent event)
    {
        DataTypeInfo info = event.getDataTypeInfo();
        if (info instanceof MyPlacesDataTypeInfo)
        {
            MyPlacesDataTypeInfo dataType = (MyPlacesDataTypeInfo)info;
            dataType.getKmlPlacemark().setVisibility(Boolean.valueOf(info.isVisible()));
            myModel.notifyObservers();
        }
    }

    /**
     * Sets the group and all of its descendants to active.
     *
     * @param group The group to set to active.
     */
    private void setActive(DataGroupInfo group)
    {
        group.groupStream().parallel().forEach(g -> g.activationProperty().setActive(true));
    }
}
