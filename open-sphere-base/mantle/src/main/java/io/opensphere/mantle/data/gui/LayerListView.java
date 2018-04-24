package io.opensphere.mantle.data.gui;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.fx.FilterableCheckBoxListView;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.mantle.data.DataTypeInfo;
import javafx.application.Platform;

/**
 * A filterable check box list view for picking layers.
 */
public class LayerListView extends FilterableCheckBoxListView<DataTypeInfo>
{
    /** The toString function. */
    private static final Function<DataTypeInfo, String> TO_STRING = t -> t.getDisplayName();

    /**
     * Constructor.
     *
     * @param dataTypes the data type options
     */
    public LayerListView(Collection<? extends DataTypeInfo> dataTypes)
    {
        super(sort(dataTypes), TO_STRING, "Search layers");
        getListView().setTooltipProvider(
                t -> StringUtilities.concat(t.getDisplayName(), " - ", t.getParent().getTopParentDisplayName()));
    }

    /**
     * Gets the selections. This can be called from any thread.
     *
     * @return the selections
     */
    public Collection<DataTypeInfo> getSelections()
    {
        if (Platform.isFxApplicationThread())
        {
            return getListView().getSelections();
        }
        else
        {
            final AtomicReference<Collection<DataTypeInfo>> selections = new AtomicReference<>();
            FXUtilities.runAndWait(() -> selections.set(getListView().getSelections()));
            return selections.get();
        }
    }

    /**
     * Sorts the data types.
     *
     * @param dataTypes the data types
     * @return the sorted data types
     */
    private static Collection<DataTypeInfo> sort(Collection<? extends DataTypeInfo> dataTypes)
    {
        return CollectionUtilities.sort(dataTypes, (t1, t2) -> TO_STRING.apply(t1).compareTo(TO_STRING.apply(t2)));
    }
}
