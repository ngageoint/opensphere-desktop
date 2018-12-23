package io.opensphere.mantle.icon.impl;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.javafx.ConcurrentStringProperty;
import javafx.beans.Observable;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

/**   */
public class Test
{
    static final Logger LOG = Logger.getLogger(Test.class);

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        ObservableList<TestObject> testList = FXCollections
                .observableArrayList(object -> new Observable[] { object.getSetProperty(), object.getMemberSets() });
        testList.addListener(new ListChangeListener<TestObject>()
        {
            @Override
            public void onChanged(Change<? extends TestObject> c)
            {
                while (c.next())
                {
                    if (c.wasAdded())
                    {
                        for (TestObject testObject : c.getAddedSubList())
                        {
                            LOG.info("SOURCELIST: Item Added: " + testObject.getNameProperty().get() + " ["
                                    + testObject.getMemberSets().stream().collect(Collectors.joining(",")) + "]");
                        }
                    }
                    if (c.wasRemoved())
                    {
                        for (TestObject testObject : c.getRemoved())
                        {
                            LOG.info("SOURCELIST: Item Removed: " + testObject.getNameProperty().get() + " ["
                                    + testObject.getMemberSets().stream().collect(Collectors.joining(",")) + "]");
                        }
                    }
                    if (c.wasUpdated())
                    {
                        for (int i = c.getFrom(); i < c.getTo(); i++)
                        {
                            TestObject testObject = c.getList().get(i);
                            LOG.info("SOURCELIST: Item Changed: " + testObject.getNameProperty().get() + " ["
                                    + testObject.getMemberSets().stream().collect(Collectors.joining(",")) + "]");
                        }
                    }
                }
            }
        });

        ObservableList<TestObject> fooList = testList.filtered(o -> o.getMemberSets().contains("FOO"));
//        o.getSetProperty().get().equals("FOO"));
        fooList.addListener(new ListChangeListener<TestObject>()
        {
            @Override
            public void onChanged(Change<? extends TestObject> c)
            {
                while (c.next())
                {
                    if (c.wasAdded())
                    {
                        for (TestObject testObject : c.getAddedSubList())
                        {
                            LOG.info("FOOLIST: Item Added: " + testObject.getNameProperty().get() + " ["
                                    + testObject.getMemberSets().stream().collect(Collectors.joining(",")) + "]");
                        }
                    }
                    if (c.wasRemoved())
                    {
                        for (TestObject testObject : c.getRemoved())
                        {
                            LOG.info("FOOLIST: Item Removed: " + testObject.getNameProperty().get() + " ["
                                    + testObject.getMemberSets().stream().collect(Collectors.joining(",")) + "]");
                        }
                    }
                    if (c.wasUpdated())
                    {
                        for (int i = c.getFrom(); i < c.getTo(); i++)
                        {
                            TestObject testObject = c.getList().get(i);
                            LOG.info("FOOLIST: Item Changed: " + testObject.getNameProperty().get() + " ["
                                    + testObject.getMemberSets().stream().collect(Collectors.joining(",")) + "]");
                        }
                    }
                }
            }
        });
        ObservableList<TestObject> barList = testList.filtered(o -> o.getMemberSets().contains("BAR"));
        barList.addListener(new ListChangeListener<TestObject>()
        {
            @Override
            public void onChanged(Change<? extends TestObject> c)
            {
                while (c.next())
                {
                    if (c.wasAdded())
                    {
                        for (TestObject testObject : c.getAddedSubList())
                        {
                            LOG.info("BARLIST: Item Added: " + testObject.getNameProperty().get() + " ["
                                    + testObject.getMemberSets().stream().collect(Collectors.joining(",")) + "]");
                        }
                    }
                    if (c.wasRemoved())
                    {
                        for (TestObject testObject : c.getRemoved())
                        {
                            LOG.info("BARLIST: Item Removed: " + testObject.getNameProperty().get() + " ["
                                    + testObject.getMemberSets().stream().collect(Collectors.joining(",")) + "]");
                        }
                    }
                    if (c.wasUpdated())
                    {
                        for (int i = c.getFrom(); i < c.getTo(); i++)
                        {
                            TestObject testObject = c.getList().get(i);
                            LOG.info("BARLIST: Item Changed: " + testObject.getNameProperty().get() + " ["
                                    + testObject.getMemberSets().stream().collect(Collectors.joining(",")) + "]");
                        }
                    }
                }
            }
        });
        ObservableList<TestObject> doubleList = testList
                .filtered(o -> o.getMemberSets().contains("BAR") || o.getMemberSets().contains("BAS"));
        doubleList.addListener(new ListChangeListener<TestObject>()
        {
            @Override
            public void onChanged(Change<? extends TestObject> c)
            {
                while (c.next())
                {
                    if (c.wasAdded())
                    {
                        for (TestObject testObject : c.getAddedSubList())
                        {
                            LOG.info("BA*LIST: Item Added: " + testObject.getNameProperty().get() + " ["
                                    + testObject.getMemberSets().stream().collect(Collectors.joining(",")) + "]");
                        }
                    }
                    if (c.wasRemoved())
                    {
                        for (TestObject testObject : c.getRemoved())
                        {
                            LOG.info("BA*LIST: Item Removed: " + testObject.getNameProperty().get() + " ["
                                    + testObject.getMemberSets().stream().collect(Collectors.joining(",")) + "]");
                        }
                    }
                    if (c.wasUpdated())
                    {
                        for (int i = c.getFrom(); i < c.getTo(); i++)
                        {
                            TestObject testObject = c.getList().get(i);
                            LOG.info("BA*LIST: Item Changed: " + testObject.getNameProperty().get() + " ["
                                    + testObject.getMemberSets().stream().collect(Collectors.joining(",")) + "]");
                        }
                    }
                }
            }
        });

