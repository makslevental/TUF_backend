package database_maks_pkg;

import java.io.*;

/** Various helper functions. Some shamelessly (and carelessly) stolen from the internet).
 *
 */
public class Util{
    /**
     * Read bytes from a File into a byte[].
     *
     * @param file The File to read.
     * @return A byte[] containing the contents of the File.
     * @throws IOException Thrown if the File is too long to read or couldn't be
     * read fully.
     */
    public static byte[] readBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            throw new IOException("Could not completely read file");
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

    /**
     * Writes the specified byte[] to the specified File path.
     *
     * @param theFile File Object representing the path to write to.
     * @param bytes The byte[] of data to write to the File.
     * @throws IOException Thrown if there is problem creating or writing the
     * File.
     */
    public static void writeBytesToFile(File theFile, byte[] bytes) throws Exception {
        BufferedOutputStream bos = null;

        FileOutputStream fos = new FileOutputStream(theFile);
        bos = new BufferedOutputStream(fos);
        bos.write(bytes);
        bos.flush();
        bos.close();
    }

    /** Transforms object array into double array
     *
     * Sqlite returns object arrays from a getObject call and because of Java boxing there's no other way to (for example in place)
     * cast to double.
     *
     * @param arr array objects returned by Sqlite
     * @return double array of Sqlite attributes
     */
    protected static double[] dblUnboxArray(Object[] arr) {
        double[] unboxedArr = new double[arr.length];
        for(int i=0;i<arr.length;++i) unboxedArr[i]=(Double)arr[i];
        return unboxedArr;
    }

    /** Transforms object array into int array
     *
     * Sqlite returns object arrays from a getObject call and because of Java boxing there's no other way to (for example in place)
     * cast to int.
     *
     * @param arr array objects returned by Sqlite
     * @return int array of Sqlite attributes
     */
    protected static int[] intUnboxArray(Object[] arr) {
        int[] unboxedArr = new int[arr.length];
        for(int i=0;i<arr.length;++i) unboxedArr[i]=(Integer)arr[i];
        return unboxedArr;
    }

    /** Transforms object array into String array
     *
     * Sqlite returns object arrays from a getObject call and because of Java boxing there's no other way to (for example in place)
     * cast to String.
     *
     * @param arr array objects returned by Sqlite
     * @return int array of Sqlite attributes
     */
    protected static String[] stringUnboxArray(Object[] arr) {
        String[] unboxedArr = new String[arr.length];
        for(int i=0;i<arr.length;++i){
            unboxedArr[i]=(String)arr[i];
        }
        return unboxedArr;
    }

    /** I don't remember what I needed this for.
     *
     * @param array1
     * @param array2
     * @return
     */
    protected static int[] concatArr(int[] array1, int[] array2){
        int[] array1and2 = new int[array1.length + array2.length];
        System.arraycopy(array1, 0, array1and2, 0, array1.length);
        System.arraycopy(array2, 0, array1and2, array1.length, array2.length);
        return array1and2;
    }


    public static void touch(File file, long timestamp) throws IOException {
        if (!file.exists())
            new FileOutputStream(file).close();
        file.setLastModified(timestamp);
    }

}