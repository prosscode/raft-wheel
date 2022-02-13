package wheel.core.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wheel.core.log.entry.Entry;
import wheel.core.log.entry.EntryMeta;
import wheel.core.log.entry.GeneralEntry;
import wheel.core.log.entry.NoOpEntry;
import wheel.core.log.sequence.EmptySequenceException;
import wheel.core.log.sequence.EntrySequence;
import wheel.core.node.NodeId;
import wheel.core.rpc.message.AppendEntriesRpc;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @Date 2022/2/13
 * @Author by shuang.peng
 * @Description AbstractLog 日志实现
 */
public abstract class AbstractLog implements Log{

    private static final Logger logger = LoggerFactory.getLogger(AbstractLog.class);

    protected EntrySequence entrySequence;
    protected int commitIndex = 0;

    // 获取最后一条日志的元信息
    public EntryMeta getLastEntryMeta() {
        if (entrySequence.isEmpty()) {
            return new EntryMeta(Entry.KIND_NO_OP, 0, 0);
        }
        return entrySequence.getLastEntry().getMeta();
    }

    /**
     * 创建appendEntries消息
     * @param term term
     * @param selfId nodeId
     * @param nextIndex 下一个日志索引
     * @param maxEntries 最大读取的日志条数（raft算法中没有提及AppendEntries消息中日志条目的数量，假如传输全部的日志条目，可能会导致网络堵塞）
     * @return AppendEntriesRpc
     * @throws EmptySequenceException
     */
    public AppendEntriesRpc createAppendEntriesRpc(int term, NodeId selfId, int nextIndex, int maxEntries) throws EmptySequenceException {
        // 检查nextIndex
        int nextLogIndex = entrySequence.getNextLogIndex();
        if(nextIndex > nextLogIndex){
            throw new IllegalArgumentException("illegal next index " + nextIndex);
        }
        AppendEntriesRpc rpc = new AppendEntriesRpc();
        rpc.setTerm(term);
        rpc.setLeaderId(selfId);
        rpc.setLeaderCommit(commitIndex);
        // 设置前一条日志的元信息，有可能不存在
        Entry entry = entrySequence.getEntry(nextIndex - 1);
        if(entry!=null){
            rpc.setPrevLogIndex(entry.getIndex());
            rpc.setPrevLogTerm(entry.getTerm());
        }
        // 设置entries
        if(!entrySequence.isEmpty()){
            int maxIndex = (maxEntries == ALL_ENTRIES) ?
                    nextLogIndex : Math.min(nextLogIndex, nextIndex + maxEntries);
            rpc.setEntries(entrySequence.subList(nextIndex,maxIndex));
        }
        return rpc;
    }

    /**
     * 用于request vote中投票检查，取出最后一条日志条目的元信息，判断term和索引
     * @param lastLogIndex
     * @param lastLogTerm
     * @return
     */
    public boolean isNewerThan(int lastLogIndex, int lastLogTerm) {
        EntryMeta lastEntryMeta = getLastEntryMeta();
        logger.debug("last entry ({},{}),candidate ({},{})",
                lastEntryMeta.getIndex(), lastEntryMeta.getTerm(), lastLogIndex, lastLogTerm);
        return lastEntryMeta.getTerm() > lastLogTerm || lastEntryMeta.getIndex() > lastLogIndex;
    }

    /**
     * 追加日志条目的appendEntry方法
     * @param term term
     * @return
     */
    public NoOpEntry appendEntry(int term){
        NoOpEntry entry = new NoOpEntry(entrySequence.getNextLogIndex(),term);
        entrySequence.append(entry);
        return entry;
    }

    /**
     * 追加一般日志
     * @param term term
     * @param command 日志内容
     * @return
     */
     public GeneralEntry appendEntry(int term,byte[] command){
         GeneralEntry entry = new GeneralEntry(entrySequence.getNextLogIndex(), term, command);
         entrySequence.append(entry);
         return entry;
     }

    /**
     * 仅追加日志条目
     * 但追加之前需要移除不一致的的日志条目，和Leader日志对齐
     * @param prevLogIndex 前一条日志索引
     * @param prevLogTerm 前一条日志term
     * @param leaderEntries leader的日志条目
     * @return
     */
     public boolean appendEntriesFromLeader(int prevLogIndex, int prevLogTerm, List<Entry> leaderEntries) throws EmptySequenceException {
         // 检查前一条日志是否匹配
         if (!checkIfPreviousLogMatches(prevLogIndex, prevLogTerm)) {
             return false;
         }
         // leader节点传递过来的日志条目为空，可能是心跳消息，不需要进一步操作
         if(leaderEntries.isEmpty()){
             return true;
         }
         // 移除冲突的日志条目并返回接下来要追加的日志条目
         EntrySequenceView newEntries = removeUnmatchedLog(new EntrySequenceView(leaderEntries));
         // 追加日志
         appendEntriesFromLeader(newEntries);
         return true;
     }

    /**
     * 推进commitIndex
     * @param newCommitIndex 新的commitIndex
     * @param currentTerm 当前term
     */
     public void advanceCommitIndex(int newCommitIndex,int currentTerm){
         if (!validateNewCommitIndex(newCommitIndex,currentTerm)) {
             return;
         }
         logger.debug("advance commit index from {} to {}", commitIndex, newCommitIndex);
         entrySequence.commit(newCommitIndex);
     }

