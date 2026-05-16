package com.zhaoxinms.resi.receivable.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhaoxinms.common.exception.ServiceException;
import com.zhaoxinms.common.utils.SecurityUtils;
import com.zhaoxinms.common.utils.StringUtils;
import com.zhaoxinms.resi.archive.entity.ResiCustomer;
import com.zhaoxinms.resi.archive.entity.ResiCustomerAsset;
import com.zhaoxinms.resi.archive.entity.ResiRoom;
import com.zhaoxinms.resi.archive.service.IResiCustomerAssetService;
import com.zhaoxinms.resi.archive.service.IResiCustomerService;
import com.zhaoxinms.resi.archive.service.IResiRoomService;
import com.zhaoxinms.resi.common.ResiConstants;
import com.zhaoxinms.resi.feeconfig.entity.ResiFeeAllocation;
import com.zhaoxinms.resi.feeconfig.entity.ResiFeeDefinition;
import com.zhaoxinms.resi.feeconfig.service.IResiFeeAllocationService;
import com.zhaoxinms.resi.feeconfig.service.IResiFeeDefinitionService;
import com.zhaoxinms.resi.receivable.dto.ResiReceivableCreateTempReq;
import com.zhaoxinms.resi.receivable.dto.ResiReceivableGenerateReq;
import com.zhaoxinms.resi.receivable.dto.ResiReceivableGenerateVo;
import com.zhaoxinms.resi.receivable.entity.ResiReceivable;
import com.zhaoxinms.resi.receivable.mapper.ResiReceivableMapper;
import com.zhaoxinms.resi.receivable.service.IResiReceivableGenService;
import com.zhaoxinms.util.DynamicExpressiontUtil;

/**
 * 应收账单生成 Service实现
 */
@Service
public class ResiReceivableGenServiceImpl implements IResiReceivableGenService {

    private static final Logger log = LoggerFactory.getLogger(ResiReceivableGenServiceImpl.class);

    @Autowired
    private ResiReceivableMapper receivableMapper;

    @Autowired
    private IResiFeeAllocationService feeAllocationService;

    @Autowired
    private IResiFeeDefinitionService feeDefinitionService;

    @Autowired
    private IResiRoomService roomService;

    @Autowired
    private IResiCustomerAssetService customerAssetService;

