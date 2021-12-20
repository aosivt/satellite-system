package org.satellite.system.image.converter.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public interface Md5StringNameFile {
    default String getMD5Checksum(final String nameFile){
        byte[] b = nameFile.getBytes(StandardCharsets.UTF_8);
        String result = "";
        for (int i = 0; i < b.length; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }
    default String convertIdToStringPath(final String hash) {
        return hash.replaceAll("(.{2})", "$1/");
    }
    default String getStringPathForStringNameFile(final String nameFile){
        return convertIdToStringPath(getMD5Checksum(nameFile));
    }
}
