import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Savepoint;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        String user = "dbdb2002";
        String password = "dbdb!2002";
        String DB_URL = "jdbc:postgresql://localhost:5432/sample2002";

        try (Connection conn = DriverManager.getConnection(DB_URL, user, password)) {
            System.out.println("클래스 로딩 성공");
            TransactionManager txManager = new TransactionManager(conn);
            AccountService accountService = new AccountService(conn);

            System.out.print("송금계좌를 입력하세요: ");
            int sendId = sc.nextInt();

            System.out.print("수신계좌를 입력하세요: ");
            int getId = sc.nextInt();

            System.out.print("이체 금액을 입력하세요: ");
            int transfer = sc.nextInt();

            // 트랜잭션 시작
            txManager.beginTransaction();
            Savepoint savepoint = null;

            try {
                // 송금 계좌 잔액 확인 및 금액 차감
                int sendBalance = accountService.getBalance(sendId);
                if (sendBalance < transfer) {
                    throw new IllegalArgumentException("송금 계좌 잔액 부족");
                }
                accountService.updateBalance(sendId, -transfer);
                savepoint = txManager.setSavepoint("AfterDeduct");

                boolean success = false;

                while (!success) {
                    try{
                        accountService.getBalance(getId); // 수신 계좌 존재 여부 확인
                        accountService.updateBalance(getId, transfer); // 수신 계좌 금액 증가
                        success = true;
                    }catch (Exception e){
                        txManager.rollbackToSavepoint(savepoint); // savepoint로 이동

                        System.out.print("수신계좌를 다시 입력하세요: ");
                        getId = sc.nextInt();

                        System.out.print("이체금액을 다시 입력하세요: ");
                        transfer = sc.nextInt();
                    }
                }


                // 트랜잭션 커밋
                txManager.commitTransaction();
                System.out.println("이체 성공!");

                // 최종 잔액 출력
                System.out.println("송금 계좌 " + sendId + "번 최종 잔액: " + accountService.getBalance(sendId));
                System.out.println("수신 계좌 " + getId + "번 최종 잔액: " + accountService.getBalance(getId));
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                txManager.rollbackTransaction();
                System.out.println("전체 작업이 롤백되었습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
