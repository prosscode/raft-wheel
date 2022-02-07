package wheel.core.log.sequence;

import wheel.core.log.entry.Entry;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @Date 2022/1/30
 * @Author shuang.peng
 * @Description MemoryEntrySequence
 * 基于内存的日志条目序列，使用ArrayList<Entry>作为操作对象，需要随机访问日志序列。
 * （链表LinkedList的随机访问性能访问比较低）
 */
public class MemoryEntrySequence extends AbstractEntrySequence {

    private final List<Entry> entries = new ArrayList<>();
    private int commitIndex = 0;

    /**
     * 初始情况下，日志索引偏移量 = 下一条日志的索引 = 1
     */
    public MemoryEntrySequence() {
        this(1);
    }

    public MemoryEntrySequence(int logIndexOffset) {
        super(logIndexOffset);
    }

    // 按照索引获取日志条目
    @Override
    protected Entry doGetEntry(int index) {
        return entries.get(index - logIndexOffset);
    }

    // 获取子视图
    @Override
    protected List<Entry> doSubList(int fromIndex, int toIndex) {
        return entries.subList(fromIndex - logIndexOffset, toIndex - logIndexOffset);
    }

    // 追加日志条目
    @Override
    protected void doAppend(Entry entry) {
        entries.add(entry);
    }

    // 移除指定的索引后的日志条目
    @Override
    protected void doMoveAfter(int index) {
        if(index < doGetFirstLogIndex()){
            entries.clear();
            nextLogIndex = logIndexOffset;
        } else {
            entries.subList(index - logIndexOffset + 1, entries.size()).clear();
            nextLogIndex = index + 1;
        }
    }

    // 提交，检验由外层处理完成，到这里直接提交
    @Override
    public void commit(int index) {
        commitIndex = index;
    }

    // 获取提交索引
    @Override
    public int getCommitIndex() {
        return commitIndex;
    }

    @Override
    public void close() {
        // nothing for memory operator
    }

    @Override
    public String toString() {
        return "MemoryEntrySequence{" +
                "logIndexOffset=" + logIndexOffset +
                ", nextLogIndex=" + nextLogIndex +
                ", entries.size=" + entries.size() +
                ", commitIndex=" + commitIndex +
                '}';
    }
}
