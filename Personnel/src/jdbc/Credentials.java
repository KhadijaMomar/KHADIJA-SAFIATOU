package jdbc;

public class Credentials {
    public static String getUrl() {
        return "jdbc:mysql://localhost:3306/gestion_personnel?serverTimezone=UTC";
    }

    public static String getUser() {
        return "gestion_user"; 
    }

    public static String getPassword() {
        return "password"; 
    }

    public static String getDriverClassName() {
        return "com.mysql.cj.jdbc.Driver";
    }
}