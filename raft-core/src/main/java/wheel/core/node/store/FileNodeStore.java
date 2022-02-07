package wheel.core.node.store;

import wheel.core.node.NodeId;
import wheel.core.support.Files;
import wheel.core.support.RandomAccessFileAdapter;
import wheel.core.support.SeekableFile;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @Date 2022/1/2
 * @Author shuang.peng
 * @Description FileNodeStore
 * 存储结构组成：
 * 4字节，current term
 * 4字节，votedFor长度
 * 可变长，votedFor内容
 */
@NotThreadSafe
public class FileNodeStore implements NodeStore {
    private static final String FILE_NAME = "node.bin";
    private static final long OFFSET_TERM = 0;
    private static final long OFFSET_VOTED_FOR = 4;
    private SeekableFile seekableFile;
    private int term = 0;
    private NodeId votedFor;

    public FileNodeStore(File file) {
        try {
            // 如果文件不存在，则创建新文件
            if (!file.exists()) {
                Files.touch(file);
            }
            // 拥有对文件操作权限
            seekableFile = new RandomAccessFileAdapter(file);
            initializeOrLoad();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // 模拟从文件中读取
    public FileNodeStore(SeekableFile seekableFile) {
        try {
            this.seekableFile = seekableFile;
            initializeOrLoad();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 初始化
    private void initializeOrLoad() throws IOException {
        if (seekableFile.size() == 0) {
            // 对文件初始化操作 term + votedFor = 8 bytes
            seekableFile.truncate(8L);
            seekableFile.seek(0);
            // term
            seekableFile.writeInt(0);
            // votedFor
            seekableFile.writeInt(0);
        } else {
            // read term
            term = seekableFile.readInt();
            // read votedFor
            int votedForLength = seekableFile.readInt();
            if (votedForLength > 0) {
                byte[] bytes = new byte[votedForLength];
                seekableFile.read(bytes);
                votedFor = new NodeId(new String(bytes, StandardCharsets.UTF_8));
            }
        }

    }

    @Override
    public int getTerm() {
        return term;
    }

    @Override
    public void setTerm(int term) {
        try {
            seekableFile.seek(OFFSET_TERM);
            seekableFile.writeInt(term);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.term = term;
    }

    @Override
    public NodeId getVotedFor() {
        return votedFor;
    }

    @Override
    public void setVotedFor(NodeId votedFor) {
        try {
            seekableFile.seek(OFFSET_VOTED_FOR);
            if(votedFor == null){
                // 如果votedFor为空
                seekableFile.writeInt(0);
                seekableFile.truncate(8L);
            }else{
                byte[] bytes = votedFor.getValue().getBytes(StandardCharsets.UTF_8);
                seekableFile.writeInt(bytes.length);
                seekableFile.write(bytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.votedFor = votedFor;
    }

    @Override
    public void close() {
        try {
            seekableFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
