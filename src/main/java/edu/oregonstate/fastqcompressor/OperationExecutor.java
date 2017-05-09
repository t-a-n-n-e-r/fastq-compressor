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

/**
 * Created by tanner on 4/12/17.
 */
public class OperationExecutor {

    public static double run() throws Exception {
        if(ApplicationState.Operation.COMPRESS.equals(ApplicationState.getOperation())) {
            return compress();
        } else if(ApplicationState.Operation.DECOMPRESS.equals(ApplicationState.getOperation())) {
            return decompress();
        } else {
            throw new RuntimeException("no operation specified. please either supply -c (compress) or -d (decompress).");
        }
    }

    private static double compress() throws IOException, InterruptedException {
        final File file = new File(ApplicationState.getInputFile());
        final BufferedReader input = new BufferedReader(new FileReader(file));

        final Compressor[] compressors = new Compressor[] {
                new MetadataCompressor(ApplicationState.getOutputFile() + "1"),
                new SequenceDataCompressor(ApplicationState.getOutputFile() + "2"),
                new MetadataCompressor(ApplicationState.getOutputFile() + "3"),
                new QualityScoreCompressor(ApplicationState.getOutputFile() + "4"),
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
        final double throughput = (file.length() / (endTime - startTime) * 1000.0D) / (1024 * 1024);
        System.out.println("r/w took " + (endTime - startTime) + " ms. throughput of " + throughput + " MB/s.");
        return throughput;
    }

    private static double decompress() throws IOException, InterruptedException {
        final BufferedWriter output = new BufferedWriter(new FileWriter(new File(ApplicationState.getOutputFile())));

        final Decompressor[] decompressors = new Decompressor[] {
                new MetadataDecompressor(ApplicationState.getInputFile() + "1"),
                new SequenceDataDecompressor(ApplicationState.getInputFile() + "2"),
                new MetadataDecompressor(ApplicationState.getInputFile() + "3"),
                new QualityScoreDecompressor(ApplicationState.getInputFile() + "4")
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
        final double throughput = (new File(ApplicationState.getOutputFile()).length() / (endTime - startTime) * 1000.0D) / (1024 * 1024);
        System.out.println("r/w took " + (endTime - startTime) + " ms. throughput of " + throughput + " MB/s.");

        output.close();

        return throughput;
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
