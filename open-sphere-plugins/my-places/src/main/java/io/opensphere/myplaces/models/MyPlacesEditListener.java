package io.opensphere.myplaces.models;

import java.util.List;

import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.mantle.data.DataGroupInfo;

/**
 * Interface to a listener that listens for launch editor events.
 *
 */
public interface MyPlacesEditListener
{
    /**
     * Launches the editor.
     *
     * @param dataTypes The data type to edit.
     * @param dataGroup The group containing the type to launch the editor for.
     */
    void launchEditor(List<MyPlacesDataTypeInfo> dataTypes, DataGroupInfo dataGroup);

    /**
     * Launches the editor for a newly created place.
     *
     * @param place The data type to edit.
     * @param dataGroup The group containing the type to launch the editor for.
     */
    void launchEditor(Placemark place, MyPlacesDataGroupInfo dataGroup);
}
