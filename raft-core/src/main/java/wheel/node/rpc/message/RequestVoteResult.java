package wheel.node.rpc.message;

/**
 * @describe: RequestVote响应类
 * @author: 彭爽 pross.peng
 * @date: 2020/09/12
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
