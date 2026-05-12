package com.zhaoxinms.resi.cashier.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 收银台房间费用汇总
 */
public class ResiCashierRoomSummaryVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 应收总额（所有应收记录 receivable 之和） */
    private BigDecimal totalReceivable;

    /** 已缴总额（所有已收记录 paid_amount 之和） */
    private BigDecimal totalPaid;

    /** 欠费总额（未收和部分收的 receivable - paid_amount 之和） */
    private BigDecimal totalArrears;

    /** 未收笔数 */
    private Integer unpaidCount;

    /** 部分收笔数 */
    private Integer partPaidCount;

    /** 已收笔数 */
    private Integer paidCount;

    public ResiCashierRoomSummaryVo() {
        this.totalReceivable = BigDecimal.ZERO;
        this.totalPaid = BigDecimal.ZERO;
        this.totalArrears = BigDecimal.ZERO;
        this.unpaidCount = 0;
        this.partPaidCount = 0;
        this.paidCount = 0;
    }

    public BigDecimal getTotalReceivable() {
        return totalReceivable;
    }

    public void setTotalReceivable(BigDecimal totalReceivable) {
        this.totalReceivable = totalReceivable;
    }

    public BigDecimal getTotalPaid() {
        return totalPaid;
    }

    public void setTotalPaid(BigDecimal totalPaid) {
        this.totalPaid = totalPaid;
    }

    public BigDecimal getTotalArrears() {
        return totalArrears;
    }

    public void setTotalArrears(BigDecimal totalArrears) {
        this.totalArrears = totalArrears;
    }

    public Integer getUnpaidCount() {
        return unpaidCount;
    }

    public void setUnpaidCount(Integer unpaidCount) {
        this.unpaidCount = unpaidCount;
    }

    public Integer getPartPaidCount() {
        return partPaidCount;
    }

    public void setPartPaidCount(Integer partPaidCount) {
        this.partPaidCount = partPaidCount;
    }

    public Integer getPaidCount() {
        return paidCount;
    }

    public void setPaidCount(Integer paidCount) {
        this.paidCount = paidCount;
    }
}
