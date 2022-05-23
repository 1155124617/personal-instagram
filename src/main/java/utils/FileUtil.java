package utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class FileUtil {

    public static void saveFile(String uploadDir, String fileName, MultipartFile multipartFile) throws IOException {
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        InputStream inputStream = multipartFile.getInputStream();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
    }

    public static void saveFile(String uploadDir, String fileName, byte[] bytes) throws IOException {
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        InputStream inputStream = new ByteArrayInputStream(bytes);
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
    }

    public static void deleteDirectory(String deleteDir) throws Exception {
        File folder = new File(deleteDir);
        if (folder.exists()) {
            for (File image : folder.listFiles()) {
                if (!image.delete()) {
                    System.out.println("Image " + image.getName() + " deletion failed");
                    throw new Exception();
                }
            }
            if (!folder.delete()) {
                System.out.println("Folder " + folder.getName() + " deletion failed");
                throw new Exception();
            }
        }
    }

    public static void deleteFile(String deleteDir) throws Exception {
        File file = new File(deleteDir);
        if (file.exists()) {
            if (!file.delete()) {
                System.out.println("File " + file.getName() + " deletion failed");
                throw new Exception();
            }
        }
    }

    public static boolean exists(String fileDir) {
        File file = new File(fileDir);
        return file.exists();
    }
}
