package graphics;

final class Helpers {
	/**
	 * Restricts the {@link value} to a given range.
	 * @param value - The value that needs to be restricted
	 * @param min - The lower bound value
	 * @param max - The upper bound value
	 * @return {@link min} if {@link value} is less than {@link min}; 
	 * 		   {@link max} if {@link value} is larger than {@link max};
	 * 		   otherwise, {@link value}.
	 * 
	 * @see https://en.wikipedia.org/wiki/Clamping_(graphics)
	 */
	public static int clamp(int value, int min, int max) {
		if(value <= min)
			return min;
		if(value >= max)
			return max;
		
		return value;
		// return (value < min) ? min : ((value > max) ? max : value);
	}
}
