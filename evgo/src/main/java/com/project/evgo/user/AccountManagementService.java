package com.project.evgo.user;

import com.project.evgo.sharedkernel.dto.PageResponse;
import com.project.evgo.user.request.AccountFilterRequest;
import com.project.evgo.user.response.AdminAccountResponse;

/**
 * Service interface for account management by administrators.
 * Public API - accessible by other modules.
 */
public interface AccountManagementService {

    /**
     * Get paginated list of all accounts with optional filtering.
     *
     * @param request filter and pagination request
     * @return paginated list of accounts
     */
    PageResponse<AdminAccountResponse> findAllAccounts(AccountFilterRequest request);

    /**
     * Get account details by user ID.
     *
     * @param userId the user ID
     * @return account details
     */
    AdminAccountResponse getAccountById(Long userId);

    /**
     * Lock a user account (set status to BLOCKED).
     *
     * @param userId the user ID to lock
     */
    void lockAccount(Long userId);

    /**
     * Unlock a user account (set status to ACTIVE).
     *
     * @param userId the user ID to unlock
     */
    void unlockAccount(Long userId);

    /**
     * Soft delete a user account (set status to DELETED).
     *
     * @param userId the user ID to delete
     */
    void deleteAccount(Long userId);
}
