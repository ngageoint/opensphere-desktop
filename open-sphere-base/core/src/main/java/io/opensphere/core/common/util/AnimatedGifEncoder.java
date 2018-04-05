package io.opensphere.core.common.util;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Class AnimatedGifEncoder - Encodes a GIF file consisting of one or more
 * frames.
 *
 * No copyright asserted on the source code of this class. May be used for any
 * purpose, however, refer to the Unisys LZW patent for restrictions on use of
 * the associated LZWEncoder class. Please forward any corrections to
 * kweiner@fmsware.com.
 *
 * @author Kevin Weiner, FM Software
 * @version 1.03 November 2003
 */
public class AnimatedGifEncoder
{
    // Start BITS added code.
    public enum QUANTIZATION
    {
        OCTAL_TREE, NEURAL;
    }

    /**
     * The image width to which to scale the image or -1 if no scaling is
     * desired.
     */
    private final int myScaleToWidth;

    /**
     * The image height to which to scale the image or -1 if no scaling is
     * desired.
     */
    private final int myScaleToHeight;

    /** The bytes from the previous image added to the gif. */
    private byte[] myPreviousBytes;

    private boolean[] mySameAsPrevious;

    /**
     * Constructor.
     *
     * @param file Output file name for the generated gif.
     * @param frameInterval Interval between frames is milliseconds.
     * @param width The image width to which to scale the image or -1 if no
     *            scaling is desired.
     * @param height The image height to which to scale the image or -1 if no
     *            scaling is desired.
     */
    public AnimatedGifEncoder(String file, int frameIntervalMS, int width, int height)
    {
        myScaleToWidth = width;
        myScaleToHeight = height;
        start(file);
        setDelay(frameIntervalMS);
        setRepeat(0);
    }

    /**
     * Constructor.
     *
     * @param file Output file name for the generated gif.
     * @param frameInterval Interval between frames is milliseconds.
     * @param width The image width to which to scale the image or -1 if no
     *            scaling is desired.
     * @param height The image height to which to scale the image or -1 if no
     *            scaling is desired.
     */
    public AnimatedGifEncoder(File file, int frameIntervalMS, int width, int height)
    {
        myScaleToWidth = width;
        myScaleToHeight = height;
        start(file);
        setDelay(frameIntervalMS);
        setRepeat(0);
    }

    /**
     * Convert the image to BufferedImage.TYPE_3BYTE_BGR and scale if necessary.
     *
     * @param anImage The image to be converted.
     * @return the image after conversion.
     */
    private BufferedImage convertImage(BufferedImage anImage)
    {
        int scaleWidth = anImage.getWidth();
        int scaleHeight = anImage.getHeight();
        boolean scale = false;
        if (myScaleToWidth > 0 || myScaleToHeight > 0)
        {
            if (myScaleToWidth != scaleWidth || myScaleToHeight != scaleHeight)
            {
                scaleWidth = myScaleToWidth;
                scaleHeight = myScaleToHeight;
                scale = true;
            }
        }
        BufferedImage converted = anImage;
        if (scale)
        {
            Image scaledImage = anImage.getScaledInstance(scaleWidth, scaleHeight, Image.SCALE_SMOOTH);
            converted = new BufferedImage(scaleWidth, scaleHeight, BufferedImage.TYPE_3BYTE_BGR);
            Graphics graphics = converted.getGraphics();
            graphics.drawImage(scaledImage, 0, 0, null);
            graphics.dispose();
        }
        else if (anImage.getType() != BufferedImage.TYPE_3BYTE_BGR)
        {
            converted = new BufferedImage(scaleWidth, scaleHeight, BufferedImage.TYPE_3BYTE_BGR);
            Graphics2D graphics = converted.createGraphics();
            graphics.drawImage(anImage, 0, 0, null);
            graphics.dispose();
        }
        return converted;
    }

