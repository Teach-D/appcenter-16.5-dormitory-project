/**
 * nGrinder 부하 테스트 스크립트 - 쿠폰 Rate Limit 전/후 비교
 *
 * ===== 사전 준비 =====
 * 1. nGrinder 설치 및 실행
 *    - Docker: docker run -d -p 8300:8080 ngrinder/controller:latest
 *    - http://localhost:8300 접속 (admin/admin)
 *
 * 2. Agent 실행 (테스트 트래픽을 실제로 발생시키는 프로세스)
 *    - nGrinder 대시보드 → Agent Management → Download Agent
 *    - 압축 해제 후: ./run_agent.sh 실행
 *
 * 3. 테스트 계정 JWT 토큰 준비
 *    - POST /users/login 으로 로그인 → accessToken 복사
 *    - 아래 BEARER_TOKEN 변수에 붙여넣기
 *
 * ===== 테스트 시나리오 =====
 * [Before] Rate Limit 없는 상태에서 100 vUser 동시 요청
 *    → DB 쿼리 수, 응답 시간 P99 측정
 *
 * [After] Rate Limit 적용 후 동일 시나리오
 *    → 429 차단 비율, DB 쿼리 수 감소량 측정
 *
 * ===== nGrinder 설정값 =====
 * - vUser: 100
 * - Duration: 30초
 * - Ramp-up: 10초 (0 → 100 vUser 점진적 증가)
 * - Target URL: http://{서버IP}:8055/coupons
 *
 * ===== 측정 지표 =====
 * - TPS (Transactions Per Second)
 * - Mean Response Time (ms)
 * - P99 Response Time (ms)
 * - 429 응답 비율 (= 차단된 요청 비율)
 *
 * ===== DB 쿼리 수 측정 방법 =====
 * MySQL slow query log 또는 Prometheus + Spring Actuator 사용:
 *   # 테스트 전 카운터 초기화
 *   SELECT variable_value FROM performance_schema.global_status WHERE variable_name = 'Com_select';
 *
 *   # 테스트 후 카운터 재조회 → 차이값 = 테스트 중 SELECT 쿼리 수
 *   SELECT variable_value FROM performance_schema.global_status WHERE variable_name = 'Com_select';
 */

import static net.grinder.script.Grinder.grinder
import static org.junit.Assert.*
import net.grinder.script.GTest
import net.grinder.scriptengine.groovy.junit.GrinderRunner
import net.grinder.scriptengine.groovy.junit.annotation.RunWith
import net.grinder.scriptengine.groovy.junit.annotation.BeforeProcess
import net.grinder.scriptengine.groovy.junit.annotation.BeforeThread
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith as JUnitRunWith

import HTTPClient.HTTPResponse
import HTTPClient.NVPair

@RunWith(GrinderRunner)
class CouponRateLimitTest {

    // ===== 설정값 — 실행 전 반드시 변경 =====
    static final String BASE_URL = "http://localhost:8055"
    static final String BEARER_TOKEN = "Bearer {여기에_JWT_accessToken_입력}"
    // ==========================================

    static GTest test
    static HTTPClient.HTTPConnection connection

    @BeforeProcess
    static void beforeProcess() {
        test = new GTest(1, "GET /coupons")
        connection = new HTTPClient.HTTPConnection(BASE_URL)
        grinder.logger.info("테스트 초기화 완료 - Target: ${BASE_URL}")
    }

    @BeforeThread
    void beforeThread() {
        test.record(this, "request")
        grinder.statistics.delayReports = true
    }

    @Before
    void before() {
        grinder.logger.info("vUser ${grinder.threadNumber} 시작")
    }

    @Test
    void request() {
        NVPair[] headers = [
            new NVPair("Authorization", BEARER_TOKEN),
            new NVPair("Content-Type", "application/json")
        ]

        HTTPResponse response = connection.Get("/coupons", null, headers)

        int statusCode = response.statusCode

        // 200 (성공) 또는 429 (Rate Limit 차단) 만 허용
        // Rate Limit 없을 때는 200/기타만 나옴
        if (statusCode == 200) {
            grinder.logger.info("vUser ${grinder.threadNumber} → 200 OK (쿠폰 발급 처리)")
        } else if (statusCode == 429) {
            grinder.logger.info("vUser ${grinder.threadNumber} → 429 Rate Limit 차단")
        } else {
            grinder.logger.warn("vUser ${grinder.threadNumber} → ${statusCode} (예상치 못한 응답)")
        }

        assertTrue("응답 코드는 200 또는 429여야 합니다", statusCode == 200 || statusCode == 429)
    }
}
