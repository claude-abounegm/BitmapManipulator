package graphics;

import java.io.*;

import exceptions.*;
import io.*;

/**
 * A class which holds the header data of a Bitmap.
 * @author Claude Abounegm
 *
 */
class BitmapHeader {
	// Constant data, which is going to be written out to the new header.
	private static final byte[] TYPE = new byte[] { 'B', 'M' };
	private static final int RESERVED = 0;
	private static final int OFFSET = 54;
	private static final int HEADER_SIZE = 40;
	private static final short PLANES = 1;
	private static final short BITS = 24;
	private static final int COMPRESSION = 0;
	private static final int HORIZONTAL_RES = 72;
	private static final int VERTICAL_RES = 72;
	private static final int COLORS = 0;
	private static final int IMPORTANT_COLORS = 0;

	// Those fields will change as things are modified.
	private int size;
	private int widthPixels;
	private int widthBytes;
	private int padding;
	private int heightPixels;
	private int dataSize;
	
	/**
	 * Initializes a BitmapHeader by reading the bitmap and 
	 * advancing the reader to the first color offset.
	 * 
	 * @param reader - The reader to read the data from.
	 * @throws NotABitmapException If the file is not a bitmap.
	 * @throws IOException If an I/O error occurs.
	 * @throws NullArgumentException if reader is null.
	 */
	public BitmapHeader(BitmapReader reader) throws NotABitmapException,
			IOException {
		if(reader == null)
			throw new NullArgumentException("reader");
		
		if (reader.read() != TYPE[0] || reader.read() != TYPE[1])
			throw new NotABitmapException(reader.getUnderlyingFile());

		size = reader.readInt32();
		reader.skip(4); // reserved
		int skipBytesOffset = reader.readInt32() - 54;

		// header == 40
		if (reader.readInt32() != 40)
			throw new NotABitmapException(reader.getUnderlyingFile());

		// reads the height, and width, and set the size. 
		// This also calculates the padding required.
		this.setSize(reader.readInt32(), reader.readInt32());

		// planes == 1, bits == 24, compression == 0
		if (reader.readInt16() != 1 || reader.readInt16() != 24 || reader.readInt32() != 0)
			throw new NotABitmapException(reader.getUnderlyingFile());

		// The reset of the data is not important.
		reader.skip(20);

		// Skip any extra bytes after offset
		reader.skip(skipBytesOffset);
	}
	/**
	 * Initializes a BitmapHeader which has a specific width and height.
	 * @param width - The width of the bitmap.
	 * @param height - The height of the bitmap.
	 */
	public BitmapHeader(int width, int height) {
		if (width < 0 || height < 0)
			throw new IllegalArgumentException("width and/or height should be positive.");
		
		this.setSize(width, height);
	}
	
	/**
	 * Writes a header to the BitmapWriter's stream. 
	 * 
	 * @param writer - The writer which points to the beginning of the new Bitmap file.
	 * @throws IOException if an I/O error occurs.
	 * @throws NullArgumentException if `writer` or `list` are null.
	 */
	public void write(BitmapWriter writer) throws IOException {
		if(writer == null)
			throw new NullArgumentException("writer");
		
		writer.write(TYPE);
		writer.writeInt32(this.size);
		writer.writeInt32(RESERVED);
		writer.writeInt32(OFFSET);
		writer.writeInt32(HEADER_SIZE);
		writer.writeInt32(this.widthPixels);
		writer.writeInt32(this.heightPixels);
		writer.writeInt16(PLANES);
		writer.writeInt16(BITS);
		writer.writeInt32(COMPRESSION);
		writer.writeInt32(this.dataSize);
		writer.writeInt32(HORIZONTAL_RES);
		writer.writeInt32(VERTICAL_RES);
		writer.writeInt32(COLORS);
		writer.writeInt32(IMPORTANT_COLORS);
	}
	
	/**
	 * Sets the width and height of the bitmap; it also calculates: widthBytes,
	 * size, padding and dataSize.
	 * 
	 * @param width - The width of the bitmap, in pixels.
	 * @param height - The height of the bitmap, in pixels.
	 */
	private void setSize(int width, int height) {
		// width
		widthPixels = width;
		widthBytes = width * 3;
		
		// padding
		padding = 4 - (widthBytes % 4);
		if (padding == 4)
			padding = 0;
		
		// height
		heightPixels = height;
		
		// data size and size of the bitmap, in bytes
		dataSize = heightPixels * ((widthPixels * 3) + padding);
		size = 54 + dataSize + 2;
	}
	
	/**
	 * Gets the current width (in pixels) of the Bitmap.
	 * @return The width of the Bitmap, in pixels.
	 */
	public int getWidth() {
		return widthPixels;
	}
	
	/**
	 * Gets the current width (in bytes) of the Bitmap.
	 * @return The width of the Bitmap, in pixels.
	 */
	public int getWidthBytes() {
		return widthBytes;
	}
	
	/**
	 * Gets the current height (in pixels) of the Bitmap.
	 * @return The height of the Bitmap, in pixels.
	 */
	public int getHeight() {
		return heightPixels;
	}
	
	/**
	 * Gets the padding required to complete each row based on
	 * the Bitmap standard.
	 * 
	 * @return The number of bytes needed to pad a row of colors.
	 */
	public int getPadding() {
		return padding;
	}
}
