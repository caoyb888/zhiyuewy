package com.zhaoxinms.resi.cashier.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.zhaoxinms.base.service.BillRuleService;
import com.zhaoxinms.common.exception.ServiceException;
import com.zhaoxinms.resi.TestSecurityContext;
import com.zhaoxinms.resi.archive.entity.ResiCustomerAsset;
import com.zhaoxinms.resi.archive.entity.ResiRoom;
import com.zhaoxinms.resi.archive.service.IResiCustomerAssetService;
import com.zhaoxinms.resi.archive.service.IResiCustomerService;
import com.zhaoxinms.resi.archive.service.IResiRoomService;
import com.zhaoxinms.resi.cashier.dto.ResiCashierCalcReq;
import com.zhaoxinms.resi.cashier.dto.ResiCashierCalcVo;
import com.zhaoxinms.resi.cashier.dto.ResiCashierCollectReq;
import com.zhaoxinms.resi.cashier.dto.ResiCashierCollectVo;
import com.zhaoxinms.resi.common.ResiConstants;
import com.zhaoxinms.resi.feeconfig.entity.ResiDiscount;
import com.zhaoxinms.resi.feeconfig.mapper.ResiDiscountMapper;
import com.zhaoxinms.resi.finance.entity.ResiPayLog;
import com.zhaoxinms.resi.finance.mapper.ResiPayLogMapper;
import com.zhaoxinms.resi.finance.service.IResiPrePayService;
import com.zhaoxinms.resi.receivable.entity.ResiReceivable;
import com.zhaoxinms.resi.receivable.mapper.ResiReceivableMapper;

/**
 * 收银台 Service 单元测试
 * 覆盖：收款核心逻辑、折扣计算、重复收款拒绝、金额校验
 */
@ExtendWith(MockitoExtension.class)
class ResiCashierServiceImplTest {

    @Mock
    private IResiRoomService roomService;

    @Mock
    private IResiCustomerService customerService;

    @Mock
    private IResiCustomerAssetService customerAssetService;

    @Mock
    private ResiReceivableMapper receivableMapper;

    @Mock
    private ResiPayLogMapper payLogMapper;

    @Mock
    private ResiDiscountMapper discountMapper;

    @Mock
    private BillRuleService billRuleService;

    @Mock
    private IResiPrePayService prePayService;

    @Mock
    @SuppressWarnings("unchecked")
    private RedisTemplate<String, String> redisTemplate;

