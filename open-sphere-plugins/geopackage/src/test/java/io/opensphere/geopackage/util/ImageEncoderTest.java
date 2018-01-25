package io.opensphere.geopackage.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.junit.Ignore;
import org.junit.Test;

import io.opensphere.core.image.Image;
import io.opensphere.core.image.ImageFormatUnknownException;
import io.opensphere.core.util.image.ImageUtil;
import io.opensphere.core.util.io.StreamReader;

/**
 * Unit test for the {@link ImageEncoder} class.
 */
public class ImageEncoderTest
{
    /**
     * Tests encoding a jpg image to dds image.
     *
     * @throws IOException Bad IO.
     * @throws ImageFormatUnknownException Bad format.
     */
    @Test
    @Ignore
    public void testEncodeJpg() throws IOException, ImageFormatUnknownException
    {
        BufferedImage image = ImageUtil.BROKEN_IMAGE;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", output);

        byte[] imageBytes = output.toByteArray();

        ImageEncoder encoder = new ImageEncoder();
        InputStream ddsImage = encoder.encodeImage(imageBytes);

        InputStream expectedStream = Image.getDDSImageStream(new ByteArrayInputStream(imageBytes), "jpg", imageBytes.length,
                null);

        StreamReader reader = new StreamReader(ddsImage);
        ByteArrayOutputStream actual = new ByteArrayOutputStream();
        reader.copyStream(actual);

        reader = new StreamReader(expectedStream);
        ByteArrayOutputStream expected = new ByteArrayOutputStream();
        reader.copyStream(expected);

        assertTrue(expected.size() > 0);
        assertArrayEquals(expected.toByteArray(), actual.toByteArray());
    }

    /**
     * Tests encoding the png to a dds image.
     *
     * @throws IOException Bad IO.
     * @throws ImageFormatUnknownException Bad format.
     */
    @Test
    @Ignore
    public void testEncodePng() throws IOException, ImageFormatUnknownException
    {
        BufferedImage image = ImageUtil.BROKEN_IMAGE;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);

        byte[] imageBytes = output.toByteArray();

        ImageEncoder encoder = new ImageEncoder();
        InputStream ddsImage = encoder.encodeImage(imageBytes);

        InputStream expectedStream = Image.getDDSImageStream(new ByteArrayInputStream(imageBytes), "png", imageBytes.length,
                null);

        StreamReader reader = new StreamReader(ddsImage);
        ByteArrayOutputStream actual = new ByteArrayOutputStream();
        reader.copyStream(actual);

        reader = new StreamReader(expectedStream);
        ByteArrayOutputStream expected = new ByteArrayOutputStream();
        reader.copyStream(expected);

        assertTrue(expected.size() > 0);
        assertArrayEquals(expected.toByteArray(), actual.toByteArray());
    }

    /**
     * Tests encoding an unrecognized image.
     *
     * @throws IOException Bad IO.
     */
    @Test
    public void testEncodeUnrecognized() throws IOException
    {
        byte[] imageBytes = new byte[] { 1, 2, 3, 4 };

        ImageEncoder encoder = new ImageEncoder();
        InputStream ddsImage = encoder.encodeImage(imageBytes);

        StreamReader reader = new StreamReader(ddsImage);
        ByteArrayOutputStream actual = new ByteArrayOutputStream();
        reader.copyStream(actual);

        assertArrayEquals(imageBytes, actual.toByteArray());
    }
}
