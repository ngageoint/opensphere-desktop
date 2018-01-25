package io.opensphere.csvcommon.detect.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

import io.opensphere.core.util.QuotingBufferedReader;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.csvcommon.common.LineSampler;
import io.opensphere.csvcommon.detect.controller.LineSamplerFactory;

/**
 * Creates a line sampler given a file.
 */
public final class LineSamplerFactoryImpl implements LineSamplerFactory
{
    /**
     * The file to sample.
     */
    private final File myFile;

    /**
     * Creates a new sampler factory.
     *
     * @param file The file to sample.
     */
    public LineSamplerFactoryImpl(File file)
    {
        myFile = file;
    }

    @Override
    public LineSampler createSampler(char[] textDelimiters) throws FileNotFoundException
    {
        QuotingBufferedReader quotingReader = new QuotingBufferedReader(
                new InputStreamReader(new FileInputStream(myFile), StringUtilities.DEFAULT_CHARSET), textDelimiters, null);
        LineSampler quotedSampler = new ReaderLineSampler(quotingReader, 100, 20);

        return quotedSampler;
    }
}
