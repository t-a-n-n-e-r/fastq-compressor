package edu.oregonstate.fastqcompressor.decompress;

import java.io.IOException;

/**
 * Created by tanner on 4/11/17.
 */
public class SequenceDataDecompressor extends Decompressor {

    public SequenceDataDecompressor(String file) {
        super(file);
    }

    @Override
    protected boolean handle() {
        final byte[] buff = new byte[25];
        final byte[] reconstructed = new byte[100];
        try {
            int read = getInputStream().read(buff);
            if(read != 25)
                return false;

            for(int i = 0; i < 25; i++) {
                for(int j = 0; j < 4; j++) {
                    switch((buff[i] & (0b00000011 << (3 - j) * 2)) >> (3 - j) * 2) {
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
