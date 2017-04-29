package com.networknt.codegen;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    /**
     * Camelize name (parameter, property, method, etc) with upper case for first letter
     * copied from Twitter elephant bird
     * https://github.com/twitter/elephant-bird/blob/master/core/src/main/java/com/twitter/elephantbird/util/Strings.java
     *
     * @param word string to be camelize
     * @return camelized string
     */
    public static String camelize(String word) {
        return camelize(word, false);
    }

    /**
     * Camelize name (parameter, property, method, etc)
     *
     * @param word string to be camelize
     * @param lowercaseFirstLetter lower case for first letter if set to true
     * @return camelized string
     */
    public static String camelize(String word, boolean lowercaseFirstLetter) {
        // Replace all slashes with dots (package separator)
        Pattern p = Pattern.compile("\\/(.?)");
        Matcher m = p.matcher(word);
        while (m.find()) {
            word = m.replaceFirst("." + m.group(1)/*.toUpperCase()*/);
            m = p.matcher(word);
        }

        // case out dots
        String[] parts = word.split("\\.");
        StringBuilder f = new StringBuilder();
        for (String z : parts) {
            if (z.length() > 0) {
                f.append(Character.toUpperCase(z.charAt(0))).append(z.substring(1));
            }
        }
        word = f.toString();

        m = p.matcher(word);
        while (m.find()) {
            word = m.replaceFirst("" + Character.toUpperCase(m.group(1).charAt(0)) + m.group(1).substring(1)/*.toUpperCase()*/);
            m = p.matcher(word);
        }

        // Uppercase the class name.
        p = Pattern.compile("(\\.?)(\\w)([^\\.]*)$");
        m = p.matcher(word);
        if (m.find()) {
            String rep = m.group(1) + m.group(2).toUpperCase() + m.group(3);
            rep = rep.replaceAll("\\$", "\\\\\\$");
            word = m.replaceAll(rep);
        }

        // Remove all underscores
        p = Pattern.compile("(_)(.)");
        m = p.matcher(word);
        while (m.find()) {
            word = m.replaceFirst(m.group(2).toUpperCase());
            m = p.matcher(word);
        }

        if (lowercaseFirstLetter) {
            word = word.substring(0, 1).toLowerCase() + word.substring(1);
        }

        return word;
    }

}
