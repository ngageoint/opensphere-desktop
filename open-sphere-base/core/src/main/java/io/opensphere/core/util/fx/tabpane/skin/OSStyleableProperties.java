package io.opensphere.core.util.fx.tabpane.skin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.opensphere.core.util.fx.tabpane.OSTabAnimation;
import javafx.beans.value.WritableValue;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import javafx.css.converter.EnumConverter;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TabPane;

/* Super-lazy instantiation pattern from Bill Pugh. */
class OSStyleableProperties
{
    private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

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

    static
    {
        final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(SkinBase.getClassCssMetaData());
        styleables.add(OPEN_TAB_ANIMATION);
        styleables.add(CLOSE_TAB_ANIMATION);
        STYLEABLES = Collections.unmodifiableList(styleables);
    }
}
