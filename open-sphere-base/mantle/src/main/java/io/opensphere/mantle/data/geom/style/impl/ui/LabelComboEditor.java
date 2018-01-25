package io.opensphere.mantle.data.geom.style.impl.ui;

import java.awt.EventQueue;
import java.util.List;

import io.opensphere.core.event.EventListener;
import io.opensphere.core.event.EventManager;
import io.opensphere.mantle.data.AbstractDataTypeInfoChangeEvent;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.event.DataTypeInfoMetaDataKeyAddedChangeEvent;
import io.opensphere.mantle.data.event.DataTypeInfoMetaDataKeyRemovedChangeEvent;
import io.opensphere.mantle.data.geom.style.MutableVisualizationStyle;
import io.opensphere.mantle.data.geom.util.ListSupport;

/** Label combo editor. */
public class LabelComboEditor extends MultiComboEditor
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The data type. */
    private final DataTypeInfo myDataType;

    /** The listener for data type column changes. */
    private final EventListener<AbstractDataTypeInfoChangeEvent> myDataTypeListener = this::handleEvent;

    /**
     * Constructor.
     *
     * @param eventManager the event manager
     * @param label the label
     * @param style the style
     * @param paramKey the param key
     * @param dataType the data type
     * @param isNameAbove whether the name label should be placed above (vs.
     *            left of) the editor components.
     */
    public LabelComboEditor(EventManager eventManager, PanelBuilder label, MutableVisualizationStyle style, String paramKey,
            DataTypeInfo dataType, boolean isNameAbove)
    {
        super(label, style, paramKey);
        myDataType = dataType;
        setListSupport(new ListSupport('\\', ','));
        setNameAbove(isNameAbove);
        setup(false, dataType.getMetaDataInfo().getKeyNames());
        eventManager.subscribe(AbstractDataTypeInfoChangeEvent.class, myDataTypeListener);
    }

    /**
     * Handles AbstractDataTypeInfoChangeEvent.
     *
     * @param event the event
     */
    private void handleEvent(AbstractDataTypeInfoChangeEvent event)
    {
        if (event.getDataTypeKey().equals(myDataType.getTypeKey()) && (event instanceof DataTypeInfoMetaDataKeyAddedChangeEvent
                || event instanceof DataTypeInfoMetaDataKeyRemovedChangeEvent))
        {
            final List<String> keyNames = myDataType.getMetaDataInfo().getKeyNames();
            EventQueue.invokeLater(() -> setOptions(false, keyNames));
        }
    }
}
