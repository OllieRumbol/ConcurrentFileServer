import java.util.concurrent.Semaphore;

/**
 * FileInformation
 */
public class FileInformation {
    public FileFrame fileFrame;
    public Semaphore lock;

    public FileInformation(FileFrame fileFrame){
        this.fileFrame = fileFrame;
        this.lock = new Semaphore(Integer.MAX_VALUE, true);
    }

    public FileInformation(FileFrame fileFrame, Semaphore lock){
        this.fileFrame = fileFrame;
        this.lock = lock;
    }    
}