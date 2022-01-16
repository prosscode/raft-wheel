package wheel.core.rpc.message;

/**
 * @describe: RequestVote响应类
 * @created by shuang.peng
 * @date: 2021/12/26
 */
public class RequestVoteResult {

    // 选举term
    private final int term;
    // 是否投票
    private final boolean voteGranted;

    public RequestVoteResult(int term, boolean voteGranted) {
        this.term = term;
        this.voteGranted = voteGranted;
    }

    public int getTerm() {
        return term;
    }

    public boolean isVoteGranted() {
        return voteGranted;
    }

    @Override
    public String toString() {
        return "RequestVoteResult{" +
                "term=" + term +
                ", voteGranted=" + voteGranted +
                '}';
    }
}
