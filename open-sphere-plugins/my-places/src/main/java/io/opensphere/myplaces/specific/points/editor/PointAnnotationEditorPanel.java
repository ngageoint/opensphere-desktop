package io.opensphere.myplaces.specific.points.editor;

import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.micromata.opengis.kml.v_2_2_0.AltitudeMode;
import de.micromata.opengis.kml.v_2_2_0.ExtendedData;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
import io.opensphere.core.mgrs.MGRSCalcUtils;
import io.opensphere.core.mgrs.MGRSConverter;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.GeographicPositionFormat;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.LatLonAlt.CoordFormat;
import io.opensphere.core.model.LatLonAltParser;
import io.opensphere.core.util.TerrainUtil;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.myplaces.constants.Constants;
import io.opensphere.myplaces.editor.controller.AnnotationEditController;
import io.opensphere.myplaces.editor.controller.AnnotationsHelper;
import io.opensphere.myplaces.editor.view.AnnotationEditorPanel;
import io.opensphere.myplaces.util.ExtendedDataUtils;

/** An editor that edits points and their locations. */
@SuppressWarnings("PMD.GodClass")
public class PointAnnotationEditorPanel extends AnnotationEditorPanel
{
    /** The serial version id. */
    private static final long serialVersionUID = 1L;

    /** Maximum accepted value for altitude. */
    private static final int MAX_ALT = 500_000_000;

    /** The location editor as a GridBagPanel. */
    private GridBagPanel editLocPanel;

    /** The location GridBagPanel. */
    private GridBagPanel locationPanel;

    /** The decimal GridBagPanel. */
    private GridBagPanel decimalPanel;

    /** The mgrs GridBagPanel. */
    private GridBagPanel mgrsPanel;

    /** editor for degrees and decimal minutes as a GribBagPanel. */
    private GridBagPanel degDminPanel;

    /** The DMS panel. */
    private GridBagPanel dmsPanel;

    /** The panel into which altitude is entered. */
    private GridBagPanel altPanel;

    /** The edit loc combo. */
    private JComboBox<GeographicPositionFormat> editLocCombo;

    /** The show hide check box. */
    private JCheckBox showHideCheckBox;

    /**
     * Constructs a new point editor.
     *
     * @param pointPanelController The controller of the editor.
     */
    public PointAnnotationEditorPanel(AnnotationEditController pointPanelController)
    {
        super(pointPanelController);
    }

    /**
     * Extract position.
     *
     * @param pt the pt
     */
    @Override
    protected void extractPosition(Placemark pt)
    {
        GeographicPositionFormat type = (GeographicPositionFormat)getEditLocCombo().getSelectedItem();
        if (type == GeographicPositionFormat.DECDEG)
        {
            addLlaPoint(pt, decLatField.getText(), decLonField.getText(), CoordFormat.DECIMAL);
        }
        else if (type == GeographicPositionFormat.DMSDEG)
        {
            addLlaPoint(pt, dmsLatField.getText(), dmsLonField.getText(), CoordFormat.DMS);
        }
        else if (type == GeographicPositionFormat.DEG_DMIN)
        {
            addLlaPoint(pt, ddmLatField.getText(), ddmLonField.getText(), CoordFormat.DDM);
        }
        else if (type == GeographicPositionFormat.MGRS)
        {
            addMgrsPoint(pt, mgrsField.getText());
        }
    }

