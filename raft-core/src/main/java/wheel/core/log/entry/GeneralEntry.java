package wheel.core.log.entry;

/**
 * @Date 2022/1/23
 * @Author shuang.peng
 * @Description GeneralEntry 普通日志条目的实现
 */
public class GeneralEntry extends AbstractEntry{

    private final byte[] commandBytes;

    public GeneralEntry(int index, int term, byte[] commandBytes) {
        super(Entry.KIND_GENERAL, index, term);
        this.commandBytes = commandBytes;
    }

    @Override
    public byte[] getCommandBytes() {
        return commandBytes;
    }

    @Override
    public String toString() {
        return "GeneralEntry{" +
                "index=" + index +
                ", term=" + term +
                '}';
    }
}
