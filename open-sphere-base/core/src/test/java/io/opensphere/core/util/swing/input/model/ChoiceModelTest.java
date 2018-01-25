package io.opensphere.core.util.swing.input.model;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.util.ValidationStatus;

/**
 * Test for {@link ChoiceModel}.
 */
public class ChoiceModelTest
{
    /**
     * Test for {@link ChoiceModel#getValidationStatus()}.
     */
    @Test
    public void testIsValid()
    {
        ChoiceModel<String> model = new ChoiceModel<>(new String[] { "APPLES", "BANANAS" });
        model.setName("Fruit");

        // Null value
        Assert.assertFalse(model.getValidationStatus() == ValidationStatus.VALID);
        Assert.assertEquals("Fruit is a required field.", model.getErrorMessage());

        // Valid value
        model.set("APPLES");
        Assert.assertTrue(model.getValidationStatus() == ValidationStatus.VALID);

        // Invalid value
        model.set("CRANBERRIES");
        Assert.assertFalse(model.getValidationStatus() == ValidationStatus.VALID);
        Assert.assertEquals("Invalid value: CRANBERRIES", model.getErrorMessage());

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
