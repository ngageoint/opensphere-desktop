package io.opensphere.controlpanels.layers.tagmanager;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import io.opensphere.controlpanels.layers.event.ShowLayerSetManagerEvent;
import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.util.ChangeSupport.Callback;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.event.DataTypeInfoTagsChangeEvent;

/**
 * The Class TagManagerController.
 */
public class TagManagerController
{
    /** The change executor. */
    private final ProcrastinatingExecutor myChangeExecutor = new ProcrastinatingExecutor("TagManagerController::Dispatch", 300,
            500);

    /** The change support. */
    private final WeakChangeSupport<TagManagerControllerListener> myChangeSupport;

    /** The DGI. */
    private final DataGroupInfo myDGI;

    /** The DTI. */
    private final DataTypeInfo myDTI;

    /** The active group saved sets changed listener. */
    private final EventListener<DataTypeInfoTagsChangeEvent> myTagsChangedListener = new EventListener<DataTypeInfoTagsChangeEvent>()
    {
        @Override
        public void notify(DataTypeInfoTagsChangeEvent event)
        {
            handleActiveDataGroupSavedSetsChanged(event);
        }
    };

    /** The toolbox. */
    private final Toolbox myToolbox;

    /**
     * Instantiates a new tag manager controller.
     *
     * @param tb the {@link Toolbox}
     * @param dgi the {@link DataGroupInfo}
     */
    public TagManagerController(Toolbox tb, DataGroupInfo dgi)
    {
        this(tb, dgi, null);
    }

    /**
     * Instantiates a new tag manager controller.
     *
     * @param tb the {@link Toolbox}
     * @param dti the {@link DataTypeInfo}
     */
    public TagManagerController(Toolbox tb, DataTypeInfo dti)
    {
        this(tb, null, dti);
    }

    /**
     * Instantiates a new active layer book mark panel.
     *
     * @param tb the {@link Toolbox}
     * @param dgi the {@link DataGroupInfo}
     * @param dti the {@link DataTypeInfo}
     */
    private TagManagerController(Toolbox tb, DataGroupInfo dgi, DataTypeInfo dti)
    {
        myToolbox = tb;
        myDTI = dti;
        myDGI = dgi;
        myChangeSupport = new WeakChangeSupport<>();
        myToolbox.getEventManager().subscribe(DataTypeInfoTagsChangeEvent.class, myTagsChangedListener);
    }

    /**
     * Adds the {@link TagManagerControllerListener}.
     *
     * @param listener the {@link TagManagerControllerListener}
     */
    public void addListener(TagManagerControllerListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    /**
     * Adds the tag.
     *
     * @param tag the tag
     */
    public void addTag(String tag)
    {
        if (myDGI != null)
        {
            TagUtility.tagDataGroup(myDGI, tag, false, this);
        }
        else if (myDTI != null)
        {
            myDTI.addTag(tag, this);
        }
    }

    /**
     * Delete tag.
     *
     * @param tag the tag to remove
     */
    public void deleteTag(String tag)
    {
        if (myDGI != null)
        {
            TagUtility.removeTag(myDGI, tag, false, this);
        }
        else if (myDTI != null)
        {
            myDTI.removeTag(tag, this);
        }
    }

    /**
     * Gets the tags.
     *
     * @return the tags
     */
    public List<String> getTags()
    {
        List<String> tagList = New.list();
        if (myDGI != null)
        {
            Set<String> tagSet = New.set();
            for (DataTypeInfo dti : myDGI.getMembers(false))
            {
                tagSet.addAll(dti.getTags());
            }
            tagList.addAll(tagSet);
        }
        else if (myDTI != null)
        {
            tagList.addAll(myDTI.getTags());
        }
        Collections.sort(tagList);
        return tagList;
    }

    /**
     * Removes the {@link TagManagerControllerListener}.
     *
     * @param listener the {@link TagManagerControllerListener}
     */
    public void removeListener(TagManagerControllerListener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    /**
     * Show tags manager.
     */
    public void showTagsManager()
    {
        myToolbox.getEventManager().publishEvent(new ShowLayerSetManagerEvent());
    }

    /**
     * Fire tags changed.
     */
    private void fireTagsChanged()
    {
        myChangeSupport.notifyListeners(new Callback<TagManagerController.TagManagerControllerListener>()
        {
            @Override
            public void notify(TagManagerControllerListener listener)
            {
                listener.tagsChanged();
            }
        }, myChangeExecutor);
    }

    /**
     * Handle active data group saved sets changed.
     *
     * @param event the event
     */
    private void handleActiveDataGroupSavedSetsChanged(DataTypeInfoTagsChangeEvent event)
    {
        fireTagsChanged();
    }

    /**
     * The listener interface for the {@link TagManagerController}.
     */
    @FunctionalInterface
    public interface TagManagerControllerListener
    {
        /**
         * Tags changed.
         */
        void tagsChanged();
    }
}
