package com.example.insurancecompanysystem.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Controller
public class DamageController {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DamageController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/damages")
    public String getAllDamages(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int pageSize,
                                Model model) {
        int offset = page * pageSize;

        String sql = "SELECT * FROM all_damages OFFSET ? LIMIT ?";
        List<Map<String, Object>> viewData = jdbcTemplate.queryForList(sql, offset, pageSize);

        model.addAttribute("viewData", viewData);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", pageSize);

        return "all_damages";
    }

    @GetMapping("/damages/{id}")
    public String getDamageById(@PathVariable("id") int id, Model model) {
        String sql = "SELECT * FROM all_damages WHERE id = ?";
        Map<String, Object> damageData = jdbcTemplate.queryForMap(sql, id);

        model.addAttribute("damageData", damageData);

        return "all_damages";
    }

    @GetMapping("/damages/create")
    public String showCreateDamageForm(Model model) {
        return "create_damage";
    }

    @PostMapping("/damages/create")
    public String createDamage(@RequestParam("policyId") int policyId,
                               @RequestParam("claimId") int claimId,
                               @RequestParam("damageType") String damageType,
                               @RequestParam("statusOfPayment") boolean statusOfPayment,
                               @RequestParam("receivedPayment") double receivedPayment,
                               @RequestParam("receivedFrom") String receivedFrom,
                               @RequestParam("paidToCustomer") String paidToCustomer,
                               @RequestParam("dateOfPayment") String dateOfPayment) {
        String sql = "CALL createdamage(?, ?, ?)";
        int damageId = jdbcTemplate.update(sql, policyId, claimId, damageType);
        sql = "INSERT INTO damagepayment(damageId, status_of_payment, received_payment, received_from, paid_to_customer, date_of_payment) VALUES(?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, damageId, statusOfPayment, receivedPayment, receivedFrom, paidToCustomer, dateOfPayment);

        return "redirect:/damages";
    }
}

