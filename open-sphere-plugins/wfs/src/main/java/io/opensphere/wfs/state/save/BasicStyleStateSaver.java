package io.opensphere.wfs.state.save;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.enums.EnumUtilities;
import io.opensphere.mantle.data.geom.style.StyleAltitudeReference;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameter;
import io.opensphere.mantle.data.geom.style.impl.AbstractFeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.PointFeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.StyleUtils;
import io.opensphere.wfs.state.model.BasicFeatureStyle;

/**
 * Saves the state of basic WFS style parameters.
 */
public class BasicStyleStateSaver extends StyleStateSaver
{
    /** The Basic feature style. */
    private final BasicFeatureStyle myBasicFeatureStyle;

    {
        getStyleKeys().add(AbstractFeatureVisualizationStyle.ALTITUDE_METADATA_COLUMN_KEY_PROPERTY_KEY);
        getStyleKeys().add(AbstractFeatureVisualizationStyle.COLOR_PROPERTY_KEY);
        getStyleKeys().add(PointFeatureVisualizationStyle.ourPointSizePropertyKey);
        getStyleKeys().add(AbstractFeatureVisualizationStyle.LIFT_PROPERTY_KEY);
        getStyleKeys().add(AbstractFeatureVisualizationStyle.ALTITUDE_REFERENCE_PROPERTY_KEY);
        getStyleKeys().add(AbstractFeatureVisualizationStyle.USE_ALTITUDE_PROPERTY_KEY);
        getStyleKeys().add(AbstractFeatureVisualizationStyle.ALTITUDE_UNIT_PROPERTY_KEY);
        getStyleKeys().add(AbstractFeatureVisualizationStyle.LABEL_COLUMN_KEY_PROPERTY_KEY);
        getStyleKeys().add(AbstractFeatureVisualizationStyle.LABEL_SIZE_PROPERTY_KEY);
        getStyleKeys().add(AbstractFeatureVisualizationStyle.LABEL_COLOR_PROPERTY_KEY);
        getStyleKeys().add(AbstractFeatureVisualizationStyle.LABEL_ENABLED_PROPERTY_KEY);
    }

    /**
     * Constructor.
     */
    public BasicStyleStateSaver()
    {
        myBasicFeatureStyle = new BasicFeatureStyle();
    }

    /**
     * Constructor.
     *
     * @param style A saved state style for use by this saver. In general, it is
     *            assumed that the values are pre-populated with known or saved
     *            values.
     */
    public BasicStyleStateSaver(BasicFeatureStyle style)
    {
        myBasicFeatureStyle = style;
    }

    /**
     * Gets the basic feature style.
     *
     * @return the basic feature style
     */
    public BasicFeatureStyle getBasicFeatureStyle()
    {
        return myBasicFeatureStyle;
    }

    @Override
    public Set<VisualizationStyleParameter> populateVisualizationStyle(VisualizationStyle visStyle)
    {
        Set<VisualizationStyleParameter> vspSet = New.set();
        for (String key : getStyleKeys())
        {
            addNonNull(vspSet, genParam(visStyle.getStyleParameter(key)));
        }
        addNonNull(vspSet,
                genLabelCol(visStyle.getStyleParameter(AbstractFeatureVisualizationStyle.LABEL_COLUMN_KEY_PROPERTY_KEY)));
        return vspSet;
    }

    /**
     * Handles the case of label column(s).
     *
     * @param p the visualization style
     * @return a derived visualization style
     */
    private VisualizationStyleParameter genLabelCol(VisualizationStyleParameter p)
    {
        if (p == null)
        {
            return null;
        }
        List<BasicFeatureStyle.LblCfgRec> recs = myBasicFeatureStyle.getLabelRecs();
        // if no list is present (or it is empty), check for a single value
        if (recs == null || recs.isEmpty())
        {
            String col = myBasicFeatureStyle.getLabelColumn();
            if (col == null)
            {
                return null;
            }
            List<String> fields = new LinkedList<>();
            fields.add(Boolean.toString(false));
            fields.add(col);
            return p.deriveWithNewValue(singleton(combineLblFields(false, col)));
        }
        // process the list
        List<String> val = new LinkedList<>();
        recs.stream().forEach(r -> val.add(combineLblFields(r.showColumn, r.column)));
        return p.deriveWithNewValue(val);
    }

