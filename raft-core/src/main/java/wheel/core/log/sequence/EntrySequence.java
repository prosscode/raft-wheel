package wheel.core.log.sequence;

import wheel.core.log.entry.Entry;
import wheel.core.log.entry.EntryMeta;

import java.util.List;

/**
 * @Date 2022/1/23
 * @Author shuang.peng
 * @Description EntrySequence 日志序列接口
 * 日志序列接口是为了当实现Raft算法中的日志快照时，可能需要对Log实现进行修改
 * 提取了序列化部分提前做好了日志组件的拆分，方便更快的加入日志快照功能
 */
public interface EntrySequence {
    // 判断是否为空
    boolean isEmpty();

    // 获取第一条日志索引
    int getFirstLogIndex() throws EmptySequenceException;
    // 获取最后一条日志索引
    int getLastLogIndex() throws EmptySequenceException;
    // 获取下一条日志索引
    int getNextLogIndex();

    /**
     * subList主要用于构造AppendEntries消息时获取指定区间的日志条目
     * @return List<Entry>
     */
    // 获取序列的子视图，到最后一条日志
    List<Entry> subList(int fromIndex) throws EmptySequenceException;
    // 获取序列的子视图，指定范围，不包括toIndex所指向的日志
    List<Entry> subList(int fromIndex, int toIndex) throws EmptySequenceException;

    // 检查某个日志条目是否存在
    boolean isEntryPresent(int index);
    // 获取某个日志条目的元信息
    EntryMeta getEntryMeta(int index);
    // 获取某个日志条目
    Entry getEntry(int index);
    // 获取最后一条日志条目
    Entry getLastEntry();
    // 增加日志
    void append(Entry entry);
    // 增加多条日志
    void append(List<Entry> entries);

    // 推进commitIndex
    void commit(int index);
    // 获取当前commitIndex
    int getCommitIndex();

    /**
     * 移除某个索引之后的日志条目
     * removeAfter主要用于追加来自Leader节点的日志时出现日志冲突的情况，移除现有日志
     * @param index index
     */
    void removeAfter(int index);

    // 关闭日志序列
    void close();
}
