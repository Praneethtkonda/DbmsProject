package classwithqueries;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

class Query{
    Connection con = null;
    Statement st = null;
    ResultSet rs = null;
    public Query() throws SQLException{
         con = DriverManager.getConnection("jdbc:postgresql://localhost/postgres","postgres", "asdf");
        // System.out.println("Opened database successfully");
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
class ClassWithQueries {
    Query q;
    String one = "create table admin (pin numeric(5));";
    String two = "create table users(\n" +
"	name varchar(30),\n" +
"	acno numeric(11) unique,\n" +
"	pin  numeric(5),\n" +
"	balance numeric(5)\n" +
");";
    
    String three = "create table mainbalance(\n"
            + "balance numeric(5) unique"+");";

    ClassWithQueries() throws SQLException {
        this.q = new Query();
    }
    void executeOne() throws SQLException{
        PreparedStatement ps = q.con.prepareStatement(one);
        ps.executeUpdate();
    }
    void executeTwo() throws SQLException{
        PreparedStatement ps = q.con.prepareStatement(two);
        ps.executeUpdate();
    }
    void executeThree() throws SQLException{
        PreparedStatement ps = q.con.prepareStatement(three);
        ps.executeUpdate();
    }
     public void initializeBalance() throws SQLException{
        String ps = "Insert into mainbalance values(?)";
        PreparedStatement pst = q.con.prepareStatement(ps);
        int balance=10000;
        pst.setInt(1,balance);// ParameterIndex,variable 2 for atmbalance
        pst.executeUpdate();
    }
    public static void main(String []args) throws SQLException{
        ClassWithQueries obj = new ClassWithQueries();
        obj.executeOne();
        obj.executeTwo();
        obj.executeThree();
        obj.initializeBalance();
    }
}