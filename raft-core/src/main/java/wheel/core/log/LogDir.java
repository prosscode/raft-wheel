package wheel.core.log;

import java.io.File;

/**
 * @Date 2022/2/10
 * @Author by shuang.peng
 * @Description LogDir
 */
public interface LogDir {
    // 初始化目录
    void initialize();
    // 是否存在
    boolean exists();

    File getSnapshotFile();
    // 获取EntriesFile对应的文件
    File getEntriesFile();
    // 获取EntryIndexFile对应的文件
    File getEntryOffsetIndexFile();
    // 获取目录
    File get();
    // 重命令目录
    boolean renameTo(LogDir logDir);

}
