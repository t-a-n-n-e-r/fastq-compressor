package edu.oregonstate.fastqcompressor.compressor;

import edu.oregonstate.fastqcompressor.ApplicationState;

import java.io.*;

/**
 * Created by tanner on 4/11/17.
 */
public class SequenceDataCompressor extends Compressor {

    private static final byte MASKS = 0b00000110;

    private OutputStream tmpOutput;

    public SequenceDataCompressor(String file) {
        super(file);
        try {
            tmpOutput = new FileOutputStream(new File("/Users/tanner/Desktop/tmp"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handle(String ln) {
        try {
            if(ApplicationState.isSoftwareEnabled()) {
                for (int i = 0; i < ln.length(); i += 4) {
                    int b = (int) ln.charAt(i);
                    int b2 = (int) ln.charAt(i + 1);
                    int b3 = (int) ln.charAt(i + 2);
                    int b4 = (int) ln.charAt(i + 3);

                    int total = (b & MASKS) << 5 | (b2 & MASKS) << 3 | (b3 & MASKS) << 1 | (b4 & MASKS) >> 1;

                    getOutputStream().write((char) total);
                }
            } else {
                tmpOutput.write(ln.getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void finish() {
        if(!ApplicationState.isSoftwareEnabled()) {
            try {
                tmpOutput.close();

                final Process p = Runtime.getRuntime().exec("./compress");
                p.waitFor();

                final File fpgaOutput = new File("./tmp2");
                fpgaOutput.renameTo(new File(getOutputFilePath()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.finish();
    }
}
