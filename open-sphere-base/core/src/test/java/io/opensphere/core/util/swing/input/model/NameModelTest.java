package io.opensphere.core.util.swing.input.model;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.util.ValidationStatus;

/**
 * Test for {@link NameModel}.
 */
public class NameModelTest
{
    /**
     * Test for {@link NameModel#getValidationStatus()}.
     */
    @Test
    public void testIsValid()
    {
        NameModel model = new NameModel();
        model.setName("Name");
        model.setDisallowedNames(Collections.singletonList("Bubba"));

        // Null value
        Assert.assertFalse(model.getValidationStatus() == ValidationStatus.VALID);
        Assert.assertEquals("Name is a required field.", model.getErrorMessage());

        // Valid value
        model.set("Tom");
        Assert.assertTrue(model.getValidationStatus() == ValidationStatus.VALID);

        // Empty value
        model.set("");
        Assert.assertFalse(model.getValidationStatus() == ValidationStatus.VALID);
        Assert.assertEquals("Name cannot be blank.", model.getErrorMessage());

        // Blank value
        model.set(" ");
        Assert.assertFalse(model.getValidationStatus() == ValidationStatus.VALID);
        Assert.assertEquals("Name cannot be blank.", model.getErrorMessage());

        // Disallowed value
        model.set("Bubba");
        Assert.assertFalse(model.getValidationStatus() == ValidationStatus.VALID);
        Assert.assertEquals("Name is already in use.", model.getErrorMessage());

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

        // Not required, and empty
        model.set("");
        Assert.assertTrue(model.getValidationStatus() == ValidationStatus.VALID);
    }
}
