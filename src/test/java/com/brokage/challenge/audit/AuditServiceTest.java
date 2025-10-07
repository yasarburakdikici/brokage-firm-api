package com.brokage.challenge.audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    private static final String TEST_OPERATION = "CREATE_ORDER";
    private static final String TEST_ENTITY_TYPE = "Order";
    private static final Long TEST_ENTITY_ID = 1L;
    private static final String TEST_CUSTOMER_ID = "cust1";
    private static final String TEST_DETAILS = "Order: BUY BTC Size: 2 Price: 10.50";
    private static final String TEST_ERROR_MESSAGE = "Insufficient balance";

    @Mock
    private AuditRepository auditRepository;

    private AuditService auditService;

    @BeforeEach
    void setUp() {
        auditService = new AuditService(auditRepository);
    }

    @Test
    @DisplayName("logSuccess saves audit log with SUCCESS status")
    void logSuccess_savesAuditLog() {
        // act
        auditService.logSuccess(TEST_OPERATION, TEST_ENTITY_TYPE, TEST_ENTITY_ID, TEST_CUSTOMER_ID, TEST_DETAILS);

        // assert
        verify(auditRepository).save(org.mockito.ArgumentMatchers.argThat(auditLog ->
                auditLog.getOperation().equals(TEST_OPERATION) &&
                auditLog.getEntityType().equals(TEST_ENTITY_TYPE) &&
                auditLog.getEntityId().equals(TEST_ENTITY_ID) &&
                auditLog.getCustomerId().equals(TEST_CUSTOMER_ID) &&
                auditLog.getDetails().equals(TEST_DETAILS) &&
                auditLog.getStatus() == AuditStatus.SUCCESS &&
                auditLog.getTimestamp() != null
        ));
    }

    @Test
    @DisplayName("logFailure saves audit log with FAILURE status and error message")
    void logFailure_savesAuditLog() {
        // act
        auditService.logFailure(TEST_OPERATION, TEST_ENTITY_TYPE, TEST_ENTITY_ID, TEST_CUSTOMER_ID, TEST_DETAILS, TEST_ERROR_MESSAGE);

        // assert
        verify(auditRepository).save(org.mockito.ArgumentMatchers.argThat(auditLog ->
                auditLog.getOperation().equals(TEST_OPERATION) &&
                auditLog.getEntityType().equals(TEST_ENTITY_TYPE) &&
                auditLog.getEntityId().equals(TEST_ENTITY_ID) &&
                auditLog.getCustomerId().equals(TEST_CUSTOMER_ID) &&
                auditLog.getDetails().equals(TEST_DETAILS) &&
                auditLog.getStatus() == AuditStatus.FAILURE &&
                auditLog.getErrorMessage().equals(TEST_ERROR_MESSAGE) &&
                auditLog.getTimestamp() != null
        ));
    }
}
