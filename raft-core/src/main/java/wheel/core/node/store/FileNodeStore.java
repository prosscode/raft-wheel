package wheel.core.node.store;

import wheel.core.node.NodeId;
import wheel.core.support.Files;
import wheel.core.support.RandomAccessFileAdapter;
import wheel.core.support.SeekableFile;

import java.io.File;
import java.io.IOException;

/**
 * @Date 2022/1/2
 * @Created by shuang.peng
 * @Description FileNodeStore
 * 存储结构组成：
 * 4字节，current term
 * 4字节，votedFor长度
 * 可变长，votedFor内容
 */
public class FileNodeStore implements NodeStore {
    private static final String FILE_NAME = "node.bin";
    private static final long OFFSET_TERM = 0;
    private static final long OFFSET_VOTED_FOR = 4;
    private SeekableFile seekableFile;
    private int term = 0;
    private NodeId votedFor;

    public FileNodeStore(File file) {
        try {
            if (!file.exists()) {
                Files.touch(file);
            }
            seekableFile = new RandomAccessFileAdapter(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // 模拟从文件中读取
    public FileNodeStore(SeekableFile seekableFile) {
        this.seekableFile = seekableFile;
        initializeOrLoad();
    }

    // 初始化
    private void initializeOrLoad(){

    }

    @Override
    public int getTerm() {
        return 0;
    }

    @Override
    public void setTerm(int term) {

    }

    @Override
    public NodeId getVotedFor() {
        return null;
    }

    @Override
    public void setVotedFor(NodeId votedFor) {

    }

    @Override
    public void close() {

    }
}
