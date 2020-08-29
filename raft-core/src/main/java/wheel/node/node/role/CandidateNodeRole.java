package wheel.node.node.role;

/**
 * @describe: candidate节点角色
 *      1。 被投票，决定变成follower或者leader
 * @author: 彭爽 pross.peng
 * @date: 2020/08/23
 */
public class CandidateNodeRole extends AbstractNodeRole{
    // 得到的票数
    private final int votesCount;
    // 选举超时
    private final ElectionTimeout electionTimeout;

    // 构造函数，得1票
    public CandidateNodeRole(int term, ElectionTimeout electionTimeout) {
        this(term,1,electionTimeout);
    }

    public CandidateNodeRole(int term, int votesCount, ElectionTimeout electionTimeout) {
        super(RoleName.CANDIDATE, term);
        this.votesCount = votesCount;
        this.electionTimeout = electionTimeout;
    }

    public int getVotesCount() {
        return votesCount;
    }


    @Override
    public void cancelTimeoutTask() {
        electionTimeout.cancel();
    }

    /**
     * 增加票数意味着需要重置选举超时（cancel）
     * @param electionTimeout
     * @return
     */
    public CandidateNodeRole increaseVotesCount(ElectionTimeout electionTimeout){
        this.electionTimeout.cancel();
        return new CandidateNodeRole(term,votesCount + 1, electionTimeout);

    }

    @Override
    public String toString() {
        return "CandidateNodeRole{" +
                "votesCount=" + votesCount +
                ", electionTimeout=" + electionTimeout +
                ", term=" + term +
                '}';
    }
}
