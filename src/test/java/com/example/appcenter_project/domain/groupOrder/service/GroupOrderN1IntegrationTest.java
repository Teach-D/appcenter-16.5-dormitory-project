package com.example.appcenter_project.domain.groupOrder.service;

import com.example.appcenter_project.common.image.entity.Image;
import com.example.appcenter_project.common.image.enums.ImageType;
import com.example.appcenter_project.common.image.repository.ImageRepository;
import com.example.appcenter_project.domain.groupOrder.entity.GroupOrder;
import com.example.appcenter_project.domain.groupOrder.enums.GroupOrderSort;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * GroupOrder 목록 조회 N+1 해소 및 페이징 검증 테스트.
 *
 * <p>Docker 없이 H2 인메모리 DB로 실행되는 순수 JPA 레이어 테스트입니다.
 * 실제 SQL 쿼리 수를 Hibernate Statistics로 측정하여 N+1 해소를 검증합니다.
 *
 * <p>검증 항목:
 * <ol>
 *   <li>페이징 - SQL LIMIT/OFFSET 적용 (메모리 페이징 없음)</li>
 *   <li>N+1 Before/After 쿼리 수 비교</li>
 *   <li>batch IN 쿼리로 이미지 일괄 조회 (쿼리 수 고정)</li>
 * </ol>
 */
@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers=true"
})
@ActiveProfiles("test")
class GroupOrderN1IntegrationTest {

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

    // ===== 1. 페이징 - SQL LIMIT/OFFSET 적용 검증 =====

    @Test
    @DisplayName("페이징 - size=10 요청 시 결과 10개만 반환 (SQL LIMIT 적용)")
    void 페이징_size_10_결과_10개() {
        // given: 게시글 30개
        saveGroupOrders(30, 0);
        em.flush();
        em.clear();

        // when: page=0, size=10
        List<GroupOrder> result = groupOrderRepository.findGroupOrdersComplex(
                GroupOrderSort.LATEST, GroupOrderType.ALL, null, PageRequest.of(0, 10));

        // then: SQL LIMIT 10 적용 → 10개만 반환
        assertThat(result).hasSize(10);
    }

    @Test
    @DisplayName("페이징 - page=1 요청 시 다음 10개 반환 (SQL OFFSET 적용)")
    void 페이징_page1_다음_10개_반환() {
        // given: 게시글 30개
        List<GroupOrder> allOrders = saveGroupOrders(30, 0);
        em.flush();
        em.clear();

        // when
        List<GroupOrder> page0 = groupOrderRepository.findGroupOrdersComplex(
                GroupOrderSort.LATEST, GroupOrderType.ALL, null, PageRequest.of(0, 10));
        List<GroupOrder> page1 = groupOrderRepository.findGroupOrdersComplex(
                GroupOrderSort.LATEST, GroupOrderType.ALL, null, PageRequest.of(1, 10));

        // then: 두 페이지 결과가 겹치지 않음 (OFFSET 적용)
        List<Long> page0Ids = page0.stream().map(GroupOrder::getId).toList();
        List<Long> page1Ids = page1.stream().map(GroupOrder::getId).toList();
        assertThat(page0Ids).doesNotContainAnyElementsOf(page1Ids);
    }

    @Test
    @DisplayName("페이징 - 3페이지 합산 시 전체 30개 커버")
    void 페이징_3페이지_합산_전체_커버() {
        // given: 게시글 30개
        saveGroupOrders(30, 0);
        em.flush();
        em.clear();

        // when
        List<GroupOrder> page0 = groupOrderRepository.findGroupOrdersComplex(
                GroupOrderSort.LATEST, GroupOrderType.ALL, null, PageRequest.of(0, 10));
        List<GroupOrder> page1 = groupOrderRepository.findGroupOrdersComplex(
                GroupOrderSort.LATEST, GroupOrderType.ALL, null, PageRequest.of(1, 10));
        List<GroupOrder> page2 = groupOrderRepository.findGroupOrdersComplex(
                GroupOrderSort.LATEST, GroupOrderType.ALL, null, PageRequest.of(2, 10));

        // then
        assertThat(page0).hasSize(10);
        assertThat(page1).hasSize(10);
        assertThat(page2).hasSize(10);
    }

