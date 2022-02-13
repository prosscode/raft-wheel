package wheel.core.log;

import wheel.core.log.sequence.EntrySequence;
import wheel.core.log.sequence.MemoryEntrySequence;

/**
 * @Date 2022/2/13
 * @Author by shuang.peng
 * @Description MemoryLog 基于内存实现Log
 * 基于内存的日志在重启后数据会丢失，系统日志为空，logIndexOffset和nexLogIndex默认都为1
 */
public class MemoryLog extends AbstractLog {

    // 无参构造
    public MemoryLog() {
        this(new MemoryEntrySequence());
    }

    // 方便测试
    MemoryLog(EntrySequence entrySequence) {
        this.entrySequence = entrySequence;
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