        LOG.info(">>> Adding one>>>");
        TestObject one = new TestObject("one", "FOO");
        testList.add(one);
        LOG.info(">>> Adding two>>>");
        TestObject two = new TestObject("two", "BAR");
        testList.add(two);
        LOG.info(">>> Adding three>>>");
        TestObject three = new TestObject("three", "FOO", "BAS");
        testList.add(three);
        LOG.info(">>> Adding four>>>");
        TestObject four = new TestObject("four", "BAR");
        testList.add(four);
        LOG.info(">>> Adding five>>>");
        TestObject five = new TestObject("five", "BAS");
        testList.add(five);
        LOG.info(">>> Adding six>>>");
        TestObject six = new TestObject("six", "BAS");
        testList.add(six);
        LOG.info(">>> Adding seven>>>");
        TestObject seven = new TestObject("seven", "FOO");
        testList.add(seven);

        LOG.info(">>> Adding BAR member set on five >>>");
        five.getMemberSets().add("BAR");
        LOG.info(">>> Adding FOO member set on five >>>");
        five.getMemberSets().add("FOO");
        LOG.info(">>> Adding BAS member set on five >>>");
        five.getMemberSets().add("BAS");

        LOG.info(">>> Removing five>>>");
        testList.remove(five);
        LOG.info(">>> Removing four>>>");
        testList.remove(four);
    }

    private static class TestObject
    {
        private StringProperty myNameProperty = new ConcurrentStringProperty();

        private StringProperty mySetProperty = new ConcurrentStringProperty();

        private ObservableSet<String> myMemberSets = FXCollections.observableSet(New.set());

        public TestObject(String name, String... sets)
        {
            myNameProperty.set(name);
            myMemberSets.addAll(Arrays.asList(sets));
        }

        /**
         * Gets the value of the {@link #myNameProperty} field.
         *
         * @return the value of the myNameProperty field.
         */
        public StringProperty getNameProperty()
        {
            return myNameProperty;
        }

        /**
         * Gets the value of the {@link #mySetProperty} field.
         *
         * @return the value of the mySetProperty field.
         */
        public StringProperty getSetProperty()
        {
            return mySetProperty;
        }

        /**
         * Gets the value of the {@link #myMemberSets} field.
         *
         * @return the value of the myMemberSets field.
         */
        public ObservableSet<String> getMemberSets()
        {
            return myMemberSets;
        }
    }
}
