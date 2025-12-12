package com.localmesh.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


 //Утилита для работы с файлами: кодирование/декодирование Base64,
 //сжатие изображений, работа с аудио

public class FileUtil {
    private static final String TAG = "FileUtil";
    private static final int MAX_IMAGE_SIZE = 1024; // Максимальная ширина/высота в пикселях
    private static final int COMPRESSION_QUALITY = 80; // Качество JPEG в процентах
    

     //Конвертирует файл в Base64 строку

    public static String fileToBase64(String filePath) throws IOException {
        File file = new File(filePath);
        byte[] bytes = new byte[(int) file.length()];
        
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(bytes);
        }
        
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
    

     //Сохраняет Base64 строку как файл

    public static void base64ToFile(String base64Data, String outputPath) throws IOException {
        byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
        
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            fos.write(bytes);
        }
    }
    

     //Сжимает изображение и конвертирует в Base64

    public static String compressImageToBase64(String imagePath) {
        try {
            // Декодируем изображение с учётом размера
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imagePath, options);
            
            // Рассчитываем коэффициент сжатия
            int scale = 1;
            while (options.outWidth / scale > MAX_IMAGE_SIZE || 
                   options.outHeight / scale > MAX_IMAGE_SIZE) {
                scale *= 2;
            }
            
            // Декодируем со сжатием
            options.inJustDecodeBounds = false;
            options.inSampleSize = scale;
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
            
            if (bitmap == null) {
                Log.e(TAG, "Не удалось декодировать изображение: " + imagePath);
                return null;
            }
            
            // Сжимаем в JPEG
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, baos);
            byte[] bytes = baos.toByteArray();
            
            // Освобождаем память
            bitmap.recycle();
            
            return Base64.encodeToString(bytes, Base64.DEFAULT);
            
        } catch (Exception e) {
            Log.e(TAG, "Ошибка сжатия изображения", e);
            return null;
        }
    }
    

     //Получает длительность аудиофайла (в миллисекундах)

    public static long getAudioDuration(String audioPath) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(audioPath);
            
            String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            retriever.release();
            
            return duration != null ? Long.parseLong(duration) : 0;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка получения длительности аудио", e);
            return 0;
        }
    }
    

     //Определяет тип файла по расширению

    public static String getFileType(String fileName) {
        if (fileName == null) return "unknown";
        
        fileName = fileName.toLowerCase();
        
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || 
            fileName.endsWith(".png") || fileName.endsWith(".gif")) {
            return "image";
        } else if (fileName.endsWith(".mp3") || fileName.endsWith(".wav") || 
                  fileName.endsWith(".ogg") || fileName.endsWith(".m4a")) {
            return "audio";
        } else if (fileName.endsWith(".txt") || fileName.endsWith(".md")) {
            return "text";
        } else if (fileName.endsWith(".pdf")) {
            return "document";
        } else {
            return "file";
        }
    }
    

     //Получает имя файла из пути

    public static String getFileName(String filePath) {
        if (filePath == null) return "file";
        
        int lastSeparator = filePath.lastIndexOf(File.separator);
        return lastSeparator >= 0 ? filePath.substring(lastSeparator + 1) : filePath;
    }
    

     //Получает размер файла в читаемом формате

    public static String getReadableFileSize(long size) {
        if (size <= 0) return "0 B";
        
        final String[] units = {"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        
        return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }
}
