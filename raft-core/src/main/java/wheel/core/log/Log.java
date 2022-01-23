package wheel.core.log;

import wheel.core.log.entry.Entry;
import wheel.core.log.entry.EntryMeta;
import wheel.core.log.entry.GeneralEntry;
import wheel.core.log.entry.NoOpEntry;
import wheel.core.node.NodeId;
import wheel.core.rpc.message.AppendEntriesRpc;

import java.util.List;

/**
 * @Date 2022/1/23
 * @Created by shuang.peng
 * @Description 日志接口
 */
public interface Log {
    int ALL_ENTRIES = -1;

    // 获取最后一条日志的元信息（发送消息时）
    EntryMeta getLastEntryMeta();

    // 创建AppendEntries消息（Leader向Follower发送日志复制消息）
    AppendEntriesRpc createAppendEntriesRpc(int term, NodeId selfId, int nextIndex, int maxEntries);

    // 获取下一条日志的索引（leader服务需要重置follower服务的日志复制进度，所有的follower服务的初始nextLogIndex都是当前服务的下一条日志索引）
    int getNextIndex();
    // 获取当前的commitIndex（）
    int getCommitIndex();

    // 判断对象的lastLogIndex和lastLogTerm是否比自己的新（收到request vote消息时，选择是否投票时使用）
    boolean isNewerThan(int lastLogIndex,int lastLogTerm);

    // 增加一条NO-OP日志 (上层服务操作或者当前节点成为leader后的第一条NO-OP日志)
    NoOpEntry appendEntry(int term);

    // 增加一条普通日志
    GeneralEntry appendEntry(int term,byte[] command);

    // 追加来自Leader的日志条目（收到来自Leader服务器的日志复制请求时）
    boolean appendEntriesFromLeader(int prevLogIndex, int prevLogTerm, List<Entry> entries);

    // 推进commitIndex（收到来自Leader服务器的日志复制请求时）
    void advanceCommitIndex(int newCommitIndex,int currentTerm);

    // 关闭日志组件
    void close();
}
