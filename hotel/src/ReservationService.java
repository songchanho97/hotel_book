import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ReservationService {

    public void checkReservedRooms() {
        System.out.println("\n[현재 예약된 방 확인]");

        String sql =
                "SELECT " +
                        "   res.RoomID, " +
                        "   rt.Description AS RoomType, " +
                        "   rm.RoomNumber, " +
                        "   h.Name AS HotelName, " +
                        "   h.City, " +
                        "   h.Address, " +
                        "   res.CheckInDate, " +
                        "   res.CheckOutDate " +
                        "FROM Reservation res " +
                        "JOIN Room rm ON res.RoomID = rm.RoomID " +
                        "JOIN RoomType rt ON rm.RoomTypeID = rt.RoomTypeID " +
                        "JOIN Hotel h ON rm.HotelID = h.HotelID";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                int roomId = resultSet.getInt("RoomID");
                String roomType = resultSet.getString("RoomType");
                String roomNumber = resultSet.getString("RoomNumber");
                String hotelName = resultSet.getString("HotelName");
                String city = resultSet.getString("City");
                String address = resultSet.getString("Address");
                String checkInDate = resultSet.getString("CheckInDate");
                String checkOutDate = resultSet.getString("CheckOutDate");

                System.out.printf(
                        "Room ID: %d\n" +
                                "Room Type: %s\n" +
                                "Room Number: %s\n" +
                                "Hotel: %s\n" +
                                "Location: %s, %s\n" +
                                "Check-In: %s\n" +
                                "Check-Out: %s\n" +
                                "-----------------------------------\n",
                        roomId, roomType, roomNumber, hotelName, city, address, checkInDate, checkOutDate
                );
            }

        } catch (Exception e) {
            System.out.println("오류 발생: 예약 정보를 가져올 수 없습니다.");
            e.printStackTrace(); // 예외 세부 정보 출력
        }
    }
}
