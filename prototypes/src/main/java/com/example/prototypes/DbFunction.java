package com.example.prototypes;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DbFunction {

    /* function needed to get from database
    *  current point from user_points table
    * voucher from voucher id
    * claim voucher
    * points deduction and points addition
    * display transaction
    *
    * */
    public Connection connect_to_db(String dbname, String username, String password) {
        Connection conn = null;
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/" + dbname, username, password);
            if (conn != null) {
                System.out.println("Connection established");
            } else {
                System.out.println("Connection failed");
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return conn;
    }


    // consider if still nak kena guna the tablename and id since it will all be the same table and id
    public String read_points(Connection conn, String table_name, int user_id) {
        Statement statement;
        ResultSet rs = null;
        try {
            String query = String.format("select * from %s where user_id = '%s' ", table_name, user_id);
            statement = conn.createStatement();
            rs = statement.executeQuery(query);
            while (rs.next()) {
                System.out.println(rs.getInt("current_points"));
                return String.valueOf(rs.getInt("current_points"));
            }

        } catch (Exception e) {
            System.out.println(e);
        }
        return "0";
    }

    public List<Voucher> read_voucher (Connection conn) {
        List<Voucher> vouchers = new ArrayList<>();
        Statement statement;
        ResultSet rs = null;
        try {
            String query = "SELECT reward, points_needed, company, logo_path FROM voucher";
            statement = conn.createStatement();
            rs = statement.executeQuery(query);
            while (rs.next()) {
                String reward = rs.getString("reward");
                int points_needed = rs.getInt("points_needed");
                String company = rs.getString("company");
                String logo_path = rs.getString("logo_path");

                vouchers.add(new Voucher(reward,points_needed,company,logo_path));
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return vouchers;
    }
    public List<Transaction> read_transaction (Connection conn) {
        List<Transaction> transactions = new ArrayList<>();
        Statement statement;
        ResultSet rs = null;
        try {
            String query = "SELECT * FROM transaction WHERE user_id = 1 ORDER BY transaction_date DESC LIMIT 5";
            statement = conn.createStatement();
            rs = statement.executeQuery(query);
            while (rs.next()) {
                Date date = rs.getDate("transaction_date");
                String name = rs.getString("transaction_name");
                int points_earned = rs.getInt("points_earned");
                int points_spent = rs.getInt("points_spent");

                System.out.println(date + name + points_earned + points_spent);

                transactions.add(new Transaction(date, name, points_earned, points_spent));
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return transactions;
    }

    public void createTable(Connection conn, String table_name) {
        Statement statement;
        try {
            String query = "create table " + table_name + "(empid SERIAL, name varchar(200), address varchar(200), primary key(empid));";
            statement = conn.createStatement();
            statement.executeUpdate(query);
            System.out.println("Table Created");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void insert_row(Connection conn, String tablename, String name, String address) {
        Statement statement;
        try {
            String query = String.format("insert into %s(name, address) values('%s', '%s');", tablename, name, address);
            statement = conn.createStatement();
            statement.executeUpdate(query);
            System.out.println("Row inserted");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void read_data(Connection conn, String tablename) {
        Statement statement;
        ResultSet rs = null;
        try {
            String query = String.format("select * from %s", tablename);
            statement = conn.createStatement();
            rs = statement.executeQuery(query);
            while (rs.next()) {
                System.out.print(rs.getString("empid") + " ");
                System.out.print(rs.getString("name") + " ");
                System.out.println(rs.getString("address") + " ");
            }

        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public void update_name(Connection conn, String tablename, String old_name, String new_name) {
        Statement statement;
        try {
            String query = String.format("update %s set name = '%s' where name = '%s'", tablename, new_name, old_name);
            statement = conn.createStatement();
            statement.executeUpdate(query);
            System.out.println("Data updated");
        } catch (Exception e) {


        }

        String userPoint = "SELECT current_points FROM user_points WHERE user_id = ?";
        String voucherPoint = "SELECT points_needed FROM voucher WHERE voucher_id = ?";
        String insertTrans = "INSERT INTO transaction (user_id,transaction_date, transaction_time, points_earned, points_spent, transaction_name) VALUES (?,?,?, ?, ?, ?)";


    }

    public void search_by_name(Connection conn, String tablename, String name) {
    Statement statement;
    ResultSet rs = null;

        try {
            String query = String.format("select * from %s where name = '%s' ", tablename, name);
            statement = conn.createStatement();
            rs = statement.executeQuery(query);
            while (rs.next()){
                System.out.print(rs.getString("empid") + " ");
                System.out.print(rs.getString("name") + " ");
                System.out.println(rs.getString("address") + " ");
            conn.setAutoCommit(false);

            int currentPoints;
            try (PreparedStatement userStmt = conn.prepareStatement(userPoint)) {
                userStmt.setInt(1, userId);
                try (ResultSet rs = userStmt.executeQuery()) {
                    if (rs.next()) {
                        currentPoints = rs.getInt("current_points");
                    } else {
                        System.out.println("User not found");
                        return false;
                    }
                }
            }

            int voucherPoints;
            try (PreparedStatement voucherStmt = conn.prepareStatement(voucherPoint)) {

                voucherStmt.setInt(1, voucherId);
                try (ResultSet rs = voucherStmt.executeQuery()) {
                    if (rs.next()) {
                        voucherPoints = rs.getInt("points_needed");
                    } else {
                        System.out.println("Voucher not found");
                        return false;
                    }
                }
            }

            if (currentPoints >= voucherPoints) {
                try (PreparedStatement insertStmt = conn.prepareStatement(insertTrans)) {
                    insertStmt.setInt(1, userId);
                    insertStmt.setDate(2, currentDate);// User ID
                    insertStmt.setTimestamp(3, currentTime); // Transaction date and time
                    insertStmt.setInt(4, 0);            // Points earned
                    insertStmt.setInt(5, voucherPoints);             // Points spent
                    insertStmt.setString(6, "Voucher Redeemed"); // Transaction name
                    insertStmt.executeUpdate();
                }
                conn.commit(); // Commit transaction
                success = true;
                System.out.println("Voucher redeemed successfully!");
            } else {
                System.out.println("Not enough points");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback transaction on error
                    System.out.println("Transaction rolled back.");
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }

        } finally {
            try {
                conn.setAutoCommit(true); // Reset auto-commit
            } catch (SQLException resetEx) {
                resetEx.printStackTrace();
            }
        } catch(Exception e) {
            System.out.println(e);
        }

    }
    public void search_by_id(Connection conn, String tablename, int id) {
        Statement statement;
        ResultSet rs = null;

        try {
            String query = String.format("select * from %s where empid = %s ", tablename, id);
            statement = conn.createStatement();
            rs = statement.executeQuery(query);
            while (rs.next()){
                System.out.print(rs.getString("empid") + " ");
                System.out.print(rs.getString("name") + " ");
                System.out.println(rs.getString("address") + " ");
            }
        } catch(Exception e) {
            System.out.println(e);
        }

    }

    public void delete_row (Connection conn, String tablename, String name){
        Statement statement;
        try{
            String query = String.format("delete from %s where name = '%s'", tablename, name);
            statement = conn.createStatement();
            statement.executeUpdate(query);
            System.out.println("Data deleted");
        } catch(Exception e){
            System.out.println(e);
        }
    }

}
