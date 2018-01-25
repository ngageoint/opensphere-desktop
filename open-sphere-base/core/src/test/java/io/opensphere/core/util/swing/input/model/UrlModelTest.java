package io.opensphere.core.util.swing.input.model;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.util.ValidationStatus;

/**
 * Test for {@link UrlModel}.
 */
public class UrlModelTest
{
    /**
     * Test for {@link UrlModel#getValidationStatus()}.
     */
    @Test
    public void testIsValid()
    {
        UrlModel model = new UrlModel();
        model.setName("URL");
        model.setDisallowedUrls(Collections.singletonList("http://bad.com"));

        // Null value
        Assert.assertFalse(model.getValidationStatus() == ValidationStatus.VALID);
        Assert.assertEquals("URL is a required field.", model.getErrorMessage());

        // Valid value
        model.set("http://good.com");
        Assert.assertTrue(model.getValidationStatus() == ValidationStatus.VALID);

        // Empty value
        model.set("");
        Assert.assertFalse(model.getValidationStatus() == ValidationStatus.VALID);
        Assert.assertEquals("URL is not a valid URL.", model.getErrorMessage());

        // Invalid value
        model.set("invalid.com");
        Assert.assertFalse(model.getValidationStatus() == ValidationStatus.VALID);
        Assert.assertEquals("URL is not a valid URL.", model.getErrorMessage());

        // Disallowed value
        model.set("http://bad.com");
        Assert.assertFalse(model.getValidationStatus() == ValidationStatus.VALID);
        Assert.assertEquals("URL is already in use.", model.getErrorMessage());

        // Turn off validation
        model.setValidating(false);
        Assert.assertTrue(model.getValidationStatus() == ValidationStatus.VALID);

        // Not required, but disallowed
        model.setValidating(true);
        model.setRequired(false);
        Assert.assertFalse(model.getValidationStatus() == ValidationStatus.VALID);

        // Not required, but invalid
        model.set("invalid.com");
        Assert.assertFalse(model.getValidationStatus() == ValidationStatus.VALID);

        // Not required, and null
        model.set(null);
        Assert.assertTrue(model.getValidationStatus() == ValidationStatus.VALID);

        // Not required, and empty
        model.set("");
        Assert.assertTrue(model.getValidationStatus() == ValidationStatus.VALID);
    }
}
