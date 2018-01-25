package io.opensphere.controlpanels.animation.controller;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.controlpanels.animation.model.AnimationModel;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.ListDataEvent;
import io.opensphere.core.util.ListDataListener;
import io.opensphere.core.util.collections.New;

/**
 * Unit test for {@link LoadTimeAdapter}.
 */
public class LoadTimeAdapterTest
{
    /**
     * Tests when the load span and loop span are the same.
     */
    @Test
    public void testLoopSpanChanged()
    {
        EasyMockSupport support = new EasyMockSupport();

        List<ListDataEvent<TimeSpan>> events = New.list();
        ListDataListener<TimeSpan> listener = createChangeListener(support, events);

        support.replayAll();

        MockTimeManager manager = new MockTimeManager();
        AnimationModel animationModel = new AnimationModel();

        TimeSpan firstLoopSpan = TimeSpan.get(System.currentTimeMillis() - 20000, System.currentTimeMillis() - 10000);
        animationModel.getLoopSpan().set(firstLoopSpan);
        LoadTimeAdapter adapter = new LoadTimeAdapter(manager, animationModel);
        adapter.open();
        manager.getLoadTimeSpans().addChangeListener(listener);

        TimeSpan loopSpan = TimeSpan.get(System.currentTimeMillis() - 10000, System.currentTimeMillis());
        animationModel.getLoopSpan().set(loopSpan);

        assertEquals(1, manager.getLoadTimeSpans().size());
        assertEquals(loopSpan, manager.getLoadTimeSpans().get(0));

        assertEquals(1, events.size());
        assertEquals(1, events.get(0).getChangedElements().size());
        assertEquals(loopSpan, events.get(0).getChangedElements().get(0));

        adapter.close();

        support.verifyAll();
    }

    /**
     * Tests when the load span and loop span are the same.
     */
    @Test
    public void testInitial()
    {
        EasyMockSupport support = new EasyMockSupport();

        support.replayAll();

        MockTimeManager manager = new MockTimeManager();
        AnimationModel animationModel = new AnimationModel();

        TimeSpan loopSpan = TimeSpan.get(System.currentTimeMillis() - 10000, System.currentTimeMillis());
        animationModel.getLoopSpan().set(loopSpan);

        LoadTimeAdapter adapter = new LoadTimeAdapter(manager, animationModel);
        adapter.open();

        assertEquals(1, manager.getLoadTimeSpans().size());
        assertEquals(loopSpan, manager.getLoadTimeSpans().get(0));

        adapter.close();

        support.verifyAll();
    }

    /**
     * Tests when the load span changes and is disjoint from the loop span.
     */
    @Test
    public void testLoadSpanChanged()
    {
        EasyMockSupport support = new EasyMockSupport();

        List<ListDataEvent<TimeSpan>> events = New.list();
        ListDataListener<TimeSpan> listener = createChangeListener(support, events);

        support.replayAll();

        MockTimeManager manager = new MockTimeManager();
        AnimationModel animationModel = new AnimationModel();

        TimeSpan firstLoopSpan = TimeSpan.get(System.currentTimeMillis() - 20000, System.currentTimeMillis() - 10000);
        animationModel.getLoopSpan().set(firstLoopSpan);
        TimeSpan loadSpan = TimeSpan.get(System.currentTimeMillis() - 10000, System.currentTimeMillis());
        animationModel.loadIntervalsProperty().add(loadSpan);
        LoadTimeAdapter adapter = new LoadTimeAdapter(manager, animationModel);
        adapter.open();
        manager.getLoadTimeSpans().addChangeListener(listener);

        loadSpan = TimeSpan.get(System.currentTimeMillis() - 5000, System.currentTimeMillis());
        animationModel.loadIntervalsProperty().set(0, loadSpan);

        assertEquals(1, manager.getLoadTimeSpans().size());
        assertEquals(loadSpan, manager.getLoadTimeSpans().get(0));

        assertEquals(1, events.size());
        assertEquals(1, events.get(0).getChangedElements().size());
        assertEquals(loadSpan, events.get(0).getChangedElements().get(0));

        adapter.close();

        support.verifyAll();
    }

    /**
     * Tests when the loop span changes but the load and loop span are disjoint.
     */
    @Test
    public void testLoopSpanChangedDisjoint()
    {
        EasyMockSupport support = new EasyMockSupport();

        @SuppressWarnings("unchecked")
        ListDataListener<TimeSpan> listener = support.createMock(ListDataListener.class);

        support.replayAll();

        MockTimeManager manager = new MockTimeManager();
        AnimationModel animationModel = new AnimationModel();

        TimeSpan firstLoopSpan = TimeSpan.get(System.currentTimeMillis() - 20000, System.currentTimeMillis() - 10000);
        animationModel.getLoopSpan().set(firstLoopSpan);

        TimeSpan loadSpan = TimeSpan.get(System.currentTimeMillis() - 10000, System.currentTimeMillis());
        animationModel.loadIntervalsProperty().add(loadSpan);
        LoadTimeAdapter adapter = new LoadTimeAdapter(manager, animationModel);
        adapter.open();
        manager.getLoadTimeSpans().addChangeListener(listener);

        TimeSpan loopSpan = TimeSpan.get(System.currentTimeMillis() - 10000, System.currentTimeMillis());
        animationModel.getLoopSpan().set(loopSpan);

        assertEquals(1, manager.getLoadTimeSpans().size());
        assertEquals(loadSpan, manager.getLoadTimeSpans().get(0));

        adapter.close();

        support.verifyAll();
    }

