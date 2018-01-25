package io.opensphere.analysis.binning.algorithm;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Predicate;

import io.opensphere.analysis.binning.bins.Bin;
import io.opensphere.analysis.binning.criteria.BinCriteria;
import io.opensphere.analysis.binning.criteria.BinCriteriaElement;
import io.opensphere.core.util.ListDataListener;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;

/**
 * Groups {@link DataElement} into {@link Bin} based on a passed in
 * {@link BinCriteria}.
 */
public class DataElementCriteriaBinner implements Binner<DataElement>
{
    /**
     * The collection of sub binners that bins the bins for the other criteria
     * in the list.
     */
    private final List<Map<UUID, DataElementBinner>> myContainerStore = New.list();

    /**
     * The binning criteria.
     */
    private final List<BinCriteriaElement> myCriteria;

    /** The layer. */
    private final DataTypeInfo myLayer;

    /**
     * The mantle toolbox.
     */
    private final MantleToolbox myMantleBox;

    /**
     * The top most binner that bins against the first criteria.
     */
    private final DataElementBinner myTopBinner;

    /**
     * Constructs a new binner that will bin {@link DataElement} given the
     * specified {@link BinCriteria}.
     *
     * @param mantleBox The mantle toolbox.
     * @param criteria The binning criteria to use when binning.
     * @param layer The layer
     */
    public DataElementCriteriaBinner(MantleToolbox mantleBox, BinCriteria criteria, DataTypeInfo layer)
    {
        myMantleBox = mantleBox;
        myCriteria = New.list(criteria.getCriterias());
        myLayer = layer;
        myTopBinner = new DataElementBinner(mantleBox, myCriteria.get(0), layer);
        for (int i = 1; i < myCriteria.size(); i++)
        {
            myContainerStore.add(New.map());
        }
    }

    @Override
    public Bin<DataElement> add(DataElement data)
    {
        Bin<DataElement> bin = myTopBinner.add(data);
        for (int i = 1; i < myCriteria.size(); i++)
        {
            Map<UUID, DataElementBinner> theBinsBinners = myContainerStore.get(i - 1);
            if (!theBinsBinners.containsKey(bin.getBinId()))
            {
                BinCriteriaElement element = myCriteria.get(i);
                theBinsBinners.put(bin.getBinId(), new DataElementBinner(myMantleBox, element, myLayer));
            }

            DataElementBinner binner = theBinsBinners.get(bin.getBinId());
            bin = binner.add(data);
        }

        return bin;
    }

    @Override
    public void addAll(Collection<? extends DataElement> dataItems)
    {
        for (DataElement element : dataItems)
        {
            add(element);
        }
    }

    @Override
    public void clear()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Bin<DataElement>> getBins()
    {
        List<Bin<DataElement>> bins = myTopBinner.getBins();
        if (!myContainerStore.isEmpty())
        {
            Map<UUID, DataElementBinner> binners = myContainerStore.get(myContainerStore.size() - 1);
            bins = New.list();
            for (DataElementBinner binner : binners.values())
            {
                bins.addAll(binner.getBins());
            }
        }
        return bins;
    }

    @Override
    public Map<Object, Bin<DataElement>> getBinsMap()
    {
        Map<Object, Bin<DataElement>> bins = myTopBinner.getBinsMap();
        if (!myContainerStore.isEmpty())
        {
            Map<UUID, DataElementBinner> binners = myContainerStore.get(myContainerStore.size() - 1);
            bins = New.map();
            for (DataElementBinner binner : binners.values())
            {
                bins.putAll(binner.getBinsMap());
            }
        }
        return bins;
    }

    /**
     * Gets the {@link BinCriteria} this binner is using to create the bins.
     *
     * @return The bin criteria.
     */
    public List<BinCriteriaElement> getCriteria()
    {
        return New.unmodifiableList(myCriteria);
    }

    @Override
    public void rebin()
    {
        myTopBinner.rebin();
    }

    @Override
    public Bin<DataElement> remove(DataElement data)
    {
        Bin<DataElement> binToReturn = null;
        if (!myContainerStore.isEmpty())
        {
            UUID binId = null;
            for (int i = myContainerStore.size() - 1; i >= 0; i--)
            {
                Map<UUID, DataElementBinner> binners = myContainerStore.get(i);
                if (binId == null)
                {
                    for (Entry<UUID, DataElementBinner> entry : binners.entrySet())
                    {
                        Bin<DataElement> bin = entry.getValue().remove(data);
                        if (bin != null)
                        {
                            binId = entry.getKey();
                            if (binToReturn == null)
                            {
                                binToReturn = bin;
                            }
                            break;
                        }
                    }
                }
                else
                {
                    DataElementBinner binner = binners.get(binId);
                    Bin<DataElement> bin = binner.remove(data);
                    if (bin != null)
                    {
                        binId = bin.getBinId();
                    }
                    else
                    {
                        binId = null;
                    }
                }
            }
        }

        myTopBinner.remove(data);

        return binToReturn;
    }

    @Override
    public void removeAll(Collection<? extends DataElement> dataItems)
    {
        for (DataElement element : dataItems)
        {
            remove(element);
        }
    }

    @Override
    public void removeIf(Predicate<? super DataElement> filter)
    {
        myTopBinner.removeIf(filter);
        for (Map<UUID, DataElementBinner> binners : myContainerStore)
        {
            for (DataElementBinner binner : binners.values())
            {
                binner.removeIf(filter);
            }
        }
    }

    @Override
    public void addBin(Bin<DataElement> bin)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setListener(ListDataListener<Bin<DataElement>> listener)
    {
        throw new UnsupportedOperationException();
    }
}
