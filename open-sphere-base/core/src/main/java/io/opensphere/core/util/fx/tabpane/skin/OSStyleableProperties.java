package io.opensphere.core.util.fx.tabpane.skin;

import io.opensphere.core.util.fx.tabpane.OSTabAnimation;
import javafx.beans.value.WritableValue;
import javafx.css.CssMetaData;
import javafx.css.StyleableProperty;
import javafx.css.converter.EnumConverter;
import javafx.scene.control.TabPane;

/**
 * A class in which the set of stylable properties are encapsulated.
 */
public class OSStyleableProperties
{
    /** The animation used when opening a new tab. */
    public final static CssMetaData<TabPane, OSTabAnimation> OPEN_TAB_ANIMATION = new CssMetaData<>("-fx-open-tab-animation",
            new EnumConverter<>(OSTabAnimation.class), OSTabAnimation.GROW)
    {
        @Override
        public boolean isSettable(TabPane node)
        {
            return true;
        }

        @Override
        public StyleableProperty<OSTabAnimation> getStyleableProperty(TabPane node)
        {
            OSTabPaneSkin skin = (OSTabPaneSkin)node.getSkin();
            return (StyleableProperty<OSTabAnimation>)(WritableValue<OSTabAnimation>)skin.openTabAnimationProperty();
        }
    };

    /** The animation used when closing a tab. */
    public final static CssMetaData<TabPane, OSTabAnimation> CLOSE_TAB_ANIMATION = new CssMetaData<>("-fx-close-tab-animation",
            new EnumConverter<>(OSTabAnimation.class), OSTabAnimation.GROW)
    {
        @Override
        public boolean isSettable(TabPane node)
        {
            return true;
        }

        @Override
        public StyleableProperty<OSTabAnimation> getStyleableProperty(TabPane node)
        {
            OSTabPaneSkin skin = (OSTabPaneSkin)node.getSkin();
            return (StyleableProperty<OSTabAnimation>)(WritableValue<OSTabAnimation>)skin.closeTabAnimationProperty();
        }
    };
}
