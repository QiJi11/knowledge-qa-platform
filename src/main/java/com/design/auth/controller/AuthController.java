package com.design.auth.controller;

import com.design.common.Result;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final JdbcTemplate jdbc;

    public AuthController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostMapping("/login")
    public Result<?> login(@RequestBody JsonNode body) {
        String username = body.has("username") ? body.get("username").asText("") : "";
        String password = body.has("password") ? body.get("password").asText("") : "";

        if (username.isBlank() || password.isBlank()) {
            return Result.fail(400, "账号和密码不能为空");
        }

        var rows = jdbc.queryForList(
            "SELECT id, username, nickname, role FROM users WHERE username = ? AND password = ? LIMIT 1",
            username, password
        );

        if (rows.isEmpty()) {
            return Result.fail(401, "账号或密码错误");
        }

        var user = rows.get(0);
        String token = "token_" + user.get("id") + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.get("id"));
        data.put("username", user.get("username"));
        data.put("nickname", user.get("nickname"));
        data.put("role", user.get("role"));

        return Result.ok(data, "登录成功");
    }

    /**
     * 通过 token 获取当前用户信息（简化实现：从 token 中解析 userId）
     */
    @GetMapping("/me")
    public Result<?> me(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.fail(401, "未登录");
        }

        String token = authHeader.substring(7);
        // token 格式: token_{userId}_{random}
        try {
            String[] parts = token.split("_");
            if (parts.length < 3) {
                return Result.fail(401, "无效的 token");
            }
            long userId = Long.parseLong(parts[1]);

            var rows = jdbc.queryForList(
                "SELECT id, username, nickname, role FROM users WHERE id = ? LIMIT 1", userId
            );

            if (rows.isEmpty()) {
                return Result.fail(401, "用户不存在");
            }

            return Result.ok(rows.get(0));
        } catch (Exception e) {
            return Result.fail(401, "无效的 token");
        }
    }

    /**
     * 从请求头中解析用户角色（供其他 Controller 复用）
     */
    public static String resolveRole(JdbcTemplate jdbc, String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        try {
            String[] parts = authHeader.substring(7).split("_");
            if (parts.length < 3) return null;
            long userId = Long.parseLong(parts[1]);
            var rows = jdbc.queryForList("SELECT role FROM users WHERE id = ? LIMIT 1", userId);
            if (rows.isEmpty()) return null;
            return (String) rows.get(0).get("role");
        } catch (Exception e) {
            return null;
        }
    }

    public static Long resolveUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        try {
            String[] parts = authHeader.substring(7).split("_");
            if (parts.length < 3) return null;
            return Long.parseLong(parts[1]);
        } catch (Exception e) {
            return null;
        }
    }
}

