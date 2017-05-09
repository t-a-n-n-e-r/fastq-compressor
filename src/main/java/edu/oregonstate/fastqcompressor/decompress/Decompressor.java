package edu.oregonstate.fastqcompressor.decompress;

import edu.oregonstate.fastqcompressor.util.Files;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by tanner on 4/11/17.
 */
public abstract class Decompressor extends Thread {

    private final ConcurrentLinkedQueue<String> linkedQueue = new ConcurrentLinkedQueue<>();
    private final String file;

    private BufferedReader reader;
    private InputStream inputStream;

    public Decompressor(final String file) {
        this.file = file;
    }

    protected InputStream getInputStream() {
        try {
            if(inputStream == null)
                inputStream = Files.openInputStream(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return inputStream;
    }

    protected BufferedReader getReader() {
        try {
            if(reader == null)
                reader = Files.openReader(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return reader;
    }

    public ConcurrentLinkedQueue<String> getOutputLines() {
        return linkedQueue;
    }

    @Override
    public void run() {
        while(handle());
    }

    protected abstract boolean handle();
}