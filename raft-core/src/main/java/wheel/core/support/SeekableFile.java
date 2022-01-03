package wheel.core.support;

import java.io.IOException;
import java.io.InputStream;

/**
 * @Date 2022/1/2
 * @Created by shuang.peng
 * @Description SeekableFile 包装对文件对一系列操作
 */
public interface SeekableFile {

    long position() throws IOException;

    void seek(long position) throws IOException;

    void writeInt(int i) throws IOException;

    void writeLong(long l) throws IOException;

    void write(byte[] b) throws IOException;

    int readInt() throws IOException;

    long readLong() throws IOException;

    int read(byte[] b) throws IOException;

    long size() throws IOException;

    void truncate(long size) throws IOException;

    InputStream inputStream(long start) throws IOException;

    void flush() throws IOException;

    void close() throws IOException;
}
