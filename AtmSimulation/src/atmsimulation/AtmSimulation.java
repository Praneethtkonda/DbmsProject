package atmsimulation;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Random;

//Query Module
class Query{
    Connection con = null;
    Statement st = null;
    ResultSet rs = null;
    public Query() throws SQLException{
         con = DriverManager.getConnection("jdbc:postgresql://localhost/postgres","postgres", "asdf");
         st = con.createStatement();
    }
    public void answer(String sql) throws SQLException{
        try {
            st.executeQuery(sql);
        } catch (SQLException ex) {
            Logger.getLogger(Query.class.getName()).log(Level.SEVERE, null, ex);
        }
       con.close();   
    } 
}

//User class
class User{
    String name;
    int acno;
    int pinOfUser;
    int balance;
    int wdamt;
    Query q;
    BufferedReader b = new BufferedReader(new InputStreamReader(System.in));
    public User() throws IOException{
        System.out.println("Enter your pin:");
        pinOfUser = Integer.parseInt(b.readLine());
       
    }
    boolean initializeVariables() throws SQLException{
        this.q = new Query();
        String re = "Select * from users where pin=?;";
        PreparedStatement pst = q.con.prepareStatement(re);
        pst.setInt(1,this.pinOfUser);
        ResultSet rs = pst.executeQuery();
      
        if(rs.next()){
            
            this.name = rs.getString(1);
            this.acno = rs.getInt(2);
            this.balance =rs.getInt(4);
            // System.out.println(this.name+"\t"+this.acno+"\t"+this.balance);
            return true;
        }
        else{
            System.out.println("No matches in the database please contact admin");   
            return false;
           
        }
    }
    void checkBalance(){
        System.out.println(this.balance);
    }
    void withdrawFromAdmin() throws SQLException{
        String one = "update mainbalance set balance = balance-?";
        this.q = new Query();
        PreparedStatement pst = q.con.prepareStatement(one);
        pst.setInt(1,wdamt);
        pst.executeUpdate();
    }
    int theBalance() throws SQLException{ //Returns the current balance of the atm
        this.q = new Query();
        String no = "select * from mainbalance;";
        PreparedStatement pst = q.con.prepareStatement(no);
        ResultSet rs = pst.executeQuery();
        rs.next();
        return rs.getInt("balance");
    }
    void withdraw() throws SQLException, IOException{
        this.q = new Query();
        System.out.println("Enter your withdrawal amount:");
        int amount = Integer.parseInt(b.readLine());
       
        if(this.balance < amount) {
            System.out.println("Insufficient funds");
        }
        else{
            if(theBalance() > amount){
                this.balance -= amount;
                String two = "update users set balance = balance-? where pin=?;";
                PreparedStatement pst1 = q.con.prepareStatement(two);
                pst1.setInt(1,amount);
                pst1.setInt(2, this.pinOfUser);
                pst1.executeUpdate();
                wdamt = amount;
            }
            else{
                System.out.println("No cash in the atm, Sorry :)\n"+"As the cash in the atm is: Rs "+theBalance());
            }
        }
    }
    void changePin() throws SQLException, IOException {
        this.q = new Query();
        System.out.println("Enter your new pin:");
        int newPin = Integer.parseInt(b.readLine());
        String man  = "update users set pin = ? where acno = ?;";
        PreparedStatement as = q.con.prepareStatement(man);
        as.setInt(1,newPin);
        as.setInt(2, this.acno);
        as.executeUpdate();
    }
    void depositMainBalance() throws SQLException{ //Changes main balance in the databalance
         String one = "update mainbalance set balance = balance+?";
        this.q = new Query();
        PreparedStatement pst = q.con.prepareStatement(one);
        pst.setInt(1,wdamt);
        pst.executeUpdate();
    }
    void deposit() throws IOException, SQLException{
        System.out.println("Enter the amount to be deposited:");
        int amt = Integer.parseInt(b.readLine());
        this.balance += amt;
        //System.out.println(this.balance);
        String two = "update users set balance = balance+? where pin=?;";
        PreparedStatement pst1 = q.con.prepareStatement(two);
        pst1.setInt(1,amt);
        pst1.setInt(2, this.pinOfUser);
        pst1.executeUpdate();
        this.wdamt = amt;
    }
    void miniStatement(){
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        String d = dateFormat.format(date);
        System.out.println("Your MiniStatement\n"+"-----------------------------------\n"+
                "Name:\t"+this.name+"\n"+
                "Acno:\t"+this.acno+"\n"+
                "Balance:"+this.balance+"\n\n\n"+"\t\t\t"+d+"\n\n");
    }
     void decrementUser(int amt) throws SQLException{ // For Decrementing the balance of the user
        this.balance -= amt;
        //System.out.println(this.balance);
        String two = "update users set balance = balance-? where pin=?;";
        PreparedStatement pst1 = q.con.prepareStatement(two);
        pst1.setInt(1,amt);
        pst1.setInt(2, this.pinOfUser);
        pst1.executeUpdate();
    }
    void interBank() throws IOException, SQLException{  //Function to transfer money from one account to another
        System.out.println("Enter the account number to which money should be transfered:");
        int tempAcno = Integer.parseInt(b.readLine());
        System.out.println("Enter the money to be transfered:");
        int amt = Integer.parseInt(b.readLine());
        if(amt < this.balance){
            this.q = new Query();
            String qw = "update users set balance = balance+? where acno = ?;";
            PreparedStatement ps = q.con.prepareStatement(qw);
            ps.setInt(1,amt);
            ps.setInt(2,tempAcno);
            ps.executeUpdate();
            decrementUser(amt); //Invoking the above function
        }
        else{
            System.out.println("Insufficient funds");
        }
    }
   
}

