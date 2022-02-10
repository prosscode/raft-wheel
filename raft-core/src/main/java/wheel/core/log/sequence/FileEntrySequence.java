package wheel.core.log.sequence;

import wheel.core.log.LogDir;
import wheel.core.log.LogException;
import wheel.core.log.entry.Entry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @Date 2022/1/30
 * @Author shuang.peng
 * @Description EntriesFile、EntryIndexFile和pendingEntries构成FileEntrySequence,用于获取日志条目
 */
public class FileEntrySequence extends AbstractEntrySequence{
    private final EntryFactory entryFactory = new EntryFactory();
    private final EntriesFile entriesFile;
    private final EntryIndexFile entryIndexFile;
    private final LinkedList<Entry> pendingEntries = new LinkedList<>();
    // raft算法中定义初始commitIndex为0，和日志是否持久化无关
    private int commitIndex = 0;

    // 构造函数，指定目录
    public FileEntrySequence(LogDir logDir, int logIndexOffset) {
        // 默认logIndexOffset由外部决定
        super(logIndexOffset);
        try {
            this.entriesFile = new EntriesFile(logDir.getEntriesFile());
            this.entryIndexFile = new EntryIndexFile(logDir.getEntryOffsetIndexFile());
            initialize();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new LogException("failed to open entries file or entry index file", e);
        }
    }

    // 构造函数，指定文件
    public FileEntrySequence(EntriesFile entriesFile, EntryIndexFile entryIndexFile,int logIndexOffset) {
        super(logIndexOffset);
        this.entriesFile = entriesFile;
        this.entryIndexFile = entryIndexFile;
        initialize();
    }

    // 初始化
    private void initialize() {
        if (entryIndexFile.isEmpty()) {
            return;
        }
        // 使用日志索引文件的minEntryIndex作为logIndexOffset
        logIndexOffset = entryIndexFile.getMinEntryIndex();
        // 使用日志索引文件的maxEntryIndex+1作为nexLogOffset
        nextLogIndex = entryIndexFile.getMaxEntryIndex() + 1;
    }



    // 获取commitIndex
    public int getCommitIndex(){
        return commitIndex;
    }

    @Override
    protected List<Entry> doSubList(int fromIndex, int toIndex) {
        // 结果分别来自文件的和来自缓冲两部分
        List<Entry> result = new ArrayList<>();
        // 从文件中获取日志条目
        if (!entryIndexFile.isEmpty() && fromIndex <= entryIndexFile.getMaxEntryIndex()) {
            int maxIndex = Math.min(entryIndexFile.getMaxEntryIndex() + 1, toIndex);
            for (int i = fromIndex; i < maxIndex; i++) {
                result.add(getEntryInFile(i));
            }
        }

        // 从日志缓冲中获取日志条目
        if(!pendingEntries.isEmpty() && toIndex > pendingEntries.getFirst().getIndex()){
            Iterator<Entry> iterator = pendingEntries.iterator();
            Entry entry;
            int index;
            while (iterator.hasNext()){
                entry = iterator.next();
                index = entry.getIndex();
                if(index >= toIndex){
                    break;
                }
                if(index >= fromIndex){
                    result.add(entry);
                }
            }
        }
        return result;
    }

    // 按照索引获取文件中的日志条目
    private Entry getEntryInFile(int index){
        return null;
    }

    // 获取指定位置的日志条目
    @Override
    protected Entry doGetEntry(int index) {
        return null;
    }


    @Override
    protected void doAppend(Entry entry) {

    }

    @Override
    protected void doMoveAfter(int index) {

    }

    @Override
    public void commit(int index) {

    }

    @Override
    public void close() {

    }
}
