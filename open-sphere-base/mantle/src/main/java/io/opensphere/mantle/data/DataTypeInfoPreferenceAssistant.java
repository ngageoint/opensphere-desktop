package io.opensphere.mantle.data;

import io.opensphere.core.util.swing.input.model.BooleanModel;

/**
 * The Interface DataTypeInfoPreferenceAssistant.
 */
public interface DataTypeInfoPreferenceAssistant
{
    /**
     * Get the preference value for the given property for the given data type
     * info key. If there is no preference value, the property value is not
     * changed. This must be called from the EDT.
     *
     * @param property The property whose value is to be set.
     * @param dtiKey The data type info key.
     */
    void getBooleanPreference(BooleanModel property, String dtiKey);

    /**
     * Gets the opacity preference for a specific data type info key. If the
     * preference is found it is returned, otherwise the default is returned.
     *
     * @param dtiKey the {@link DataTypeInfo} key for the data type.
     * @param def the default
     * @return the color preference
     */
    int getColorPreference(String dtiKey, int def);

    /**
     * Gets the opacity preference for a specific data type info key. If the
     * preference is found it is returned, otherwise the default is returned.
     *
     * @param dtiKey the {@link DataTypeInfo} key for the data type.
     * @param def the default
     * @return the opacity preference
     */
    int getOpacityPreference(String dtiKey, int def);

    /**
     * Gets the play state preference for a specific data type info key. If the
     * preference is found it is returned, otherwise the default is returned.
     *
     * @param dtiKey the {@link DataTypeInfo} key for the data type.
     * @return the play state preference
     */
    PlayState getPlayStatePreference(String dtiKey);

    /**
     * Gets the is visible preference for a specific data type info key. If the
     * preference is found it is returned, otherwise the default is returned.
     *
     * @param dtiKey the {@link DataTypeInfo} key for the data type.
     * @return true, if is visible false if not.
     */
    boolean isVisiblePreference(String dtiKey);

    /**
     * Gets the is visible preference for a specific data type info key. If the
     * preference is found it is returned, otherwise the default is returned.
     *
     * @param dtiKey the {@link DataTypeInfo} key for the data type.
     * @param def The default value to return if the key does not exist.
     * @return true, if is visible false if not.
     */
    boolean isVisiblePreference(String dtiKey, boolean def);

    /**
     * Removes the preferences for the {@link DataTypeInfo} key. All types.
     *
     * @param dtiKey {@link DataTypeInfo} key for the data type.
     */
    void removePreferences(String dtiKey);

    /**
     * Removes the preferences for the {@link DataTypeInfo} key.
     *
     * @param dtiKey {@link DataTypeInfo} key for the data type.
     * @param typeToRemove the type of preference to remove
     */
    void removePreferences(String dtiKey, PreferenceType... typeToRemove);

    /**
     * Removes the preferences for prefix for all types.
     *
     * @param dtiKeyPrefix the dti key prefix
     */
    void removePreferencesForPrefix(String dtiKeyPrefix);

    /**
     * Removes the preferences for all {@link DataTypeInfo} keys with the
     * specified key prefix.
     *
     * @param dtiKeyPrefix the {@link DataTypeInfo} key for the data type
     *            prefix.
     * @param typeToRemove the type of preference to remove
     */
    void removePreferencesForPrefix(String dtiKeyPrefix, PreferenceType... typeToRemove);

    /**
     * Set the preference value for the given property for the given data type
     * info key. This must be called from the EDT.
     *
     * @param property The property whose value is to be set.
     * @param dtiKey The data type info key.
     */
    void setBooleanPreference(BooleanModel property, String dtiKey);

    /**
     * Sets the play state preference for a specific data type info key.
     *
     * @param dtiKey the {@link DataTypeInfo} key for the data type.
     * @param playState the play state preference
     */
    void setPlayStatePreference(String dtiKey, PlayState playState);

    /**
     * The Enum PreferenceType.
     */
    enum PreferenceType
    {
        /** The color preference type. */
        COLOR,

        /** The opacity preference type. */
        OPACITY,

        /** The visibility preference type. */
        VISIBILITY,

        /** The play state preference type. */
        PLAY_STATE,

        /** The boolean preference type. */
        BOOLEAN,

        ;
    }
}
