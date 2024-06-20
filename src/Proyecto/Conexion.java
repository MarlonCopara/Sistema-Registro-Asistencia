package Proyecto;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import javax.swing.JOptionPane;


public class Conexion {

    Connection connect = null;
    private static Conexion instance;

    public static Conexion getInstance() {
        if (instance == null) {
            instance = new Conexion();
        }
        return instance;
    }

    public Connection conexion() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connect = DriverManager.getConnection("jdbc:mysql://www.db4free.net:3306/proyectovisual", "proyectovisual", "proyectovisual");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error....." + ex);
        }
        return connect;
    }
}
