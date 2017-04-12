package edu.oregonstate.fastqcompressor.decompress;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by tanner on 4/11/17.
 */
public class QualityScoreDecompressor extends Decompressor {

    public QualityScoreDecompressor(BufferedReader reader) {
        super(reader);
    }

    @Override
    protected boolean handle() {
        try {
            String ln;
            if ((ln = getReader().readLine()) != null) {
                getOutputLines().add(ln);
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
