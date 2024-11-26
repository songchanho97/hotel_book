import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

public class TransactionManager {
    private final Connection conn;

    public TransactionManager(Connection conn) {
        this.conn = conn;
    }

    // 트랜잭션 시작
    public void beginTransaction() throws SQLException {
        conn.setAutoCommit(false);
    }

    // 트랜잭션 커밋
    public void commitTransaction() throws SQLException {
        conn.commit();
        conn.setAutoCommit(true);
    }

    // 트랜잭션 롤백
    public void rollbackTransaction() throws SQLException {
        conn.rollback();
    }

    // SAVEPOINT 설정
    public Savepoint setSavepoint(String name) throws SQLException {
        return conn.setSavepoint(name);
    }

    // SAVEPOINT로 롤백
    public void rollbackToSavepoint(Savepoint savepoint) throws SQLException {
        conn.rollback(savepoint);
    }
}
