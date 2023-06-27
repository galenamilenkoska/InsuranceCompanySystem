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

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/policies")
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

//    @GetMapping("/")
//    public String defaultRedirect() {
//        return "redirect:/policies";
//    }

    @GetMapping
    public String getPolicy(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int pageSize,
                            Model model) {
        // Calculate the offset based on the page number and page size
        int offset = page * pageSize;

        // Execute a SQL query with pagination
        String sql = "SELECT * FROM all_policies order by id OFFSET ? LIMIT ?";
        List<Map<String, Object>> viewData = jdbcTemplate.queryForList(sql, offset, pageSize);

        // Add the view data and pagination information to the model
        model.addAttribute("viewData", viewData);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", pageSize);

        // Calculate the total number of policies
        int totalPolicies = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM all_policies", Integer.class);

        // Calculate the total number of pages
        int totalPages = (int) Math.ceil((double) totalPolicies / pageSize);

        // Add the total number of pages to the model
        model.addAttribute("totalPages", totalPages);

        // Call the view named "policy-view" and pass the model to it
        return "all_policies";
    }


    @GetMapping("/create")
    public String showCreatePolicyForm(Model model) {
        return "create-policy-full";
    }

    @PostMapping("/create")
    public String createPolicy(
            @RequestParam("managerId") int managerId,
            @RequestParam("agentId") int agentId,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam("policyTypeId") int policyTypeId,
            @RequestParam("basePrice") BigDecimal basePrice,
            @RequestParam("deductible") BigDecimal deductible,
            @RequestParam("paymentDate") String paymentDate,
            @RequestParam("paymentAmount") BigDecimal paymentAmount,
            @RequestParam("paymentType") int paymentType,
            @RequestParam("policyStatusId") int policyStatusId,
            @RequestParam("insuranceTypeId") int insuranceTypeId,
            @RequestParam("subTypeId") int subTypeId,
            @RequestParam("policyHolderId") int policyHolderId,
            @RequestParam("registrationNo") String registrationNo,
            @RequestParam("chasisNo") String chasisNo,
            @RequestParam("description") String description,
            @RequestParam("levelOfJobRisk") String levelOfJobRisk,
            @RequestParam("propertyArea") String propertyArea,
            Model model
    ) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate parsedStartDate = LocalDate.parse(startDate, formatter);
        LocalDate parsedEndDate = LocalDate.parse(endDate, formatter);
        LocalDate parsedPaymentDate = LocalDate.parse(paymentDate, formatter);

        try {
            jdbcTemplate.execute("CALL createpolicyattributes(?, ?, ?, ?, ?)",
                    (CallableStatementCallback<Object>) cs -> {
                        cs.setString(1, registrationNo);
                        cs.setString(2, chasisNo);
                        cs.setString(3, description);
                        cs.setString(4, levelOfJobRisk);
                        cs.setString(5, propertyArea);
                        cs.execute();

                        return null;
                    });

            // Retrieve the maximum ID from policyattributes table
            int policyFullId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM policyatributes", Integer.class);

            jdbcTemplate.execute("CALL createpolicyprice(?, ?, ?)",
                    (CallableStatementCallback<Object>) cs -> {
                        cs.setInt(1, policyTypeId); // Use the request parameter
                        cs.setBigDecimal(2, basePrice); // Use the request parameter
                        cs.setBigDecimal(3, deductible); // Use the request parameter
                        cs.execute();

                        return null;
                    });

            jdbcTemplate.execute("CALL createpolicypayment(?, ?, ?)",
                    (CallableStatementCallback<Object>) cs -> {
                        cs.setDate(1, Date.valueOf(parsedPaymentDate));
                        cs.setBigDecimal(2, paymentAmount);
                        cs.setInt(3, paymentType);
                        cs.execute();

                        return null;
                    });

            jdbcTemplate.execute("CALL createpolicy(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    (CallableStatementCallback<Object>) cs -> {
                        cs.setInt(1, managerId);
                        cs.setInt(2, agentId);
                        cs.setDate(3, Date.valueOf(parsedStartDate));
                        cs.setDate(4, Date.valueOf(parsedEndDate));
                        cs.setInt(5, policyFullId);
                        cs.setInt(6, policyStatusId);
                        cs.setInt(7, policyHolderId);
                        cs.setInt(8, policyFullId);
                        cs.setInt(9, insuranceTypeId);
                        cs.setInt(10, subTypeId);
                        cs.setInt(11, policyFullId);
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

    @GetMapping("/update")
    public String showUpdatePolicyForm(@RequestParam("policyId") int index,
                                       @RequestParam("managerId") int managerId,
                                       @RequestParam("agentId") int agentId,
                                       @RequestParam("startDate") String startDate,
                                       @RequestParam("endDate") String endDate,
                                       @RequestParam("policyPriceId") int policyPriceId,
                                       @RequestParam("policyStatusId") int policyStatusId,
                                       @RequestParam("policyHolderId") int policyHolderId,
                                       @RequestParam("policyPaymentId") int policyPaymentId,
                                       @RequestParam("insuranceTypeId") int insuranceTypeId,
                                       @RequestParam("subTypeId") int subTypeId,
                                       @RequestParam("policyAttributesId") int policyAttributesId,
                                       Model model) {

        String sql = "SELECT * FROM policystatus order by id";
        List<Map<String, Object>> statusList = jdbcTemplate.queryForList(sql);

        model.addAttribute("policyId", index);
        model.addAttribute("managerId", managerId);
        model.addAttribute("agentId", agentId);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("policyPriceId", policyPriceId);
        model.addAttribute("policyStatusId", policyStatusId);
        model.addAttribute("policyHolderId", policyHolderId);
        model.addAttribute("policyPaymentId", policyPaymentId);
        model.addAttribute("insuranceTypeId", insuranceTypeId);
        model.addAttribute("subTypeId", subTypeId);
        model.addAttribute("policyAttributesId", policyAttributesId);

        model.addAttribute("statusList", statusList);

        return "update-policy";
    }

    @PostMapping("/update")
    public String updatePolicy(@RequestParam("policyId") int index,
                               @RequestParam("managerId") int managerId,
                               @RequestParam("agentId") int agentId,
                               @RequestParam("startDate") String startDate,
                               @RequestParam("endDate") String endDate,
                               @RequestParam("policyPriceId") int policyPriceId,
                               @RequestParam("policyStatusId") int policyStatusId,
                               @RequestParam("policyHolderId") int policyHolderId,
                               @RequestParam("policyPaymentId") int policyPaymentId,
                               @RequestParam("insuranceTypeId") int insuranceTypeId,
                               @RequestParam("subTypeId") int subTypeId,
                               @RequestParam("policyAttributesId") int policyAttributesId,
                               Model model) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate parsedStartDate = LocalDate.parse(startDate, formatter);
        LocalDate parsedEndDate = LocalDate.parse(endDate, formatter);

        try {
            jdbcTemplate.execute("CALL updatepolicy(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    (CallableStatementCallback<Object>) cs -> {
                        cs.setInt(1, index);
                        cs.setInt(2, managerId);
                        cs.setInt(3, agentId);
                        cs.setDate(4, Date.valueOf(parsedStartDate));
                        cs.setDate(5, Date.valueOf(parsedEndDate));
                        cs.setInt(6, policyPriceId);
                        cs.setInt(7, policyStatusId);
                        cs.setInt(8, policyHolderId);
                        cs.setInt(9, policyPaymentId);
                        cs.setInt(10, insuranceTypeId);
                        cs.setInt(11, subTypeId);
                        cs.setInt(12, policyAttributesId);
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

    //    @GetMapping("/create-policy")
//    public String showCreatePolicyForm(Model model) {
//        return "create-policy";
//    }

    //treba tuka i policyPrice i policyAtributes da kreirame pred da se kreira polisa i vajda da stavime dropdown za da moze
    //userot da odbere agent, manager i customer?
//    @PostMapping("/create-policy")
//    public String createPolicy(@RequestParam("managerId") int managerId,
//                               @RequestParam("agentId") int agentId,
//                               @RequestParam("startDate") String startDate,
//                               @RequestParam("endDate") String endDate,
//                               @RequestParam("policyPriceId") int policyPriceId,
//                               @RequestParam("policyStatusId") int policyStatusId,
//                               @RequestParam("policyHolderId") int policyHolderId,
//                               @RequestParam("policyPaymentId") int policyPaymentId,
//                               @RequestParam("insuranceTypeId") int insuranceTypeId,
//                               @RequestParam("subTypeId") int subTypeId,
//                               @RequestParam("policyAttributesId") int policyAttributesId, Model model) {
//
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//        LocalDate parsedStartDate = LocalDate.parse(startDate, formatter);
//        LocalDate parsedEndDate = LocalDate.parse(endDate, formatter);
//
//        try {
//            jdbcTemplate.execute("CALL createpolicy(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
//                    (CallableStatementCallback<Object>) cs -> {
//                        cs.setInt(1, managerId);
//                        cs.setInt(2, agentId);
//                        cs.setDate(3, Date.valueOf(parsedStartDate));
//                        cs.setDate(4, Date.valueOf(parsedEndDate));
//                        cs.setInt(5, policyPriceId);
//                        cs.setInt(6, 1);
//                        cs.setInt(7, policyHolderId);
//                        cs.setInt(8, policyPaymentId);
//                        cs.setInt(9, insuranceTypeId);
//                        cs.setInt(10, subTypeId);
//                        cs.setInt(11, policyAttributesId);
//                        cs.execute();
//                        return null;
//                    });
//        } catch (Exception e) {
//            // Handle exception and display appropriate error message
//            String errorMessage = e.getMessage();
//            model.addAttribute("errorMessage", errorMessage);
//            return "error-page";
//        }
//
//        return "redirect:/policies";
//    }

}
