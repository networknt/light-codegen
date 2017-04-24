package com.networknt.codegen;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * Created by stevehu on 2017-04-23.
 */
public class Utils {

    // Everyone who is using this method needs to closed the stream
    private static InputStream urlToStream(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        return new BufferedInputStream(url.openStream());
    }


    private static void downloadUsingNIO(String urlStr, String file) throws IOException {
        URL url = new URL(urlStr);
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        FileOutputStream fos = new FileOutputStream(file);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        rbc.close();
    }

}
