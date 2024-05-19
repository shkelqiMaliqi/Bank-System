import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Bank {
    private String name;
    private double totalTransactionFee;
    private double totalTransferAmount;
    private double transactionFlatFee;
    private double transactionPercentFee;
    private List<Account> accounts;
    private List<Transaction> transactions;


    public Bank(String name, double transactionFlatFee, double transactionPercentFee) {
        this.name = name;
        this.transactionFlatFee = transactionFlatFee;
        this.transactionPercentFee = transactionPercentFee;
        this.accounts = new ArrayList<>();
        this.transactions = new ArrayList<>();
    }

  
    Connection connect() {
        String url = "jdbc:mysql://localhost:3306/linkplus_db";
        String user = "root";
        String password = "";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connection to database sucessfully.");
        } catch (SQLException e) {
            System.out.println("Failed to connect to the database.");
            e.printStackTrace();
        }
        return conn;
    }


    public void createAccount(String name) {
        String sql = "INSERT INTO account (name, balance) VALUES (?, 0.00)";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn != null) {
                pstmt.setString(1, name);
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Account created successfully for user: " + name);
                } else {
                    System.out.println("Account creation failed the for user: " + name);
                }
            } else {
                System.out.println("Connection failed with database. Cannot create account.");
            }
        } catch (SQLException e) {
            System.out.println("SQL Error during account creation: " + e.getMessage());
            e.printStackTrace();
        }
    }

   
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getTotalTransactionFee() {
        return totalTransactionFee;
    }

    public void setTotalTransactionFee(double totalTransactionFee) {
        this.totalTransactionFee = totalTransactionFee;
    }

    public double getTotalTransferAmount() {
        return totalTransferAmount;
    }

    public void setTotalTransferAmount(double totalTransferAmount) {
        this.totalTransferAmount = totalTransferAmount;
    }

    public double getTransactionFlatFee() {
        return transactionFlatFee;
    }

    public void setTransactionFlatFee(double transactionFlatFee) {
        this.transactionFlatFee = transactionFlatFee;
    }

    public double getTransactionPercentFee() {
        return transactionPercentFee;
    }

    public void setTransactionPercentFee(double transactionPercentFee) {
        this.transactionPercentFee = transactionPercentFee;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }


