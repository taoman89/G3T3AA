/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aa;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author danisg
 */
public class DbBean implements Serializable{

    // change the dbURL if necessary.
    private static String dbDriver = "com.mysql.jdbc.Driver";
    private static Connection dbConnection;
//    static String dbURL1 = null;
//    static String dbUser1 = null;
//    static String dbPassword1 = null;
    
    private final static int TIME_OUT = 3;
    
    static String dbURLA = "jdbc:mysql://192.168.1.147:7000/exchange";
    static String dbUserA = "Exchange";
    static String dbPasswordA = "Exchange";
    
    static String dbURLB = "jdbc:mysql://localhost:3306/exchange";
    static String dbUserB = "root";
    static String dbPasswordB = "";
    
    //Read JDBC parameters from web.xml
    
    private static boolean connect(String dbURL, String dbUser, String dbPassword) throws ClassNotFoundException, SQLException, NamingException {

        if (dbConnection == null || dbConnection.isClosed()) {
            
            // connects to the database using root. change your database id/password here if necessary    
//            Context env = (Context) new InitialContext().lookup("java:comp/env");
//            dbURL1 = (String) env.lookup("dbURL");
//            dbUser1 = (String) env.lookup("dbUser");
//            dbPassword1 = (String) env.lookup("dbPassword");

        
            Class.forName(dbDriver);
            // login credentials to your MySQL server
            dbConnection = DriverManager.getConnection(dbURL, dbUser, dbPassword);
            return true;
        } else {
            return true;
        }

    }
    
    
    
    
    /*
     * Execute the given sql and returns the resultset.
     */

    public static ResultSet executeSql(String sql) throws ClassNotFoundException, SQLException, NamingException {
        //check the connection
//        if (!connect(dbURLB, dbUserB, dbPasswordB) /*&& !connect(dbURL1, dbUser1, dbPassword1)*/) {
//            return null;
//        }
        
//        try {
//            boolean status = connect(dbURLA, dbUserA, dbPasswordA);
//        } catch (Exception e) {
//            try {
//                boolean status = connect(dbURLB, dbUserB, dbPasswordB);
//            } catch (Exception ee) {
//                return null;
//            }
//        }
        
        ExecutorService service = Executors.newSingleThreadExecutor();
        
        try {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    try {
                        connect(dbURLA, dbUserA, dbPasswordA);
                    } catch (Exception e) {
                        
                    }
                }
            };
            
            Future<?> f = service.submit(r);
            
            f.get(TIME_OUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            
        } catch (TimeoutException e) {
            try {
                connect(dbURLB, dbUserB, dbPasswordB);
            } catch (Exception ee) {
                return null;
            }
        } catch (ExecutionException e) {
            
        }

        //execute sql.
        Statement dbStatement = dbConnection.createStatement();
        return dbStatement.executeQuery(sql);
    }
    /*
     * Execute the update statement and returns the # of affected rows.
     */

    public static int executeUpdate(String sql) throws SQLException, ClassNotFoundException, NamingException {
        //check the connection.
//        if (!connect(dbURLA, dbUserA, dbPasswordA) && !connect(dbURLB, dbUserB, dbPasswordB) /*&& !connect(dbURL1, dbUser1, dbPassword1)*/) {
//            return 0;
//        }
        
//        try {
//            boolean status = connect(dbURLA, dbUserA, dbPasswordA);
//        } catch (Exception e) {
//            try {
//                boolean status = connect(dbURLB, dbUserB, dbPasswordB);
//            } catch (Exception ee) {
//                return 0;
//            }
//        }
        
        ExecutorService service = Executors.newSingleThreadExecutor();
        
        try {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    try {
                        connect(dbURLA, dbUserA, dbPasswordA);
                    } catch (Exception e) {
                        
                    }
                }
            };
            
            Future<?> f = service.submit(r);
            
            f.get(TIME_OUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            
        } catch (TimeoutException e) {
            try {
                connect(dbURLB, dbUserB, dbPasswordB);
            } catch (Exception ee) {
                return 0;
            }
        } catch (ExecutionException e) {
            
        }

        //execute the update.
        Statement dbStatement = dbConnection.createStatement();
        return dbStatement.executeUpdate(sql);
    }
    /*
     * Close the connection.
     */

    public static void close() throws SQLException {
        if (dbConnection != null && !dbConnection.isClosed()) {
            dbConnection.close();
        }
    }
}