    /**
     * Tests when the user creates a load span disjoint from loop span.
     */
    @Test
    public void testDisjointLoadSpan()
    {
        EasyMockSupport support = new EasyMockSupport();

        List<ListDataEvent<TimeSpan>> events = New.list();
        ListDataListener<TimeSpan> listener = createChangeListener(support, events);

        support.replayAll();

        MockTimeManager manager = new MockTimeManager();
        AnimationModel animationModel = new AnimationModel();

        TimeSpan firstLoopSpan = TimeSpan.get(System.currentTimeMillis() - 20000, System.currentTimeMillis() - 10000);
        animationModel.getLoopSpan().set(firstLoopSpan);

        LoadTimeAdapter adapter = new LoadTimeAdapter(manager, animationModel);
        adapter.open();
        manager.getLoadTimeSpans().addChangeListener(listener);

        TimeSpan loadSpan = TimeSpan.get(System.currentTimeMillis() - 5000, System.currentTimeMillis());
        animationModel.loadIntervalsProperty().add(loadSpan);

        assertEquals(1, manager.getLoadTimeSpans().size());
        assertEquals(loadSpan, manager.getLoadTimeSpans().get(0));

        assertEquals(1, events.size());
        assertEquals(1, events.get(0).getChangedElements().size());
        assertEquals(loadSpan, events.get(0).getChangedElements().get(0));

        adapter.close();

        support.verifyAll();
    }

    /**
     * Tests when user creates multiple load spans.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testMultipleLoadSpan()
    {
        EasyMockSupport support = new EasyMockSupport();

        List<ListDataEvent<TimeSpan>> events = New.list();
        ListDataListener<TimeSpan> listener = createChangeListener(support, events);
        listener.elementsAdded(EasyMock.isA(ListDataEvent.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            events.add((ListDataEvent<TimeSpan>)EasyMock.getCurrentArguments()[0]);
            return null;
        });

        support.replayAll();

        MockTimeManager manager = new MockTimeManager();
        AnimationModel animationModel = new AnimationModel();

        TimeSpan firstLoopSpan = TimeSpan.get(System.currentTimeMillis() - 20000, System.currentTimeMillis() - 10000);
        animationModel.getLoopSpan().set(firstLoopSpan);

        LoadTimeAdapter adapter = new LoadTimeAdapter(manager, animationModel);
        adapter.open();
        manager.getLoadTimeSpans().addChangeListener(listener);

        TimeSpan loadSpan = TimeSpan.get(System.currentTimeMillis() - 5000, System.currentTimeMillis());
        animationModel.loadIntervalsProperty().add(loadSpan);

        assertEquals(1, manager.getLoadTimeSpans().size());
        assertEquals(loadSpan, manager.getLoadTimeSpans().get(0));

        assertEquals(1, events.size());
        assertEquals(1, events.get(0).getChangedElements().size());
        assertEquals(loadSpan, events.get(0).getChangedElements().get(0));

        TimeSpan secondLoad = TimeSpan.get(System.currentTimeMillis() - 20000, System.currentTimeMillis() - 15000);
        animationModel.loadIntervalsProperty().add(secondLoad);

        assertEquals(2, manager.getLoadTimeSpans().size());
        assertEquals(loadSpan, manager.getLoadTimeSpans().get(0));
        assertEquals(secondLoad, manager.getLoadTimeSpans().get(1));

        assertEquals(2, events.size());
        assertEquals(1, events.get(1).getChangedElements().size());
        assertEquals(secondLoad, events.get(1).getChangedElements().get(0));

        adapter.close();

        support.verifyAll();
    }

    /**
     * Tests when a load span changes and there are multiple of them.
     */
    @Test
    public void testMultipleLoadSpanChange()
    {
        EasyMockSupport support = new EasyMockSupport();

        List<ListDataEvent<TimeSpan>> events = New.list();
        ListDataListener<TimeSpan> listener = createChangeListener(support, events);

        support.replayAll();

        MockTimeManager manager = new MockTimeManager();
        AnimationModel animationModel = new AnimationModel();

        TimeSpan firstLoopSpan = TimeSpan.get(System.currentTimeMillis() - 20000, System.currentTimeMillis() - 10000);
        animationModel.getLoopSpan().set(firstLoopSpan);
        TimeSpan loadSpan = TimeSpan.get(System.currentTimeMillis() - 10000, System.currentTimeMillis());
        animationModel.loadIntervalsProperty().add(loadSpan);

        TimeSpan secondLoad = TimeSpan.get(System.currentTimeMillis() - 20000, System.currentTimeMillis() - 15000);
        animationModel.loadIntervalsProperty().add(secondLoad);

        LoadTimeAdapter adapter = new LoadTimeAdapter(manager, animationModel);
        adapter.open();
        manager.getLoadTimeSpans().addChangeListener(listener);

        loadSpan = TimeSpan.get(System.currentTimeMillis() - 5000, System.currentTimeMillis());
        animationModel.loadIntervalsProperty().set(1, loadSpan);

        assertEquals(2, manager.getLoadTimeSpans().size());
        assertEquals(loadSpan, manager.getLoadTimeSpans().get(1));

        assertEquals(1, events.size());
        assertEquals(1, events.get(0).getChangedElements().size());
        assertEquals(loadSpan, events.get(0).getChangedElements().get(0));

        adapter.close();

        support.verifyAll();
    }

