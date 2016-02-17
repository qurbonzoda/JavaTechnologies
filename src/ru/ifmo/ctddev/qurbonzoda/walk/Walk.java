package ru.ifmo.ctddev.qurbonzoda.walk;

import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by qurbonzoda on 10.02.16.
 */
public class Walk {
    final static int BUFFER_SIZE = 1024 * 100;
    public static void main(String[] args) {
        Path inputPath = Paths.get(args[0]);
        Path outputPath = Paths.get(args[1]);
        Charset charset = Charset.forName("UTF-8");
        byte[] data = new byte[BUFFER_SIZE];

        try (
                BufferedReader reader = Files.newBufferedReader(inputPath, charset);
                BufferedWriter writer = Files.newBufferedWriter(outputPath, charset);
        ) {
            String path;
            while((path = reader.readLine()) != null) {
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                String hashCode = "";
                try (FileInputStream dataReader = new FileInputStream(path)) {
                    FileChannel channel = dataReader.getChannel();
                    ByteBuffer byteBuffer = ByteBuffer.wrap(data);

                    int lenRead;
                    while ((lenRead = channel.read(byteBuffer)) != -1) {
                        md5.update(data, 0, lenRead);
                        byteBuffer.clear();
                    }
                    hashCode = HexBin.encode(md5.digest());
                    //hashCode = new BigInteger(1, md5.digest()).toString(16);
                } catch (IOException e) {
                    // ignore
                    hashCode = "00000000000000000000000000000000";
                }
                /*
                while (hashCode.length() < 32) {
                    hashCode = "0" + hashCode;
                }
                */
                writer.write(hashCode.toUpperCase() + " " + path + "\n");
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            System.out.println("Bad Input or Output file. Error with message: " + e.getMessage());
        }
    }
}
