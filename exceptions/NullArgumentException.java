package exceptions;

public class NullArgumentException extends IllegalArgumentException {

	private static final long serialVersionUID = -1800076529184097393L;

	public NullArgumentException(String argName) {
		super(String.format("Value cannot be null. Parameter name: %s.", argName));
	}
}
