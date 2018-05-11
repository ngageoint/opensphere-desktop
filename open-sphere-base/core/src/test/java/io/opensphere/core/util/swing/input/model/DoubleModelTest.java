package io.opensphere.core.util.swing.input.model;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.util.ValidationStatus;

/**
 * Test for {@link DoubleModel}.
 */
@SuppressWarnings("boxing")
public class DoubleModelTest
{
    /**
     * Test for {@link DoubleModel#getValidationStatus()}.
     */
    @Test
    public void testIsValid()
    {
        DoubleModel model = new DoubleModel(1, 10);
        model.setName("Number");

        // Null value
        Assert.assertFalse(model.getValidationStatus() == ValidationStatus.VALID);
        Assert.assertEquals("Number is a required field.", model.getErrorMessage());

        // Valid value
        model.set(1.);
        Assert.assertTrue(model.getValidationStatus() == ValidationStatus.VALID);
        model.set(5.);
        Assert.assertTrue(model.getValidationStatus() == ValidationStatus.VALID);
        model.set(10.);
        Assert.assertTrue(model.getValidationStatus() == ValidationStatus.VALID);

        // Invalid value
        model.set(0.);
        Assert.assertFalse(model.getValidationStatus() == ValidationStatus.VALID);
        Assert.assertEquals("Number must be a number between 1.0 and 10.0", model.getErrorMessage());
        model.set(11.);
        Assert.assertFalse(model.getValidationStatus() == ValidationStatus.VALID);
        Assert.assertEquals("Number must be a number between 1.0 and 10.0", model.getErrorMessage());

        // No maximum
        model = new DoubleModel(1, Double.MAX_VALUE);
        model.setName("Number");
        model.set(0.);
        Assert.assertFalse(model.getValidationStatus() == ValidationStatus.VALID);
        Assert.assertEquals("Number must be a number >= 1.0", model.getErrorMessage());

        // No minimum
        model = new DoubleModel(-Double.MAX_VALUE, 10.);
        model.setName("Number");
        model.set(11.);
        Assert.assertFalse(model.getValidationStatus() == ValidationStatus.VALID);
        Assert.assertEquals("Number must be a number <= 10.0", model.getErrorMessage());

        // No bounds
        model = new DoubleModel(-Double.MAX_VALUE, Double.MAX_VALUE);
        model.setName("Number");
        model.set(Double.NaN);
        Assert.assertFalse(model.getValidationStatus() == ValidationStatus.VALID);
        Assert.assertEquals("Number must be a valid number", model.getErrorMessage());

        // Turn off validation
        model.setValidating(false);
        Assert.assertTrue(model.getValidationStatus() == ValidationStatus.VALID);

        // Not required, but invalid
        model.setValidating(true);
        model.setRequired(false);
        Assert.assertFalse(model.getValidationStatus() == ValidationStatus.VALID);

        // Not required, and null
        model.set(null);
        Assert.assertTrue(model.getValidationStatus() == ValidationStatus.VALID);
    }
}
