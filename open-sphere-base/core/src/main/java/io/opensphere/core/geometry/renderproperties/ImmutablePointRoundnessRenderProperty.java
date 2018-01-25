package io.opensphere.core.geometry.renderproperties;

import java.util.Collection;
import java.util.Collections;

/** An immutable implementation of {@link PointRoundnessRenderProperty}. */
public abstract class ImmutablePointRoundnessRenderProperty implements PointRoundnessRenderProperty
{
    /** Immutable instance for round points. */
    public static final ImmutablePointRoundnessRenderProperty ROUND = new ImmutablePointRoundnessRenderProperty()
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isRound()
        {
            return true;
        }
    };

    /** Immutable instance for square points. */
    public static final ImmutablePointRoundnessRenderProperty SQUARE = new ImmutablePointRoundnessRenderProperty()
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isRound()
        {
            return false;
        }
    };

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    @Override
    public void addListener(RenderPropertyChangeListener listen)
    {
    }

    @Override
    public ImmutablePointRoundnessRenderProperty clone()
    {
        return this;
    }

    @Override
    public int compareTo(PointRoundnessRenderProperty o)
    {
        if (isRound() == o.isRound())
        {
            return 0;
        }
        return isRound() ? 1 : -1;
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
        return isRound() == ((PointRoundnessRenderProperty)obj).isRound();
    }

    @Override
    public Collection<? extends RenderProperties> getThisPlusDescendants()
    {
        return Collections.singleton(this);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (isRound() ? 1231 : 1237);
        return result;
    }

    @Override
    public void removeListener(RenderPropertyChangeListener listen)
    {
    }

    @Override
    public void setRound(boolean round)
    {
        throw new UnsupportedOperationException();
    }
}
