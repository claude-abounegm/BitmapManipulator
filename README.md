# BitmapManipulator

A simple bitmap manipulator. Only 24-bits BMPs are supported. The Bitmap class can either open a ".bmp" file, 
or create a new empty Bitmap with a desired size. Some operations such as blur, double, shrink, invert,
grayscale, horizontal mirror, and right rotate are already implemented.

This application was programmed for Algorithms and Data Structures course for Dr. Barry Wittman.

usage:
    
    import graphics.Bitmap;
    
    static void main() {
    	Bitmap bmp = new Bitmap("test.bmp");
    	bmp.invert();
    	bmp.write("inverted.bmp");
    }
