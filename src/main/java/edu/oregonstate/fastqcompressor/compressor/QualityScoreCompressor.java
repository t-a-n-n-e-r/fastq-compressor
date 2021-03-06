package edu.oregonstate.fastqcompressor.compressor;

import java.io.IOException;

/**
 * Created by tanner on 4/11/17.
 */
public class QualityScoreCompressor extends Compressor {

    public QualityScoreCompressor(String file) {
        super(file);
    }

    @Override
    public void handle(String ln) {
        try {
            getWriter().write(ln);
            getWriter().newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
