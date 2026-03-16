package com.design.auth.controller;

import com.design.common.Result;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final JdbcTemplate jdbc;

    public AdminController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private void requireAdmin(String authHeader) {
        String role = AuthController.resolveRole(jdbc, authHeader);
        if (!"admin".equals(role)) {
            throw new RuntimeException("无权操作：需要管理员权限");
        }
    }

    @GetMapping("/users")
    public Result<?> listUsers(@RequestHeader(value = "Authorization", required = false) String auth) {
        requireAdmin(auth);
        List<Map<String, Object>> users = jdbc.queryForList(
            "SELECT id, username, nickname, role, created_at FROM users ORDER BY id"
        );
        return Result.ok(users);
    }

    @PutMapping("/users/{id}/role")
    public Result<?> updateRole(
        @PathVariable("id") Long id,
        @RequestBody JsonNode body,
        @RequestHeader(value = "Authorization", required = false) String auth
    ) {
        requireAdmin(auth);

        String newRole = body.has("role") ? body.get("role").asText("") : "";
        if (!"admin".equals(newRole) && !"student".equals(newRole)) {
            return Result.fail(400, "role 只能是 admin 或 student");
        }

        int updated = jdbc.update("UPDATE users SET role = ? WHERE id = ?", newRole, id);
        if (updated == 0) {
            return Result.fail(404, "用户不存在");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("role", newRole);
        return Result.ok(data, "角色修改成功");
    }

    @GetMapping("/stats")
    public Result<?> dashboardStats(@RequestHeader(value = "Authorization", required = false) String auth) {
        requireAdmin(auth);

        Map<String, Object> stats = new HashMap<>();
        stats.put("userCount", jdbc.queryForObject("SELECT COUNT(*) FROM users", Integer.class));
        stats.put("courseCount", jdbc.queryForObject("SELECT COUNT(*) FROM courses", Integer.class));
        stats.put("taskCount", jdbc.queryForObject("SELECT COUNT(*) FROM learning_tasks", Integer.class));
        stats.put("orderCount", jdbc.queryForObject("SELECT COUNT(*) FROM orders", Integer.class));

        return Result.ok(stats);
    }
}
