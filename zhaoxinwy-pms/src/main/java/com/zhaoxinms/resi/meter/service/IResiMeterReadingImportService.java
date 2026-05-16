package com.zhaoxinms.resi.meter.service;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import com.zhaoxinms.resi.meter.dto.ResiMeterReadingImportConfirmReq;
import com.zhaoxinms.resi.meter.dto.ResiMeterReadingImportPreviewVo;
import com.zhaoxinms.resi.meter.dto.ResiMeterReadingImportResultVo;

/**
 * 抄表Excel导入Service接口
 *
 * @author zhaoxinms
 */
public interface IResiMeterReadingImportService {

    /**
     * 下载导入模板
     *
     * @param projectId 项目ID
     * @param period    抄表期间（yyyy-MM）
     * @param response  HTTP响应
     * @throws IOException IO异常
     */
    void downloadTemplate(Long projectId, String period, HttpServletResponse response) throws IOException;

    /**
     * 上传Excel并预览
     *
     * @param projectId 项目ID
     * @param period    抄表期间（yyyy-MM）
     * @param is        Excel输入流
     * @return 预览结果
     */
    ResiMeterReadingImportPreviewVo uploadPreview(Long projectId, String period, InputStream is) throws Exception;

    /**
     * 确认导入（从Redis取出数据写入DB）
     *
     * @param req 确认请求
     * @return 导入结果
     */
    ResiMeterReadingImportResultVo confirmImport(ResiMeterReadingImportConfirmReq req);
}
