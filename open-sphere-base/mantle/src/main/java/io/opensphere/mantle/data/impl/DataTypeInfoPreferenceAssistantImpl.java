package io.opensphere.mantle.data.impl;

import java.awt.Color;
import java.awt.EventQueue;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.core.util.lang.enums.EnumUtilities;
import io.opensphere.core.util.swing.input.model.BooleanModel;
import io.opensphere.mantle.data.AbstractDataTypeInfoChangeEvent;
import io.opensphere.mantle.data.DataTypeInfoPreferenceAssistant;
import io.opensphere.mantle.data.PlayState;
import io.opensphere.mantle.data.event.DataTypeInfoColorChangeEvent;
import io.opensphere.mantle.data.event.DataTypeVisibilityChangeEvent;

/**
 * The Class DataTypeInfoPreferenceAssistant.
 */
public class DataTypeInfoPreferenceAssistantImpl implements DataTypeInfoPreferenceAssistant
{
    /** The "dtiKey" parameter. */
    private static final String DTI_KEY = "dtiKey";

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(DataTypeInfoPreferenceAssistantImpl.class);

    /** The Constant ourEventExecutorService. */
    private static final ThreadPoolExecutor ourEventExecutorService = new ThreadPoolExecutor(1, 1, 20, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory("DataTypeInfoPreferenceAssistantImpl:Worker", 3, 4));

    static
    {
        ourEventExecutorService.allowCoreThreadTimeOut(true);
    }

    /** The data type change listener. */
    @SuppressWarnings("PMD.SingularField")
    private final EventListener<AbstractDataTypeInfoChangeEvent> myDataTypeChangeListener;

    /** The Preferences. */
    private final Preferences myPreferences;

    /** The Toolbox. */
    @SuppressWarnings("PMD.SingularField")
    private final Toolbox myToolbox;

    /**
     * Get the preferences key for default value of the given type.
     *
     * @param dtiKey the dti key
     * @param type the type
     * @return the pref key for type
     */
    private static String getPrefKeyForDefaultType(String dtiKey, PreferenceType type)
    {
        StringBuilder key = new StringBuilder();
        key.append(dtiKey);
        key.append("::DEFAULT_").append(type.toString());
        return key.toString();
    }

    /**
     * Gets the pref key for type.
     *
     * @param dtiKey the dti key
     * @param type the type
     * @return the pref key for type
     */
    private static String getPrefKeyForType(String dtiKey, PreferenceType type)
    {
        StringBuilder key = new StringBuilder();
        key.append(dtiKey);
        key.append("::").append(type.toString());
        return key.toString();
    }

    /**
     * To type list.
     *
     * @param types the types
     * @return the string
     */
    private static String toTypeList(PreferenceType... types)
    {
        if (types == null)
        {
            return "NULL";
        }
        else if (types.length == 0)
        {
            return "NONE";
        }
        else
        {
            return Arrays.stream(types).map(t -> t.toString()).collect(Collectors.joining(","));
        }
    }

    /**
     * Instantiates a new data type info preference assistant impl.
     *
     * @param tb the {@link Toolbox}
     */
    public DataTypeInfoPreferenceAssistantImpl(Toolbox tb)
    {
        Utilities.checkNull(tb, "tb");
        myToolbox = tb;
        myDataTypeChangeListener = createDataTypeChangeListener();
        myToolbox.getEventManager().subscribe(AbstractDataTypeInfoChangeEvent.class, myDataTypeChangeListener);
        myPreferences = myToolbox.getPreferencesRegistry().getPreferences(DataTypeInfoPreferenceAssistant.class);
    }

    @Override
    public void getBooleanPreference(BooleanModel property, String dtiKey)
    {
        assert EventQueue.isDispatchThread();
        Utilities.checkNull(dtiKey, DTI_KEY);
        boolean pref = myPreferences.getBoolean(getPrefKeyForType(dtiKey, PreferenceType.BOOLEAN) + "::" + property.getName(),
                myPreferences.getBoolean(getPrefKeyForDefaultType(dtiKey, PreferenceType.BOOLEAN) + "::" + property.getName(),
                        property.get().booleanValue()));
        property.set(Boolean.valueOf(pref));
    }

    @Override
    public int getColorPreference(String dtiKey, int def)
    {
        Utilities.checkNull(dtiKey, DTI_KEY);
        return myPreferences.getInt(getPrefKeyForType(dtiKey, PreferenceType.COLOR),
                myPreferences.getInt(getPrefKeyForDefaultType(dtiKey, PreferenceType.COLOR), def));
    }

