package wheel.core.log.sequence;

import wheel.core.support.RandomAccessFileAdapter;
import wheel.core.support.SeekableFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @Date 2022/2/8
 * @Author by shuang.peng
 * @Description 快速访问依赖日志条目索引文件EntryIndexFile
 *
 * EntryIndexFile文件结构：
 * -> [ minEntryIndex起始索引(int) + maxEntryIndex结束索引(int) ]
 * -> offset位置偏移(long) + 日志类型kind(int) + 日志term(int)
 */
public class EntryIndexFile implements Iterable<EntryIndexItem>{

    private final SeekableFile seekableFile;
    private Map<Integer,EntryIndexItem> entryIndexMap = new HashMap<>();
    // 日志条目数
    private int entryIndexCount;
    // 最小日志索引
    private int minEntryIndex;
    // 最大日志索引
    private int maxEntryIndex;
    // 最大条目索引的偏移
    private static final long OFFSET_MAX_ENTRY_INDEX = Integer.BYTES;
    // 单条日志条目元信息的长度,
    private static final int LENGTH_ENTRY_INDEX_ITEM = 16;

    public EntryIndexFile(File file) throws IOException {
        this(new RandomAccessFileAdapter(file));
    }

    public EntryIndexFile(SeekableFile seekableFile) throws IOException {
        this.seekableFile = seekableFile;
        load();
    }

    // 加载所有日志元信息
    private void load() throws IOException {
        if(seekableFile.size() == 0L){
            entryIndexCount = 0;
            return;
        }
        minEntryIndex = seekableFile.readInt();
        maxEntryIndex = seekableFile.readInt();
        updateEntryIndexCount();
        // 逐条加载
        long offset;
        int kind;
        int term;
        for (int i = minEntryIndex; i <= maxEntryIndex; i++) {
            offset = seekableFile.readLong();
            kind = seekableFile.readInt();
            term = seekableFile.readInt();
            entryIndexMap.put(i,new EntryIndexItem(i,offset,kind,term));
        }
    }

    // 更新日志条目数量
    private void updateEntryIndexCount(){
        entryIndexCount = maxEntryIndex - minEntryIndex + 1;
    }

    @Override
    public Iterator<EntryIndexItem> iterator() {
        return null;
    }
}
