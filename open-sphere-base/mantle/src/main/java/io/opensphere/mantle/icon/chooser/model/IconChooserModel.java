package io.opensphere.mantle.icon.chooser.model;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.icon.IconRegistryListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.util.Callback;

/**
 * The model which backs the icon chooser. This model is tied to mirror the
 * contents of the {@link IconRegistry}, however, it reflects unidirectional
 * changes from the registry in a read only manner. New icons are added to the
 * registry and this model is updated via events.
 */
public class IconChooserModel
{
    /** The {@link Logger} instance used to capture output of this class. */
    private static final Logger LOG = Logger.getLogger(IconChooserModel.class);

    /**
     * A callback in which the set of extractors used to define live updates on
     * the model are configured. An extractor allows the model to register a
     * change when a given field is changed, permitting live-changes to filtered
     * lists. For example, when the collection name property is configured as an
     * extractor, any changes to the collection name will emit a change through
     * the {@link #myIconRecords} observable list. This allows for a filtered
     * sublist to be created from the {@link #myIconRecords} that defines
     * inclusion in the sublist as matching a given value for the collection
     * name property. When changes are made to an {@link IconRecord}'s
     * collection name value, then the sublist's member set changes to include
     * or exclude the changed record based on the value change of the property.
     */
    private static final Callback<IconRecord, Observable[]> EXTRACTORS = r -> new Observable[] { r.collectionNameProperty(),
        r.favoriteProperty(), r.getTags(), r.nameProperty(), r.descriptionProperty() };

    /**
     * The icon records stored in the model. This is updated and backed by the
     * icon registry. Package visibility to avoid synthetic accessor methods.
     */
    final ObservableList<IconRecord> myIconRecords;

    private final ObservableMap<String, IconSet> myCollections = FXCollections.observableHashMap();

    /**
     * An observable list of the unique collection names contained within all
     * {@link IconRecord}s.
     */
    private final ObservableList<String> myCollectionNames = FXCollections.observableArrayList();

    /** The icon registry to which the model is bound. */
    private IconRegistry myIconRegistry;

    /** The listener used to react to registry changes. */
    private IconRegistryListener myRegistryListener;

    /**
     * Creates a new icon chooser model.
     */
    public IconChooserModel()
    {
        myIconRecords = FXCollections.observableArrayList(EXTRACTORS);
        myIconRecords.addListener((ListChangeListener<IconRecord>)c -> updateCollectionNames());
        myRegistryListener = new IconRegistryListener()
        {
            @Override
            public void iconsUnassigned(List<Long> deIds, Object source)
            {
                /* intentionally blank */
            }

            @Override
            public void iconsRemoved(List<IconRecord> removed, Object source)
            {
                myIconRecords.removeAll(removed);
            }

            @Override
            public void iconsAdded(List<IconRecord> added, Object source)
            {
                FXUtilities.runOnFXThread(() -> myIconRecords.addAll(added));
            }

            @Override
            public void iconAssigned(long iconId, List<Long> deIds, Object source)
            {
                /* intentionally blank */
            }
        };
    }

    /**
     * Stores the supplied value in the {@link #myIconRegistry} field.
     *
     * @param iconRegistry the value to store in the iconRegistry field.
     */
    public void setIconRegistry(IconRegistry iconRegistry)
    {
        if (myIconRegistry != null)
        {
            myIconRegistry.removeListener(myRegistryListener);
        }

        myIconRegistry = iconRegistry;
        iconRegistry.addListener(myRegistryListener);

        Collection<IconRecord> iconRecords = myIconRegistry.getIconRecords();
        myIconRecords.retainAll(iconRecords);
        myIconRecords.addAll(CollectionUtils.disjunction(iconRecords, myIconRecords));
        myIconRecords.sort((o1, o2) -> o1.nameProperty().get().compareTo(o2.nameProperty().get()));
    }

    /**
     * Gets the value of the {@link #myIconRecords} field.
     *
     * @return the value of the myIconRecords field.
     */
    public ObservableList<IconRecord> getIconRecords()
    {
        return myIconRecords;
    }

    /**
     * Gets an observable list containing the icon records that match the
     * supplied predicate. This returns a live-updating sublist of the model,
     * but this list will only update with child record changes if the predicate
     * is configured on one of the fields defined within the extractor set
     * configured on {@link #myIconRecords} (defined in {@link #EXTRACTORS}).
     *
     * @param predicate the predicate with which to match icon records.
     * @return a live-filtered list of items matching the supplied predicate.
     */
    public ObservableList<IconRecord> getIconRecords(Predicate<IconRecord> predicate)
    {
        return myIconRecords.filtered(predicate);
    }

    /**
     * Gets the value of the {@link #myCollectionNames} field.
     *
     * @return the value of the myCollectionNames field.
     */
    public ObservableList<String> getCollectionNames()
    {
        return myCollectionNames;
    }

    /**
     * Updates the list of collection names to match the unique set defined
     * within the icon records.
     */
    private void updateCollectionNames()
    {
        LOG.info("Updating collection names.");
        Set<String> set = myIconRecords.stream().map(r -> r.collectionNameProperty().get()).distinct()
                .collect(Collectors.toSet());

        // ensure the default sets are present:
        set.add(IconRecord.FAVORITES_COLLECTION);

        // ensure only the items in the set are present in the list:
        myCollectionNames.retainAll(set);
        // add new items present in the set not previously in the list:
        myCollectionNames.addAll(CollectionUtils.disjunction(set, myCollectionNames));

        for (String name : myCollectionNames)
        {
            myCollections.computeIfAbsent(name, k ->
            {
                IconSet newIconSet = new IconSet();
                newIconSet.setName(name);
                return newIconSet;
            });
        }
        LOG.info("Finished updating collection names.");
    }

    /**
     * Gets the value of the {@link #myCollections} field.
     *
     * @return the value stored in the {@link #myCollections} field.
     */
    public ObservableMap<String, IconSet> getCollections()
    {
        return myCollections;
    }
}
