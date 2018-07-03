package io.opensphere.mantle.infinity;

/*
 * QueryBinParameters class.
 * 
 * Contains the variables that will be used for infinity county-by
 * numeric and data binning
 * 
 */
public class QueryBinParameters 
{
	/** The numeric bin width */
	Double myBinWidth;

	/** The numeric bin offset */
	Double myBinOffset;

	/** The user number format */
	String myUserNumberFormat;

	/** The date format */
	String myDateFormat;

	/** The date interval */
	String myDateInterval;

	/** The dayOfWeek.  True if dayOfWeek, false if hourOfDay */
	Boolean myDayOfWeek;
	
	/**
	 * Constructor.
	 */
	public QueryBinParameters() 
	{
		// Intentionally left blank
	}

	/**
	 * Get the bin width.
	 * 
	 * @return the binWidth
	 */
	public Double getBinWidth() {
		return myBinWidth;
	}

	/**
	 * Set the bin width
	 * 
	 * @param BinWidth the binWidth to set
	 */
	public void setBinWidth(Double binWidth) {
		myBinWidth = binWidth;
	}

	/**
	 * Get the bin offset.
	 * 
	 * @return the BinOffset
	 */
	public Double getBinOffset() {
		return myBinOffset;
	}

	/**
	 * Set the bin offset.
	 * 
	 * @param binOffset the binOffset to set
	 */
	public void setBinOffset(Double binOffset) {
		myBinOffset = binOffset;
	}

	/**
	 * Get the user number format.
	 * 
	 * @return the userNumberFormat
	 */
	public String getUserNumberFormat() {
		return myUserNumberFormat;
	}

	/**
	 * Set the user number format.
	 * 
	 * @param userNumberFormat the userNumberFormat to set
	 */
	public void setUserNumberFormat(String userNumberFormat) {
		myUserNumberFormat = userNumberFormat;
	}

	/**
	 * Get the date format.
	 * 
	 * @return the dateFormat
	 */
	public String getDateFormat() {
		return myDateFormat;
	}

	/**
	 * Set the date format.
	 * 
	 * @param dateFormat the dateFormat to set
	 */
	public void setDateFormat(String dateFormat) {
		myDateFormat = dateFormat;
	}

	/**
	 * Get the date interval.
	 * 
	 * @return the dateInterval
	 */
	public String getDateInterval() {
		return myDateInterval;
	}

	/**
	 * Set the date interval.
	 * 
	 * @param dateInterval the dateInterval to set
	 */
	public void setDateInterval(String dateInterval) {
		myDateInterval = dateInterval;
	}

	/**
	 * Get the dayOfWeek.
	 * 
	 * @return the dayOfWeek
	 */
	public Boolean getDayOfWeek() {
		return myDayOfWeek;
	}

	/**
	 * Set the dayOfWeek
	 * 
	 * @param dayOfWeek the dayOfWeek to set
	 */
	public void setDayOfWeek(Boolean dayOfWeek) {
		myDayOfWeek = dayOfWeek;
	}

}
