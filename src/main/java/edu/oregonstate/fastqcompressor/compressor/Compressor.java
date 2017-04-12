package edu.oregonstate.fastqcompressor.compressor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by tanner on 4/11/17.
 */
public abstract class Compressor extends Thread {

    private boolean running = true;

    private final ConcurrentLinkedQueue<String> linkedQueue = new ConcurrentLinkedQueue<>();
    private final BufferedWriter writer;

    public Compressor(final BufferedWriter writer) {
        this.writer = writer;
    }

    public int getBacklogSize() {
        return linkedQueue.size();
    }

    protected BufferedWriter getWriter() {
        return writer;
    }

    public void pass(String ln) {
        linkedQueue.add(ln);
    }

    @Override
    public void run() {
        while(true) {
            String ln;
            while((ln = linkedQueue.poll()) != null) {
                handle(ln);
            }
            if(!running) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    public void finish() {
        running = false;
    }

    protected abstract void handle(String ln);
}
