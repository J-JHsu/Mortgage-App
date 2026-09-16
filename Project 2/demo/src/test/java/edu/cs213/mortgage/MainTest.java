package edu.cs213.mortgage;

import edu.cs213.mortgage.config.DatabaseCon;
import edu.cs213.mortgage.service.MortgageService;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"DB_URL=", "DB_USERNAME=", "DB_PASSWORD="})
class MainTest {
    @LocalServerPort
    private int port;
    @Autowired
    private MortgageService service;
    @Autowired
    private DatabaseCon database;

    @Test
    void webServerStartsWithoutDatabaseButOperationsFailClearly() {
        assertTrue(port > 0);
        assertNotNull(service);
        assertTrue(assertThrows(SQLException.class, database::connect).getMessage().contains("DB_USERNAME"));
        assertThrows(SQLException.class, () -> service.search(List.of()));
    }
}
