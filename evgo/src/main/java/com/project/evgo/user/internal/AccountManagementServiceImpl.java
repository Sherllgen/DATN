package com.project.evgo.user.internal;

import com.project.evgo.sharedkernel.dto.PageResponse;
import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.enums.UserStatus;
import com.project.evgo.sharedkernel.exceptions.AppException;
import com.project.evgo.user.AccountManagementService;
import com.project.evgo.user.request.AccountFilterRequest;
import com.project.evgo.user.response.AdminAccountResponse;
import com.project.evgo.user.security.SecurityUtil;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of AccountManagementService.
 * Internal - not accessible by other modules.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountManagementServiceImpl implements AccountManagementService {

    private final UserRepository userRepository;
    private final UserDtoConverter userDtoConverter;

    @Override
    public PageResponse<AdminAccountResponse> findAllAccounts(AccountFilterRequest request) {
        Sort sort = Sort.by(
                Sort.Direction.fromString(request.sortDir()),
                request.sortBy());
        Pageable pageable = PageRequest.of(request.page(), request.size(), sort);

        Specification<User> spec = buildSpecification(request);
        Page<User> userPage = userRepository.findAll(spec, pageable);

        List<AdminAccountResponse> content = userDtoConverter.convertToAdminList(userPage.getContent());

        return new PageResponse<>(
                content,
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.isFirst(),
                userPage.isLast());
    }

    @Override
    public AdminAccountResponse getAccountById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userDtoConverter.convertToAdmin(user);
    }

    @Transactional
    @Override
    public void lockAccount(Long userId) {
        validateNotSelf(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() == UserStatus.DELETED) {
            throw new AppException(ErrorCode.ACCOUNT_ALREADY_DELETED);
        }

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new AppException(ErrorCode.ACCOUNT_ALREADY_LOCKED);
        }

        user.setStatus(UserStatus.BLOCKED);
        userRepository.save(user);
        log.info("Account {} locked by admin {}", userId, SecurityUtil.getCurrentUserId());
    }

    @Transactional
    @Override
    public void unlockAccount(Long userId) {
        validateNotSelf(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() == UserStatus.DELETED) {
            throw new AppException(ErrorCode.ACCOUNT_ALREADY_DELETED);
        }

        if (user.getStatus() != UserStatus.BLOCKED) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_LOCKED);
        }

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        log.info("Account {} unlocked by admin {}", userId, SecurityUtil.getCurrentUserId());
    }

    @Transactional
    @Override
    public void deleteAccount(Long userId) {
        validateNotSelf(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() == UserStatus.DELETED) {
            throw new AppException(ErrorCode.ACCOUNT_ALREADY_DELETED);
        }

        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);
        log.info("Account {} soft deleted by admin {}", userId, SecurityUtil.getCurrentUserId());
    }

    /**
     * Validate that the current user is not trying to modify their own account.
     */
    private void validateNotSelf(Long targetUserId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId != null && currentUserId.equals(targetUserId)) {
            throw new AppException(ErrorCode.CANNOT_MODIFY_OWN_ACCOUNT);
        }
    }

    /**
     * Build a JPA Specification for dynamic filtering.
     */
    private Specification<User> buildSpecification(AccountFilterRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Exclude deleted users by default
            if (request.status() == null) {
                predicates.add(criteriaBuilder.notEqual(root.get("status"), UserStatus.DELETED));
            } else {
                predicates.add(criteriaBuilder.equal(root.get("status"), request.status()));
            }

            // Filter by role
            if (request.role() != null && !request.role().isBlank()) {
                predicates.add(criteriaBuilder.equal(
                        root.join("roles").get("name"),
                        request.role()));
            }

            // Search by email or fullName
            if (request.search() != null && !request.search().isBlank()) {
                String searchPattern = "%" + request.search().toLowerCase() + "%";
                Predicate emailPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")),
                        searchPattern);
                Predicate namePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("fullName")),
                        searchPattern);
                predicates.add(criteriaBuilder.or(emailPredicate, namePredicate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
