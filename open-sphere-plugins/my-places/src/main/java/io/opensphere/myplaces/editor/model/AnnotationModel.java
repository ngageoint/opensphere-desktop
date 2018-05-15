package io.opensphere.myplaces.editor.model;

import java.awt.Color;
import java.awt.Font;

import de.micromata.opengis.kml.v_2_2_0.ExtendedData;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.swing.FontWrapper;
import io.opensphere.core.util.swing.TextStyleModel;
import io.opensphere.core.util.swing.input.model.BooleanModel;
import io.opensphere.core.util.swing.input.model.ColorModel;
import io.opensphere.core.util.swing.input.model.WrappedModel;
import io.opensphere.myplaces.constants.Constants;
import io.opensphere.myplaces.util.ExtendedDataUtils;
import io.opensphere.myplaces.util.PlacemarkUtils;

/** The GUI model of an annotation point. */
@SuppressWarnings("PMD.GodClass")
public class AnnotationModel extends WrappedModel<Placemark>
{
    /** Whether to animate a timed my place. */
    private final BooleanModel myAnimate = new BooleanModel();

    /** The border color. */
    private final ColorModel myBorderColor = new ColorModel();

    /** The fill color of a polygon, if applicable. */
    private final ColorModel myPolygonFillColor = new ColorModel();

    /** The error message. */
    private String myError;

    /** Indicates if the model can even show headings and distances. */
    private boolean myIsDistanceHeadingCapable;

    /** Whether the bubble is filled. */
    private final BooleanModel myIsBubbleFilled = new BooleanModel();

    /** Whether the polygon is filled. */
    private final BooleanModel myIsPolygonFilled = new BooleanModel(false);

    /** Indicates if the model can even show locations. */
    private boolean myIsLocationCapable;

    /** Whether to show the altitude. */
    private final BooleanModel myShowAltitude = new BooleanModel();

    /** Whether to show the decimal lat/lon. */
    private final BooleanModel myShowDecimalLatLon = new BooleanModel();

    /** Whether to show the description. */
    private final BooleanModel myShowDescription = new BooleanModel();

    /** A flag used to indicate if the model can display velocity. */
    private boolean myVelocityCapable;

    /** The model used to store the display state of the velocity value. */
    private final BooleanModel myShowVelocity = new BooleanModel();

    /** A flag used to indicate if the model can display duration. */
    private boolean myDurationCapable;

    /** The model used to store the display state of the duration value. */
    private final BooleanModel myShowDuration = new BooleanModel();

    /** Whether to show the distance. */
    private final BooleanModel myShowDistance = new BooleanModel();

    /** Whether to show the DMS lat/lon. */
    private final BooleanModel myShowDMSLatLon = new BooleanModel();

    /** Whether to show the heading. */
    private final BooleanModel myShowHeading = new BooleanModel();

    /** Whether to show a timed my place in the timeline. */
    private final BooleanModel myShowInTimeline = new BooleanModel();

    /** Whether to show the MGRS lat/lon. */
    private final BooleanModel myShowMGRSLatLon = new BooleanModel();

    /** Whether to show the title. */
    private final BooleanModel myShowTitle = new BooleanModel();

    /** The model in which the display state of the field titles is stored. */
    private final BooleanModel myShowFieldTitles = new BooleanModel();

    /** The Text style model. */
    private final TextStyleModel myTextStyleModel = new TextStyleModel();

    /**
     * Returns a new font with the given style change applied.
     *
     * @param font The font
     * @param style The style to apply
     * @param isAdd True to add the style, false to remove it
     * @return The new font
     */
    private static Font changeStyle(Font font, int style, boolean isAdd)
    {
        return font.deriveFont(isAdd ? font.getStyle() | style : font.getStyle() & ~style);
    }

    /**
     * Count if true.
     *
     * @param value the value
     * @return the 1 if true 0 if false.
     */
    private static int countIfTrue(Boolean value)
    {
        return value != null && value.booleanValue() ? 1 : 0;
    }

