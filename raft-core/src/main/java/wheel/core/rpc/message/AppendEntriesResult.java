package wheel.core.rpc.message;

/**
 * @describe:
 * @created by shuang.peng
 * @date: 2020/09/12
 */
public class AppendEntriesResult {
    private final int term;
    // 是否追加成功
    private final boolean success;

    public AppendEntriesResult(int term, boolean success) {
        this.term = term;
        this.success = success;
    }

    public int getTerm() {
        return term;
    }

    public boolean isSuccess() {
        return success;
    }

    @Override
    public String toString() {
        return "AppendEntriesResult{" +
                "term=" + term +
                ", success=" + success +
                '}';
    }
}
