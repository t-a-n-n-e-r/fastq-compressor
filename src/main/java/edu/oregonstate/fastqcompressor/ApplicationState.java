package edu.oregonstate.fastqcompressor;

/**
 * Created by tanner on 4/12/17.
 */
public class ApplicationState {

    private String inputFile;
    private String outputFile;
    private boolean gzipEnabled;
    private Operation operation;

    private ApplicationState() {}

    public String getInputFile() {
        return inputFile;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public boolean isGzipEnabled() {
        return gzipEnabled;
    }

    public Operation getOperation() {
        return operation;
    }

    public static ApplicationState build(String[] args) {
        final ApplicationState applicationState = new ApplicationState();

        int i = 0;
        for(; i < args.length; i++) {
            if(args[i].startsWith("-")) {
                handleOptions(applicationState, args[i]);
            } else break;
        }

        if(applicationState.operation == null) {
            throw new RuntimeException("no operation specified.");
        } else if(i >= args.length) {
            throw new RuntimeException("no input file specified.");
        }

        applicationState.inputFile = args[i++];

        if(i < args.length) {
            applicationState.outputFile = args[i++];
        } else {
            if(applicationState.operation.equals(Operation.COMPRESS)) {
                applicationState.outputFile = applicationState.inputFile + ".fuc";
            } else if(applicationState.operation.equals(Operation.DECOMPRESS)) {
                if(!applicationState.inputFile.endsWith(".fuc"))
                    throw new RuntimeException("not sure what to name output file. expected .fuc file as input.");
                applicationState.outputFile = applicationState.inputFile.substring(0, applicationState.inputFile.length() - 4);
            }
        }

        if(i != args.length) {
            throw new RuntimeException("too many arguments.");
        }

        return applicationState;
    }

    private static void handleOptions(final ApplicationState applicationState, String opts) {
        if(opts.contains("g"))
            applicationState.gzipEnabled = true;
        opts = opts.replace("g", "");

        if(opts.contains("c"))
            if(applicationState.operation == null)
                applicationState.operation = Operation.COMPRESS;
            else
                throw new RuntimeException("too many operations supplied.");
        opts = opts.replace("c", "");

        if(opts.contains("d"))
            if(applicationState.operation == null)
                applicationState.operation = Operation.DECOMPRESS;
            else
                throw new RuntimeException("too many operations supplied.");
        opts = opts.replace("d", "");

        if(!opts.equals("-")) {
            throw new RuntimeException("invalid options specified: " + opts);
        }
    }

    public enum Operation {
        COMPRESS, DECOMPRESS
    }

}
