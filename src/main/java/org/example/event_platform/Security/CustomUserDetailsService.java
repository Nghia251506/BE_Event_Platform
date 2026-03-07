package org.example.event_platform.Security;

import org.example.event_platform.Entity.User;
import org.example.event_platform.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true) // Thêm Transactional để đảm bảo load được các Set/List (Permissions) nếu cần
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Trả về trực tiếp đối tượng User (Entity) của ông
        // Vì User đã implements UserDetails nên Spring Security sẽ tự động gọi hàm getAuthorities() ông đã viết trong Entity
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với username: " + username));
    }

    // QUAN TRỌNG: Phải thêm prefix "ROLE_" trước roleCode
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        String roleCode = user.getRoles().getName(); // Ví dụ: "ADMIN", "USER"

        // Thêm prefix "ROLE_" để Spring Security hasRole() hoạt động
        String authorityName = "ROLE_" + roleCode;

        return Collections.singletonList(new SimpleGrantedAuthority(authorityName));
    }
}