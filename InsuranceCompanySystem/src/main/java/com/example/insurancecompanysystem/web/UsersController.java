package com.example.insurancecompanysystem.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/users")
public class UsersController {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UsersController(JdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public String getCustomers(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int pageSize,
                               Model model) {
        // Calculate the offset based on the page number and page size
        int offset = page * pageSize;

        // Execute a SQL query with pagination
        String sql = "SELECT * FROM full_user order by id OFFSET ? LIMIT ?";
        List<Map<String, Object>> viewData = jdbcTemplate.queryForList(sql, offset, pageSize);

        // Add the view data and pagination information to the model
        model.addAttribute("viewData", viewData);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", pageSize);

        int totalClaims = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM full_user", Integer.class);
        int totalPages = (int) Math.ceil((double) totalClaims / pageSize);

        model.addAttribute("totalPages", totalPages);

        return "all_users";
    }

    @GetMapping("manager-policies")
    public String getManagerPolicies(@RequestParam("managerId") int managerId,
                               Model model) {
        String sql = "SELECT * FROM getpoliciespermanager(?)";
        List<Map<String, Object>> policiesList = jdbcTemplate.queryForList(sql, managerId);

        model.addAttribute("policiesList", policiesList);

        return "manager-policies";
    }

    @PostMapping("/delete")
    public String deleteUser(@RequestParam("userId") int userId, Model model) {

        try {
            jdbcTemplate.execute("CALL deleteuserbyid(?)",
                    (CallableStatementCallback<Object>) cs -> {
                        cs.setInt(1, userId);
                        cs.execute();
                        return null;
                    });
        } catch (Exception e) {
            // Handle exception and display appropriate error message
            String errorMessage = e.getMessage();
            model.addAttribute("errorMessage", errorMessage);
            return "error-page";
        }

        return "redirect:/users";
    }

}
