import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Client
 */
public class Client extends Thread {
    private FileServer fileServer;
    public String name;

    public Client(FileServer fileServer, String name){
        this.fileServer = fileServer;
        this.name = name;
    }

    @Override
    public void run() {
        //Random file
        int fileChoice = ThreadLocalRandom.current().nextInt(0, 5);
        //Random mode
        boolean fileMode = ThreadLocalRandom.current().nextBoolean();
        Optional<File> chosenFile = null;
        //Opening random file
        if(fileChoice == 0){
            System.out.println("Thread " + name + " is opening file a in " + (fileMode ? "Read" : "Write") +  " mode.");
            chosenFile = fileServer.open("a", fileMode ? Mode.READABLE : Mode.READWRITEABLE);
        }
        else if(fileChoice == 1){
            System.out.println("Thread " + name + " is opening file b in " + (fileMode ? "Read" : "Write") +  " mode.");
            chosenFile = fileServer.open("b", fileMode ? Mode.READABLE : Mode.READWRITEABLE);
        }
        else if(fileChoice == 2){
            System.out.println("Thread " + name + " is opening file c in " + (fileMode ? "Read" : "Write") +  " mode.");
            chosenFile = fileServer.open("c", fileMode ? Mode.READABLE : Mode.READWRITEABLE);
        }  
        else if(fileChoice == 3){
            System.out.println("Thread " + name + " is opening file d in " + (fileMode ? "Read" : "Write") +  " mode.");
            chosenFile = fileServer.open("d", fileMode ? Mode.READABLE : Mode.READWRITEABLE);
        } 
        else{
            System.out.println("Thread " + name + " is opening file e in " + (fileMode ? "Read" : "Write") +  " mode.");
            chosenFile = fileServer.open("e", fileMode ? Mode.READABLE : Mode.READWRITEABLE);
        }   
        //Reading or writing to file
        if(chosenFile.isPresent()){
            if(fileMode){
                System.out.println("Thread " + name + " is reading " + chosenFile.get().read() +" from file " + chosenFile.get().filename() + ".");
            }
            else{
                System.out.println("Thread " + name + " is writing to file " + chosenFile.get().filename() + ".");
                chosenFile.get().write(chosenFile.get().read() + " World Hello");
            }           
        }
        //Closing file
        System.out.println("Thread " + name + " is closing file " + chosenFile.get().filename() + ".");
        fileServer.close(chosenFile.get());
    }

    public static void main(String[] args) throws InterruptedException {
        ArrayList<Client> clients = new ArrayList<>();
        //Creating server and files
        FileServer fileServer = new MyFileServer();
        fileServer.create("a", "Hello World");
        fileServer.create("b", "Hello World 2");
        fileServer.create("c", "Hello World 3");
        fileServer.create("d", "Hello World 4");
        fileServer.create("e", "Hello World 5");
        System.out.println("Starting threads.");
        //Adding threads to the list to keep track of them
        for (int i = 1; i <= 7; i++) {
            clients.add(new Client(fileServer, Integer.toString(i)));
        }
        //Starting all my threads 
        for (Client client : clients) {
            System.out.println("Starting thread " + client.name + ".");
            client.start();
        }
        //Waiting for threads to complete so I can the print done
        for (Client client : clients) {
            client.join();
        }
        System.out.println("Threads are finished.");
    }
}