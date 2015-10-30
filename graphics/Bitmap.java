package graphics;

import java.io.*;
import java.util.concurrent.*;

import io.*;
import exceptions.*;

/**
 * A class which handles Bitmap files and data, and provide methods to perform
 * operations on the picture.
 * 
 * @author Claude Abounegm
 *
 */
public class Bitmap {
	// ugly ParallelRunner.. thank you Java, sorry Dr. Wittman.
	private abstract class ParallelRunner {
		void execute(int start, int end) {
		}

		public final void startAndWait(int splitValue) {
			// distribute the work evenly
			int step = splitValue / nThreads;
			if (splitValue % nThreads != 0)
				++step;
			
			ExecutorService exec = Executors.newFixedThreadPool(nThreads);
			
			// for each thread, do work split up
			for (int i = 0; i < nThreads; ++i) {
				// variables need to be final to be used in anonymous functions.
				final int iFinal = i, 
					      stepFinal = step;
				
				exec.execute(new Runnable() {
					public void run() {
						execute(iFinal * stepFinal, Math.min((iFinal + 1) * stepFinal, splitValue));
					}
				});
			}
			
			exec.shutdown();
			
			try {
				exec.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			} catch (InterruptedException e) {
				
			}
		}
	}
	
	/**
	 * A value that represents the radius (in pixels) at which blur should look ahead to.
	 * For this assignment, the radius is required to be two pixels.
	 */
	public static final int BLUR_RADIUS = 2;
	
	private int nThreads;
	private BitmapHeader header;
	private byte[][] colors;

	/**
	 * Initializes a new Bitmap object. 
	 * @param file - The bitmap to be opened for reading.
	 * @throws BitmapNotFoundException  if the bitmap was not found.
	 * @throws NotABitmapException if the file specified is not a bitmap.
	 * @throws NullArgumentException  if the file specified is null.
	 */
	public Bitmap(File file) throws BitmapNotFoundException, NotABitmapException {
		// read in the file
		this.read(file);
		// assume having one thread
		this.nThreads = 1;
	}
	/**
	 * Initializes a new Bitmap object. It's used as a building block, where you
	 * can create a new image from scratch.
	 * 
	 * @param width - The width of the image.
	 * @param height - The height of the image.
	 */
	public Bitmap(int width, int height) {
		// initialize the header
		this.header = new BitmapHeader(width, height);
		
		// initialize the colors array
		this.colors = new byte[header.getHeight()][header.getWidthBytes()];
	}
	