    /**
     * Use the specified lat/lon String to update the Placemark and associated
     * data structures.
     * @param pt the Placemark
     * @param latStr the latitude formatted as a String
     * @param lonStr the longitude formatted as a String
     * @param fmt the format used for lat/lon Strings
     */
    private void addLlaPoint(Placemark pt, String latStr, String lonStr, CoordFormat fmt)
    {
        Point point = pt.createAndSetPoint();
        ExtendedData extendedData = pt.getExtendedData();
        boolean useAltitude = getEnableAltitudeField().isSelected();
        Altitude.ReferenceLevel altRef =
                useAltitude ? Altitude.ReferenceLevel.ELLIPSOID : Altitude.ReferenceLevel.TERRAIN;

        double lat = LatLonAltParser.parseLat(latStr, fmt);
        double lon = LatLonAltParser.parseLon(lonStr, fmt);
        double alt = useAltitude ? getAltitude() : 0.0;
        LatLonAlt location = LatLonAlt.createFromDegreesMeters(lat, lon, alt, altRef);
        addCoordinate(point, location, useAltitude);
        GeographicPosition gp = new GeographicPosition(location);
        ExtendedDataUtils.putString(extendedData, Constants.MGRS_ID,
                getPointPanelController().getAnnotationsHelper().findMGRS(gp));

        // also include the decimal precision for decimal data fields
        if (fmt == CoordFormat.DECIMAL)
        {
            int precision = Integer.parseInt(getDecimalDegreesPrecisionSpinner().getValue().toString());
            ExtendedDataUtils.putString(extendedData, Constants.DECIMAL_PRECISION, String.valueOf(precision));
        }
    }

    /**
     * Use the specified MGRS String to update the Placemark and associated
     * data structures.
     * @param pt the Placemark
     * @param mgrs the MGRS String
     */
    private void addMgrsPoint(Placemark pt, String mgrs)
    {
        GeographicPosition pos = new MGRSConverter().convertToLatLon(mgrs);
        if (pos == null)
        {
            return;
        }

        int precision = Integer.parseInt(getMGRSPrecisionComboBox().getSelectedItem().toString());
        String mgrsString = MGRSCalcUtils.reducePrecision(mgrs, precision);

        Point point = pt.createAndSetPoint();
        ExtendedData extendedData = pt.getExtendedData();
        ExtendedDataUtils.putString(extendedData, Constants.MGRS_ID, mgrsString);
        ExtendedDataUtils.putString(extendedData, Constants.MGRS_PRECISION, String.valueOf(precision / 2));

        if (getEnableAltitudeField().isSelected())
        {
            double altitude = getAltitude();
            pos = new GeographicPosition(LatLonAlt.createFromDegreesMeters(pos.getLatLonAlt().getLatD(),
                    pos.getLatLonAlt().getLonD(), altitude, Altitude.ReferenceLevel.ELLIPSOID));
            LatLonAlt lla = pos.getLatLonAlt();
            point.addToCoordinates(lla.getLonD(), lla.getLatD(), altitude);
        }
        else
        {
            LatLonAlt lla = pos.getLatLonAlt();
            point.addToCoordinates(lla.getLonD(), lla.getLatD());
        }
    }

    /**
     * Adds a coordinate to the point.
     *
     * @param point the point
     * @param location the location to use
     * @param useAltitude whether to include altitude
     */
    private static void addCoordinate(Point point, LatLonAlt location, boolean useAltitude)
    {
        if (useAltitude)
        {
            point.addToCoordinates(location.getLonD(), location.getLatD(), location.getAltM());
            point.setAltitudeMode(AltitudeMode.ABSOLUTE);
        }
        else
        {
            point.addToCoordinates(location.getLonD(), location.getLatD());
            point.setAltitudeMode(AltitudeMode.CLAMP_TO_GROUND);
        }
    }

    /**
     * Gets the altitude value.
     *
     * @return the altitude
     */
    private double getAltitude()
    {
        String altitudeString = StringUtilities.trim(getAltitudeField().getText());
        return Double.parseDouble(altitudeString);
    }

    /**
     * Gets the edits the loc panel.
     *
     * @return the edits the loc panel
     */
    @Override
    protected GridBagPanel getEditLocPanel()
    {
        if (editLocPanel == null)
        {
            editLocPanel = new GridBagPanel();
            editLocPanel.setBorder(BorderFactory.createTitledBorder("Location Type"));
            GridBagPanel editPanel = new GridBagPanel();
            editPanel.setGridx(0).setGridy(0).setInsets(0, 30, 3, 0).anchorEast().fillNone().add(new JLabel("Edit:"));
            editPanel.setGridx(1).setInsets(0, 5, 3, 5).fillHorizontal().add(getEditLocCombo());
            editLocPanel.setGridx(0).setGridy(0).anchorWest().fillHorizontal().add(editPanel);
            editLocPanel.setGridy(1).setInsets(0, 0, 0, 5).add(getLocationPanel());
        }
        return editLocPanel;
    }

