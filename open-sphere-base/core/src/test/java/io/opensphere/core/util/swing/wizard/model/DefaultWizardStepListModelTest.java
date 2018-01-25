package io.opensphere.core.util.swing.wizard.model;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.util.swing.wizard.model.WizardStepListModel.StepState;
import io.opensphere.core.util.swing.wizard.model.WizardStepListModel.WizardStepListModelChangeListener;

/** Test {@link DefaultWizardStepListModel}. */
public class DefaultWizardStepListModelTest
{
    /** Step title. */
    private static final String STEP1 = "step1";

    /** Step title. */
    private static final String STEP2 = "step2";

    /** Step title. */
    private static final String STEP3 = "step3";

    /** Test {@link DefaultWizardStepListModel#allStepsAreValid()}. */
    @Test
    public void testAllStepsAreValid()
    {
        List<? extends String> steps = Arrays.asList(STEP1, STEP2, STEP3);
        DefaultWizardStepListModel model = new DefaultWizardStepListModel(steps, 0);

        Assert.assertFalse(model.allStepsAreValid());

        model.setStepState(0, StepState.VALID);
        Assert.assertFalse(model.allStepsAreValid());
        model.setStepState(1, StepState.VALID);
        Assert.assertFalse(model.allStepsAreValid());
        model.setStepState(2, StepState.VALID);
        Assert.assertTrue(model.allStepsAreValid());

        for (StepState state : EnumSet.complementOf(EnumSet.of(StepState.VALID)))
        {
            for (int index = 0; index < steps.size(); ++index)
            {
                model.setStepState(index, state);

                if (state != StepState.WARNING)
                {
                    Assert.assertFalse(model.allStepsAreValid());
                }
                else
                {
                    Assert.assertTrue(model.allStepsAreValid());
                }

                model.setStepState(index, StepState.VALID);
                Assert.assertTrue(model.allStepsAreValid());
            }
        }
    }

