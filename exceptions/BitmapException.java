package exceptions;

import java.io.File;

public class BitmapException extends Exception {
	private static final long serialVersionUID = 1858877817067661707L;

	private File _file;

	public BitmapException(File file) {
		_file = file;
	}
	
	public File getUnderlyingFile() {
		return _file;
	}
}
