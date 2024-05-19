import java.util.Scanner;

public class BankSystemApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Bank bank = new Bank("My Bank", 10.00, 5.00);

        while (true) {
            System.out.println("1 -> Create Account");
            System.out.println("2 -> Deposit");
            System.out.println("3 -> Withdraw");
            System.out.println("4 -> Transfer");
            System.out.println("5 -> Check Balance");
            System.out.println("6 -> List Accounts");
            System.out.println("7 -> View Transactions");
            System.out.println("8 -> Exit");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    System.out.print("Enter account name: ");
                    String name = scanner.next();
                    bank.createAccount(name);
                    break;
                case 2:
                    System.out.print("Enter account ID: ");
                    int accountId = scanner.nextInt();
                    System.out.print("Enter amount to deposit: ");
                    double depositAmount = scanner.nextDouble();
                    bank.deposit(accountId, depositAmount);
                    break;
                case 3:
                    System.out.print("Enter account ID: ");
                    int withdrawAccountId = scanner.nextInt();
                    System.out.print("Enter amount to withdraw: ");
                    double withdrawAmount = scanner.nextDouble();
                    bank.withdraw(withdrawAccountId, withdrawAmount);
                    break;
                case 4:
                    System.out.print("Enter originating account ID: ");
                    int fromAccountId = scanner.nextInt();
                    System.out.print("Enter resulting account ID: ");
                    int toAccountId = scanner.nextInt();
                    System.out.print("Enter amount to transfer: ");
                    double transferAmount = scanner.nextDouble();
                    scanner.nextLine(); 
                    System.out.print("Enter reason for the transfer: ");
                    String reason = scanner.nextLine();
                    bank.transfer(fromAccountId, toAccountId, transferAmount, reason);
                    break;
                case 5:
                    System.out.print("Enter account ID: ");
                    int checkBalanceAccountId = scanner.nextInt();
                    bank.checkBalance(checkBalanceAccountId);
                    break;
                case 6:
                    System.out.println("Accounts:");
                    bank.listAccounts();
                    break;
                case 7:
                    bank.viewTransactions();
                    break;
                case 8:
                    System.exit(0);
            }
        }
    }
}
