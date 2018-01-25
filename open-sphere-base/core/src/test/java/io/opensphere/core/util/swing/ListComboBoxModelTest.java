package io.opensphere.core.util.swing;

import java.util.Arrays;
import java.util.List;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.junit.Assert;
import org.junit.Test;

/** Test {@link ListComboBoxModel}. */
public class ListComboBoxModelTest
{
    /** Test constructor. */
    @Test
    public void testListComboBoxModel()
    {
        ListComboBoxModel<Object> model = new ListComboBoxModel<>();
        Assert.assertEquals(0, model.getSize());
    }

    /** Test constructor. */
    @Test
    public void testListComboBoxModelEArray()
    {
        Integer[] arr = new Integer[] { Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2), };
        ListComboBoxModel<Integer> model = new ListComboBoxModel<>(arr);
        Assert.assertEquals(3, model.getSize());
        Assert.assertEquals(arr[0], model.getElementAt(0));
        Assert.assertEquals(arr[1], model.getElementAt(1));
        Assert.assertEquals(arr[2], model.getElementAt(2));
    }

    /** Test constructor. */
    @Test
    public void testListComboBoxModelCollectionOfQextendsE()
    {
        List<Integer> list = Arrays.asList(Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2));
        ListComboBoxModel<Integer> model = new ListComboBoxModel<>(list);
        Assert.assertEquals(3, model.getSize());
        Assert.assertEquals(list.get(0), model.getElementAt(0));
        Assert.assertEquals(list.get(1), model.getElementAt(1));
        Assert.assertEquals(list.get(2), model.getElementAt(2));
    }

    /** Test {@link ListComboBoxModel#setSelectedItem(Object)}. */
    @SuppressWarnings("deprecation")
    @Test
    public void testSetSelectedItem()
    {
        List<Integer> list = Arrays.asList(Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2));
        ListComboBoxModel<Integer> model = new ListComboBoxModel<>(list);

        ListDataListener listener = EasyMock.createMock(ListDataListener.class);
        listener.contentsChanged(eqEvent(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, -1, -1)));
        EasyMock.expectLastCall().times(2);
        EasyMock.replay(listener);
        model.addListDataListener(listener);

        Assert.assertEquals(Integer.valueOf(0), model.getSelectedItem());

        model.setSelectedItem(Integer.valueOf(2));

        Assert.assertEquals(Integer.valueOf(2), model.getSelectedItem());

        model.setSelectedItem(null);

        Assert.assertNull(model.getSelectedItem());
        EasyMock.verify(listener);
    }

    /**
     * Test {@link ListComboBoxModel#setSelectedItem(Object)} with a bad
     * argument.
     */
    @SuppressWarnings("deprecation")
    @Test(expected = IllegalArgumentException.class)
    public void testSetSelectedItemBad()
    {
        List<Integer> list = Arrays.asList(Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2));
        ListComboBoxModel<Integer> model = new ListComboBoxModel<>(list);

        ListDataListener listener = EasyMock.createMock(ListDataListener.class);
        EasyMock.replay(listener);
        model.addListDataListener(listener);

        model.setSelectedItem(Integer.valueOf(3));
    }

    /** Test {@link ListComboBoxModel#setSelectedElement(Object)}. */
    @Test
    public void testSetSelectedElement()
    {
        List<Integer> list = Arrays.asList(Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2));
        ListComboBoxModel<Integer> model = new ListComboBoxModel<>(list);

        ListDataListener listener = EasyMock.createMock(ListDataListener.class);
        listener.contentsChanged(eqEvent(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, -1, -1)));
        EasyMock.expectLastCall().times(2);
        EasyMock.replay(listener);
        model.addListDataListener(listener);

        Assert.assertEquals(Integer.valueOf(0), model.getSelectedItem());

        model.setSelectedElement(Integer.valueOf(2));

        Assert.assertEquals(Integer.valueOf(2), model.getSelectedItem());

        model.setSelectedElement(null);

        Assert.assertNull(model.getSelectedItem());
        EasyMock.verify(listener);
    }

    /**
     * Test {@link ListComboBoxModel#setSelectedElement(Object)} with a bad
     * argument.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSetSelectedElementBad()
    {
        List<Integer> list = Arrays.asList(Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2));
        ListComboBoxModel<Integer> model = new ListComboBoxModel<>(list);

        ListDataListener listener = EasyMock.createMock(ListDataListener.class);
        EasyMock.replay(listener);
        model.addListDataListener(listener);

        model.setSelectedElement(Integer.valueOf(3));
    }

    /** Test {@link ListComboBoxModel#getIndexOf(Object)}. */
    @Test
    public void testGetIndexOf()
    {
        List<Integer> list = Arrays.asList(Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2));
        ListComboBoxModel<Integer> model = new ListComboBoxModel<>(list);

        Assert.assertEquals(0, model.getIndexOf(Integer.valueOf(0)));
        Assert.assertEquals(1, model.getIndexOf(Integer.valueOf(1)));
        Assert.assertEquals(2, model.getIndexOf(Integer.valueOf(2)));
        Assert.assertEquals(-1, model.getIndexOf(Integer.valueOf(4)));
        Assert.assertEquals(-1, model.getIndexOf(null));
    }

    /** Test {@link ListComboBoxModel#addElement(Object)}. */
    @Test
    public void testAddElement()
    {
        List<Integer> list = Arrays.asList(Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2));
        ListComboBoxModel<Integer> model = new ListComboBoxModel<>(list);

        ListDataListener listener = EasyMock.createMock(ListDataListener.class);
        listener.intervalAdded(eqEvent(new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, 3, 3)));
        EasyMock.replay(listener);
        model.addListDataListener(listener);

        model.addElement(Integer.valueOf(3));

        Assert.assertEquals(Integer.valueOf(3), model.getElementAt(3));
        EasyMock.verify(listener);
    }

    /** Test {@link ListComboBoxModel#insertElementAt(Object, int)}. */
    @Test
    public void testInsertElementAt()
    {
        List<Integer> list = Arrays.asList(Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2));
        ListComboBoxModel<Integer> model = new ListComboBoxModel<>(list);

        ListDataListener listener = EasyMock.createMock(ListDataListener.class);
        listener.intervalAdded(eqEvent(new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, 0, 0)));
        EasyMock.replay(listener);
        model.addListDataListener(listener);

        model.insertElementAt(Integer.valueOf(3), 0);

        Assert.assertEquals(Integer.valueOf(3), model.getElementAt(0));
        Assert.assertEquals(Integer.valueOf(0), model.getElementAt(1));
        Assert.assertEquals(Integer.valueOf(1), model.getElementAt(2));
        Assert.assertEquals(Integer.valueOf(2), model.getElementAt(3));

        EasyMock.verify(listener);
        EasyMock.reset(listener);
        listener.intervalAdded(eqEvent(new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, 1, 1)));
        EasyMock.replay(listener);

        model.insertElementAt(Integer.valueOf(4), 1);

        Assert.assertEquals(Integer.valueOf(3), model.getElementAt(0));
        Assert.assertEquals(Integer.valueOf(4), model.getElementAt(1));
        Assert.assertEquals(Integer.valueOf(0), model.getElementAt(2));
        Assert.assertEquals(Integer.valueOf(1), model.getElementAt(3));
        Assert.assertEquals(Integer.valueOf(2), model.getElementAt(4));

        EasyMock.verify(listener);
        EasyMock.reset(listener);
        listener.intervalAdded(eqEvent(new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, 5, 5)));
        EasyMock.replay(listener);

        model.insertElementAt(Integer.valueOf(5), 5);

        Assert.assertEquals(Integer.valueOf(3), model.getElementAt(0));
        Assert.assertEquals(Integer.valueOf(4), model.getElementAt(1));
        Assert.assertEquals(Integer.valueOf(0), model.getElementAt(2));
        Assert.assertEquals(Integer.valueOf(1), model.getElementAt(3));
        Assert.assertEquals(Integer.valueOf(2), model.getElementAt(4));
        Assert.assertEquals(Integer.valueOf(5), model.getElementAt(5));

        EasyMock.verify(listener);
    }

    /** Test {@link ListComboBoxModel#removeElementAt(int)}. */
    @Test
    public void testRemoveElementAt()
    {
        List<Integer> list = Arrays.asList(Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2));
        ListComboBoxModel<Integer> model = new ListComboBoxModel<>(list);

        ListDataListener listener = EasyMock.createMock(ListDataListener.class);
        listener.contentsChanged(eqEvent(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, -1, -1)));
        listener.intervalRemoved(eqEvent(new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, 0, 0)));
        EasyMock.replay(listener);
        model.addListDataListener(listener);

        model.removeElementAt(0);

        Assert.assertEquals(Integer.valueOf(1), model.getElementAt(0));
        Assert.assertEquals(Integer.valueOf(2), model.getElementAt(1));

        EasyMock.verify(listener);
    }

    /** Test {@link ListComboBoxModel#removeElement(Object)}. */
    @Test
    public void testRemoveElement()
    {
        List<Integer> list = Arrays.asList(Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2));
        ListComboBoxModel<Integer> model = new ListComboBoxModel<>(list);

        ListDataListener listener = EasyMock.createMock(ListDataListener.class);
        listener.intervalRemoved(eqEvent(new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, 1, 1)));
        EasyMock.replay(listener);
        model.addListDataListener(listener);

        model.removeElement(Integer.valueOf(1));

        Assert.assertEquals(Integer.valueOf(0), model.getElementAt(0));
        Assert.assertEquals(Integer.valueOf(2), model.getElementAt(1));

        EasyMock.verify(listener);
    }

    /** Test {@link ListComboBoxModel#removeAllElements()}. */
    @Test
    public void testRemoveAllElements()
    {
        List<Integer> list = Arrays.asList(Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2));
        ListComboBoxModel<Integer> model = new ListComboBoxModel<>(list);

        ListDataListener listener = EasyMock.createMock(ListDataListener.class);
        listener.intervalRemoved(eqEvent(new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, 0, 2)));
        EasyMock.replay(listener);
        model.addListDataListener(listener);

        model.removeAllElements();

        Assert.assertEquals(0, model.getSize());

        EasyMock.verify(listener);
    }

    /**
     * Report an EasyMock matcher for the given event.
     *
     * @param expectedEvent The event.
     * @return {@code null}
     */
    private static ListDataEvent eqEvent(final ListDataEvent expectedEvent)
    {
        EasyMock.reportMatcher(new IArgumentMatcher()
        {
            @Override
            public boolean matches(Object argument)
            {
                if (argument instanceof ListDataEvent)
                {
                    return ((ListDataEvent)argument).getType() == expectedEvent.getType()
                            && ((ListDataEvent)argument).getIndex0() == expectedEvent.getIndex0()
                            && ((ListDataEvent)argument).getIndex1() == expectedEvent.getIndex1();
                }
                else
                {
                    return false;
                }
            }

            @Override
            public void appendTo(StringBuffer sb)
            {
                sb.append("matches(ListDataEvent type=").append(expectedEvent.getType()).append(" index0=")
                        .append(expectedEvent.getIndex0()).append(" index1=").append(expectedEvent.getIndex1()).append(')');
            }
        });
        return null;
    }
}
