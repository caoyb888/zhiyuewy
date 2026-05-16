package com.zhaoxinms.resi.archive.dto;

import java.util.Date;

import javax.validation.constraints.NotNull;

/**
 * 房屋过户请求DTO
 *
 * @author zhaoxinms
 */
public class ResiRoomTransferReq {

    /** 房间ID */
    @NotNull(message = "房间不能为空")
    private Long roomId;

    /** 新业主客户ID */
    @NotNull(message = "新业主不能为空")
    private Long newCustomerId;

    /** 过户生效日期 */
    @NotNull(message = "过户日期不能为空")
    private Date transferDate;

    /** 过户备注 */
    private String transferRemark;

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public Long getNewCustomerId() {
        return newCustomerId;
    }

    public void setNewCustomerId(Long newCustomerId) {
        this.newCustomerId = newCustomerId;
    }

    public Date getTransferDate() {
        return transferDate;
    }

    public void setTransferDate(Date transferDate) {
        this.transferDate = transferDate;
    }

    public String getTransferRemark() {
        return transferRemark;
    }

    public void setTransferRemark(String transferRemark) {
        this.transferRemark = transferRemark;
    }
}
