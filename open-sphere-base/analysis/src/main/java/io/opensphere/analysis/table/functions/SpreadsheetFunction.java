package io.opensphere.analysis.table.functions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.JTable;

/**
 * Function class that defines string representations, value formatting, and how
 * to execute.
 */
public abstract class SpreadsheetFunction
{
    /** The format string. */
    protected final String myFormatString;

    /** The function label/name. */
    protected final String myLabel;

    /**
     * Initializes enumerable.
     *
     * @param formatString the function result format string
     * @param label the function name
     */
    public SpreadsheetFunction(String formatString, String label)
    {
        myFormatString = formatString;
        myLabel = label;
    }

    /**
     * Format String getter.
     *
     * @return myFormatString
     */
    public String getFormatString()
    {
        return myFormatString;
    }

    /**
     * Label getter.
     *
     * @return myLabel
     */
    public String getLabel()
    {
        return myLabel;
    }

    /**
     * Executes the function.
     *
     * @param table the table to execute the function on
     * @return the resulting numeric function execution
     */
    public abstract Number execute(JTable table);

    /**
     * Converts an object to its primitive double value.
     *
     * @param value the object to convert
     * @return the double value, or 0.0 if the object is not a Number
     */
    protected double convertValue(Object value)
    {
        try
        {
            return Double.valueOf(value.toString()).doubleValue();
        }
        catch (NumberFormatException | NullPointerException e)
        {
            return 0.0;
        }
    }

    /**
     * Attempts conversion of an object to a Double in order to determine
     * whether or not it is a Number.
     *
     * @param value the object to convert
     * @return if the conversion was successful
     */
    protected boolean isNumber(Object value)
    {
        try
        {
            Double.valueOf(value.toString());
            return true;
        }
        catch (NumberFormatException | NullPointerException e)
        {
            return false;
        }
    }

    /** Representation of Table Cell Summation. */
    public static class Sum extends SpreadsheetFunction
    {
        /** Constructs a Sum function. */
        public Sum()
        {
            super("%-10.3f", "Sum");
        }

        /**
         * Sums the selected cells in a given table.
         *
         * @override
         */
        @Override
        public Number execute(JTable table)
        {
            double sum = 0.0;

            int[] rows = table.getSelectedRows();
            int[] cols = table.getSelectedColumns();
            for (int r : rows)
            {
                for (int c : cols)
                {
                    sum += convertValue(table.getValueAt(r, c));
                }
            }

            return Double.valueOf(sum);
        }
    }

    /** Representation of Table Selection Minimum. */
    public static class Min extends SpreadsheetFunction
    {
        /** Constructs a Min function. */
        public Min()
        {
            super("%-10.3f", "Minimum Value");
        }

        /**
         * Determines the minimum value of the selected cells in a given table,
         * or 0 if nothing is selected.
         */
        @SuppressWarnings("boxing")
        @Override
        public Number execute(JTable table)
        {
            int[] rows = table.getSelectedRows();
            int[] cols = table.getSelectedColumns();

            List<Object> selectedValues = new ArrayList<>();
            for (int r : rows)
            {
                for (int c : cols)
                {
                    selectedValues.add(table.getValueAt(r, c));
                }
            }

            Optional<Double> result = selectedValues.stream().filter(this::isNumber).map(this::convertValue)
                    .reduce((a, b) -> Math.min(a, b));

            return result.orElse(Double.valueOf(0.0));
        }
    }

    public static class Max extends SpreadsheetFunction
    {
        public Max()
        {
            super("%-10.3f", "Maximum Value");
        }

        @Override
        public Number execute(JTable table)
        {
            return 0;
        }
    }

    public static class Median extends SpreadsheetFunction
    {
        public Median()
        {
            super("%-10.3f", "Median Value");
        }

        @Override
        public Number execute(JTable table)
        {
            return 0;
        }
    }

    public static class Mean extends SpreadsheetFunction
    {
        public Mean()
        {
            super("%-10.3f", "Mean Value");
        }

        @Override
        public Number execute(JTable table)
        {
            return 0;
        }
    }
}
