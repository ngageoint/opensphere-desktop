package io.opensphere.analysis.base.controller;

import java.text.ParseException;
import java.util.Date;
import java.util.function.Function;

import org.apache.log4j.Logger;

import io.opensphere.analysis.base.model.UIBin;
import io.opensphere.analysis.binning.algorithm.AbstractBinner;
import io.opensphere.analysis.binning.algorithm.Binner;
import io.opensphere.analysis.binning.algorithm.DataElementBinner;
import io.opensphere.analysis.binning.bins.Bin;
import io.opensphere.analysis.binning.criteria.BinCriteriaElement;
import io.opensphere.analysis.binning.criteria.CriteriaType;
import io.opensphere.analysis.binning.criteria.RangeCriteria;
import io.opensphere.analysis.binning.criteria.TimeCriteria;
import io.opensphere.analysis.binning.criteria.UniqueCriteria;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.DateTimeUtilities;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.impl.specialkey.TimeKey;

/** Data element binner for UIs. */
public class UIDataElementBinner extends DataElementBinner
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(UIDataElementBinner.class);

    /**
     * Constructor.
     *
     * @param mantleToolbox The mantle toolbox
     * @param criteriaElement The criteria element
     * @param layer The layer
     */
    public UIDataElementBinner(MantleToolbox mantleToolbox, BinCriteriaElement criteriaElement, DataTypeInfo layer)
    {
        super(mantleToolbox, criteriaElement, layer);
    }

    @Override
    protected Binner<DataElement> createInnerBinner(BinCriteriaElement criteriaElement)
    {
        AbstractBinner<DataElement> binner = null;
        CriteriaType criteria = criteriaElement.getCriteriaType();
        String field = criteriaElement.getField();
        if (criteria.getClass() == RangeCriteria.class)
        {
            Function<DataElement, Object> dataToValue = elem -> elem.getMetaData().getValue(field);
            binner = new UIRangeBinner((RangeCriteria)criteria, dataToValue, this::createUIBin);
        }
        else if (criteria.getClass() == UniqueCriteria.class)
        {
            Function<DataElement, Object> dataToValue = elem -> elem.getMetaData().getValue(field);
            binner = new UIUniqueValueBinner((UniqueCriteria)criteria, dataToValue, this::createUIBin);
        }
        else if (criteria.getClass() == TimeCriteria.class)
        {
            Function<DataElement, Date> dataToValue = elem ->
            {
                Object value = elem.getMetaData().getValue(field);
                if (elem.getDataTypeInfo().getMetaDataInfo().getSpecialTypeForKey(field) == TimeKey.DEFAULT)
                {
                    value = elem.getTimeSpan();
                }
                return parseDate(value);
            };
            binner = new UITimeBinner((TimeCriteria)criteria, dataToValue, this::createUIBin);
        }
        return binner;
    }

    /**
     * Creates a UI bin from a regular bin.
     *
     * @param bin the bin
     * @return the UI bin
     */
    private UIBin createUIBin(Bin<DataElement> bin)
    {
        if (bin == null)
        {
            return null;
        }

        UIBin uiBin = new UIBin(bin);
        uiBin.setColor(FXUtilities.fromAwtColor(getLayer().getBasicVisualizationInfo().getTypeColor()));
        return uiBin;
    }

    /**
     * Parses an object into a Date.
     *
     * @param o the object
     * @return the Date
     */
    private Date parseDate(Object o)
    {
        Date date = null;
        if (o instanceof Date)
        {
            date = (Date)o;
        }
        else if (o instanceof TimeSpan)
        {
            TimeSpan span = (TimeSpan)o;
            date = span.isUnboundedStart() ? null : span.getStartDate();
        }
        else if (o instanceof String)
        {
            try
            {
                date = DateTimeUtilities.parseISO8601Date((String)o);
            }
            catch (ParseException e)
            {
                LOGGER.error(e);
            }
        }
        else if (o != null)
        {
            LOGGER.error("Could not convert " + o + " to a Date.");
        }
        return date;
    }
}
