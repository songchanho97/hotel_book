import java.sql.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);

        Connection conn = null;
        PreparedStatement pstmt1 = null;
        PreparedStatement pstmt2 = null;
        ResultSet rs = null;
        Savepoint savepoint = null;


        String user = "dbdb2002";
        String password = "dbdb!2002";
        String DB_URL = "jdbc:postgresql://localhost:5432/sample2002";

        try{
            Class.forName("org.postgresql.Driver");
            System.out.println("클래스 로딩 성공");

            conn = DriverManager.getConnection(DB_URL, user, password);
            conn.setAutoCommit(false); // 자동커밋해제

            System.out.println("송금계좌를 입력하세요: ");
            int send_id = sc.nextInt();

            System.out.println("수신계좌를 입력하세요");
            int get_id = sc.nextInt();

            System.out.println("이체 금액을 입력하세요");
            int transfer = sc.nextInt();


            // 1. 송금 계좌 처리
            String sql1 = "select balance from accounts where account_id = ?";
            pstmt1 = conn.prepareStatement(sql1);
            pstmt1.setInt(1, send_id);
            ResultSet res1 = pstmt1.executeQuery();

            if(res1.next()){
                int sendBalance = res1.getInt("balance");

                // 송금 계좌 잔액 확인
                if (sendBalance < transfer){
                    throw new IllegalArgumentException("송금 계좌 잔액 부족");
                }
                // 송금 계좌 금액 차감
                StringBuffer updateSql1 = new StringBuffer();
                updateSql1.append("UPDATE accounts SET balance = balance - ? WHERE account_id = ?");
                PreparedStatement updateStmt1 = conn.prepareStatement(updateSql1.toString());
                updateStmt1.setInt(1, transfer);
                updateStmt1.setInt(2, send_id);
                updateStmt1.executeUpdate();

                // 송금 계좌 작업 후 SAVEPOINT 설정
                savepoint = conn.setSavepoint("AfterDeduct");

            } else {
                throw new IllegalArgumentException("송금 계좌를 찾을 수 없음");
            }

            // 수신 계좌 작업
            StringBuffer sql2 = new StringBuffer();
            sql2.append("select balance from accounts where account_id = ?");
            pstmt2 = conn.prepareStatement(sql2.toString());
            pstmt2.setInt(1, get_id);
            ResultSet res2 = pstmt2.executeQuery();

            if(res2.next()){
                // 수신 계좌 금액 증가
                StringBuffer updateSql2 = new StringBuffer();
                updateSql2.append("UPDATE accounts SET balance = balance + ? WHERE account_id = ?");
                PreparedStatement updateStmt2 = conn.prepareStatement(updateSql2.toString());
                updateStmt2.setInt(1, transfer);
                updateStmt2.setInt(2, get_id);
                updateStmt2.executeUpdate();
            }else{
                // 수신 계좌가 없을 경우, SAVEPOINT로 롤백
                System.out.println("수신 계좌를 찾을 수 없음. ");
                if(savepoint != null){
                    conn.rollback(savepoint); // savepoint 롤백
                    System.out.println("송금 계좌 금액 차감 작업 취소");
                }
                throw new IllegalArgumentException("수신 계좌를 찾을 수 없음");
            }


//            int result = pstmt.executeUpdate(); // insert, update, delete
//            System.out.println(result + "행이 삽입되었습니다.");

            // 3. 트랜잭션 커밋
            conn.commit(); // 커밋
            System.out.println("이체 성공!");
            conn.setAutoCommit(true);  //자동커밋 설정

            // 최종 잔액 출력
            // 송금 계좌 최종 잔액
            ResultSet finalRes1 = pstmt1.executeQuery();
            if (finalRes1.next()) {
                System.out.println("송금 계좌 " + send_id + "번의 최종 잔액: " + finalRes1.getInt("balance"));
            }

            // 수신 계좌 최종 잔액
            ResultSet finalRes2 = pstmt2.executeQuery();
            if (finalRes2.next()) {
                System.out.println("수신 계좌 " + get_id + "번의 최종 잔액: " + finalRes2.getInt("balance"));
            }


        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }catch (SQLException e){
            e.printStackTrace();
            try{
                conn.rollback(); // 롤백
                System.out.println("전체 작업이 취소되었습니다.");
            }catch (SQLException e1){
                e1.printStackTrace();
            }
        }finally {
            if(rs != null){
                try{
                    rs.close();
                }catch (SQLException e){
                    e.printStackTrace();
                }
            }
            if(pstmt1 != null){
                try{
                    pstmt1.close();
                }catch (SQLException e){
                    e.printStackTrace();
                }
            }
            if(pstmt2 != null){
                try{
                    pstmt2.close();
                }catch (SQLException e){
                    e.printStackTrace();
                }
            }
            if(conn != null){
                try{
                    conn.close();
                }catch (SQLException e){
                    e.printStackTrace();
                }
            }
        }
    }
}