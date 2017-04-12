package edu.oregonstate.fastqcompressor.decompress;

import java.io.BufferedReader;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by tanner on 4/11/17.
 */
public abstract class Decompressor extends Thread {

    private final ConcurrentLinkedQueue<String> linkedQueue = new ConcurrentLinkedQueue<>();
    private final BufferedReader reader;

    public Decompressor(final BufferedReader reader) {
        this.reader = reader;
    }

    protected BufferedReader getReader() {
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