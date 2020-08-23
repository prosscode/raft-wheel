package wheel.node.role;

import wheel.node.RoleName;

/**
 * @describe: 角色和状态数据公共类
 *      抽取公共的字段作为父类数据，方便为每一个角色建模。
 * @author: 彭爽 pross.peng
 * @date: 2020/08/23
 */
public abstract class AbstractNodeRole {

    private final RoleName name;
    protected final int term;

    public AbstractNodeRole(RoleName name, int term) {
        this.name = name;
        this.term = term;
    }

    // 取消超时或者定时任务
    public abstract void cancelTimeoutTask();

    // 获取角色名
    public RoleName getName() {
        return name;
    }

    // 获取当前节点上的term
    public int getTerm() {
        return term;
    }

}