    /** Constructor. */
    public AnnotationModel()
    {
        myShowTitle.setNameAndDescription("Title", "Whether to show the title in the bubble");
        myShowFieldTitles.setNameAndDescription("Field Titles", "Whether to show the titles of each field in the bubble");
        myShowDecimalLatLon.setNameAndDescription("Decimal lat/lon", "Whether to show the decimal lat/lon in the bubble");
        myShowDMSLatLon.setNameAndDescription("DMS lat/lon", "Whether to show the DMS lat/lon in the bubble");
        myShowMGRSLatLon.setNameAndDescription("MGRS", "Whether to show the MGRS lat/lon in the bubble");
        myShowAltitude.setNameAndDescription("Altitude", "Whether to show the altitude in the bubble");
        myShowDescription.setNameAndDescription("Description", "Whether to show the description lat/lon in the bubble");
        myIsBubbleFilled.setNameAndDescription("Fill Bubble", "Whether to fill the bubble background");
        myIsPolygonFilled.setNameAndDescription("Fill Polygon", "Whether to fill the polygon");
        myBorderColor.setNameAndDescription("Color", "The border color");
        myPolygonFillColor.setNameAndDescription("Fill Color", "The color with which to fill a polygon.");
        myShowDistance.setNameAndDescription("Distance", "Whether to show the distance in the bubble");
        myShowHeading.setNameAndDescription("Heading", "Whether to show the heading in the bubble");
        myShowVelocity.setNameAndDescription("Velocity", "Whether to show the velocity in the bubble");
        myShowDuration.setNameAndDescription("Duration", "Whether to show duration in the bubble.");
        myShowInTimeline.setNameAndDescription("Show on timeline", "Whether to show a my place with a time in the timeline");
        myAnimate.setNameAndDescription("Animate", "Whether to be able to animate a my place with time.");

        addModel(myShowTitle);
        addModel(myShowFieldTitles);
        addModel(myShowDecimalLatLon);
        addModel(myShowDMSLatLon);
        addModel(myShowMGRSLatLon);
        addModel(myShowAltitude);
        addModel(myShowDescription);
        addModel(myShowDistance);
        addModel(myShowHeading);
        addModel(myShowVelocity);
        addModel(myShowInTimeline);
        addModel(myAnimate);
        addModel(myTextStyleModel.getFont());
        addModel(myTextStyleModel.getFontSize());
        addModel(myTextStyleModel.getBold());
        addModel(myTextStyleModel.getItalic());
        addModel(myTextStyleModel.getFontColor());
        addModel(myBorderColor);
        addModel(myPolygonFillColor);
        addModel(myIsBubbleFilled);
        addModel(myIsPolygonFilled);
    }

    /**
     * Gets the model that indicates if a timed my place should be animated.
     *
     * @return The animate model.
     */
    public BooleanModel getAnimate()
    {
        return myAnimate;
    }

    /**
     * Gets the border color.
     *
     * @return the border color
     */
    public ColorModel getBorderColor()
    {
        return myBorderColor;
    }

    /**
     * Gets the value of the {@link #myPolygonFillColor} field.
     *
     * @return the value stored in the {@link #myPolygonFillColor} field.
     */
    public ColorModel getPolygonFillColor()
    {
        return myPolygonFillColor;
    }

    @Override
    public synchronized String getErrorMessage()
    {
        return myError != null ? myError : super.getErrorMessage();
    }

    /**
     * Getter for isFilled.
     *
     * @return the isFilled
     */
    public BooleanModel getBubbleFilled()
    {
        return myIsBubbleFilled;
    }

    /**
     * Gets the value of the {@link #myIsPolygonFilled} field.
     *
     * @return the value stored in the {@link #myIsPolygonFilled} field.
     */
    public BooleanModel getPolygonFilled()
    {
        return myIsPolygonFilled;
    }

    /**
     * Gets the showAltitude.
     *
     * @return the showAltitude
     */
    public BooleanModel getShowAltitude()
    {
        return myShowAltitude;
    }

