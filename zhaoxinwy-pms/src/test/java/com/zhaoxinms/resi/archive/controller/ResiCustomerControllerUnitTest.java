package com.zhaoxinms.resi.archive.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zhaoxinms.base.util.DesUtil;
import com.zhaoxinms.common.core.domain.AjaxResult;
import com.zhaoxinms.resi.archive.entity.ResiCustomer;
import com.zhaoxinms.resi.archive.service.IResiCustomerService;
import com.zhaoxinms.resi.TestSecurityContext;

@ExtendWith(MockitoExtension.class)
class ResiCustomerControllerUnitTest {

    @Mock
    private IResiCustomerService customerService;

    @InjectMocks
    private ResiCustomerController controller;

    @BeforeEach
    void setUp() {
        TestSecurityContext.setNormalUser();
    }

    @AfterEach
    void tearDown() {
        TestSecurityContext.clear();
    }

    @Test
    @DisplayName("add: idCard should be encrypted")
    void add_withIdCard_encryptStorage() {
        String plainIdCard = "110101199001011234";
        ResiCustomer customer = new ResiCustomer();
        customer.setProjectId(1L);
        customer.setCustomerName("Zhang San");
        customer.setPhone("13800188888");
        customer.setIdCard(plainIdCard);

        when(customerService.save(any(ResiCustomer.class))).thenAnswer(inv -> {
            ResiCustomer saved = inv.getArgument(0);
            assertNotEquals(plainIdCard, saved.getIdCard());
            assertTrue(saved.getIdCard().length() > plainIdCard.length());
            return true;
        });

        AjaxResult result = controller.add(customer);
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("add: null idCard should stay null")
    void add_withoutIdCard_noEncrypt() {
        ResiCustomer customer = new ResiCustomer();
        customer.setProjectId(1L);
        customer.setCustomerName("Li Si");
        customer.setPhone("13900199999");
        customer.setIdCard(null);

        when(customerService.save(any(ResiCustomer.class))).thenAnswer(inv -> {
            ResiCustomer saved = inv.getArgument(0);
            assertNull(saved.getIdCard());
            return true;
        });

        AjaxResult result = controller.add(customer);
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("add: empty idCard should become null")
    void add_emptyIdCard_convertToNull() {
        ResiCustomer customer = new ResiCustomer();
        customer.setProjectId(1L);
        customer.setCustomerName("Wang Wu");
        customer.setPhone("13700177777");
        customer.setIdCard("   ");

        when(customerService.save(any(ResiCustomer.class))).thenAnswer(inv -> {
            ResiCustomer saved = inv.getArgument(0);
            assertNull(saved.getIdCard());
            return true;
        });

        AjaxResult result = controller.add(customer);
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("edit: plain idCard should be encrypted")
    void edit_withPlainIdCard_encrypt() {
        Long id = 1L;
        String plainIdCard = "110101199001011234";
        String encryptedIdCard = DesUtil.aesEncode(plainIdCard);

        ResiCustomer old = new ResiCustomer();
        old.setId(id);
        old.setIdCard(encryptedIdCard);

        ResiCustomer update = new ResiCustomer();
        update.setIdCard(plainIdCard);

        when(customerService.getById(id)).thenReturn(old);
        when(customerService.updateById(any(ResiCustomer.class))).thenAnswer(inv -> {
            ResiCustomer updated = inv.getArgument(0);
            assertNotEquals(plainIdCard, updated.getIdCard());
            assertTrue(updated.getIdCard().length() > 32);
            return true;
        });

        AjaxResult result = controller.edit(id, update);
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("edit: encrypted idCard should not be re-encrypted")
    void edit_withEncryptedIdCard_noReEncrypt() {
        Long id = 1L;
        String encryptedIdCard = DesUtil.aesEncode("110101199001011234");

        ResiCustomer old = new ResiCustomer();
        old.setId(id);
        old.setIdCard(encryptedIdCard);

        ResiCustomer update = new ResiCustomer();
        update.setIdCard(encryptedIdCard);

        when(customerService.getById(id)).thenReturn(old);
        when(customerService.updateById(any(ResiCustomer.class))).thenAnswer(inv -> {
            ResiCustomer updated = inv.getArgument(0);
            assertEquals(encryptedIdCard, updated.getIdCard());
            return true;
        });

        AjaxResult result = controller.edit(id, update);
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("edit: null idCard should keep old value")
    void edit_idCardNull_keepOld() {
        Long id = 1L;
        String encryptedIdCard = DesUtil.aesEncode("110101199001011234");

        ResiCustomer old = new ResiCustomer();
        old.setId(id);
        old.setIdCard(encryptedIdCard);

        ResiCustomer update = new ResiCustomer();
        update.setIdCard(null);

        when(customerService.getById(id)).thenReturn(old);
        when(customerService.updateById(any(ResiCustomer.class))).thenAnswer(inv -> {
            ResiCustomer updated = inv.getArgument(0);
            assertEquals(encryptedIdCard, updated.getIdCard());
            return true;
        });

        AjaxResult result = controller.edit(id, update);
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @DisplayName("edit: empty idCard should keep old value (current behavior)")
    void edit_emptyIdCard_keepOld() {
        Long id = 1L;
        String encryptedIdCard = DesUtil.aesEncode("110101199001011234");

        ResiCustomer old = new ResiCustomer();
        old.setId(id);
        old.setIdCard(encryptedIdCard);

        ResiCustomer update = new ResiCustomer();
        update.setIdCard("  ");

        when(customerService.getById(id)).thenReturn(old);
        when(customerService.updateById(any(ResiCustomer.class))).thenAnswer(inv -> {
            ResiCustomer updated = inv.getArgument(0);
            // Current behavior: empty string -> null -> keep old value
            // To support clearing, controller logic needs to distinguish null vs empty
            assertEquals(encryptedIdCard, updated.getIdCard());
            return true;
        });

        AjaxResult result = controller.edit(id, update);
        assertEquals(200, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    @Disabled("Requires Servlet context for PageHelper startPage()")
    @DisplayName("list: should decrypt idCard before return")
    void list_shouldDecryptIdCard() {
        // Disabled: startPage() needs HTTP request context
    }

    @Test
    @Disabled("Requires Servlet context for PageHelper startPage()")
    @DisplayName("list: should keep original if decrypt fails")
    void list_decryptFail_keepOriginal() {
        // Disabled: startPage() needs HTTP request context
    }
}
