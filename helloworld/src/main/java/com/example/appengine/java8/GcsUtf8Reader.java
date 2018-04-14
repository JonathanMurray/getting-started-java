package com.example.appengine.java8;

import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsInputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;

public class GcsUtf8Reader {

  /**
   * Used below to determine the size of chucks to read in. Should be > 1kb and < 10MB
   */
  private static final int BUFFER_SIZE = 2 * 1024 * 1024;
  private final GcsService gcsService;

  public GcsUtf8Reader(GcsService gcsService) {

    this.gcsService = gcsService;
  }

  String readUtf8FromBucket(String bucketName, String objectName) throws IOException {
    GcsFilename fileName = new GcsFilename(bucketName, objectName);
    GcsInputChannel readChannel = this.gcsService
        .openPrefetchingReadChannel(fileName, 0, BUFFER_SIZE);
    ByteArrayOutputStream fileContentStream = new ByteArrayOutputStream();
    copy(Channels.newInputStream(readChannel), fileContentStream);
    return fileContentStream.toString("UTF-8");
  }

  /**
   * Transfer the data from the inputStream to the outputStream. Then close both streams.
   */
  public static void copy(InputStream input, OutputStream output) throws IOException {
    try {
      byte[] buffer = new byte[BUFFER_SIZE];
      int bytesRead = input.read(buffer);
      while (bytesRead != -1) {
        output.write(buffer, 0, bytesRead);
        bytesRead = input.read(buffer);
      }
    } finally {
      input.close();
      output.close();
    }
  }
}
