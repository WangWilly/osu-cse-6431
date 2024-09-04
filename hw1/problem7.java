package hw1;

import java.lang.Thread;

class FakeFileIO {
    private String fileContent;
    private CondObj condition;
    private boolean isUsing;
    private boolean writeBarrier;
    private int readCount;

    public FakeFileIO(String fileContent) {
        System.out.println("[FakeFileIO] \t initialized with content: " + fileContent);
        this.fileContent = fileContent;
        condition = new CondObj();
        isUsing = false;
        writeBarrier = true;
        readCount = 0;
    }

    public void write(String content) {
        synchronized (condition) {
            while (isUsing) {
                try {
                    condition.wait(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // force writer to wait, then reader can read the content.
            while (writeBarrier) {
                try {
                    condition.notifyAll();
                    condition.wait(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                writeBarrier = false;
            }
            
            isUsing = true;
            System.out.println("[FakeFileIO] \t writing content: " + content);
            fileContent = content;
    
            isUsing = false;
            writeBarrier = true;
            condition.notifyAll();
        }
    }

    public String read() {
        synchronized (condition) {
            if (readCount == 0) {
                while (isUsing) {
                    try {
                        condition.wait(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            readCount++;
            if (readCount == 1) {
                isUsing = true;
            }
            
            System.out.println("[FakeFileIO] \t reading content: " + fileContent);
            String content = fileContent;

            readCount--;
            if (readCount == 0) {
                isUsing = false;
                condition.notifyAll();
            }

            return content;
        }
    }
}

class Writer extends Thread {
    private FakeFileIO fileIO;
    private static String[] contents = {
        "content000",
        "content001",
        "content002",
        "content003",
        "content004",
        "content005",
        "content006",
        "content007",
        "content008",
        "content009"
    };

    public Writer(FakeFileIO fileIO) {
        this.fileIO = fileIO;
    }

    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            fileIO.write(contents[i]);
            System.out.println("[Writer] \t wrote content: " + contents[i]);
        }
    }
}

class Reader extends Thread {
    private FakeFileIO fileIO;

    public Reader(FakeFileIO fileIO) {
        this.fileIO = fileIO;
    }

    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            fileIO.read();
            System.out.println("[Reader] \t read content");
        }
    }
}


public class Problem7 {
    // testing
    public static void main(String[] args) {
        System.out.println("[main] \t Starting reader-writer problem...");

        FakeFileIO fileIO = new FakeFileIO("initial content");
        Writer writer = new Writer(fileIO);
        Reader reader = new Reader(fileIO);

        writer.start();
        reader.start();

        try {
            System.out.println("[main] \t Waiting for threads to finish...");
            writer.join();
            reader.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("[main] \t Reader-writer problem finished.");
    }
}
