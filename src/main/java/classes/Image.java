package classes;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;

public class Image {

    @Getter @Setter private MultipartFile multipartFile;
    @Getter @Setter private String fileName;
    @Getter @Setter private String owner;
    @Getter @Setter private Timestamp uploadTime;

    public Image(MultipartFile multipartFile, String fileName, String owner, Timestamp uploadTime) {
        this.setMultipartFile(multipartFile);
        this.setFileName(fileName);
        this.setOwner(owner);
        this.setUploadTime(uploadTime);
    }

    public Image(String fileName, String owner, Timestamp uploadTime) {
        this.setFileName(fileName);
        this.setOwner(owner);
        this.setUploadTime(uploadTime);
    }
}
