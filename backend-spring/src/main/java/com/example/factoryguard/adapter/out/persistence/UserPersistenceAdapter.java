package com.example.factoryguard.adapter.out.persistence;

import com.example.factoryguard.adapter.out.persistence.entity.UserEntity;
import com.example.factoryguard.adapter.out.persistence.mapper.UserMapper;
import com.example.factoryguard.application.port.out.UserRepository;
import com.example.factoryguard.domain.user.User;
import com.example.factoryguard.domain.user.UserStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class UserPersistenceAdapter implements UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserPersistenceAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        List<UserEntity> results = jdbcTemplate.query(sql, (rs, rowNum) -> mapRow(rs), id);
        return results.stream().map(UserMapper::toDomain).findFirst();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        List<UserEntity> results = jdbcTemplate.query(sql, (rs, rowNum) -> mapRow(rs), email);
        return results.stream().map(UserMapper::toDomain).findFirst();
    }

    @Override
    public Optional<User> findByGoogleSub(String googleSub) {
        String sql = "SELECT * FROM users WHERE google_sub = ?";
        List<UserEntity> results = jdbcTemplate.query(sql, (rs, rowNum) -> mapRow(rs), googleSub);
        return results.stream().map(UserMapper::toDomain).findFirst();
    }

    @Override
    public User save(User user) {
        UserEntity entity = UserMapper.toEntity(user);
        
        if (user.getId() == null) {
            String sql = "INSERT INTO users (email, password, name, google_sub, picture, company, position, phone, role, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, 
                entity.getEmail(), 
                entity.getPassword(), 
                entity.getName(),
                entity.getGoogleSub(),
                entity.getPicture(),
                entity.getCompany(),
                entity.getPosition(),
                entity.getPhone(),
                entity.getRole(), 
                entity.getStatus(), 
                entity.getCreatedAt(), 
                entity.getUpdatedAt());
        } else {
            String sql = "UPDATE users SET email = ?, password = ?, name = ?, google_sub = ?, picture = ?, company = ?, position = ?, phone = ?, role = ?, status = ?, updated_at = ? WHERE id = ?";
            jdbcTemplate.update(sql, 
                entity.getEmail(), 
                entity.getPassword(), 
                entity.getName(),
                entity.getGoogleSub(),
                entity.getPicture(),
                entity.getCompany(),
                entity.getPosition(),
                entity.getPhone(),
                entity.getRole(), 
                entity.getStatus(), 
                entity.getUpdatedAt(),
                entity.getId());
        }
        
        return findByEmail(user.getEmail()).orElse(user);
    }

    @Override
    public List<User> findByStatus(UserStatus status) {
        String sql = "SELECT * FROM users WHERE status = ?";
        List<UserEntity> results = jdbcTemplate.query(sql, (rs, rowNum) -> mapRow(rs), status.getValue());
        return results.stream().map(UserMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByGoogleSub(String googleSub) {
        String sql = "SELECT COUNT(*) FROM users WHERE google_sub = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, googleSub);
        return count != null && count > 0;
    }

private UserEntity mapRow(java.sql.ResultSet rs) throws java.sql.SQLException {
    UserEntity entity = new UserEntity();
    entity.setId(rs.getLong("id"));
    entity.setEmail(rs.getString("email"));
    entity.setPassword(rs.getString("password"));
    entity.setName(rs.getString("name"));
    entity.setGoogleSub(rs.getString("google_sub"));
    entity.setPicture(rs.getString("picture"));
    entity.setCompany(rs.getString("company"));
    entity.setPosition(rs.getString("position"));
    entity.setPhone(rs.getString("phone"));
    entity.setRole(rs.getString("role"));
    entity.setStatus(rs.getString("status"));
    entity.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime()); // ✅ 수정
    entity.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime()); // ✅ 수정
    return entity;
}
}
