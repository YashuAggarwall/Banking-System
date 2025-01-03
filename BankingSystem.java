import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class BankingSystem {
    private static final String url = "jdbc:mysql://localhost:3306/banking_system";
    private static final String username = "root";
    private static final String password = "Yashu@121";

    public BankingSystem() {
    }

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException var4) {
            ClassNotFoundException e = var4;
            System.out.println(e.getMessage());
        }

        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/banking_system", "root", "Yashu@121");
            System.out.print("WELCOME TO MY BANK");

            while(true) {
                while(true) {
                    Scanner s = new Scanner(System.in);
                    System.out.println();
                    System.out.println("1. Create Account");
                    System.out.println("2. Deposit");
                    System.out.println("3. Withdraw");
                    System.out.println("4. Balance");
                    System.out.println("5. Reset PIN");
                    System.out.println("6. Exit");
                    System.out.print("Choose an option: ");
                    int ch = s.nextInt();
                    switch (ch) {
                        case 1:
                            createaccount(con, s);
                            break;
                        case 2:
                            deposit(con, s);
                            break;
                        case 3:
                            withdraw(con, s);
                            break;
                        case 4:
                            balance(con, s);
                            break;
                        case 5:
                            reset(con, s);
                            break;
                        case 6:
                            exit();
                    }
                }
            }
        } catch (SQLException var5) {
            SQLException e = var5;
            System.out.println(e.getMessage());
        } catch (InterruptedException var6) {
            InterruptedException e = var6;
            throw new RuntimeException(e);
        }
    }

    public static void createaccount(Connection con, Scanner scan) throws SQLException {
        System.out.println("Enter your first name");
        String firstname = scan.next();
        System.out.println("Enter your second name");
        String secondname = scan.next();
        System.out.println("Enter Phone number");
        String num = scan.next();
        String query1 = "SELECT * FROM accounts WHERE phone_number=?";
        PreparedStatement p = con.prepareStatement(query1);
        p.setString(1, num);
        ResultSet res = p.executeQuery();
        if (res.next()) {
            System.out.println("Already A User");
        } else {
            System.out.println("Enter Initial Amount");
            int balance = scan.nextInt();
            System.out.println("Enter Pin You Want To Sent");
            int pin = scan.nextInt();
            String query = "INSERT INTO accounts(account_number,balance,firstname,secondname,pin,phone_number) VALUES(?,?,?,?,?,?)";

            try {
                int accountNumber = (int)(Math.random() * 9000.0) + 1000;
                String acc = String.valueOf(accountNumber);
                PreparedStatement pstmt = con.prepareStatement(query);
                pstmt.setString(1, acc);
                pstmt.setDouble(2, (double)balance);
                pstmt.setString(3, firstname);
                pstmt.setString(4, secondname);
                pstmt.setInt(5, pin);
                pstmt.setString(6, num);
                pstmt.executeUpdate();
                System.out.println("ACCOUNT CREATED SUCCESSFULLY");
                System.out.println("YOUR ACCOUNT NUMBER IS: " + accountNumber);
            } catch (SQLException var14) {
                SQLException e = var14;
                System.out.println(e.getMessage());
            }
        }

    }

    public static boolean authentication() throws SQLException {
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter account number");
        int acc = scan.nextInt();
        String acc1 = String.valueOf(acc);
        System.out.println("Enter your security pin");
        int pin = scan.nextInt();
        String query = "SELECT pin FROM accounts WHERE account_number= ?";
        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/banking_system", "root", "Yashu@121");
        PreparedStatement pstmt = con.prepareStatement(query);
        pstmt.setString(1, acc1);
        ResultSet res = pstmt.executeQuery();

        int pin1;
        do {
            if (!res.next()) {
                return false;
            }

            pin1 = res.getInt("pin");
        } while(pin != pin1);

        return true;
    }

    public static void deposit(Connection con, Scanner scan) throws SQLException {
        boolean ban = authentication();
        if (ban) {
            String query = "UPDATE accounts SET balance = balance + ? WHERE account_number = ?";
            System.out.println("Enter amount to be deposited");
            double am = scan.nextDouble();
            System.out.println("Enter Account Number in which you want to Deposit");
            int acc = scan.nextInt();
            String acc1 = String.valueOf(acc);

            try {
                con.setAutoCommit(false);
                PreparedStatement pstmt = con.prepareStatement(query);
                pstmt.setDouble(1, am);
                pstmt.setString(2, acc1);
                pstmt.executeUpdate();
                con.commit();
                System.out.println("Transaction Completed!!!");
            } catch (SQLException var9) {
                SQLException e = var9;
                System.out.println(e.getMessage());
                con.rollback();
                System.out.println("Transaction Failed!!!");
            }
        } else {
            System.out.println("Wrong PIN");
        }

    }

    public static void withdraw(Connection con, Scanner scan) throws SQLException {
        boolean ban = authentication();
        if (ban) {
            String checkBalanceQuery = "SELECT balance FROM accounts WHERE account_number = ?";
            String withdrawQuery = "UPDATE accounts SET balance = balance - ? WHERE account_number = ?";
            System.out.println("Enter amount to be Withdrawn:");
            double amount = scan.nextDouble();
            System.out.println("Enter Account Number from which you want to Withdraw:");
            int acc = scan.nextInt();
            String acc1 = String.valueOf(acc);

            try {
                con.setAutoCommit(false);
                PreparedStatement checkBalanceStmt = con.prepareStatement(checkBalanceQuery);
                checkBalanceStmt.setString(1, acc1);
                ResultSet rs = checkBalanceStmt.executeQuery();
                if (rs.next()) {
                    double currentBalance = rs.getDouble("balance");
                    if (currentBalance >= amount) {
                        PreparedStatement withdrawStmt = con.prepareStatement(withdrawQuery);
                        withdrawStmt.setDouble(1, amount);
                        withdrawStmt.setString(2, acc1);
                        withdrawStmt.executeUpdate();
                        con.commit();
                        System.out.println("Transaction Completed! New balance: " + (currentBalance - amount));
                    } else {
                        System.out.println("Insufficient funds! Current balance: " + currentBalance);
                        con.rollback();
                    }
                } else {
                    System.out.println("Account not found!");
                    con.rollback();
                }
            } catch (SQLException var17) {
                SQLException e = var17;
                System.out.println("Error: " + e.getMessage());
                con.rollback();
                System.out.println("Transaction Failed!");
            } finally {
                con.setAutoCommit(true);
            }
        } else {
            System.out.println("Wrong PIN");
        }

    }

    public static void balance(Connection con, Scanner scan) throws SQLException {
        Boolean ban = authentication();
        if (ban) {
            String checkBalanceQuery = "SELECT balance FROM accounts WHERE account_number = ?";
            System.out.println("Enter Account Number");
            int acc = scan.nextInt();
            String acc1 = String.valueOf(acc);
            PreparedStatement checkBalanceStmt = con.prepareStatement(checkBalanceQuery);
            checkBalanceStmt.setString(1, acc1);
            ResultSet rs = checkBalanceStmt.executeQuery();
            if (rs.next()) {
                double currentBalance = rs.getDouble("balance");
                System.out.println(currentBalance);
            }
        } else {
            System.out.println("WRONG PIN");
        }

    }

    public static void reset(Connection con, Scanner scan) throws SQLException {
        System.out.println("Enter Account Number");
        int acc = scan.nextInt();
        String acc1 = String.valueOf(acc);
        System.out.println("Enter Your Current PIN");
        int pin1 = scan.nextInt();
        String query = "SELECT pin from accounts WHERE account_number= ?";
        PreparedStatement pstmt = con.prepareStatement(query);
        pstmt.setString(1, acc1);
        ResultSet res = pstmt.executeQuery();

        while(res.next()) {
            int pin2 = res.getInt("pin");
            if (pin1 == pin2) {
                System.out.println("Enter new pin");
                int newpin = scan.nextInt();
                String query1 = "UPDATE accounts SET pin= ? WHERE account_number=?";
                PreparedStatement stmt = con.prepareStatement(query1);
                stmt.setInt(1, newpin);
                stmt.setString(2, acc1);
                int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated > 0) {
                    System.out.println("PIN reset successful.");
                } else {
                    System.out.println("Error: Unable to reset PIN.");
                }
            } else {
                System.out.println("Incorrect current PIN. PIN not reset.");
            }
        }

    }

    public static void exit() throws InterruptedException {
        System.out.print("Exiting System");

        for(int i = 5; i != 0; --i) {
            System.out.print(".");
            Thread.sleep(1000L);
        }

        System.out.println();
        System.out.println("ThankYou For Using Banking System!!!");
    }
}