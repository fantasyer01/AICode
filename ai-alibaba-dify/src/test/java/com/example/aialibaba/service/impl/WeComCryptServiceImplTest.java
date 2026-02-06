package com.example.aialibaba.service.impl;

import com.example.aialibaba.config.WeComConfig;
import com.example.aialibaba.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("WeComCryptService Tests")
class WeComCryptServiceImplTest {

    @Mock
    private WeComConfig weComConfig;

    @Test
    @DisplayName("isConfigured should return false when config is missing")
    void testIsConfigured_NotConfigured() {
        lenient().when(weComConfig.isConfigured()).thenReturn(false);
        WeComCryptServiceImpl weComCryptService = new WeComCryptServiceImpl(weComConfig, null);
        
        assertFalse(weComCryptService.isConfigured());
    }

    @Test
    @DisplayName("verifyUrl should throw exception when not configured")
    void testVerifyUrl_NotConfigured_ThrowsException() {
        lenient().when(weComConfig.isConfigured()).thenReturn(false);
        WeComCryptServiceImpl weComCryptService = new WeComCryptServiceImpl(weComConfig, null);
        
        ServiceException exception = assertThrows(ServiceException.class, () -> 
                weComCryptService.verifyUrl("sig", "ts", "nonce", "echo"));
        
        assertEquals("WECOM_NOT_CONFIGURED", exception.getErrorCode());
    }

    @Test
    @DisplayName("decryptMsg should throw exception when not configured")
    void testDecryptMsg_NotConfigured_ThrowsException() {
        lenient().when(weComConfig.isConfigured()).thenReturn(false);
        WeComCryptServiceImpl weComCryptService = new WeComCryptServiceImpl(weComConfig, null);
        
        ServiceException exception = assertThrows(ServiceException.class, () -> 
                weComCryptService.decryptMsg("sig", "ts", "nonce", "<xml></xml>"));
        
        assertEquals("WECOM_NOT_CONFIGURED", exception.getErrorCode());
    }

    @Test
    @DisplayName("encryptMsg should throw exception when not configured")
    void testEncryptMsg_NotConfigured_ThrowsException() {
        lenient().when(weComConfig.isConfigured()).thenReturn(false);
        WeComCryptServiceImpl weComCryptService = new WeComCryptServiceImpl(weComConfig, null);
        
        ServiceException exception = assertThrows(ServiceException.class, () -> 
                weComCryptService.encryptMsg("reply", "ts", "nonce"));
        
        assertEquals("WECOM_NOT_CONFIGURED", exception.getErrorCode());
    }
}
