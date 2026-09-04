package com.audin.motivora.service;

import com.audin.motivora.dto.request.ChangePasswordRequest;
import com.audin.motivora.dto.request.UpdateProfileRequest;
import com.audin.motivora.dto.response.UserDTOResponse;

/**
 * Self-service operations the signed-in user performs on their own account.
 * Distinct from {@link UserService}, which is the ADMIN-facing user administration.
 */
public interface AccountService {

    UserDTOResponse getProfile();

    UserDTOResponse updateProfile(UpdateProfileRequest request);

    UserDTOResponse updateAvatar(String avatarUrl);

    /** Changes the password after checking the current one, then closes every session. */
    void changePassword(ChangePasswordRequest request);

    /**
     * Deletes the caller's own account: required by App Store guideline 5.1.1(v) for any
     * app that lets users create one.
     */
    void deleteOwnAccount(String passwordConfirmation);
}
