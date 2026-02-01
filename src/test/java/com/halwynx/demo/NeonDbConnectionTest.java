package com.halwynx.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.data.mongodb.autoconfigure.MongoDataAutoConfiguration,org.springframework.boot.data.mongodb.autoconfigure.MongoAutoConfiguration"
})
class NeonDbConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void testNeonDbConnection() throws Exception {
        assertNotNull(dataSource, "DataSource should not be null");

        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection, "Connection should not be null");
            assertFalse(connection.isClosed(), "Connection should be open");

            DatabaseMetaData metaData = connection.getMetaData();
            System.out.println("=== NeonDB Connection Successful ===");
            System.out.println("Database: " + metaData.getDatabaseProductName());
            System.out.println("Version: " + metaData.getDatabaseProductVersion());
            System.out.println("Driver: " + metaData.getDriverName() + " " + metaData.getDriverVersion());
            System.out.println("URL: " + metaData.getURL());
            System.out.println("User: " + metaData.getUserName());
        }
    }

    @Test
    void testSslConnection() throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SHOW ssl")) {

            if (resultSet.next()) {
                String sslStatus = resultSet.getString(1);
                System.out.println("SSL Status: " + sslStatus);
                assertEquals("on", sslStatus, "SSL should be enabled");
            }
        }
    }

    @Test
    void testDatabaseOperations() throws Exception {
        String testTable = "connection_test_" + System.currentTimeMillis();

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            // Create test table
            statement.execute("CREATE TABLE " + testTable + " (id SERIAL PRIMARY KEY, test_value VARCHAR(255))");
            System.out.println("Created test table: " + testTable);

            // Insert test data
            int inserted = statement.executeUpdate(
                "INSERT INTO " + testTable + " (test_value) VALUES ('connection_test')"
            );
            assertEquals(1, inserted, "Should insert 1 row");
            System.out.println("Inserted test data");

            // Read test data
            try (ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM " + testTable)) {
                assertTrue(resultSet.next(), "Should have result");
                int count = resultSet.getInt(1);
                assertTrue(count > 0, "Should have at least one record");
                System.out.println("Record count: " + count);
            }

            // Cleanup
            statement.execute("DROP TABLE " + testTable);
            System.out.println("Dropped test table");

            System.out.println("=== Database CRUD operations work correctly! ===");
        }
    }
}
