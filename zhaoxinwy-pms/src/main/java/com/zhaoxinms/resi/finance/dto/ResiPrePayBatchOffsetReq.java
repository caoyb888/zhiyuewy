package com.zhaoxinms.resi.finance.dto;

import java.math.BigDecimal;
import java.util.List;

import javax.validation.constraints.NotEmpty;

/**
 * 批量冲抵预收款请求DTO
 */
public class ResiPrePayBatchOffsetReq {

    @NotEmpty(message = "冲抵项不能为空")
    private List<OffsetItem> items;

    /** 操作人ID（后台填充） */
    private String creatorUserId;

    public List<OffsetItem> getItems() {
        return items;
    }

    public void setItems(List<OffsetItem> items) {
        this.items = items;
    }

    public String getCreatorUserId() {
        return creatorUserId;
    }

    public void setCreatorUserId(String creatorUserId) {
        this.creatorUserId = creatorUserId;
    }

    public static class OffsetItem {

        /** 预收款账户ID */
        private String accountId;

        /** 冲抵金额 */
        private BigDecimal offsetAmount;

        /** 关联应收ID（可选） */
        private String receivableId;

        /** 备注 */
        private String remark;

        public String getAccountId() {
            return accountId;
        }

        public void setAccountId(String accountId) {
            this.accountId = accountId;
        }

        public BigDecimal getOffsetAmount() {
            return offsetAmount;
        }

        public void setOffsetAmount(BigDecimal offsetAmount) {
            this.offsetAmount = offsetAmount;
        }

        public String getReceivableId() {
            return receivableId;
        }

        public void setReceivableId(String receivableId) {
            this.receivableId = receivableId;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }
    }
}
