package com.example.appcenter_project.domain.groupOrder.service;

import com.example.appcenter_project.common.image.entity.Image;
import com.example.appcenter_project.common.image.enums.ImageType;
import com.example.appcenter_project.common.image.repository.ImageRepository;
import com.example.appcenter_project.domain.groupOrder.entity.GroupOrder;
import com.example.appcenter_project.domain.groupOrder.enums.GroupOrderType;
import com.example.appcenter_project.domain.groupOrder.repository.GroupOrderRepository;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * N+1 문제 존재 증명 테스트 (페이징 없음 — Before/After 코드 모두 컴파일 가능).
 *
 * <p>이 테스트는 개선 전(Before) 상태에서도 실행되도록,
 * findGroupOrdersComplex의 Pageable 파라미터를 사용하지 않고
 * ImageRepository 직접 호출로 N+1 패턴을 시뮬레이션합니다.
 *
 * <p>핵심 검증:
 * <ul>
 *   <li>Before: GroupOrder 수(N)만큼 이미지 쿼리 발생 = N+1</li>
 *   <li>After:  batch IN 쿼리 1회로 모든 이미지 조회</li>
 * </ul>
 */
@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers=true"
})
@ActiveProfiles("test")
class GroupOrderN1BeforeAfterTest {

    @Autowired private GroupOrderRepository groupOrderRepository;
    @Autowired private ImageRepository imageRepository;
    @Autowired private UserRepository userRepository;
    @PersistenceContext private EntityManager em;

    private User testUser;
    private Statistics stats;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(User.createNewUser("20230001", "password"));
        em.flush();

