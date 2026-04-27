/**
 * nGrinder 부하 테스트 스크립트 - 공동구매 목록 조회 N+1 Before/After 비교
 *
 * ===== 사전 준비 =====
 * 1. nGrinder 실행
 *    docker run -d -p 8300:80 -p 16001:16001 -p 12000-12009:12000-12009 ngrinder/controller:latest
 *    http://localhost:8300 (admin/admin)
 *
 * 2. Agent 실행 (nGrinder 대시보드 → Agent Management → Download Agent)
 *
 * 3. 테스트 데이터 준비
 *    - 공동구매 게시글 최소 20개 + 각 게시글에 이미지 1~3장 업로드
 *    - POST /users/login → accessToken 복사 → 아래 BEARER_TOKEN 입력
 *
 * ===== Before 측정 방법 =====
 * GroupOrderService.findGroupOrders() 리팩토링 전 버전으로 배포 후 측정.
 * 이미 리팩토링했다면 git stash / 별도 브랜치로 되돌려서 측정.
 *
 * ===== After 측정 방법 =====
 * 리팩토링 완료 버전(이 PR)으로 배포 후 동일 조건 측정.
 *
 * ===== nGrinder 설정값 =====
 * - vUser: 50
 * - Duration: 30초
 * - Ramp-up: 10초 (0 → 50 vUser 점진적 증가)
 * - Target URL: http://{서버IP}:8055/group-orders
 *
 * ===== 측정 지표 =====
 * - TPS (Transactions Per Second)
 * - Mean Response Time (ms)
 * - P99 Response Time (ms)
 *
 * ===== DB 쿼리 수 측정 =====
 * 테스트 전후 아래 SQL로 Com_select 차이 측정:
 *   SELECT variable_value FROM performance_schema.global_status
 *   WHERE variable_name = 'Com_select';
 *
 * 기대값:
 *   Before: 요청 1건당 (게시글 수 + 1)개 SELECT
 *   After:  요청 1건당 2개 SELECT (게시글 목록 1 + 이미지 IN 1)
 */

import static net.grinder.script.Grinder.grinder
import net.grinder.script.GTest
import net.grinder.scriptengine.groovy.junit.GrinderRunner
import net.grinder.scriptengine.groovy.junit.annotation.BeforeProcess
import net.grinder.scriptengine.groovy.junit.annotation.BeforeThread
import org.junit.Test
import org.junit.runner.RunWith

import net.grinder.plugin.http.HTTPRequest
import HTTPClient.NVPair

@RunWith(GrinderRunner)
class GroupOrderN1Test {

    // ===== 설정값 — 실행 전 반드시 변경 =====
    static final String TARGET_URL = "http://172.21.240.1:8055/group-orders"
    static final String BEARER_TOKEN = "Bearer {여기에_JWT_토큰_입력}"
    // ==========================================

    static GTest test
    static HTTPRequest request

    @BeforeProcess
    static void beforeProcess() {
        request = new HTTPRequest()
        request.headers = [
            new NVPair("Authorization", BEARER_TOKEN),
            new NVPair("Content-Type", "application/json")
        ] as NVPair[]
        test = new GTest(1, "GET /group-orders")
        test.record(request)
        grinder.logger.info("테스트 초기화 완료 - Target: ${TARGET_URL}")
    }

    @BeforeThread
    void beforeThread() {
        grinder.statistics.delayReports = true
    }

    @Test
    void doRequest() {
        def response = request.GET(TARGET_URL)
        int statusCode = response.statusCode

        if (statusCode == 200) {
            grinder.logger.info("vUser ${grinder.threadNumber} → 200 OK")
        } else if (statusCode == 429) {
            grinder.logger.info("vUser ${grinder.threadNumber} → 429 Rate Limit 차단")
        } else {
            grinder.logger.warn("vUser ${grinder.threadNumber} → ${statusCode}")
        }
    }
}
