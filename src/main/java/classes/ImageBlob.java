package classes;

import lombok.Getter;
import lombok.Setter;

public class ImageBlob {

    @Getter @Setter private String imageName;
    @Getter @Setter private byte[] content;
    @Getter @Setter private String owner;

    public ImageBlob (String imageName, byte[] content, String owner) {
        this.setImageName(imageName);
        this.setContent(content);
        this.setOwner(owner);
    }
}