	/**
	 * Reads the bitmap in.
	 * 
	 * @param file
	 *            - The file which points to the bitmap.
	 * @throws NotABitmapException
	 *             if the file is not a valid bitmap. This includes having a
	 *             valid bitmap which is not 24-bit.
	 * @throws BitmapNotFoundException if the file was not found.
	 */
	private void read(File file) throws NotABitmapException,
			BitmapNotFoundException {

		BitmapReader reader = null;
		try {
			reader = new BitmapReader(file);
			
			// reads the bitmap header
			this.header = new BitmapHeader(reader);
			
			// read in the color data
			this.colors = new byte[header.getHeight()][header.getWidthBytes()];
			for (int i = 0; i < header.getHeight(); ++i) {
				reader.read(colors[i], 0, colors[i].length);
				reader.skip(header.getPadding());
			}
		} catch (FileNotFoundException e) {
			throw new BitmapNotFoundException(file);
		} catch (IOException e) {
			throw new NotABitmapException(file);
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * Writes the Bitmap to the specified file. The method creates a 
	 * new file if it doesn't already exist, otherwise it overwrites it.
	 * 
	 * @param file - The file to which the method is writing the bitmap's data to.
	 */
	public void write(File file) {
		BitmapWriter writer = null;
		
		try {
			writer = new BitmapWriter(file);
			
			// write the 54 bytes of the header to the stream.
			header.write(writer);

			// This is an array of bytes filled with zeros. It is used
			// to pad the end of a row after colors have been written.
			// This is due to the bitmap requiring each row of bytes to
			// be divisible by four.
			byte[] paddingBytes = new byte[header.getPadding()];

			// write the colors' data to the stream
			for (int i = 0; i < header.getHeight(); ++i) {
				writer.write(colors[i]);
				writer.write(paddingBytes);
			}
			
			// write the two extra bytes at the end of the bitmap to make its
			// size even.
			writer.write(new byte[] { 0, 0 });
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (writer != null)
					writer.close();
			} catch (IOException e) {
			}
		}
	}
	
	/**
	 * Creates a new Pixel which points to (x, y) in the bitmap. This pixel provides
	 * methods to perform operation on them. Each Pixel can be re-used as many
	 * times as needed.
	 * 
	 * @param x - The x-coordinate of the pixel.
	 * @param y - The y-coordinate of the pixel.
	 * @return A new Pixel which points to the specified location (x, y).
	 */
	public Pixel newPixel(int x, int y) {
		return new Pixel(this, x, y);
	}
	/**
	 * Creates a new Pixel which points to an invalid location and cannot be 
	 * used before being moved to a valid location, using Pixel.moveTo(x, y).
	 * @return A new Pixel that can be re-used.
	 */
	public Pixel newEmptyPixel() {
		return new Pixel(this);
	}
	
	/**
	 * Gets the 2D array that contains the raw colors.
	 * 
	 * @return The 2D array containing the raw colors of the Bitmap.
	 */
	public byte[][] getRawColors() {
		return colors;
	}
	
	/**
	 * Sets the number of threads used for image manipulation operations.
	 * @param n - Number of threads, bigger than or equal to one.
	 * @return true if the value was accepted; otherwise, false.
	 */
	public boolean setThreads(int n) {
		if (n >= 1) {
			this.nThreads = n;
			return true;
		}

		return false;
	}
	
	/**
	 * Gets the current width (in pixels) of the Bitmap.
	 * @return The width of the Bitmap, in pixels.
	 */
	public int getWidth() {
		return header.getWidth();
	}
	/**
	 * Gets the current width (in bytes) of the Bitmap.
	 * 
	 * @return The width of the Bitmap, in bytes.
	 */
	int getWidthBytes() {
		return header.getWidthBytes();
	}
	/**
	 * Gets the current height (in pixels) of the Bitmap.
	 * @return The height of the Bitmap, in pixels.
	 */
	public int getHeight() {
		return header.getHeight();
	}
	
	/**
	 * Makes this bitmap an exact copy of the source bitmap `srcBmp`.
	 * 
	 * @param srcBmp - The bitmap to copy the data from.
	 */
	public void copyFrom(Bitmap srcBmp) {
		if(srcBmp == null) 
			throw new NullArgumentException("bitmap");
		
		// no need to re-initialize header and array if they are the same size of the source bitmap.
		if (srcBmp.getWidth() != this.getWidth() || srcBmp.getHeight() != this.getHeight()) {
			// the content of this bitmap is overwritten with the other bitmap's content.
			this.header = new BitmapHeader(srcBmp.getWidth(), srcBmp.getHeight());
			this.colors = new byte[header.getHeight()][header.getWidthBytes()];
		}
		
		// copy the colors from source to destination.
		byte[][] srcColors = srcBmp.getRawColors();
		for (int i = 0; i < srcColors.length; ++i) {
		    System.arraycopy(srcColors[i], 0, colors[i], 0, srcColors[0].length);
		}
	}
	
	/**
	 * Inverts the picture's colors.
	 */
	public void invert() {
		new ParallelRunner() {
			@Override
			void execute(int x_start, int x_end) {
				// create a dummy pixel to move around the bitmap.
				Pixel p = newEmptyPixel();

				// go through each pixel and perform operations on them.
				for (int x = x_start; x < x_end; ++x) {
					for (int y = 0; y < getHeight(); ++y) {
						p.moveTo(x, y);
						p.setColorsTo(255 - p.getBlue(), 255 - p.getGreen(), 255 - p.getRed());
					}
				}
			}
		}.startAndWait(this.getWidth());
	}
	
	/**
	 * Changes the colors of the picture to gray-scale.
	 */
	public void grayscale() {
		new ParallelRunner() {
			@Override
			void execute(int x_start, int x_end) {
				// create a dummy pixel to move around the bitmap.
				Pixel p = newEmptyPixel();

				// go through each pixel and perform operations on them.
				for (int x = x_start; x < x_end; ++x) {
					for (int y = 0; y < getHeight(); ++y) {
						p.moveTo(x, y);
						p.setAllColorsTo((int) (0.30 * p.getRed() + 0.59 * p.getGreen() + 0.11 * p.getBlue()));
					}
				}
			}
		}.startAndWait(this.getWidth());
	}
	
	/**
	 * Horizontally mirrors the picture.
	 */
	public void horizontalMirror() {
		new ParallelRunner() {
			@Override
			void execute(int y_start, int y_end) {
				// we need two pixels, so we can swap them around.
				Pixel p1 = newEmptyPixel(),
					  p2 = newEmptyPixel();

				for (int y = y_start; y < y_end; ++y) {
					// we go up to half the width since we're swapping
					// the colors from both sides.
					for (int x = 0; x < getWidth() / 2; ++x) {
						p1.moveTo(x, y);
						p2.moveTo(getWidth() - x - 1, y);

						// swap the two pixels
						Pixel.swap(p1, p2);
					}
				}
			}
		}.startAndWait(this.getHeight());
	}
	
	/**
	 * Rotates the picture 90 degrees to the right.
	 */
	public void rotate90Degrees() {
		// create a new bitmap to keep the rotated image in
		Bitmap newBitmap = new Bitmap(this.getHeight(), this.getWidth());
		
		new ParallelRunner() {
			@Override
			void execute(int y_start, int y_end) {
				// we need two dummy pixels: one to hold the data of the source pixel,
				// one to hold the destination pixel
				Pixel destPixel = newBitmap.newEmptyPixel(),
					   srcPixel = newEmptyPixel();

				// move (x, y) to (y, x); this rotates the picture by 90 degrees.
				for (int x = 0; x < getWidth(); ++x) {
					for (int y = y_start; y < y_end; ++y) {
						srcPixel.moveTo(x, y);
						destPixel.moveTo(y, x);
						
						destPixel.setColorsFrom(srcPixel);
					}
				}
			}
		}.startAndWait(this.getHeight());
		
		// copy the data from newBitmap to this bitmap.
		this.copyFrom(newBitmap);
	}
	
	/**
	 * Blurs the pictures with a radius of Bitmap.BLUR_RADIUS.
	 */
	public void blur() {
		// we create a new list as we need to keep track of the original pixels and their
		// surroundings to average them correctly.
		Bitmap newBitmap = new Bitmap(this.getWidth(), this.getHeight());
		
		new ParallelRunner() {
			@Override
			void execute(int x_start, int x_end) {
				Pixel destPixel = newBitmap.newEmptyPixel(), 
					   srcPixel = newEmptyPixel();

				// those values are used for clamping, since we want to go up
				// the value, so the max value is array.length - 1.
				int  widthMinus1 = x_end - 1, 
					heightMinus1 = getHeight() - 1;

				// define the variables to use them in the loop
				int startX, endX, startY, endY;

				for (int x1 = x_start; x1 <= widthMinus1; ++x1) {
					for (int y1 = 0; y1 <= heightMinus1; ++y1) {
						// calculate the starting and ending points to average
						// out the pixels around in the specified radius. The 
						// `minus one` is because the array is in the range 
						// [0, width[ so clamp works well.
						startX = Helpers.clamp(x1 - BLUR_RADIUS, 0, widthMinus1);
						startY = Helpers.clamp(y1 - BLUR_RADIUS, 0, heightMinus1);

						endX = Helpers.clamp(x1 + BLUR_RADIUS, 0, widthMinus1);
						endY = Helpers.clamp(y1 + BLUR_RADIUS, 0, heightMinus1);

						// change the location to (x1, y1)
						destPixel.moveTo(x1, y1);

						// avgStart() allows Pixel to start accepting values
						// from avgAdd().
						destPixel.avgStart();

						// Average out the values around the radius BLUR_RADIUS
						for (int x2 = startX; x2 <= endX; ++x2)
							for (int y2 = startY; y2 <= endY; ++y2)
								destPixel.avgAdd(srcPixel.moveTo(x2, y2));

						// calculate the average and store it at (x1, y1)
						destPixel.avgStop();
					}
				}
			}
		}.startAndWait(this.getWidth());
		
		// copy the data from newBitmap to this bitmap.
		this.copyFrom(newBitmap);
	}
	
	/**
	 * Shrinks the picture by two.
	 */
	public void shrink() {
		// if height or width is odd, make it even
		int height = this.getHeight() - (this.getHeight() % 2 == 0 ? 0 : 1);
		int width = this.getWidth() - (this.getWidth() % 2 == 0 ? 0 : 1);

		// create a list with half the width and half the height.
		Bitmap newBitmap = new Bitmap(width / 2, height / 2);
		
		new ParallelRunner() {
			@Override
			void execute(int x_start, int x_end) {
				Pixel destPixel = newBitmap.newEmptyPixel(),
					   srcPixel = newEmptyPixel();

				// variables to hold the equivalent location of the
				// pixels in the original list relative to the new list.
				//
				// |x|x|@|@|
				// |x|x|@|@| -> |x_avg|@_avg|
				// |~|~|o|o| -> |~_avg|o_avg|
				// |~|~|o|o|
				// 4x4 2x2
				int oldX = 0, oldY = 0;
				for (int x = x_start; x < x_end; ++x) {
					oldX = x * 2; // x's position is twice the original position

					for (int y = 0; y < newBitmap.getHeight(); ++y) {
						destPixel.moveTo(x, y); // set the pixel to (x, y)

						oldY = y * 2; // y's position is twice the original
										// position

						// start averaging out values
						destPixel.avgStart();

						// we need to average out 4 pixels every time:
						// (x, y); (x+1, y); (x, y+1); (x+1, y+1)
						destPixel.avgAdd(srcPixel.moveTo(oldX, oldY));
						destPixel.avgAdd(srcPixel.moveTo(oldX + 1, oldY));
						destPixel.avgAdd(srcPixel.moveTo(oldX, oldY + 1));
						destPixel.avgAdd(srcPixel.moveTo(oldX + 1, oldY + 1));

						// set the new pixel at (x, y) to the average
						destPixel.avgStop();
					}
				}
			}
		}.startAndWait(newBitmap.getWidth());
		
		this.copyFrom(newBitmap);
	}
	
	/**
	 * Doubles the size of the picture.
	 */
	public void doubleSize() {
		// create a new bitmap with twice the width and height
		Bitmap newBitmap = new Bitmap(this.getWidth() * 2, this.getHeight() * 2);
		
		new ParallelRunner() {
			@Override
			void execute(int x_start, int x_end) {
				Pixel destPixel = newBitmap.newEmptyPixel(),
				      srcPixel = newEmptyPixel();
					
				int newX = 0, newY = 0; // the new coordinates of the x and y positions for the new pixel
				for (int x = x_start; x < x_end; ++x) {
					// x's position is twice the original position
					newX = x * 2;

					for (int y = 0; y < getHeight(); ++y) {
						// y's position is twice the original position
						newY = y * 2;

						// set oldPixel's location to (x, y)
						srcPixel.moveTo(x, y);

						// we need to set 4 pixels with the same pixel.
						// this always works as the newList is double the size
						// of the original,
						// so the width is always even.
						destPixel.moveTo(newX, newY).setColorsFrom(srcPixel);
						destPixel.moveTo(newX + 1, newY).setColorsFrom(srcPixel);
						destPixel.moveTo(newX, newY + 1).setColorsFrom(srcPixel);
						destPixel.moveTo(newX + 1, newY + 1).setColorsFrom(srcPixel);
					}
				}
			}
		}.startAndWait(this.getWidth());
		
		this.copyFrom(newBitmap);
	}
}