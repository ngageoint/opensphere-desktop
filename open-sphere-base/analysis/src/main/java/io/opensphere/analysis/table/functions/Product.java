package io.opensphere.analysis.table.functions;

import io.opensphere.core.util.lang.NumberUtilities;

/** Product of values. */
public class Product extends ColumnFunction
{
    /** Constructs a Product function. */
    public Product()
    {
        super("Product", 0, Product::performProd);
    }

    /**
     * Maps all objects to Double, then returns their product.
     * <p>
     * Non-parseable objects are mapped to the (multiplicative) identity value.
     *
     * @param objects the objects to operate on
     * @return the product
     */
    static Double performProd(Object... objects)
    {
        double product = 1.;
        for (Object o : objects)
        {
            product *= NumberUtilities.parseDouble(o, 1.);
        }

        return Double.valueOf(product);
    }
}
