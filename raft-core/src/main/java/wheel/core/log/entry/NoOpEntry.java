package wheel.core.log.entry;

/**
 * @Date 2022/1/23
 * @Author shuang.peng
 * @Description NoOpEntry
 */
public class NoOpEntry extends AbstractEntry {

    public NoOpEntry(int kind, int index, int term) {
        super(KIND_NO_OP, index, term);
    }

    // no_op_entry, no data
    public byte[] getCommandBytes(){
        return new byte[0];
    }

    @Override
    public String toString() {
        return "NoOpEntry{" +
                "index=" + index +
                ", term=" + term +
                '}';
    }
}
