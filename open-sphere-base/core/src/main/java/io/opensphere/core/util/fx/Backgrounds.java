package io.opensphere.core.util.fx;

import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;

/**
 * A utility class used to simplify JavaFX background interactions.
 */
public final class Backgrounds
{
    /**
     * Private constructor hidden from use. Throws exceptions to protect against
     * use through reflection.
     */
    private Backgrounds()
    {
        throw new UnsupportedOperationException("Instantiation of utility classes is not permitted.");
    }

    /**
     * Creates a color-painted background with no insets or radius.
     *
     * @param color the color to apply to the background.
     * @return a Background used to just apply color.
     */
    public static Background color(Color color)
    {
        return new Background(new BackgroundFill(color, null, null));
    }
}
