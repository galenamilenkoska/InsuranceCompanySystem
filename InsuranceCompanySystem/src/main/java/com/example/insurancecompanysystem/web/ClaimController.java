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
@RequestMapping("/claims")
public class ClaimController {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ClaimController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public String getClaims(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int pageSize,
                            Model model) {
        // Calculate the offset based on the page number and page size
        int offset = page * pageSize;

        // Execute a SQL query with pagination
        String sql = "SELECT * FROM all_claims order by id OFFSET ? LIMIT ?";
        List<Map<String, Object>> viewData = jdbcTemplate.queryForList(sql, offset, pageSize);

        // Add the view data and pagination information to the model
        model.addAttribute("viewData", viewData);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", pageSize);

        int totalClaims = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM all_claims", Integer.class);
        int totalPages = (int) Math.ceil((double) totalClaims / pageSize);

        model.addAttribute("totalPages", totalPages);

        return "all_claims";
    }

    @GetMapping("/create")
    public String showCreateClaimForm(Model model) {
        return "create-claim";
    }

    @PostMapping("/create")
    public String createClaim(@RequestParam("policyId") int policyId,
                               @RequestParam("adjusterId") int adjusterId,
                               @RequestParam("startDate") String startDate,
                               @RequestParam("description") String description,
                               @RequestParam("customerId") int customerId, Model model) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate parsedStartDate = LocalDate.parse(startDate, formatter);

        try {
            jdbcTemplate.execute("CALL createclaim(?, ?, ?, ?, ?, ?)",
                    (CallableStatementCallback<Object>) cs -> {
                        cs.setInt(1, policyId);
                        cs.setDate(2, Date.valueOf(parsedStartDate));
                        cs.setString(3, String.valueOf(description));
                        cs.setInt(4, adjusterId);
                        cs.setInt(5, customerId);
                        cs.setInt(6, 1);
                        cs.execute();
                        return null;
                    });
        } catch (Exception e) {
            // Handle exception and display appropriate error message
            String errorMessage = e.getMessage();
            model.addAttribute("errorMessage", errorMessage);
            return "error-page";
        }

        return "redirect:/claims";
    }

    @GetMapping("/update")
    public String showUpdateClaimForm(@RequestParam("claimId") int claimId,
                                      @RequestParam("policyId") int policyId,
                                      @RequestParam("adjusterId") int adjusterId,
                                      @RequestParam("startDate") String startDate,
                                      @RequestParam("description") String description,
                                      @RequestParam("customerId") int customerId,
                                      @RequestParam("statusId") int statusId, Model model) {

        String sql = "SELECT * FROM policystatus order by id";
        List<Map<String, Object>> statusList = jdbcTemplate.queryForList(sql);

        model.addAttribute("claimId", claimId);
        model.addAttribute("policyId", policyId);
        model.addAttribute("adjusterId", adjusterId);
        model.addAttribute("startDate", startDate);
        model.addAttribute("description", description);
        model.addAttribute("customerId", customerId);
        model.addAttribute("statusId", statusId);
        model.addAttribute("statusList", statusList);

//        model.addAttribute("statusDropdown", statuses);


        return "update-claim";
    }

    @PostMapping("/update")
    public String updateClaim(@RequestParam("claimId") int claimId,
                              @RequestParam("policyId") int policyId,
                              @RequestParam("adjusterId") int adjusterId,
                              @RequestParam("startDate") String startDate,
                              @RequestParam("description") String description,
                              @RequestParam("customerId") int customerId,
                              @RequestParam("statusId") int statusId, Model model) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate parsedStartDate = LocalDate.parse(startDate, formatter);

        try {
            jdbcTemplate.execute("CALL updateclaim(?, ?, ?, ?, ?, ?, ?)",
                    (CallableStatementCallback<Object>) cs -> {
                        cs.setInt(1, claimId);
                        cs.setInt(2, policyId);
                        cs.setDate(3, Date.valueOf(parsedStartDate));
                        cs.setString(4, String.valueOf(description));
                        cs.setInt(5, adjusterId);
                        cs.setInt(6, customerId);
                        cs.setInt(7, statusId);
                        cs.execute();
                        return null;
                    });
        } catch (Exception e) {
            // Handle exception and display appropriate error message
            String errorMessage = e.getMessage();
            model.addAttribute("errorMessage", errorMessage);
            return "error-page";
        }

        return "redirect:/claims";
    }

}
