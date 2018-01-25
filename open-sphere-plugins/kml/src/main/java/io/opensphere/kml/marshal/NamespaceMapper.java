package io.opensphere.kml.marshal;

import java.util.regex.Pattern;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

/**
 * The namespace prefix mapper used by the KmlMarshaller.
 *
 */
public class NamespaceMapper extends NamespacePrefixMapper
{
    /** Pattern for Atom namespace. */
    private final Pattern myAtomPattern = Pattern.compile("http://www.w3.org/\\d{4}/Atom");

    /** Pattern for gx namespace. */
    private final Pattern myGXPattern = Pattern.compile("http://www.google.com/kml/ext/.*?");

    /** Pattern for xAL namespace. */
    private final Pattern myXALPattern = Pattern.compile("urn:oasis:names:tc:ciq:xsdschema:xAL:.*?");

    @Override
    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix)
    {
        if (myAtomPattern.matcher(namespaceUri).matches())
        {
            return "atom";
        }
        if (myXALPattern.matcher(namespaceUri).matches())
        {
            return "xal";
        }
        if (myGXPattern.matcher(namespaceUri).matches())
        {
            return "gx";
        }
        return null;
    }
}