     // 检查新的commitIndex
    private boolean validateNewCommitIndex(int newCommitIndex, int currentTerm){
         // 小于当前的commitIndex
        if(newCommitIndex <= entrySequence.getCommitIndex()){
            return false;
        }
        EntryMeta meta = entrySequence.getEntryMeta(newCommitIndex);
        if(meta == null){
            logger.debug("log of new commit index {} not found",newCommitIndex);
            return false;
        }
        // 日志条目的term必须是当前term，才能推进commitIndex
        if (meta.getTerm() != currentTerm) {
            logger.debug("log term of new commit index != current term ({} != {})", meta.getTerm(), currentTerm);
            return false;
        }
        return true;
    }


    // 移除操作完成后，leaderEntries中的一部分或者全部日志将按照要求被追加到本地日志中
    private void appendEntriesFromLeader(EntrySequenceView leaderEntries){
         if(leaderEntries.isEmpty()){
             return;
         }
         logger.debug("append entries from leader from {} to {}",leaderEntries.getFirstLogIndex(),leaderEntries.getLastLogIndex());
        for (Entry leaderEntry : leaderEntries) {
            entrySequence.append(leaderEntry);
        }
    }

     // prevLogIndex不一定对应最后一条日志，Leader节点会从后往前找到第一个匹配的日志
    private boolean checkIfPreviousLogMatches(int prevLogIndex, int prevLogTerm){
         // 检查指定索引的日志条目
         EntryMeta meta = entrySequence.getEntryMeta(prevLogIndex);
         // 日志不存在
         if(meta == null){
             logger.debug("previous log {} not found",prevLogIndex);
             return false;
         }
         int term = meta.getTerm();
         if (term != prevLogTerm) {
             logger.debug("different term of previous log, local {},remote {}", term, prevLogTerm);
             return false;
         }
         return true;
     }

     // 移除不一致的操作removeUnmatchedLog，先找到第一个不一致的日志条目，然后按移除之后的日志条目方式执行
     private EntrySequenceView removeUnmatchedLog(EntrySequenceView leaderEntries) throws EmptySequenceException {
         // leader节点过来的entries不应该为空
        assert !leaderEntries.isEmpty();
        // 找到第一个不匹配的日志索引
        int firstUnmatched = findFirstUnmatchedLog(leaderEntries);
        // 没有不匹配的日志
        if(firstUnmatched < 0){
            return new EntrySequenceView(Collections.emptyList());
        }
        // 移除不匹配的日志索引开始的所有日志
        removeEntriesAfter(firstUnmatched - 1);
        // 返回之后追加的日志条目
        return leaderEntries.subView(firstUnmatched);
    }

    // 查找第一条不匹配的日志
    private int findFirstUnmatchedLog(EntrySequenceView leaderEntries){
         int logIndex;
         EntryMeta followerEntryMeta;
         // 从前往后遍历leaderEntries
        for (Entry leaderEntry : leaderEntries) {
            logIndex = leaderEntry.getIndex();
            // 按照索引查找日志条目元信息
            followerEntryMeta = entrySequence.getEntryMeta(logIndex);
            // 日志不存在或者term不一致
            if(followerEntryMeta == null || followerEntryMeta.getTerm() != leaderEntry.getTerm()){
                return logIndex;
            }
        }
        // 否则没有不一致的日志条目
        return -1;
    }

    // 移除最后一个匹配的日志之后的所有日志
    private void removeEntriesAfter(int index) throws EmptySequenceException {
         if (entrySequence.isEmpty() || index >= entrySequence.getLastLogIndex()){
             return;
         }
         // 如果移除了已经应用的日志，需要从头开始重新构建状态机
        logger.debug("remove entries after {}",index);
         entrySequence.removeAfter(index);
    }


    /**
     * EntrySequenceView用来替代直接操作Entry数组，直接传入entries操作
     * 提供了按照日志索引获取，根据子视图检查等功能
     */
    private static class EntrySequenceView implements Iterable<Entry> {
        private final List<Entry> entries;
        private int firstLogIndex;
        private int lastLogIndex;

        EntrySequenceView(List<Entry> entries) {
            this.entries = entries;
            if (!entries.isEmpty()) {
                firstLogIndex = entries.get(0).getIndex();
                lastLogIndex = entries.get(entries.size() - 1).getIndex();
            }
        }

        Entry get(int index) {
            if (entries.isEmpty() || index < firstLogIndex || index > lastLogIndex) {
                return null;
            }
            return entries.get(index - firstLogIndex);
        }

        boolean isEmpty() {
            return entries.isEmpty();
        }

        int getFirstLogIndex() {
            return firstLogIndex;
        }

        int getLastLogIndex() {
            return lastLogIndex;
        }

        EntrySequenceView subView(int fromIndex) {
            if (entries.isEmpty() || fromIndex > lastLogIndex) {
                return new EntrySequenceView(Collections.emptyList());
            }
            return new EntrySequenceView(
                    entries.subList(fromIndex - firstLogIndex, entries.size())
            );
        }

        @Override
        @Nonnull
        public Iterator<Entry> iterator() {
            return entries.iterator();
        }
    }

}
