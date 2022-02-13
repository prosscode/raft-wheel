package wheel.core.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @Date 2022/2/13
 * @Author by shuang.peng
 * @Description RootDir
 */
public class RootDir {
    private static final Logger logger = LoggerFactory.getLogger(RootDir.class);
    static final String FILE_NAME_SNAPSHOT = "service.ss";
    static final String FILE_NAME_ENTRIES = "entries.bin";
    static final String FILE_NAME_ENTRY_OFFSET_INDEX = "entries.idx";
    private static final String DIR_NAME_GENERATING = "generating";
    private static final String DIR_NAME_INSTALLING = "installing";
    private final File baseDir;

    RootDir(File baseDir) {
        if (!baseDir.exists()) {
            throw new IllegalArgumentException("dir " + baseDir + " not exists");
        }
        this.baseDir = baseDir;
    }

    LogGeneration getLatestGeneration() {
        File[] files = baseDir.listFiles();
        if (files == null) {
            return null;
        }
        LogGeneration latest = null;
        String fileName;
        LogGeneration generation;
        for (File file : files) {
            if (!file.isDirectory()) {
                continue;
            }
            fileName = file.getName();
            if (DIR_NAME_GENERATING.equals(fileName) || DIR_NAME_INSTALLING.equals(fileName) ||
                    !LogGeneration.isValidDirName(fileName)) {
                continue;
            }
            generation = new LogGeneration(file);
            if (latest == null || generation.compareTo(latest) > 0) {
                latest = generation;
            }
        }
        return latest;
    }

    LogGeneration createFirstGeneration() {
        LogGeneration generation = new LogGeneration(baseDir, 0);
        generation.initialize();
        return generation;
    }
}
