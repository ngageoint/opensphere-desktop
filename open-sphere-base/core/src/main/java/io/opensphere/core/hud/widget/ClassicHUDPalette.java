package io.opensphere.core.hud.widget;

import java.awt.Color;

/** A holder for the colors used in the classic HUD. */
public final class ClassicHUDPalette
{
    /** Color for decoration line. */
    public static final Color ourBottomDecorLineColor = new Color(0.6f, 0.6f, 0.6f, 0.85f);

    /** Color of the check box background. */
    public static final Color ourCheckBoxBackgroundColor = new Color(84, 84, 107);

    /** Color of the check box border. */
    public static final Color ourCheckBoxBorderColor = new Color(200, 200, 255);

    /** Highlight color for check box text. */
    public static final Color ourCheckBoxHighlightBlue = new Color(.329f, .964f, .996f);

    /** Color of the check mark. */
    public static final Color ourCheckColor = Color.WHITE;

    /** Background color for the selection circle. */
    public static final Color ourRadioButtonBackgroundColor = new Color(161, 161, 210);

    /** color for the selection dot. */
    public static final Color ourRadioButtonSelectorColor = new Color(33, 33, 33);

    /** Color for the background of sliders. */
    public static final Color ourSliderBackgroundColor = new Color(84, 84, 107).brighter().brighter();

    /** Slider Puck Armed color. */
    public static final Color ourSliderPuckArmColor = Color.WHITE.darker();

    /** Slider Puck Disarmed color. */
    public static final Color ourSliderPuckDisarmColor = Color.WHITE;

    /** Color for the slider track. */
    public static final Color ourSliderTrackColor = new Color(84, 84, 107).darker();

    /** Color for the bar. */
    public static final Color ourTitleBarColor = new Color(84, 84, 107, (int)(255 * 0.9));

    /** Color for decoration line. */
    public static final Color ourTopDecorLineColor = new Color(0f, 0f, 0f, 0.85f);

    /** Disallow instantiation. */
    private ClassicHUDPalette()
    {
    }
}
