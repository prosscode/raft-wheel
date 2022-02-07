package wheel.core.log.sequence;

import wheel.core.log.entry.Entry;
import wheel.core.log.entry.EntryMeta;

import java.util.Collections;
import java.util.List;

/**
 * @Date 2022/1/30
 * @Author shuang.peng
 * @Description AbstractEntrySequence抽象大部分和存储无关的方法
 * logIndexOffset：日志索引偏移量
 * nextLogIndex：下一条日志的索引
 */
public abstract class AbstractEntrySequence implements EntrySequence {
    int logIndexOffset;
    int nextLogIndex;

    /**
     * 初始情况下，日志索引偏移量 = 下一条日志的索引 = 1
     */
    public AbstractEntrySequence(int logIndexOffset) {
        this.logIndexOffset = logIndexOffset;
        this.nextLogIndex = logIndexOffset;
    }

    // 判断是否为空
    @Override
    public boolean isEmpty() {
        return logIndexOffset == nextLogIndex;
    }

    // 获取第一条日志的索引，为空的话报错
    @Override
    public int getFirstLogIndex() throws EmptySequenceException {
        if(isEmpty()){
            throw new EmptySequenceException();
        }
        return doGetFirstLogIndex();
    }

    // 判断日志条目是否存在
    @Override
    public boolean isEntryPresent(int index) {
        return !isEmpty() && index >= doGetFirstLogIndex() && index <= doGetLastLogIndex();
    }

    // 获取日志索引偏移
    public int doGetFirstLogIndex() {
        return logIndexOffset;
    }

    // 获取最后一条日志的索引，为空的话报错
    @Override
    public int getLastLogIndex() throws EmptySequenceException {
        if(isEmpty()){
            throw new EmptySequenceException();
        }
        return doGetLastLogIndex();
    }

    // 获取下一条日志的索引
    @Override
    public int getNextLogIndex() {
        return nextLogIndex;
    }

    // 获取指定索引的日志条目
    @Override
    public Entry getEntry(int index){
        if(!isEntryPresent(index)){
            return null;
        }
        return doGetEntry(index);
    }

    // 获取最后一条日志条目
    @Override
    public Entry getLastEntry() {
        return isEmpty() ? null : doGetEntry(doGetLastLogIndex());
    }

    // 获取最后一条日志的索引
    private int doGetLastLogIndex() {
        return nextLogIndex - 1;
    }

    // 获取指定索引的日志条目的元信息
    @Override
    public EntryMeta getEntryMeta(int index) {
        Entry entry = getEntry(index);
        return entry != null ? entry.getMeta() : null;
    }

    // 获取指定索引的日志条目，抽象方法，涉及到从存储中读取日志条目的逻辑，交给子类来处理
    protected abstract Entry doGetEntry(int index);


    // 获取一个子视图，不指定结束索引
    @Override
    public List<Entry> subList(int fromIndex) throws EmptySequenceException {
        if (isEmpty() || fromIndex > doGetLastLogIndex()) {
            return Collections.emptyList();
        }
        return subList(Math.max(fromIndex, doGetFirstLogIndex()), nextLogIndex);
    }

    // 获取一个子视图，指定结束的索引
    @Override
    public List<Entry> subList(int fromIndex, int toIndex) throws EmptySequenceException {
        if (isEmpty()) {
            throw new EmptySequenceException();
        }
        // 检查索引
        if (fromIndex < doGetFirstLogIndex()
                || toIndex > doGetLastLogIndex() + 1
                || fromIndex > toIndex) {
            throw new IllegalArgumentException("illegal from index" + fromIndex + " or to index " + toIndex);
        }
        return null;
    }

    // 获取一个子视图，抽象方法，需要访问存储中的日志条目
    protected abstract List<Entry> doSubList(int fromIndex, int toIndex);

    // 追加日志条目
    @Override
    public void append(List<Entry> entries) {
        for (Entry entry : entries) {
            append(entry);
        }
    }

    @Override
    public void append(Entry entry) {
        // 保证新日志的索引是当前序列的下一条日志索引
        if (entry.getIndex() != nextLogIndex) {
            throw new IllegalArgumentException("entry index must be " + nextLogIndex);
        }
        doAppend(entry);

        /**
         * 考虑点：日志索引是类似数据库表的自增列一样由序列自定管理还是独立于序列每次都生成。
         * 需要保证数据正确性。
         */
        // 递增序列的日志索引
        nextLogIndex ++;
    }
    // 追加日志，抽象方法
    protected abstract void doAppend(Entry entry);

    // 移除指定索引后的日志条目
    @Override
    public void removeAfter(int index) {
        if(isEmpty() || index >= doGetLastLogIndex()){
            return;
        }
        doMoveAfter(index);

    }
    // 抽象方法
    protected abstract void doMoveAfter(int index);
}
