package io.opensphere.core.appl;

import io.opensphere.core.TimeManager.Fade;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.util.Utilities;

/** Default fade implementation. */
public class DefaultFade implements Fade
{
    /** The fade-in. */
    private final Duration myFadeIn;

    /** The fade-out. */
    private final Duration myFadeOut;

    /**
     * Constructor.
     *
     * @param fadeIn The fade-in.
     * @param fadeOut The fade-out.
     */
    public DefaultFade(Duration fadeIn, Duration fadeOut)
    {
        Utilities.checkNull(fadeIn, "fadeIn");
        Utilities.checkNull(fadeOut, "fadeOut");
        myFadeIn = fadeIn;
        myFadeOut = fadeOut;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        DefaultFade other = (DefaultFade)obj;
        return myFadeIn.compareTo(other.myFadeIn) == 0 && myFadeOut.compareTo(other.myFadeOut) == 0;
    }

    @Override
    public Duration getFadeIn()
    {
        return myFadeIn;
    }

    @Override
    public Duration getFadeOut()
    {
        return myFadeOut;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myFadeIn == null ? 0 : myFadeIn.hashCode());
        result = prime * result + (myFadeOut == null ? 0 : myFadeOut.hashCode());
        return result;
    }

    @Override
    public Fade reverse()
    {
        return new DefaultFade(getFadeOut(), getFadeIn());
    }

    @Override
    public String toString()
    {
        return "DefaultFade [in=" + myFadeIn + ", out=" + myFadeOut + "]";
    }
}
