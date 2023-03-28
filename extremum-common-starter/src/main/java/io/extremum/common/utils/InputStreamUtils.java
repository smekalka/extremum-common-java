package io.extremum.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class InputStreamUtils {
    private static final int BYTE_BUFFER_SIZE = 1024;
    private static final Logger LOGGER = LoggerFactory.getLogger(InputStreamUtils.class);

    public static byte[] toByteArray(InputStream is) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        byte[] buffer = new byte[BYTE_BUFFER_SIZE];

        int read;

        try {
            while ((read = is.read(buffer, 0, buffer.length)) != -1) {
                bos.write(buffer, 0, read);
            }
        } catch (IOException ex) {
            LOGGER.error("Can't read an InputStream", ex);
            throw new RuntimeException("Can't read an InputStream", ex);
        }

        return bos.toByteArray();
    }

    public static InputStream fromByteArray(byte[] bytes) {
        return new ByteArrayInputStream(bytes);
    }

    public static InputStream fromString(String string) {
        return new ByteArrayInputStream(string.getBytes());
    }

    public static String convertToString(InputStream inputStream) {
        Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }
}
