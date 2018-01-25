package io.opensphere.core.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import io.opensphere.core.cache.accessor.PropertyAccessor;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;

/**
 * A representation of a change to cached values.
 */
public class CacheModificationReport
{
    /** The data model category. */
    private final DataModelCategory myDataModelCategory;

    /** The affected model ids. */
    private final long[] myIds;

    /** The descriptors for the changed properties. */
    private final Collection<? extends PropertyDescriptor<?>> myPropertyDescriptors;

    /**
     * Find reports in the list with identical data model categories and
     * affected ids, and combine them.
     *
     * @param list The list to merge.
     */
    public static void merge(List<CacheModificationReport> list)
    {
        Iterator<CacheModificationReport> iter0 = list.iterator();
        for (int i = 0; i < list.size(); ++i)
        {
            CacheModificationReport report0 = iter0.next();
            DataModelCategory dataModelCategory0 = report0.getDataModelCategory();

            List<PropertyDescriptor<?>> mergedPropertyDescriptors = new ArrayList<>();

            for (Iterator<CacheModificationReport> iter1 = list.listIterator(i + 1); iter1.hasNext();)
            {
                CacheModificationReport report1 = iter1.next();

                if (dataModelCategory0.equals(report1.getDataModelCategory())
                        && Arrays.equals(report0.getIds(), report1.getIds()))
                {
                    mergedPropertyDescriptors.addAll(report1.getPropertyDescriptors());
                    iter1.remove();
                }
            }

            if (!mergedPropertyDescriptors.isEmpty())
            {
                mergedPropertyDescriptors.addAll(0, report0.getPropertyDescriptors());

                list.set(i, new CacheModificationReport(dataModelCategory0, report0.getIds(), mergedPropertyDescriptors));
            }
        }
    }

    /**
     * Construct the cache modification report.
     *
     * @param dataModelCategory The affected data model category.
     * @param ids The affected model ids.
     * @param propertyDescriptors The descriptors for the changed properties.
     */
    public CacheModificationReport(DataModelCategory dataModelCategory, long[] ids,
            Collection<? extends PropertyDescriptor<?>> propertyDescriptors)
    {
        Utilities.checkNull(dataModelCategory, "dataModelCategory");
        Utilities.checkNull(ids, "ids");
        Utilities.checkNull(propertyDescriptors, "propertyDescriptors");

        myDataModelCategory = dataModelCategory;
        myIds = ids.clone();
        myPropertyDescriptors = Collections.unmodifiableCollection(new ArrayList<PropertyDescriptor<?>>(propertyDescriptors));
    }

    /**
     * Filter a collection of accessors to only contain the ones applicable to
     * this report.
     *
     * @param <T> The type of the objects that provide data to the accessors.
     * @param input The input accessors.
     * @return The filtered accessors.
     */
    public <T> Collection<? extends PropertyAccessor<? super T, ?>> filterAccessors(
            Collection<? extends PropertyAccessor<? super T, ?>> input)
    {
        Collection<PropertyAccessor<? super T, ?>> accessors = New.collection(input.size());

        Iterator<? extends PropertyAccessor<? super T, ?>> accIter = input.iterator();
        Iterator<? extends PropertyDescriptor<?>> descIter = getPropertyDescriptors().iterator();

        // They're probably in the same order, so take advantage.
        PropertyDescriptor<?> desc = null;
        while (accIter.hasNext())
        {
            PropertyAccessor<? super T, ?> acc = accIter.next();
            if (desc == null && descIter.hasNext())
            {
                desc = descIter.next();
            }
            if (acc.getPropertyDescriptor().equals(desc))
            {
                accessors.add(acc);
                desc = null;
            }
        }
        // If all the descriptors weren't used, it means they weren't in order.
        // Make sure none were missed.
        if (desc != null || descIter.hasNext())
        {
            for (PropertyAccessor<? super T, ?> acc : input)
            {
                if (!accessors.contains(acc) && getPropertyDescriptors().contains(acc.getPropertyDescriptor()))
                {
                    accessors.add(acc);
                }
            }
        }
        return accessors;
    }

    /**
     * Accessor for the dataModelCategory.
     *
     * @return The dataModelCategory.
     */
    public DataModelCategory getDataModelCategory()
    {
        return myDataModelCategory;
    }

    /**
     * Accessor for the ids.
     *
     * @return The ids.
     */
    public long[] getIds()
    {
        return myIds.clone();
    }

    /**
     * Accessor for the propertyDescriptors.
     *
     * @return The propertyDescriptors.
     */
    public Collection<? extends PropertyDescriptor<?>> getPropertyDescriptors()
    {
        return myPropertyDescriptors;
    }
}
