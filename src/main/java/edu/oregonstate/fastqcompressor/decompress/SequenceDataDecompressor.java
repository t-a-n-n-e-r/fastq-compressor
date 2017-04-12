package edu.oregonstate.fastqcompressor.decompress;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by tanner on 4/11/17.
 */
public class SequenceDataDecompressor extends Decompressor {

    public SequenceDataDecompressor(BufferedReader reader) {
        super(reader);
    }

    @Override
    protected boolean handle() {
        final char[] buff = new char[25];
        final char[] reconstructed = new char[100];
        try {
            int read = getReader().read(buff);
            if(read != 25)
                return false;

            for(int i = 0; i < 25; i++) {
                for(int j = 0; j < 4; j++) {
                    switch((buff[i] & (0b00000011 << j * 2)) >> j * 2) {
                        case 0b00000000: reconstructed[i * 4 + j] = 'A'; break;
                        case 0b00000001: reconstructed[i * 4 + j] = 'C'; break;
                        case 0b00000010: reconstructed[i * 4 + j] = 'T'; break;
                        case 0b00000011: reconstructed[i * 4 + j] = 'G'; break;
                    }
                }
            }

            getOutputLines().add(new String(reconstructed));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
