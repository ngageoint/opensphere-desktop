package io.opensphere.analysis.table.functions;

import io.opensphere.core.util.lang.NumberUtilities;

/** Quotient of values in order of appearance. */
public class Quotient extends ColumnFunction
{
    /** Constructs a Difference function. */
    public Quotient()
    {
        super("Quotient", 0, Quotient::performDiv);
    }

    /**
     * Maps all objects to Double, then returns their quotient.
     * <p>
     * Non-parseable objects are mapped to the (multiplicative) identity value
     *
     * @param objects the objects to operate on
     * @return the quotient
     */
    static Double performDiv(Object... objects)
    {
        double[] divArr = new double[objects.length];
        for (int i = 0; i < objects.length; i++)
        {
            divArr[i] = NumberUtilities.parseDouble(objects[i], 1.);
        }

        // this is gonna throw if we don't have any objects but whatever
        double div = divArr[0];
        for (int i = 1; i < divArr.length; i++)
        {
            div /= divArr[i];
        }

        return Double.valueOf(div);
    }
}
