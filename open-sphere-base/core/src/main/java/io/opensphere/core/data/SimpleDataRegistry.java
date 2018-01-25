package io.opensphere.core.data;

import java.util.Collection;
import java.util.function.Predicate;

import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.util.ref.WeakReference;

/**
 * Simplified {@link DataRegistry} API with an easier learning curve that only
 * handles in-memory, session only, single property, synchronous caching.
 *
 * @param <E> The type of model handled by this interface.
 */
public interface SimpleDataRegistry<E>
{
    /**
     * Add a listener to be notified when models are added or removed. Only a
     * {@link WeakReference} is held to the listener.
     *
     * @param listener The listener.
     */
    void addChangeListener(DataRegistryListener<E> listener);

    /**
     * Add a model to the registry. The model will be kept in memory and will
     * not be serialized to disk.
     *
     * @param model The model to be added.
     */
    void addModel(E model);

    /**
     * Get the category used by this interface.
     *
     * @return The category.
     */
    DataModelCategory getCategory();

    /**
     * Get all models that belong to the category handled by this interface.
     *
     * @return The models.
     */
    Collection<E> getModels();

    /**
     * Get all models that belong to the category handled by this interface and
     * pass the given programmatic filter.
     *
     * @param filter The filter that the models must pass.
     * @return The models.
     */
    Collection<E> getModels(Predicate<E> filter);

    /**
     * Get the property descriptor used by this interface.
     *
     * @return The property descriptor.
     */
    PropertyDescriptor<E> getPropertyDescriptor();

    /**
     * Remove a listener.
     *
     * @param listener The listener.
     */
    void removeChangeListener(DataRegistryListener<E> listener);

    /**
     * Remove a model from the registry.
     *
     * @param model The model to remove.
     */
    void removeModel(E model);

    /**
     * Remove all models of the category handled by this interface.
     */
    void removeModels();

    /**
     * Remove all models that belong to the category handled by this interface
     * and pass the given programmatic filter.
     *
     * @param filter The filter that the models must pass.
     */
    void removeModels(Predicate<E> filter);
}
