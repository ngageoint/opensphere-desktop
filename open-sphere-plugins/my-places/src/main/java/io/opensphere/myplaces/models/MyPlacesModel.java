package io.opensphere.myplaces.models;

import java.util.Map;
import java.util.Observable;
import java.util.function.Function;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.core.util.collections.New;

/**
 * The model representing all my places data.
 */
public class MyPlacesModel extends Observable
{
    /**
     * The my data groups.
     */
    private volatile MyPlacesDataGroupInfo myDataGroups;

    /** The Geom id to place marks. */
    private final Map<Long, Placemark> myGeomIdToPlaceMarks = New.map();

    /**
     * The my places tree.
     */
    private volatile Kml myPlaces;

    /**
     * Apply a function to each placemark in a feature.
     *
     * @param func The function to be applied.
     * @param feature The feature.
     */

    public static void applyToEachPlacemark(Function<Placemark, Void> func, Feature feature)
    {
        if (feature instanceof Placemark)
        {
            func.apply((Placemark)feature);
        }
        else if (feature instanceof Folder)
        {
            for (Feature child : ((Folder)feature).getFeature())
            {
                applyToEachPlacemark(func, child);
            }
        }
        else if (feature instanceof Document)
        {
            for (Feature child : ((Document)feature).getFeature())
            {
                applyToEachPlacemark(func, child);
            }
        }
    }

    /**
     * Apply a function to each placemark in my places.
     *
     * @param func The function.
     */
    public void applyToEachPlacemark(Function<Placemark, Void> func)
    {
        applyToEachPlacemark(func, myPlaces.getFeature());
    }

    /**
     * Find a placemark using the id.
     *
     * @param id the id
     * @return the placemark
     */
    public Placemark findPlacemark(long id)
    {
        return myGeomIdToPlaceMarks.get(Long.valueOf(id));
    }

    /**
     * Gets the root data group for my places.
     *
     * @return the root data group.
     */
    public MyPlacesDataGroupInfo getDataGroups()
    {
        return myDataGroups;
    }

    /**
     * Gets the geom id to place marks.
     *
     * @return the geom id to place marks
     */
    public Map<Long, Placemark> getGeomIdToPlaceMarks()
    {
        return myGeomIdToPlaceMarks;
    }

    /**
     * Gets the my places tree.
     *
     * @return The my places tree.
     */
    public Kml getMyPlaces()
    {
        return myPlaces;
    }

    @Override
    public void notifyObservers()
    {
        setChanged();
        super.notifyObservers();
    }

    /**
     * Sets the root data group for my places.
     *
     * @param dataGroups The data groups.
     */
    public void setDataGroups(MyPlacesDataGroupInfo dataGroups)
    {
        myDataGroups = dataGroups;
    }

    /**
     * Sets the my places tree.
     *
     * @param newMyPlaces The new my places tree.
     */
    public void setMyPlaces(Kml newMyPlaces)
    {
        myPlaces = newMyPlaces;
        super.setChanged();
        super.notifyObservers();
    }
}
