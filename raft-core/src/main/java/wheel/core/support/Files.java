package wheel.core.support;

import java.io.File;
import java.io.IOException;

/**
 * @Date 2022/1/2
 * @Created by shuang.peng
 * @Description Files
 */
public class Files {

    public static void touch(File file) throws IOException {
        if (!file.createNewFile() && !file.setLastModified(System.currentTimeMillis())) {
            throw new IOException("failed to touch file " + file);
        }
    }

}
