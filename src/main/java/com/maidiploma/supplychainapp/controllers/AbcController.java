package com.maidiploma.supplychainapp.controllers;

import com.maidiploma.supplychainapp.model.ProductWithCategory;
import com.maidiploma.supplychainapp.service.CalculateService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@Controller
@RequestMapping("/abcxyz")
public class AbcController {

    private final CalculateService calculateService;

    public AbcController(CalculateService calculateService) {
        this.calculateService = calculateService;
    }

    @GetMapping
    public String getABCPage() {
        return "abc";
    }

    @PostMapping("/run")
    public String calculateABC(
            @RequestParam("aPercentage") int a,
            @RequestParam("bPercentage") int b,
            @RequestParam("cPercentage") int c,
            @RequestParam("xPercentage") int x,
            @RequestParam("yPercentage") int y,
            Model model) {

        List<ProductWithCategory> products = calculateService.ABCXYZ(a*0.01,b*0.01,c*0.01, x*0.01, y*0.01, 0);
        int[] stats = calculateService.getStats(products);

        model.addAttribute("products", products);
        model.addAttribute("data", stats);

        return "abc";
    }

}