        stats = em.getEntityManagerFactory()
                .unwrap(SessionFactory.class)
                .getStatistics();
        stats.setStatisticsEnabled(true);
    }

    // ===== N+1 Before: 개별 이미지 조회 =====

    @Test
    @DisplayName("[Before] 게시글 10개 — 이미지 개별 조회 시 쿼리 10번 발생 (N+1)")
    void before_N1_이미지_개별조회_쿼리10번() {
        // given: 게시글 10개, 이미지 각 2개
        List<GroupOrder> orders = saveGroupOrders(10, 2);
        em.flush();
        em.clear();

        // when: N+1 패턴 — 개선 전 findRepresentativeImage() 시뮬레이션
        stats.clear();
        for (GroupOrder order : orders) {
            // GroupOrderService.findRepresentativeImage() 가 내부에서 호출하는 것과 동일
            imageRepository.findAllByEntityIdAndImageType(order.getId(), ImageType.GROUP_ORDER);
        }
        long queryCount = stats.getQueryExecutionCount();

        // then: 게시글 10개 → 이미지 쿼리 10회 (N+1 문제 존재 증명)
        System.out.println("[Before] 이미지 쿼리 수 = " + queryCount + " (기대: 10)");
        assertThat(queryCount).isEqualTo(10)
                .as("N+1 문제: 게시글 N개마다 이미지 쿼리 N번 발생");
    }

    @Test
    @DisplayName("[Before] 게시글 100개 — 이미지 개별 조회 시 쿼리 100번 발생 (N+1 심각성)")
    void before_N1_게시글100개_쿼리100번() {
        // given: 게시글 100개, 이미지 각 1개
        List<GroupOrder> orders = saveGroupOrders(100, 1);
        em.flush();
        em.clear();

        // when
        stats.clear();
        for (GroupOrder order : orders) {
            imageRepository.findAllByEntityIdAndImageType(order.getId(), ImageType.GROUP_ORDER);
        }
        long queryCount = stats.getQueryExecutionCount();

        // then: 100번 쿼리 → 실서비스에서 치명적
        System.out.println("[Before] 게시글 100개 → 이미지 쿼리 수 = " + queryCount);
        assertThat(queryCount).isEqualTo(100);
    }

    // ===== N+1 After: batch IN 쿼리 =====

    @Test
    @DisplayName("[After] 게시글 10개 — batch IN 쿼리 1번으로 이미지 전체 조회")
    void after_batch_이미지_조회_쿼리1번() {
        // given: 게시글 10개, 이미지 각 2개
        List<GroupOrder> orders = saveGroupOrders(10, 2);
        em.flush();
        em.clear();

        // when: batch fetch 패턴 — 개선 후 buildRepresentativeImageMap() 시뮬레이션
        stats.clear();
        List<Long> ids = orders.stream().map(GroupOrder::getId).toList();
        List<Image> images = imageRepository.findGroupOrderImagesByEntityIds(ids);
        long queryCount = stats.getQueryExecutionCount();

        // then: 쿼리 1번 + 이미지 20개(10*2) 반환
        System.out.println("[After]  이미지 쿼리 수 = " + queryCount + " (기대: 1)");
        assertThat(queryCount).isEqualTo(1);
        assertThat(images).hasSize(20);
    }

    @Test
    @DisplayName("[After] 게시글 100개 — batch IN 쿼리 1번 (데이터 수 무관하게 고정)")
    void after_batch_게시글100개_쿼리1번() {
        // given: 게시글 100개, 이미지 각 1개
        List<GroupOrder> orders = saveGroupOrders(100, 1);
        em.flush();
        em.clear();

        // when
        stats.clear();
        List<Long> ids = orders.stream().map(GroupOrder::getId).toList();
        imageRepository.findGroupOrderImagesByEntityIds(ids);
        long queryCount = stats.getQueryExecutionCount();

        // then: 데이터 100개여도 쿼리 1번
        System.out.println("[After]  게시글 100개 → 이미지 쿼리 수 = " + queryCount);
        assertThat(queryCount).isEqualTo(1);
    }

    // ===== Before vs After 직접 비교 =====

    @Test
    @DisplayName("[비교] Before 20쿼리 vs After 1쿼리 — 20배 차이 증명")
    void 비교_Before20쿼리_After1쿼리() {
        // given: 게시글 20개 + 이미지 각 2개
        List<GroupOrder> orders = saveGroupOrders(20, 2);
        em.flush();
        em.clear();

        List<Long> ids = orders.stream().map(GroupOrder::getId).toList();

        // Before 측정
        stats.clear();
        for (Long id : ids) {
            imageRepository.findAllByEntityIdAndImageType(id, ImageType.GROUP_ORDER);
        }
        long beforeQueryCount = stats.getQueryExecutionCount();

        // After 측정
        stats.clear();
        imageRepository.findGroupOrderImagesByEntityIds(ids);
        long afterQueryCount = stats.getQueryExecutionCount();

        // 결과 출력
        System.out.println("===== N+1 Before vs After =====");
        System.out.println("Before 쿼리 수: " + beforeQueryCount);
        System.out.println("After  쿼리 수: " + afterQueryCount);
        System.out.println("개선율: " + beforeQueryCount + "배 감소");

        // then
        assertThat(beforeQueryCount).isEqualTo(20);
        assertThat(afterQueryCount).isEqualTo(1);
        assertThat(beforeQueryCount).isGreaterThanOrEqualTo(afterQueryCount * 20);
    }

    // ===== Helper =====

    private List<GroupOrder> saveGroupOrders(int count, int imagesPerOrder) {
        List<GroupOrder> saved = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            GroupOrder order = groupOrderRepository.save(GroupOrder.builder()
                    .title("게시글" + i)
                    .groupOrderType(GroupOrderType.DELIVERY)
                    .price(10000)
                    .deadline(LocalDateTime.now().plusDays(7))
                    .description("설명" + i)
                    .user(testUser)
                    .build());
            saved.add(order);

            for (int j = 0; j < imagesPerOrder; j++) {
                imageRepository.save(Image.of(
                        "/images/group_order/test_" + i + "_" + j + ".jpg",
                        "test_" + i + "_" + j + ".jpg",
                        ImageType.GROUP_ORDER,
                        order.getId()
                ));
            }
        }
        return saved;
    }
}
