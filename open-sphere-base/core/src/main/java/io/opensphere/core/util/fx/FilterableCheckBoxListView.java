package io.opensphere.core.util.fx;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

/**
 * Pane with a filterable CheckBoxListView.
 *
 * @param <T> the type of the items
 */
public class FilterableCheckBoxListView<T> extends BorderPane
{
    /** All the items. */
    private final Collection<? extends T> myAllItems;

    /** The list view. */
    private final CheckBoxListView<T> myListView;

    /**
     * Constructor.
     *
     * @param items the items
     * @param toStringFunction the to string function
     * @param promptText the prompt text for the search field
     */
    public FilterableCheckBoxListView(Collection<? extends T> items, Function<T, String> toStringFunction, String promptText)
    {
        myAllItems = items;
        myListView = new CheckBoxListView<>(items, toStringFunction);

        TextField textField = new TextField();
        textField.setPromptText(promptText);
        textField.textProperty().addListener((obs, o, n) -> filterListItems(n));
        setTop(textField);
        setCenter(myListView);
        setMargin(textField, new Insets(0, 0, 5, 0));
    }

    /**
     * Gets the listView.
     *
     * @return the listView
     */
    public final CheckBoxListView<T> getListView()
    {
        return myListView;
    }

    /**
     * Filters the items shown in the list.
     *
     * @param searchText the search text
     */
    private void filterListItems(String searchText)
    {
        List<? extends T> filteredItems = myAllItems.stream()
                .filter(i -> containsIgnoreCase(myListView.getToStringFunction().apply(i), searchText))
                .collect(Collectors.toList());
        myListView.getItems().setAll(filteredItems);
    }

    /**
     * Determines of the first string contains the second, ignoring case.
     *
     * @param s1 the first string
     * @param s2 the second string
     * @return see description
     */
    private static boolean containsIgnoreCase(String s1, String s2)
    {
        return s1.toLowerCase().contains(s2.toLowerCase());
    }
}
