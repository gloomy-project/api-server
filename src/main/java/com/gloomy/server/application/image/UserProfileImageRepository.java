package com.gloomy.server.application.image;

import com.gloomy.server.domain.image.ImageStatus;
import com.gloomy.server.domain.image.UserProfileImage;
import com.gloomy.server.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileImageRepository extends JpaRepository<UserProfileImage, Long> {
    UserProfileImage findAllByUserIdAndStatus(User userId, ImageStatus status);
}
