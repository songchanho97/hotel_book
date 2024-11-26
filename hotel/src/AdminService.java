import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class AdminService {

    public void adminLogin(Scanner scanner) {
        System.out.println("관리자 로그인을 선택했습니다.");

        System.out.print("관리자 이름(AdminName): ");
        String adminName = scanner.nextLine();

        System.out.print("관리자 비밀번호(AdminPW): ");
        String adminPW = scanner.nextLine();

        try (Connection connection = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM Admin WHERE AdminName = ? AND AdminPW = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, adminName);
            preparedStatement.setString(2, adminPW);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                System.out.println("관리자 로그인 성공");
                showAdminOptions(scanner);
            } else {
                System.out.println("로그인 실패: 관리자 이름 또는 비밀번호가 잘못되었습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAdminOptions(Scanner scanner) {
        ReservationService reservationService = new ReservationService();
        HousekeepingService housekeepingService = new HousekeepingService();

        while (true) {
            System.out.println("\n관리자 작업을 선택하세요:");
            System.out.println("1. 현재 예약된 방 확인하기");
            System.out.println("2. 하우스키핑 확인하기");
            System.out.println("3. 로그아웃");
            System.out.print("선택: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    reservationService.checkReservedRooms();
                    break;
                case 2:
                    housekeepingService.checkHousekeeping();
                    break;
                case 3:
                    System.out.println("관리자 로그아웃. 메인 메뉴로 돌아갑니다.");
                    return;
                default:
                    System.out.println("잘못된 입력입니다. 다시 선택해주세요.");
            }
        }
    }
}
