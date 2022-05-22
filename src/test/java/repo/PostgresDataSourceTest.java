package repo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class PostgresDataSourceTest {

    @Autowired
    PostgresDataSource postgresDataSource;

    @Test
    void deleteUserByName() throws SQLException {
        postgresDataSource.deleteUserByName("1234");
    }
}