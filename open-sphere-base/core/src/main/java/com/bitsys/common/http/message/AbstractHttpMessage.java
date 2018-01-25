package com.bitsys.common.http.message;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;

import com.bitsys.common.http.util.StringMatcherIgnoreCase;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

/**
 * This class provides the basic implementation for an {@link HttpMessage}.
 */
public abstract class AbstractHttpMessage implements HttpMessage
{
   /**
    * The HTTP headers for this message.
    */
   private final ListMultimap<String, String> headers;

   /**
    * Constructs a <code>AbstractHttpMessage</code>.
    */
   public AbstractHttpMessage()
   {
      headers = LinkedListMultimap.create();
   }

   @Override
   public ListMultimap<String, String> getHeaders()
   {
      return headers;
   }

   @Override
   public Multimap<String, String> getHeader(final String name)
   {
      return Multimaps.filterKeys(headers, new StringMatcherIgnoreCase(name));
   }

   @Override
   public String getHeaderValue(final String name)
   {
      final Multimap<String, String> matchingHeaders = getHeader(name);
      final Collection<String> values = matchingHeaders.values();

      final String value;
      if (values.isEmpty())
      {
         value = null;
      }

      // According to RFC 2616, if multiple message-header fields with the same field-name are
      // present, it MUST be possible to combine the multiple header fields into one pair by
      // combining the field values separated by a comma.
      else
      {
         value = StringUtils.join(values, ',');
      }
      return value;
   }
}
