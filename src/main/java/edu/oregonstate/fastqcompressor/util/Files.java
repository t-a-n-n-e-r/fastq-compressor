package edu.oregonstate.fastqcompressor.util;

import edu.oregonstate.fastqcompressor.ApplicationState;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by tanner on 5/9/17.
 */
public class Files {

    public static InputStream openInputStream(final String file) throws IOException {
        return ApplicationState.isGzipEnabled()
                ? new GZIPInputStream(new FileInputStream(file))
                : new FileInputStream(file);
    }

    public static BufferedReader openReader(final String file) throws IOException {
        return new BufferedReader(new InputStreamReader(openInputStream(file)));
    }

    public static OutputStream openOutputStream(final String file) throws IOException {
        return ApplicationState.isGzipEnabled()
                ? new GZIPOutputStream(new FileOutputStream(file))
                : new FileOutputStream(file);
    }

    public static BufferedWriter openWriter(final String file) throws IOException {
        return new BufferedWriter(new OutputStreamWriter(openOutputStream(file)));
    }

}
