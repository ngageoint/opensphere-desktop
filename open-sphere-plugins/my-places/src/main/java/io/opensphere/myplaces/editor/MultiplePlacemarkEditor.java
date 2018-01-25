package io.opensphere.myplaces.editor;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import de.micromata.opengis.kml.v_2_2_0.ExtendedData;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Polygon;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.OptionDialog;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.myplaces.constants.Constants;
import io.opensphere.myplaces.editor.view.AnnotationStyleEditorPanel;
import io.opensphere.myplaces.models.DataTypeInfoMyPlaceChangedEvent;
import io.opensphere.myplaces.models.MyPlacesDataTypeInfo;
import io.opensphere.myplaces.models.MyPlacesEditListener;
import io.opensphere.myplaces.util.ExtendedDataUtils;
import io.opensphere.myplaces.util.PlacemarkUtils;

/**
 * Edits multiple points.
 *
 */
public class MultiplePlacemarkEditor
{
    /**
     * The toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * Returns one of the objects if they are equal, otherwise returns null.
     *
     * @param <T> The type of the objects
     * @param o1 The first object
     * @param o2 The second object
     * @return One of the objects if they are equal, otherwise null
     */
    private static <T> T getIfEqual(T o1, T o2)
    {
        return o1 != null && !o1.equals(o2) ? null : o1;
    }

    /**
     * Constructs a new manual point creator.
     *
     * @param toolbox The toolbx.
     */
    public MultiplePlacemarkEditor(Toolbox toolbox)
    {
        myToolbox = toolbox;
    }

    /**
     * Allows the user to edit a single point.
     *
     * @param dataTypes the points to edit.
     * @param dataGroup The group containing the type.
     * @param editListener The edit listener.
     */
    public void editPlaces(List<MyPlacesDataTypeInfo> dataTypes, DataGroupInfo dataGroup, MyPlacesEditListener editListener)
    {
        Map<String, MyPlacesDataTypeInfo> pointIdToDataType = New.map();
        List<Placemark> placemarks = new ArrayList<>();
        for (MyPlacesDataTypeInfo dataType : dataTypes)
        {
            Placemark placemark = dataType.getKmlPlacemark();
            placemarks.add(placemark);
            pointIdToDataType.put(placemark.getId(), dataType);
        }

        Placemark commonPlacemark = getCommonPlacemark(placemarks);

        AnnotationStyleEditorPanel panel = new AnnotationStyleEditorPanel(myToolbox, true, true,
                commonPlacemark.getGeometry() instanceof Polygon);
        panel.setPlacemark(commonPlacemark);

        OptionDialog dialog = new OptionDialog(myToolbox.getUIRegistry().getMainFrameProvider().get(), panel,
                "Edit Selected My Places");
        dialog.buildAndShow();
        if (dialog.getSelection() == JOptionPane.OK_OPTION)
        {
            for (Placemark placemark : placemarks)
            {
                panel.getModel().saveChangedInputs(placemark);

                MyPlacesDataTypeInfo info = pointIdToDataType.get(placemark.getId());

                info.fireChangeEvent(new DataTypeInfoMyPlaceChangedEvent(info, this));
            }
        }
    }

