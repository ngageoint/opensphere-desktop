package io.opensphere.core.util.fx.tabpane.skin;

import java.util.function.Supplier;

import io.opensphere.core.function.Procedure;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.util.Duration;

/** An animation used to modify the location of a given skin. */
public class HeaderAnimation extends Transition
{
    /** The skin affected by the animation. */
    private Supplier<OSTabHeaderSkin> mySkinSupplier;

    /** The supplier used to access the source location. */
    private Supplier<Double> mySourceSupplier;

    /** The supplier used to access the transition location. */
    private Supplier<Double> myTransitionSupplier;

    /**
     * Creates a new animation, using the supplied functions during execution.
     *
     * @param procedure the procedure to call when complete.
     * @param skinSupplier the supplier of the skin to modify.
     * @param sourceSupplier the supplier of the source location.
     * @param transitionSupplier the supplier of the transition location.
     */
    public HeaderAnimation(Procedure procedure, Supplier<OSTabHeaderSkin> skinSupplier, Supplier<Double> sourceSupplier,
            Supplier<Double> transitionSupplier)
    {
        mySkinSupplier = skinSupplier;
        mySourceSupplier = sourceSupplier;
        myTransitionSupplier = transitionSupplier;
        setInterpolator(Interpolator.EASE_BOTH);
        setCycleDuration(Duration.millis(OSTabPaneSkin.ANIMATION_DURATION));
        setOnFinished(event -> procedure.invoke());
    }

    @Override
    protected void interpolate(double frac)
    {
        mySkinSupplier.get().setLayoutX(mySourceSupplier.get() + myTransitionSupplier.get() * frac);
    }
}
