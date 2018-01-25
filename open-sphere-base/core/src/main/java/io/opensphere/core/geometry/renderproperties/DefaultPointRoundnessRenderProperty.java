package io.opensphere.core.geometry.renderproperties;

/** Standard implementation of {@link PointRoundnessRenderProperty}. */
public class DefaultPointRoundnessRenderProperty extends AbstractRenderProperties implements PointRoundnessRenderProperty
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** If the point should be drawn round. */
    private volatile boolean myRound;

    @Override
    public DefaultPointRoundnessRenderProperty clone()
    {
        return (DefaultPointRoundnessRenderProperty)super.clone();
    }

    @Override
    public int compareTo(PointRoundnessRenderProperty o)
    {
        if (myRound == o.isRound())
        {
            return 0;
        }
        return myRound ? 1 : -1;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!super.equals(obj) || getClass() != obj.getClass())
        {
            return false;
        }
        DefaultPointRoundnessRenderProperty other = (DefaultPointRoundnessRenderProperty)obj;
        return myRound == other.myRound;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (myRound ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean isRound()
    {
        return myRound;
    }

    @Override
    public void setRound(boolean round)
    {
        myRound = round;
        notifyChanged();
    }
}
