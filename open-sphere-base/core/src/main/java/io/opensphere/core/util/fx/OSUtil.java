package io.opensphere.core.util.fx;

import javafx.scene.input.KeyCode;

/**
 * A utility class containing methods used at runtime for platform distinction.
 */
public final class OSUtil
{
    /** Private constructor to prevent instantiation. */
    private OSUtil()
    {
        throw new UnsupportedOperationException("Instantiation of utility classes is not permitted.");
    }

    /**
     * Tests to determine if the application is running on a mac.
     *
     * @return true if the application is running on a mac.
     */
    public static boolean isMac()
    {
        return System.getProperty("os.name").toLowerCase().startsWith("mac os x");
    }

    /**
     * Returns the key code for the key which is commonly used on the
     * corresponding platform as a modifier key in shortcuts. For example it is
     * {@code KeyCode.CONTROL} on Windows (Ctrl + C, Ctrl + V ...) and
     * {@code KeyCode.META} on MacOS (Cmd + C, Cmd + V ...).
     *
     * @return the key code for shortcut modifier key
     */
    public static KeyCode getPlatformShortcutKey()
    {
        return isMac() ? KeyCode.META : KeyCode.CONTROL;
    }
}