    /**
     * Combines all labels into a single string.
     *
     * @param show ???
     * @param col the column
     * @return the combined label
     */
    private static String combineLblFields(boolean show, String col)
    {
        List<String> fields = new LinkedList<>();
        fields.add(Boolean.toString(show));
        fields.add(col);
        return StyleUtils.listSupp.writeList(fields);
    }

    /**
     * Handles all of the normal cases.
     *
     * @param p the visualization style
     * @return the derived visualization style
     */
    private VisualizationStyleParameter genParam(VisualizationStyleParameter p)
    {
        if (p == null)
        {
            return null;
        }
        String key = p.getKey();
        if (key.equals(AbstractFeatureVisualizationStyle.ALTITUDE_METADATA_COLUMN_KEY_PROPERTY_KEY))
        {
            return p.deriveWithNewValue(myBasicFeatureStyle.getAltitudeColumn());
        }
        else if (key.equals(AbstractFeatureVisualizationStyle.COLOR_PROPERTY_KEY))
        {
            return p.deriveWithNewValue(WFSStateSaver.getColor(myBasicFeatureStyle.getPointColor()));
        }
        else if (key.equals(PointFeatureVisualizationStyle.ourPointSizePropertyKey))
        {
            return p.deriveWithNewValue(Float.valueOf(myBasicFeatureStyle.getPointSize()));
        }
        else if (key.equals(AbstractFeatureVisualizationStyle.LIFT_PROPERTY_KEY))
        {
            return p.deriveWithNewValue(Double.valueOf(myBasicFeatureStyle.getLift()));
        }
        else if (key.equals(AbstractFeatureVisualizationStyle.ALTITUDE_REFERENCE_PROPERTY_KEY))
        {
            return p.deriveWithNewValue(
                    EnumUtilities.fromString(StyleAltitudeReference.class, myBasicFeatureStyle.getAltitudeRef()));
        }
        else if (key.equals(AbstractFeatureVisualizationStyle.USE_ALTITUDE_PROPERTY_KEY))
        {
            return p.deriveWithNewValue(Boolean.valueOf(myBasicFeatureStyle.isUseAltitude()));
        }
        else if (key.equals(AbstractFeatureVisualizationStyle.ALTITUDE_UNIT_PROPERTY_KEY))
        {
            return p.deriveWithNewValue(myBasicFeatureStyle.getAltitudeColUnits());
        }
        else if (key.equals(AbstractFeatureVisualizationStyle.LABEL_SIZE_PROPERTY_KEY))
        {
            return p.deriveWithNewValue(Integer.valueOf(myBasicFeatureStyle.getLabelSize()));
        }
        else if (key.equals(AbstractFeatureVisualizationStyle.LABEL_COLOR_PROPERTY_KEY)
                && myBasicFeatureStyle.getLabelColor() != null)
        {
            return p.deriveWithNewValue(WFSStateSaver.getColor(myBasicFeatureStyle.getLabelColor()));
        }
        else if (key.equals(AbstractFeatureVisualizationStyle.LABEL_ENABLED_PROPERTY_KEY))
        {
            return p.deriveWithNewValue(Boolean.valueOf(myBasicFeatureStyle.isUseLabels()));
        }
        return null;
    }

    /**
     * Creates a singleton list.
     * <p>
     * Could stand to be replaced with
     * {@link java.util.Collections#singletonList(Object)}
     *
     * @param e the value to build a list around
     * @return a list consisting of a single value
     */
    private static <E> List<E> singleton(E e)
    {
        List<E> ret = new LinkedList<>();
        addNonNull(ret, e);
        return ret;
    }

    /**
     * Adds an item to a collection, as long as that item is not null.
     *
     * @param c the collection
     * @param e the item to add
     */
    private static <E> void addNonNull(Collection<E> c, E e)
    {
        if (e != null)
        {
            c.add(e);
        }
    }