    /**
     * Tests when a user deletes a load span.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testDeleteLoadSpan()
    {
        EasyMockSupport support = new EasyMockSupport();

        List<ListDataEvent<TimeSpan>> events = New.list();
        ListDataListener<TimeSpan> listener = support.createMock(ListDataListener.class);
        listener.elementsRemoved(EasyMock.isA(ListDataEvent.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            events.add((ListDataEvent<TimeSpan>)EasyMock.getCurrentArguments()[0]);
            return null;
        });

        support.replayAll();

        MockTimeManager manager = new MockTimeManager();
        AnimationModel animationModel = new AnimationModel();

        TimeSpan firstLoopSpan = TimeSpan.get(System.currentTimeMillis() - 20000, System.currentTimeMillis() - 10000);
        animationModel.getLoopSpan().set(firstLoopSpan);
        TimeSpan loadSpan = TimeSpan.get(System.currentTimeMillis() - 10000, System.currentTimeMillis());
        animationModel.loadIntervalsProperty().add(loadSpan);

        TimeSpan secondLoad = TimeSpan.get(System.currentTimeMillis() - 20000, System.currentTimeMillis() - 15000);
        animationModel.loadIntervalsProperty().add(secondLoad);

        LoadTimeAdapter adapter = new LoadTimeAdapter(manager, animationModel);
        adapter.open();
        manager.getLoadTimeSpans().addChangeListener(listener);
        animationModel.loadIntervalsProperty().remove(0);

        assertEquals(1, manager.getLoadTimeSpans().size());
        assertEquals(secondLoad, manager.getLoadTimeSpans().get(0));

        assertEquals(1, events.size());
        assertEquals(1, events.get(0).getChangedElements().size());
        assertEquals(loadSpan, events.get(0).getChangedElements().get(0));

        adapter.close();

        support.verifyAll();
    }

    /**
     * Tests when a user deletes a disjoint load span causing the load span to
     * now equal loop span.
     */
    @Test
    public void testLoadSpanLoopSpanJoin()
    {
        EasyMockSupport support = new EasyMockSupport();

        List<ListDataEvent<TimeSpan>> events = New.list();
        ListDataListener<TimeSpan> listener = createChangeListener(support, events);

        support.replayAll();

        MockTimeManager manager = new MockTimeManager();
        AnimationModel animationModel = new AnimationModel();

        TimeSpan firstLoopSpan = TimeSpan.get(System.currentTimeMillis() - 20000, System.currentTimeMillis() - 10000);
        animationModel.getLoopSpan().set(firstLoopSpan);
        TimeSpan loadSpan = TimeSpan.get(System.currentTimeMillis() - 10000, System.currentTimeMillis());
        animationModel.loadIntervalsProperty().add(loadSpan);
        LoadTimeAdapter adapter = new LoadTimeAdapter(manager, animationModel);
        adapter.open();
        manager.getLoadTimeSpans().addChangeListener(listener);

        animationModel.loadIntervalsProperty().remove(0);

        assertEquals(1, manager.getLoadTimeSpans().size());
        assertEquals(firstLoopSpan, manager.getLoadTimeSpans().get(0));

        assertEquals(1, events.size());
        assertEquals(1, events.get(0).getChangedElements().size());
        assertEquals(firstLoopSpan, events.get(0).getChangedElements().get(0));

        adapter.close();

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link ListDataListener} expected the change
     * event.
     *
     * @param support Used to create the mock.
     * @param events The change events that occurred.
     * @return The mock.
     */
    @SuppressWarnings("unchecked")
    private ListDataListener<TimeSpan> createChangeListener(EasyMockSupport support, List<ListDataEvent<TimeSpan>> events)
    {
        ListDataListener<TimeSpan> listener = support.createMock(ListDataListener.class);
        listener.elementsChanged(EasyMock.isA(ListDataEvent.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            events.add((ListDataEvent<TimeSpan>)EasyMock.getCurrentArguments()[0]);
            return null;
        });

        return listener;
    }
}
