package io.opensphere.arcgis2.model;

import org.codehaus.jackson.annotate.JsonSetter;

/**
 * Contains information about an ArcGIS map layer.
 */
public class DocumentInfo
{
    /**
     * The anti aliasing mode.
     */
    private String myAntialiasingMode;

    /**
     * The author of the layer.
     */
    private String myAuthor;

    /**
     * The category this layer is in.
     */
    private String myCategory;

    /**
     * Comments about the layer.
     */
    private String myComments;

    /**
     * Some keywords describing this layer.
     */
    private String myKeywords;

    /**
     * What this layer is about.
     */
    private String mySubject;

    /**
     * The text anti aliasing mode.
     */
    private String myTextAntialiasingMode;

    /**
     * The name of the layer.
     */
    private String myTitle;

    /**
     * The anti aliasing mode.
     *
     * @return the antialiasingMode
     */
    public String getAntialiasingMode()
    {
        return myAntialiasingMode;
    }

    /**
     * The author of the layer.
     *
     * @return the author
     */
    public String getAuthor()
    {
        return myAuthor;
    }

    /**
     * The category this layer is in.
     *
     * @return the category
     */
    public String getCategory()
    {
        return myCategory;
    }

    /**
     * Comments about the layer.
     *
     * @return the comments
     */
    public String getComments()
    {
        return myComments;
    }

    /**
     * Some keywords describing this layer.
     *
     * @return the keywords
     */
    public String getKeywords()
    {
        return myKeywords;
    }

    /**
     * What this layer is about.
     *
     * @return the subject
     */
    public String getSubject()
    {
        return mySubject;
    }

    /**
     * The text anti aliasing mode.
     *
     * @return the textAntialiasingMode
     */
    public String getTextAntialiasingMode()
    {
        return myTextAntialiasingMode;
    }

    /**
     * The name of the layer.
     *
     * @return the title
     */
    public String getTitle()
    {
        return myTitle;
    }

    /**
     * The anti aliasing mode.
     *
     * @param antialiasingMode the antialiasingMode to set
     */
    @JsonSetter("AntialiasingMode")
    public void setAntialiasingMode(String antialiasingMode)
    {
        myAntialiasingMode = antialiasingMode;
    }

    /**
     * The author of the layer.
     *
     * @param author the author to set
     */
    @JsonSetter("Author")
    public void setAuthor(String author)
    {
        myAuthor = author;
    }

    /**
     * The category this layer is in.
     *
     * @param category the category to set
     */
    @JsonSetter("Category")
    public void setCategory(String category)
    {
        myCategory = category;
    }

    /**
     * Comments about the layer.
     *
     * @param comments the comments to set
     */
    @JsonSetter("Comments")
    public void setComments(String comments)
    {
        myComments = comments;
    }

    /**
     * Some keywords describing this layer.
     *
     * @param keywords the keywords to set
     */
    @JsonSetter("Keywords")
    public void setKeywords(String keywords)
    {
        myKeywords = keywords;
    }

    /**
     * What this layer is about.
     *
     * @param subject the subject to set
     */
    @JsonSetter("Subject")
    public void setSubject(String subject)
    {
        mySubject = subject;
    }

    /**
     * The text anti aliasing mode.
     *
     * @param textAntialiasingMode the textAntialiasingMode to set
     */
    @JsonSetter("TextAntialiasingMode")
    public void setTextAntialiasingMode(String textAntialiasingMode)
    {
        myTextAntialiasingMode = textAntialiasingMode;
    }

    /**
     * The name of the layer.
     *
     * @param title the title to set
     */
    @JsonSetter("Title")
    public void setTitle(String title)
    {
        myTitle = title;
    }
}
