package graphics;

import exceptions.*;

/**
 * 
 * @author Claude Abounegm
 *
 */
public class Pixel {	
	/**
	 * A class that is used to average out pixel values. This class can be
	 * reused by calling Averages.reset(). This class is NOT thread safe.
	 * 
	 * @author Claude Abounegm
	 */
	private static class PixelsAverage {
		private double blue;
		private double red;
		private double green;
		private int count;

		/**
		 * Creates an Averages object, and Averages.add() can now be used.
		 */
		public PixelsAverage() {
			this.reset();
		}

		/**
		 * Resets the average values to zero.
		 */
		public void reset() {
			blue = 0;
			red = 0;
			green = 0;
			count = 0;
		}

		/**
		 * Adds the current value as a weighted average. The final value is not
		 * calculated until Averages.setAverage() is called.
		 * 
		 * @param in
		 *            - The pixel to use the colors from for the average.
		 */
		public void add(Pixel in) {
			if (in != null && in.isValid()) {
				blue += in.getBlue();
				red += in.getRed();
				green += in.getGreen();
				++count;
			}
		}

		/**
		 * Calculates the average based on the values added to the average.
		 * 
		 * @param out
		 *            - The pixel to set the average colors to.
		 */
		public void setAverage(Pixel out) {
			// System.out.printf("%d - ", _count); // debugging
			out.setColorsTo((byte) (blue / count), (byte) (green / count),
					(byte) (red / count));
		}
	}
	
	/*
	 * Since we are dealing with raw colors, and each pixel is made of
	 * three colors, we need to know the offset of the BLUE, GREEN, and RED
	 * colors when retrieving them. So if we're at location 0 of the array,
	 * and BLUE = 0, then array[0] is the blue color of the first pixel;
	 * and GREEN = 1, then array[0 + 1] = array[1], is the green of the first pixel.
	 * So, position 3 is the start of the second pixel (since 0, 1, 2 are the first pixel),
	 * and array[3] is BLUE, array[3 + 1] is GREEN, array[3 + 2] is RED. And so on.
	 */
	private static final int BLUE = 0;
	private static final int GREEN = 1;
	private static final int RED = 2;
	
	private Bitmap parent;          // the bitmap which holds the data of the pixel
	private PixelsAverage averages; // a class which calculates the averages of many pixels
	private int offsetY;            // the y position at which the pixel starts
	private int offsetX;            // the x position at which the pixel starts
	
	/**
	 * Initializes a new object which points at (x, y).
	 * @param parent - The Bitmap in which this Pixel is enclosed.
	 * @param x - The x-coordinate of the Pixel.
	 * @param y - The y-coordinate of the Pixel.
	 * @throws NullArgumentException if parent is null.
	 */
	Pixel(Bitmap parent, int x, int y) {
		if (parent == null)
			throw new NullArgumentException("parent");

		this.parent = parent;

		// We want to be able to create a "dirty" pixel. So, for any x or y that is
		// less than zero, the pixel is invalidated. Otherwise, the pixel is
		// moved to the desired coordinates, if valid, after going checking that
		// they are in-bound.
		if (x < 0 || y < 0)
			this.invalidate();
		else
			this.moveTo(x, y);
	}
	/**
	 * Initializes a new object which does not point to a valid location.
	 * @param parent - The parent in which the Pixel should be enclosed.
	 * @throws NullArgumentException if parent is null.
	 */
	Pixel(Bitmap parent) {
		this(parent, -1, -1);
	}
	
	/**
	 * Invalidates the pixel, so it wouldn't point at any location. This is
	 * particularly useful when wanting to reuse the pixel, but do not want to
	 * be able to write anything to it until needed.
	 */
	public void invalidate() {
		offsetY = -1;
		offsetX = -1;
	}
	/**
	 * Checks whether this pixel is pointing to a valid location, and operations
	 * can be done on it. The pixel should always be valid unless invalidate()
	 * was called, or the Pixel has not been moved to point anywhere after
	 * initialized.
	 * 
	 * @return true if pixel is valid; otherwise, false.
	 */
	public boolean isValid() {
		// brackets don't matter but they are added for easier reading.
		return (offsetY >= 0 && offsetY < parent.getHeight())
				&& (offsetX >= 0 && offsetX < parent.getWidthBytes());
	}
	
	/**
	 * Moves the position of the Pixel to (x, y). This allows to perform
	 * different operations on the Pixel without having to create a new Pixel
	 * object every time. If you would rather create a new Pixel every time, use
	 * Bitmap.newPixel().
	 * 
	 * @param x - The x-coordinate of the Pixel.
	 * @param y - The y-coordinate of the Pixel.
	 * @return This same Pixel, used for chaining.
	 */
	public Pixel moveTo(int x, int y) {
		if (x < 0 || x >= parent.getWidth())
			throw new ArrayIndexOutOfBoundsException(x);
		if (y < 0 || y >= parent.getHeight())
			throw new ArrayIndexOutOfBoundsException(y);

		// set the offsets. remember, we are dealing with raw data, thus
		// offsetX is the actual location of the first color, and not the pixel.
		this.offsetY = y;
		this.offsetX = x * 3;

		return this;
	}
	