    // ===== 2. N+1 Before/After 쿼리 수 비교 =====

    @Test
    @DisplayName("N+1 Before - 게시글 20개 이미지 개별 조회 시 쿼리 20개 발생")
    void N1_Before_이미지_개별_조회_쿼리_20개() {
        // given: 게시글 20개 + 이미지 2개씩
        List<GroupOrder> orders = saveGroupOrders(20, 2);
        em.flush();
        em.clear();

        // when: N+1 패턴 — 게시글마다 이미지를 개별 조회
        stats.clear();
        for (GroupOrder order : orders) {
            imageRepository.findAllByEntityIdAndImageType(order.getId(), ImageType.GROUP_ORDER);
        }
        long beforeQueryCount = stats.getQueryExecutionCount();

        // then: 게시글 수(20)만큼 쿼리 발생
        assertThat(beforeQueryCount).isEqualTo(20);
    }

    @Test
    @DisplayName("N+1 After - 게시글 20개 이미지 batch IN 조회 시 쿼리 1개")
    void N1_After_이미지_batch_조회_쿼리_1개() {
        // given: 게시글 20개 + 이미지 2개씩
        List<GroupOrder> orders = saveGroupOrders(20, 2);
        em.flush();
        em.clear();

        // when: batch fetch 패턴 — IN 쿼리 1회
        stats.clear();
        List<Long> ids = orders.stream().map(GroupOrder::getId).toList();
        List<Image> images = imageRepository.findGroupOrderImagesByEntityIds(ids);
        long afterQueryCount = stats.getQueryExecutionCount();

        // then: 단 1개 쿼리로 모든 이미지 조회
        assertThat(afterQueryCount).isEqualTo(1);
        assertThat(images).hasSize(40); // 20개 × 이미지 2개
    }

    @Test
    @DisplayName("N+1 Before/After 비교 - batch 방식이 개별 방식보다 쿼리 수 10배 이상 적음")
    void N1_Before_After_쿼리수_10배_차이() {
        // given: 게시글 20개 + 이미지 2개씩
        List<GroupOrder> orders = saveGroupOrders(20, 2);
        em.flush();
        em.clear();

        List<Long> ids = orders.stream().map(GroupOrder::getId).toList();

        // Before: 개별 조회
        stats.clear();
        for (Long id : ids) {
            imageRepository.findAllByEntityIdAndImageType(id, ImageType.GROUP_ORDER);
        }
        long beforeQueryCount = stats.getQueryExecutionCount();

        // After: batch IN 조회
        stats.clear();
        imageRepository.findGroupOrderImagesByEntityIds(ids);
        long afterQueryCount = stats.getQueryExecutionCount();

        // then
        assertThat(beforeQueryCount).isEqualTo(20);
        assertThat(afterQueryCount).isEqualTo(1);
        assertThat(beforeQueryCount).isGreaterThanOrEqualTo(afterQueryCount * 10);
    }

    // ===== 3. 페이징 + batch fetch 조합 쿼리 수 =====

    @Test
    @DisplayName("페이징 목록 조회 후 batch 이미지 조회 — 전체 쿼리 수 2개 고정")
    void 페이징_후_batch_이미지_조회_쿼리_2개() {
        // given: 게시글 100개 + 이미지 2개씩
        saveGroupOrders(100, 2);
        em.flush();
        em.clear();

        stats.clear();

        // when: ① 페이징 목록 조회
        List<GroupOrder> page = groupOrderRepository.findGroupOrdersComplex(
                GroupOrderSort.LATEST, GroupOrderType.ALL, null, PageRequest.of(0, 20));

        // ② 해당 페이지의 이미지만 batch 조회
        List<Long> pageIds = page.stream().map(GroupOrder::getId).toList();
        imageRepository.findGroupOrderImagesByEntityIds(pageIds);

        long totalQueryCount = stats.getQueryExecutionCount();

        // then: 데이터 100개여도 쿼리 수 2개 고정
        assertThat(page).hasSize(20);
        assertThat(totalQueryCount).isEqualTo(2);
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