    /**
     * Gets the show hide point checkbox.
     *
     * @return the show hide point checkbox
     */
    @Override
    protected JCheckBox getShowHideFeatureCheckbox()
    {
        if (showHideCheckBox == null)
        {
            showHideCheckBox = new JCheckBox("Show Point");
            showHideCheckBox.setBorder(null);
        }
        return showHideCheckBox;
    }

    /**
     * Positions valid.
     *
     * @return the string
     */
    @Override
    protected String positionsValid()
    {
        GeographicPositionFormat type = (GeographicPositionFormat)editLocCombo.getSelectedItem();
        if (type == GeographicPositionFormat.DECDEG)
        {
            if (!LatLonAlt.isValidDecimalLat(decLatField.getText(), 'N', 'n', 'S', 's'))
            {
                return "Latitude is not valid.";
            }
            if (!LatLonAlt.isValidDecimalLon(decLonField.getText(), 'E', 'e', 'W', 's'))
            {
                return "Longitude is not valid.";
            }
        }
        else if (type == GeographicPositionFormat.DMSDEG)
        {
            if (Double.isNaN(LatLonAltParser.parseLat(dmsLatField.getText(), CoordFormat.DMS)))
            {
                return "DMS Latitude is not valid.";
            }
            if (Double.isNaN(LatLonAltParser.parseLon(dmsLonField.getText(), CoordFormat.DMS)))
            {
                return "DMS Longitude is not valid.";
            }
        }
        else if (type == GeographicPositionFormat.DEG_DMIN)
        {
            if (Double.isNaN(LatLonAltParser.parseLat(ddmLatField.getText(), CoordFormat.DDM)))
            {
                return "DD MM.MM Latitude is not valid.";
            }
            if (Double.isNaN(LatLonAltParser.parseLon(ddmLonField.getText(), CoordFormat.DDM)))
            {
                return "DDD MM.MM Longitude is not valid.";
            }
        }
        else if (new MGRSConverter().convertToLatLon(mgrsField.getText()) == null)
        {
            return "MGRS value is not valid.";
        }

        if (getEnableAltitudeField().isSelected())
        {
            try
            {
                if (getAltitude() > MAX_ALT)
                {
                    return "Altitude may not exceed " + NumberFormat.getInstance().format(MAX_ALT) + " meters.";
                }
            }
            catch (NumberFormatException e)
            {
                return "Altitude is not a number.";
            }
        }

        return null;
    }

