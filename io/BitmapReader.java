package io;
import java.io.*;

/**
 * A BitmapReader supplies functions allowing to be able to read data from a Bitmap file.
 * 
 * @author Claude Abounegm
 *
 */
public class BitmapReader extends FileInputStream {
	// the underlying file
	private File _file;
	
	/**
	 * Creates a BitmapReader by opening a connection to an actual file, the file 
	 * named by the File object file in the file system.
	 * 
	 * @param file - the file to be opened for reading.
	 * @throws FileNotFoundException if the file does not exist, is a directory rather 
	 * than a regular file, or for some other reason cannot be opened for reading.
	 */
	public BitmapReader(File file) throws FileNotFoundException {
		super(file);
		_file = file;
	}

	/**
	 * Reads a 2-byte integer which is stored in little endian from the 
	 * current stream and advances the current position of the stream by two bytes.
	 * 
	 * @return A 2-byte signed integer read from the current stream.
	 * @throws IOException if an I/O error occurs
	 */
	public short readInt16() throws IOException {
		return (short) (this.read() | this.read() << 8);
	}

	/**
	 * Reads a 4-byte signed integer which is stored in little endian from the 
	 * current stream and advances the current position of the stream by four bytes.
	 * 
	 * @return A 4-byte signed integer read from the current stream.
	 * @throws IOException if an I/O error occurs
	 */
	public int readInt32() throws IOException {
		return (this.read() | this.read() << 8 | this.read() << 16 | this.read() << 24);
	}
	
	/**
	 * Gets the file which the stream is reading from.
	 * @return The file which the stream is reading from.
	 */
	public File getUnderlyingFile() {
		return _file;
	}
}
