package io.opensphere.core.util;

import java.util.Observable;

/** A version of Observable that doesn't suck. */
public class NonSuckingObservable extends Observable
{
    @Override
    public void notifyObservers(Object arg)
    {
        setChanged();
        super.notifyObservers(arg);
    }
}