    /**
     * Sets the annotation point params.
     *
     * @param mapPoint the map point to retrieve settings from
     */
    @Override
    protected void setAnnotationPointParams(Placemark mapPoint)
    {
        super.setAnnotationPointParams(mapPoint);

        double lat = 0d;
        double lon = 0d;
        double altitude = 0d;
        Point point = (Point)mapPoint.getGeometry();
        if (point != null && !point.getCoordinates().isEmpty())
        {
            lat = point.getCoordinates().get(0).getLatitude();
            lon = point.getCoordinates().get(0).getLongitude();
            altitude = point.getCoordinates().get(0).getAltitude();
        }

        AnnotationEditController aec = getPointPanelController();
        AnnotationsHelper annoHelp = aec.getAnnotationsHelper();

        decLatField.setText(annoHelp.formatDecimal(lat));
        decLonField.setText(annoHelp.formatDecimal(lon));
        dmsLatField.setText(LatLonAlt.latToDMSString(lat, 3));
        dmsLonField.setText(LatLonAlt.lonToDMSString(lon, 3));
        ddmLatField.setText(LatLonAlt.latToDdmString(lat, 3));
        ddmLonField.setText(LatLonAlt.lonToDdmString(lon, 3));

        LatLonAlt lla = LatLonAlt.createFromDegrees(lat, lon, ReferenceLevel.ELLIPSOID);
        GeographicPosition gp = new GeographicPosition(lla);
        String mgrsString = annoHelp.findMGRS(gp);
        mgrsField.setText(mgrsString);
        int mgrsPrecisionIndex = 0;
        String mgrsPrecision = ExtendedDataUtils.getString(mapPoint.getExtendedData(), Constants.MGRS_PRECISION);
        if (mgrsPrecision != null)
        {
            mgrsPrecisionIndex = 5 - Integer.parseInt(mgrsPrecision);
        }
        getMGRSPrecisionComboBox().setSelectedIndex(mgrsPrecisionIndex);
        String decimalPrecision = ExtendedDataUtils.getString(mapPoint.getExtendedData(), Constants.DECIMAL_PRECISION);
        if (decimalPrecision != null)
        {
            getDecimalDegreesPrecisionSpinner().setValue(Integer.valueOf(decimalPrecision));
        }

        if (Double.isNaN(altitude))
        {
            altitude = TerrainUtil.getInstance().getElevationInMeters(aec.getToolbox().getMapManager(), gp);
        }
        getAltitudeField().setText(annoHelp.formatDecimal(altitude));
    }

    /**
     * Gets the decimal panel.
     *
     * @return the decimal panel
     */
    private JPanel getDecimalPanel()
    {
        if (decimalPanel == null)
        {
            decimalPanel = new GridBagPanel();
            decimalPanel.setGridx(0).setInsets(0, 35, 0, 0).fillNone().add(new JLabel("Lat:"));
            decimalPanel.setGridx(1).setInsets(0, 0, 0, 0).fillHorizontal().setGridwidth(3).add(decLatField);
            decimalPanel.setGridx(0).setInsets(0, 35, 0, 0).setGridy(1).fillNone().setGridwidth(1).add(new JLabel("Lon:"));
            decimalPanel.setGridx(1).setInsets(0, 0, 0, 0).fillHorizontal().setGridwidth(3).add(decLonField);
            decimalPanel.setGridy(2).setGridwidth(1).anchorWest().fillNone().setGridwidth(1)
                    .add(getDecimalDegreesPrecisionSpinner());
            decimalPanel.setGridx(2).anchorWest().setInsets(0, 5, 0, 0).fillNone().add(new JLabel("Display Precision"));
            decimalPanel.setGridx(3).fillHorizontalSpace();
        }
        return decimalPanel;
    }

    /**
     * Lazily instantiates the panel into which the user may enter altitude
     * information.
     *
     * @return the panel into which the user can enter altitude information.
     */
    private JPanel getAltitudePanel()
    {
        if (altPanel == null)
        {
            altPanel = new GridBagPanel();
            altPanel.setGridx(0).setInsets(0, 0, 0, 0).fillNone().add(new JLabel("Has Altitude:"));
            altPanel.setGridx(1).setInsets(0, 0, 0, 0).fillHorizontal().add(getEnableAltitudeField());
            altPanel.setGridx(0).setInsets(0, 35, 0, 0).fillNone().add(new JLabel("Altitude (Meters):"));
            altPanel.setGridx(1).setInsets(0, 0, 0, 0).fillHorizontal().add(getAltitudeField());
        }
        return altPanel;
    }

    /**
     * Gets the degrees/minutes/seconds panel.
     *
     * @return the dMS panel
     */
    private GridBagPanel getDMSPanel()
    {
        if (dmsPanel == null)
        {
            dmsPanel = new GridBagPanel();
            dmsPanel.setGridx(0).setInsets(0, 35, 0, 0).fillNone().add(new JLabel("Lat:"));
            dmsPanel.setGridx(1).setInsets(0, 0, 0, 0).fillHorizontal().add(dmsLatField);
            dmsPanel.setGridx(0).setInsets(0, 35, 0, 0).setGridy(1).fillNone().add(new JLabel("Lon:"));
            dmsPanel.setGridx(1).setInsets(0, 0, 0, 0).fillHorizontal().add(dmsLonField);
        }
        return dmsPanel;
    }

