package exceptions;

import java.io.*;

public class BitmapNotFoundException extends BitmapException {

	private static final long serialVersionUID = -6684495759689193297L;
	
	public BitmapNotFoundException(File file) {
		super(file);
	}
}
