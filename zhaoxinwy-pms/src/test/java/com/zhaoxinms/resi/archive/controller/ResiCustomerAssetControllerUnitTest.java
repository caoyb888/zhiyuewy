package com.zhaoxinms.resi.archive.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zhaoxinms.common.core.domain.AjaxResult;
import com.zhaoxinms.resi.archive.entity.ResiCustomerAsset;
import com.zhaoxinms.resi.archive.service.IResiCustomerAssetService;
import com.zhaoxinms.resi.TestSecurityContext;

/**
 * Customer Asset Binding Controller Unit Test
 * Covers: duplicate binding prevention, unbind idempotency
 */
@ExtendWith(MockitoExtension.class)
class ResiCustomerAssetControllerUnitTest {

    @Mock
    private IResiCustomerAssetService assetService;

    @InjectMocks
    private ResiCustomerAssetController controller;

    @BeforeEach
    void setUp() {
        TestSecurityContext.setNormalUser();
    }

    @AfterEach
    void tearDown() {
        TestSecurityContext.clear();
    }

    @Test
    @DisplayName("bind: new binding should succeed")
    void bindAsset_newBinding_success() {
        ResiCustomerAsset asset = new ResiCustomerAsset();
        asset.setCustomerId(1L);
        asset.setProjectId(1L);
        asset.setAssetType(1);
        asset.setAssetId(101L);

        when(assetService.selectCurrentBinding(1, 101L)).thenReturn(null);
        when(assetService.save(any(ResiCustomerAsset.class))).thenReturn(true);

        AjaxResult result = controller.bindAsset(asset);
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("bind: asset bound by other customer should fail")
    void bindAsset_boundByOther_error() {
        ResiCustomerAsset asset = new ResiCustomerAsset();
        asset.setCustomerId(2L);
        asset.setProjectId(1L);
        asset.setAssetType(1);
        asset.setAssetId(101L);

        ResiCustomerAsset existing = new ResiCustomerAsset();
        existing.setId(100L);
        existing.setCustomerId(1L); // other customer
        existing.setAssetType(1);
        existing.setAssetId(101L);
        existing.setIsCurrent(1);

        when(assetService.selectCurrentBinding(1, 101L)).thenReturn(existing);

        AjaxResult result = controller.bindAsset(asset);
        assertEquals(500, result.get(AjaxResult.CODE_TAG));
        String msg = (String) result.get(AjaxResult.MSG_TAG);
        assertTrue(msg.contains("已被其他客户绑定"));
    }

    @Test
    @DisplayName("bind: same customer same asset should fail")
    void bindAsset_sameCustomerSameAsset_error() {
        ResiCustomerAsset asset = new ResiCustomerAsset();
        asset.setCustomerId(1L);
        asset.setProjectId(1L);
        asset.setAssetType(1);
        asset.setAssetId(101L);

        ResiCustomerAsset existing = new ResiCustomerAsset();
        existing.setId(100L);
        existing.setCustomerId(1L); // same customer
        existing.setAssetType(1);
        existing.setAssetId(101L);
        existing.setIsCurrent(1);

        when(assetService.selectCurrentBinding(1, 101L)).thenReturn(existing);

        AjaxResult result = controller.bindAsset(asset);
        assertEquals(500, result.get(AjaxResult.CODE_TAG));
        String msg = (String) result.get(AjaxResult.MSG_TAG);
        assertTrue(msg.contains("已绑定"));
    }

    @Test
    @DisplayName("bind: should set bindDate and isCurrent")
    void bindAsset_shouldSetDefaults() {
        ResiCustomerAsset asset = new ResiCustomerAsset();
        asset.setCustomerId(1L);
        asset.setProjectId(1L);
        asset.setAssetType(1);
        asset.setAssetId(101L);

        when(assetService.selectCurrentBinding(1, 101L)).thenReturn(null);
        when(assetService.save(any(ResiCustomerAsset.class))).thenAnswer(inv -> {
            ResiCustomerAsset saved = inv.getArgument(0);
            assertNotNull(saved.getBindDate());
            assertEquals(Integer.valueOf(1), saved.getIsCurrent());
            return true;
        });

        AjaxResult result = controller.bindAsset(asset);
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("unbind: normal unbind should succeed")
    void unbindAsset_normal_success() {
        Long id = 100L;
        ResiCustomerAsset asset = new ResiCustomerAsset();
        asset.setId(id);
        asset.setIsCurrent(1);

        when(assetService.getById(id)).thenReturn(asset);
        when(assetService.updateById(any(ResiCustomerAsset.class))).thenAnswer(inv -> {
            ResiCustomerAsset updated = inv.getArgument(0);
            assertEquals(Integer.valueOf(0), updated.getIsCurrent());
            assertNotNull(updated.getUnbindDate());
            return true;
        });

        AjaxResult result = controller.unbindAsset(id);
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("unbind: already unbound should fail")
    void unbindAsset_alreadyUnbound_error() {
        Long id = 100L;
        ResiCustomerAsset asset = new ResiCustomerAsset();
        asset.setId(id);
        asset.setIsCurrent(0);

        when(assetService.getById(id)).thenReturn(asset);

        AjaxResult result = controller.unbindAsset(id);
        assertEquals(500, result.get(AjaxResult.CODE_TAG));
        String msg = (String) result.get(AjaxResult.MSG_TAG);
        assertTrue(msg.contains("已解绑"));
    }

    @Test
    @DisplayName("unbind: non-existent record should fail")
    void unbindAsset_notFound_error() {
        Long id = 999L;
        when(assetService.getById(id)).thenReturn(null);

        AjaxResult result = controller.unbindAsset(id);
        assertEquals(500, result.get(AjaxResult.CODE_TAG));
        String msg = (String) result.get(AjaxResult.MSG_TAG);
        assertTrue(msg.contains("不存在"));
    }

    @Test
    @DisplayName("assets: should return asset list")
    void assets_shouldReturnList() {
        Long customerId = 1L;
        ResiCustomerAsset a1 = new ResiCustomerAsset();
        a1.setId(1L);
        a1.setAssetName("Room 101");

        when(assetService.selectAssetsByCustomerId(customerId)).thenReturn(Arrays.asList(a1));

        AjaxResult result = controller.assets(customerId);
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }
}
