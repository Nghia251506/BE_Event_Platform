package org.example.event_platform.Mapper;

import org.example.event_platform.Dto.Auth.*;
import org.example.event_platform.Entity.Permission;
import org.example.event_platform.Entity.Role;
import org.example.event_platform.Entity.User;
import org.mapstruct.*;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    // 1. Chuyển từ Entity sang Response (Phẳng hóa dữ liệu)
    @Mapping(target = "tenantId", source = "tenant.id")
    @Mapping(target = "tenantName", source = "tenant.name")
    @Mapping(target = "roleName", source = "roles.name")
    @Mapping(target = "permissions", source = "user", qualifiedByName = "combineAllPermissions")
//    @Mapping(target = "userEvents", source = "userEvents")
    UserResponse toResponse(User user);

    // 2. Chuyển từ CreateRequest sang Entity
    @Mapping(target = "roles", ignore = true) // Sẽ set thủ công trong Service bằng roleRepository
    @Mapping(target = "password", ignore = true) // Sẽ mã hóa thủ công trong Service
    User toEntity(CreateUserRequest request);

    // 3. Cập nhật Entity từ UpdateRequest
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    void updateEntity(@MappingTarget User user, UserUpdateRequest request);

    // Helper method để biến Set<Permission> thành Set<String> (Tên quyền)
    @Named("mapPermissions")
    default Set<String> mapPermissions(Set<Permission> permissions) {
        if (permissions == null) return null;
        return permissions.stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());
    }
    @Named("combineAllPermissions")
    default Set<String> combineAllPermissions(User user) {
        Set<String> allPerms = new HashSet<>();

        // 1. Lấy quyền từ Role
        if (user.getRoles() != null && user.getRoles().getPermissions() != null) {
            user.getRoles().getPermissions().forEach(p -> allPerms.add(p.getName()));
        }

        // 2. Lấy quyền gán riêng (bảng user_permissions)
        if (user.getPermissions() != null) {
            user.getPermissions().forEach(p -> allPerms.add(p.getName()));
        }

        return allPerms;
    }
}