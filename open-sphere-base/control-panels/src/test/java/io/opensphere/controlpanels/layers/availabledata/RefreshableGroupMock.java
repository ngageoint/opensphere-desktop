package io.opensphere.controlpanels.layers.availabledata;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.swing.tree.MutableTreeNode;

import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.Quadrilateral;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.mantle.data.AbstractDataGroupInfoChangeEvent;
import io.opensphere.mantle.data.DataGroupActivationProperty;
import io.opensphere.mantle.data.DataGroupEvent;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataGroupInfoAssistant;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.RefreshableDataGroupInfo;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.event.EventHandler;

/**
 * A mock class used to test the RefreshTreeCellRenderer.
 *
 */
public class RefreshableGroupMock implements DataGroupInfo, RefreshableDataGroupInfo
{
    /**
     * The countdown latch.
     */
    private CountDownLatch myLatch;

    /**
     * Default constructor.
     */
    public RefreshableGroupMock()
    {
    }

    /**
     * Constructor with a countdown latch.
     *
     * @param latch The latch to countdown on refresh.
     */
    public RefreshableGroupMock(CountDownLatch latch)
    {
        myLatch = latch;
    }

    @Override
    public DataGroupActivationProperty activationProperty()
    {
        return null;
    }

    @Override
    public void addChild(DataGroupInfo dgi, Object source)
    {
    }

    @Override
    public void addMember(DataTypeInfo dti, Object source)
    {
    }

    @Override
    public void clearMembers(boolean recursive, Object source)
    {
    }

    @Override
    public Set<DataGroupInfo> createGroupSet(Predicate<? super DataGroupInfo> nodeFilter)
    {
        return null;
    }

    @Override
    public MutableTreeNode createTreeNode()
    {
        return null;
    }

    @Override
    public MutableTreeNode createTreeNode(Comparator<? super DataGroupInfo> comparator,
            Predicate<? super DataGroupInfo> nodeFilter)
    {
        return null;
    }

    @Override
    public Set<DataTypeInfo> findMembers(Predicate<? super DataTypeInfo> dtiFilter, boolean recursive, boolean stopOnFirstFound)
    {
        return null;
    }

    @Override
    public void fireGroupInfoChangeEvent(AbstractDataGroupInfoChangeEvent e)
    {
    }

    @Override
    public DataGroupInfoAssistant getAssistant()
    {
        return null;
    }

    @Override
    public WeakChangeSupport<Consumer<? super DataGroupInfo>> getChildAddedChangeSupport()
    {
        return null;
    }

    @Override
    public List<DataGroupInfo> getChildren()
    {
        return null;
    }

    @Override
    public Set<String> getDataCategories()
    {
        return null;
    }

    @Override
    public void getDescendants(Collection<DataGroupInfo> descendants)
    {
    }

    @Override
    public String getDisplayName()
    {
        return null;
    }

    @Override
    public String getDisplayNameWithPostfixTopParentName()
    {
        return null;
    }

    @Override
    public Boolean getExpandedByDefault()
    {
        return Boolean.TRUE;
    }

    @Override
    public DataGroupInfo getGroupById(String id)
    {
        return null;
    }

    @Override
    public String getGroupDescription()
    {
        return null;
    }

    @Override
    public String getId()
    {
        return null;
    }

    @Override
    public String getLongDisplayName()
    {
        return null;
    }

    @Override
    public DataTypeInfo getMemberById(String id, boolean recursive)
    {
        return null;
    }

    @Override
    public Set<MapVisualizationType> getMemberMapVisualizationTypes(boolean recursive)
    {
        return null;
    }

    @Override
    public Set<DataTypeInfo> getMembers(boolean recurseChildren)
    {
        return null;
    }

    @Override
    public DataGroupInfo getParent()
    {
        return null;
    }

    @Override
    public boolean getPreviewImage(ObservableValue<? super BufferedImage> observableImage)
    {
        return false;
    }

    @Override
    public String getProviderType()
    {
        return null;
    }

    @Override
    public boolean getRegion(ObservableValue<? super Quadrilateral<GeographicPosition>> observableRegion)
    {
        return false;
    }

    @Override
    public String getSummaryDescription()
    {
        return null;
    }

    @Override
    public DataGroupInfo getTopParent()
    {
        return null;
    }