public void deposit(int accountId, double amount) {
    String sql = "UPDATE account SET balance = balance + ? WHERE id = ?";
    try (Connection conn = this.connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        if (conn != null) {
            pstmt.setDouble(1, amount);
            pstmt.setInt(2, accountId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                Transaction transaction = new Transaction(amount, 0, accountId, "Deposit");
                transaction.recordTransaction(conn);
                transactions.add(transaction);
                System.out.println("Deposit of $" + amount + " successful for account ID: " + accountId);
            } else {
                System.out.println("Deposit failed: insufficient funds or invalid account: " + accountId);
            }
        } else {
            System.out.println("Connection is null. Cannot perform deposit.");
        }
    } catch (SQLException e) {
        System.out.println("SQL Error during deposit: " + e.getMessage());
        e.printStackTrace();
    }
}
public void withdraw(int accountId, double amount) {
    String sql = "UPDATE account SET balance = balance - ? WHERE id = ? AND balance >= ?";
    try (Connection conn = this.connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        if (conn != null) {
            pstmt.setDouble(1, amount);
            pstmt.setInt(2, accountId);
            pstmt.setDouble(3, amount);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                Transaction transaction = new Transaction(amount, accountId, 0, "Withdrawal");
                transaction.recordTransaction(conn);
                transactions.add(transaction);
                System.out.println("Withdrawal of $" + amount + " was successful for account ID: " + accountId);
            } else {
                System.out.println("Withdraw failed: insufficient funds or invalid account. " + accountId);
            }
        } else {
            System.out.println("Connection is null. Cannot perform withdrawal.");
        }
    } catch (SQLException e) {
        System.out.println("SQL Error during withdrawal: " + e.getMessage());
        e.printStackTrace();
    }
}


   
    public void transfer(int fromAccountId, int toAccountId, double amount, String reason) {
    try (Connection conn = this.connect()) {
        if (conn != null) {
            conn.setAutoCommit(false);
            double fee = 1.00;
            double amountWithFee = amount + fee;

          
            String withdrawSql = "UPDATE account SET balance = balance - ? WHERE id = ? AND balance >= ?";
            try (PreparedStatement withdrawStmt = conn.prepareStatement(withdrawSql)) {
                withdrawStmt.setDouble(1, amountWithFee);
                withdrawStmt.setInt(2, fromAccountId);
                withdrawStmt.setDouble(3, amountWithFee);
                int rowsAffected = withdrawStmt.executeUpdate();
                if (rowsAffected == 0) {
                    System.out.println("Transfer failed: insufficient funds or invalid originating account.");
                    conn.rollback();
                    return;
                }
            }

          
            String depositSql = "UPDATE account SET balance = balance + ? WHERE id = ?";
            try (PreparedStatement depositStmt = conn.prepareStatement(depositSql)) {
                depositStmt.setDouble(1, amount);
                depositStmt.setInt(2, toAccountId);
                int rowsAffected = depositStmt.executeUpdate();
                if (rowsAffected == 0) {
                    System.out.println("Transfer failed: invalid resulting account.");
                    conn.rollback();
                    return;
                }
            }

      
            Transaction transaction = new Transaction(amount, fromAccountId, toAccountId, reason);
            transaction.recordTransaction(conn);
            transactions.add(transaction);

            
            conn.commit();
            addTransactionFee(fee);
            System.out.println("Transfer of $" + amount + " from account ID " + fromAccountId +
                    " to account ID " + toAccountId + " was successful with $" + fee + " fee applied.");
        } else {
            System.out.println("Connection is null. Cannot perform transfer.");
        }
    } catch (SQLException e) {
        System.out.println("SQL Error during transfer: " + e.getMessage());
        e.printStackTrace();
    }
}

    public void checkBalance(int accountId) {
        String sql = "SELECT balance FROM account WHERE id = ?";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double balance = rs.getDouble("balance");
                    System.out.println("The balance for account ID " + accountId + " is $" + balance);
                } else {
                    System.out.println("Account not found with ID: " + accountId);
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error during balance check: " + e.getMessage());
            e.printStackTrace();
        }
    }

  
    public void listAccounts() {
        String sql = "SELECT id, name, balance FROM account";
        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                double balance = rs.getDouble("balance");
                System.out.println("Account ID: " + id + ", Name: " + name + ", Balance: $" + balance);
            }
        } catch (SQLException e) {
            System.out.println("SQL Error during listing accounts: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void viewTransactions() {
        for (Transaction transaction : transactions) {
            System.out.println("Transaction ID: " + transaction.getId() +
                    ", Amount: $" + transaction.getAmount() +
                    ", From Account: " + transaction.getOriginatingAccountId() +
                    ", To Account: " + transaction.getResultingAccountId() +
                    ", Reason: " + transaction.getReason());
        }
    }
       

    public void addTransactionFee(double fee) {
        totalTransactionFee += fee;
    }

    void recordTransaction() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

 
    private static class Transaction {
        private int id;
        private double amount;
        private int originatingAccountId;
        private int resultingAccountId;
        private String reason;

       
        public Transaction(double amount, int originatingAccountId, int resultingAccountId, String reason) {
            this.amount = amount;
            this.originatingAccountId = originatingAccountId;
            this.resultingAccountId = resultingAccountId;
            this.reason = reason;
        }

   
         public void recordTransaction(Connection conn) {
            String sql = "INSERT INTO transaction (amount, originating_account_id, resulting_account_id, reason) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setDouble(1, amount);
                pstmt.setInt(2, originatingAccountId);
                pstmt.setInt(3, resultingAccountId);
                pstmt.setString(4, reason);
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            this.id = generatedKeys.getInt(1);
                        }
                    }
                } else {
                    System.out.println("Transaction recording failed.");
                }
            } catch (SQLException e) {
                System.out.println("SQL Error during transaction recording: " + e.getMessage());
                e.printStackTrace();
            }
        }


    
        public int getId() {
            return id;
        }

        public double getAmount() {
            return amount;
        }

        public int getOriginatingAccountId() {
            return originatingAccountId;
        }

        public int getResultingAccountId() {
            return resultingAccountId;
        }

        public String getReason() {
            return reason;
        }
    }

}