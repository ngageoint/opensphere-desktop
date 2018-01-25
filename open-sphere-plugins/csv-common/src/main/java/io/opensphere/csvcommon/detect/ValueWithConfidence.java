package io.opensphere.csvcommon.detect;

/**
 * Holds a value with associated confidence.
 *
 * @param <T> The type of the value.
 */
public class ValueWithConfidence<T>
{
    /** The value. */
    private T myValue;

    /** The confidence (0.0 to 1.0). */
    private float myConfidence;

    /**
     * Constructor.
     */
    public ValueWithConfidence()
    {
    }

    /**
     * Constructor.
     *
     * @param value the value
     * @param confidence the confidence (0.0 to 1.0)
     */
    public ValueWithConfidence(T value, float confidence)
    {
        myValue = value;
        setConfidenceInternal(confidence);
    }

    /**
     * Gets the confidence.
     *
     * @return the confidence (0.0 to 1.0)
     */
    public float getConfidence()
    {
        return myConfidence;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public T getValue()
    {
        return myValue;
    }

    /**
     * Sets the confidence.
     *
     * @param confidence the confidence (0.0 to 1.0)
     */
    public void setConfidence(float confidence)
    {
        setConfidenceInternal(confidence);
    }

    /**
     * Sets the value.
     *
     * @param value the value
     */
    public void setValue(T value)
    {
        myValue = value;
    }

    @Override
    public String toString()
    {
        return "ValueWithConfidence [value=" + myValue + ", confidence=" + myConfidence + "]";
    }

    /**
     * Sets the confidence.
     *
     * @param confidence the confidence
     */
    private void setConfidenceInternal(float confidence)
    {
        if (confidence < 0.0 || confidence > 1.0)
        {
            throw new IllegalArgumentException(
                    "Confidence value of " + confidence + " is outside the allowed range of 0.0 to 1.0");
        }
        myConfidence = confidence;
    }
}
