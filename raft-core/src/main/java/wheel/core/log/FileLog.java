package wheel.core.log;

import wheel.core.log.sequence.FileEntrySequence;

import java.io.File;

/**
 * @Date 2022/2/13
 * @Author by shuang.peng
 * @Description FileLog
 * 日志根目录结构如下：
 * log-root
 * ├── log-1
 *      ├── entries.bin
 *      └── entries.idx
 * └── log-100
 *     ├── entries.bin
 *     └── entries.idx
 */
public class FileLog extends AbstractLog{

    private final RootDir rootDir;
    private final FileEntrySequence entrySequence;

    public FileLog(File baseDir) {
        this.rootDir = new RootDir(baseDir);
        // 获取最新的日志
        LogGeneration latestGeneration = rootDir.getLatestGeneration();
        if (latestGeneration != null) {
            // 日志存在
            // todo fix latestGeneration.getLogIndexOffset()
            entrySequence = new FileEntrySequence(latestGeneration, latestGeneration.getLastIncludedIndex());
        } else {
            // 日志不存在
            LogGeneration firstGeneration = rootDir.createFirstGeneration();
            entrySequence = new FileEntrySequence(firstGeneration, 1);
        }

    }

    @Override
    public int getNextIndex() {
        return 0;
    }

    @Override
    public int getCommitIndex() {
        return 0;
    }

    @Override
    public void close() {

    }
}
