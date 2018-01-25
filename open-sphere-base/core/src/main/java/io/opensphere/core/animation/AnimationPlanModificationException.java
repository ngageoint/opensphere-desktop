package io.opensphere.core.animation;

/**
 * The Class InvalidAnimationCommandKeyException.
 */
public class AnimationPlanModificationException extends Exception
{
    /**
     * This message is used when attempting to modify a plan which is the the
     * current plan.
     */
    public static final String PLAN_MISMATCH = "Plan does not match expected plan.";

    /**
     * the serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new invalid animation command key exception.
     *
     * @param message the message
     */
    public AnimationPlanModificationException(String message)
    {
        super(message);
    }

    /**
     * Instantiates a new invalid animation command key exception.
     *
     * @param message the message
     * @param t the t
     */
    public AnimationPlanModificationException(String message, Throwable t)
    {
        super(message, t);
    }

    /**
     * Instantiates a new invalid animation command key exception.
     *
     * @param t the t
     */
    public AnimationPlanModificationException(Throwable t)
    {
        super(t);
    }
}
