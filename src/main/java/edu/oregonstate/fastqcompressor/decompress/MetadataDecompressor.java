package edu.oregonstate.fastqcompressor.decompress;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tanner on 4/11/17.
 */
public class MetadataDecompressor extends Decompressor {

    private String lastNonemptyLine;
    private Map<Integer, String> splitVals = new HashMap<>();

    public MetadataDecompressor(BufferedReader reader) {
        super(reader);
    }

    @Override
    protected boolean handle() {
        try {
            String ln;
            if ((ln = getReader().readLine()) != null) {
                if(ln.equals("")) {
                    getOutputLines().add(lastNonemptyLine);
                } else {
                    final String[] split = ln.split(":", -1);
                    if(split.length == 1) {
                        getOutputLines().add(ln);
                        lastNonemptyLine = ln;
                    } else {
                        final StringBuilder builder = new StringBuilder();
                        for(int i = 0; i < split.length; i++) {
                            if(!split[i].equals("")) {
                                splitVals.put(i, split[i]);
                            }
                            builder.append(i == 0 ? splitVals.get(i) : ":" + splitVals.get(i));
                        }
                        getOutputLines().add(builder.toString());
                    }
                }
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
