package com.audin.motivora.service.Impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.audin.motivora.dto.request.ChangePasswordRequest;
import com.audin.motivora.dto.request.UpdateProfileRequest;
import com.audin.motivora.dto.response.UserDTOResponse;
import com.audin.motivora.entity.User;
import com.audin.motivora.enums.UserStatus;
import com.audin.motivora.exception.BusinessException;
import com.audin.motivora.mapper.AuthMapper;
import com.audin.motivora.repository.FavoriteRepository;
import com.audin.motivora.repository.DeviceTokenRepository;
import com.audin.motivora.repository.NotificationPreferenceRepository;
import com.audin.motivora.repository.NotificationRepository;
import com.audin.motivora.repository.OtpCodeRepository;
import com.audin.motivora.repository.UserRepository;
import com.audin.motivora.security.JwtService;
import com.audin.motivora.service.AccountService;
import com.audin.motivora.utils.AuthUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final OtpCodeRepository otpCodeRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;
    private final AuthUtil authUtil;
    private final JwtService jwtService;

    @Override
    @Transactional(readOnly = true)
    public UserDTOResponse getProfile() {
        return this.authMapper.toDto(this.authUtil.getCurrentUser());
    }

    @Override
    @Transactional
    public UserDTOResponse updateProfile(UpdateProfileRequest request) {
        User user = this.reload();
        if (request.getPseudo() != null && !request.getPseudo().isBlank()) {
            user.setPseudo(request.getPseudo().trim());
        }
        return this.authMapper.toDto(this.userRepository.save(user));
    }

    @Override
    @Transactional
    public UserDTOResponse updateAvatar(String avatarUrl) {
        User user = this.reload();
        user.setAvatarUrl(avatarUrl);
        return this.authMapper.toDto(this.userRepository.save(user));
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = this.reload();

        if (!this.passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException("INVALID_CURRENT_PASSWORD", "Current password is incorrect");
        }
        if (this.passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BusinessException("PASSWORD_UNCHANGED", "The new password must differ from the current one");
        }

        user.setPassword(this.passwordEncoder.encode(request.getNewPassword()));
        this.userRepository.save(user);

        // Every device must re-authenticate with the new password.
        this.jwtService.disableTokens(user);
    }

    /**
     * Anonymises rather than hard-deletes: quotes contributed by the user are referenced
     * elsewhere, so the row stays while every piece of personal data is dropped and the
     * email is freed for a future signup.
     */
    @Override
    @Transactional
    public void deleteOwnAccount(String passwordConfirmation) {
        User user = this.reload();

        if (!this.passwordEncoder.matches(passwordConfirmation, user.getPassword())) {
            throw new BusinessException("INVALID_PASSWORD", "Password confirmation is incorrect");
        }

        this.favoriteRepository.deleteAll(this.favoriteRepository.findAllByUserId(user.getId()));
        this.otpCodeRepository.deleteByUserId(user.getId());
        this.deviceTokenRepository.deleteByUserId(user.getId());
        this.notificationRepository.deleteByUserId(user.getId());
        this.preferenceRepository.deleteByUserId(user.getId());
        user.getFollowedThemes().clear();

        user.setEmail("deleted-" + UUID.randomUUID() + "@motivora.invalid");
        user.setPseudo("Compte supprimé");
        user.setPassword(this.passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setAvatarUrl(null);
        user.setEmailVerifiedAt(null);
        user.setStatus(UserStatus.DELETED);
        user.setDeletedAt(LocalDateTime.now());
        this.userRepository.save(user);

        this.jwtService.disableTokens(user);
        log.info("Account {} deleted at the owner's request", user.getId());
    }

    /**
     * The principal comes from the security context and may be a snapshot from when the
     * token was issued; account writes must run against the row as it stands.
     */
    private User reload() {
        Integer id = this.authUtil.getCurrentUser().getId();
        return this.userRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("User not found"));
    }
}
