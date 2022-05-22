package utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilTest {

    @Test
    void deleteDirectory() throws Exception {
        FileUtil.deleteDirectory("user-images/1234");
    }
}