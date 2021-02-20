package calc;

import javafx.scene.control.TableView;

import java.sql.*;

import static calc.Main.connSQL;

// Работа с БД SQL
public class KSQL {
    private String DBName;

    public int ksqlDELETE(String query) {  // Выполняем запрос DELETE
        int rows = 0;
        if(query.equals("")) {
            return -2;
        }
        try {
            ksqlConnect();  // Если не делали, то сделаем.
            PreparedStatement statement = connSQL.prepareStatement(query);
    //        System.out.println("DEL1 " + query);

            rows = statement.executeUpdate();
            return rows;
        }
        catch (SQLSyntaxErrorException e) {
            if(e.getSQLState().equals("42S02")) { // Нет таблицы
                System.out.println("Не найдена таблица ("+e.getMessage()+ ")");
            }
            if(e.getSQLState().equals("42000")) { // Нет DB
                System.out.println("На сервере не найдена БД (" + e.getSQLState() + ")");
            }
        }
/*        catch (CommunicationsException e) {
            System.out.println("Нет соединения с сервером БД (" + e.getSQLState() + ")");
        }*/
        catch (SQLException e){
            System.out.println("e1= " + e.getMessage());
        }
//        System.out.println("query is ready");
        return -1;
    }

    public long ksqlINSERT(String query) {  // Выполняем запрос INSERT. Возвращаем id добавленной записи
        int rows = 0;
        if(query.equals("")) {
            return -2;
        }
        try {
            ksqlConnect();  // Если не делали, то сделаем.
            PreparedStatement statement = connSQL.prepareStatement(query);
            rows = statement.executeUpdate();
            if(rows > 0) {
                statement = connSQL.prepareStatement("CALL IDENTITY();");
                ResultSet rs = statement.executeQuery();
                rs.next();
                return rs.getLong("@p0");
            }
            return -1;
        }
        catch (SQLSyntaxErrorException e) {
            if(e.getSQLState().equals("42S02")) { // Нет таблицы
                System.out.println("Не найдена таблица ("+e.getMessage()+ ")");
            }
            if(e.getSQLState().equals("42000")) { // Нет DB
                System.out.println("На сервере не найдена БД (" + e.getSQLState() + ")");
            }
        }
/*        catch (CommunicationsException e) {
            System.out.println("Нет соединения с сервером БД (" + e.getSQLState() + ")");
        }*/
        catch (SQLException e){
          //  System.out.println("e2= " + e.getMessage());
            System.out.println("e2= " + e);
        }
//        System.out.println("query is ready");
        return -1;
    }

    public ResultSet ksqlSELECT(String query) {  // Выполняем запрос SELECT и возвращаем ResultSet
        if(query.equals("")) {
            return null;
        }
        try {
            if (!ksqlConnect()) return null;  // Если не делали, то сделаем.
            PreparedStatement selectStatement = connSQL.prepareStatement(query);
            ResultSet rs = selectStatement.executeQuery();
            return rs;
        }
        catch (SQLSyntaxErrorException e) {
                if(e.getSQLState().equals("42S02")) { // Нет таблицы
                    System.out.println("Не найдена таблица ("+e.getMessage()+ ")");
                }
                if(e.getSQLState().equals("42000")) { // Нет DB
                    System.out.println("На сервере не найдена БД (" + e.getSQLState() + ")");
                }
        }
/*        catch (CommunicationsException e) {
            System.out.println("Нет соединения с сервером БД (" + e.getSQLState() + ")");
        }*/
            catch (SQLException e){
                System.out.println("e3= " + e.getMessage());
            }
//        System.out.println("query is ready");
        return null;
    }

    public KSQL() {  //
        // sqlConnect();  // ВОзможно сразу коннектиться. А может и нет. Потом посмотрим
    }
    public boolean ksqlConnect() {  // Соединяемся с сервером и нужной БД
       if(connSQL == null) { // Еще нет коннекта - создаем. connSQL - глобальная
            try {
                connSQL = DriverManager
                        .getConnection("jdbc:hsqldb:file:/d:\\_work\\ilia\\_java\\calc_eq\\docs\\db\\cached;ifexists=true",
                                "user", "111");
            }
            catch (SQLSyntaxErrorException e) {
                if(e.getSQLState().equals("42S02")) { // Нет таблицы
                    System.out.println("Не найдена таблица ("+e.getMessage()+ ")");
                }
                if(e.getSQLState().equals("42000")) { // Нет DB
                    System.out.println("На сервере не найдена БД (" + e.getSQLState() + ")");
                }
            }
//        catch (CommunicationsException e) {
  //          System.out.println("Нет соединения с сервером БД (" + e.getSQLState() + ")");
//        }
            catch (SQLException e) {
                if(e.getSQLState().equals("S1000")) { // БД занята другим приложением
                    System.out.println("БД занята другим приложением ("+e.getMessage()+ ")");
                    return false;
                } else {
                    System.out.println("e4= " + e.getSQLState() + " " + e.getMessage());
                }
            }
        }
        return true;
    }

    public void ksqlClose() { // Закрываем коннект
        if(connSQL != null) {
            try {
                connSQL.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }
}
