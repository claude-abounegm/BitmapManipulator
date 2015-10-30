import java.io.*;
import java.util.Scanner;

import exceptions.*;
import graphics.*;

/**
 * A class which contains the main() entry point for the application.
 * It manipulates all the data entry, and bitmap input and output.
 * 
 * @author Claude Abounegm
 *
 */
public class Manipulator {

	public static void main(String[] args) {
		// The scanner to read input from console.
		Scanner scanner = null;

		try {
			// Create a new input scanner.
			scanner = new Scanner(System.in);
			System.out.print("What image file would you like to edit: ");

			// Read the input name and create a new Bitmap object with the
			// bitmap's data to be able to perform operations on it later.
			Bitmap bitmap = new Bitmap(new File(scanner.next()));

			// Get thread input.
			do {
				System.out.print("How many threads would you like to use: ");
			} while (!bitmap.setThreads(scanner.nextInt(10)));

			char command = ' ';
			do {
				System.out.print("What command would you like to perform (i, g, b, h, s, d, r, or q): ");
				command = scanner.next().toLowerCase().charAt(0); // get the first char inputed by the user.

				boolean valid = true;                 // assumes that the user inputed a correct command.
				double startTime = System.nanoTime(), // the start time of the command.
						endTime;                      // the end time of the command.

				switch (command) {
					case 'i': // Invert
						bitmap.invert();
						break;
					case 'g': // Gray-scale
						bitmap.grayscale();
						break;
					case 'b': // Blur
						bitmap.blur();
						break;
					case 'h': // Horizontal Mirror
						bitmap.horizontalMirror();
						break;
					case 's': // Shrink
						bitmap.shrink();
						break;
					case 'd': // Double the size
						bitmap.doubleSize();
						break;
					case 'r': // Rotate 90 degrees to the right
						bitmap.rotate90Degrees(); 
						break;
					default: // otherwise, invalid
						valid = false;
						break;
				}
				
				// Find the end time, so we can calculate how many seconds have elapsed.
				endTime = System.nanoTime();
				
				if (command != 'q') {
					if (valid)
						System.out.printf("Command took %.3f seconds to execute\n", (endTime - startTime) / 1000000000);
					else
						System.out.println("Command is not valid; please try again.");
				}
			} while (command != 'q'); // quit when command is q.
			
			System.out.print("What do you want to name your new image file: ");
			// Read the output name of the file, and write the new bitmap to it.
			bitmap.write(new File(scanner.next()));
			
		} catch (BitmapNotFoundException e) {
			System.out.printf("The file: \"%s\" was not found.\n", e.getUnderlyingFile().getAbsolutePath());
		} catch (NotABitmapException e) {
			System.out.printf("The file: \"%s\" is not a valid bitmap, or is not supported by this application.\n", 
					e.getUnderlyingFile().getAbsolutePath());
		} finally {
			if (scanner != null)
				scanner.close();
		}
	}
}
