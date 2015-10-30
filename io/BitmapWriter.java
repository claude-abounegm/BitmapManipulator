package io;
import java.io.*;

/**
 * A BitmapReader supplies functions allowing to be able to write data to a Bitmap file.
 * 
 * @author Claude Abounegm
 *
 */
public class BitmapWriter extends FileOutputStream {

	/**
	 * Creates a BitmapWriter by opening a connection to an actual file, the file 
	 * named by the File object file in the file system.
	 * 
	 * @param file - the file to be opened for writing.
	 * @throws FileNotFoundException if the file does not exist, is a directory rather 
	 * than a regular file, or for some other reason cannot be opened for reading.
	 */
	public BitmapWriter(File file) throws FileNotFoundException {
		super(file);
	}
	
	/**
	 * Writes -in little endian- a 2-byte integer to the current stream 
	 * and advances the current position of the stream by two bytes.
	 * 
	 * @throws IOException if an I/O error occurs
	 */
	public void writeInt16(short value) throws IOException {
		this.write(value & 0xFF);
		this.write((value >>> 8) & 0xFF);
	}
	
	/**
	 * Writes -in little endian- a 4-byte integer to the current stream 
	 * and advances the current position of the stream by four bytes.
	 * 
	 * @throws IOException if an I/O error occurs
	 */
	public void writeInt32(int value) throws IOException {
		this.write(value & 0xFF);
		this.write((value >>> 8) & 0xFF);
		this.write((value >>> 16) & 0xFF);
		this.write((value >>> 24) & 0xFF);
	}
}
