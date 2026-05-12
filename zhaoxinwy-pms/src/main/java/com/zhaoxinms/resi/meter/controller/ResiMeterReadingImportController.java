package com.zhaoxinms.resi.meter.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.zhaoxinms.common.annotation.Log;
import com.zhaoxinms.common.core.controller.BaseController;
import com.zhaoxinms.common.core.domain.AjaxResult;
import com.zhaoxinms.common.enums.BusinessType;
import com.zhaoxinms.common.exception.ServiceException;
import com.zhaoxinms.resi.common.annotation.ResiProjectScope;
import com.zhaoxinms.resi.meter.dto.ResiMeterReadingImportConfirmReq;
import com.zhaoxinms.resi.meter.dto.ResiMeterReadingImportPreviewVo;
import com.zhaoxinms.resi.meter.dto.ResiMeterReadingImportResultVo;
import com.zhaoxinms.resi.meter.service.IResiMeterReadingImportService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 抄表Excel批量导入控制器（三步模式）
 *
 * 三步流程：
 * 1. GET /template   - 下载导入模板（含仪表编号、上期读数）
 * 2. POST /upload    - 上传Excel，解析并返回预览（含错误/警告行高亮）
 * 3. POST /confirm   - 确认导入，数据从Redis写入DB
 *
 * @author zhaoxinms
 */
@Api(tags = "住宅收费-抄表批量导入")
@RestController
@RequestMapping("/resi/meter/reading/import")
public class ResiMeterReadingImportController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(ResiMeterReadingImportController.class);

    /** 文件大小限制：10MB */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @Autowired
    private IResiMeterReadingImportService importService;

    /**
     * 步骤一：下载导入模板
     *
     * @param projectId 项目ID
     * @param period    抄表期间（yyyy-MM），可选，预填到模板中
     * @param response  HTTP响应
     */
    @ApiOperation("下载抄表导入模板")
    @PreAuthorize("@ss.hasPermi('resi:meter:import')")
    @ResiProjectScope
    @GetMapping("/template")
    public void template(@RequestParam("projectId") Long projectId,
                         @RequestParam(value = "period", required = false) String period,
                         HttpServletResponse response) throws IOException {
        importService.downloadTemplate(projectId, period, response);
    }

    /**
     * 步骤二：上传Excel并预览
     *
     * @param projectId 项目ID
     * @param period    抄表期间（yyyy-MM）
     * @param file      Excel文件
     * @return 预览结果（含正常/警告/错误行统计及明细）
     */
    @ApiOperation("上传抄表Excel并预览")
    @PreAuthorize("@ss.hasPermi('resi:meter:import')")
    @Log(title = "住宅收费-抄表批量导入", businessType = BusinessType.IMPORT)
    @ResiProjectScope
    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam("projectId") Long projectId,
                             @RequestParam(value = "period", required = false) String period,
                             @RequestParam("file") MultipartFile file) {
        // 文件非空校验
        if (file == null || file.isEmpty()) {
            return AjaxResult.error("上传文件不能为空");
        }

        // 文件大小校验
        if (file.getSize() > MAX_FILE_SIZE) {
            return AjaxResult.error("文件大小超出限制10MB");
        }

        // 文件类型校验
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null
                || (!originalFilename.endsWith(".xlsx") && !originalFilename.endsWith(".xls"))) {
            return AjaxResult.error("请选择Excel文件（.xlsx或.xls）");
        }

        try {
            ResiMeterReadingImportPreviewVo preview = importService.uploadPreview(projectId, period, file.getInputStream());
            return AjaxResult.success(preview);
        } catch (ServiceException e) {
            return AjaxResult.error(e.getMessage());
        } catch (Exception e) {
            log.error("抄表导入预览异常 projectId={} period={}", projectId, period, e);
            return AjaxResult.error("文件解析失败：" + e.getMessage());
        }
    }

    /**
     * 步骤三：确认导入
     *
     * @param req 确认请求（含batchId）
     * @return 导入结果统计
     */
    @ApiOperation("确认抄表导入")
    @PreAuthorize("@ss.hasPermi('resi:meter:import')")
    @Log(title = "住宅收费-抄表批量导入确认", businessType = BusinessType.INSERT)
    @PostMapping("/confirm")
    public AjaxResult confirm(@RequestBody @Validated ResiMeterReadingImportConfirmReq req) {
        try {
            ResiMeterReadingImportResultVo result = importService.confirmImport(req);
            return AjaxResult.success(result);
        } catch (ServiceException e) {
            return AjaxResult.error(e.getMessage());
        } catch (Exception e) {
            log.error("抄表导入确认异常 batchId={}", req.getBatchId(), e);
            return AjaxResult.error("导入失败：" + e.getMessage());
        }
    }
}
