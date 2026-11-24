import java.io.*;
import java.util.Scanner;
import java.util.Stack;

// Expense Tracker - Linked List, Sorting, Binary Search, Stack, File Handling
public class main {

    // ---------- Node of our own linked list ----------
    static class Expense {
        String id;
        String category;
        double amount;
        Expense next;

        Expense(String id, String category, double amount) {
            this.id = id;
            this.category = category;
            this.amount = amount;
        }
    }

    // ---------- Own singly linked list ----------
    static Expense head = null;
    static Expense tail = null;
    static int size = 0;
    static boolean sorted = false;                 // true when list is sorted low -> high

    static Stack<String> undoStack = new Stack<>(); // IDs of added expenses
    static final String FILE_NAME = "expenses.txt";

    // ---------- Helpers ----------
    static String fmt(double a) {
        if (a == (long) a) return String.valueOf((long) a);
        return String.valueOf(a);
    }

    static String describe(Expense e) {
        return e.id + " | " + e.category + " | " + fmt(e.amount);
    }

    static String generateId() {
        int max = 100;                              // first ID will be X101
        for (Expense c = head; c != null; c = c.next) {
            try {
                int n = Integer.parseInt(c.id.substring(1));
                if (n > max) max = n;
            } catch (Exception ignored) { }
        }
        return "X" + (max + 1);
    }

    static void appendNode(Expense e) {
        if (head == null) head = tail = e;
        else { tail.next = e; tail = e; }
        size++;
        sorted = false;
    }

    // ---------- Features ----------
    static void addExpense(Scanner sc) {
        System.out.print("Category: ");
        String category = sc.nextLine().trim();
        System.out.print("Amount: ");
        String amtText = sc.nextLine().trim();

        double amount;
        try {
            amount = Double.parseDouble(amtText);
        } catch (NumberFormatException ex) {
            System.out.println("Invalid amount. Amount must be greater than 0.");
            return;
        }
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0) {
            System.out.println("Invalid amount. Amount must be greater than 0.");
            return;
        }
        if (category.isEmpty() || category.contains(",")) {
            System.out.println("Invalid category. Must not be empty or contain a comma.");
            return;
        }

        Expense e = new Expense(generateId(), category, amount);
        appendNode(e);
        undoStack.push(e.id);
        System.out.println("Expense added. ID: " + e.id + " | " + e.category + " | " + fmt(e.amount));
    }

    static void viewAll() {
        if (head == null) {
            System.out.println("No expenses found.");
            return;
        }
        int i = 1;
        for (Expense c = head; c != null; c = c.next) {
            System.out.println(i++ + ". " + describe(c));
        }
    }

    // Sort linked list by amount, low to high (insertion sort on nodes)
    static void sortByAmount() {
        Expense newHead = null;
        Expense cur = head;
        while (cur != null) {
            Expense nxt = cur.next;
            if (newHead == null || cur.amount < newHead.amount) {
                cur.next = newHead;
                newHead = cur;
            } else {
                Expense p = newHead;
                while (p.next != null && p.next.amount <= cur.amount) p = p.next;
                cur.next = p.next;
                p.next = cur;
            }
            cur = nxt;
        }
        head = newHead;
        tail = head;
        while (tail != null && tail.next != null) tail = tail.next;
        sorted = true;
    }

    static void sortAndShow() {
        if (head == null) {
            System.out.println("No expenses to sort.");
            return;
        }
        sortByAmount();
        viewAll();
    }

    // Binary search (works only on sorted data, so we sort first)
    static void binarySearch(Scanner sc) {
        if (head == null) {
            System.out.println("No expenses to search.");
            return;
        }
        System.out.print("Enter amount to search: ");
        double target;
        try {
            target = Double.parseDouble(sc.nextLine().trim());
        } catch (NumberFormatException ex) {
            System.out.println("Invalid amount.");
            return;
        }

        if (!sorted) sortByAmount();               // binary search needs sorted data

        Expense[] arr = new Expense[size];
        int k = 0;
        for (Expense c = head; c != null; c = c.next) arr[k++] = c;

        int low = 0, high = size - 1, found = -1;
        while (low <= high) {
            int mid = (low + high) / 2;
            if (arr[mid].amount == target) {
                found = mid;
                high = mid - 1;                    // keep looking left for first match
            } else if (arr[mid].amount < target) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        if (found == -1) System.out.println("Not found: " + fmt(target));
        else System.out.println("Found: " + describe(arr[found]));
    }

    static void undoLast() {
        if (undoStack.isEmpty()) {
            System.out.println("Nothing to undo.");
            return;
        }
        String id = undoStack.pop();
        Expense prev = null, cur = head;
        while (cur != null && !cur.id.equals(id)) {
            prev = cur;
            cur = cur.next;
        }
        if (cur == null) {
            System.out.println("Nothing to undo.");
            return;
        }
        if (prev == null) head = cur.next;
        else prev.next = cur.next;
        if (cur == tail) tail = prev;
        size--;
        System.out.println("Last expense " + id + " removed.");
        System.out.println("Total expenses: " + size);
    }

    // ---------- File handling ----------
    static void loadFromFile() {
        File f = new File(FILE_NAME);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split(",");
                if (p.length != 3) continue;
                try {
                    Expense e = new Expense(p[0].trim(), p[1].trim(), Double.parseDouble(p[2].trim()));
                    appendNode(e);
                    undoStack.push(e.id);
                } catch (NumberFormatException ignored) { }
            }
        } catch (IOException ex) {
            System.out.println("Could not read " + FILE_NAME + ": " + ex.getMessage());
        }
    }

    static void saveToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Expense c = head; c != null; c = c.next) {
                pw.println(c.id + "," + c.category + "," + fmt(c.amount));
            }
        } catch (IOException ex) {
            System.out.println("Could not save " + FILE_NAME + ": " + ex.getMessage());
        }
    }

    // ---------- Main ----------
    static void showMenu() {
        System.out.println("\n===== Expense Tracker =====");
        System.out.println("1. Add expense");
        System.out.println("2. View all expenses");
        System.out.println("3. Sort by amount (low to high)");
        System.out.println("4. Binary search by amount");
        System.out.println("5. Undo last added expense");
        System.out.println("6. Save and exit");
        System.out.print("Choose option: ");
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        loadFromFile();

        boolean running = true;
        while (running) {
            showMenu();
            if (!sc.hasNextLine()) break;
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1": addExpense(sc); break;
                case "2": viewAll(); break;
                case "3": sortAndShow(); break;
                case "4": binarySearch(sc); break;
                case "5": undoLast(); break;
                case "6": running = false; break;
                default: System.out.println("Invalid option. Choose 1-6.");
            }
        }
        saveToFile();
        System.out.println("Data saved to " + FILE_NAME + ". Goodbye!");
        sc.close();
    }
}