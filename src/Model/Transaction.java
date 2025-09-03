package Model;

import java.time.LocalDate;

public class Transaction {
	private String transactionID;
	private Employee employee;
	private Books book;
	private LocalDate issueDate;
	private LocalDate dueDate;
	private LocalDate returnDate;
	private double fine;

	public Transaction() {
		super();
	}

	public Transaction(String transactionID, Employee employee, Books book, LocalDate issueDate, LocalDate dueDate) {
		super();
		this.transactionID = transactionID;
		this.employee = employee;
		this.book = book;
		this.issueDate = issueDate;
		this.dueDate = dueDate;
		this.returnDate = null;
		this.fine = 0.0;
	}

	public void returnBook(LocalDate returnDate) {
		this.returnDate = returnDate;
		if (returnDate.isAfter(dueDate)) {
			long lateDays = java.time.temporal.ChronoUnit.DAYS.between(dueDate, returnDate);
			this.fine = lateDays * 0.1;
		}
	}

	public String getTransactionID() {
		return transactionID;
	}

	public Employee getEmployee() {
		return employee;
	}

	public Books getBook() {
		return book;
	}

	public LocalDate getIssueDate() {
		return issueDate;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public LocalDate getReturnDate() {
		return returnDate;
	}

	public double getFine() {
		return fine;
	}

	@Override
	public String toString() {
		return "Transaction [transactionID=" + transactionID + ", employeeID=" + employee.getName() + ", Bool="
				+ book.getTitle() + ", issueDate=" + issueDate + ", dueDate=" + dueDate + ", returnDate=" + returnDate
				+ ", fine=" + fine + "]";
	}

}
