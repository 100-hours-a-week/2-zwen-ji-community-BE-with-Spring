package com.example.project.service;

import com.example.project.domain.User;
import com.example.project.exception.ResourceNotFoundException;
import com.example.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 모든 사용자 조회
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ID로 사용자 조회
    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // 사용자 생성
    @Transactional
    public User createUser(User user) {
        // 이메일 중복 확인
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + user.getEmail());
        }

        // 닉네임 중복 확인 (닉네임 필드가 있다면)
        if (user.getNickname() != null && !user.getNickname().isEmpty() &&
                userRepository.findByNickname(user.getNickname()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다: " + user.getNickname());
        }

        return userRepository.save(user);
    }

    // 사용자 정보 업데이트
    @Transactional
    public User updateUser(User user) {
        // 사용자 존재 확인
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + user.getId()));

        // 이메일 중복 확인 (변경된 경우)
        if (!existingUser.getEmail().equals(user.getEmail()) &&
                userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + user.getEmail());
        }

        // 닉네임 중복 확인 (변경된 경우)
        if (user.getNickname() != null && !user.getNickname().isEmpty() &&
                !existingUser.getNickname().equals(user.getNickname()) &&
                userRepository.findByNickname(user.getNickname()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다: " + user.getNickname());
        }

        return userRepository.save(user);
    }

    // 사용자 삭제 (물리적 삭제)
    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // 사용자 삭제 (논리적 삭제)
    @Transactional
    public void softDeleteUser(Long id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setDeleted(true);
            userRepository.save(user);
        });
    }

    // 사용자 인증
    @Transactional(readOnly = true)
    public Optional<User> authenticateUser(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // 암호화된 비밀번호 비교
            if (passwordEncoder.matches(password, user.getPassword())) {
                return Optional.of(user);
            }
        }

        return Optional.empty();
    }

    // 이메일로 사용자 찾기
    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // 닉네임으로 사용자 찾기
    @Transactional(readOnly = true)
    public Optional<Object> getUserByNickname(String nickname) {
        return userRepository.findByNickname(nickname);
    }

    // 프로필 이미지 URL 업데이트
    @Transactional
    public User updateProfileImage(Long userId, String profileImageUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        user.setProfileImageUrl(profileImageUrl);
        return userRepository.save(user);
    }

    // 사용자 프로필 이미지 URL 가져오기
    @Transactional(readOnly = true)
    public String getUserProfileImageUrl(Long userId) {
        return userRepository.findById(userId)
                .map(User::getProfileImageUrl)
                .orElse(null);
    }


    @Transactional
    public User changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));

        // 현재 비밀번호 검증
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 유효성 검사 (필요한 경우)
        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이어야 합니다.");
        }

        // 새 비밀번호 암호화
        String encodedNewPassword = passwordEncoder.encode(newPassword);

        // 암호화된 비밀번호 저장
        user.setPassword(encodedNewPassword);

        return userRepository.save(user);
    }

    @Transactional
    public User updatePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));

        // 새 비밀번호 암호화 및 저장
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedNewPassword);

        return userRepository.save(user);
    }

    // 이메일 변경
    @Transactional
    public User changeEmail(Long userId, String newEmail) {
        // 이메일 중복 확인
        if (userRepository.findByEmail(newEmail).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + newEmail);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        user.setEmail(newEmail);
        return userRepository.save(user);
    }

    // 닉네임 변경
    @Transactional
    public User changeNickname(Long userId, String newNickname) {
        // 닉네임 중복 확인
        if (userRepository.findByNickname(newNickname).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다: " + newNickname);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        user.setNickname(newNickname);
        return userRepository.save(user);
    }


//    // 사용자의 현재 권한(Role) 확인
//    @Transactional(readOnly = true)
//    public boolean hasRole(Long userId, String roleName) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
//
//        // 역할 확인 로직 (User 클래스에 역할 필드가 있다고 가정)
//        return user.getRoles().stream()
//                .anyMatch(role -> role.getName().equals(roleName));
//    }
//
//    // 사용자 프로필 조회
//    @Transactional(readOnly = true)
//    public UserProfileDTO getUserProfile(Long userId) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
//
//        UserProfileDTO profile = new UserProfileDTO();
//        profile.setId(user.getId());
//        profile.setEmail(user.getEmail());
//        profile.setNickname(user.getNickname());
//        profile.setProfileImageUrl(user.getProfileImageUrl());
//        // 필요한 추가 정보 설정
//
//        return profile;
//    }
}