    /**
     * Getter for decimalLatLon.
     *
     * @return the decimalLatLon
     */
    public BooleanModel getShowDecimalLatLon()
    {
        return myShowDecimalLatLon;
    }

    /**
     * Getter for description.
     *
     * @return the description
     */
    public BooleanModel getShowDescription()
    {
        return myShowDescription;
    }

    /**
     * Getter for distance..
     *
     * @return the distance.
     */
    public BooleanModel getShowDistance()
    {
        return myShowDistance;
    }

    /**
     * Getter for dMSLatLon.
     *
     * @return the dMSLatLon
     */
    public BooleanModel getShowDMSLatLon()
    {
        return myShowDMSLatLon;
    }

    /**
     * Getter for heading.
     *
     * @return the heading.
     */
    public BooleanModel getShowHeading()
    {
        return myShowHeading;
    }

    /**
     * Gets the value of the {@link #myShowVelocity} field.
     *
     * @return the value stored in the {@link #myShowVelocity} field.
     */
    public BooleanModel getShowVelocity()
    {
        return myShowVelocity;
    }

    /**
     * Gets the value of the {@link #myShowDuration} field.
     *
     * @return the value stored in the {@link #myShowDuration} field.
     */
    public BooleanModel getShowDuration()
    {
        return myShowDuration;
    }

    /**
     * Gets the model that indicates if a timed my place should show in the
     * timeline.
     *
     * @return The show in timeline model.
     */
    public BooleanModel getShowInTimeline()
    {
        return myShowInTimeline;
    }

    /**
     * Getter for mGRSLatLon.
     *
     * @return the mGRSLatLon
     */
    public BooleanModel getShowMGRSLatLon()
    {
        return myShowMGRSLatLon;
    }

    /**
     * Getter for title.
     *
     * @return the title
     */
    public BooleanModel getShowTitle()
    {
        return myShowTitle;
    }

    /**
     * Gets the value of the {@link #myShowFieldTitles} field.
     *
     * @return the value stored in the {@link #myShowFieldTitles} field.
     */
    public BooleanModel getShowFieldTitles()
    {
        return myShowFieldTitles;
    }

    /**
     * Gets the text style model.
     *
     * @return the text style model
     */
    public TextStyleModel getTextStyleModel()
    {
        return myTextStyleModel;
    }

    @Override
    public ValidationStatus getValidationStatus()
    {
        myError = null;
        if (super.getValidationStatus() == ValidationStatus.VALID)
        {
            if (!isValidating())
            {
                return ValidationStatus.VALID;
            }
            if (!bubbleCheckBoxesValid())
            {
                myError = "You must select an item under Bubble Contents.";
                return ValidationStatus.ERROR;
            }
            return ValidationStatus.VALID;
        }
        return ValidationStatus.ERROR;
    }

    /**
     * Indicates if the model can even show a heading or a distance.
     *
     * @return True if heading and distance should be shown, false otherwise.
     */
    public boolean isDistanceHeadingCapable()
    {
        return myIsDistanceHeadingCapable;
    }

    /**
     * Indicates if the model can even show a location information.
     *
     * @return True if locations should be shown, false otherwise.
     */
    public boolean isLocationCapable()
    {
        return myIsLocationCapable;
    }

    /**
     * Gets the value of the {@link #myVelocityCapable} field.
     *
     * @return the value stored in the {@link #myVelocityCapable} field.
     */
    public boolean isVelocityCapable()
    {
        return myVelocityCapable;
    }

    /**
     * Gets the value of the {@link #myDurationCapable} field.
     *
     * @return the value stored in the {@link #myDurationCapable} field.
     */
    public boolean isDurationCapable()
    {
        return myDurationCapable;
    }

    /**
     * Saves only the changed inputs to the given point.
     *
     * @param placemark The placemark
     */
    public void saveChangedInputs(Placemark placemark)
    {
        saveChangedAnnoSettings(placemark);
        saveChangedPointSettings(placemark);
    }

