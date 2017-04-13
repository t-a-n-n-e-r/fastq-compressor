package edu.oregonstate.fastqcompressor;

/**
 * Created by tanner on 4/11/17.
 */
public class Application {

    private static ApplicationState state;

    public static void main(String[] args) throws Exception {
        if(args.length == 0) {
            new GUI();
            return;
        }

        try {
            parse(args);
        } catch (RuntimeException re) {
            System.out.println(re.getMessage());
            System.out.println("usage: <options> <input file>");
            System.out.println("options... ");
            System.out.println("  -c  > perform compression operation on input file");
            System.out.println("  -d  > perform decompression operation on input file");
            System.out.println("  -g  > enable on-the-fly gzip of output");
            return;
        }
        OperationExecutor.run();
    }

    public static void parse(String[] args) {
        int i = 0;
        for(; i < args.length; i++) {
            if(args[i].startsWith("-")) {
                handleOptions(args[i]);
            } else break;
        }

        if(ApplicationState.getOperation() == null) {
            throw new RuntimeException("no operation specified.");
        } else if(i >= args.length) {
            throw new RuntimeException("no input file specified.");
        }

        ApplicationState.setInputFile(args[i++]);
        ApplicationState.determineOutputFile();

        if(i != args.length) {
            throw new RuntimeException("too many arguments.");
        }
    }

    private static void handleOptions(String opts) {
        if(opts.contains("g"))
            ApplicationState.setGzipEnabled(true);
        opts = opts.replace("g", "");

        if(opts.contains("c"))
            if(ApplicationState.getOperation() == null)
                ApplicationState.setOperation(ApplicationState.Operation.COMPRESS);
            else
                throw new RuntimeException("too many operations supplied.");
        opts = opts.replace("c", "");

        if(opts.contains("d"))
            if(ApplicationState.getOperation() == null)
                ApplicationState.setOperation(ApplicationState.Operation.DECOMPRESS);
            else
                throw new RuntimeException("too many operations supplied.");
        opts = opts.replace("d", "");

        if(!opts.equals("-")) {
            throw new RuntimeException("invalid options specified: " + opts);
        }
    }

}
