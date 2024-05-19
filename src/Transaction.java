import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Transaction {
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

   
    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getOriginatingAccountId() {
        return originatingAccountId;
    }

    public void setOriginatingAccountId(int originatingAccountId) {
        this.originatingAccountId = originatingAccountId;
    }

    public int getResultingAccountId() {
        return resultingAccountId;
    }

    public void setResultingAccountId(int resultingAccountId) {
        this.resultingAccountId = resultingAccountId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

   
    void recordTransaction(Connection conn) throws SQLException {
        String sql = "INSERT INTO transaction (amount, originating_account_id, resulting_account_id, reason) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, this.amount);
            pstmt.setInt(2, this.originatingAccountId);
            pstmt.setInt(3, this.resultingAccountId);
            pstmt.setString(4, this.reason);
            pstmt.executeUpdate();
        }
    }

    
   public void deposit(int accountId, double amount) {
    String sql = "UPDATE account SET balance = balance + ? WHERE id = ?";
    try (Connection conn = this.connect();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        if (conn != null) {
            double fee = 1.00;
            double amountWithFee = amount - fee;
            pstmt.setDouble(1, amountWithFee);
            pstmt.setInt(2, accountId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                Transaction transaction = new Transaction(amount, 0, accountId, "Deposit");
                transaction.recordTransaction(conn);
                addTransactionFee(fee);
                transactions.add(transaction);  
                System.out.println("Deposit of $" + amount + " successful for account ID: " + accountId + " with $" + fee + " fee applied.");
            } else {
                System.out.println("Deposit failed for account ID: " + accountId);
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
            double fee = 1.00;
            double amountWithFee = amount + fee;
            pstmt.setDouble(1, amountWithFee);
            pstmt.setInt(2, accountId);
            pstmt.setDouble(3, amountWithFee);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                Transaction transaction = new Transaction(amount, accountId, 0, "Withdrawal");
                transaction.recordTransaction(conn);
                addTransactionFee(fee);
                transactions.add(transaction); 
                System.out.println("Withdrawal of $" + amount + " successful for account ID: " + accountId + " with $" + fee + " fee applied.");
            } else {
                System.out.println("Withdrawal failed for account ID: " + accountId);
            }
        } else {
            System.out.println("Connection is null. Cannot perform withdrawal.");
        }
    } catch (SQLException e) {
        System.out.println("SQL Error during withdrawal: " + e.getMessage());
        e.printStackTrace();
    }
}

    
    public void transfer(int fromAccountId, int toAccountId, double amount) {
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

          
            Transaction transaction = new Transaction(amount, fromAccountId, toAccountId, "Transfer");
            transaction.recordTransaction(conn);
            transactions.add(transaction);  

           
            conn.commit();
            addTransactionFee(fee);
            System.out.println("Transfer of $" + amount + " from account ID " + fromAccountId + " to account ID " + toAccountId + " was successful with $" + fee + " fee applied.");
        } else {
            System.out.println("Connection is null. Cannot perform transfer.");
        }
    } catch (SQLException e) {
        System.out.println("SQL Error during transfer: " + e.getMessage());
        e.printStackTrace();
    }
}

    private Connection connect() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void addTransactionFee(double fee) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
