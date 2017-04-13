package edu.oregonstate.fastqcompressor;

import edu.oregonstate.fastqcompressor.compressor.Compressor;
import edu.oregonstate.fastqcompressor.compressor.MetadataCompressor;
import edu.oregonstate.fastqcompressor.compressor.QualityScoreCompressor;
import edu.oregonstate.fastqcompressor.compressor.SequenceDataCompressor;
import edu.oregonstate.fastqcompressor.decompress.Decompressor;
import edu.oregonstate.fastqcompressor.decompress.MetadataDecompressor;
import edu.oregonstate.fastqcompressor.decompress.QualityScoreDecompressor;
import edu.oregonstate.fastqcompressor.decompress.SequenceDataDecompressor;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by tanner on 4/11/17.
 */
public class Application {

    private static ApplicationState state;

    public static void main(String[] args) throws Exception {
        try {
            state = ApplicationState.build(args);
        } catch (RuntimeException re) {
            System.out.println(re.getMessage());
            System.out.println("usage: <options> <input file> <? output file>");
            System.out.println("options... ");
            System.out.println("  -c  > perform compression operation on input file");
            System.out.println("  -d  > perform decompression operation on input file");
            System.out.println("  -g  > enable on-the-fly gzip of output");
            return;
        }

        if(ApplicationState.Operation.COMPRESS.equals(state.getOperation())) {
            compress();
        } else if(ApplicationState.Operation.DECOMPRESS.equals(state.getOperation())) {
            decompress();
        } else {
            System.out.println("no operation specified. please either supply -c (compress) or -d (decompress).");
        }
    }

    private static void compress() throws IOException, InterruptedException {
        final File file = new File(state.getInputFile());
        final BufferedReader input = new BufferedReader(new FileReader(file));

        final Compressor[] compressors = new Compressor[] {
                new MetadataCompressor(openWriter(state.getOutputFile() + ".1")),
                new SequenceDataCompressor(openWriter(state.getOutputFile() + ".2")),
                new MetadataCompressor(openWriter(state.getOutputFile() + ".3")),
                new QualityScoreCompressor(openWriter(state.getOutputFile() + ".4")),
        };

        for (final Compressor w : compressors) {
            w.start();
        }

        final long startTime = System.currentTimeMillis();

        while (true) {
            final String ln1 = input.readLine();
            if (ln1 == null)
                break;
            final String ln2 = input.readLine();
            final String ln3 = input.readLine();
            final String ln4 = input.readLine();

            compressors[0].pass(ln1);
            compressors[1].pass(ln2);
            compressors[2].pass(ln3);
            compressors[3].pass(ln4);
        }

        final long readDone = System.currentTimeMillis();
        System.out.println("read done @ " + (readDone - startTime) + " ms.");

        for (final Compressor w : compressors) {
            w.finish();
        }

        waitForDeath(compressors);

        final long endTime = System.currentTimeMillis();
        System.out.println("r/w took " + (endTime - startTime) + " ms. throughput of "
                + (file.length() / (endTime - startTime) * 1000.0D) / (1024 * 1024) + " MB/s.");
    }

    private static void decompress() throws IOException, InterruptedException {
        final BufferedWriter output = new BufferedWriter(new FileWriter(new File(state.getOutputFile())));

        final Decompressor[] decompressors = new Decompressor[] {
                new MetadataDecompressor(openReader(state.getInputFile() + ".1")),
                new SequenceDataDecompressor(openReader(state.getInputFile() + ".2")),
                new MetadataDecompressor(openReader(state.getInputFile() + ".3")),
                new QualityScoreDecompressor(openReader(state.getInputFile() + ".4"))
        };

        for(final Decompressor d : decompressors) {
            d.start();
        }

        final String[] lines = new String[decompressors.length];
        int index = 0;

        final long startTime = System.currentTimeMillis();

        outer:
        while(true) {
            for(int i = index; i < decompressors.length; i++) {
                lines[i] = decompressors[i].getOutputLines().poll();
                if(lines[i] == null) {
                    if(!decompressors[i].isAlive())
                        break outer;
                    Thread.sleep(10);
                    index = i;
                    continue outer;
                }
            }

            index = 0;

            for(int i = 0; i < decompressors.length; i++) {
                output.write(lines[i]);
                output.newLine();
            }
        }

        final long endTime = System.currentTimeMillis();
        System.out.println("r/w took " + (endTime - startTime) + " ms. throughput of "
                + (new File(state.getOutputFile()).length() / (endTime - startTime) * 1000.0D) / (1024 * 1024) + " MB/s.");

        output.close();
    }

    private static BufferedReader openReader(final String file) throws IOException {
        return state.isGzipEnabled()
                ? new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))))
                : new BufferedReader(new InputStreamReader(new FileInputStream(file)));
    }

    private static BufferedWriter openWriter(final String file) throws IOException {
        return state.isGzipEnabled()
                ? new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(file))))
                : new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
    }

    private static void waitForDeath(final Thread[] threads) throws InterruptedException {
        while(true) {
            boolean alive = false;
            for(final Thread t : threads) {
                if(t.isAlive()) {
                    alive = true;
                    break;
                }
            }
            if(!alive)
                break;
            else
                Thread.sleep(20);
        }
    }
}
