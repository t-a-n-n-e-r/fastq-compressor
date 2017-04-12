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

/**
 * Created by tanner on 4/11/17.
 */
public class Application {

    public static void main(String[] args) {
        compress();
        //decompress();
    }

    private static void compress() {
        int lnNum = 0;
        try {
            final File file = new File("./test3.fastq");
            final BufferedReader input = new BufferedReader(new FileReader(file));

            final Compressor[] compressors = new Compressor[] {
                    new MetadataCompressor(openWriter("./test.fastq.1")),
                    new SequenceDataCompressor(openWriter("./test.fastq.2")),
                    new MetadataCompressor(openWriter("./test.fastq.3")),
                    new QualityScoreCompressor(openWriter("./test.fastq.4")),
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

                lnNum += 4;
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
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("ln = " + lnNum);
        }
    }

    private static void decompress() {
        try {
            final BufferedWriter output = new BufferedWriter(new FileWriter(new File("./test.fastq.out")));

            final Decompressor[] decompressors = new Decompressor[] {
                    new MetadataDecompressor(openReader("./test.fastq.1")),
                    new SequenceDataDecompressor(openReader("./test.fastq.2")),
                    new MetadataDecompressor(openReader("./test.fastq.3")),
                    new QualityScoreDecompressor(openReader("./test.fastq.4"))
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
                    + (new File("./test.fastq.out").length() / (endTime - startTime) * 1000.0D) / (1024 * 1024) + " MB/s.");

            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static BufferedReader openReader(final String file) throws IOException {
        return new BufferedReader(new InputStreamReader(
                new GZIPInputStream(new FileInputStream(file))));
    }

    private static BufferedWriter openWriter(final String file) throws IOException {
        return new BufferedWriter(new OutputStreamWriter(
                (new FileOutputStream(file))));
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
