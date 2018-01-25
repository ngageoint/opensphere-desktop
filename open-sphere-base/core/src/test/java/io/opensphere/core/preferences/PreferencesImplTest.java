package io.opensphere.core.preferences;

import org.junit.Assert;
import org.junit.Test;

/** Tests for {@link PreferencesImpl}. */
public class PreferencesImplTest
{
    /** A preference key. */
    private static final String KEY = "key";

    /** Test topic. */
    private static final String TOPIC = "Test Topic";

    /** Test {@link PreferencesImpl#remove(String, Object)}. */
    @Test
    public void testRemove()
    {
        PreferencesImpl prefs = new PreferencesImpl(TOPIC);
        Assert.assertFalse(prefs.getBoolean(KEY, false));
        Assert.assertNull(prefs.putBoolean(KEY, true, this));
        Assert.assertTrue(prefs.getBoolean(KEY, false));
        prefs.addPreferenceChangeListener(KEY, new PreferenceChangeListener()
        {
            @Override
            public void preferenceChange(PreferenceChangeEvent evt)
            {
                Assert.assertNull(evt.getValueAsBoolean(null));
            }
        });
        prefs.remove(KEY, this);
        Assert.assertFalse(prefs.getBoolean(KEY, false));
    }
}
