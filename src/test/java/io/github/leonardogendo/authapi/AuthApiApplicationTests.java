package io.github.leonardogendo.authapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
@SpringBootTest(
        properties = {
                "jwt.secret=dGVzdC10ZXN0LXRlc3QtdGVzdC10ZXN0LXRlc3QtdGVzdC10ZXN0LTEyMzQ1Njc4OTA=",
                "jwt.issuer=auth-api-test",
                "jwt.audience=auth-api-test",
                "jwt.access-token-ttl=15m"
        }
)
class AuthApiApplicationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17");

	@Test
	void contextLoads() {
	}

}