    /** Test {@link DefaultWizardStepListModel#decrementStep()}. */
    @Test
    public void testDecrementStep()
    {
        List<? extends String> steps = Arrays.asList(STEP1, STEP2, STEP3);
        DefaultWizardStepListModel model = new DefaultWizardStepListModel(steps, 2);
        WizardStepListModelChangeListener listener = EasyMock.createMock(WizardStepListModelChangeListener.class);
        model.getChangeSupport().addListener(listener);

        Assert.assertEquals(2, model.getCurrentStep());
        expectStepChange(listener, model, 1, STEP2);
        model.decrementStep();
        Assert.assertEquals(1, model.getCurrentStep());
        expectStepChange(listener, model, 0, STEP1);
        model.decrementStep();
        Assert.assertEquals(0, model.getCurrentStep());

        try
        {
            EasyMock.reset(listener);
            EasyMock.replay(listener);
            model.decrementStep();
            Assert.fail("Exception should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
            EasyMock.verify(listener);
        }
    }

    /** Test {@link DefaultWizardStepListModel#getCurrentStepTitle()}. */
    @Test
    public void testGetCurrentStepTitle()
    {
        List<? extends String> steps = Arrays.asList(STEP1, STEP2, STEP3);
        DefaultWizardStepListModel model = new DefaultWizardStepListModel(steps, 2);
        Assert.assertEquals(STEP3, model.getCurrentStepTitle());

        model.setCurrentStep(0);
        Assert.assertEquals(STEP1, model.getCurrentStepTitle());

        model.setCurrentStep(1);
        Assert.assertEquals(STEP2, model.getCurrentStepTitle());
    }

    /** Test {@link DefaultWizardStepListModel#getStepCount()}. */
    @Test
    public void testGetStepCount()
    {
        List<? extends String> steps = Arrays.asList(STEP1, STEP2, STEP3);
        DefaultWizardStepListModel model = new DefaultWizardStepListModel(steps, 2);
        Assert.assertEquals(3, model.getStepCount());
    }

    /** Test {@link DefaultWizardStepListModel#getStepState(int)}. */
    @Test
    public void testGetStepStateInt()
    {
        List<? extends String> steps = Arrays.asList(STEP1, STEP2, STEP3);
        DefaultWizardStepListModel model = new DefaultWizardStepListModel(steps, 2);
        Assert.assertEquals(StepState.INDETERMINATE, model.getStepState(0));

        for (StepState state : EnumSet.allOf(StepState.class))
        {
            for (int index = 0; index < steps.size(); ++index)
            {
                model.setStepState(index, state);
                Assert.assertEquals(state, model.getStepState(index));
            }
        }
    }

    /** Test {@link DefaultWizardStepListModel#getStepState(String)}. */
    @Test
    public void testGetStepStateString()
    {
        List<? extends String> steps = Arrays.asList(STEP1, STEP2, STEP3);
        DefaultWizardStepListModel model = new DefaultWizardStepListModel(steps, 2);
        Assert.assertEquals(StepState.INDETERMINATE, model.getStepState(STEP1));

        for (StepState state : EnumSet.allOf(StepState.class))
        {
            for (int index = 0; index < steps.size(); ++index)
            {
                model.setStepState(steps.get(index), state);
                Assert.assertEquals(state, model.getStepState(index));
            }
        }
    }

    /** Test {@link DefaultWizardStepListModel#getStepTitle(int)}. */
    @Test
    public void testGetStepTitle()
    {
        List<? extends String> steps = Arrays.asList(STEP1, STEP2, STEP3);
        DefaultWizardStepListModel model = new DefaultWizardStepListModel(steps, 2);

        for (int index = 0; index < steps.size(); ++index)
        {
            Assert.assertEquals(steps.get(index), model.getStepTitle(index));
        }
    }

    /** Test {@link DefaultWizardStepListModel#getStepTitles()}. */
    @Test
    public void testGetStepTitles()
    {
        List<? extends String> steps = Arrays.asList(STEP1, STEP2, STEP3);
        DefaultWizardStepListModel model = new DefaultWizardStepListModel(steps, 2);
        Assert.assertEquals(steps, model.getStepTitles());
    }

    /** Test {@link DefaultWizardStepListModel#incrementStep()}. */
    @Test
    public void testIncrementStep()
    {
        List<? extends String> steps = Arrays.asList(STEP1, STEP2, STEP3);
        DefaultWizardStepListModel model = new DefaultWizardStepListModel(steps, 0);
        WizardStepListModelChangeListener listener = EasyMock.createMock(WizardStepListModelChangeListener.class);
        model.getChangeSupport().addListener(listener);
        Assert.assertEquals(0, model.getCurrentStep());
        expectStepChange(listener, model, 1, STEP2);
        model.incrementStep();
        Assert.assertEquals(1, model.getCurrentStep());
        expectStepChange(listener, model, 2, STEP3);
        model.incrementStep();
        Assert.assertEquals(2, model.getCurrentStep());

        try
        {
            EasyMock.reset(listener);
            EasyMock.replay(listener);
            model.incrementStep();
            Assert.fail("Exception should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
            EasyMock.verify(listener);
        }
    }

    /** Test {@link DefaultWizardStepListModel#setCurrentStep(int)}. */
    @Test
    @SuppressWarnings("PMD.EmptyCatchBlock")
    public void testSetCurrentStep()
    {
        List<? extends String> steps = Arrays.asList(STEP1, STEP2, STEP3);
        DefaultWizardStepListModel model = new DefaultWizardStepListModel(steps, 2);
        Assert.assertEquals(2, model.getCurrentStep());
        model.setCurrentStep(1);
        Assert.assertEquals(1, model.getCurrentStep());
        model.decrementStep();
        Assert.assertEquals(0, model.getCurrentStep());

        try
        {
            model.decrementStep();
            Assert.fail("Exception should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
        }
    }

    /** Test {@link DefaultWizardStepListModel#setCurrentStep(String)}. */
    @Test
    @SuppressWarnings("PMD.EmptyCatchBlock")
    public void testSetCurrentStepString()
    {
        List<? extends String> steps = Arrays.asList(STEP1, STEP2, STEP3);
        DefaultWizardStepListModel model = new DefaultWizardStepListModel(steps, 2);
        Assert.assertEquals(2, model.getCurrentStep());
        model.setCurrentStep(STEP2);
        Assert.assertEquals(1, model.getCurrentStep());
        model.setCurrentStep(STEP1);
        Assert.assertEquals(0, model.getCurrentStep());

        try
        {
            model.setCurrentStep("junk");
            Assert.fail("Exception should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
        }
    }

    /** Test {@link DefaultWizardStepListModel#setStepState(int, StepState)}. */
    @Test
    public void testSetStepStateIntStepState()
    {
        List<? extends String> steps = Arrays.asList(STEP1, STEP2, STEP3);
        DefaultWizardStepListModel model = new DefaultWizardStepListModel(steps, 0);
        WizardStepListModelChangeListener listener = EasyMock.createMock(WizardStepListModelChangeListener.class);
        model.getChangeSupport().addListener(listener);

        for (int index = 0; index < steps.size(); ++index)
        {
            for (StepState state : EnumSet.allOf(StepState.class))
            {
                expectStepStateChange(listener, model, index, steps.get(index), state);
                model.setStepState(index, state);
                Assert.assertEquals(state, model.getStepState(index));
            }
        }
    }

    /**
     * Test {@link DefaultWizardStepListModel#setStepState(String, StepState)}.
     */
    @Test
    public void testSetStepStateStringStepState()
    {
        List<? extends String> steps = Arrays.asList(STEP1, STEP2, STEP3);
        DefaultWizardStepListModel model = new DefaultWizardStepListModel(steps, 0);
        WizardStepListModelChangeListener listener = EasyMock.createMock(WizardStepListModelChangeListener.class);
        model.getChangeSupport().addListener(listener);

        for (int index = 0; index < steps.size(); ++index)
        {
            for (StepState state : EnumSet.allOf(StepState.class))
            {
                expectStepStateChange(listener, model, index, steps.get(index), state);
                model.setStepState(steps.get(index), state);
                Assert.assertEquals(state, model.getStepState(index));
            }
        }
    }

    /**
     * Setup a mock listener to expect a step change.
     *
     * @param listener The listener.
     * @param model The model.
     * @param step The new step.
     * @param title The new step title.
     */
    private void expectStepChange(WizardStepListModelChangeListener listener, WizardStepListModel model, int step, String title)
    {
        EasyMock.reset(listener);
        listener.currentStepChanged(model, step, title);
        EasyMock.replay(listener);
    }

    /**
     * Setup a mock listener to expect a step state change.
     *
     * @param listener The listener.
     * @param model The model.
     * @param step The changed step.
     * @param title The changed step title.
     * @param state The new step state.
     */
    private void expectStepStateChange(WizardStepListModelChangeListener listener, WizardStepListModel model, int step,
            String title, StepState state)
    {
        EasyMock.reset(listener);
        listener.stepStateChanged(model, step, title, state);
        EasyMock.replay(listener);
    }
}