    /**
     * Insert the transparent color for pixels which match the previous frame.
     * Also ensure that no colors are an exact match for the transparent color.
     *
     * @param src The image to process.
     * @return the image after processing.
     */
    private BufferedImage preProcessImage(BufferedImage src)
    {
        BufferedImage source = convertImage(src);
        pixels = ((DataBufferByte)source.getData().getDataBuffer()).getData();

        int nPix = pixels.length / 3;
        mySameAsPrevious = new boolean[nPix];
        for (int i = 0; i < nPix; ++i)
        {
            if (myPreviousBytes == null)
            {
                mySameAsPrevious[i] = false;
            }
            else
            {
                int pos = i * 3;
                if (myPreviousBytes[pos] == pixels[pos] && myPreviousBytes[pos + 1] == pixels[pos + 1]
                        && myPreviousBytes[pos + 2] == pixels[pos + 2])
                {
                    mySameAsPrevious[i] = true;
                }
            }
        }

        myPreviousBytes = pixels;
        return source;
    }

    /**
     *
     * @param colorModel
     */
    protected void analyzePixelsOctTree()
    {
        int len = pixels.length;
        int nPix = len / 3;
        indexedPixels = new byte[nPix];
        colorTab = new byte[256 * 3];

        PaletteBuilderSizable palBuilder = new PaletteBuilderSizable(image, 255);
        palBuilder.getColorTableAndIndexedPixels(colorTab, indexedPixels);
        transIndex = 255;

        int k = 0;
        for (int i = 0; i < nPix; ++i)
        {
            if (mySameAsPrevious[i])
            {
                indexedPixels[i] = (byte)transIndex;
                k += 3;
            }
        }

        pixels = null;
        colorDepth = 8;
        palSize = 7;
    }

