package wheel.core.node.role;

import wheel.core.node.NodeId;

/**
 * @describe: 角色和状态数据公共类
 *      抽取公共的字段作为父类数据，方便为每一个角色建模。
 * @created by shuang.peng
 * @date: 2020/08/23
 */
public abstract class AbstractNodeRole {

    /**
     * 角色字段的不可变（final）：
     * follower选举超时或者接受到来自leader节点服务器的心跳时，必须新建一个角色实例
     * 保证并发环境下的数据安全
     */
    private final RoleName name;
    protected final int term;

    public AbstractNodeRole(RoleName name, int term) {
        this.name = name;
        this.term = term;
    }

    // 取消超时或者定时任务
    public abstract void cancelTimeoutOrTask();

    // 获取角色名
    public RoleName getName() {
        return name;
    }

    // 获取当前节点上的term
    public int getTerm() {
        return term;
    }

    public abstract NodeId getLeaderId(NodeId selfId);

}
