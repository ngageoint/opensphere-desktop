package io.opensphere.mantle.data;

import java.awt.EventQueue;

import io.opensphere.core.util.ObservableValueListenerHandle;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.swing.input.model.BooleanModel;

/**
 * A binding that loads a boolean model value from the preferences and listens
 * for changes to the model to save back to the preferences.
 */
public class PropertyPreferenceBinding extends ObservableValueListenerHandle<Boolean>
{
    /** The data type info preference assistant. */
    private final DataTypeInfoPreferenceAssistant myDataTypeInfoPreferenceAssistant;

    /** The data type info key. */
    private final String myDtiKey;

    /**
     * Constructor.
     *
     * @param dtiKey The data type info key.
     * @param model The model.
     * @param dataTypeInfoPreferenceAssistant The data type info preference
     *            assistant.
     */
    public PropertyPreferenceBinding(String dtiKey, BooleanModel model,
            DataTypeInfoPreferenceAssistant dataTypeInfoPreferenceAssistant)
    {
        super(model, (v, o, n) -> dataTypeInfoPreferenceAssistant.setBooleanPreference(model, dtiKey));

        myDtiKey = Utilities.checkNull(dtiKey, "dtiKey");
        myDataTypeInfoPreferenceAssistant = Utilities.checkNull(dataTypeInfoPreferenceAssistant,
                "dataTypeInfoPreferenceAssistant");
    }

    @Override
    public void open()
    {
        super.open();

        EventQueue.invokeLater(
            () -> myDataTypeInfoPreferenceAssistant.getBooleanPreference((BooleanModel)getObservable(), myDtiKey));
    }
}
