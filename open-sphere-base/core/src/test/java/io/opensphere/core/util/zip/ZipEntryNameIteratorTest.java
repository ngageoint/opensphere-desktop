package io.opensphere.core.util.zip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.junit.Assert;
import org.junit.Test;

/** Test {@link ZipEntryNameIterator}. */
public class ZipEntryNameIteratorTest
{
    /**
     * Test {@link ZipEntryNameIterator}.
     *
     * @throws IOException If a test stream has an error.
     */
    @Test
    public void test() throws IOException
    {
        String name1 = "name1";
        String name2 = "name2";
        String name3 = "name3";

        byte[] bytes1 = new byte[100];
        byte[] bytes2 = new byte[100];
        byte[] bytes3 = new byte[100];

        Random rand = new Random();
        rand.nextBytes(bytes1);
        rand.nextBytes(bytes2);
        rand.nextBytes(bytes3);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);

        zos.putNextEntry(new ZipEntry(name1));
        zos.write(bytes1);
        zos.putNextEntry(new ZipEntry(name2));
        zos.write(bytes2);
        zos.putNextEntry(new ZipEntry(name3));
        zos.write(bytes3);

        zos.close();

        InputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ZipInputStream inputStream = new ZipInputStream(bais);

        ZipEntryNameIterator iter = new ZipEntryNameIterator(inputStream);
        Assert.assertTrue(iter.hasNext());
        Assert.assertTrue(iter.hasNext());
        Assert.assertEquals(name1, iter.next());
        Assert.assertTrue(iter.hasNext());
        Assert.assertEquals(name2, iter.next());
        Assert.assertTrue(iter.hasNext());
        Assert.assertEquals(name3, iter.next());
    }
}
