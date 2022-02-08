package wheel.core.log.sequence;

import wheel.core.log.entry.Entry;
import wheel.core.log.entry.GeneralEntry;
import wheel.core.log.entry.NoOpEntry;

/**
 * @Date 2022/2/8
 * @Author by shuang.peng
 * @Description EntryFactory 实例化日志条目
 */
public class EntryFactory {

    public Entry create(int kind, int index, int term, byte[] commandBytes) {
        switch (kind) {
            case Entry.KIND_NO_OP:
                return new NoOpEntry(index, term);
            case Entry.KIND_GENERAL:
                return new GeneralEntry(index, term, commandBytes);
            default:
                throw new IllegalArgumentException("unexpected entry kind" + kind);
        }
    }

}
