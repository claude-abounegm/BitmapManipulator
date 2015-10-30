package exceptions;

import java.io.File;


public class NotABitmapException extends BitmapException {
	private static final long serialVersionUID = 897413355907507720L;

	public NotABitmapException(File file) {
		super(file);
	}
}
