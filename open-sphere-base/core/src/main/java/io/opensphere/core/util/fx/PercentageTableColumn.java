package io.opensphere.core.util.fx;

import com.google.common.base.Preconditions;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.TableColumn;

/**
 * A simple extension of the table column to add the ability to specify column
 * width as a percentage of the table width, rather than as a fixed value.
 * 
 * @param <S> The type of the TableView generic type (i.e. S ==
 *            TableView&lt;S&gt;)
 * @param <T> The type of the content in all cells in this TableColumn.
 */
public class PercentageTableColumn<S, T> extends TableColumn<S, T>
{
    /** The property in which the width is stored as a percentage. */
    private DoubleProperty percentageWidth = new SimpleDoubleProperty();

    /**
     * Creates a new table column.
     */
    public PercentageTableColumn()
    {
        percentageWidth.addListener((observable, oldValue, newValue) ->
        {
            Preconditions.checkArgument(newValue.doubleValue() >= 0 && newValue.doubleValue() <= 1,
                    "Percentage must be expressed as a decimal value between 0 and 1 (inclusive).");
        });

        // The table property is set delayed each column by column
        tableViewProperty().addListener((observable, oldValue, newValue) ->
        {
            ReadOnlyDoubleProperty tableWidth = getTableView().widthProperty();
            this.prefWidthProperty().bind(createPercentageWidthBinding(tableWidth));
        });
    }

    /**
     * Creates a binding to the width of the table.
     *
     * @param tableWidth the property for which to create the binding.
     * @return a binding bound to the width of the table.
     */
    private DoubleBinding createPercentageWidthBinding(ReadOnlyDoubleProperty tableWidth)
    {
        return Bindings.createDoubleBinding(() ->
        {
            // If the user doesn't define the percentage
            if (percentageWidth.get() <= 0)
            {
                return getWidth();
            }
            else
            {
                double tableWidthDouble = tableWidth.get();
                return percentageWidth.get() * tableWidthDouble;
            }
        }, percentageWidth, tableWidth);
    }

    /**
     * Gets the width of the column, expressed as a percentage.
     * 
     * @return the width of the column, expressed as a percentage.
     */
    public double getPercentageWidth()
    {
        return percentageWidth.get();
    }

    /**
     * Sets the width of the column, as a percentage.
     * 
     * @param percentageWidth the width of the column, as a percentage.
     */
    public void setPercentageWidth(double percentageWidth)
    {
        this.percentageWidth.set(percentageWidth);
    }

    /**
     * Gets the property in which the width is bound.
     * 
     * @return the width property.
     */
    public DoubleProperty percentageWidthProperty()
    {
        return percentageWidth;
    }
}
