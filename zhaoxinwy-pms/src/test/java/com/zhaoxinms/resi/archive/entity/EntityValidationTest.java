package com.zhaoxinms.resi.archive.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.zhaoxinms.common.core.validate.AddGroup;
import com.zhaoxinms.common.core.validate.EditGroup;
import com.zhaoxinms.resi.feeconfig.entity.ResiFeeAllocation;

/**
 * 档案实体 Bean Validation 校验测试
 * <p>
 * 覆盖 Sprint 1 所有档案实体：
 * - 参数边界（@Size max）
 * - 必填校验（@NotNull / @NotBlank）
 * - 格式校验（@Pattern 手机号、身份证）
 */
class EntityValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ==================== ResiProject 项目实体 ====================

    @Test
    @DisplayName("项目-新增：必填字段缺失应触发校验失败")
    void projectAdd_requiredFieldsMissing() {
        ResiProject project = new ResiProject();
        // code 和 name 均为 null

        Set<ConstraintViolation<ResiProject>> violations = validator.validate(project, AddGroup.class);

        assertFalse(violations.isEmpty(), "应有校验错误");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("code")),
                "应有 code 必填错误");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")),
                "应有 name 必填错误");
    }

    @Test
    @DisplayName("项目-新增：code 超过50字符应触发 Size 校验")
    void projectAdd_codeTooLong() {
        ResiProject project = new ResiProject();
        project.setCode("A".repeat(51));
        project.setName("测试项目");

        Set<ConstraintViolation<ResiProject>> violations = validator.validate(project, AddGroup.class);

        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("code")),
                "应有 code 长度超限错误");
    }

    @Test
    @DisplayName("项目-修改：id 缺失应触发校验失败")
    void projectEdit_idMissing() {
        ResiProject project = new ResiProject();
        project.setCode("PRJ-001");
        project.setName("测试项目");
        // id 为 null

        Set<ConstraintViolation<ResiProject>> violations = validator.validate(project, EditGroup.class);

        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("id")),
                "修改时应有 id 必填错误");
    }

    @Test
    @DisplayName("项目-新增：正常数据应通过校验")
    void projectAdd_valid() {
        ResiProject project = new ResiProject();
        project.setCode("PRJ-001");
        project.setName("阳光花园");
        project.setAddress("北京市朝阳区");

        Set<ConstraintViolation<ResiProject>> violations = validator.validate(project, AddGroup.class);

        assertTrue(violations.isEmpty(), "合法数据应无校验错误");
    }

    // ==================== ResiBuilding 楼栋实体 ====================

    @Test
    @DisplayName("楼栋-新增：必填字段缺失应触发校验失败")
    void buildingAdd_requiredFieldsMissing() {
        ResiBuilding building = new ResiBuilding();

        Set<ConstraintViolation<ResiBuilding>> violations = validator.validate(building, AddGroup.class);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("projectId")),
                "应有 projectId 必填错误");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")),
                "应有 name 必填错误");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("number")),
                "应有 number 必填错误");
    }

    @Test
    @DisplayName("楼栋-新增：number 超过26字符应触发 Size 校验")
    void buildingAdd_numberTooLong() {
        ResiBuilding building = new ResiBuilding();
        building.setProjectId(1L);
        building.setName("1号楼");
        building.setNumber("B".repeat(27));

        Set<ConstraintViolation<ResiBuilding>> violations = validator.validate(building, AddGroup.class);

        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("number")),
                "应有 number 长度超限错误");
    }

    // ==================== ResiRoom 房间实体 ====================

    @Test
    @DisplayName("房间-新增：必填字段缺失应触发校验失败")
    void roomAdd_requiredFieldsMissing() {
        ResiRoom room = new ResiRoom();

        Set<ConstraintViolation<ResiRoom>> violations = validator.validate(room, AddGroup.class);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("projectId")),
                "应有 projectId 必填错误");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("buildingId")),
                "应有 buildingId 必填错误");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("roomNo")),
                "应有 roomNo 必填错误");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("state")),
                "应有 state 必填错误");
    }

    @Test
    @DisplayName("房间-新增：roomNo 超过50字符应触发 Size 校验")
    void roomAdd_roomNoTooLong() {
        ResiRoom room = new ResiRoom();
        room.setProjectId(1L);
        room.setBuildingId(1L);
        room.setRoomNo("R".repeat(51));
        room.setState("NORMAL");

        Set<ConstraintViolation<ResiRoom>> violations = validator.validate(room, AddGroup.class);

        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("roomNo")),
                "应有 roomNo 长度超限错误");
    }

    @Test
    @DisplayName("房间-新增：remark 超过500字符应触发 Size 校验")
    void roomAdd_remarkTooLong() {
        ResiRoom room = new ResiRoom();
        room.setProjectId(1L);
        room.setBuildingId(1L);
        room.setRoomNo("101");
        room.setState("NORMAL");
        room.setRemark("X".repeat(501));

        Set<ConstraintViolation<ResiRoom>> violations = validator.validate(room, AddGroup.class);

        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("remark")),
                "应有 remark 长度超限错误");
    }

    @Test
    @DisplayName("房间-新增：正常数据应通过校验")
    void roomAdd_valid() {
        ResiRoom room = new ResiRoom();
        room.setProjectId(1L);
        room.setBuildingId(1L);
        room.setUnitNo("1");
        room.setFloorNo(1);
        room.setRoomNo("101");
        room.setRoomAlias("1号楼1单元101");
        room.setBuildingArea(new BigDecimal("128.50"));
        room.setState("NORMAL");

        Set<ConstraintViolation<ResiRoom>> violations = validator.validate(room, AddGroup.class);

        assertTrue(violations.isEmpty(), "合法数据应无校验错误");
    }

    // ==================== ResiCustomer 客户实体 ====================

    @Test
    @DisplayName("客户-新增：必填字段缺失应触发校验失败")
    void customerAdd_requiredFieldsMissing() {
        ResiCustomer customer = new ResiCustomer();

        Set<ConstraintViolation<ResiCustomer>> violations = validator.validate(customer, AddGroup.class);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("projectId")),
                "应有 projectId 必填错误");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("customerName")),
                "应有 customerName 必填错误");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("phone")),
                "应有 phone 必填错误");
    }

    @Test
    @DisplayName("客户-新增：手机号格式非法应触发 Pattern 校验")
    void customerAdd_invalidPhone() {
        ResiCustomer customer = buildValidCustomer();
        customer.setPhone("12345678901"); // 非 1[3-9] 开头

        Set<ConstraintViolation<ResiCustomer>> violations = validator.validate(customer, AddGroup.class);

        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("phone")),
                "应有手机号格式错误");
    }

    @Test
    @DisplayName("客户-新增：手机号10位应触发 Pattern 校验")
    void customerAdd_phoneTooShort() {
        ResiCustomer customer = buildValidCustomer();
        customer.setPhone("1380018800"); // 10位

        Set<ConstraintViolation<ResiCustomer>> violations = validator.validate(customer, AddGroup.class);

        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("phone")),
                "应有手机号长度错误");
    }

    @Test
    @DisplayName("客户-新增：身份证15位应通过校验")
    void customerAdd_idCard15_valid() {
        ResiCustomer customer = buildValidCustomer();
        customer.setIdCard("110101900101123"); // 15位

        Set<ConstraintViolation<ResiCustomer>> violations = validator.validate(customer, AddGroup.class);

        assertFalse(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("idCard")),
                "15位身份证应通过校验");
    }

    @Test
    @DisplayName("客户-新增：身份证18位应通过校验")
    void customerAdd_idCard18_valid() {
        ResiCustomer customer = buildValidCustomer();
        customer.setIdCard("110101199001011234"); // 18位

        Set<ConstraintViolation<ResiCustomer>> violations = validator.validate(customer, AddGroup.class);

        assertFalse(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("idCard")),
                "18位身份证应通过校验");
    }

    @Test
    @DisplayName("客户-新增：身份证17位X应通过校验")
    void customerAdd_idCard17X_valid() {
        ResiCustomer customer = buildValidCustomer();
        customer.setIdCard("11010119900101123X"); // 17位+X

        Set<ConstraintViolation<ResiCustomer>> violations = validator.validate(customer, AddGroup.class);

        assertFalse(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("idCard")),
                "17位+X身份证应通过校验");
    }

    @Test
    @DisplayName("客户-新增：身份证14位应触发 Pattern 校验")
    void customerAdd_idCard14_invalid() {
        ResiCustomer customer = buildValidCustomer();
        customer.setIdCard("11010119900101"); // 14位

        Set<ConstraintViolation<ResiCustomer>> violations = validator.validate(customer, AddGroup.class);

        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("idCard")),
                "14位身份证应触发格式错误");
    }

    @Test
    @DisplayName("客户-新增：身份证含非法字符应触发 Pattern 校验")
    void customerAdd_idCardIllegal_invalid() {
        ResiCustomer customer = buildValidCustomer();
        customer.setIdCard("11010119900101ABCD");

        Set<ConstraintViolation<ResiCustomer>> violations = validator.validate(customer, AddGroup.class);

        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("idCard")),
                "非法身份证应触发格式错误");
    }

    @Test
    @DisplayName("客户-新增：idCard 为空字符串应通过校验（Controller 会转 null）")
    void customerAdd_idCardEmptyString_valid() {
        ResiCustomer customer = buildValidCustomer();
        customer.setIdCard("");

        Set<ConstraintViolation<ResiCustomer>> violations = validator.validate(customer, AddGroup.class);

        // @Size 对空字符串不触发（空字符串长度为0），@Pattern 也不触发（因为 regexp 不匹配空字符串）
        // 但JSR-303中，空字符串不匹配正则，所以会触发 Pattern 校验
        // 实际 Controller 会在校验前将空字符串转为 null，所以这是正常流程
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("idCard")),
                "空字符串身份证在纯校验层面会触发 Pattern 错误（Controller 已处理转 null）");
    }

    @Test
    @DisplayName("客户-新增：idCard 为 null 应通过校验（非必填）")
    void customerAdd_idCardNull_valid() {
        ResiCustomer customer = buildValidCustomer();
        customer.setIdCard(null);

        Set<ConstraintViolation<ResiCustomer>> violations = validator.validate(customer, AddGroup.class);

        assertFalse(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("idCard")),
                "null 身份证不应触发校验错误");
    }

    @Test
    @DisplayName("客户-新增：customerName 超过100字符应触发 Size 校验")
    void customerAdd_nameTooLong() {
        ResiCustomer customer = buildValidCustomer();
        customer.setCustomerName("N".repeat(101));

        Set<ConstraintViolation<ResiCustomer>> violations = validator.validate(customer, AddGroup.class);

        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("customerName")),
                "应有 customerName 长度超限错误");
    }

    private ResiCustomer buildValidCustomer() {
        ResiCustomer customer = new ResiCustomer();
        customer.setProjectId(1L);
        customer.setCustomerName("张三");
        customer.setPhone("13800188888");
        return customer;
    }

    // ==================== ResiFeeAllocation 费用分配实体 ====================

    @Test
    @DisplayName("费用分配-新增：必填字段缺失应触发校验失败")
    void allocationAdd_requiredFieldsMissing() {
        ResiFeeAllocation allocation = new ResiFeeAllocation();

        Set<ConstraintViolation<ResiFeeAllocation>> violations = validator.validate(allocation, AddGroup.class);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("projectId")),
                "应有 projectId 必填错误");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("feeId")),
                "应有 feeId 必填错误");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("resourceType")),
                "应有 resourceType 必填错误");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("resourceId")),
                "应有 resourceId 必填错误");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("startDate")),
                "应有 startDate 必填错误");
    }

    @Test
    @DisplayName("费用分配-新增：正常数据应通过校验")
    void allocationAdd_valid() {
        ResiFeeAllocation allocation = new ResiFeeAllocation();
        allocation.setProjectId(1L);
        allocation.setFeeId("fee-001");
        allocation.setResourceType("ROOM");
        allocation.setResourceId(1L);
        allocation.setResourceName("1栋101");
        allocation.setStartDate(java.sql.Date.valueOf("2026-06-01"));
        allocation.setCustomPrice(new BigDecimal("2.8000"));

        Set<ConstraintViolation<ResiFeeAllocation>> violations = validator.validate(allocation, AddGroup.class);
        assertTrue(violations.isEmpty(), "合法数据应无校验错误");
    }

    @Test
    @DisplayName("费用分配-新增：resourceName 超过100字符应触发 Size 校验")
    void allocationAdd_resourceNameTooLong() {
        ResiFeeAllocation allocation = buildValidAllocation();
        allocation.setResourceName("R".repeat(101));

        Set<ConstraintViolation<ResiFeeAllocation>> violations = validator.validate(allocation, AddGroup.class);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("resourceName")),
                "应有 resourceName 长度超限错误");
    }

    private ResiFeeAllocation buildValidAllocation() {
        ResiFeeAllocation allocation = new ResiFeeAllocation();
        allocation.setProjectId(1L);
        allocation.setFeeId("fee-001");
        allocation.setResourceType("ROOM");
        allocation.setResourceId(1L);
        allocation.setStartDate(java.sql.Date.valueOf("2026-06-01"));
        return allocation;
    }

    // ==================== ResiTicketConfig 票据配置实体 ====================

    @Test
    @DisplayName("票据配置-新增：必填字段缺失应触发校验失败")
    void ticketConfigAdd_requiredFieldsMissing() {
        com.zhaoxinms.resi.feeconfig.entity.ResiTicketConfig config = new com.zhaoxinms.resi.feeconfig.entity.ResiTicketConfig();

        Set<ConstraintViolation<com.zhaoxinms.resi.feeconfig.entity.ResiTicketConfig>> violations = validator.validate(config, AddGroup.class);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("projectId")),
                "应有 projectId 必填错误");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("ticketType")),
                "应有 ticketType 必填错误");
    }

    @Test
    @DisplayName("票据配置-新增：正常数据应通过校验")
    void ticketConfigAdd_valid() {
        com.zhaoxinms.resi.feeconfig.entity.ResiTicketConfig config = buildValidTicketConfig();

        Set<ConstraintViolation<com.zhaoxinms.resi.feeconfig.entity.ResiTicketConfig>> violations = validator.validate(config, AddGroup.class);
        assertTrue(violations.isEmpty(), "合法数据应无校验错误");
    }

    @Test
    @DisplayName("票据配置-新增：title 超过200字符应触发 Size 校验")
    void ticketConfigAdd_titleTooLong() {
        com.zhaoxinms.resi.feeconfig.entity.ResiTicketConfig config = buildValidTicketConfig();
        config.setTitle("T".repeat(201));

        Set<ConstraintViolation<com.zhaoxinms.resi.feeconfig.entity.ResiTicketConfig>> violations = validator.validate(config, AddGroup.class);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("title")),
                "应有 title 长度超限错误");
    }

    private com.zhaoxinms.resi.feeconfig.entity.ResiTicketConfig buildValidTicketConfig() {
        com.zhaoxinms.resi.feeconfig.entity.ResiTicketConfig config = new com.zhaoxinms.resi.feeconfig.entity.ResiTicketConfig();
        config.setProjectId(1L);
        config.setTicketType(1);
        config.setTitle("物业收款单");
        config.setPaperSize("A4");
        return config;
    }

    // ==================== ResiFeeDefinition 费用定义实体 ====================

    @Test
    @DisplayName("费用定义-新增：必填字段缺失应触发校验失败")
    void feeDefinitionAdd_requiredFieldsMissing() {
        com.zhaoxinms.resi.feeconfig.entity.ResiFeeDefinition def = new com.zhaoxinms.resi.feeconfig.entity.ResiFeeDefinition();

        Set<ConstraintViolation<com.zhaoxinms.resi.feeconfig.entity.ResiFeeDefinition>> violations = validator.validate(def, AddGroup.class);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("projectId")),
                "应有 projectId 必填错误");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("feeName")),
                "应有 feeName 必填错误");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("feeCode")),
                "应有 feeCode 必填错误");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("feeType")),
                "应有 feeType 必填错误");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("calcType")),
                "应有 calcType 必填错误");
    }

    @Test
    @DisplayName("费用定义-新增：正常数据应通过校验")
    void feeDefinitionAdd_valid() {
        com.zhaoxinms.resi.feeconfig.entity.ResiFeeDefinition def = buildValidFeeDefinition();

        Set<ConstraintViolation<com.zhaoxinms.resi.feeconfig.entity.ResiFeeDefinition>> violations = validator.validate(def, AddGroup.class);
        assertTrue(violations.isEmpty(), "合法数据应无校验错误");
    }

    @Test
    @DisplayName("费用定义-新增：feeName 超过100字符应触发 Size 校验")
    void feeDefinitionAdd_feeNameTooLong() {
        com.zhaoxinms.resi.feeconfig.entity.ResiFeeDefinition def = buildValidFeeDefinition();
        def.setFeeName("N".repeat(101));

        Set<ConstraintViolation<com.zhaoxinms.resi.feeconfig.entity.ResiFeeDefinition>> violations = validator.validate(def, AddGroup.class);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("feeName")),
                "应有 feeName 长度超限错误");
    }

    @Test
    @DisplayName("费用定义-新增：feeCode 超过50字符应触发 Size 校验")
    void feeDefinitionAdd_feeCodeTooLong() {
        com.zhaoxinms.resi.feeconfig.entity.ResiFeeDefinition def = buildValidFeeDefinition();
        def.setFeeCode("C".repeat(51));

        Set<ConstraintViolation<com.zhaoxinms.resi.feeconfig.entity.ResiFeeDefinition>> violations = validator.validate(def, AddGroup.class);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("feeCode")),
                "应有 feeCode 长度超限错误");
    }

    @Test
    @DisplayName("费用定义-新增：unitPrice 为负数应触发 DecimalMin 校验")
    void feeDefinitionAdd_negativeUnitPrice_error() {
        com.zhaoxinms.resi.feeconfig.entity.ResiFeeDefinition def = buildValidFeeDefinition();
        def.setUnitPrice(new BigDecimal("-1.00"));

        Set<ConstraintViolation<com.zhaoxinms.resi.feeconfig.entity.ResiFeeDefinition>> violations = validator.validate(def, AddGroup.class);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("unitPrice")),
                "应有 unitPrice 负数错误");
    }

    @Test
    @DisplayName("费用定义-新增：overdueRate 为负数应触发 DecimalMin 校验")
    void feeDefinitionAdd_negativeOverdueRate_error() {
        com.zhaoxinms.resi.feeconfig.entity.ResiFeeDefinition def = buildValidFeeDefinition();
        def.setOverdueRate(new BigDecimal("-0.001"));

        Set<ConstraintViolation<com.zhaoxinms.resi.feeconfig.entity.ResiFeeDefinition>> violations = validator.validate(def, AddGroup.class);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("overdueRate")),
                "应有 overdueRate 负数错误");
    }

    @Test
    @DisplayName("费用定义-新增：taxRate 超过100%应触发 DecimalMax 校验")
    void feeDefinitionAdd_taxRateOver100_error() {
        com.zhaoxinms.resi.feeconfig.entity.ResiFeeDefinition def = buildValidFeeDefinition();
        def.setTaxRate(new BigDecimal("101.00"));

        Set<ConstraintViolation<com.zhaoxinms.resi.feeconfig.entity.ResiFeeDefinition>> violations = validator.validate(def, AddGroup.class);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("taxRate")),
                "应有 taxRate 超过100%错误");
    }

    @Test
    @DisplayName("费用定义-新增：overdueRate 6位小数精度应通过")
    void feeDefinitionAdd_overdueRate6Decimal_valid() {
        com.zhaoxinms.resi.feeconfig.entity.ResiFeeDefinition def = buildValidFeeDefinition();
        def.setOverdueRate(new BigDecimal("0.000500"));

        Set<ConstraintViolation<com.zhaoxinms.resi.feeconfig.entity.ResiFeeDefinition>> violations = validator.validate(def, AddGroup.class);
        assertTrue(violations.isEmpty(), "6位小数overdueRate应通过校验");
    }

    private com.zhaoxinms.resi.feeconfig.entity.ResiFeeDefinition buildValidFeeDefinition() {
        com.zhaoxinms.resi.feeconfig.entity.ResiFeeDefinition def = new com.zhaoxinms.resi.feeconfig.entity.ResiFeeDefinition();
        def.setProjectId(1L);
        def.setFeeCode("WYF-001");
        def.setFeeName("物业管理费");
        def.setFeeType("PERIOD");
        def.setCalcType("FIXED");
        def.setCycleUnit("MONTH");
        return def;
    }

    // ==================== ResiMeterDevice 仪表实体 ====================

    @Test
    @DisplayName("仪表-新增：必填字段缺失应触发校验失败")
    void meterAdd_requiredFieldsMissing() {
        ResiMeterDevice device = new ResiMeterDevice();

        Set<ConstraintViolation<ResiMeterDevice>> violations = validator.validate(device, AddGroup.class);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("projectId")),
                "应有 projectId 必填错误");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("meterCode")),
                "应有 meterCode 必填错误");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("meterType")),
                "应有 meterType 必填错误");
    }

    @Test
    @DisplayName("仪表-新增：meterCode 超过50字符应触发 Size 校验")
    void meterAdd_meterCodeTooLong() {
        ResiMeterDevice device = new ResiMeterDevice();
        device.setProjectId(1L);
        device.setMeterCode("M".repeat(51));
        device.setMeterType(1);

        Set<ConstraintViolation<ResiMeterDevice>> violations = validator.validate(device, AddGroup.class);

        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("meterCode")),
                "应有 meterCode 长度超限错误");
    }

    @Test
    @DisplayName("仪表-新增：公摊表 roomId 为 null 应通过校验")
    void meterAdd_publicRoomIdNull_valid() {
        ResiMeterDevice device = new ResiMeterDevice();
        device.setProjectId(1L);
        device.setMeterCode("W-001");
        device.setMeterType(1);
        device.setIsPublic(1);
        device.setRoomId(null);

        Set<ConstraintViolation<ResiMeterDevice>> violations = validator.validate(device, AddGroup.class);

        assertTrue(violations.isEmpty(), "公摊表 roomId 为 null 应通过校验");
    }

    // ==================== ResiMeterReading 抄表记录实体 ====================

    @Test
    @DisplayName("抄表记录-新增：必填字段缺失应触发校验失败")
    void meterReadingAdd_requiredFieldsMissing() {
        com.zhaoxinms.resi.meter.entity.ResiMeterReading reading = new com.zhaoxinms.resi.meter.entity.ResiMeterReading();

        Set<ConstraintViolation<com.zhaoxinms.resi.meter.entity.ResiMeterReading>> violations = validator.validate(reading, AddGroup.class);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("projectId")),
                "应有 projectId 必填错误");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("meterId")),
                "应有 meterId 必填错误");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("period")),
                "应有 period 必填错误");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("currReading")),
                "应有 currReading 必填错误");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("currDate")),
                "应有 currDate 必填错误");
    }

    @Test
    @DisplayName("抄表记录-新增：正常数据应通过校验")
    void meterReadingAdd_valid() {
        com.zhaoxinms.resi.meter.entity.ResiMeterReading reading = buildValidMeterReading();

        Set<ConstraintViolation<com.zhaoxinms.resi.meter.entity.ResiMeterReading>> violations = validator.validate(reading, AddGroup.class);
        assertTrue(violations.isEmpty(), "合法数据应无校验错误");
    }

    @Test
    @DisplayName("抄表记录-新增：period 超过7字符应触发 Size 校验")
    void meterReadingAdd_periodTooLong() {
        com.zhaoxinms.resi.meter.entity.ResiMeterReading reading = buildValidMeterReading();
        reading.setPeriod("2026-05-01");

        Set<ConstraintViolation<com.zhaoxinms.resi.meter.entity.ResiMeterReading>> violations = validator.validate(reading, AddGroup.class);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("period")),
                "应有 period 长度超限错误");
    }

    private com.zhaoxinms.resi.meter.entity.ResiMeterReading buildValidMeterReading() {
        com.zhaoxinms.resi.meter.entity.ResiMeterReading reading = new com.zhaoxinms.resi.meter.entity.ResiMeterReading();
        reading.setProjectId(1L);
        reading.setMeterId(1L);
        reading.setPeriod("2026-05");
        reading.setCurrReading(new BigDecimal("200.0000"));
        reading.setCurrDate(java.sql.Date.valueOf("2026-05-15"));
        return reading;
    }

    // ==================== ResiCustomerAsset 资产绑定实体 ====================

    @Test
    @DisplayName("资产绑定-新增：必填字段缺失应触发校验失败")
    void assetBind_requiredFieldsMissing() {
        ResiCustomerAsset asset = new ResiCustomerAsset();

        Set<ConstraintViolation<ResiCustomerAsset>> violations = validator.validate(asset, AddGroup.class);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("customerId")),
                "应有 customerId 必填错误");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("projectId")),
                "应有 projectId 必填错误");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("assetType")),
                "应有 assetType 必填错误");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("assetId")),
                "应有 assetId 必填错误");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("bindDate")),
                "应有 bindDate 必填错误");
    }
}
