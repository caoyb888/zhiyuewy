package com.zhaoxinms.resi.common;

/**
 * 住宅物业收费模块常量定义
 * 对应 DATABASE_DESIGN 第5章枚举值域字典
 *
 * @author zhaoxinms
 */
public class ResiConstants {

    // ==================== 费用相关 ====================

    /**
     * 费用类型
     */
    public static final String FEE_TYPE_PERIOD = "PERIOD";
    public static final String FEE_TYPE_TEMP = "TEMP";
    public static final String FEE_TYPE_DEPOSIT = "DEPOSIT";
    public static final String FEE_TYPE_PRE = "PRE";

    /**
     * 计费方式
     */
    public static final String CALC_TYPE_FIXED = "FIXED";
    public static final String CALC_TYPE_AREA = "AREA";
    public static final String CALC_TYPE_USAGE = "USAGE";
    public static final String CALC_TYPE_FORMULA = "FORMULA";

    /**
     * 计费周期单位
     */
    public static final String CYCLE_UNIT_MONTH = "MONTH";
    public static final String CYCLE_UNIT_QUARTER = "QUARTER";
    public static final String CYCLE_UNIT_YEAR = "YEAR";

    /**
     * 滞纳金计算类型
     */
    public static final String OVERDUE_TYPE_DAY = "DAY";
    public static final String OVERDUE_TYPE_MONTH = "MONTH";

    /**
     * 金额取整方式
     */
    public static final String ROUND_TYPE_ROUND = "ROUND";
    public static final String ROUND_TYPE_CEIL = "CEIL";
    public static final String ROUND_TYPE_FLOOR = "FLOOR";

    /**
     * 专款专冲标记
     */
    public static final int EARMARK_ENABLE_YES = 1;
    public static final int EARMARK_ENABLE_NO = 0;

    // ==================== 账单与状态 ====================

    /**
     * 缴费状态（resi_receivable.pay_state）
     */
    public static final String PAY_STATE_UNPAID = "0";
    public static final String PAY_STATE_PART_PAID = "1";
    public static final String PAY_STATE_PAID = "2";
    public static final String PAY_STATE_WAIVED = "3";

    /**
     * 抄表状态（resi_meter_reading.status）
     */
    public static final String METER_STATUS_INPUT = "INPUT";
    public static final String METER_STATUS_BILLED = "BILLED";
    public static final String METER_STATUS_VERIFIED = "VERIFIED";

    /**
     * 收款流水类型（resi_pay_log.pay_type）
     */
    public static final String PAY_TYPE_COLLECT = "COLLECT";
    public static final String PAY_TYPE_REFUND = "REFUND";
    public static final String PAY_TYPE_WRITEOFF = "WRITEOFF";

    /**
     * 支付方式（resi_pay_log.pay_method / resi_deposit.pay_method）
     */
    public static final String PAY_METHOD_CASH = "CASH";
    public static final String PAY_METHOD_WECHAT = "WECHAT";
    public static final String PAY_METHOD_TRANSFER = "TRANSFER";
    public static final String PAY_METHOD_BANK = "BANK";
    public static final String PAY_METHOD_OTHER = "OTHER";

    /**
     * 预收款操作类型（resi_pre_pay.op_type）
     */
    public static final String PRE_PAY_OP_IN = "IN";
    public static final String PRE_PAY_OP_OUT = "OUT";
    public static final String PRE_PAY_OP_REFUND = "REFUND";

    /**
     * 押金状态（resi_deposit.state）
     */
    public static final String DEPOSIT_STATE_COLLECTED = "COLLECTED";
    public static final String DEPOSIT_STATE_REFUNDED = "REFUNDED";

    // ==================== 档案状态 ====================

    /**
     * 房间使用状态（resi_room.state）
     */
    public static final String ROOM_STATE_NORMAL = "NORMAL";
    public static final String ROOM_STATE_VACANT = "VACANT";
    public static final String ROOM_STATE_DECORATING = "DECORATING";
    public static final String ROOM_STATE_TRANSFERRED = "TRANSFERRED";

    /**
     * 房间类型（resi_room.room_type）
     */
    public static final int ROOM_TYPE_RESIDENTIAL = 1;
    public static final int ROOM_TYPE_SHOP = 2;
    public static final int ROOM_TYPE_GARAGE = 3;
    public static final int ROOM_TYPE_STORAGE = 4;

    /**
     * 仪表类型（resi_meter_device.meter_type）
     */
    public static final int METER_TYPE_WATER = 1;
    public static final int METER_TYPE_ELECTRIC = 2;
    public static final int METER_TYPE_GAS = 3;
    public static final int METER_TYPE_HEATING = 4;

    /**
     * 客户类型（resi_customer.customer_type）
     */
    public static final int CUSTOMER_TYPE_OWNER = 1;
    public static final int CUSTOMER_TYPE_TENANT = 2;
    public static final int CUSTOMER_TYPE_TEMP = 3;

    /**
     * 资源类型（多表通用）
     */
    public static final String RESOURCE_TYPE_ROOM = "ROOM";
    public static final String RESOURCE_TYPE_PARKING = "PARKING";
    public static final String RESOURCE_TYPE_STORAGE = "STORAGE";