    @Override
    public int getOpacityPreference(String dtiKey, int def)
    {
        Utilities.checkNull(dtiKey, DTI_KEY);
        return myPreferences.getInt(getPrefKeyForType(dtiKey, PreferenceType.OPACITY),
                myPreferences.getInt(getPrefKeyForDefaultType(dtiKey, PreferenceType.OPACITY), def));
    }

    @Override
    public PlayState getPlayStatePreference(String dtiKey)
    {
        Utilities.checkNull(dtiKey, DTI_KEY);
        String prefString = myPreferences.getString(getPrefKeyForType(dtiKey, PreferenceType.PLAY_STATE),
                myPreferences.getString(getPrefKeyForDefaultType(dtiKey, PreferenceType.PLAY_STATE), PlayState.STOP.toString()));
        PlayState playState = EnumUtilities.fromString(PlayState.class, prefString);

        // To avoid unintended and non-obvious resource usage, don't restore a
        // paused state.
        if (playState == PlayState.PAUSE)
        {
            playState = PlayState.STOP;
        }
        return playState;
    }

    @Override
    public boolean isVisiblePreference(String dtiKey)
    {
        return isVisiblePreference(dtiKey, true);
    }

    @Override
    public boolean isVisiblePreference(String dtiKey, boolean def)
    {
        Utilities.checkNull(dtiKey, DTI_KEY);
        return myPreferences.getBoolean(getPrefKeyForType(dtiKey, PreferenceType.VISIBILITY),
                myPreferences.getBoolean(getPrefKeyForDefaultType(dtiKey, PreferenceType.VISIBILITY), def));
    }

    @Override
    public void removePreferences(String dtiKey)
    {
        ourEventExecutorService.execute(new RemovePreferencesWorker(dtiKey, PreferenceType.values()));
    }

    @Override
    public void removePreferences(String dtiKey, PreferenceType... typeToRemove)
    {
        ourEventExecutorService.execute(new RemovePreferencesWorker(dtiKey, typeToRemove));
    }

    @Override
    public void removePreferencesForPrefix(String dtiKeyPrefix)
    {
        ourEventExecutorService.execute(new RemovePreferencesByPrefixWorker(dtiKeyPrefix, PreferenceType.values()));
    }

    @Override
    public void removePreferencesForPrefix(String dtiKeyPrefix, PreferenceType... typeToRemove)
    {
        ourEventExecutorService.execute(new RemovePreferencesByPrefixWorker(dtiKeyPrefix, typeToRemove));
    }

    @Override
    public void setBooleanPreference(BooleanModel property, String dtiKey)
    {
        assert EventQueue.isDispatchThread();
        Utilities.checkNull(dtiKey, DTI_KEY);
        myPreferences.putBoolean(getPrefKeyForType(dtiKey, PreferenceType.BOOLEAN) + "::" + property.getName(),
                property.get().booleanValue(), this);
    }

    @Override
    public void setPlayStatePreference(final String dtiKey, final PlayState playState)
    {
        String key = getPrefKeyForType(dtiKey, PreferenceType.PLAY_STATE);
        if (playState == PlayState.STOP)
        {
            myPreferences.remove(key, this);
        }
        else
        {
            myPreferences.putString(key, playState.toString(), this);
        }
    }

    /**
     * Creates the data type change listener.
     *
     * @return the event listener
     */
    private EventListener<AbstractDataTypeInfoChangeEvent> createDataTypeChangeListener()
    {
        return event ->
        {
            if (event instanceof DataTypeVisibilityChangeEvent)
            {
                ourEventExecutorService.execute(new DataTypeInfoVisibilityChangeWorker((DataTypeVisibilityChangeEvent)event));
            }
            else if (event instanceof DataTypeInfoColorChangeEvent)
            {
                ourEventExecutorService.execute(new DataTypeInfoColorChangeWorker((DataTypeInfoColorChangeEvent)event));
            }
        };
    }

    /**
     * The Class DataTypeInfoChangeWorker.
     */
    private class DataTypeInfoColorChangeWorker implements Runnable
    {
        /** The event. */
        private final DataTypeInfoColorChangeEvent myEvent;

        /**
         * Instantiates a new data type info change worker.
         *
         * @param event the event
         */
        public DataTypeInfoColorChangeWorker(DataTypeInfoColorChangeEvent event)
        {
            myEvent = event;
        }