    @Autowired
    private IResiCustomerService customerService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResiReceivableGenerateVo batchGenerate(ResiReceivableGenerateReq req) {
        Long projectId = req.getProjectId();
        String billPeriod = req.getBillPeriod();
        String genBatch = "GEN-" + projectId + "-" + billPeriod;

        // 1. 防重复：查询该批次是否已有记录（包括已收和未收）
        int existingCount = receivableMapper.selectCount(
                new QueryWrapper<ResiReceivable>()
                        .eq("gen_batch", genBatch));
        if (existingCount > 0) {
            log.info("批量生成应收跳过，批次已存在 genBatch={} existingCount={}", genBatch, existingCount);
            ResiReceivableGenerateVo skipResult = new ResiReceivableGenerateVo();
            skipResult.setGenBatch(genBatch);
            skipResult.setTotal(existingCount);
            skipResult.setSuccess(0);
            skipResult.setSkip(existingCount);
            return skipResult;
        }

        // 2. 解析账单月份，计算周期起止日
        YearMonth yearMonth = YearMonth.parse(billPeriod);
        LocalDate periodStart = yearMonth.atDay(1);
        LocalDate periodEnd = yearMonth.atEndOfMonth();
        Date periodStartDate = Date.from(periodStart.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date periodEndDate = Date.from(periodEnd.atStartOfDay(ZoneId.systemDefault()).toInstant());

        // 3. 查询有效费用分配（关联费用定义，只取周期费）
        // 条件：enabled=1, fee_type=PERIOD, start_date <= periodEnd, end_date IS NULL OR end_date >= periodStart
        List<ResiFeeAllocation> allocations = queryValidPeriodAllocations(projectId, periodStartDate, periodEndDate);
        int total = allocations.size();
        if (total == 0) {
            ResiReceivableGenerateVo emptyResult = new ResiReceivableGenerateVo();
            emptyResult.setGenBatch(genBatch);
            emptyResult.setTotal(0);
            emptyResult.setSuccess(0);
            emptyResult.setSkip(0);
            return emptyResult;
        }

        Date now = new Date();
        String userId = String.valueOf(SecurityUtils.getUserId());

        List<ResiReceivable> insertList = new ArrayList<>();
        int skip = 0;

        for (ResiFeeAllocation alloc : allocations) {
            // 查询费用定义
            ResiFeeDefinition feeDef = feeDefinitionService.getById(alloc.getFeeId());
            if (feeDef == null || !Integer.valueOf(1).equals(feeDef.getEnabledMark())) {
                skip++;
                continue;
            }

            // 查询资源信息
            ResiRoom room = null;
            if (ResiConstants.RESOURCE_TYPE_ROOM.equals(alloc.getResourceType())) {
                room = roomService.getById(alloc.getResourceId());
            }
            if (room == null && ResiConstants.RESOURCE_TYPE_ROOM.equals(alloc.getResourceType())) {
                skip++;
                continue;
            }

            // 计算 num 和 price
            BigDecimal num;
            BigDecimal price = alloc.getCustomPrice() != null ? alloc.getCustomPrice() : feeDef.getUnitPrice();
            if (price == null) {
                price = BigDecimal.ZERO;
            }

            String calcType = feeDef.getCalcType();
            BigDecimal totalAmount;

            if (ResiConstants.CALC_TYPE_FIXED.equals(calcType)) {
                num = BigDecimal.ONE;
                totalAmount = price;
            } else if (ResiConstants.CALC_TYPE_AREA.equals(calcType)) {
                num = room != null && room.getBuildingArea() != null ? room.getBuildingArea() : BigDecimal.ZERO;
                totalAmount = num.multiply(price);
            } else if (ResiConstants.CALC_TYPE_FORMULA.equals(calcType)) {
                num = room != null && room.getBuildingArea() != null ? room.getBuildingArea() : BigDecimal.ONE;
                String formula = StringUtils.isNotBlank(alloc.getCustomFormula()) ? alloc.getCustomFormula() : feeDef.getFormula();
                if (StringUtils.isBlank(formula)) {
                    skip++;
                    continue;
                }
                try {
                    String resultStr = DynamicExpressiontUtil.getExpressResult(
                            formula, num.toPlainString(), price.toPlainString());
                    totalAmount = new BigDecimal(resultStr);
                } catch (Exception e) {
                    log.warn("公式计算失败 feeId={} formula={} error={}", feeDef.getId(), formula, e.getMessage());
                    skip++;
                    continue;
                }
            } else {
                // USAGE 类型不走批量生成（由抄表入账生成）
                skip++;
                continue;
            }

            // 取整处理
            String roundType = feeDef.getRoundType();
            if (StringUtils.isBlank(roundType)) {
                roundType = ResiConstants.ROUND_TYPE_ROUND;
            }
            if (ResiConstants.ROUND_TYPE_CEIL.equals(roundType)) {
                totalAmount = totalAmount.setScale(2, RoundingMode.CEILING);
            } else if (ResiConstants.ROUND_TYPE_FLOOR.equals(roundType)) {
                totalAmount = totalAmount.setScale(2, RoundingMode.FLOOR);
            } else {
                totalAmount = totalAmount.setScale(2, RoundingMode.HALF_UP);
            }

            // 计算计费周期 beginDate / endDate
            Date[] periodRange = calcBillPeriod(billPeriod, feeDef.getCycleUnit(), feeDef.getCycleValue());

            // 查询当前业主
            Long customerId = null;
            String customerName = null;
            if (room != null) {
                List<ResiCustomerAsset> assets = customerAssetService.lambdaQuery()
                        .eq(ResiCustomerAsset::getAssetType, ResiConstants.ASSET_TYPE_ROOM)
                        .eq(ResiCustomerAsset::getAssetId, room.getId())
                        .eq(ResiCustomerAsset::getIsCurrent, 1)
                        .list();
                if (assets != null && !assets.isEmpty()) {
                    customerId = assets.get(0).getCustomerId();
                    ResiCustomer customer = customerService.getById(customerId);
                    if (customer != null) {
                        customerName = customer.getCustomerName();
                    }
                }
            }

            ResiReceivable receivable = new ResiReceivable();
            receivable.setProjectId(projectId);
            receivable.setResourceType(alloc.getResourceType());
            receivable.setResourceId(alloc.getResourceId());
            receivable.setResourceName(alloc.getResourceName());
            receivable.setCustomerId(customerId);
            receivable.setCustomerName(customerName);
            receivable.setFeeId(feeDef.getId());
            receivable.setFeeName(feeDef.getFeeName());
            receivable.setFeeType(feeDef.getFeeType());
            receivable.setBillPeriod(billPeriod);
            receivable.setBeginDate(periodRange[0]);
            receivable.setEndDate(periodRange[1]);
            receivable.setNum(num);
            receivable.setPrice(price);
            receivable.setTotal(totalAmount);
            receivable.setOverdueFee(BigDecimal.ZERO);
            receivable.setDiscountAmount(BigDecimal.ZERO);
            receivable.setReceivable(totalAmount);
            receivable.setPayState(ResiConstants.PAY_STATE_UNPAID);
            receivable.setPaidAmount(BigDecimal.ZERO);
            receivable.setGenBatch(genBatch);
            receivable.setRemark("周期费批量生成：" + billPeriod);
            receivable.setEnabledMark(1);
            receivable.setCreatorTime(now);
            receivable.setCreatorUserId(userId);
            receivable.setLastModifyTime(now);
            receivable.setLastModifyUserId(userId);

            insertList.add(receivable);
        }

        // 4. 批量插入，500条一批
        int success = 0;
        if (!insertList.isEmpty()) {
            int batchSize = 500;
            for (int i = 0; i < insertList.size(); i += batchSize) {
                List<ResiReceivable> batch = insertList.subList(i, Math.min(i + batchSize, insertList.size()));
                receivableMapper.batchInsert(batch);
                success += batch.size();
            }
        }

        log.info("批量生成应收完成 projectId={} billPeriod={} genBatch={} total={} success={} skip={}",
                projectId, billPeriod, genBatch, total, success, skip);

        ResiReceivableGenerateVo result = new ResiReceivableGenerateVo();
        result.setGenBatch(genBatch);
        result.setTotal(total);
        result.setSuccess(success);
        result.setSkip(skip + (total - insertList.size() - skip)); // 兼容逻辑
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByGenBatch(String genBatch) {
        if (StringUtils.isBlank(genBatch)) {
            return 0;
        }
        // 只删除未收状态的记录（pay_state='0'），已收的不允许删除
        List<ResiReceivable> list = receivableMapper.selectList(
                new QueryWrapper<ResiReceivable>()
                        .eq("gen_batch", genBatch)
                        .eq("pay_state", ResiConstants.PAY_STATE_UNPAID));
        if (list == null || list.isEmpty()) {
            return 0;
        }
        Date now = new Date();
        String userId = String.valueOf(SecurityUtils.getUserId());
        for (ResiReceivable item : list) {
            item.setDeleteTime(now);
            item.setDeleteUserId(userId);
            item.setLastModifyTime(now);
            item.setLastModifyUserId(userId);
            receivableMapper.updateById(item);
        }
        return list.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createTempReceivable(ResiReceivableCreateTempReq req) {
        // 1. 校验费用定义
        ResiFeeDefinition feeDef = feeDefinitionService.getById(req.getFeeId());
        if (feeDef == null) {
            throw new ServiceException("费用定义不存在");
        }
        if (!Integer.valueOf(1).equals(feeDef.getEnabledMark())) {
            throw new ServiceException("费用定义已停用，无法录入");
        }
        // 项目数据隔离校验
        if (!feeDef.getProjectId().equals(req.getProjectId())) {
            throw new ServiceException("费用定义不属于所选项目");
        }

        BigDecimal num = req.getNum();
        BigDecimal price = req.getPrice();
        if (num == null || price == null) {
            throw new ServiceException("数量和单价不能为空");
        }
        BigDecimal total = num.multiply(price);
        total = total.setScale(2, RoundingMode.HALF_UP);

        // 2. 查询资源信息（资源名称 + 当前绑定客户）
        String resourceName = null;
        Long customerId = null;
        String customerName = null;

        if (ResiConstants.RESOURCE_TYPE_ROOM.equals(req.getResourceType())) {
            ResiRoom room = roomService.getById(req.getResourceId());
            if (room == null) {
                throw new ServiceException("所选房间不存在");
            }
            if (!room.getProjectId().equals(req.getProjectId())) {
                throw new ServiceException("房间不属于所选项目");
            }
            resourceName = room.getRoomAlias();
            List<ResiCustomerAsset> assets = customerAssetService.lambdaQuery()
                    .eq(ResiCustomerAsset::getAssetType, ResiConstants.ASSET_TYPE_ROOM)
                    .eq(ResiCustomerAsset::getAssetId, room.getId())
                    .eq(ResiCustomerAsset::getIsCurrent, 1)
                    .list();
            if (assets != null && !assets.isEmpty()) {
                customerId = assets.get(0).getCustomerId();
                ResiCustomer customer = customerService.getById(customerId);
                if (customer != null) {
                    customerName = customer.getCustomerName();
                }
            }
        } else if (ResiConstants.RESOURCE_TYPE_PARKING.equals(req.getResourceType())) {
            // 车位资源：通过通用客户资产绑定查询
            List<ResiCustomerAsset> assets = customerAssetService.lambdaQuery()
                    .eq(ResiCustomerAsset::getAssetType, ResiConstants.ASSET_TYPE_PARKING)
                    .eq(ResiCustomerAsset::getAssetId, req.getResourceId())
                    .eq(ResiCustomerAsset::getIsCurrent, 1)
                    .list();
            if (assets != null && !assets.isEmpty()) {
                customerId = assets.get(0).getCustomerId();
                ResiCustomer customer = customerService.getById(customerId);
                if (customer != null) {
                    customerName = customer.getCustomerName();
                }
            }
        } else if (ResiConstants.RESOURCE_TYPE_STORAGE.equals(req.getResourceType())) {
            // 储藏室资源（按房间处理）
            ResiRoom room = roomService.getById(req.getResourceId());
            if (room != null) {
                resourceName = room.getRoomAlias();
                List<ResiCustomerAsset> assets = customerAssetService.lambdaQuery()
                        .eq(ResiCustomerAsset::getAssetType, ResiConstants.ASSET_TYPE_ROOM)
                        .eq(ResiCustomerAsset::getAssetId, room.getId())
                        .eq(ResiCustomerAsset::getIsCurrent, 1)
                        .list();
                if (assets != null && !assets.isEmpty()) {
                    customerId = assets.get(0).getCustomerId();
                    ResiCustomer customer = customerService.getById(customerId);
                    if (customer != null) {
                        customerName = customer.getCustomerName();
                    }
                }
            }
        }

        Date now = new Date();
        String userId = String.valueOf(SecurityUtils.getUserId());

        ResiReceivable receivable = new ResiReceivable();
        receivable.setProjectId(req.getProjectId());
        receivable.setResourceType(req.getResourceType());
        receivable.setResourceId(req.getResourceId());
        receivable.setResourceName(resourceName);
        receivable.setCustomerId(customerId);
        receivable.setCustomerName(customerName);
        receivable.setFeeId(feeDef.getId());
        receivable.setFeeName(feeDef.getFeeName());
        receivable.setFeeType(ResiConstants.FEE_TYPE_TEMP);
        receivable.setNum(num);
        receivable.setPrice(price);
        receivable.setTotal(total);
        receivable.setOverdueFee(BigDecimal.ZERO);
        receivable.setDiscountAmount(BigDecimal.ZERO);
        receivable.setReceivable(total);
        receivable.setPayState(ResiConstants.PAY_STATE_UNPAID);
        receivable.setPaidAmount(BigDecimal.ZERO);
        receivable.setRemark(req.getRemark());
        receivable.setEnabledMark(1);
        receivable.setCreatorTime(now);
        receivable.setCreatorUserId(userId);
        receivable.setLastModifyTime(now);
        receivable.setLastModifyUserId(userId);

        receivableMapper.insert(receivable);
        log.info("临时费录入成功 projectId={} resourceId={} feeId={} total={}",
                req.getProjectId(), req.getResourceId(), feeDef.getId(), total);
    }

    /**
     * 查询指定项目下在账单月期间有效的周期费分配
     */
    private List<ResiFeeAllocation> queryValidPeriodAllocations(Long projectId, Date periodStart, Date periodEnd) {
        // 查询所有启用的分配记录，再过滤费用定义类型为 PERIOD 的
        QueryWrapper<ResiFeeAllocation> qw = new QueryWrapper<>();
        qw.eq("enabled_mark", 1);
        qw.eq("project_id", projectId);
        // 生效日期 <= 账单月最后一天
        qw.le("start_date", periodEnd);
        // 截止日期为NULL 或 >= 账单月第一天
        qw.and(w -> w.isNull("end_date").or().ge("end_date", periodStart));

        List<ResiFeeAllocation> allocs = feeAllocationService.list(qw);
        List<ResiFeeAllocation> result = new ArrayList<>();
        for (ResiFeeAllocation alloc : allocs) {
            ResiFeeDefinition feeDef = feeDefinitionService.getById(alloc.getFeeId());
            if (feeDef != null && Integer.valueOf(1).equals(feeDef.getEnabledMark())
                    && ResiConstants.FEE_TYPE_PERIOD.equals(feeDef.getFeeType())) {
                result.add(alloc);
            }
        }
        return result;
    }

    /**
     * 计算账单周期起止日
     */
    private Date[] calcBillPeriod(String billPeriod, String cycleUnit, Integer cycleValue) {
        YearMonth ym = YearMonth.parse(billPeriod);
        LocalDate begin = ym.atDay(1);
        LocalDate end;

        if (cycleValue == null || cycleValue < 1) {
            cycleValue = 1;
        }

        if (ResiConstants.CYCLE_UNIT_QUARTER.equals(cycleUnit)) {
            end = begin.plusMonths(3L * cycleValue).minusDays(1);
        } else if (ResiConstants.CYCLE_UNIT_YEAR.equals(cycleUnit)) {
            end = begin.plusMonths(12L * cycleValue).minusDays(1);
        } else {
            // 默认 MONTH
            end = begin.plusMonths(cycleValue).minusDays(1);
        }

        return new Date[]{
                Date.from(begin.atStartOfDay(ZoneId.systemDefault()).toInstant()),
                Date.from(end.atStartOfDay(ZoneId.systemDefault()).toInstant())
        };
    }
}
