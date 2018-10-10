package io.opensphere.core.data;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import io.opensphere.core.cache.SimpleSessionOnlyCacheDeposit;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.SimpleQuery;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;

/**
 * Implementation for {@link SimpleDataRegistry}.
 *
 * @param <E> The type of model handled by this interface.
 */
public class SimpleDataRegistryImpl<E> implements SimpleDataRegistry<E>
{
    /** The data model category. */
    private final DataModelCategory myDataModelCategory;

    /** The real data registry. */
    private final DataRegistry myDataRegistry;

    /** The property descriptor for the models. */
    private final PropertyDescriptor<E> myPropertyDescriptor;

    /**
     * Construct the simple interface.
     *
     * @param type The type of models handled by this interface.
     * @param source The source string, used to construct the
     *            {@link DataModelCategory}. This would typically be the name of
     *            the class adding the models.
     * @param dataRegistry The real data registry.
     */
    public SimpleDataRegistryImpl(Class<E> type, String source, DataRegistry dataRegistry)
    {
        myDataRegistry = Utilities.checkNull(dataRegistry, "dataRegistry");
        myDataModelCategory = new DataModelCategory(Utilities.checkNull(source, "source"), SimpleDataRegistryImpl.class.getName(),
                Utilities.checkNull(type, "type").getName());
        myPropertyDescriptor = new PropertyDescriptor<>("value", type);
    }

    @Override
    public void addChangeListener(DataRegistryListener<E> listener)
    {
        getDataRegistry().addChangeListener(listener, getCategory(), getPropertyDescriptor());
    }

    @Override
    public void addModel(E model)
    {
        getDataRegistry().addModels(
                new SimpleSessionOnlyCacheDeposit<>(getCategory(), getPropertyDescriptor(), Collections.singleton(model)));
    }

    @Override
    public DataModelCategory getCategory()
    {
        return myDataModelCategory;
    }

    @Override
    public Collection<E> getModels()
    {
        SimpleQuery<E> query = new SimpleQuery<>(getCategory(), getPropertyDescriptor());
        getDataRegistry().performLocalQuery(query);
        return query.getResults();
    }

    @Override
    public Collection<E> getModels(Predicate<E> filter)
    {
        Collection<E> models = getModels();
        Collection<E> results = New.collection(models.size());
        for (E model : models)
        {
            if (filter.test(model))
            {
                results.add(model);
            }
        }
        return results;
    }

    @Override
    public PropertyDescriptor<E> getPropertyDescriptor()
    {
        return myPropertyDescriptor;
    }

    @Override
    public void removeChangeListener(DataRegistryListener<E> listener)
    {
        getDataRegistry().removeChangeListener(listener);
    }

    @Override
    public void removeModel(E model)
    {
        SimpleQuery<E> query = new SimpleQuery<>(getCategory(), getPropertyDescriptor());
        long[] idsForCategory = getDataRegistry().performLocalQuery(query);
        long idToRemove = -1L;
        for (int index = 0; index < idsForCategory.length; ++index)
        {
            if (query.getResults().get(index).equals(model))
            {
                idToRemove = idsForCategory[index];
                break;
            }
        }
        if (idToRemove != -1L)
        {
            getDataRegistry().removeModels(new long[] { idToRemove });
        }
    }

    @Override
    public void removeModels()
    {
        getDataRegistry().removeModels(getCategory(), false);
    }

    @Override
    public void removeModels(Predicate<E> filter)
    {
        SimpleQuery<E> query = new SimpleQuery<>(getCategory(), getPropertyDescriptor());
        long[] idsForCategory = getDataRegistry().performLocalQuery(query);
        TLongList idsToRemove = new TLongArrayList(idsForCategory.length);
        for (int index = 0; index < idsForCategory.length; ++index)
        {
            if (filter.test(query.getResults().get(index)))
            {
                idsToRemove.add(idsForCategory[index]);
            }
        }
        getDataRegistry().removeModels(idsToRemove.toArray());
    }

    /**
     * Get the data registry.
     *
     * @return The data registry.
     */
    protected DataRegistry getDataRegistry()
    {
        return myDataRegistry;
    }
}
