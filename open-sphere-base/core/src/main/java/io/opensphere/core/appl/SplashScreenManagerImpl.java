package io.opensphere.core.appl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.SplashScreen;
import java.awt.geom.Rectangle2D;

import org.apache.log4j.Logger;

import io.opensphere.core.SplashScreenManager;

/**
 * Implementation of {@link SplashScreenManager}.
 */
public class SplashScreenManagerImpl implements SplashScreenManager
{
    /** Prefix used for log messages. */
    public static final String INIT_MESSAGE_PREFIX = "Init message: ";

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(SplashScreenManagerImpl.class);

    @Override
    public void setInitMessage(String mesg)
    {
        SplashScreen splash = SplashScreen.getSplashScreen();
        if (splash == null)
        {
            LOGGER.info(INIT_MESSAGE_PREFIX + mesg);
        }
        else
        {
            Graphics2D splashGraphics = splash.createGraphics();
            if (splashGraphics != null && splash.isVisible())
            {
                splashGraphics.setColor(Color.BLACK);
                Rectangle2D bounds = splashGraphics.getFontMetrics().getStringBounds(mesg, splashGraphics);
                Dimension winSize = splash.getSize();
                splashGraphics.setBackground(new Color(0, 0, 0, 0));
                int borderWidth = 20;
                int clearHeight = (int)(bounds.getMaxY() - bounds.getMinY()) * 2;
                splashGraphics.clearRect(borderWidth, winSize.height - 20 - clearHeight / 2, winSize.width - 2 * borderWidth,
                        clearHeight);
                splashGraphics.drawString(mesg, winSize.width / 2.0f - (float)(bounds.getMaxX() - bounds.getMinX()) / 2.0f,
                        winSize.height - 20);
                splash.update();
            }
        }
    }
}