    /**
     * Main for testing. To check for correctness examine the frames individual
     * of resulting gif (gimp can be used for this).
     *
     * @param args The only argument used is the first one; it needs to be the
     *            fully qualified path for the output file.
     */
    public static void main(String[] args)
    {
        JDialog dialog = new JDialog();
        JPanel top = new JPanel(new FlowLayout());
        dialog.setContentPane(top);
        top.add(new JButton("Test Button"));
        JCheckBox check = new JCheckBox("Test check");
        top.add(check);
        JLabel red = new JLabel("r");
        red.setForeground(Color.red);
        top.add(red);
        JLabel green = new JLabel("g");
        green.setForeground(Color.green);
        top.add(green);
        JLabel blue = new JLabel("b");
        blue.setForeground(Color.blue);
        top.add(blue);
        dialog.pack();
        dialog.setVisible(true);

        try
        {
            AnimatedGifEncoder recorder = new AnimatedGifEncoder(args[0], 500, -1, -1);

            for (int i = 0; i < 20; ++i)
            {
                if (i == 3)
                {
                    top.setBackground(Color.black);
                    top.validate();
                }
                BufferedImage frame = new BufferedImage(top.getWidth(), top.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
                Graphics2D g = frame.createGraphics();
                top.paint(g);
                recorder.addFrame(frame, QUANTIZATION.NEURAL);
            }

            recorder.finish();
        }
        catch (Exception e)
        {
            System.err.println("Failed to encode image." + e);
        }

        dialog.setVisible(false);
        System.exit(0);
    }

    // End BITS added code. Below this line I have tried to keep as much of the
    // original code as possible and comment out code instead of deleting. The
    // changes are intended to only affect how transparency is handled.

    /** image size */
    protected int width;

    protected int height;

// transparent color if given
//    protected Color transparent = null;

    /** transparent index in color table */
    protected int transIndex;

    /** no repeat */
    protected int repeat = -1;

    /** frame delay (hundredths) */
    protected int delay = 0;

    /** ready to output frames */
    protected boolean started = false;

    protected OutputStream out;

    /** current frame */
    protected BufferedImage image;

    /** BGR byte array from frame */
    protected byte[] pixels;

    /** converted frame indexed to palette */
    protected byte[] indexedPixels;

    /** number of bit planes */
    protected int colorDepth;

    /** RGB palette */
    protected byte[] colorTab;

    /** active palette entries */
    protected boolean[] usedEntry = new boolean[256];

    /** color table size (bits-1) */
    protected int palSize = 7;

    /** disposal code (-1 = use default) */
    protected int dispose = -1;

    /** close stream when finished */
    protected boolean closeStream = false;

    protected boolean firstFrame = true;

    /** if false, get size from first frame */
    protected boolean sizeSet = false;

    /** default sample interval for quantizer */
    protected int sample = 10;

    /**
     * Sets the delay time between each frame, or changes it for subsequent
     * frames (applies to last frame added).
     *
     * @param ms int delay time in milliseconds
     */
    public void setDelay(int ms)
    {
        delay = Math.round(ms / 10.0f);
    }

    /**
     * Sets the GIF frame disposal code for the last added frame and any
     * subsequent frames. Default is 0 if no transparent color has been set,
     * otherwise 2.
     *
     * @param code int disposal code.
     */
    public void setDispose(int code)
    {
        if (code >= 0)
        {
            dispose = code;
        }
    }

    /**
     * Sets the number of times the set of GIF frames should be played. Default
     * is 1; 0 means play indefinitely. Must be invoked before the first image
     * is added.
     *
     * @param iter int number of iterations.
     * @return
     */
    public void setRepeat(int iter)
    {
        if (iter >= 0)
        {
            repeat = iter;
        }
    }

//    /**
//     * Sets the transparent color for the last added frame and any subsequent
//     * frames. Since all colors are subject to modification in the quantization
//     * process, the color in the final palette for each frame closest to the
//     * given color becomes the transparent color for that frame. May be set to
//     * null to indicate no transparent color.
//     *
//     * @param c Color to be treated as transparent on display.
//     */
//    public void setTransparent(Color c)
//    {
//        transparent = c;
//    }

    public boolean addFrame(BufferedImage ima)
    {
        return addFrame(ima, QUANTIZATION.OCTAL_TREE);
    }

    /**
     * Adds next GIF frame. The frame is not written immediately, but is
     * actually deferred until the next frame is received so that timing data
     * can be inserted. Invoking <code>finish()</code> flushes all frames. If
     * <code>setSize</code> was not invoked, the size of the first image is used
     * for all subsequent frames.
     *
     * @param ima BufferedImage containing frame to write.
     * @param quant The quantization method to use.
     * @return true if successful.
     */
    public boolean addFrame(BufferedImage ima, QUANTIZATION quant)
    {
        BufferedImage im = preProcessImage(ima);
        if (im == null || !started)
        {
            return false;
        }
        boolean ok = true;
        try
        {
            if (!sizeSet)
            {
                // use first frame's size
                setSize(im.getWidth(), im.getHeight());
            }
            image = im;
            if (quant == QUANTIZATION.OCTAL_TREE)
            {
                analyzePixelsOctTree();
            }
            else
            {
                // build color table & map pixels
                analyzePixels();
            }

            if (firstFrame)
            {
                // logical screen descriptior
                writeLSD();
                // global color table
                writePalette();
                if (repeat >= 0)
                {
                    // use NS app extension to indicate reps
                    writeNetscapeExt();
                }
            }
            // write graphic control extension
            writeGraphicCtrlExt();
            // image descriptor
            writeImageDesc();
            if (!firstFrame)
            {
                // local color table
                writePalette();
            }
            // encode and write pixel data
            writePixels();
            firstFrame = false;
        }
        catch (IOException e)
        {
            ok = false;
        }

        return ok;
    }

    /**
     * Flushes any pending data and closes output file. If writing to an
     * OutputStream, the stream is not closed.
     */
    public boolean finish()
    {
        if (!started)
        {
            return false;
        }
        boolean ok = true;
        started = false;
        try
        {
            // gif trailer
            out.write(0x3b);
            out.flush();
            if (closeStream)
            {
                out.close();
            }
        }
        catch (IOException e)
        {
            ok = false;
        }

        // reset for subsequent use
        transIndex = 0;
        out = null;
        image = null;
        pixels = null;
        indexedPixels = null;
        colorTab = null;
        closeStream = false;
        firstFrame = true;

        return ok;
    }

    /**
     * Sets frame rate in frames per second. Equivalent to
     * <code>setDelay(1000/fps)</code>.
     *
     * @param fps float frame rate (frames per second)
     */
    public void setFrameRate(float fps)
    {
        if (fps != 0f)
        {
            delay = Math.round(100f / fps);
        }
    }

    /**
     * Sets quality of color quantization (conversion of images to the maximum
     * 256 colors allowed by the GIF specification). Lower values (minimum = 1)
     * produce better colors, but slow processing significantly. 10 is the
     * default, and produces good color mapping at reasonable speeds. Values
     * greater than 20 do not yield significant improvements in speed.
     *
     * @param quality int greater than 0.
     * @return
     */
    public void setQuality(int quality)
    {
        if (quality < 1)
        {
            quality = 1;
        }
        sample = quality;
    }

    /**
     * Sets the GIF frame size. The default size is the size of the first frame
     * added if this method is not invoked.
     *
     * @param w int frame width.
     * @param h int frame width.
     */
    public void setSize(int w, int h)
    {
        if (started && !firstFrame)
        {
            return;
        }
        width = w;
        height = h;
        if (width < 1)
        {
            width = 320;
        }
        if (height < 1)
        {
            height = 240;
        }
        sizeSet = true;
    }

    /**
     * Initiates GIF file creation on the given stream. The stream is not closed
     * automatically.
     *
     * @param os OutputStream on which GIF images are written.
     * @return false if initial write failed.
     */
    public boolean start(OutputStream os)
    {
        if (os == null)
        {
            return false;
        }
        boolean ok = true;
        closeStream = false;
        out = os;
        try
        {
            // header
            writeString("GIF89a");
        }
        catch (IOException e)
        {
            ok = false;
        }
        return started = ok;
    }

    /**
     * Initiates writing of a GIF file with the specified name.
     *
     * @param file String containing output file name.
     * @return false if open or initial write failed.
     */
    public boolean start(String file)
    {
        boolean ok = true;
        try
        {
            out = new BufferedOutputStream(new FileOutputStream(file));
            ok = start(out);
            closeStream = true;
        }
        catch (IOException e)
        {
            ok = false;
        }
        return started = ok;
    }

    public boolean start(File file)
    {
        boolean ok = true;
        try
        {
            out = new BufferedOutputStream(new FileOutputStream(file));
            ok = start(out);
            closeStream = true;
        }
        catch (IOException e)
        {
            ok = false;
        }
        return started = ok;
    }

    /**
     * Analyzes image colors and creates color map.
     */
    protected void analyzePixels()
    {
        int len = pixels.length;
        int nPix = len / 3;
        indexedPixels = new byte[nPix];
        NeuQuant nq = new NeuQuant(pixels, len, sample);
        byte[] tempColorTab = nq.process();
        colorTab = new byte[256 * 3];
        // The transparent index will be the first entry, so leave the first
        // triplet blank.
        System.arraycopy(tempColorTab, 0, colorTab, 3, 255 * 3);

        // convert map from BGR to RGB
        for (int i = 0; i < colorTab.length; i += 3)
        {
            byte temp = colorTab[i];
            colorTab[i] = colorTab[i + 2];
            colorTab[i + 2] = temp;
            int entryNum = i / 3;
            usedEntry[entryNum] = false;
        }

        transIndex = 0;
        usedEntry[0] = true;

        // map image pixels to new palette
        int k = 0;
        int nonblank = 0;
        for (int i = 0; i < nPix; ++i)
        {
            if (mySameAsPrevious[i])
            {
                indexedPixels[i] = (byte)transIndex;
                k += 3;
            }
            else
            {
                int index = nq.map(pixels[k++] & 0xff, pixels[k++] & 0xff, pixels[k++] & 0xff) + 1;
                usedEntry[index] = true;
                indexedPixels[i] = (byte)index;
                ++nonblank;
            }
        }

        pixels = null;
        colorDepth = 8;
        palSize = 7;

//        // get closest match to transparent color if specified
//        if (transparent != null)
//        {
//            transIndex = findClosest(transparent);
//        }
    }

    /**
     * Returns index of palette color closest to c
     *
     */
    protected int findClosest(Color c)
    {
        if (colorTab == null)
        {
            return -1;
        }
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();
        int minpos = 0;
        int dmin = 256 * 256 * 256;
        int len = colorTab.length;
        for (int i = 0; i < len;)
        {
            int dr = r - (colorTab[i++] & 0xff);
            int dg = g - (colorTab[i++] & 0xff);
            int db = b - (colorTab[i] & 0xff);
            int d = dr * dr + dg * dg + db * db;
            int index = i / 3;
            if (usedEntry[index] && d < dmin)
            {
                dmin = d;
                minpos = index;
            }
            i++;
        }
        return minpos;
    }

    /**
     * Extracts image pixels into byte array "pixels"
     */
//    protected void getImagePixels()
//    {
//        int w = image.getWidth();
//        int h = image.getHeight();
//        int type = image.getType();
//        if (w != width || h != height || type != BufferedImage.TYPE_3BYTE_BGR)
//        {
//            // create new image with right size/format
//            BufferedImage temp = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
//            Graphics2D g = temp.createGraphics();
//            g.drawImage(image, 0, 0, null);
//            image = temp;
//        }
//        pixels = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
//    }

    /**
     * Writes Graphic Control Extension
     */
    protected void writeGraphicCtrlExt() throws IOException
    {
        // extension introducer
        out.write(0x21);
        // GCE label
        out.write(0xf9);
        // data block size
        out.write(4);
        int transp, disp;
//        if (transparent == null)
//        {
//            transp = 0;
        // dispose = no action
//            disp = 0;
//        }
//        else
//        {
        transp = 1;
        // force clear if using transparent color
//            disp = 2;
        // changed this to not dispose the image.
        disp = 0;
//        }
        if (dispose >= 0)
        {
            // user override
            disp = dispose & 7;
        }
        disp <<= 2;

        // packed fields:
        // 1:3 reserved
        // 4:6 disposal
        // 7 user input - 0 = none
        // 8 transparency flag
        out.write(0 | disp | 0 | transp);

        // delay x 1/100 sec
        writeShort(delay);
        // transparent color index
        out.write(transIndex);
        // block terminator
        out.write(0);
    }

    /**
     * Writes Image Descriptor
     */
    protected void writeImageDesc() throws IOException
    {
        // image separator
        out.write(0x2c);
        // image position x,y = 0,0
        writeShort(0);
        writeShort(0);
        // image size
        writeShort(width);
        writeShort(height);
        // packed fields
        if (firstFrame)
        {
            // no LCT - GCT is used for first (or only) frame
            out.write(0);
        }
        else
        {
            // specify normal LCT

            // 1 local color table 1=yes
            // 2 interlace - 0=no
            // 3 sorted - 0=no
            // 4-5 reserved
            // 6-8 size of color table
            out.write(0x80 | 0 | 0 | 0 | palSize);
        }
    }

    /**
     * Writes Logical Screen Descriptor
     */
    protected void writeLSD() throws IOException
    {
        // logical screen size
        writeShort(width);
        writeShort(height);
        // packed fields
        // 1 : global color table flag = 1 (gct used)
        // 2-4 : color resolution = 7
        // 5 : gct sort flag = 0
        // 6-8 : gct size
        out.write(0x80 | 0x70 | 0x00 | palSize);

        // background color index
        out.write(0);
        // pixel aspect ratio - assume 1:1
        out.write(0);
    }

    /**
     * Writes Netscape application extension to define repeat count.
     */
    protected void writeNetscapeExt() throws IOException
    {
        // extension introducer
        out.write(0x21);
        // app extension label
        out.write(0xff);
        // block size
        out.write(11);
        // app id + auth code
        writeString("NETSCAPE" + "2.0");
        // sub-block size
        out.write(3);
        // loop sub-block id
        out.write(1);
        // loop count (extra iterations, 0=repeat forever)
        writeShort(repeat);
        // block terminator
        out.write(0);
    }

    /**
     * Writes color table
     */
    protected void writePalette() throws IOException
    {
        out.write(colorTab, 0, colorTab.length);
        int n = 3 * 256 - colorTab.length;
        for (int i = 0; i < n; i++)
        {
            out.write(0);
        }
    }

    /**
     * Encodes and writes pixel data
     */
    protected void writePixels() throws IOException
    {
        LZWEncoder encoder = new LZWEncoder(width, height, indexedPixels, colorDepth);
        encoder.encode(out);
    }

    /**
     * Write 16-bit value to output stream, LSB first
     */
    protected void writeShort(int value) throws IOException
    {
        out.write(value & 0xff);
        out.write(value >> 8 & 0xff);
    }

    /**
     * Writes string to output stream
     */
    protected void writeString(String s) throws IOException
    {
        for (int i = 0; i < s.length(); i++)
        {
            out.write((byte)s.charAt(i));
        }
    }
}
