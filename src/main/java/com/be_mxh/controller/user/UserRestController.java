package com.be_mxh.controller.user;

import com.be_mxh.entity.User;
import com.be_mxh.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserRestController {
    private final UserRepository userRepository;

    /**
     * Lấy danh sách tất cả user (admin / test)
     */
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Lấy thông tin user theo ID
     */
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found with id = " + id));
    }

    /**
     * Tìm user theo username hoặc email
     * VD: /api/users/search?keyword=lam
     */
    @GetMapping("/search")
    public User findByUsernameOrEmail(
            @RequestParam String keyword) {

        return userRepository
                .findByUsernameOrEmail(keyword, keyword)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }

    /**
     * Xoá user (demo)
     */
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
    }
}