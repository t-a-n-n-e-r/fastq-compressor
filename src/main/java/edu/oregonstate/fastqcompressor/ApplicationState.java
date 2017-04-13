package edu.oregonstate.fastqcompressor;

/**
 * Created by tanner on 4/12/17.
 */
public class ApplicationState {

    private static String inputFile;
    private static String outputFile;
    private static boolean gzipEnabled;
    private static Operation operation;

    private ApplicationState() {}

    public static String getInputFile() {
        return inputFile;
    }

    public static String getOutputFile() {
        return outputFile;
    }

    public static boolean isGzipEnabled() {
        return gzipEnabled;
    }

    public static Operation getOperation() {
        return operation;
    }

    public static void setInputFile(String inputFile) {
        ApplicationState.inputFile = inputFile;
    }

    public static void setOutputFile(String outputFile) {
        ApplicationState.outputFile = outputFile;
    }

    public static void setGzipEnabled(boolean gzipEnabled) {
        ApplicationState.gzipEnabled = gzipEnabled;
    }

    public static void setOperation(Operation operation) {
        ApplicationState.operation = operation;
    }

    public static void determineOutputFile() {
        if(getOperation().equals(Operation.COMPRESS)) {
            ApplicationState.setOutputFile(isGzipEnabled() ? getInputFile() + ".gfuc" : getInputFile() + ".fuc");
        } else if(getOperation().equals(Operation.DECOMPRESS)) {
            if(!getInputFile().endsWith(".fuc") && !isGzipEnabled() || !getInputFile().endsWith(".gfuc") && isGzipEnabled())
                throw new RuntimeException("not sure what to name output file. expected "
                        + (isGzipEnabled() ? "gfuc" : "fuc") + " file as input.");
            setOutputFile(getInputFile().substring(0, getInputFile().length() - (isGzipEnabled() ? 5 : 4)));
        }
    }

    public enum Operation {
        COMPRESS, DECOMPRESS
    }

}
