package com.example.insurancecompanysystem.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
public class PolicyController {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PolicyController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

//    @GetMapping("/policies")
//    public String getPolicy(Model model) {
//        // Execute a SQL query to retrieve data from the view
//        String sql = "SELECT * FROM all_policies";
//        List<Map<String, Object>> viewData = jdbcTemplate.queryForList(sql);
//
//        // Add the view data to the model
//        model.addAttribute("viewData", viewData);
//
//        // Call the view named "policy-view" and pass the model to it
//        return "policy-view";
//    }


    @GetMapping("/policies")
    public String getPolicy(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int pageSize,
                            Model model) {
        // Calculate the offset based on the page number and page size
        int offset = page * pageSize;

        // Execute a SQL query with pagination
        String sql = "SELECT * FROM all_policies OFFSET ? LIMIT ?";
        List<Map<String, Object>> viewData = jdbcTemplate.queryForList(sql, offset, pageSize);

        // Add the view data and pagination information to the model
        model.addAttribute("viewData", viewData);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", pageSize);

        // Call the view named "policy-view" and pass the model to it
        return "policy-view";
    }
}
