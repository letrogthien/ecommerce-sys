package com.chuadatten.user.file;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service

public class FileBase64 {
    @Value("${security.secret.aes.key}")
    String secretKey;
    @Value("${security.secret.aes.iv}")
    String initVector;
    public String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }

    public String fileToBase64(MultipartFile file) throws IOException {
        String fileExtension = getFileExtension(file.getOriginalFilename());
        String base64Prefix = "data:image/" + fileExtension + ";base64,";
        String base64Data = java.util.Base64.getEncoder().encodeToString(file.getBytes());
        return base64Prefix + base64Data;
    }
    public File base64ToFile(String base64String, String filePath) throws IOException {
        if (base64String == null || !base64String.contains(",")) {
            throw new IllegalArgumentException("Invalid base64 string");
        }
        String base64Data = base64String.split(",")[1];
        byte[] fileBytes = java.util.Base64.getDecoder().decode(base64Data);
        FileUtils.writeByteArrayToFile(new File(filePath), fileBytes);
        return new File(filePath);
    }



    public String encodeBase64(String baseString) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        byte[] keyBytes = java.util.Base64.getDecoder().decode(secretKey);
        SecretKey s = new javax.crypto.spec.SecretKeySpec(keyBytes, "AES");

        byte[] ivBytes = java.util.Base64.getDecoder().decode(initVector);
        IvParameterSpec ivParams = new IvParameterSpec(ivBytes);
        
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        cipher.init(Cipher.ENCRYPT_MODE, s, ivParams);
        byte[] encryptedBytes = cipher.doFinal(baseString.getBytes());

        return java.util.Base64.getEncoder().encodeToString(encryptedBytes);
        
    }

    public String decodeBase64(String encodedString) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        byte[] keyBytes = java.util.Base64.getDecoder().decode(secretKey);
        SecretKey s = new javax.crypto.spec.SecretKeySpec(keyBytes, "AES");

        byte[] ivBytes = java.util.Base64.getDecoder().decode(initVector);
        IvParameterSpec ivParams = new IvParameterSpec(ivBytes);
        
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        cipher.init(Cipher.DECRYPT_MODE, s, ivParams);
        byte[] decodedBytes = java.util.Base64.getDecoder().decode(encodedString);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);

        return new String(decryptedBytes);
    }


}
