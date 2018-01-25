package io.opensphere.analysis.binning.algorithm;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import io.opensphere.analysis.binning.bins.Bin;
import io.opensphere.analysis.binning.criteria.BinCriteriaElement;
import io.opensphere.analysis.binning.criteria.CriteriaType;
import io.opensphere.analysis.binning.criteria.RangeCriteria;
import io.opensphere.analysis.binning.criteria.TimeCriteria;
import io.opensphere.analysis.binning.criteria.UniqueCriteria;
import io.opensphere.analysis.util.DataTypeUtilities;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.ListDataListener;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.util.DataElementLookupException;

/** Binner for {@link DataElement}s. */
public class DataElementBinner implements Binner<DataElement>
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(DataElementBinner.class);

    /** The mantle toolbox. */
    private final MantleToolbox myMantleToolbox;

    /** The criteria element. */
    private final BinCriteriaElement myCriteriaElement;

    /** The layer. */
    private final DataTypeInfo myLayer;

    /** The actual binner. */
    private final Binner<DataElement> myInnerBinner;

    /**
     * Constructor.
     *
     * @param mantleToolbox The mantle toolbox
     * @param criteriaElement The criteria element
     * @param layer The layer
     */
    public DataElementBinner(MantleToolbox mantleToolbox, BinCriteriaElement criteriaElement, DataTypeInfo layer)
    {
        myMantleToolbox = mantleToolbox;
        myCriteriaElement = criteriaElement;
        myLayer = layer;
        myInnerBinner = createInnerBinner(criteriaElement);
    }

    @Override
    public Bin<DataElement> add(DataElement data)
    {
        return myInnerBinner.add(data);
    }

    @Override
    public void addAll(Collection<? extends DataElement> dataItems)
    {
        myInnerBinner.addAll(dataItems);
    }

    @Override
    public Bin<DataElement> remove(DataElement data)
    {
        return myInnerBinner.remove(data);
    }

    @Override
    public void removeAll(Collection<? extends DataElement> dataItems)
    {
        myInnerBinner.removeAll(dataItems);
    }

    @Override
    public void removeIf(Predicate<? super DataElement> filter)
    {
        myInnerBinner.removeIf(filter);
    }

    @Override
    public void clear()
    {
        myInnerBinner.clear();
    }

    @Override
    public void addBin(Bin<DataElement> bin)
    {
        myInnerBinner.addBin(bin);
    }

    @Override
    public void rebin()
    {
        myInnerBinner.rebin();
    }

    @Override
    public List<Bin<DataElement>> getBins()
    {
        return myInnerBinner.getBins();
    }

    @Override
    public Map<Object, Bin<DataElement>> getBinsMap()
    {
        return myInnerBinner.getBinsMap();
    }

    @Override
    public void setListener(ListDataListener<Bin<DataElement>> listener)
    {
        myInnerBinner.setListener(listener);
    }

    /**
     * Gets the criteriaElement.
     *
     * @return the criteriaElement
     */
    public BinCriteriaElement getCriteriaElement()
    {
        return myCriteriaElement;
    }

    /**
     * Gets the layer.
     *
     * @return the layer
     */
    public DataTypeInfo getLayer()
    {
        return myLayer;
    }

    /**
     * Adds all the data elements for the layer to the binner.
     *
     * @param timeSpan the time span to load, or null for all time
     */
    public void addAllElements(TimeSpan timeSpan)
    {
        List<Long> ids = myMantleToolbox.getDataElementLookupUtils().getDataElementCacheIds(myLayer, timeSpan);
        addIds(ids);
    }

    /**
     * Adds data elements for the given IDs.
     *
     * @param ids the IDs
     */
    public void addIds(List<Long> ids)
    {
        try
        {
            List<DataElement> elements = myMantleToolbox.getDataElementLookupUtils().getDataElements(ids, myLayer, null, true);
            elements = elements.stream().filter(e -> e != null && e.getVisualizationState().isVisible()).collect(Collectors.toList());
            if (!elements.isEmpty())
            {
                addAll(elements);
            }
        }
        catch (DataElementLookupException e)
        {
            LOGGER.error(e, e);
        }
    }

    /**
     * Removes data elements for the given IDs.
     *
     * @param ids the IDs
     */
    public void removeIds(Set<Long> ids)
    {
        removeIf(element -> ids.contains(Long.valueOf(element.getIdInCache())));
    }

    /**
     * Automatically bins the current data.
     */
    public void autoBin()
    {
        LOGGER.info("autoBin");
        CriteriaType criteriaType = myCriteriaElement.getCriteriaType();
        if (criteriaType instanceof RangeCriteria)
        {
            RangeCriteria rangeCriteria = (RangeCriteria)criteriaType;
            final int minAutoBinCount = Math.min(3, getBins().stream().mapToInt(b -> b.getSize()).sum());
            final int maxAutoBinCount = 100;

            if (getBins().size() > maxAutoBinCount)
            {
                do
                {
                    rangeCriteria.setBinWidth(rangeCriteria.getBinWidth() * 10);
                    LOGGER.info("Rebinning " + rangeCriteria.getBinWidth());
                    rebin();
                }
                while (getBins().size() > maxAutoBinCount);
            }
            else
            {
                while (getBins().size() < minAutoBinCount)
                {
                    rangeCriteria.setBinWidth(rangeCriteria.getBinWidth() / 10);
                    LOGGER.info("Rebinning " + rangeCriteria.getBinWidth());
                    rebin();
                }
            }
        }
    }

    /**
     * Sets the createEmptyBins.
     *
     * @param createEmptyBins the createEmptyBins
     */
    public void setCreateEmptyBins(boolean createEmptyBins)
    {
        if (myInnerBinner instanceof AbstractBinner)
        {
            ((AbstractBinner<?>)myInnerBinner).setCreateEmptyBins(createEmptyBins);
        }
    }

    /**
     * Sets the createNABin.
     *
     * @param createNABin the createNABin
     */
    public void setCreateNABin(boolean createNABin)
    {
        if (myInnerBinner instanceof AbstractBinner)
        {
            ((AbstractBinner<?>)myInnerBinner).setCreateNABin(createNABin);
        }
    }

    /**
     * Creates the inner binner.
     *
     * @param criteriaElement the criteria element
     * @return the binner
     */
    protected Binner<DataElement> createInnerBinner(BinCriteriaElement criteriaElement)
    {
        Binner<DataElement> binner = null;
        CriteriaType criteria = criteriaElement.getCriteriaType();
        String field = criteriaElement.getField();
        if (criteria.getClass() == RangeCriteria.class)
        {
            Function<DataElement, Object> dataToValue = e -> e.getMetaData().getValue(field);
            binner = new RangeBinner<>((RangeCriteria)criteria, dataToValue, DataTypeUtilities::toDouble,
                    DataTypeUtilities::fromDouble);
        }
        else if (criteria.getClass() == UniqueCriteria.class)
        {
            Function<DataElement, Object> dataToValue = e -> e.getMetaData().getValue(field);
            binner = new UniqueValueBinner<>((UniqueCriteria)criteria, dataToValue);
        }
        else if (criteria.getClass() == TimeCriteria.class)
        {
            Function<DataElement, Date> dataToValue = e -> (Date)e.getMetaData().getValue(field);
            binner = new TimeBinner<>((TimeCriteria)criteria, dataToValue);
        }
        return binner;
    }
}