    /**
     * Get the panel for editing location in degrees plus decimal minutes,
     * constructing it if necessary.
     * @return see above
     */
    private JPanel getDegDminPanel()
    {
        if (degDminPanel == null)
        {
            degDminPanel = new GridBagPanel();
            degDminPanel.setGridx(0).setInsets(0, 35, 0, 0).fillNone().add(new JLabel("Lat:"));
            degDminPanel.setGridx(1).setInsets(0, 0, 0, 0).fillHorizontal().add(ddmLatField);
            degDminPanel.setGridx(0).setInsets(0, 35, 0, 0).setGridy(1).fillNone().add(new JLabel("Lon:"));
            degDminPanel.setGridx(1).setInsets(0, 0, 0, 0).fillHorizontal().add(ddmLonField);
        }
        return degDminPanel;
    }

    /**
     * Gets the edits the loc combo.
     *
     * @return the edits the loc combo
     */
    private JComboBox<GeographicPositionFormat> getEditLocCombo()
    {
        if (editLocCombo == null)
        {
            editLocCombo = new JComboBox<>();
            editLocCombo.addItem(GeographicPositionFormat.DECDEG);
            editLocCombo.addItem(GeographicPositionFormat.DMSDEG);
            editLocCombo.addItem(GeographicPositionFormat.DEG_DMIN);
            editLocCombo.addItem(GeographicPositionFormat.MGRS);
            editLocCombo.addActionListener(e -> switchLocationPanel(
                    (GeographicPositionFormat)editLocCombo.getSelectedItem()));
            editLocCombo.setSelectedIndex(1);
        }
        return editLocCombo;
    }

    /**
     * Gets the location panel.
     *
     * @return the location panel
     */
    private GridBagPanel getLocationPanel()
    {
        if (locationPanel == null)
        {
            locationPanel = new GridBagPanel();
        }
        return locationPanel;
    }

    /**
     * Gets the mGRS panel.
     *
     * @return the mGRS panel
     */
    private JPanel getMGRSPanel()
    {
        if (mgrsPanel == null)
        {
            mgrsPanel = new GridBagPanel();
            mgrsPanel.setGridx(0).setInsets(0, 15, 0, 5).anchorEast().fillNone().add(new JLabel("MGRS:"));
            mgrsPanel.setGridx(1).setGridwidth(3).setInsets(0, 0, 0, 0).fillHorizontal().add(mgrsField);
            mgrsPanel.setGridy(1).setGridwidth(1).anchorWest().fillNone().add(getMGRSPrecisionComboBox());
            mgrsPanel.setGridx(2).anchorWest().setInsets(0, 5, 0, 0).fillNone().add(new JLabel("Display Precision"));
            mgrsPanel.setGridx(3).fillHorizontalSpace();
        }
        return mgrsPanel;
    }

    /**
     * Switch location panel.
     *
     * @param type the type
     */
    private void switchLocationPanel(GeographicPositionFormat type)
    {
        getLocationPanel().removeAll();
        if (type == GeographicPositionFormat.DMSDEG)
        {
            getLocationPanel().setGridx(0).setGridy(0).fillHorizontal().add(getDMSPanel());
        }
        else if (type == GeographicPositionFormat.DEG_DMIN)
        {
            getLocationPanel().setGridx(0).setGridy(0).fillHorizontal().add(getDegDminPanel());
        }
        else if (type == GeographicPositionFormat.DECDEG)
        {
            getLocationPanel().setGridx(0).setGridy(0).fillHorizontal().add(getDecimalPanel());
        }
        else if (type == GeographicPositionFormat.MGRS)
        {
            getLocationPanel().setGridx(0).setGridy(0).fillHorizontal().add(getMGRSPanel());
        }

        getLocationPanel().setGridx(0).setGridy(1).fillHorizontal().add(getAltitudePanel());

        getLocationPanel().revalidate();
        getLocationPanel().repaint();
    }
}
