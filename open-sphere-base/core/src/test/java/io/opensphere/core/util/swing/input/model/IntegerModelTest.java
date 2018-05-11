package io.opensphere.core.util.swing.input.model;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.util.ValidationStatus;

/**
 * Test for {@link IntegerModel}.
 */
@SuppressWarnings("boxing")
public class IntegerModelTest
{
    /**
     * Test for {@link IntegerModel#getValidationStatus()}.
     */
    @Test
    public void testIsValid()
    {
        IntegerModel model = new IntegerModel(1, 10);
        model.setName("Number");

        // Null value
        Assert.assertFalse(model.getValidationStatus() == ValidationStatus.VALID);
        Assert.assertEquals("Number is a required field.", model.getErrorMessage());

        // Valid value
        model.set(1);
        Assert.assertTrue(model.getValidationStatus() == ValidationStatus.VALID);
        model.set(5);
        Assert.assertTrue(model.getValidationStatus() == ValidationStatus.VALID);
        model.set(10);
        Assert.assertTrue(model.getValidationStatus() == ValidationStatus.VALID);

        // Invalid value
        model.set(0);
        Assert.assertFalse(model.getValidationStatus() == ValidationStatus.VALID);
        Assert.assertEquals("Number must be an integer between 1 and 10", model.getErrorMessage());
        model.set(11);
        Assert.assertFalse(model.getValidationStatus() == ValidationStatus.VALID);
        Assert.assertEquals("Number must be an integer between 1 and 10", model.getErrorMessage());

        // No bounds
        model = new IntegerModel();
        model.setName("Number");
        model.set(Integer.MIN_VALUE);
        Assert.assertTrue(model.getValidationStatus() == ValidationStatus.VALID);
        Assert.assertNull(model.getErrorMessage());
        model.set(Integer.MAX_VALUE);
        Assert.assertTrue(model.getValidationStatus() == ValidationStatus.VALID);
        Assert.assertNull(model.getErrorMessage());

        // No maximum
        model = new IntegerModel(1, Integer.MAX_VALUE);
        model.setName("Number");
        model.set(0);
        Assert.assertFalse(model.getValidationStatus() == ValidationStatus.VALID);
        Assert.assertEquals("Number must be an integer >= 1", model.getErrorMessage());

        // No minimum
        model = new IntegerModel(Integer.MIN_VALUE, 10);
        model.setName("Number");
        model.set(11);
        Assert.assertFalse(model.getValidationStatus() == ValidationStatus.VALID);
        Assert.assertEquals("Number must be an integer <= 10", model.getErrorMessage());

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
