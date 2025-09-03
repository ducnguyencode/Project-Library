package UI;

import Model.Books;
import Model.Employee;
import Model.Transaction;

import java.util.ArrayList;

public class TransactionMockData {
    private static ArrayList<Books> books = null;
    private static ArrayList<Employee> employees = null;
    private static ArrayList<Transaction> transactions = null;

    public static ArrayList<Books> getBooks() {
        if (books == null) {
            books = new ArrayList<>();
            books.add(new Books("JA-SM-001", "101-1001", "Java Basics", "Smith", true));
            books.add(new Books("DA-MI-002", "102-1002", "Data Mining", "John", false));
            books.add(new Books("NE-KO-003", "103-1003", "Networking 101", "Kobayashi", true));
        }
        return books;
    }

    public static ArrayList<Employee> getEmployees() {
        if (employees == null) {
            employees = new ArrayList<>();
            employees.add(new Employee("E001", "Alice", "123 Street", "0123456789", "IT"));
            employees.add(new Employee("E002", "Bob", "456 Avenue", "0987654321", "HR"));
        }
        return employees;
    }

    public static ArrayList<Transaction> getTransactions() {
        if (transactions == null) {
            transactions = new ArrayList<>();
        }
        return transactions;
    }
}
