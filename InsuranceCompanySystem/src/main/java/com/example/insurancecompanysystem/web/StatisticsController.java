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
@RequestMapping("/stats")
public class StatisticsController {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public StatisticsController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("most-used-insurances")
    public String getInsurances(Model model) {

        // Execute a SQL query with pagination
        String sql = "SELECT * FROM most_purchased_insurance_type order by number_purchased_policies desc";
        List<Map<String, Object>> viewData = jdbcTemplate.queryForList(sql);

        // Add the view data and pagination information to the model
        model.addAttribute("viewData", viewData);

        return "used-insurances";
    }

    @GetMapping("policy_claims_damages")
    public String getPolicyClaims(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int pageSize, Model model) {
        // Calculate the offset based on the page number and page size
        int offset = page * pageSize;

        // Execute a SQL query with pagination
        String sql = "SELECT * FROM policies_count_claims_damages order by idc OFFSET ? LIMIT ?";
        List<Map<String, Object>> viewData = jdbcTemplate.queryForList(sql, offset, pageSize);

        // Add the view data and pagination information to the model
        model.addAttribute("viewData", viewData);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", pageSize);

        // Calculate the total number of policies
        int totalPolicies = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM policies_count_claims_damages", Integer.class);

        // Calculate the total number of pages
        int totalPages = (int) Math.ceil((double) totalPolicies / pageSize);

        // Add the total number of pages to the model
        model.addAttribute("totalPages", totalPages);

        return "policy_claims_damages";
    }
}