    /**
     * 资产类型（resi_customer_asset.asset_type）
     */
    public static final int ASSET_TYPE_ROOM = 1;
    public static final int ASSET_TYPE_PARKING = 2;
    public static final int ASSET_TYPE_STORAGE = 3;

    /**
     * 车位使用状态（resi_parking_space.state）
     */
    public static final String PARKING_STATE_IDLE = "IDLE";
    public static final String PARKING_STATE_OCCUPIED = "OCCUPIED";
    public static final String PARKING_STATE_SOLD = "SOLD";

    /**
     * 车位类型（resi_parking_space.space_type）
     */
    public static final int PARKING_TYPE_ABOVEGROUND = 1;
    public static final int PARKING_TYPE_UNDERGROUND = 2;
    public static final int PARKING_TYPE_MECHANICAL = 3;

    /**
     * 产权类型（resi_parking_space.property_type）
     */
    public static final int PROPERTY_TYPE_OWNED = 1;
    public static final int PROPERTY_TYPE_RENT = 2;
    public static final int PROPERTY_TYPE_PUBLIC = 3;

    // ==================== C端相关 ====================

    /**
     * 公告类型（resi_notice.notice_type）
     */
    public static final int NOTICE_TYPE_NORMAL = 1;
    public static final int NOTICE_TYPE_URGENT = 2;
    public static final int NOTICE_TYPE_ACTIVITY = 3;

    /**
     * 公告推送范围（resi_notice.target_type）
     */
    public static final int TARGET_TYPE_ALL = 1;
    public static final int TARGET_TYPE_BUILDING = 2;
    public static final int TARGET_TYPE_ROOM = 3;

    /**
     * 公告状态（resi_notice.status）
     */
    public static final int NOTICE_STATUS_DRAFT = 0;
    public static final int NOTICE_STATUS_PUBLISHED = 1;
    public static final int NOTICE_STATUS_REVOKED = 2;

    /**
     * 推送场景（resi_push_record.push_scene）
     */
    public static final String PUSH_SCENE_ARREARS = "ARREARS";
    public static final String PUSH_SCENE_PAY_SUCCESS = "PAY_SUCCESS";
    public static final String PUSH_SCENE_NOTICE = "NOTICE";
    public static final String PUSH_SCENE_DAILY_REPORT = "DAILY_REPORT";

    /**
     * 推送方式（resi_push_record.push_type）
     */
    public static final int PUSH_TYPE_WECHAT = 1;
    public static final int PUSH_TYPE_SMS = 2;
    public static final int PUSH_TYPE_STATION = 3;

    /**
     * 推送状态（resi_push_record.status）
     */
    public static final int PUSH_STATUS_PENDING = 0;
    public static final int PUSH_STATUS_SUCCESS = 1;
    public static final int PUSH_STATUS_FAIL = 2;

    /**
     * 发票类型（resi_invoice_record.invoice_type）
     */
    public static final String INVOICE_TYPE_VAT_NORMAL = "VAT_NORMAL";
    public static final String INVOICE_TYPE_VAT_SPECIAL = "VAT_SPECIAL";
    public static final String INVOICE_TYPE_E_INVOICE = "E_INVOICE";

    /**
     * 调账类型（resi_adjust_log.adjust_type）
     */
    public static final String ADJUST_TYPE_AMOUNT = "AMOUNT";
    public static final String ADJUST_TYPE_PERIOD = "PERIOD";
    public static final String ADJUST_TYPE_STATUS = "STATUS";
    public static final String ADJUST_TYPE_OVERDUE_WAIVE = "OVERDUE_WAIVE";

    /**
     * 操作端来源（resi_pay_log.client）
     */
    public static final int CLIENT_B_END = 1;
    public static final int CLIENT_C_END = 2;

    // ==================== Redis Key 前缀 ====================

    /**
     * Redis Key 前缀规范：resi:{模块}:{项目ID}:{业务标识}
     */
    public static final String REDIS_PREFIX = "resi:";
    public static final String REDIS_DASHBOARD_PREFIX = REDIS_PREFIX + "dashboard:";
    public static final String REDIS_PAY_NOTIFY_PREFIX = REDIS_PREFIX + "pay:notify:";
    public static final String REDIS_RECEIPT_NO_PREFIX = REDIS_PREFIX + "receipt:no:";
    public static final String REDIS_METER_IMPORT_PREFIX = REDIS_PREFIX + "meter:import:";
    public static final String REDIS_PAY_CREATING_PREFIX = REDIS_PREFIX + "pay:creating:";

    // ==================== 收据流水规则编码 ====================

    /**
     * 住宅收据流水规则编码（base_billrule.en_code）
     */
    public static final String BILL_RULE_RESI_RECEIPT = "RESI_RECEIPT";

    // ==================== 有效标志 ====================

    /**
     * 有效标志（档案类表通用）
     */
    public static final int ENABLED_MARK_YES = 1;
    public static final int ENABLED_MARK_NO = 0;

    private ResiConstants() {
        throw new UnsupportedOperationException("常量类不可实例化");
    }
}
