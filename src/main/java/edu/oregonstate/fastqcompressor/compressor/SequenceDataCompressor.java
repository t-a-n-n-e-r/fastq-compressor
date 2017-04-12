package edu.oregonstate.fastqcompressor.compressor;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Created by tanner on 4/11/17.
 */
public class SequenceDataCompressor extends Compressor {

    private static final byte MASKS = 0b00000110;

    public SequenceDataCompressor(BufferedWriter writer) {
        super(writer);
    }

    @Override
    public void handle(String ln) {
        try {
            for(int i = 0; i < ln.length(); i += 4) {
                int b = (int) ln.charAt(i);
                int b2 = (int) ln.charAt(i + 1);
                int b3 = (int) ln.charAt(i + 2);
                int b4  = (int) ln.charAt(i + 3);

                int total = (b & MASKS) >> 1 | (b2 & MASKS) << 1 | (b3 & MASKS) << 3 | (b4 & MASKS) << 5;

                getWriter().write((char) total);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
