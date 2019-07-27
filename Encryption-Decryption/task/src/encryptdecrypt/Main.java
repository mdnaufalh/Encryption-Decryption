package encryptdecrypt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

enum AlgorithmType {
    SHIFT_ENCRYPTION,
    UNICODE_ENCRYPTION
}

class AlgorithmFactory {
    public static EncryptionAlgorithm setAlgorithm(AlgorithmType type) {
        if (type == AlgorithmType.SHIFT_ENCRYPTION) {
            return new ShiftEncryption();
        } else if (type == AlgorithmType.UNICODE_ENCRYPTION) {
            return new UnicodeEncryption();
        }
        return null;
    }
}

interface EncryptionAlgorithm {
    public String encrypt(String message, int key);

    public String decrypt(String message, int key);

    public String getOutput(String message, int key, String mode);
}

class UnicodeEncryption implements EncryptionAlgorithm {
    @Override
    public String encrypt(String message, int key) {
        StringBuilder convertedMessage = new StringBuilder();
        for (int i = 0; i < message.length(); ++i) {
            convertedMessage.append((char) (message.charAt(i) + key));
        }
        return convertedMessage.toString();
    }

    @Override
    public String decrypt(String message, int key) {
        return encrypt(message, -key);
    }

    @Override
    public String getOutput(String message, int key, String mode) {
        if ("enc".equalsIgnoreCase(mode)) {
            return encrypt(message, key);
        } else if ("dec".equalsIgnoreCase(mode)) {
            return decrypt(message, key);
        } else {
            System.err.println("Invalid mode: Use \"enc\" | \"dec\"");
        }
        return null;
    }
}

class ShiftEncryption implements EncryptionAlgorithm {
    @Override
    public String encrypt(String message, int key) {
        StringBuilder convertedMessage = new StringBuilder();
        for (int i = 0; i < message.length(); ++i) {
            char ch = message.charAt(i);
            if (Character.isAlphabetic(ch)) {
                if (Character.isUpperCase(ch)) {
                    ch = (char) ((ch - 'A' + key) % 26 + 'A');
                } else {
                    ch = (char) ((ch - 'a' + key) % 26 + 'a');
                }
            }
            convertedMessage.append(ch);
        }
        return convertedMessage.toString();
    }

    @Override
    public String decrypt(String message, int key) {
        key %= 26;  // To prevent negative values while conversion
        StringBuilder convertedMessage = new StringBuilder();
        for (int i = 0; i < message.length(); ++i) {
            char ch = message.charAt(i);
            if (Character.isAlphabetic(ch)) {
                if (Character.isUpperCase(ch)) {
                    ch = (char) ((ch - 'A' - key + 26) % 26 + 'A');
                } else {
                    ch = (char) ((ch - 'a' - key + 26) % 26 + 'a');
                }
            }
            convertedMessage.append(ch);
        }
        return convertedMessage.toString();
    }

    @Override
    public String getOutput(String message, int key, String mode) {
        if ("enc".equalsIgnoreCase(mode)) {
            return encrypt(message, key);
        } else if ("dec".equalsIgnoreCase(mode)) {
            return decrypt(message, key);
        } else {
            System.err.println("Invalid mode: Use \"enc\" | \"dec\"");
            System.exit(0);
        }
        return null;
    }
}

public class Main {

    private static int key = 0;
    private static String message = null;
    private static String mode = null;
    private static String inputFilePath = null;
    private static String outputFilePath = null;
    private static String output = null;
    private static AlgorithmType algorithmType = null;
    
    /*
     * Parse arguments from the terminal
     */
    public static void parseArguments(String[] args) {
        for (int i = 0; i < args.length; i += 2) {
            if ("-mode".equals(args[i])) {
                mode = args[i + 1];
            } else if ("-key".equals(args[i])) {
                key = Integer.parseInt(args[i + 1]);
            } else if ("-data".equals(args[i])) {
                message = args[i + 1];
            } else if ("-in".equals(args[i])) {
                inputFilePath = args[i + 1];
            } else if ("-out".equals(args[i])) {
                outputFilePath = args[i + 1];
            } else if ("-alg".equals(args[i])) {
                String temp = args[i + 1];
                if ("unicode".equalsIgnoreCase(temp)) {
                    algorithmType = AlgorithmType.UNICODE_ENCRYPTION;
                } else if ("shift".equalsIgnoreCase(temp)) {
                    algorithmType = AlgorithmType.SHIFT_ENCRYPTION;
                } else {
                    System.err.println("Specify algorithm type : shift | unicode");
                    System.exit(0);
                }
            }
        }
    }

    /*
     * Read missing parameters from stdin if not specified in the arguments initially.
     */
    public static void getMessageFromStdin() {
        if (message == null && inputFilePath == null) {
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNext()) {
                String op = scanner.next();
                if ("-data".equals(op)) {
                    message = scanner.next();
                }
                if ("-in".equals(op)) {
                    inputFilePath = scanner.next();
                }
            }
            scanner.close();
        }
    }

    /*
     * Read message from input file to be encrypted/decrypted.
     */
    public static void readMessageFromFile() throws FileNotFoundException {
        try {
            if (message == null) {
                assert inputFilePath != null;
                File inputFile = new File(inputFilePath);
                Scanner scanner = new Scanner(inputFile);
                StringBuilder temp = new StringBuilder();
                while (scanner.hasNext()) {
                    temp.append(scanner.next());
                    temp.append(" ");
                }
                message = temp.toString();
                scanner.close();
            }
        } catch (Exception e) {
            System.err.println("Incorrect input file path " + e.getMessage());
            System.exit(0);
        }
        message = message.trim();
    }

    /*
     * Print decrypted/encrypted output message to specified output file or the stdout.
     */
    public static void printOutput() throws FileNotFoundException {
        try {
            if (outputFilePath == null) {
                System.out.print(output);
            } else {
                File outputFile = new File(outputFilePath);
                PrintWriter pw = new PrintWriter(outputFile);
                pw.print(output);
                pw.close();
            }
        } catch (FileNotFoundException e) {
            System.err.println("Output file is a directory! " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        parseArguments(args);
        getMessageFromStdin();
        readMessageFromFile();
        EncryptionAlgorithm algorithm = AlgorithmFactory.setAlgorithm(algorithmType);
        output = algorithm.getOutput(message, key, mode);
        printOutput();
    }


}