    @Override
    public String getTopParentDisplayName()
    {
        return null;
    }

    @Override
    public Stream<DataGroupInfo> groupStream()
    {
        return null;
    }

    @Override
    public boolean hasChildren()
    {
        return false;
    }

    @Override
    public boolean hasDetails()
    {
        return false;
    }

    @Override
    public boolean hasFeatureTypes(boolean recursive)
    {
        return false;
    }

    @Override
    public boolean hasImageTileTypes(boolean recursive)
    {
        return false;
    }

    @Override
    public boolean hasMember(DataTypeInfo dti, boolean recursive)
    {
        return false;
    }

    @Override
    public boolean hasMembers(boolean recursive)
    {
        return false;
    }

    @Override
    public boolean hasTimelineMember(boolean recursive)
    {
        return false;
    }

    @Override
    public boolean hasVisualizationStyles(boolean recursive)
    {
        return false;
    }

    @Override
    public boolean isDragAndDrop()
    {
        return false;
    }

    @Override
    public boolean isFlattenable()
    {
        return false;
    }

    @Override
    public boolean isParentAncestor(DataGroupInfo dgi)
    {
        return false;
    }

    @Override
    public boolean isRootNode()
    {
        return false;
    }

    @Override
    public boolean isTaggable()
    {
        return false;
    }

    @Override
    public void notifyChildAddedListeners(DataGroupInfo child)
    {
    }

    @Override
    public int numChildren()
    {
        return 0;
    }

    @Override
    public int numMembers(boolean recursive)
    {
        return 0;
    }

    @Override
    public void refresh()
    {
        myLatch.countDown();
    }

    @Override
    public boolean removeChild(DataGroupInfo dgi, Object source)
    {
        return false;
    }

    @Override
    public boolean removeChildKeepActive(DataGroupInfo dgi, Object source)
    {
        return false;
    }

    @Override
    public boolean removeMember(DataTypeInfo dti, boolean recursive, Object source)
    {
        return false;
    }

    @Override
    public void setDisplayName(String name, Object source)
    {
    }

    @Override
    public void setExpandedByDefault(Boolean expandedByDefault)
    {
    }

    @Override
    public void setGroupVisible(Predicate<? super DataTypeInfo> dtiFilter, boolean visible, boolean recursive, Object source)
    {
    }

    @Override
    public void setId(String id, Object source)
    {
    }

    @Override
    public void setParent(DataGroupInfo parent)
    {
    }

    @Override
    public boolean userActivationStateControl()
    {
        return false;
    }

    @Override
    public boolean userDeleteControl()
    {
        return false;
    }

    @Override
    public boolean usesStyles(boolean recursive)
    {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.DataGroupInfo#setActivationSupported(boolean)
     */
    @Override
    public void setActivationSupported(boolean pActivationSupported)
    {
        // intentionally blank
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.DataGroupInfo#isActivationSupported()
     */
    @Override
    public boolean isActivationSupported()
    {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.DataGroupInfo#activationSupportProperty()
     */
    @Override
    public BooleanProperty activationSupportProperty()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.DataGroupInfo#setTriggeringSupported(boolean)
     */
    @Override
    public void setTriggeringSupported(boolean pTriggeringSupported)
    {
        // intentionally blank
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.DataGroupInfo#isTriggeringSupported()
     */
    @Override
    public boolean isTriggeringSupported()
    {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.DataGroupInfo#triggerSupportProperty()
     */
    @Override
    public BooleanProperty triggerSupportProperty()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.DataGroupInfo#triggeredProperty()
     */
    @Override
    public ObjectProperty<EventHandler<DataGroupEvent>> triggeredProperty()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.DataGroupInfo#setTriggerHandler(javafx.event.EventHandler)
     */
    @Override
    public void setTriggerHandler(EventHandler<DataGroupEvent> pHandler)
    {
        // intentionally blank
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.DataGroupInfo#trigger(DataGroupEvent)
     */
    @Override
    public void trigger(DataGroupEvent pEvent)
    {
        // intentionally blank
    }

    @Override
    public boolean isPreserveChildOrder()
    {
        return false;
    }

    @Override
    public void setPreserveChildOrder(boolean preserveChildOrder)
    {
    }

    @Override
    public boolean isHidden()
    {
        return false;
    }

    @Override
    public void setHidden(boolean hidden)
    {
    }
}
