import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

/**
 * MyFileServer
 */
public class MyFileServer implements FileServer {

    HashMap<String, FileInformation> files;

    public MyFileServer() {
        files = new HashMap<>();
    }

    @Override
    public void create(String filename, String content) {
        // Validating file name
        if (!files.containsKey(filename)) {
            files.put(filename, new FileInformation(new FileFrame(content, Mode.CLOSED)));
        }
    }

    @Override
    public Optional<File> open(String filename, Mode mode) {
        FileInformation chosenFile = files.get(filename);

        // Can't open files that are closed, unknown or dont exist
        if (mode == Mode.CLOSED || mode == Mode.UNKNOWN || chosenFile == null) {
            return Optional.empty();
        }
        // File is closed and opening the file in read for the first time
        else if (chosenFile.fileFrame.mode == Mode.CLOSED && mode == Mode.READABLE) {
            try {
                files.replace(filename, new FileInformation(new FileFrame(chosenFile.fileFrame.content, Mode.READABLE)));
                files.get(filename).lock.acquire();
                return Optional.of(new File(filename, chosenFile.fileFrame.content, Mode.READABLE));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return Optional.empty();
            }
        }
        // File is closed and opening the file in writable for the first time
        else if (chosenFile.fileFrame.mode == Mode.CLOSED && mode == Mode.READWRITEABLE) {
            try {
                chosenFile.lock.acquire(Integer.MAX_VALUE);
                files.replace(filename, new FileInformation(new FileFrame(chosenFile.fileFrame.content, Mode.READWRITEABLE), chosenFile.lock));                
                return Optional.of(new File(filename, chosenFile.fileFrame.content, Mode.READWRITEABLE));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return Optional.empty();
            }
        }
        // File is being read and another request asks for read access
        else if (chosenFile.fileFrame.mode == Mode.READABLE && mode == Mode.READABLE) {
            try {
                chosenFile.lock.acquire();
                return Optional.of(new File(filename, chosenFile.fileFrame.content, Mode.READABLE));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return Optional.empty();
            }
        }
        // File is being written to and another request asks for write access
        else if (chosenFile.fileFrame.mode == Mode.READWRITEABLE && mode == Mode.READWRITEABLE) {
            try {
                chosenFile.lock.acquire(Integer.MAX_VALUE);
                return Optional.of(new File(filename, chosenFile.fileFrame.content, Mode.READWRITEABLE));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return Optional.empty();
            }
        }
        // File is being read from and a request is made to write to the file
        else if (chosenFile.fileFrame.mode == Mode.READABLE && mode == Mode.READWRITEABLE) {
            try {                
                chosenFile.lock.acquire(Integer.MAX_VALUE);
                files.replace(filename, new FileInformation( new FileFrame(chosenFile.fileFrame.content, Mode.READWRITEABLE), chosenFile.lock));
                return Optional.of(new File(filename, chosenFile.fileFrame.content, Mode.READWRITEABLE));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return Optional.empty();
            }
        } 
        //File is being written to and a request comes in for read access
        else if (chosenFile.fileFrame.mode == Mode.READWRITEABLE && mode == Mode.READABLE) {
            try {
                chosenFile.lock.acquire();
                files.replace(filename, new FileInformation(new FileFrame(chosenFile.fileFrame.content, Mode.READABLE), chosenFile.lock));
                return Optional.of(new File(filename, chosenFile.fileFrame.content, Mode.READABLE));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return Optional.empty();
            }
        } 
        else {
            return Optional.empty();
        }
    }

    @Override
    public void close(File file) {
        FileInformation closingFile = files.get(file.filename());
        if (file.mode() == Mode.READABLE && closingFile.fileFrame.mode == Mode.READABLE) {
            if (closingFile.lock.availablePermits()+1 == Integer.MAX_VALUE) {
                files.replace(file.filename(), new FileInformation(new FileFrame(closingFile.fileFrame.content, Mode.CLOSED), closingFile.lock));
            }
            files.get(file.filename()).lock.release();
        } else if (file.mode() == Mode.READWRITEABLE && closingFile.fileFrame.mode == Mode.READWRITEABLE) {           
            files.replace(file.filename(), new FileInformation(new FileFrame(file.read(), Mode.CLOSED), closingFile.lock)); 
            files.get(file.filename()).lock.release(Integer.MAX_VALUE);
        }
    }

    @Override
    public Mode fileStatus(String filename) {
        return files.get(filename) != null ? files.get(filename).fileFrame.mode : Mode.UNKNOWN;
    }

    @Override
    public Set<String> availableFiles() {
        return files.keySet();
    }
}