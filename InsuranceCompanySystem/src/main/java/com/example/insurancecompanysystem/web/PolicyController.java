package com.example.insurancecompanysystem.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

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
        return "all_policies";
    }


    @GetMapping("/create-policy")
    public String showCreatePolicyForm(Model model) {
        // Add any necessary data to the model if needed
        return "create-policy"; // Replace with the actual name of your HTML form view
    }


    //treba tuka i policyPrice i policyAtributes da kreirame pred da se kreira polisa i vajda da stavime dropdown za da moze
    //userot da odbere agent, manager i customer?
    @PostMapping("/create-policy")
    public String createPolicy(@RequestParam("managerId") int managerId,
                               @RequestParam("agentId") int agentId,
                               @RequestParam("startDate") String startDate,
                               @RequestParam("endDate") String endDate,
                               @RequestParam("policyPriceId") int policyPriceId,
                               @RequestParam("policyStatusId") int policyStatusId,
                               @RequestParam("policyHolderId") int policyHolderId,
                               @RequestParam("policyPaymentId") int policyPaymentId,
                               @RequestParam("insuranceTypeId") int insuranceTypeId,
                               @RequestParam("subTypeId") int subTypeId,
                               @RequestParam("policyAttributesId") int policyAttributesId, Model model) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate parsedStartDate = LocalDate.parse(startDate, formatter);
        LocalDate parsedEndDate = LocalDate.parse(endDate, formatter);

        try {
            jdbcTemplate.execute("CALL createpolicy(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    (CallableStatementCallback<Object>) cs -> {
                        cs.setInt(1, managerId);
                        cs.setInt(2, agentId);
                        cs.setDate(3, Date.valueOf(parsedStartDate));
                        cs.setDate(4, Date.valueOf(parsedEndDate));
                        cs.setInt(5, policyPriceId);
                        cs.setInt(6, 1);
                        cs.setInt(7, policyHolderId);
                        cs.setInt(8, policyPaymentId);
                        cs.setInt(9, insuranceTypeId);
                        cs.setInt(10, subTypeId);
                        cs.setInt(11, policyAttributesId);
                        cs.execute();
                        return null;
                    });
        } catch (Exception e) {
            // Handle exception and display appropriate error message
            String errorMessage = e.getMessage();
            model.addAttribute("errorMessage", errorMessage);
            return "error-page";
        }

        return "redirect:/policies";
    }




////////////////////TEST//////////////////////////////////////////
    //za da vi se kreiraat polisi ova vo konsola kaj bazata treba da go izvrsite
    // SELECT MAX(id) FROM policy;
    //SELECT setval('policy_id_seq', 3000000 - 1);
//    @GetMapping("/create-policy")
//    public ResponseEntity<String> testCreatePolicy() {
//        try {
//            int managerId = 3;
//            int agentId = 1;
//            LocalDate startDate = LocalDate.of(2022, 6, 1);
//            LocalDate endDate = LocalDate.of(2023, 6, 30);
//            int priceId = 3;
//            int statusId = 4;
//            int holderId = 5;
//            int paymentId = 6;
//            int typeId = 7;
//            int subTypeId = 8;
//            int attributesId = 9;
//
//            // Call the stored procedure using jdbcTemplate without the new unique id
//            String sql = "CALL createpolicy(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
//            jdbcTemplate.update(sql, managerId, agentId, startDate, endDate, priceId, statusId, holderId, paymentId, typeId, subTypeId, attributesId);
//
//            return ResponseEntity.ok("Policy created successfully");
//        } catch (DataAccessException e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create policy: " + e.getMessage());
//        }
//    }


}
