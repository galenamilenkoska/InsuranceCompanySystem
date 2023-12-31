package com.example.insurancecompanysystem.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/damages")
public class DamageController {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DamageController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public String getAllDamages(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int pageSize,
                                Model model) {
        int offset = page * pageSize;

        String sql = "SELECT * FROM all_damages OFFSET ? LIMIT ?";
        List<Map<String, Object>> viewData = jdbcTemplate.queryForList(sql, offset, pageSize);

        // Add the view data and pagination information to the model
        model.addAttribute("viewData", viewData);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", pageSize);

        int totalClaims = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM all_damages", Integer.class);
        int totalPages = (int) Math.ceil((double) totalClaims / pageSize);

        model.addAttribute("totalPages", totalPages);

        return "all_damages";
    }

    @GetMapping("/{id}")
    public String getDamageById(@PathVariable("id") int id, Model model) {
        String sql = "SELECT * FROM all_damages WHERE id = ?";
        Map<String, Object> damageData = jdbcTemplate.queryForMap(sql, id);

        model.addAttribute("damageData", damageData);

        return "all_damages";
    }

    @GetMapping("/create")
    public String showCreateDamageForm(Model model) {
        return "create_damage";
    }

    @PostMapping("/create")
    public String createDamage(@RequestParam("policyId") int policyId,
                               @RequestParam("claimId") int claimId,
                               @RequestParam("damageType") String damageType,
                               @RequestParam("statusOfPayment") boolean statusOfPayment,
                               @RequestParam("receivedPayment") double receivedPayment,
                               @RequestParam("receivedFrom") String receivedFrom,
                               @RequestParam("paidToCustomer") String paidToCustomer,
                               @RequestParam("dateOfPayment") String dateOfPayment) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate parseddateOfPayment = LocalDate.parse(dateOfPayment, formatter);

        String sql = "CALL createdamage(?, ?, ?)";
        jdbcTemplate.update(sql, policyId, claimId, damageType);
        int damageId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM damage", Integer.class);

        sql = "INSERT INTO damagepayment(damageId, status_of_payment, received_payment, received_from, paid_to_customer, date_of_payment) VALUES(?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, damageId, statusOfPayment, receivedPayment, receivedFrom, paidToCustomer, parseddateOfPayment);

        return "redirect:/damages";
    }
    /*
    g sql = "SELECT * FROM all_policies order by id OFFSET ? LIMIT ?";
        List<Map<String, Object>> viewData = jdbcTemplate.queryForList(sql, offset, pageSize);
     */

    @GetMapping("/update/{id}")
    public String showCreateDamageForm(@RequestParam("damageId") int damageId,
                                       @RequestParam("claimId") int claimId,
                                       @RequestParam("damageType") String damageType,
                                       @RequestParam("statusOfPayment") boolean statusOfPayment,
                                       @RequestParam("receivedPayment") double receivedPayment,
                                       @RequestParam("receivedFrom") String receivedFrom,
                                       @RequestParam("paidToCustomer") String paidToCustomer,
                                       @RequestParam("dateOfPayment") String dateOfPayment,
                                       Model model) {
        model.addAttribute("damageId", damageId);
        String sql = "SELECT policyid FROM damage WHERE id = ";
        sql.concat(String.valueOf(damageId));
        int policyId = jdbcTemplate.queryForObject(sql, Integer.class);
        model.addAttribute("policyId", policyId);
        model.addAttribute("claimId", claimId);
        model.addAttribute("damageType", damageType);
        model.addAttribute("statusOfPayment", statusOfPayment);
        model.addAttribute("receivedPayment", receivedPayment);
        model.addAttribute("receivedFrom", receivedFrom);
        model.addAttribute("paidToCustomer", paidToCustomer);
        model.addAttribute("dateOfPayment", dateOfPayment);

        return "update_damage";
    }

    @PutMapping("/update/{id}")
    public String updateDamage(
                                @PathVariable("damageId") int damageId,
                                @RequestParam("policyId") int policyId,
                               @RequestParam("claimId") int claimId,
                               @RequestParam("damageType") String damageType,
                               @RequestParam("statusOfPayment") boolean statusOfPayment,
                               @RequestParam("receivedPayment") double receivedPayment,
                               @RequestParam("receivedFrom") String receivedFrom,
                               @RequestParam("paidToCustomer") String paidToCustomer,
                               @RequestParam("dateOfPayment") String dateOfPayment,
                                Model model) {
        String sql = "CALL updatedamage(?, ?, ?, ?)";
        try{
            jdbcTemplate.update(sql, damageId, policyId, claimId, damageType);
        }
        catch(Exception e){
            String errorMessage = e.getMessage();
            model.addAttribute("errorMessage", errorMessage);
            return "error-page";
        }
        jdbcTemplate.update("delete from damagepayment where damageId = ?", damageId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(dateOfPayment, formatter);
        sql = "INSERT INTO damagepayment(damageId, status_of_payment, received_payment, received_from, paid_to_customer, date_of_payment) VALUES(?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, damageId, statusOfPayment, receivedPayment, receivedFrom, paidToCustomer, date);

        return "redirect:/damages";
    }
}

