package cs321.create;

import java.util.Scanner;

/**
 * Extracts the SSH log file entries.
 *
 * @author 
 */
public class SSHFileReader {
    private Scanner scanner;

    /**
     * Creates a new SSHFileReader with the specified file name.
     *
     * @param fileName the name of the file to read
     */
    public SSHFileReader(String fileName) {
        try {
            this.scanner = new Scanner(new java.io.File(fileName));
        } catch (java.io.FileNotFoundException e) {
            System.err.println("Error: File not found: " + fileName);
            System.exit(1);
        }
    }

    /**
     * Returns true if there are more lines to read from the file.
     *
     * @return true if there are more lines to read, false otherwise
     */
    public boolean hasNextLine() {
        return this.scanner.hasNextLine();
    }

    /**
     * Returns the next line from the file.
     *
     * @return the next line from the file
     */
    public String nextLine() {
        return this.scanner.nextLine();
    }

    /**
     * Closes the scanner.
     */
    public void close() {
        this.scanner.close();
    }

}
