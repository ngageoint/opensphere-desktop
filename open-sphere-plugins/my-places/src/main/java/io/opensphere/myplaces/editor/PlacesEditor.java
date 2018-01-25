package io.opensphere.myplaces.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.UUID;

import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.core.Toolbox;
import io.opensphere.mantle.mp.event.impl.MapAnnotationCreatedEvent;
import io.opensphere.myplaces.editor.controller.AnnotationEditController;
import io.opensphere.myplaces.editor.view.AnnotationEditorPanel;
import io.opensphere.myplaces.models.DataTypeInfoMyPlaceChangedEvent;
import io.opensphere.myplaces.models.MyPlacesDataGroupInfo;
import io.opensphere.myplaces.models.MyPlacesDataTypeInfo;
import io.opensphere.myplaces.models.MyPlacesEditListener;
import io.opensphere.myplaces.util.PlacemarkUtils;

/**
 * Edits a single point.
 *
 */
public class PlacesEditor implements ActionListener
{
    /**
     * The tool box.
     */
    private final Toolbox myToolbox;

    /**
     * The placemark being edited.
     */
    private Placemark myEditPlacemark;

    /**
     * The data type info being edited.
     */
    private MyPlacesDataTypeInfo myEditType;

    /**
     * The group containing the the edit type.
     */
    private MyPlacesDataGroupInfo myEditGroup;

    /**
     * The edit listener.
     */
    private MyPlacesEditListener myEditListener;

    /** The Annotation points controller. */
    private final AnnotationEditController myAnnotationPointsController;

    /**
     * Instantiates a new point editor.
     *
     * @param controller the controller
     */
    public PlacesEditor(AnnotationEditController controller)
    {
        myToolbox = controller.getToolbox();
        myAnnotationPointsController = controller;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (!AnnotationEditorPanel.RESULT_CANCEL.equals(e.getActionCommand()))
        {
            if (myEditType == null)
            {
                handleCreate();
            }
            else
            {
                handleEdit();
            }
        }
        else
        {
            myToolbox.getEventManager().publishEvent(new MapAnnotationCreatedEvent(this, false));
        }
    }

    /**
     * Allows the user to edit a newly created place.
     *
     * @param place The newly created place.
     * @param dataGroup The data group the place will belong to.
     * @param editListener The listens when the user wants to edit the place.
     */
    public void createPlace(Placemark place, MyPlacesDataGroupInfo dataGroup, MyPlacesEditListener editListener)
    {
        AnnotationEditorPanel panel = instantiateEditor(myAnnotationPointsController);
        myEditType = null;
        myEditGroup = dataGroup;
        myEditListener = editListener;
        myEditPlacemark = place;
        panel.setPlace(myEditPlacemark, this);
        panel.showEditor(myToolbox.getUIRegistry().getMainFrameProvider().get());
    }

    /**
     * Allows the user to edit a single place.
     *
     * @param dataType the place to edit.
     * @param dataGroup The group containing the type.
     * @param editListener The edit listener.
     */
    public void editPlace(MyPlacesDataTypeInfo dataType, MyPlacesDataGroupInfo dataGroup, MyPlacesEditListener editListener)
    {
        AnnotationEditorPanel panel = instantiateEditor(myAnnotationPointsController);
        myEditType = dataType;
        myEditGroup = dataGroup;
        myEditListener = editListener;
        myEditPlacemark = dataType.getKmlPlacemark();
        panel.setPlace(myEditPlacemark, this);
        panel.showEditor(myToolbox.getUIRegistry().getMainFrameProvider().get());
    }

    /**
     * Creates the editor to edit the placemark.
     *
     * @param controller The edit controller.
     * @return The editor.
     */
    protected AnnotationEditorPanel instantiateEditor(AnnotationEditController controller)
    {
        return new AnnotationEditorPanel(controller);
    }

    /**
     * Handles the creation of a place.
     */
    private void handleCreate()
    {
        myEditGroup.getKmlFolder().addToFeature(myEditPlacemark);
        myEditPlacemark.setVisibility(Boolean.TRUE);
        MyPlacesDataTypeInfo dataType = PlacemarkUtils.createDataType(myEditPlacemark, myToolbox, this, myEditListener);
        myEditGroup.addMember(dataType, this);
        myToolbox.getEventManager().publishEvent(new MapAnnotationCreatedEvent(this, false));
    }

    /**
     * Handles the edit of an existing place.
     */
    private void handleEdit()
    {
        if (!myEditPlacemark.getName().equals(myEditType.getDisplayName()))
        {
            Placemark placemark = myEditGroup.getKmlFolder().createAndAddPlacemark();

            PlacemarkUtils.copyPlacemark(myEditPlacemark, placemark);
            placemark.setId(UUID.randomUUID().toString());

            placemark.setVisibility(Boolean.TRUE);
            MyPlacesDataTypeInfo dataType = PlacemarkUtils.createDataType(placemark, myToolbox, this, myEditListener);
            myEditGroup.addMember(dataType, this);

            myEditGroup.removeMember(myEditType, false, this);
        }
        else
        {
            myEditType.fireChangeEvent(new DataTypeInfoMyPlaceChangedEvent(myEditType, this));
        }
    }
}
