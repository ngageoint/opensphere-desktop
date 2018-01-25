package io.opensphere.csvcommon.detect.basic;

import gnu.trove.map.hash.TCharIntHashMap;
import gnu.trove.set.hash.TCharHashSet;
import io.opensphere.csvcommon.common.LineSampler;
import io.opensphere.csvcommon.detect.LineDetector;
import io.opensphere.csvcommon.detect.ValueWithConfidence;
import io.opensphere.csvcommon.detect.ValuesWithConfidence;

/**
 * Module for detecting the comment character.
 */
public class CommentDetector implements LineDetector<Character>
{
    /**
     * The threshold percentage of lines commented out before we say the comment
     * character is not the comment character.
     */
    private static final float ourCommentThreshold = 0.5f;

    /**
     * The characters that are not considered as potential comment characters
     * (in addition to letters and digits).
     */
    private final TCharHashSet myCharactersToIgnore = new TCharHashSet();

    /**
     * Gets the confidence multiplier for the given character.
     *
     * @param ch the character
     * @return the score multiplier
     */
    private static float getConfidenceMultiplier(char ch)
    {
        final float nonHashMultiplier = 0.9f;
        return ch == '#' ? 1f : nonHashMultiplier;
    }

    /**
     * Constructor.
     */
    public CommentDetector()
    {
        this(null, null);
    }

    /**
     * Constructor.
     *
     * @param delimiter the optional delimiter
     * @param quoteChar the optional quote character
     */
    public CommentDetector(Character delimiter, Character quoteChar)
    {
        myCharactersToIgnore.add('"');
        myCharactersToIgnore.add('\'');
        myCharactersToIgnore.add(',');
        if (delimiter != null)
        {
            myCharactersToIgnore.add(delimiter.charValue());
        }
        if (quoteChar != null)
        {
            myCharactersToIgnore.add(quoteChar.charValue());
        }
    }

    @Override
    public ValuesWithConfidence<Character> detect(LineSampler sampler)
    {
        ValueWithConfidence<Character> result = new ValueWithConfidence<>();

        TCharIntHashMap firstChars = new TCharIntHashMap();
        for (String line : sampler.getBeginningSampleLines())
        {
            if (!line.isEmpty())
            {
                char firstChar = line.charAt(0);
                if (isComment(firstChar))
                {
                    if (!firstChars.containsKey(firstChar))
                    {
                        firstChars.put(firstChar, 0);
                    }

                    firstChars.put(firstChar, firstChars.get(firstChar) + 1);
                }
            }
        }

        for (char ch : firstChars.keys())
        {
            float confidence = getConfidenceMultiplier(ch);
            float firstCharLinePercentage = (sampler.getBeginningSampleLines().size() - firstChars.get(ch))
                    / (float)sampler.getBeginningSampleLines().size();

            if (confidence > result.getConfidence() && firstCharLinePercentage > ourCommentThreshold)
            {
                result.setValue(Character.valueOf(ch));
                result.setConfidence(confidence);
            }
        }

        return new ValuesWithConfidence<Character>(result);
    }

    /**
     * Determines if the given character is a potential comment character.
     *
     * @param ch the character
     * @return Whether it is a potential comment character
     */
    private boolean isComment(char ch)
    {
        return !(Character.isLetterOrDigit(ch) || Character.isWhitespace(ch) || myCharactersToIgnore.contains(ch));
    }
}
