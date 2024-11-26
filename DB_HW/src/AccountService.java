import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountService {
    private final Connection conn;

    public AccountService(Connection conn) {
        this.conn = conn;
    }

    // 계좌 잔액 조회
    public int getBalance(int accountId) throws SQLException {
        String sql = "SELECT balance FROM accounts WHERE account_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("balance");
                } else {
                    throw new IllegalArgumentException("계좌를 찾을 수 없습니다: " + accountId);
                }
            }
        }
    }

    // 계좌 잔액 업데이트
    public void updateBalance(int accountId, int amount) throws SQLException {
        String sql = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, amount);
            pstmt.setInt(2, accountId);
            pstmt.executeUpdate();
        }
    }
}