	/**
	 * Gets the blue component of this pixel.
	 * 
	 * @return The blue color component of the pixel, the returned result is
	 *         between 0-255, inclusive.
	 */
	public int getBlue() {
		return parent.getRawColors()[offsetY][offsetX + BLUE] & 0xFF;
	}
	/**
	 * Sets the blue component of this pixel.
	 * 
	 * @param blue
	 *            - the value of the color.
	 */
	public void setBlue(byte blue) {
		parent.getRawColors()[offsetY][offsetX + BLUE] = blue;
	}	
	/**
	 * Sets the blue component of this pixel.
	 * 
	 * @param blue
	 *            - the value of the color. This integer is truncated, and only
	 *            the least significant byte is kept. Any value not in the range
	 *            of [0, 255] will not be handled correctly by this method.
	 */
	public void setBlue(int blue) {
		this.setBlue((byte) blue);
	}
	
	/**
	 * Gets the green component of this pixel.
	 * 
	 * @return The green color component of the pixel, the returned result is
	 *         between 0-255, inclusive.
	 */
	public int getGreen() {
		return parent.getRawColors()[offsetY][offsetX + GREEN] & 0xFF;
	}
	/**
	 * Sets the green component of this pixel.
	 * 
	 * @param green
	 *            - the value of the color.
	 */
	public void setGreen(byte green) {
		parent.getRawColors()[offsetY][offsetX + GREEN] = green;
	}
	/**
	 * Sets the green component of this pixel.
	 * 
	 * @param green
	 *            - the value of the color. This integer is truncated, and only
	 *            the least significant byte is kept. Any value not in the range
	 *            of [0, 255] will not be handled correctly by this method.
	 */
	public void setGreen(int green) {
		this.setGreen((byte) green);
	}
		
	/**
	 * Gets the red component of this pixel.
	 * 
	 * @return The red color component of the pixel, the returned result is
	 *         between 0-255, inclusive.
	 */
	public int getRed() {
		return parent.getRawColors()[offsetY][offsetX + RED] & 0xFF;
	}
	/**
	 * Sets the red component of this pixel.
	 * 
	 * @param red
	 *            - the value of the color.
	 */
	public void setRed(byte red) {
		parent.getRawColors()[offsetY][offsetX + RED] = red;
	}
	/**
	 * Sets the red component of this pixel.
	 * 
	 * @param red
	 *            - the value of the color. This integer is truncated, and only
	 *            the least significant byte is kept. Any value not in the range
	 *            of [0, 255] will not be handled correctly by this method.
	 */
	public void setRed(int red) {
		this.setRed((byte) red);
	}
	
	/**
	 * Sets the blue, green and red components of this pixel. This is provided
	 * as a syntactic sugar, and acts exactly like the set{Color}() methods.
	 * 
	 * @param blue
	 *            - the value of the blue component.
	 * @param green
	 *            - the value of the green component.
	 * @param red
	 *            - the value of the red component.
	 */
	public void setColorsTo(byte blue, byte green, byte red) {
		this.setBlue(blue);
		this.setGreen(green);
		this.setRed(red);
	}
	/**
	 * Sets the blue, green and red components of this pixel. This is provided
	 * as a syntactic sugar, and acts exactly like the set{Color}() methods.
	 * 
	 * @param blue
	 *            - the value of the blue component.
	 * @param green
	 *            - the value of the green component.
	 * @param red
	 *            - the value of the red component.
	 */
	public void setColorsTo(int blue, int green, int red) {
		this.setColorsTo((byte) blue, (byte) green, (byte) red);
	}
	
	/**
	 * Sets all the colors (blue, green, red) to the same color.
	 * 
	 * @param value - The value to which the three colors should be set to.
	 */
	public void setAllColorsTo(byte value) {
		this.setBlue(value);
		this.setGreen(value);
		this.setRed(value);
	}
	/**
	 * Sets all the colors (blue, green, red) to the same color.
	 * 
	 * @param value - The value to which the three colors should be set to.
	 */
	public void setAllColorsTo(int value) {
		this.setAllColorsTo((byte) value);
	}
	
	/**
	 * Sets the colors of this pixel, to be same colors as the other pixel.
	 * @param p - The pixel to copy the colors from.
	 */
	public void setColorsFrom(Pixel p) {
		if (p == null)
			throw new NullArgumentException("p");

		this.setColorsTo(p.getBlue(), p.getGreen(), p.getRed());
	}
	
	/**
	 * Starts accepting values through avgAdd(), those values will be averaged out
	 * when avgStop() is called. 
	 */
	public void avgStart() {
		if (averages == null)
			averages = new PixelsAverage();
		else
			averages.reset();
	}
	/**
	 * Adds a Pixel to the cumulative values, so all the colors can be averaged out later.
	 * 
	 * @param p
	 */
	public void avgAdd(Pixel p) {
		if (averages != null) {
			averages.add(p);
		}
	}
	/**
	 * Stops averaging out values, and sets the value of this Pixel to average 
	 * value of all the Pixels added.
	 */
	public void avgStop() {
		if (averages != null) {
			averages.setAverage(this);
			averages.reset();
		}
	}
	
	/**
	 * Gets the Bitmap in which this Pixel is contained.
	 * @return The Bitmap in which this Pixel is contained.
	 */
	public Bitmap getParent() {
		return parent;
	}
	
	/**
	 * Swaps the Pixels' color data. So, x has y's data, and y has x's data.
	 * 
	 * @param x
	 *            - The first Pixel to which the data will be swapped.
	 * @param y
	 *            - The second Pixel to which the data will be swapped.
	 */
	public static void swap(Pixel x, Pixel y) {
		// swap blue component
		int temp = y.getBlue();
		y.setBlue(x.getBlue());
		x.setBlue(temp);

		// swap green component
		temp = y.getGreen();
		y.setGreen(x.getGreen());
		x.setGreen(temp);

		// swap red component
		temp = y.getRed();
		y.setRed(x.getRed());
		x.setRed(temp);
	}
}