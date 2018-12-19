package io.opensphere.core.util.fx.tabpane;

import java.util.IdentityHashMap;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ObservableListBase;
import javafx.util.Callback;

final class OSElementObserver<E>
{

    private static class ElementsMapElement
    {
        InvalidationListener listener;

        int counter;

        public ElementsMapElement(InvalidationListener listener)
        {
            this.listener = listener;
            this.counter = 1;
        }

        public void increment()
        {
            counter++;
        }

        public int decrement()
        {
            return --counter;
        }

        private InvalidationListener getListener()
        {
            return listener;
        }
    }

    private Callback<E, Observable[]> extractor;

    private final Callback<E, InvalidationListener> listenerGenerator;

    private final ObservableListBase<E> list;

    private IdentityHashMap<E, OSElementObserver.ElementsMapElement> elementsMap = new IdentityHashMap<E, OSElementObserver.ElementsMapElement>();

    OSElementObserver(Callback<E, Observable[]> extractor, Callback<E, InvalidationListener> listenerGenerator,
            ObservableListBase<E> list)
    {
        this.extractor = extractor;
        this.listenerGenerator = listenerGenerator;
        this.list = list;
    }

    void attachListener(final E e)
    {
        if (elementsMap != null && e != null)
        {
            if (elementsMap.containsKey(e))
            {
                elementsMap.get(e).increment();
            }
            else
            {
                InvalidationListener listener = listenerGenerator.call(e);
                for (Observable o : extractor.call(e))
                {
                    o.addListener(listener);
                }
                elementsMap.put(e, new OSElementObserver.ElementsMapElement(listener));
            }
        }
    }

    void detachListener(E e)
    {
        if (elementsMap != null && e != null)
        {
            OSElementObserver.ElementsMapElement el = elementsMap.get(e);
            for (Observable o : extractor.call(e))
            {
                o.removeListener(el.getListener());
            }
            if (el.decrement() == 0)
            {
                elementsMap.remove(e);
            }
        }
    }

}