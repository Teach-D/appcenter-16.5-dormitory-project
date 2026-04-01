package com.example.appcenter_project.support;

import com.example.appcenter_project.domain.user.repository.SchoolLoginRepository;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Testcontainers 기반 통합 테스트 베이스 클래스.
 *
 * <p>MySQL, Redis 컨테이너를 static으로 선언해 JVM 내 모든 하위 테스트 클래스가
 * 동일한 컨테이너를 재사용합니다. 컨테이너는 테스트 스위트 시작 시 1회 기동되고
 * 종료 시 자동으로 정리됩니다.
 *
 * <p>사용 예:
 * <pre>{@code
 * class UserServiceIntegrationTest extends AbstractIntegrationTest {
 *     @Autowired UserService userService;
 *
 *     @Test
 *     void someTest() { ... }
 * }
 * }</pre>
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class AbstractIntegrationTest {

    // Oracle 연결 없이 테스트 가능하도록 Mock 처리
    // 포털 로그인 검증이 필요한 테스트에서 when(schoolLoginRepository.loginCheck(...)).thenReturn("Y") 사용
    @MockitoBean
    protected SchoolLoginRepository schoolLoginRepository;

    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static final GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }
}