//Admin class
class Admin{
    int balance = 10000;
    Scanner s = new Scanner(System.in);
    Random r = new Random();
    private int p;  // Ensuring the security it is a private pin
    PreparedStatement pst = null;
    BufferedReader b = new BufferedReader(new InputStreamReader(System.in));
    public void generatePin() throws SQLException{
        p = r.nextInt((9999-1000)+1)+1000; // (max-min)+1 - min
        String ps = "Insert into admin(pin) values(?);";
        Query q = new Query();
        pst= q.con.prepareStatement(ps);
        pst.setInt(1,p); 
        pst.executeUpdate();
    }
    int checkBalance() throws SQLException{
        Query q = new Query();
        String no = "select * from mainbalance;";
        pst = q.con.prepareStatement(no);
        ResultSet rs = pst.executeQuery();
        rs.next();
        return rs.getInt("balance");
    }
    public int givePin(){
        return this.p;
    }
    void createBankAccount() throws IOException, SQLException{
        Query q = new Query();
        int pin = r.nextInt((9999-1000)+1)+1000;
        int ano = r.nextInt((999999999-100000000)+1)+100000000;
        System.out.println("Enter the name of Account holder:");
        String name=b.readLine();
        System.out.println("Enter the balance that he wants to deposit in the bank:");
        int bal = Integer.parseInt(b.readLine());
        String qr = "Insert into users values(?,?,?,?);";
        PreparedStatement ps = q.con.prepareStatement(qr);
        ps.setString(1,name);
        ps.setInt(2,ano);
        ps.setInt(3,pin);
        ps.setInt(4,bal);
        ps.executeUpdate();
        System.out.println("User Account Details\n"+"-----------------------------------\n"+
                "Name:\t"+name+"\n"+
                "Acno:\t"+ano+"\n"+
                "Pin\t:"+pin+"\n"+
                "Balance\t:"+bal);
    }
    public void deleteAdmin() throws SQLException{
        Query q = new Query();
        String hs = "Delete from admin;";
        pst = q.con.prepareStatement(hs);
        pst.executeUpdate();
    }
    void cashLoading() throws SQLException{
        String one = "update mainbalance set balance = balance+?";
        Query q = new Query();
        PreparedStatement a = q.con.prepareStatement(one);
        a.setInt(1,10000);
        a.executeUpdate();
    }
}

//Main class 
public class AtmSimulation {
    public static void main(String[] args) throws SQLException, IOException, Exception {
        Admin a = new Admin();
        a.deleteAdmin(); // Ensuring whether no other admin pin is in the database
        a.generatePin();// Generating a new admin pin for the atm when it is on
        BufferedReader b = new BufferedReader(new InputStreamReader(System.in));
        Query q = new Query();
        int choice = 1;
        int h;
        while(choice == 1){
            System.out.println("               Welcome to Tatikonda ATM\n"+
                    "           -------------------------------------------\n"+
                    "Please insert your card and\n" +
                    "Type 7 for User and 8 for Admin ");
            h = Integer.parseInt(b.readLine());
            if(h == 7){
                System.out.println("Hello Sir");
                User u = new User();
               
               if( u.initializeVariables()){
                    System.out.println("Press 1 for Balance Enquiry\n"+"Press 2 for Withdrawal\n"+"Press 3 for MiniStatement\n"+"Press 4 for the change of the pin\n"+"Press 5 for Inter Bank Transfer\n"+"Press 6 for Deposition\n");
                    int option = Integer.parseInt(b.readLine());
                    switch(option){
                        case 1:
                            u.checkBalance();
                            break;
                        case 2:
                            u.withdraw();
                            u.withdrawFromAdmin();
                            break;
                        case 3:
                            u.miniStatement();
                            break;
                        case 4:
                            u.changePin();
                            break;
                        case 5:    
                            u.interBank();
                            break;
                        case 6:
                            u.deposit();
                            break;
                        default:
                            System.out.println("Please check the options");
                            break;
                    }        
                }
                System.out.println("Want to do another transaction?? Press 1 otherwise press 0");
                choice = Integer.parseInt(b.readLine());
            }
            else if(h == 8){
                System.out.println("If you are a Admin enter your Unique Password:");
                int adpin = Integer.parseInt(b.readLine());
                int ap = a.givePin();
                if(ap == adpin){
                    System.out.println("          Welcome Admin\n"+"Press 1 for checking the atmbalance\n"+"Press 2 for Cash Loading\n"+"Press 3 for opening an account");
                    int option = Integer.parseInt(b.readLine());
                    switch(option){
                        case 1:
                            System.out.println("Balance in the atm : Rs "+ a.checkBalance());
                            break;
                        case 2:
                            a.cashLoading();
                            System.out.println("Cash Loading Successful\n"+"New balance : Rs"+ a.checkBalance());
                            break;
                        case 3:
                            a.createBankAccount();
                            break;
                    }
                    
                   
                }
                else{ 
                    System.out.println("You are not admin this incident will be reported");
                    //choice =0;
                }
                System.out.println("Want to do another transaction?? Press 1 otherwise press 0");
                choice = Integer.parseInt(b.readLine());
            }
        }
    }
}
//End of the program 
//Author@TSSP