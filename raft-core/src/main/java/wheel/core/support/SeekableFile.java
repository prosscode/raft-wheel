package wheel.core.support;

import java.io.IOException;
import java.io.InputStream;

/**
 * @Date 2022/1/2
 * @Author shuang.peng
 * @Description SeekableFile 包装对文件的一系列操作
 */
public interface SeekableFile {

    // 获取当前位置
    long position() throws IOException;

    // 移动到指定位置
    void seek(long position) throws IOException;

    // 写入整数
    void writeInt(int i) throws IOException;

    // 写入长整数
    void writeLong(long l) throws IOException;

    // 写入字节数组
    void write(byte[] b) throws IOException;

    // 读取整数
    int readInt() throws IOException;

    // 读取长整数
    long readLong() throws IOException;

    // 读取字节数组
    int read(byte[] b) throws IOException;

    // 获取文件大小
    long size() throws IOException;

    // 截取到指定大小
    void truncate(long size) throws IOException;

    // 获取从指定位置开始的输入流
    InputStream inputStream(long start) throws IOException;

    // 强制输出到磁盘
    void flush() throws IOException;

    // 关闭文件
    void close() throws IOException;
}
