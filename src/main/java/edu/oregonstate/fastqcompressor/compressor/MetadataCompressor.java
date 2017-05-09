package edu.oregonstate.fastqcompressor.compressor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tanner on 4/11/17.
 */
public class MetadataCompressor extends Compressor {

    private String lastLine;
    private Map<Integer, String> splitVals = new HashMap<>();

    public MetadataCompressor(String file) {
        super(file);
    }

    @Override
    public void handle(String ln) {
        try {
            final String[] split = ln.split(":");
            if(split.length == 1) {
                if (ln.equals(lastLine)) {
                    getWriter().newLine();
                } else {
                    getWriter().write(ln);
                    getWriter().newLine();
                }
            } else {
                for(int i = 0; i < split.length; i++) {
                    if(split[i].equals(splitVals.get(i)))
                        getWriter().write(i == 0 ? "" : ":");
                    else {
                        splitVals.put(i, split[i]);
                        getWriter().write(i == 0 ? splitVals.get(i) : ":" + splitVals.get(i));
                    }
                }
                getWriter().newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        lastLine = ln;
    }
}
