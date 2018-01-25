package io.opensphere.test;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

/**
 * A JUnit test runner used to execute a given JUnit on the Swing Event Dispatcher Thread.
 */
public class SwingJunitTestRunner extends BlockJUnit4ClassRunner
{
    /**
     * Creates a new test runner for the supplied class.
     *
     * @param pClass the class to be executed in the runner.
     * @throws InitializationError if the test runner cannot be initialized.
     */
    public SwingJunitTestRunner(Class<?> pClass) throws InitializationError
    {
        super(pClass);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.junit.runners.ParentRunner#run(org.junit.runner.notification.RunNotifier)
     */
    @Override
    public void run(RunNotifier pNotifier)
    {
        try
        {
            SwingUtilities.invokeAndWait(() -> super.run(pNotifier));
        }
        catch (InterruptedException | InvocationTargetException e)
        {
            pNotifier.fireTestFailure(new Failure(getDescription(), e));
        }
    }
}
