import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class GuestService {

    // 게스트 로그인 메서드
    public void guestLogin(Scanner scanner) {
        System.out.println("게스트 로그인을 선택했습니다.");

        // 게스트 이름과 비밀번호 입력받기
        System.out.print("게스트 이름(GuestName): ");
        String guestName = scanner.nextLine();

        System.out.print("게스트 비밀번호(GuestPW): ");
        String guestPW = scanner.nextLine();

        // 게스트 인증
        try (Connection connection = DatabaseManager.getConnection()) {
            String sql = "SELECT GuestID FROM Guest WHERE GuestName = ? AND GuestPW = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, guestName);
            preparedStatement.setString(2, guestPW);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int guestId = resultSet.getInt("GuestID");
                System.out.println("게스트 로그인 성공!");
                guestOptions(guestId, scanner);
            } else {
                System.out.println("로그인 실패: 게스트 이름 또는 비밀번호가 잘못되었습니다.");
            }
        } catch (Exception e) {
            System.out.println("오류 발생: 게스트 로그인을 처리할 수 없습니다.");
            e.printStackTrace();
        }
    }

    // 게스트 작업 메뉴
    public void guestOptions(int guestId, Scanner scanner) {
        while (true) {
            System.out.println("\n게스트 작업을 선택하세요:");
            System.out.println("1. 가능한 숙소 확인하기");
            System.out.println("2. 전체 비용 계산하기");
            System.out.println("3. 내가 예약한 숙소 내역");
            System.out.println("4. 로그아웃");
            System.out.print("선택: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // 버퍼 비우기

            switch (choice) {
                case 1:
                    checkAvailableRooms(scanner);
                    break;
                case 2:
                    calculateTotalCost(guestId);
                    break;
                case 3:
                    showMyReservations(guestId);
                    break;
                case 4:
                    System.out.println("로그아웃되었습니다. 메인 메뉴로 돌아갑니다.");
                    return;
                default:
                    System.out.println("잘못된 입력입니다. 다시 선택해주세요.");
            }
        }
    }


        public void checkAvailableRooms(Scanner scanner) {
            System.out.println("\n[가능한 숙소 확인하기]");
            String sql =
                    "SELECT rm.RoomID, rt.Description AS RoomType, rt.BasePrice, rt.PeakPrice, h.Name AS HotelName, h.City " +
                            "FROM Room rm " +
                            "JOIN RoomType rt ON rm.RoomTypeID = rt.RoomTypeID " +
                            "JOIN Hotel h ON rm.HotelID = h.HotelID " +
                            "WHERE rm.RoomID NOT IN (SELECT RoomID FROM Reservation)";

            try (Connection connection = DatabaseManager.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(sql);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                // 표 헤더 출력
                System.out.printf("%-10s %-20s %-10s %-10s %-30s %-20s\n",
                        "Room ID", "Room Type", "Base Price", "Peak Price", "Hotel Name", "City");
                System.out.println("---------------------------------------------------------------------------------------------------");

                // 데이터 출력
                while (resultSet.next()) {
                    int roomId = resultSet.getInt("RoomID");
                    String roomType = resultSet.getString("RoomType");
                    double basePrice = resultSet.getDouble("BasePrice");
                    double peakPrice = resultSet.getDouble("PeakPrice");
                    String hotelName = resultSet.getString("HotelName");
                    String city = resultSet.getString("City");

                    System.out.printf("%-10d %-20s %-10.2f %-10.2f %-30s %-20s\n",
                            roomId, roomType, basePrice, peakPrice, hotelName, city);
                }

                // 사용자로부터 Room ID 입력받기
                System.out.println("\n예약하고자 하는 Room ID를 입력해주세요:");
                int selectedRoomId = scanner.nextInt();
                scanner.nextLine(); // 버퍼 비우기

                System.out.printf("Room ID %d를 선택하셨습니다. 예약 프로세스를 진행합니다...\n", selectedRoomId);

                makeReservation(selectedRoomId, guestId);

                // 예약 처리 로직 추가 가능 (예: makeReservation(selectedRoomId))
            } catch (Exception e) {
                System.out.println("오류 발생: 가능한 숙소를 가져올 수 없습니다.");
                e.printStackTrace();
            }
        }



    // 2. 전체 비용 계산하기
    private void calculateTotalCost(int guestId) {
        System.out.println("\n[전체 비용 계산하기]");
        String sql =
                "SELECT SUM(rt.BasePrice) AS TotalCost " +
                        "FROM Reservation res " +
                        "JOIN Room rm ON res.RoomID = rm.RoomID " +
                        "JOIN RoomType rt ON rm.RoomTypeID = rt.RoomTypeID " +
                        "WHERE res.GuestID = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, guestId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                double totalCost = resultSet.getDouble("TotalCost");
                System.out.printf("현재 예약된 숙소의 총 비용: %.2f\n", totalCost);
            } else {
                System.out.println("예약된 숙소가 없습니다.");
            }

        } catch (Exception e) {
            System.out.println("오류 발생: 전체 비용을 계산할 수 없습니다.");
            e.printStackTrace();
        }
    }

    // 3. 내가 예약한 숙소 내역
    private void showMyReservations(int guestId) {
        System.out.println("\n[내가 예약한 숙소 내역]");
        String sql =
                "SELECT res.RoomID, rt.Description AS RoomType, rt.BasePrice, h.Name AS HotelName, h.City, res.CheckInDate, res.CheckOutDate " +
                        "FROM Reservation res " +
                        "JOIN Room rm ON res.RoomID = rm.RoomID " +
                        "JOIN RoomType rt ON rm.RoomTypeID = rt.RoomTypeID " +
                        "JOIN Hotel h ON rm.HotelID = h.HotelID " +
                        "WHERE res.GuestID = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, guestId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int roomId = resultSet.getInt("RoomID");
                String roomType = resultSet.getString("RoomType");
                double basePrice = resultSet.getDouble("BasePrice");
                String hotelName = resultSet.getString("HotelName");
                String city = resultSet.getString("City");
                String checkInDate = resultSet.getString("CheckInDate");
                String checkOutDate = resultSet.getString("CheckOutDate");

                System.out.printf(
                        "Room ID: %d | Room Type: %s | Base Price: %.2f | Hotel: %s | City: %s | Check-In: %s | Check-Out: %s\n",
                        roomId, roomType, basePrice, hotelName, city, checkInDate, checkOutDate
                );
            }

        } catch (Exception e) {
            System.out.println("오류 발생: 예약 내역을 가져올 수 없습니다.");
            e.printStackTrace();
        }
    }

    private void makeReservation(int roomId, int guestId) {
        String sql = "INSERT INTO Reservation (RoomID, GuestID, CheckInDate, CheckOutDate, TotalCost) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, roomId);
            preparedStatement.setInt(2, guestId);
            preparedStatement.setDate(3, java.sql.Date.valueOf("2024-12-01")); // Check-in Date
            preparedStatement.setDate(4, java.sql.Date.valueOf("2024-12-05")); // Check-out Date
            preparedStatement.setDouble(5, 100.00); // Total Cost Example

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("예약이 성공적으로 완료되었습니다!");
            } else {
                System.out.println("예약 실패: 다시 시도해주세요.");
            }
        } catch (Exception e) {
            System.out.println("오류 발생: 예약을 처리할 수 없습니다.");
            e.printStackTrace();
        }
    }
}
