package io.opensphere.filterbuilder2.layers;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import io.opensphere.core.datafilter.columns.MutableColumnMappingController;
import io.opensphere.core.util.ValidatorSupport;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.fx.CheckBoxListView;
import io.opensphere.core.util.fx.Editor;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.filterbuilder.filter.v1.Filter;
import io.opensphere.filterbuilder.filter.v1.Source;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;

/** JavaFX layer association pane. */
public class LayerAssociationPane extends VBox implements Editor
{
    /** The mantle toolbox. */
    private final MantleToolbox myMantleToolbox;

    /** The filter. */
    private final Filter myFilter;

    /** The layer selector pane. */
    private final CheckBoxListView<Source> myLayerPane;

    /** The column mappings pane. */
    private final ColumnMappingsPane myColumnMappingsPane;

    /**
     * Constructor.
     *
     * @param controller the column mapping controller
     * @param mantleToolbox the mantle toolbox
     * @param filter the filter
     */
    public LayerAssociationPane(MutableColumnMappingController controller, MantleToolbox mantleToolbox, Filter filter)
    {
        this(controller, mantleToolbox, filter, filter.getFields());
    }

    /**
     * Construct a LayerAssociationPane for the specified Filter. Also included is a set of fields to be used by the Filter. The
     * set of fields provided may be different from that obtained by calling the Filter's getFields method to support the case of
     * the Filter being edited.
     *
     * @param colCtrl column mapping controller (mutable variety)
     * @param tools the toolbox for the Mantle
     * @param filter the Filter
     * @param filterFields the fields to be used by the Filter
     */
    public LayerAssociationPane(MutableColumnMappingController colCtrl, MantleToolbox tools, Filter filter,
            Set<String> filterFields)
    {
        super(10);
        myMantleToolbox = tools;
        myFilter = filter;

        myLayerPane = createLayerPane();
        myColumnMappingsPane = new ColumnMappingsPane(colCtrl, myMantleToolbox, myFilter.getTypeKey(), filterFields);
        updateMappingPane();
        getChildren().addAll(createTopPane(), myColumnMappingsPane);
        myLayerPane.getChangedObservable().addObserver((obs, arg) -> updateMappingPane());
    }

    @Override
    public ValidatorSupport getValidatorSupport()
    {
        return myColumnMappingsPane.getValidatorSupport();
    }

    @Override
    public void accept()
    {
        List<Source> srcList = myFilter.getOtherSources();
        // map the Filter's sources by type key
        Map<String, Source> orig = mapBy(srcList, s -> s.getTypeKey());
        srcList.clear();
        // of those selected, substitute the original, if present
        myLayerPane.getSelections().stream().map(s ->
        {
            Source s0 = orig.get(s.getTypeKey());
            if (s0 != null)
            {
                return s0;
            }
            return s;
        }).forEach(s -> srcList.add(s));

        myColumnMappingsPane.accept();
    }

    /**
     * Maps the supplied collection using the supplied function for each value.
     *
     * @param c the collection to map.
     * @param f the function to apply to each value.
     * @return a Map generated from the supplied collection.
     * @param <K> the generic type of the key of the map.
     * @param <V> the generic type of the value of the map.
     */
    private static <K, V> Map<K, V> mapBy(Collection<V> c, Function<V, K> f)
    {
        TreeMap<K, V> m = new TreeMap<>();
        c.stream().forEach(v -> m.put(f.apply(v), v));
        return m;
    }

    /**
     * Check to see if the user should be queried to confirm the action of this editor. In particular, if the user has removed
     * support for all layers, the possibly unintended effect will be to delete the filter completely. Such an operation should
     * not be allowed without confirmation that it is intended.
     *
     * @return true if and only if no layers are selected
     */
    public boolean requireConfirmation()
    {
        return myLayerPane.getSelections().isEmpty();
    }

    /**
     * Creates the top pane.
     *
     * @return the top pane
     */
    private Node createTopPane()
    {
        return FXUtilities.newBorderPane(new Label("Choose Layers:"), myLayerPane);
    }

    /**
     * Creates the layer pane.
     *
     * @return the layer pane
     */
    private CheckBoxListView<Source> createLayerPane()
    {
        CheckBoxListView<Source> pane = new CheckBoxListView<>(getSources(), LayerAssociationPane::toDisplayText);
        pane.setPrefHeight(250);
        pane.setSelected(myFilter.getOtherSources());
        return pane;
    }

    /**
     * Gets the possible sources (data types) for this filter.
     *
     * @return the sources
     */
    private Collection<Source> getSources()
    {
        Predicate<DataTypeInfo> typePredicate = t ->
        {
            MetaDataInfo metaData = t.getMetaDataInfo();
            return metaData != null && metaData.getKeyCount() > 0;
        };

        Stream<DataGroupInfo> activeGroups = myMantleToolbox.getDataGroupController().getDataGroupInfoSet().stream()
                .flatMap(g -> g.groupStream()).filter(g -> g.activationProperty().isActive());
        Stream<DataTypeInfo> filterableTypes = activeGroups.flatMap(g -> g.getMembers(false).stream()).filter(typePredicate);
        List<Source> sources = filterableTypes.map(Source::fromDataType).collect(Collectors.toList());

        Collections.sort(sources, (o1, o2) -> toDisplayText(o1).compareTo(toDisplayText(o2)));
        sources.addAll(0, CollectionUtilities.difference(myFilter.getOtherSources(), sources));
        return sources;
    }

    /**
     * Updates the mapping pane.
     */
    private void updateMappingPane()
    {
        // remove the filter's native type from the list before passing it on
        List<Source> types = new LinkedList<>();
        myLayerPane.getSelections().stream()
                .filter(s -> !s.getTypeKey().equals(myFilter.getSource().getTypeKey()))
                .forEach(s -> types.add(s));

        myColumnMappingsPane.setDataTypes(toDataTypes(types));
        myColumnMappingsPane.setVisible(myColumnMappingsPane.hasMappings());
    }

    /**
     * Converts sources to data types.
     *
     * @param sources the sources
     * @return the data types
     */
    private Collection<DataTypeInfo> toDataTypes(Collection<? extends Source> sources)
    {
        return sources.stream().map(this::toDataType).filter(t -> t != null).collect(Collectors.toList());
    }

    /**
     * Converts a source to a data type.
     *
     * @param source the source
     * @return the data type
     */
    private DataTypeInfo toDataType(Source source)
    {
        return myMantleToolbox.getDataGroupController().findMemberById(source.getTypeKey());
    }

    /**
     * Gets the display text for the source.
     *
     * @param source the source
     * @return the display text
     */
    private static String toDisplayText(Source source)
    {
        StringBuilder sb = new StringBuilder(32);
        sb.append(source.getTypeDisplayName());
        sb.append(" (");
        sb.append(source.getServerName());
        sb.append(')');
        return sb.toString();
    }
}