    @Override
    public void saveStyleParams(VisualizationStyle visStyle)
    {
        // handle the majority of cases
        for (String key : getStyleKeys())
        {
            storeParam(key, visStyle.getStyleParameterValue(key));
        }
        // handle the case of label column(s)
        storeLabelCol(visStyle.getStyleParameterValue(AbstractFeatureVisualizationStyle.LABEL_COLUMN_KEY_PROPERTY_KEY));
    }

    /**
     * handles the case of label column(s)
     *
     * @param obj the list of labels to store
     */
    @SuppressWarnings("unchecked")
    private void storeLabelCol(Object obj)
    {
        // it should be a list of Strings; if not, punt!
        if (!(obj instanceof List))
        {
            return;
        }
        List<String> val = (List<String>)obj;
        // if empty, we are done
        if (val.isEmpty())
        {
            return;
        }

        myBasicFeatureStyle.clearLabelRecs();
        boolean first = true;
        for (String col : val)
        {
            List<String> fields = StyleUtils.listSupp.parseList(col);
            if (first)
            {
                myBasicFeatureStyle.setLabelColumn(fields.get(1));
            }
            first = false;
            myBasicFeatureStyle.addLabelRec(fields.get(0), fields.get(1));
        }
    }

    /**
     * handles all of the normal cases
     *
     * @param key the column to store the object in
     * @param val the object to store
     */
    private void storeParam(String key, Object val)
    {
        if (val == null)
        {
            return;
        }
        if (key.equals(AbstractFeatureVisualizationStyle.ALTITUDE_METADATA_COLUMN_KEY_PROPERTY_KEY))
        {
            myBasicFeatureStyle.setAltitudeColumn(val.toString());
        }
        else if (key.equals(AbstractFeatureVisualizationStyle.COLOR_PROPERTY_KEY))
        {
            myBasicFeatureStyle.setPointColor(WFSStateSaver.getColorString(val));
        }
        else if (key.equals(PointFeatureVisualizationStyle.ourPointSizePropertyKey))
        {
            myBasicFeatureStyle.setPointSize(Float.parseFloat(val.toString()));
        }
        else if (key.equals(AbstractFeatureVisualizationStyle.LIFT_PROPERTY_KEY))
        {
            myBasicFeatureStyle.setLift(Double.parseDouble(val.toString()));
        }
        else if (key.equals(AbstractFeatureVisualizationStyle.ALTITUDE_REFERENCE_PROPERTY_KEY))
        {
            myBasicFeatureStyle.setAltitudeRef(val.toString());
        }
        else if (key.equals(AbstractFeatureVisualizationStyle.USE_ALTITUDE_PROPERTY_KEY))
        {
            myBasicFeatureStyle.setUseAltitude(((Boolean)val).booleanValue());
        }
        else if (key.equals(AbstractFeatureVisualizationStyle.ALTITUDE_UNIT_PROPERTY_KEY))
        {
            myBasicFeatureStyle.setAltitudeColUnits(val.toString());
        }
        else if (key.equals(AbstractFeatureVisualizationStyle.LABEL_SIZE_PROPERTY_KEY))
        {
            myBasicFeatureStyle.setLabelSize(Integer.parseInt(val.toString()));
        }
        else if (key.equals(AbstractFeatureVisualizationStyle.LABEL_COLOR_PROPERTY_KEY))
        {
            myBasicFeatureStyle.setLabelColor(WFSStateSaver.getColorString(val));
        }
        else if (key.equals(AbstractFeatureVisualizationStyle.LABEL_ENABLED_PROPERTY_KEY))
        {
            myBasicFeatureStyle.setUseLabels(((Boolean)val).booleanValue());
        }
    }

    /**
     * Sets the altitude column for the basic style.
     *
     * @param altitudeKey the new altitude column
     */
    public void setAltitudeColumn(String altitudeKey)
    {
        myBasicFeatureStyle.setAltitudeColumn(altitudeKey);
    }

    /**
     * Sets the point color for the basic style.
     *
     * @param pointColor the new point color
     */
    public void setPointColor(String pointColor)
    {
        myBasicFeatureStyle.setPointColor(pointColor);
    }

    /**
     * Sets the point opacity for the basic style.
     *
     * @param typeOpacity the new point opacity
     */
    public void setPointOpacity(int typeOpacity)
    {
        myBasicFeatureStyle.setPointOpacity(typeOpacity);
    }
}
