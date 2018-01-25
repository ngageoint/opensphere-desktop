package io.opensphere.core.util.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Executor;

import org.apache.log4j.Logger;
import org.junit.Test;

import io.opensphere.core.util.concurrent.ThreadedStateMachine.State;
import io.opensphere.core.util.concurrent.ThreadedStateMachine.StateChangeHandler;
import io.opensphere.core.util.concurrent.ThreadedStateMachine.StateController;
import org.junit.Assert;

/**
 * Test for {@link ThreadedStateMachine}.
 */
public class ThreadedStateMachineTest
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ThreadedStateMachineTest.class);

    /**
     * General test for the state machine. Create three sequential states, add
     * some test objects to the first state, and then do various manipulations
     * and verify that the objects are in the correct state at each stage.
     */
    @Test
    public void testMachine()
    {
        ThreadedStateMachine<TestObject> tsm = new ThreadedStateMachine<>(EnumSet.allOf(TestState.class));
        StateChangeHandler<TestObject> alphaHandler = new StateChangeHandler<TestObject>()
        {
            @Override
            public void handleStateChanged(List<? extends TestObject> objects, State newState,
                    StateController<TestObject> controller)
            {
                controller.changeState(objects, TestState.BRAVO);
            }
        };
        StateChangeHandler<TestObject> bravoHandler = new StateChangeHandler<TestObject>()
        {
            @Override
            public void handleStateChanged(List<? extends TestObject> objects, State newState,
                    StateController<TestObject> controller)
            {
                controller.changeState(objects, TestState.CHARLIE);
            }
        };
        StateChangeHandler<TestObject> charlieHandler = new StateChangeHandler<TestObject>()
        {
            @Override
            public void handleStateChanged(List<? extends TestObject> objects, State newState,
                    StateController<TestObject> controller)
            {
            }
        };

        TestExecutor executorA = new TestExecutor();
        TestExecutor executorB = new TestExecutor();
        TestExecutor executorC = new TestExecutor();

        tsm.registerStateChangeHandler(EnumSet.of(TestState.ALPHA), alphaHandler, executorA, null, 0);
        tsm.registerStateChangeHandler(EnumSet.of(TestState.BRAVO), bravoHandler, executorB, null, 0);
        tsm.registerStateChangeHandler(EnumSet.of(TestState.CHARLIE), charlieHandler, executorC, null, 0);

        final int objectCount = 25000;
        List<TestObject> objects = new ArrayList<>(objectCount);
        for (int i = 0; i < objectCount; i++)
        {
            objects.add(new TestObject(i));
        }

        Assert.assertEquals(0, tsm.getObjectsInState(TestState.ALPHA).size());
        Assert.assertEquals(0, tsm.getObjectsInState(TestState.BRAVO).size());
        Assert.assertEquals(0, tsm.getObjectsInState(TestState.CHARLIE).size());
        tsm.resetState(objects, TestState.ALPHA);
        Assert.assertEquals(0, tsm.getObjectsInState(TestState.ALPHA).size());
        Assert.assertEquals(0, tsm.getObjectsInState(TestState.BRAVO).size());
        Assert.assertEquals(0, tsm.getObjectsInState(TestState.CHARLIE).size());
        executorA.runAll();
        Assert.assertEquals(objectCount, tsm.getObjectsInState(TestState.ALPHA).size());
        Assert.assertEquals(0, tsm.getObjectsInState(TestState.BRAVO).size());
        Assert.assertEquals(0, tsm.getObjectsInState(TestState.CHARLIE).size());

        // Should have no effect.
        tsm.resetState(TestState.CHARLIE);
        Assert.assertEquals(objectCount, tsm.getObjectsInState(TestState.ALPHA).size());
        Assert.assertEquals(0, tsm.getObjectsInState(TestState.BRAVO).size());
        Assert.assertEquals(0, tsm.getObjectsInState(TestState.CHARLIE).size());

        // This should move everything to BRAVO.
        executorB.runAll();
        Assert.assertEquals(0, tsm.getObjectsInState(TestState.ALPHA).size());
        Assert.assertEquals(objectCount, tsm.getObjectsInState(TestState.BRAVO).size());
        Assert.assertEquals(0, tsm.getObjectsInState(TestState.CHARLIE).size());

        // Reset to ALPHA
        tsm.resetState(objects, TestState.ALPHA);
        Assert.assertEquals(0, tsm.getObjectsInState(TestState.ALPHA).size());
        Assert.assertEquals(objectCount, tsm.getObjectsInState(TestState.BRAVO).size());
        Assert.assertEquals(0, tsm.getObjectsInState(TestState.CHARLIE).size());
        executorA.runAll();
        Assert.assertEquals(objectCount, tsm.getObjectsInState(TestState.ALPHA).size());
        Assert.assertEquals(0, tsm.getObjectsInState(TestState.BRAVO).size());
        Assert.assertEquals(0, tsm.getObjectsInState(TestState.CHARLIE).size());

        // Move to BRAVO.
        executorB.runAll();
        Assert.assertEquals(0, tsm.getObjectsInState(TestState.ALPHA).size());
        Assert.assertEquals(objectCount, tsm.getObjectsInState(TestState.BRAVO).size());
        Assert.assertEquals(0, tsm.getObjectsInState(TestState.CHARLIE).size());

        // Move to CHARLIE.
        executorC.runAll();
        Assert.assertEquals(0, tsm.getObjectsInState(TestState.ALPHA).size());
        Assert.assertEquals(0, tsm.getObjectsInState(TestState.BRAVO).size());
        Assert.assertEquals(objectCount, tsm.getObjectsInState(TestState.CHARLIE).size());

        List<TestObject> objects2 = new ArrayList<>(objectCount);
        for (int i = objectCount; i < objectCount * 2; i++)
        {
            objects2.add(new TestObject(i));
        }

        tsm.resetState(objects2, TestState.ALPHA);
        executorA.runAll();
        Assert.assertEquals(objectCount, tsm.getObjectsInState(TestState.ALPHA).size());
        Assert.assertEquals(0, tsm.getObjectsInState(TestState.BRAVO).size());
        Assert.assertEquals(objectCount, tsm.getObjectsInState(TestState.CHARLIE).size());
        executorB.runAll();
        Assert.assertEquals(0, tsm.getObjectsInState(TestState.ALPHA).size());
        Assert.assertEquals(objectCount, tsm.getObjectsInState(TestState.BRAVO).size());
        Assert.assertEquals(objectCount, tsm.getObjectsInState(TestState.CHARLIE).size());

        tsm.resetState(TestState.ALPHA);
        executorA.runAll();
        executorC.runAll();
        Assert.assertEquals(objectCount * 2, tsm.getObjectsInState(TestState.ALPHA).size());
        Assert.assertEquals(0, tsm.getObjectsInState(TestState.BRAVO).size());
        Assert.assertEquals(0, tsm.getObjectsInState(TestState.CHARLIE).size());
        executorB.runAll();
        Assert.assertEquals(0, tsm.getObjectsInState(TestState.ALPHA).size());
        Assert.assertEquals(objectCount * 2, tsm.getObjectsInState(TestState.BRAVO).size());
        Assert.assertEquals(0, tsm.getObjectsInState(TestState.CHARLIE).size());
        executorC.runAll();
        Assert.assertEquals(0, tsm.getObjectsInState(TestState.ALPHA).size());
        Assert.assertEquals(0, tsm.getObjectsInState(TestState.BRAVO).size());
        Assert.assertEquals(objectCount * 2, tsm.getObjectsInState(TestState.CHARLIE).size());
    }

    /**
     * Test object for use in the state machine.
     */
    public static class TestObject
    {
        /** The id for the object. */
        private final int myId;

        /**
         * Construct the test object.
         *
         * @param id The id for the object.
         */
        public TestObject(int id)
        {
            myId = id;
        }

        /**
         * Get the id.
         *
         * @return The id.
         */
        public int getId()
        {
            return myId;
        }
    }

    /** States to use in the state machine. */
    public enum TestState implements State
    {
        /** First state. */
        ALPHA(1),

        /** Second state. */
        BRAVO(2),

        /** Third state. */
        CHARLIE(3);

        /** The order of the state. */
        private final int myOrder;

        /**
         * Construct a state.
         *
         * @param order The order of the state.
         */
        TestState(int order)
        {
            myOrder = order;
        }

        @Override
        public int getStateOrder()
        {
            return myOrder;
        }
    }

    /** Executor that allows us control of when runnables are run. */
    private static class TestExecutor implements Executor
    {
        /** The runnables that have yet to run. */
        private final List<Runnable> myRunnables = new ArrayList<>();

        @Override
        public synchronized void execute(Runnable command)
        {
            myRunnables.add(command);
        }

        /**
         * Run all the runnables I have so far.
         */
        public void runAll()
        {
            final Collection<Runnable> all;
            synchronized (this)
            {
                all = new ArrayList<>(myRunnables);
                myRunnables.clear();
            }
            List<Thread> threads = new ArrayList<>();
            for (final Runnable r : all)
            {
                Thread thread = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        r.run();
                    }
                });
                thread.start();
                threads.add(thread);
            }
            try
            {
                for (Thread thread : threads)
                {
                    thread.join();
                }
            }
            catch (InterruptedException e)
            {
                LOGGER.error(e, e);
            }
        }
    }
}
