package io.opensphere.core.preferences;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/** Test container for preferences. */
@XmlRootElement
class TestPreferenceContainer
{
    /** The preference. */
    @XmlElement(name = "preference")
    private Preference<?> myPreference;

    /**
     * Constructor that takes one preference.
     *
     * @param pref The preference.
     */
    public TestPreferenceContainer(Preference<?> pref)
    {
        myPreference = pref;
    }

    /**
     * Default constructor.
     */
    protected TestPreferenceContainer()
    {
    }

    /**
     * Get the preferences.
     *
     * @return The preferences.
     */
    public Preference<?> getPreference()
    {
        return myPreference;
    }
}
