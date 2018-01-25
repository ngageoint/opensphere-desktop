package io.opensphere.core.util.fx;

import java.util.Collection;
import java.util.Map;
import java.util.Observable;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.util.Callback;
import javafx.util.StringConverter;

import io.opensphere.core.util.NonSuckingObservable;
import io.opensphere.core.util.collections.New;

/**
 * ListView of CheckBoxes.
 *
 * @param <T> the type of the items
 */
public class CheckBoxListView<T> extends ListView<T>
{
    /** The selections. */
    private final Map<T, BooleanProperty> mySelections = New.map();

    /** The to string function. */
    private final Function<T, String> myToStringFunction;

    /** The changed observable. */
    private final Observable myChangedObservable = new NonSuckingObservable();

    /** The selection listener. */
    private final ChangeListener<? super Boolean> mySelectionListener = (obs, o, n) -> myChangedObservable.notifyObservers();

    /** The optional tooltip provider. */
    private Callback<T, String> myTooltipProvider;

    /**
     * Constructor.
     *
     * @param items the items
     * @param toStringFunction the to string function
     */
    public CheckBoxListView(Collection<? extends T> items, Function<T, String> toStringFunction)
    {
        super();
        myToStringFunction = toStringFunction;
        setCellFactory(param -> newCell());
        addItems(items);
    }

    /**
     * Selects the selections.
     *
     * @param selections the selections
     */
    public void setSelected(Collection<? extends T> selections)
    {
        for (T selection : selections)
        {
            BooleanProperty selectedProperty = mySelections.get(selection);
            if (selectedProperty != null)
            {
                selectedProperty.set(true);
            }
        }
    }

    /**
     * Gets the selections.
     *
     * @return the selections
     */
    public Collection<T> getSelections()
    {
        return mySelections.entrySet().stream().filter(e -> e.getValue().get()).map(e -> e.getKey()).collect(Collectors.toList());
    }

    /**
     * Gets the changedObservable.
     *
     * @return the changedObservable
     */
    public Observable getChangedObservable()
    {
        return myChangedObservable;
    }

    /**
     * Adds items.
     *
     * @param items the items
     */
    public final void addItems(Collection<? extends T> items)
    {
        getItems().addAll(items);
        for (T item : items)
        {
            BooleanProperty selectedProperty = new SimpleBooleanProperty();
            selectedProperty.addListener(mySelectionListener);
            mySelections.put(item, selectedProperty);
        }
    }

    /**
     * Gets the toStringFunction.
     *
     * @return the toStringFunction
     */
    public Function<T, String> getToStringFunction()
    {
        return myToStringFunction;
    }

    /**
     * Sets the tooltipProvider.
     *
     * @param tooltipProvider the tooltipProvider
     */
    public void setTooltipProvider(Callback<T, String> tooltipProvider)
    {
        myTooltipProvider = tooltipProvider;
    }

    /**
     * Creates a new cell.
     *
     * @return the cell
     */
    private ListCell<T> newCell()
    {
        TooltipListCell<T> cell = new TooltipListCell<>(mySelections::get);
        cell.setTooltipProvider(myTooltipProvider);
        cell.setConverter(new StringConverter<T>()
        {
            @Override
            public String toString(T item)
            {
                return myToStringFunction.apply(item);
            }

            @Override
            public T fromString(String string)
            {
                throw new UnsupportedOperationException();
            }
        });
        return cell;
    }

    /**
     * CheckBoxListCell with tooltip support.
     *
     * @param <T> the type of the items
     */
    private static class TooltipListCell<T> extends CheckBoxListCell<T>
    {
        /** The tooltip text provider. */
        private Callback<T, String> myTooltipProvider;

        /** The tooltip. */
        private final Tooltip myTooltip = new Tooltip();

        /**
         * Constructor.
         *
         * @param getSelectedProperty A {@link Callback} that will return an
         *            {@code ObservableValue<Boolean>} given an item from the
         *            ListView.
         */
        public TooltipListCell(Callback<T, ObservableValue<Boolean>> getSelectedProperty)
        {
            super(getSelectedProperty);
            setTooltip(new Tooltip());
        }

        @Override
        public void updateItem(T item, boolean empty)
        {
            super.updateItem(item, empty);
            if (!empty && myTooltipProvider != null)
            {
                myTooltip.setText(myTooltipProvider.call(item));
                setTooltip(myTooltip);
            }
            else
            {
                setTooltip(null);
            }
        }

        /**
         * Sets the tooltipProvider.
         *
         * @param tooltipProvider the tooltipProvider
         */
        public void setTooltipProvider(Callback<T, String> tooltipProvider)
        {
            myTooltipProvider = tooltipProvider;
        }
    }
}
