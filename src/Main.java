import java.util.concurrent.locks.ReentrantLock;

public class Main{
    private static final int NUM_PHILOSOPHERS = 7; // Например: номер по списку 7 => 7 + 10
    private static final int CYCLES = 5; // Количество итераций размышлений/еды

    private final ReentrantLock[] forks = new ReentrantLock[NUM_PHILOSOPHERS];

    public Main() {
        for (int i = 0; i < NUM_PHILOSOPHERS; i++) {
            forks[i] = new ReentrantLock(true); // true => fair lock
        }
    }

    class Philosopher extends Thread {
        private final int id;

        public Philosopher(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            int left = id;
            int right = (id + 1) % NUM_PHILOSOPHERS;

            for (int i = 0; i < CYCLES; i++) {
                think();

                boolean eaten = false;
                while (!eaten) {
                    // Чередуем порядок захвата вилок, чтобы избежать deadlock
                    if (id % 2 == 0) {
                        eaten = tryEat(left, right);
                    } else {
                        eaten = tryEat(right, left);
                    }
                }
            }

            System.out.println("Философ " + id + " завершил цикл.");
        }

        private void think() {
            System.out.println("Философ " + id + " размышляет.");
            try {
                Thread.sleep((long) (Math.random() * 500));
            } catch (InterruptedException ignored) {}
        }

        private boolean tryEat(int firstFork, int secondFork) {
            if (forks[firstFork].tryLock()) {
                try {
                    if (forks[secondFork].tryLock()) {
                        try {
                            System.out.println("Философ " + id + " ест.");
                            Thread.sleep((long) (Math.random() * 500));
                        } catch (InterruptedException ignored) {}
                        finally {
                            forks[secondFork].unlock();
                        }
                        return true;
                    }
                } finally {
                    forks[firstFork].unlock();
                }
            }

            // Не удалось взять обе вилки
            try {
                Thread.sleep(10); // Немного подождать перед повторной попыткой
            } catch (InterruptedException ignored) {}
            return false;
        }
    }

    public void startDinner() {
        Thread[] philosophers = new Thread[NUM_PHILOSOPHERS];
        for (int i = 0; i < NUM_PHILOSOPHERS; i++) {
            philosophers[i] = new Philosopher(i);
            philosophers[i].start();
        }

        for (int i = 0; i < NUM_PHILOSOPHERS; i++) {
            try {
                philosophers[i].join();
            } catch (InterruptedException ignored) {}
        }

        System.out.println("Ужин завершён.");
    }

    public static void main(String[] args) {
        new Main().startDinner();
    }
}
