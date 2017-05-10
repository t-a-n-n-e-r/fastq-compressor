package edu.oregonstate.fastqcompressor.compressor;

import edu.oregonstate.fastqcompressor.util.Files;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by tanner on 4/11/17.
 */
public abstract class Compressor extends Thread {

    private boolean running = true;

    private final ConcurrentLinkedQueue<String> linkedQueue = new ConcurrentLinkedQueue<>();
    private final String file;

    private BufferedWriter writer;
    private OutputStream outputStream;

    public Compressor(final String file) {
        this.file = file;
    }

    public int getBacklogSize() {
        return linkedQueue.size();
    }

    protected OutputStream getOutputStream() {
        if(outputStream == null) {
            try {
                outputStream = new BufferedOutputStream(Files.openOutputStream(file));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return outputStream;
    }

    protected BufferedWriter getWriter() {
        if(writer == null) {
            try {
                writer = Files.openWriter(getOutputFilePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return writer;
    }

    protected String getOutputFilePath() {
        return file;
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
                    if(writer != null)
                        writer.close();
                    else if(outputStream != null)
                        outputStream.close();
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