    /**
     * Creates a placemark the represents common values for all placemarks.
     *
     * @param isTitle If title is checked.
     * @param isFieldTitle If Field Title is checked.
     * @param isLatLon If lat lon is displayed.
     * @param isDms If degrees minutes seconds is displayed.
     * @param isMgrs If mgrs is displayed.
     * @param isAltitude If altitude is displayed.
     * @param isDesc If description is displayed.
     * @param isFilled If bubble is filled.
     * @param font The font.
     * @return The common placemark.
     */
    private Placemark createCommonPlacemark(boolean isTitle, boolean isFieldTitle, boolean isLatLon, boolean isDms,
            boolean isMgrs, boolean isAltitude, boolean isDesc, boolean isFilled, Font font)
    {
        Placemark commonPlacemark = new Placemark();
        ExtendedData extendedData = commonPlacemark.createAndSetExtendedData();

        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_TITLE, isTitle);
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_FIELD_TITLE, isFieldTitle);
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_LAT_LON_ID, isLatLon);
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_DMS_ID, isDms);
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_MGRS, isMgrs);
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_SHOW_ALTITUDE, isAltitude);
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_DESC_ID, isDesc);
        ExtendedDataUtils.putBoolean(extendedData, Constants.IS_BUBBLE_FILLED_ID, isFilled);

        PlacemarkUtils.setPlacemarkFont(commonPlacemark, font);

        return commonPlacemark;
    }

    /**
     * Gets the common point for all the points.
     *
     * @param placemarks The placemarks to get a common placemark for.
     * @return The common placemark.
     */
    private Placemark getCommonPlacemark(List<Placemark> placemarks)
    {
        boolean isTitle = true;
        boolean isFieldTitle = true;
        boolean isLatLon = true;
        boolean isDms = true;
        boolean isMgrs = true;
        boolean isAltitude = true;
        boolean isDesc = true;
        boolean isFilled = true;
        boolean isHeading = true;
        boolean isVelocity = true;
        boolean isDuration = true;
        boolean isDistance = true;
        boolean isDistanceHeadingCapable = false;
        boolean isVelocityCapable = false;
        boolean isDurationCapable = false;
        boolean isLocationCapable = false;
        Placemark firstPlace = placemarks.get(0);
        Font firstFont = PlacemarkUtils.getPlacemarkFont(firstPlace);
        String fontName = firstFont.getName();
        int fontSize = firstFont.getSize();
        boolean isBold = firstFont.isBold();
        boolean isItalic = firstFont.isItalic();
        boolean isShowOnTimeline = true;
        boolean isAnimate = true;

        Color fontColor = PlacemarkUtils.getPlacemarkTextColor(firstPlace);
        Color color = PlacemarkUtils.getPlacemarkColor(firstPlace);
        for (Placemark placemark : placemarks)
        {
            ExtendedData extendedData = placemark.getExtendedData();

            isTitle &= ExtendedDataUtils.getBoolean(extendedData, Constants.IS_TITLE, false);
            isFieldTitle &= ExtendedDataUtils.getBoolean(extendedData, Constants.IS_FIELD_TITLE, false);
            isDesc &= ExtendedDataUtils.getBoolean(extendedData, Constants.IS_DESC_ID, false);
            isFilled &= ExtendedDataUtils.getBoolean(extendedData, Constants.IS_BUBBLE_FILLED_ID, false);

            boolean distanceHeading = ExtendedDataUtils.getBoolean(extendedData, Constants.IS_HEADING_DISTANCE_CAPABLE, false);
            if (distanceHeading)
            {
                isDistance &= ExtendedDataUtils.getBoolean(extendedData, Constants.IS_DISTANCE_ID, false);
                isHeading &= ExtendedDataUtils.getBoolean(extendedData, Constants.IS_HEADING_ID, false);
                isDistanceHeadingCapable = true;
            }

            boolean locationCapable = ExtendedDataUtils.getBoolean(extendedData, Constants.IS_LOCATION_CAPABLE, true);
            if (locationCapable)
            {
                isLocationCapable = true;
                isLatLon &= ExtendedDataUtils.getBoolean(extendedData, Constants.IS_LAT_LON_ID, false);
                isDms &= ExtendedDataUtils.getBoolean(extendedData, Constants.IS_DMS_ID, false);
                isMgrs &= ExtendedDataUtils.getBoolean(extendedData, Constants.IS_MGRS, false);
                isAltitude &= ExtendedDataUtils.getBoolean(extendedData, Constants.IS_SHOW_ALTITUDE, false);
            }

            boolean velocityCapable = ExtendedDataUtils.getBoolean(extendedData, Constants.IS_VELOCITY_CAPABLE, true);
            if (velocityCapable)
            {
                isVelocityCapable = true;
                isVelocity &= ExtendedDataUtils.getBoolean(extendedData, Constants.IS_VELOCITY_ID, false);
            }

            boolean durationCapable = ExtendedDataUtils.getBoolean(extendedData, Constants.IS_DURATION_CAPABLE, true);
            if (durationCapable)
            {
                isDurationCapable = true;
                isDuration &= ExtendedDataUtils.getBoolean(extendedData, Constants.IS_DURATION_ID, false);
            }

            Font placeFont = PlacemarkUtils.getPlacemarkFont(placemark);
            fontName = getIfEqual(fontName, placeFont.getName());

            if (fontSize != placeFont.getSize())
            {
                fontSize = PlacemarkUtils.DEFAULT_FONT.getSize();
            }

            isBold &= placeFont.isBold();
            isItalic &= placeFont.isItalic();
            fontColor = getIfEqual(fontColor, PlacemarkUtils.getPlacemarkTextColor(placemark));
            color = getIfEqual(color, PlacemarkUtils.getPlacemarkColor(placemark));

            isShowOnTimeline &= ExtendedDataUtils.getBoolean(extendedData, Constants.IS_SHOW_IN_TIMELINE, true);
            isAnimate &= ExtendedDataUtils.getBoolean(extendedData, Constants.IS_ANIMATE, true);
        }

        Font font = getFont(fontName, isBold, isItalic, fontSize);

        Placemark commonPlacemark = createCommonPlacemark(isTitle, isFieldTitle, isLatLon, isDms, isMgrs, isAltitude, isDesc,
                isFilled, font);
        modifyCommonPlacemark(commonPlacemark, isHeading, isDistance, isVelocity, isDuration, isLocationCapable,
                isDistanceHeadingCapable, isVelocityCapable, isDurationCapable, fontColor, color);
        ExtendedDataUtils.putBoolean(commonPlacemark.getExtendedData(), Constants.IS_ANIMATE, isAnimate);
        ExtendedDataUtils.putBoolean(commonPlacemark.getExtendedData(), Constants.IS_SHOW_IN_TIMELINE, isShowOnTimeline);

        return commonPlacemark;
    }

    /**
     * Gets the font for the common placemark.
     *
     * @param theFontName The name of the font.
     * @param isBold Indicates if it is bold.
     * @param isItalic Indicates if it is italic.
     * @param fontSize The font size.
     * @return The font.
     */
    private Font getFont(String theFontName, boolean isBold, boolean isItalic, int fontSize)
    {
        String fontName = theFontName;
        if (fontName == null)
        {
            fontName = PlacemarkUtils.DEFAULT_FONT.getName();
        }
        int style = 0;
        if (isBold)
        {
            style += Font.BOLD;
        }
        if (isItalic)
        {
            style += Font.ITALIC;
        }
        Font font = new Font(fontName, style, fontSize);

        return font;
    }

    /**
     * Modifies the common placemark.
     *
     * @param commonPlacemark The common placemark.
     * @param isHeading If heading is checked.
     * @param isDistance If distance is checked.
     * @param isVelocity If velocity is checked.
     * @param isDuration If duration is checked.
     * @param isLocationCapable Indicates if location capable.
     * @param isDistanceHeadingCapable Indicates if distance heading capable.
     * @param isVelocityCapable Indicates if velocity capable.
     * @param isDurationCapable Indicates if duration capable
     * @param fontColor The font color.
     * @param color The color.
     */
    private void modifyCommonPlacemark(Placemark commonPlacemark, boolean isHeading, boolean isDistance, boolean isVelocity,
            boolean isDuration, boolean isLocationCapable, boolean isDistanceHeadingCapable, boolean isVelocityCapable,
            boolean isDurationCapable, Color fontColor, Color color)
    {
        ExtendedDataUtils.putBoolean(commonPlacemark.getExtendedData(), Constants.IS_HEADING_ID, isHeading);
        ExtendedDataUtils.putBoolean(commonPlacemark.getExtendedData(), Constants.IS_DISTANCE_ID, isDistance);
        ExtendedDataUtils.putBoolean(commonPlacemark.getExtendedData(), Constants.IS_VELOCITY_ID, isVelocity);
        ExtendedDataUtils.putBoolean(commonPlacemark.getExtendedData(), Constants.IS_DURATION_ID, isVelocity);

        ExtendedDataUtils.putBoolean(commonPlacemark.getExtendedData(), Constants.IS_HEADING_DISTANCE_CAPABLE,
                isDistanceHeadingCapable);
        ExtendedDataUtils.putBoolean(commonPlacemark.getExtendedData(), Constants.IS_LOCATION_CAPABLE, isLocationCapable);
        ExtendedDataUtils.putBoolean(commonPlacemark.getExtendedData(), Constants.IS_VELOCITY_CAPABLE, isVelocityCapable);
        ExtendedDataUtils.putBoolean(commonPlacemark.getExtendedData(), Constants.IS_DURATION_CAPABLE, isDurationCapable);

        PlacemarkUtils.setPlacemarkTextColor(commonPlacemark, fontColor != null ? fontColor : Color.white);
        PlacemarkUtils.setPlacemarkColor(commonPlacemark, color != null ? color : Color.gray);
    }
}
