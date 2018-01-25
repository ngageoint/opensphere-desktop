package com.bitsys.common.http.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

/**
 * This class assists in converting to/from Apache HttpClient classes.
 */
public final class ConversionUtils
{
   /**
    * Constructs a <code>ConversionUtils</code>.
    */
   private ConversionUtils()
   {
   }

   /**
    * Converts an Apache {@link NameValuePair} to a {@link Pair}.
    *
    * @param pair
    *           the Apache pair to convert.
    * @return the converted pair.
    */
   public static Pair<String, String> toPair(final NameValuePair pair)
   {
      return new ImmutablePair<>(pair.getName(), pair.getValue());
   }

   /**
    * Converts an {@link Entry} to an Apache {@link NameValuePair}.
    *
    * @param entry
    *           the entry to convert.
    * @return the converted Apache pair.
    */
   public static NameValuePair toNameValuePair(final Entry<String, String> entry)
   {
      return new BasicNameValuePair(entry.getKey(), entry.getValue());
   }

   /**
    * Converts the iterable of {@link Entry Entries} to a collection of Apache
    * {@link NameValuePair}s.
    *
    * @param entries
    *           the entries to convert.
    * @return the converted pair.
    */
   public static Collection<? extends NameValuePair> toNameValuePairs(final Iterable<? extends Entry<String, String>> entries)
   {
      final List<NameValuePair> apacheList = new ArrayList<>();
      for (final Entry<String, String> entry : entries)
      {
         apacheList.add(toNameValuePair(entry));
      }
      return apacheList;
   }
}
