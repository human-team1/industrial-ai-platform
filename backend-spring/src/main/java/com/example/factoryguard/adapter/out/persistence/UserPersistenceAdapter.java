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
        List<UserEntity> results = jdbcTemplate.query(sql, (rs, rowNum) -> {
            UserEntity entity = new UserEntity();
            entity.setId(rs.getLong("id"));
            entity.setEmail(rs.getString("email"));
            entity.setPassword(rs.getString("password"));
            entity.setName(rs.getString("name"));
            entity.setRole(rs.getString("role"));
            entity.setStatus(rs.getString("status"));
            entity.setCreatedAt(rs.getString("created_at"));
            entity.setUpdatedAt(rs.getString("updated_at"));
            return entity;
        }, id);
        return results.stream().map(UserMapper::toDomain).findFirst();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        List<UserEntity> results = jdbcTemplate.query(sql, (rs, rowNum) -> {
            UserEntity entity = new UserEntity();
            entity.setId(rs.getLong("id"));
            entity.setEmail(rs.getString("email"));
            entity.setPassword(rs.getString("password"));
            entity.setName(rs.getString("name"));
            entity.setRole(rs.getString("role"));
            entity.setStatus(rs.getString("status"));
            entity.setCreatedAt(rs.getString("created_at"));
            entity.setUpdatedAt(rs.getString("updated_at"));
            return entity;
        }, email);
        return results.stream().map(UserMapper::toDomain).findFirst();
    }

    @Override
    public User save(User user) {
        UserEntity entity = UserMapper.toEntity(user);
        
        if (user.getId() == null) {
            String sql = "INSERT INTO users (email, password, name, role, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, 
                entity.getEmail(), 
                entity.getPassword(), 
                entity.getName(), 
                entity.getRole(), 
                entity.getStatus(), 
                entity.getCreatedAt(), 
                entity.getUpdatedAt());
        } else {
            String sql = "UPDATE users SET email = ?, password = ?, name = ?, role = ?, status = ?, updated_at = ? WHERE id = ?";
            jdbcTemplate.update(sql, 
                entity.getEmail(), 
                entity.getPassword(), 
                entity.getName(), 
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
        List<UserEntity> results = jdbcTemplate.query(sql, (rs, rowNum) -> {
            UserEntity entity = new UserEntity();
            entity.setId(rs.getLong("id"));
            entity.setEmail(rs.getString("email"));
            entity.setPassword(rs.getString("password"));
            entity.setName(rs.getString("name"));
            entity.setRole(rs.getString("role"));
            entity.setStatus(rs.getString("status"));
            entity.setCreatedAt(rs.getString("created_at"));
            entity.setUpdatedAt(rs.getString("updated_at"));
            return entity;
        }, status.getValue());
        return results.stream().map(UserMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }
}