    /**
     * Saves the inputs to the given point.
     *
     * @param placemark The placemark
     */
    public void saveInputs(Placemark placemark)
    {
        updateDomainModel(placemark);
    }

    @Override
    protected void updateDomainModel(Placemark placemark)
    {
        ExtendedData extendedData = placemark.getExtendedData();

        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_TITLE, myShowTitle.get().booleanValue());
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_FIELD_TITLE, myShowFieldTitles.get().booleanValue());
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_LAT_LON_ID, myShowDecimalLatLon.get().booleanValue());
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_DMS_ID, myShowDMSLatLon.get().booleanValue());
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_MGRS, myShowMGRSLatLon.get().booleanValue());
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_SHOW_ALTITUDE, myShowAltitude.get().booleanValue());
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_DESC_ID, myShowDescription.get().booleanValue());
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_DISTANCE_ID, myShowDistance.get().booleanValue());
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_HEADING_ID, myShowHeading.get().booleanValue());
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_VELOCITY_ID, myShowVelocity.get().booleanValue());
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_DURATION_ID, myShowDuration.get().booleanValue());

        PlacemarkUtils.setPlacemarkFont(placemark, myTextStyleModel.getSelectedFont());

        PlacemarkUtils.setPlacemarkTextColor(placemark, myTextStyleModel.getFontColor().get());
        PlacemarkUtils.setPlacemarkColor(placemark, myBorderColor.get());
        PlacemarkUtils.setPolygonFillColor(placemark, myPolygonFillColor.get());

        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_BUBBLE_FILLED_ID, myIsBubbleFilled.get().booleanValue());
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_POLYGON_FILLED_ID, myIsPolygonFilled.get().booleanValue());
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_ANIMATE, myAnimate.get().booleanValue());
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_SHOW_IN_TIMELINE, myShowInTimeline.get().booleanValue());
    }

    @Override
    protected void updateViewModel(Placemark placemark)
    {
        if (placemark != null && placemark.getExtendedData() != null)
        {
            ExtendedData extendedData = placemark.getExtendedData();
            myShowTitle.set(ExtendedDataUtils.getObjBoolean(extendedData, Constants.IS_TITLE, false));
            myShowFieldTitles.set(ExtendedDataUtils.getObjBoolean(extendedData, Constants.IS_FIELD_TITLE, false));
            myShowDecimalLatLon.set(ExtendedDataUtils.getObjBoolean(extendedData, Constants.IS_LAT_LON_ID, false));
            myShowDMSLatLon.set(ExtendedDataUtils.getObjBoolean(extendedData, Constants.IS_DMS_ID, false));
            myShowMGRSLatLon.set(ExtendedDataUtils.getObjBoolean(extendedData, Constants.IS_MGRS, false));
            myShowAltitude.set(ExtendedDataUtils.getObjBoolean(extendedData, Constants.IS_SHOW_ALTITUDE, false));
            myShowDescription.set(ExtendedDataUtils.getObjBoolean(extendedData, Constants.IS_DESC_ID, false));
            myShowDistance.set(ExtendedDataUtils.getObjBoolean(extendedData, Constants.IS_DISTANCE_ID, true));
            myShowHeading.set(ExtendedDataUtils.getObjBoolean(extendedData, Constants.IS_HEADING_ID, true));
            myShowVelocity.set(ExtendedDataUtils.getObjBoolean(extendedData, Constants.IS_VELOCITY_ID, true));
            myShowDuration.set(ExtendedDataUtils.getObjBoolean(extendedData, Constants.IS_DURATION_ID, true));
            myIsDistanceHeadingCapable = ExtendedDataUtils.getBoolean(extendedData, Constants.IS_HEADING_DISTANCE_CAPABLE, false);
            myIsLocationCapable = ExtendedDataUtils.getBoolean(extendedData, Constants.IS_LOCATION_CAPABLE, true);
            myVelocityCapable = ExtendedDataUtils.getBoolean(extendedData, Constants.IS_VELOCITY_CAPABLE, false);
            myDurationCapable = ExtendedDataUtils.getBoolean(extendedData, Constants.IS_DURATION_CAPABLE, false);
            myShowInTimeline.set(ExtendedDataUtils.getObjBoolean(extendedData, Constants.IS_SHOW_IN_TIMELINE, true));
            myAnimate.set(ExtendedDataUtils.getObjBoolean(extendedData, Constants.IS_ANIMATE, true));
        }
        else
        {
            myShowTitle.set(Boolean.FALSE);
            myShowFieldTitles.set(Boolean.FALSE);
            myShowDecimalLatLon.set(Boolean.FALSE);
            myShowDMSLatLon.set(Boolean.FALSE);
            myShowMGRSLatLon.set(Boolean.FALSE);
            myShowAltitude.set(Boolean.FALSE);
            myShowDescription.set(Boolean.FALSE);
            myShowDistance.set(Boolean.FALSE);
            myShowHeading.set(Boolean.FALSE);
            myShowVelocity.set(Boolean.FALSE);
            myShowDuration.set(Boolean.FALSE);
            myIsDistanceHeadingCapable = false;
            myIsLocationCapable = true;
            myVelocityCapable = false;
            myDurationCapable = false;
            myShowInTimeline.set(Boolean.TRUE);
            myShowInTimeline.set(Boolean.TRUE);
        }

        Font font = PlacemarkUtils.getPlacemarkFont(placemark);

        myTextStyleModel.getFont().set(new FontWrapper(font));
        myTextStyleModel.getBold().set(Boolean.valueOf(font.isBold()));
        myTextStyleModel.getItalic().set(Boolean.valueOf(font.isItalic()));
        myTextStyleModel.getFontSize().set(Integer.valueOf(font.getSize()));

        if (placemark != null && placemark.getExtendedData() != null)
        {
            ExtendedData extendedData = placemark.getExtendedData();
            myTextStyleModel.getFontColor().set(PlacemarkUtils.getPlacemarkTextColor(placemark));
            myBorderColor.set(PlacemarkUtils.getPlacemarkColor(placemark));
            myPolygonFillColor.set(PlacemarkUtils.getPolygonFillColor(placemark));
            myIsBubbleFilled.set(ExtendedDataUtils.getObjBoolean(extendedData, Constants.IS_BUBBLE_FILLED_ID, true));
            myIsPolygonFilled.set(ExtendedDataUtils.getObjBoolean(extendedData, Constants.IS_POLYGON_FILLED_ID, false));
        }
        else
        {
            myTextStyleModel.getFontColor().set(Color.WHITE);
            myBorderColor.set(Color.WHITE);
            myPolygonFillColor.set(Color.WHITE);
            myIsBubbleFilled.set(Boolean.FALSE);
            myIsPolygonFilled.set(Boolean.FALSE);
        }

        setChanged(false);
    }

    /**
     * Bubble check boxes valid.
     *
     * @return whether they are valid
     */
    private boolean bubbleCheckBoxesValid()
    {
        int count = 0;
        count += countIfTrue(getShowTitle().get());
        count += countIfTrue(getShowDecimalLatLon().get());
        count += countIfTrue(getShowDMSLatLon().get());
        count += countIfTrue(getShowMGRSLatLon().get());
        count += countIfTrue(getShowAltitude().get());
        count += countIfTrue(getShowDescription().get());
        count += countIfTrue(getShowDistance().get());
        count += countIfTrue(getShowHeading().get());
        count += countIfTrue(getShowVelocity().get());
        count += countIfTrue(getShowDuration().get());
        return count > 0;
    }

    /**
     * Saves only the changed inputs to the given point's annotation settings.
     *
     * @param placemark The placemark
     */
    private void saveChangedAnnoSettings(Placemark placemark)
    {
        ExtendedData extendedData = placemark.getExtendedData();
        saveProperty(extendedData, Constants.IS_TITLE, myShowTitle);
        saveProperty(extendedData, Constants.IS_FIELD_TITLE, myShowFieldTitles);
        saveProperty(extendedData, Constants.IS_LAT_LON_ID, myShowDecimalLatLon);
        saveProperty(extendedData, Constants.IS_DMS_ID, myShowDMSLatLon);
        saveProperty(extendedData, Constants.IS_MGRS, myShowMGRSLatLon);
        saveProperty(extendedData, Constants.IS_SHOW_ALTITUDE, myShowAltitude);
        saveProperty(extendedData, Constants.IS_DESC_ID, myShowDescription);
        saveProperty(extendedData, Constants.IS_DISTANCE_ID, myShowDistance);
        saveProperty(extendedData, Constants.IS_HEADING_ID, myShowHeading);
        saveProperty(extendedData, Constants.IS_VELOCITY_ID, myShowVelocity);
        saveProperty(extendedData, Constants.IS_DURATION_ID, myShowDuration);
        saveProperty(extendedData, Constants.IS_ANIMATE, myAnimate);
        saveProperty(extendedData, Constants.IS_SHOW_IN_TIMELINE, myShowInTimeline);
    }

    /**
     * Saves only the changed inputs to the given point's non-annotation
     * settings.
     *
     * @param placemark The placemark
     */
    private void saveChangedPointSettings(Placemark placemark)
    {
        if (myTextStyleModel.getFont().isChanged())
        {
            Font existingFont = PlacemarkUtils.getPlacemarkFont(placemark);
            Font newFont = myTextStyleModel.getFont().get().getFont().deriveFont(existingFont.getStyle(), existingFont.getSize());
            PlacemarkUtils.setPlacemarkFont(placemark, newFont);
        }
        if (myTextStyleModel.getFontSize().isChanged())
        {
            Font existingFont = PlacemarkUtils.getPlacemarkFont(placemark);
            Font newFont = existingFont.deriveFont(myTextStyleModel.getFontSize().get().floatValue());
            PlacemarkUtils.setPlacemarkFont(placemark, newFont);
        }
        if (myTextStyleModel.getBold().isChanged())
        {
            Font existingFont = PlacemarkUtils.getPlacemarkFont(placemark);
            Font newFont = changeStyle(existingFont, Font.BOLD, myTextStyleModel.getBold().get().booleanValue());
            PlacemarkUtils.setPlacemarkFont(placemark, newFont);
        }
        if (myTextStyleModel.getItalic().isChanged())
        {
            Font existingFont = PlacemarkUtils.getPlacemarkFont(placemark);
            Font newFont = changeStyle(existingFont, Font.ITALIC, myTextStyleModel.getItalic().get().booleanValue());
            PlacemarkUtils.setPlacemarkFont(placemark, newFont);
        }
        if (myTextStyleModel.getFontColor().isChanged())
        {
            PlacemarkUtils.setPlacemarkTextColor(placemark, myTextStyleModel.getFontColor().get());
        }
        if (myBorderColor.isChanged())
        {
            PlacemarkUtils.setPlacemarkColor(placemark, myBorderColor.get());
        }
        if (myPolygonFillColor.isChanged())
        {
            PlacemarkUtils.setPolygonFillColor(placemark, myPolygonFillColor.get());
        }
        saveProperty(placemark.getExtendedData(), Constants.IS_BUBBLE_FILLED_ID, myIsBubbleFilled);
        saveProperty(placemark.getExtendedData(), Constants.IS_POLYGON_FILLED_ID, myIsPolygonFilled);
    }

    /**
     * Saves the property to the extended data.
     *
     * @param extendedData the extended data
     * @param propertyName the property name
     * @param property the property
     */
    private static void saveProperty(ExtendedData extendedData, String propertyName, BooleanModel property)
    {
        if (property.isChanged())
        {
            ExtendedDataUtils.putBoolean(extendedData, propertyName, property.get().booleanValue());
        }
    }
}