    @InjectMocks
    private ResiCashierServiceImpl cashierService;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        TestSecurityContext.setAdminUser();
        // mock lambdaQuery 链式调用，避免 NPE
        LambdaQueryChainWrapper<ResiCustomerAsset> mockWrapper = mock(LambdaQueryChainWrapper.class);
        lenient().when(mockWrapper.eq(any(), any())).thenReturn(mockWrapper);
        lenient().when(mockWrapper.list()).thenReturn(Collections.emptyList());
        lenient().when(customerAssetService.lambdaQuery()).thenReturn(mockWrapper);
    }

    @AfterEach
    void tearDown() {
        TestSecurityContext.clear();
    }

    // ==================== S4-06 收款金额验证 ====================

    @Test
    @DisplayName("正常收款：应收100.00，实收100.00，pay_state='2'")
    void collect_normal_success() {
        // Given
        String receivableId = "recv-001";
        ResiCashierCollectReq req = buildCollectReq(receivableId, new BigDecimal("100.00"));

        ResiReceivable receivable = buildReceivable(receivableId, new BigDecimal("100.00"), ResiConstants.PAY_STATE_UNPAID);
        when(receivableMapper.selectList(any(QueryWrapper.class))).thenReturn(Collections.singletonList(receivable));
        when(billRuleService.getNumber(ResiConstants.BILL_RULE_RESI_RECEIPT)).thenReturn("ZS20260701000001");
        when(receivableMapper.updateById(any(ResiReceivable.class))).thenReturn(1);
        when(payLogMapper.insert(any(ResiPayLog.class))).thenReturn(1);

        ResiRoom room = buildRoom(1L);
        when(roomService.getById(1L)).thenReturn(room);

        // When
        ResiCashierCollectVo result = cashierService.collect(req);

        // Then
        assertNotNull(result.getPayNo());
        assertTrue(result.getPayNo().startsWith("ZS"));
        assertEquals(new BigDecimal("100.00"), result.getPayAmount());
        assertEquals(1, result.getFeeItems().size());

        // 验证应收记录被更新
        verify(receivableMapper, times(1)).updateById(argThat(r ->
            ResiConstants.PAY_STATE_PAID.equals(r.getPayState()) &&
            new BigDecimal("100.00").equals(r.getPaidAmount()) &&
            r.getPayLogId() != null
        ));

        // 验证流水写入
        verify(payLogMapper, times(1)).insert(argThat(log ->
            new BigDecimal("100.00").equals(log.getPayAmount()) &&
            ResiConstants.PAY_TYPE_COLLECT.equals(log.getPayType()) &&
            BigDecimal.ZERO.equals(log.getDiscountAmount())
        ));
    }

    @Test
    @DisplayName("收款带折扣9折：应收100.00，discount_amount=10.00，实收90.00")
    void collect_with_discount_90() {
        // Given
        String receivableId = "recv-002";
        String discountId = "disc-001";
        ResiCashierCollectReq req = buildCollectReq(receivableId, new BigDecimal("90.00"));
        req.setDiscountId(discountId);

        ResiReceivable receivable = buildReceivable(receivableId, new BigDecimal("100.00"), ResiConstants.PAY_STATE_UNPAID);
        when(receivableMapper.selectList(any(QueryWrapper.class))).thenReturn(Collections.singletonList(receivable));

        ResiDiscount discount = buildDiscount(discountId, new BigDecimal("0.9000"), null);
        when(discountMapper.selectById(discountId)).thenReturn(discount);

        when(billRuleService.getNumber(ResiConstants.BILL_RULE_RESI_RECEIPT)).thenReturn("ZS20260701000002");
        when(receivableMapper.updateById(any(ResiReceivable.class))).thenReturn(1);
        when(payLogMapper.insert(any(ResiPayLog.class))).thenReturn(1);

        ResiRoom room = buildRoom(1L);
        when(roomService.getById(1L)).thenReturn(room);

        // When
        ResiCashierCollectVo result = cashierService.collect(req);

        // Then
        assertEquals(new BigDecimal("90.00"), result.getPayAmount());
        assertEquals(new BigDecimal("10.00"), result.getDiscountAmount());

        // 验证流水中的折扣金额正确
        verify(payLogMapper, times(1)).insert(argThat(log ->
            new BigDecimal("10.00").equals(log.getDiscountAmount()) &&
            new BigDecimal("90.00").equals(log.getPayAmount())
        ));
    }

    @Test
    @DisplayName("重复收款拒绝：应收已收，再次收款抛异常")
    void collect_alreadyPaid_shouldFail() {
        // Given
        String receivableId = "recv-003";
        ResiCashierCollectReq req = buildCollectReq(receivableId, new BigDecimal("100.00"));

        // 模拟锁定后返回已收状态（SELECT FOR UPDATE 后状态被其他事务修改）
        ResiReceivable receivable = buildReceivable(receivableId, new BigDecimal("100.00"), ResiConstants.PAY_STATE_PAID);
        when(receivableMapper.selectList(any(QueryWrapper.class))).thenReturn(Collections.singletonList(receivable));

        // When & Then
        ServiceException ex = assertThrows(ServiceException.class, () -> cashierService.collect(req));
        assertTrue(ex.getMessage().contains("已被收取"));

        verify(billRuleService, never()).getNumber(anyString());
        verify(payLogMapper, never()).insert(any(ResiPayLog.class));
    }

    @Test
    @DisplayName("实收金额不足拒绝：应收100.00，实收80.00，抛异常")
    void collect_amountMismatch_shouldFail() {
        // Given
        String receivableId = "recv-004";
        ResiCashierCollectReq req = buildCollectReq(receivableId, new BigDecimal("80.00"));

        ResiReceivable receivable = buildReceivable(receivableId, new BigDecimal("100.00"), ResiConstants.PAY_STATE_UNPAID);
        when(receivableMapper.selectList(any(QueryWrapper.class))).thenReturn(Collections.singletonList(receivable));

        // When & Then
        ServiceException ex = assertThrows(ServiceException.class, () -> cashierService.collect(req));
        assertTrue(ex.getMessage().contains("实收金额与应收金额不符"));

        verify(billRuleService, never()).getNumber(anyString());
        verify(payLogMapper, never()).insert(any(ResiPayLog.class));
    }

    @Test
    @DisplayName("已减免费用不可收款：pay_state='3'，抛异常")
    void collect_waived_shouldFail() {
        // Given
        String receivableId = "recv-005";
        ResiCashierCollectReq req = buildCollectReq(receivableId, new BigDecimal("100.00"));

        ResiReceivable receivable = buildReceivable(receivableId, new BigDecimal("100.00"), ResiConstants.PAY_STATE_WAIVED);
        when(receivableMapper.selectList(any(QueryWrapper.class))).thenReturn(Collections.singletonList(receivable));

        // When & Then
        ServiceException ex = assertThrows(ServiceException.class, () -> cashierService.collect(req));
        assertTrue(ex.getMessage().contains("已减免"));
    }

    // ==================== 预览计算测试 ====================

    @Test
    @DisplayName("预览计算-正常：2笔费用合计200.00，无折扣")
    void calc_normal_noDiscount() {
        // Given
        String r1 = "recv-101", r2 = "recv-102";
        ResiCashierCalcReq req = new ResiCashierCalcReq();
        req.setProjectId(1L);
        req.setReceivableIds(Arrays.asList(r1, r2));

        ResiReceivable recv1 = buildReceivable(r1, new BigDecimal("100.00"), ResiConstants.PAY_STATE_UNPAID);
        ResiReceivable recv2 = buildReceivable(r2, new BigDecimal("100.00"), ResiConstants.PAY_STATE_UNPAID);
        when(receivableMapper.selectList(any(QueryWrapper.class))).thenReturn(Arrays.asList(recv1, recv2));

        // When
        ResiCashierCalcVo result = cashierService.calc(req);

        // Then
        assertEquals(new BigDecimal("200.00"), result.getTotalAmount());
        assertEquals(BigDecimal.ZERO, result.getDiscountAmount());
        assertEquals(new BigDecimal("200.00"), result.getReceivableAmount());
        assertEquals(new BigDecimal("200.00"), result.getPayAmount());
        assertEquals(2, result.getItems().size());
    }

    @Test
    @DisplayName("预览计算-9折：total=100.00，discount=10.00，receivable=90.00")
    void calc_with_discount_90() {
        // Given
        String receivableId = "recv-201";
        String discountId = "disc-002";
        ResiCashierCalcReq req = new ResiCashierCalcReq();
        req.setProjectId(1L);
        req.setReceivableIds(Collections.singletonList(receivableId));
        req.setDiscountId(discountId);

        ResiReceivable receivable = buildReceivable(receivableId, new BigDecimal("100.00"), ResiConstants.PAY_STATE_UNPAID);
        when(receivableMapper.selectList(any(QueryWrapper.class))).thenReturn(Collections.singletonList(receivable));

        ResiDiscount discount = buildDiscount(discountId, new BigDecimal("0.9000"), null);
        when(discountMapper.selectById(discountId)).thenReturn(discount);

        // When
        ResiCashierCalcVo result = cashierService.calc(req);

        // Then
        assertEquals(new BigDecimal("100.00"), result.getTotalAmount());
        assertEquals(new BigDecimal("10.00"), result.getDiscountAmount());
        assertEquals(new BigDecimal("90.00"), result.getReceivableAmount());
        assertEquals(new BigDecimal("90.00"), result.getPayAmount());
        assertEquals("9折测试", result.getDiscountName());
    }

    @Test
    @DisplayName("预览计算-已缴清费用不可再次预览")
    void calc_alreadyPaid_shouldFail() {
        // Given
        String receivableId = "recv-202";
        ResiCashierCalcReq req = new ResiCashierCalcReq();
        req.setProjectId(1L);
        req.setReceivableIds(Collections.singletonList(receivableId));

        ResiReceivable receivable = buildReceivable(receivableId, new BigDecimal("100.00"), ResiConstants.PAY_STATE_PAID);
        when(receivableMapper.selectList(any(QueryWrapper.class))).thenReturn(Collections.singletonList(receivable));

        // When & Then
        ServiceException ex = assertThrows(ServiceException.class, () -> cashierService.calc(req));
        assertTrue(ex.getMessage().contains("已缴清"));
    }

    @Test
    @DisplayName("预览计算-折扣不适用所选费用，抛异常")
    void calc_discountNotApplicable_shouldFail() {
        // Given
        String receivableId = "recv-203";
        String discountId = "disc-003";
        ResiCashierCalcReq req = new ResiCashierCalcReq();
        req.setProjectId(1L);
        req.setReceivableIds(Collections.singletonList(receivableId));
        req.setDiscountId(discountId);

        ResiReceivable receivable = buildReceivable(receivableId, new BigDecimal("100.00"), ResiConstants.PAY_STATE_UNPAID);
        receivable.setFeeId("fee-other");
        when(receivableMapper.selectList(any(QueryWrapper.class))).thenReturn(Collections.singletonList(receivable));

        ResiDiscount discount = buildDiscount(discountId, new BigDecimal("0.9000"), "[\"fee-specific\"]");
        when(discountMapper.selectById(discountId)).thenReturn(discount);

        // When & Then
        ServiceException ex = assertThrows(ServiceException.class, () -> cashierService.calc(req));
        assertTrue(ex.getMessage().contains("不适用于所选费用"));
    }

    // ==================== 并发逻辑验证 ====================

    @Test
    @DisplayName("并发收款幂等逻辑：锁定后检测到已收状态，抛异常")
    void collect_concurrent_idempotent_logic() {
        // Given
        String receivableId = "recv-301";
        ResiCashierCollectReq req = buildCollectReq(receivableId, new BigDecimal("100.00"));

        // 第一次调用返回未收，第二次调用返回已收（模拟并发下另一线程已收款）
        ResiReceivable unpaid = buildReceivable(receivableId, new BigDecimal("100.00"), ResiConstants.PAY_STATE_UNPAID);
        ResiReceivable paid = buildReceivable(receivableId, new BigDecimal("100.00"), ResiConstants.PAY_STATE_PAID);

        when(receivableMapper.selectList(any(QueryWrapper.class)))
                .thenReturn(Collections.singletonList(unpaid))
                .thenReturn(Collections.singletonList(paid));

        // 第一次调用成功
        when(billRuleService.getNumber(ResiConstants.BILL_RULE_RESI_RECEIPT)).thenReturn("ZS20260701000003");
        when(receivableMapper.updateById(any(ResiReceivable.class))).thenReturn(1);
        when(payLogMapper.insert(any(ResiPayLog.class))).thenReturn(1);
        ResiRoom room = buildRoom(1L);
        when(roomService.getById(1L)).thenReturn(room);

        ResiCashierCollectVo result1 = cashierService.collect(req);
        assertNotNull(result1.getPayNo());

        // 第二次调用（模拟另一线程）应失败
        ServiceException ex = assertThrows(ServiceException.class, () -> cashierService.collect(req));
        assertTrue(ex.getMessage().contains("已被收取"));
    }

    // ==================== Helper Methods ====================

    private ResiCashierCollectReq buildCollectReq(String receivableId, BigDecimal payAmount) {
        ResiCashierCollectReq req = new ResiCashierCollectReq();
        req.setProjectId(1L);
        req.setResourceType(ResiConstants.RESOURCE_TYPE_ROOM);
        req.setResourceId(1L);
        req.setReceivableIds(Collections.singletonList(receivableId));
        req.setPayMethod(ResiConstants.PAY_METHOD_CASH);
        req.setPayAmount(payAmount);
        return req;
    }

    private ResiReceivable buildReceivable(String id, BigDecimal total, String payState) {
        ResiReceivable r = new ResiReceivable();
        r.setId(id);
        r.setProjectId(1L);
        r.setResourceType(ResiConstants.RESOURCE_TYPE_ROOM);
        r.setResourceId(1L);
        r.setFeeId("fee-test");
        r.setFeeName("测试物业费");
        r.setBillPeriod("2026-07");
        r.setNum(BigDecimal.ONE);
        r.setPrice(total);
        r.setTotal(total);
        r.setOverdueFee(BigDecimal.ZERO);
        r.setDiscountAmount(BigDecimal.ZERO);
        r.setReceivable(total);
        r.setPayState(payState);
        r.setPaidAmount(BigDecimal.ZERO);
        return r;
    }

    private ResiDiscount buildDiscount(String id, BigDecimal rate, String feeScope) {
        ResiDiscount d = new ResiDiscount();
        d.setId(id);
        d.setProjectId(1L);
        d.setDiscountName("9折测试");
        d.setDiscountRate(rate);
        d.setFeeScope(feeScope);
        d.setEnabledMark(1);
        d.setValidStart(null);
        d.setValidEnd(null);
        return d;
    }

    private ResiRoom buildRoom(Long id) {
        ResiRoom room = new ResiRoom();
        room.setId(id);
        room.setProjectId(1L);
        room.setBuildingId(1L);
        room.setRoomNo("101");
        room.setRoomAlias("1栋1单元101");
        return room;
    }
}
