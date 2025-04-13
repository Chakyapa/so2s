
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class Library {
    static int books;
    Writer[] writers;
    Reader[] readers;

    static ArrayList<String> library = new ArrayList<>();
    static final ReentrantReadWriteLock rwl = new
            ReentrantReadWriteLock(true);
    static final Lock writeLock = rwl.writeLock();
    static final Lock readLock = rwl.readLock();

    public Library (int writers, int readers, int books) {
        this.writers = new Writer[writers];
        this.readers = new Reader[readers];
        Library.books = books;

        for (int i = 0; i < writers; i++) {
            this.writers[i] = new Writer("Writer " + (i+1));
        }
        for (int i = 0; i < readers; i++) {
            this.readers[i] = new Reader("Reader " + (i+1));
        }
    }

    public void start() {
        for (Writer writer : this.writers) {
            writer.start();
        }
        for (Reader reader : this.readers) {
            reader.start();
        }
    }
}




class Writer extends Thread {
    String name;
    ArrayList<String> bookList = new
            ArrayList<>(Arrays.asList("Civil War", "Endgame", "Infinity Gauntlet",
            "Black Panther"));

    public final Lock writeLock = Library.writeLock;
    ArrayList<String> library = Library.library;
    ArrayList<String> writtenBooks = new ArrayList<>();
    static int count = 0;

    public Writer (String name) {
        this.name = name;
    }

    @Override
    public void run() {

        while(library.size() < Library.books){
            try {
                writeLock.lock();
                if (library.size() < Library.books){
                    String randomBook = bookList.get(count);
                    if (!library.contains(randomBook)){
                        sleep(1000);
                        library.add(randomBook);
                        writtenBooks.add(randomBook);
                        System.out.println(name + " wrote " + randomBook);
                        count++;
                        sleep(100);
                    }
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
            finally {
                writeLock.unlock();
            }
            if (library.size() == Library.books){
                System.out.println(name + " book list: \n" + writtenBooks);
            }
        }

    }
}

class Reader extends Thread {
    public final Lock readLock = Library.readLock;
    ArrayList<String> readBooks = new ArrayList<>();
    ArrayList<String> library = Library.library;
    String name;

    public Reader(String name) {
        this.name = name;
    }

    @Override
    public void run() {
        while(readBooks.size() < Library.books){
            try {
                if (Library.rwl.isWriteLocked()){
                    System.out.println(name + ": writer is in library");
                }
                readLock.lock();
                int random = (int)(Math.random()*library.size());
                if(random < library.size()) {
                    String randomBook = library.get(random);

                    if(readBooks.size() < Library.books){
                        if(!readBooks.contains(randomBook)){
                            sleep(300);
                            readBooks.add(randomBook);
                            System.out.println(name + " read book " + randomBook);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
            }
            readLock.unlock();
        }
        System.out.println(name + " finished reading \n" + readBooks);
    }
}
public class Main {
    public static void main(String[] args) {
        final int writers = 3;
        final int readers = 5;
        final int books = 4;
        Library library = new Library(writers, readers, books);
        library.start();
    }
}