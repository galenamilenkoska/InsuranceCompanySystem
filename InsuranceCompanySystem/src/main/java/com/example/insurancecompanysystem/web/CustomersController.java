package com.example.insurancecompanysystem.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/customers")
public class CustomersController {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public CustomersController(JdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public String getCustomers(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int pageSize,
                            Model model) {
        // Calculate the offset based on the page number and page size
        int offset = page * pageSize;

        // Execute a SQL query with pagination
        String sql = "SELECT * FROM customer_all_info order by id OFFSET ? LIMIT ?";
        List<Map<String, Object>> viewData = jdbcTemplate.queryForList(sql, offset, pageSize);

        // Add the view data and pagination information to the model
        model.addAttribute("viewData", viewData);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", pageSize);

        int totalClaims = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM customer_all_info", Integer.class);
        int totalPages = (int) Math.ceil((double) totalClaims / pageSize);

        model.addAttribute("totalPages", totalPages);

        return "all_customers";
    }
}
