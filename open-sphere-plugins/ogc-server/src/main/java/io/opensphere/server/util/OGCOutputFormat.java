package io.opensphere.server.util;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** An enumeration to define and prioritize OutputFormats. */
public enum OGCOutputFormat
{
    /** The Output Format for GML 3.1.1 formatted data from an OGC Server. */
    GML_311("text/xml; subtype=gml/3.1.1", 20, false),

    /** The Output Format for Java Serialized objects from a custom Server. */
    JAVA_OBJECT("application/x-java-serialized-object", 11, false),

    /** The Output format for Feature Streaming Java Serialized objects. */
    STREAMING_FEATURE_JAVA_OBJECT("application/x-java-serialized-object-2", 10, true),

    /**
     * Avro batch mode with no compression. This is a stand-in with a bogus
     * MIME-type that is not currently used by the server.
     */
    AVRO_B("avro/binary-batch;null", 4, false),

    /**
     * Avro batch mode with "deflate" compression. This is a stand-in with a
     * bogus MIME-type that is not currently used by the server.
     */
    AVRO_B_DEFLATE("avro/binary-batch;deflate", 3, false),

    /** Avro streaming format with no compression. */
    AVRO("avro/binary;null", 2, true),

    // Avro format with "snappy" compression, currently not supported.
    // AVRO_BIN_SNAPPY("avro/binary;snappy", 1, true),

    /** Avro streaming format with "deflate" compression. */
    AVRO_DEFLATE("avro/binary;deflate", 0, true);

    /** Map the members by format string for easy conversion. */
    private static final Map<String, OGCOutputFormat> FMTMAP = new TreeMap<>();
    static
    {
        for (OGCOutputFormat f : values())
        {
            FMTMAP.put(f.fmtString, f);
        }
    }

    /** Comparator that orders OutputFormats from most to least preferable. */
    private static final Comparator<OGCOutputFormat> FORMAT_COMPARATOR = (left, right) -> Integer.compare(left.prefOrd,
            right.prefOrd);

    /** My format string. */
    private final String fmtString;

    /** The numerical position in the preference ordering. */
    private final int prefOrd;

    /** Indicator of whether this is a streaming format. */
    private final boolean myStreaming;

    /**
     * Gets the default format.
     *
     * @return the default format
     */
    public static OGCOutputFormat getDefaultFormat()
    {
        return GML_311;
    }

    /**
     * Get the most preferred format from a list of formats. This method takes
     * the provided collection of formats, narrows that list to those supported
     * by this class and returns the first entry in the list after sorting by
     * order of preference.
     *
     * @param formats the iterable collection of output formats from which to
     *            choose the preferred format.
     * @return the preferred format from the provided list
     */
    public static OGCOutputFormat getPreferredFormat(List<String> formats)
    {
        return formats.stream().map(f -> FMTMAP.get(f)).filter(f -> f != null).collect(Collectors.minBy(FORMAT_COMPARATOR))
                .orElse(getDefaultFormat());
    }

    /**
     * Tell whether the output format represented by the given string
     * represented format is the same as the streaming format.
     *
     * @param outputFormat the string represented format
     * @return true when the output format is the streaming format.
     */
    public static boolean isStreaming(String outputFormat)
    {
        return FMTMAP.get(outputFormat).isStreaming();
    }

    /**
     * Private constructor for enum elements.
     *
     * @param formatString the format string
     * @param ord the preference ordinal
     * @param streaming a flag used to indicate that the output format supports
     *            streaming.
     */
    private OGCOutputFormat(String formatString, int ord, boolean streaming)
    {
        fmtString = formatString;
        prefOrd = ord;
        myStreaming = streaming;
    }

    /**
     * Gets the format string.
     *
     * @return the format string
     */
    public String getFormatString()
    {
        return fmtString;
    }

    /**
     * Tests to determine if the output format supports streaming.
     *
     * @return true if the output format is streaming, false otherwise.
     */
    public boolean isStreaming()
    {
        return myStreaming;
    }
}
