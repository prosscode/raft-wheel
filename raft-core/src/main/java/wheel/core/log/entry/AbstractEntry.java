package wheel.core.log.entry;

/**
 * @Date 2022/1/23
 * @Created by shuang.peng
 * @Description AbstractEntry
 */
public abstract class AbstractEntry implements Entry{
    private final int kind;
    protected final int index;
    protected final int term;

    public AbstractEntry(int kind, int index, int term) {
        this.kind = kind;
        this.index = index;
        this.term = term;
    }

    @Override
    public int getKind() {
        return this.kind;
    }

    @Override
    public int getIndex() {
        return this.index;
    }

    @Override
    public int getTerm() {
        return this.index;
    }

    @Override
    public EntryMeta getMeta() {
        return new EntryMeta(kind,index,term);
    }

    @Override
    public byte[] getCommandBytes() {
        return new byte[0];
    }
}
