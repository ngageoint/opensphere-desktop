package io.opensphere.analysis.table.functions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.JTable;

/**
 * Function class that defines string representations, value formatting, and how
 * to execute.
 */
@SuppressWarnings("boxing")
public abstract class SpreadsheetFunction
{
    /** The format string. */
    protected final String myFormatString;

    /** The function label/name. */
    protected final String myLabel;

    /**
     * Constructs a Spreadsheet Function.
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

            return sum;
        }
    }

    /**
     * Representation of Table Selection Minimum. Does not include non-numeric
     * cells.
     */
    public static class Min extends SpreadsheetFunction
    {
        /** Constructs a Min function. */
        public Min()
        {
            super("%-10.3f", "Minimum Value");
        }

        /**
         * Determines the minimum value of the selected cells in a given table.
         *
         * @override
         * @return the minimum value, or 0.0 if nothing is selected
         */
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
                    .reduce((a, b) -> Double.min(a, b));

            return result.orElse(0.0);
        }
    }

    /** Representation of Table Selection Maximum. */
    public static class Max extends SpreadsheetFunction
    {
        /** Constructs a Max function. */
        public Max()
        {
            super("%-10.3f", "Maximum Value");
        }

        /**
         * Determines the maximum value of the selected cells in a given table.
         *
         * @override
         * @return the maximum value, of 0.0 if nothing is selected
         */
        @Override
        public Number execute(JTable table)
        {
            double max = 0.0;

            int[] rows = table.getSelectedRows();
            int[] cols = table.getSelectedColumns();
            for (int r : rows)
            {
                for (int c : cols)
                {
                    max = Math.max(max, convertValue(table.getValueAt(r, c)));
                }
            }

            return max;
        }
    }

    /**
     * Representation of Table Selection Median. Does not include non-numeric
     * cells.
     */
    public static class Median extends SpreadsheetFunction
    {
        /** Constructs a Median function. */
        public Median()
        {
            super("%-10.3f", "Median Value");
        }

        /**
         * Determines the median value of the selected cells in a given table.
         *
         * @override
         * @return the median value, or NaN if nothing is selected
         */
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

            Double[] result = selectedValues.stream().filter(this::isNumber).map(this::convertValue).sorted()
                    .toArray(Double[]::new);

            int len = result.length;
            if (len == 0)
            {
                return Double.NaN;
            }
            else if (len % 2 == 0)
            {
                return (result[len / 2] + result[len / 2 - 1]) / 2;
            }
            else
            {
                return result[len / 2];
            }
        }
    }

    /**
     * Representation of Table Selection Mean. Does not include non-numeric
     * cells.
     */
    public static class Mean extends SpreadsheetFunction
    {
        /** Constructs a Mean function. */
        public Mean()
        {
            super("%-10.3f", "Mean Value");
        }

        /**
         * Determines the mean value of the selected cells in a given table.
         *
         * @override
         * @return the mean value, or NaN if nothing is selected
         */
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
                    .reduce((a, b) -> Double.sum(a, b));

            return result.map((d) -> d / selectedValues.size()).orElse(Double.NaN);
        }
    }
}
