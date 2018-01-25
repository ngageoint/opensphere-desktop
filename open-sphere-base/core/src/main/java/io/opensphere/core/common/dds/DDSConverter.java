package io.opensphere.core.common.dds;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

public class DDSConverter
{
    /**
     * A simple program that isolates the loading of a png file and the
     * conversion of that file.
     *
     * This is mainly for profiling the performance and quality of the image.
     *
     */

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException
    {
        DDSEncoder encoder = new DDSEncoder();
        RealTimeYCoCgEncoder highQualEncoder = new RealTimeYCoCgEncoder();

        int numberOfRepeats = Integer.parseInt(args[0]);
        ByteBuffer outputNotDirect = null;

        FileWriter fw = new FileWriter(
                System.getProperty("user.home") + System.getProperty("file.separator") + "convertTimeRealTimeEncoder.log", true);
        FileInputStream is = new FileInputStream(args[1]);
        BufferedImage bufferedSrcImage = javax.imageio.ImageIO.read(is);

        if (bufferedSrcImage.getType() == BufferedImage.TYPE_4BYTE_ABGR)
        {
            System.out.println("ABGR image...");
            encoder.setABGR();
        }
        if (bufferedSrcImage.getType() == BufferedImage.TYPE_INT_BGR
                || bufferedSrcImage.getType() == BufferedImage.TYPE_3BYTE_BGR)
        {
            System.out.println("BGR image...");
            encoder.setBGR();
        }

        DataBufferByte buff = (DataBufferByte)bufferedSrcImage.getData().getDataBuffer();
        byte[] sourceArray = buff.getData();
        final int destDataSize = (int)((bufferedSrcImage.getHeight() * bufferedSrcImage.getWidth()));
        outputNotDirect = ByteBuffer.allocate(destDataSize + DDSEncoder.DDS_HEADER_SIZE);
        outputNotDirect.order(ByteOrder.LITTLE_ENDIAN);

        long timein = System.currentTimeMillis();
        for (int i = 0; i < numberOfRepeats; i++)
        {
            encoder.encodeDDS(512, 512, DDSEncoder.CompressionType.DXT5, sourceArray, outputNotDirect);
            // outputNotDirect = encoder.encodeDDS_safe(bufferedSrcImage, true);
            outputNotDirect.flip();
        }
        fw.append(((System.currentTimeMillis() - timein) / numberOfRepeats) + "\n");
        fw.flush();

        FileOutputStream os = new FileOutputStream(args[2]);
        FileChannel out = os.getChannel();
        out.write(outputNotDirect);

        ByteBuffer outputNotDirect2 = null;
        fw = new FileWriter(
                System.getProperty("user.home") + System.getProperty("file.separator") + "convertTimeRealTimeYCoCgEncoder.log",
                true);

        is = new FileInputStream(args[1]);
        bufferedSrcImage = javax.imageio.ImageIO.read(is);

        if (bufferedSrcImage.getType() == BufferedImage.TYPE_4BYTE_ABGR)
        {
            System.out.println("ABGR image...");
            highQualEncoder.setABGR();
        }
        if (bufferedSrcImage.getType() == BufferedImage.TYPE_INT_BGR
                || bufferedSrcImage.getType() == BufferedImage.TYPE_3BYTE_BGR)
        {
            System.out.println("BGR image...");
            highQualEncoder.setBGR();
        }

        buff = (DataBufferByte)bufferedSrcImage.getData().getDataBuffer();
        sourceArray = buff.getData();

        outputNotDirect2 = ByteBuffer.allocate(destDataSize + DDSEncoder.DDS_HEADER_SIZE);
        outputNotDirect2.order(ByteOrder.LITTLE_ENDIAN);

        timein = System.currentTimeMillis();

        for (int i = 0; i < numberOfRepeats; i++)
        {
            highQualEncoder.encodeDDS(512, 512, RealTimeYCoCgEncoder.CompressionType.DXT5_YCoCg, sourceArray, outputNotDirect2);
            outputNotDirect2.flip();
        }
        fw.append(((System.currentTimeMillis() - timein) / numberOfRepeats) + "\n");
        fw.flush();
        FileOutputStream os2 = new FileOutputStream(args[3]);
        FileChannel out2 = os2.getChannel();
        out2.write(outputNotDirect2);

        System.out.println("done...");
    }
}
