package com.chuadatten.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.chuadatten.user.entity.Role;
import com.chuadatten.user.entity.UserAuth;
import com.chuadatten.user.entity.UserRole;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {
    List<UserRole> findByUserId(UUID userId);
    List<UserRole> findByRoleId(UUID roleId);
    void deleteByUserIdAndRoleId(UUID userId, UUID roleId);

    @Query("SELECT r FROM Role r WHERE r.id IN (SELECT ur.role.id FROM UserRole ur WHERE ur.user.id = :userId)")
    List<Role> findRolesByUserId(@Param("userId") UUID userId);

    @Query("SELECT u FROM UserAuth u WHERE u.id IN (SELECT ur.user.id FROM UserRole ur WHERE ur.role.id = :roleId)")
    List<UserAuth> findUsersByRoleId(@Param("roleId") UUID roleId);
    
    @Query("SELECT COUNT(ur) FROM UserRole ur WHERE ur.role.name = :roleName")
    Long countByRoleName(@Param("roleName") String roleName);
}
