package com.project.evgo.user.internal;

import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.exceptions.AppException;
import com.project.evgo.user.UserService;
import com.project.evgo.user.request.ChangePasswordRequest;
import com.project.evgo.user.request.UpdateProfileRequest;
import com.project.evgo.user.response.UserResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of UserService.
 * Internal - not accessible by other modules.
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserDtoConverter userDtoConverter;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Optional<UserResponse> findById(Long id) {
        return userDtoConverter.convert(userRepository.findById(id));
    }

    @Override
    public List<UserResponse> findAll() {
        return userDtoConverter.convert(userRepository.findAll());
    }

    @Override
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userDtoConverter.convert(user);
    }

    @Transactional
    @Override
    public UserResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (request.fullName() != null) {
            user.setFullName(request.fullName());
        }
        if (request.phoneNumber() != null) {
            user.setPhoneNumber(request.phoneNumber());
        }
        if (request.gender() != null) {
            user.setGender(request.gender());
        }
        if (request.birthday() != null) {
            user.setBirthday(request.birthday());
        }

        User updatedUser = userRepository.save(user);
        return userDtoConverter.convert(updatedUser);
    }

    @Transactional
    @Override
    public void changePassword(String email, ChangePasswordRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new AppException(ErrorCode.PASSWORD_MISMATCH);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.CURRENT_PASSWORD_INCORRECT);
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }


//    @Override
//    public UserResponse uploadAvatar(String email, MultipartFile file) {
//        if (file.isEmpty()) {
//            throw new AppException(ErrorCode.FILE_UPLOAD_ERROR, "File upload error");
//        }
//
//        // Validate file type
//        String contentType = file.getContentType();
//        if (contentType == null || !contentType.startsWith("image/")) {
//            throw new AppException(ErrorCode.FILE_TYPE_NOT_SUPPORTED);
//        }
//
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
//
//        // TODO: Implement file storage logic (e.g., save to disk, cloud storage)
//        // For now, just store the filename
//        String avatarUrl = "/uploads/avatars/" + file.getOriginalFilename();
//        user.setAvatarUrl(avatarUrl);
//
//        User updatedUser = userRepository.save(user);
//        return userDtoConverter.convert(updatedUser);
//    }
    
}