        @Override
        public void run()
        {
            int newAlpha = myEvent.getColor().getAlpha();
            String alphaPrefKey = getPrefKeyForType(myEvent.getDataTypeKey(), PreferenceType.OPACITY);
            myPreferences.putInt(alphaPrefKey, newAlpha, DataTypeInfoPreferenceAssistantImpl.this);

            if (!myEvent.isOpacityChangeOnly())
            {
                Color newColor = myEvent.getColor();
                String colorPrefKey = getPrefKeyForType(myEvent.getDataTypeKey(), PreferenceType.COLOR);
                myPreferences.putInt(colorPrefKey, newColor.getRGB(), DataTypeInfoPreferenceAssistantImpl.this);
            }
        }
    }

    /**
     * The Class DataTypeInfoChangeWorker.
     */
    private class DataTypeInfoVisibilityChangeWorker implements Runnable
    {
        /** The event. */
        private final DataTypeVisibilityChangeEvent myEvent;

        /**
         * Instantiates a new data type info change worker.
         *
         * @param event the event
         */
        public DataTypeInfoVisibilityChangeWorker(DataTypeVisibilityChangeEvent event)
        {
            myEvent = event;
        }

        @Override
        public void run()
        {
            String prefKey = getPrefKeyForType(myEvent.getDataTypeKey(), PreferenceType.VISIBILITY);
            if (myEvent.isSavePreference())
            {
                boolean visible = myEvent.isVisible();
                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace("Writing visibility preference for DTIKey: " + myEvent.getDataTypeKey() + " Value: " + visible);
                }
                if (visible == myPreferences
                        .getBoolean(getPrefKeyForDefaultType(myEvent.getDataTypeKey(), PreferenceType.VISIBILITY), true))
                {
                    myPreferences.remove(prefKey, DataTypeInfoPreferenceAssistantImpl.this);
                }
                else
                {
                    myPreferences.putBoolean(prefKey, visible, DataTypeInfoPreferenceAssistantImpl.this);
                }
            }
            else
            {
                myPreferences.remove(prefKey, DataTypeInfoPreferenceAssistantImpl.this);
            }
        }
    }

    /**
     * The Class DataTypeInfoChangeWorker.
     */
    private class RemovePreferencesByPrefixWorker implements Runnable
    {
        /** The DTI key. */
        private final String myKeyPrefix;

        /** The Types. */
        private final PreferenceType[] myTypes;

        /**
         * Instantiates a new data type info change worker.
         *
         * @param dtiKey the dti key
         * @param types the types
         */
        public RemovePreferencesByPrefixWorker(String dtiKey, PreferenceType... types)
        {
            myKeyPrefix = dtiKey;
            myTypes = types;
        }

        @Override
        public void run()
        {
            if (myTypes != null && myTypes.length > 0)
            {
                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace("Remove DataTypeInfo On Start Preferences By Prefix: " + myKeyPrefix + " For Types: "
                            + toTypeList(myTypes));
                }
                Set<String> prefKeys = New.set(myPreferences.keysWithPrefix(myKeyPrefix));

                for (PreferenceType type : myTypes)
                {
                    Set<String> prefKeysOfType = New.set(myPreferences.keysWithSuffix(type.toString()));
                    Set<String> keySetToProcess = New.set(prefKeys);
                    keySetToProcess.retainAll(prefKeysOfType);

                    for (String prefKey : keySetToProcess)
                    {
                        if (LOGGER.isTraceEnabled())
                        {
                            LOGGER.trace("Remove DataTypeInfo On Start Preferences: " + prefKey);
                        }
                        myPreferences.remove(prefKey, DataTypeInfoPreferenceAssistantImpl.this);
                    }
                }
            }
        }
    }

    /**
     * The Class DataTypeInfoChangeWorker.
     */
    private class RemovePreferencesWorker implements Runnable
    {
        /** The DTI key. */
        private final String myDTIKey;

        /** The Types. */
        private final PreferenceType[] myTypes;

        /**
         * Instantiates a new data type info change worker.
         *
         * @param dtiKey the dti key
         * @param types the types
         */
        public RemovePreferencesWorker(String dtiKey, PreferenceType... types)
        {
            myDTIKey = dtiKey;
            myTypes = types;
        }

        @Override
        public void run()
        {
            if (myTypes != null && myTypes.length > 0)
            {
                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace("Remove DataTypeInfo On Start Preferences For Key: " + myDTIKey + " For Types: "
                            + toTypeList(myTypes));
                }
                for (PreferenceType type : myTypes)
                {
                    String prefKey = getPrefKeyForType(myDTIKey, type);
                    if (LOGGER.isTraceEnabled())
                    {
                        LOGGER.trace("Remove DataTypeInfo On Start Preferences: " + prefKey);
                    }
                    myPreferences.remove(prefKey, DataTypeInfoPreferenceAssistantImpl.this);
                }
            }
        }
    }
}
