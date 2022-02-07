package wheel.core.log.entry;

/**
 * @Date 2022/1/23
 * @Author shuang.peng
 * @Description 日志条目
 */
public interface Entry {
    // 日志类型
    int KIND_NO_OP = 0;
    int KIND_GENERAL = 1;
    // 节点操作
    int KIND_ADD_NODE = 3;
    int KIND_REMOVE_NODE = 4;

    // 获取日志类型
    int getKind();
    // 获取索引
    int getIndex();
    // 获取term
    int getTerm();
    // 获取元信息（kind，index，term）
    EntryMeta getMeta();
    // 获取日志负载
    byte[] getCommandBytes();

}
