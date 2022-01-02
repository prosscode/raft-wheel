package wheel.core.support;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

/**
 * @Date 2022/1/2
 * @Created by shuang.peng
 * @Description RandomAccessFileAdapter
 */
public class RandomAccessFileAdapter implements SeekableFile{
    private final File file;
    private RandomAccessFile randomAccessFile;

    public RandomAccessFileAdapter(File file) throws FileNotFoundException {
        this(file,"rw");
    }

    public RandomAccessFileAdapter(File file,String mode) throws FileNotFoundException {
        this.file = file;
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, mode);
    }
}
