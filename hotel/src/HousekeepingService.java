import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class HousekeepingService {

    public void checkHousekeeping() {
        System.out.println("\n[하우스키핑 확인]");

        try (Connection connection = DatabaseManager.getConnection()) {
            String sql = "SELECT RoomID, ServiceDate, ServiceType FROM Service";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int roomId = resultSet.getInt("RoomID");
                String serviceDate = resultSet.getString("ServiceDate");
                String serviceType = resultSet.getString("ServiceType");
                System.out.printf("Room ID: %d, Date: %s, Service: %s\n", roomId, serviceDate, serviceType);
            }
        } catch (Exception e) {
            System.out.println("오류 발생: 하우스키핑 정보를 가져올 수 없습니다.");
            e.printStackTrace();
        }
    }
}
