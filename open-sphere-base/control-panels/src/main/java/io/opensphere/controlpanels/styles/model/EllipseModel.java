package io.opensphere.controlpanels.styles.model;

import java.io.Serializable;
import java.util.Observable;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * The model to describe the ellipse style bullseye.
 */
public class EllipseModel extends Observable implements Serializable
{
    /**
     * The orientation property.
     */
    public static final String ORIENTATION_PROP = "orientation";

    /**
     * The semi major property.
     */
    public static final String SEMI_MAJOR_PROP = "semimajor";

    /**
     * The semi major units property.
     */
    public static final String SEMI_MAJOR_UNITS_PROP = "semimajorunits";

    /**
     * The semi minor property.
     */
    public static final String SEMI_MINOR_PROP = "semiminor";

    /**
     * The semi minor units property.
     */
    public static final String SEMI_MINOR_UNITS_PROP = "semiminorunits";

    /**
     * Defaults serialization id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The available units for semi major and minor lengths.
     */
    private transient ObservableList<String> myAvailableUnits;

    /**
     * Indicates if the ellipse style is enabled or not.
     */
    private transient BooleanProperty myEllipseEnabled;

    /**
     * The ellipse orientation.
     */
    private double myOrientation;

    /**
     * The semi major length.
     */
    private double mySemiMajor;

    /**
     * The semi major length units.
     */
    private String mySemiMajorUnits;

    /**
     * The semi minor length.
     */
    private double mySemiMinor;

    /**
     * The semi minor length units.
     */
    private String mySemiMinorUnits;

    /**
     * Gets the available units for semi major and minor lengths and initializes
     * it if it has not been done already.
     *
     * @return the availableUnits
     */
    public ObservableList<String> getAvailableUnits()
    {
        if (myAvailableUnits == null)
        {
            myAvailableUnits = FXCollections.observableArrayList();
        }
        return myAvailableUnits;
    }

    /**
     * Indicates if the ellipse is enabled and initializes the property if it
     * has not been initialize yet.
     *
     * @return the ellipseEnabled
     */
    public BooleanProperty getEllipseEnabled()
    {
        if (myEllipseEnabled == null)
        {
            myEllipseEnabled = new SimpleBooleanProperty();
        }
        return myEllipseEnabled;
    }

    /**
     * Gets the ellipse orientation.
     *
     * @return the orientation
     */
    public double getOrientation()
    {
        return myOrientation;
    }

    /**
     * Gets the semi major length.
     *
     * @return the semiMajor
     */
    public double getSemiMajor()
    {
        return mySemiMajor;
    }

    /**
     * Gets the semi major length units.
     *
     * @return the semiMajorUnits
     */
    public String getSemiMajorUnits()
    {
        return mySemiMajorUnits;
    }

    /**
     * Gets the semi minor length.
     *
     * @return the semiMinor
     */
    public double getSemiMinor()
    {
        return mySemiMinor;
    }

    /**
     * Gets the semi minor length units.
     *
     * @return the semiMinorUnits
     */
    public String getSemiMinorUnits()
    {
        return mySemiMinorUnits;
    }

    /**
     * Sets the ellipse orientation.
     *
     * @param orientation the orientation to set
     */
    public void setOrientation(double orientation)
    {
        myOrientation = orientation;
        setChanged();
        notifyObservers(ORIENTATION_PROP);
    }

    /**
     * Sets the semi major length.
     *
     * @param semiMajor the semiMajor to set
     */
    public void setSemiMajor(double semiMajor)
    {
        mySemiMajor = semiMajor;
        setChanged();
        notifyObservers(SEMI_MAJOR_PROP);
    }

    /**
     * Sets the semi major length units.
     *
     * @param semiMajorUnits the semiMajorUnits to set
     */
    public void setSemiMajorUnits(String semiMajorUnits)
    {
        mySemiMajorUnits = semiMajorUnits;
        setChanged();
        notifyObservers(SEMI_MAJOR_UNITS_PROP);
    }

    /**
     * Sets the semi minor length.
     *
     * @param semiMinor the semiMinor to set
     */
    public void setSemiMinor(double semiMinor)
    {
        mySemiMinor = semiMinor;
        setChanged();
        notifyObservers(SEMI_MINOR_PROP);
    }

    /**
     * Sets the semi minor length units.
     *
     * @param semiMinorUnits the semiMinorUnits to set
     */
    public void setSemiMinorUnits(String semiMinorUnits)
    {
        mySemiMinorUnits = semiMinorUnits;
        setChanged();
        notifyObservers(SEMI_MINOR_UNITS_PROP);
    }
}
