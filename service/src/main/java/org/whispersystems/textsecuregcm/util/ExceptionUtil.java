package org.whispersystems.textsecuregcm.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class ExceptionUtil {
    public static String getExceptionDetail(Exception e)  {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        PrintStream printStream = new PrintStream(byteArrayOutputStream );

        e.printStackTrace(printStream);
        try {
            return byteArrayOutputStream.toString("utf-8");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

}
