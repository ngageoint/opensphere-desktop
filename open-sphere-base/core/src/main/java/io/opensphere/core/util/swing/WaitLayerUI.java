package io.opensphere.core.util.swing;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.function.Function;

import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.Timer;
import javax.swing.plaf.LayerUI;

import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * UI that can be used to paint a wait indicator over a component.
 *
 * Example:
 *
 * <pre>
 * JLayer&lt;JPanel&gt; layer = WaitLayerUI.wrap(new JPanel());
 * WaitLayerUI.setBusy(layer, true);
 * </pre>
 *
 * @param <V> The type of the view component.
 */
public class WaitLayerUI<V extends Component> extends LayerUI<V> implements ActionListener
{
    /** The default view fade. */
    public static final float DEFAULT_VIEW_FADE = .3f;

    /** How many increments to put into the fade. */
    private static final int MAXIMUM_FADE_COUNT = 15;

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Aspect ratio for the strokes in the busy indicator. */
    private static final float STROKE_ASPECT_RATIO = .25f;

    /** Property that indicates a timer tick. */
    private static final String TICK_PROPERTY_NAME = "tick";

    /** The current angle being painted. */
    private int myAngle;

    /** The counter for the fade. */
    private int myFadeCount;

    /** Flag indicating if the wait indicator is fading. */
    private boolean myFadingOut = true;

    /** A timer to do repainting. */
    private Timer myTimer;

    /**
     * Transform to be used to determine the painting region for the wait
     * indicator. The input to the transform will be the component being
     * painted.
     */
    @Nullable
    private Function<V, Rectangle> myTransform;

    /** How much to fade the wrapped component. */
    private float myViewFade = DEFAULT_VIEW_FADE;

    /**
     * Method that can be used to set a {@link JLayer} busy, assuming it has a
     * {@link WaitLayerUI} installed.
     *
     * @param layer The layer.
     * @param busy Flag indicating if the layer should be busy.
     */
    public static void setBusy(JLayer<?> layer, boolean busy)
    {
        ((WaitLayerUI<?>)layer.getUI()).setBusy(busy);
    }

    /**
     * Convenience method that wraps a component in a {@link JLayer} with a
     * {@link WaitLayerUI}.
     *
     * @param <T> The type of the component.
     * @param component The component to be wrapped.
     * @return The {@link JLayer}.
     */
    public static <T extends Component> JLayer<T> wrap(T component)
    {
        return new JLayer<>(component, new WaitLayerUI<T>());
    }

    /**
     * Convenience method that wraps a component in a {@link JLayer} with a
     * {@link WaitLayerUI}.
     *
     * @param <T> The type of the component.
     * @param component The component to be wrapped.
     * @param viewFade How much to fade the painted component while the wait
     *            indicator is being painted, in the range [0, 1].
     * @param transform Transform to be used to determine the painting region
     *            for the wait indicator. The input to the transform will be the
     *            component being painted.
     * @return The {@link JLayer}.
     */
    public static <T extends Component> JLayer<T> wrap(T component, float viewFade, Function<T, Rectangle> transform)
    {
        return new JLayer<>(component, new WaitLayerUI<>(viewFade, transform));
    }

    /**
     * Constructor.
     */
    public WaitLayerUI()
    {
        this(DEFAULT_VIEW_FADE, (Function<V, Rectangle>)null);
    }

    /**
     * Constructor.
     *
     * @param viewFade How much to fade the painted component while the wait
     *            indicator is being painted, in the range [0, 1].
     * @param transform Transform to be used to determine the painting region
     *            for the wait indicator. The input to the transform will be the
     *            component being painted.
     */
    public WaitLayerUI(float viewFade, Function<V, Rectangle> transform)
    {
        myViewFade = viewFade;
        myTransform = transform;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        firePropertyChange(TICK_PROPERTY_NAME, Integer.valueOf(0), Integer.valueOf(1));
        myAngle += 3;
        if (myAngle == 360)
        {
            myAngle = 0;
        }

        if (myFadingOut)
        {
            if (--myFadeCount <= 0)
            {
                myTimer.stop();
            }
        }
        else if (myFadeCount < MAXIMUM_FADE_COUNT)
        {
            ++myFadeCount;
        }
    }

    @Override
    public void applyPropertyChange(PropertyChangeEvent evt, JLayer<? extends V> l)
    {
        if (TICK_PROPERTY_NAME.equals(evt.getPropertyName()))
        {
            l.repaint();
        }
    }

    /**
     * Get if the layer is currently busy.
     *
     * @return {@code true} if the layer is currently busy.
     */
    public boolean isBusy()
    {
        return myTimer != null && myTimer.isRunning();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void paint(Graphics g, JComponent c)
    {
        super.paint(g, c);

        if (!isBusy() || myAngle < 0)
        {
            return;
        }

        Rectangle bounds = myTransform != null ? myTransform.apply(((JLayer<V>)c).getView()) : new Rectangle(c.getSize());
        Graphics2D g2 = (Graphics2D)g.create();

        float fade = (float)myFadeCount / MAXIMUM_FADE_COUNT;

        if (myViewFade > 0)
        {
            Composite orig = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, myViewFade * fade));
            g2.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            g2.setComposite(orig);
        }

        int s = Math.min(bounds.width, bounds.height) / 5;
        int cx = bounds.x + bounds.width / 2;
        int cy = bounds.y + bounds.height / 2;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setStroke(new BasicStroke(s * STROKE_ASPECT_RATIO, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setPaint(Color.WHITE);
        g2.rotate(Math.PI * myAngle / 180, cx, cy);
        final int numSpokes = 12;
        final double spokeRotation = 2 * Math.PI / numSpokes;
        for (int i = 0; i < numSpokes; i++)
        {
            g2.drawLine(cx + s, cy, cx + s * 2, cy);
            g2.rotate(spokeRotation, cx, cy);
            float scale = (numSpokes - 1f - i) / (numSpokes - 1);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, scale * fade));
        }

        g2.dispose();
    }

    /**
     * Set the layer busy or not.
     *
     * @param busy Flag indicating if the layer should be busy or not.
     */
    public void setBusy(boolean busy)
    {
        if (busy)
        {
            if (myFadingOut)
            {
                myFadingOut = false;
                myFadeCount = 0;
                if (myTimer == null)
                {
                    myTimer = new Timer(40, this);
                }
                myTimer.setInitialDelay(200);
                myAngle = -3;
                myTimer.start();
            }
        }
        else
        {
            myFadingOut = true;
        }
    }
}
