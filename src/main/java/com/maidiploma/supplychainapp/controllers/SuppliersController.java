package com.maidiploma.supplychainapp.controllers;

import com.maidiploma.supplychainapp.model.Product;
import com.maidiploma.supplychainapp.model.Supplier;
import com.maidiploma.supplychainapp.model.SuppliersProducts;
import com.maidiploma.supplychainapp.model.compositeKeys.SupplierProductId;
import com.maidiploma.supplychainapp.service.ProductService;
import com.maidiploma.supplychainapp.service.SupplierService;
import com.maidiploma.supplychainapp.service.SuppliersProductsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/suppliers")
public class SuppliersController {


    private final SupplierService supplierService;
    private final ProductService productService;
    private final SuppliersProductsService suppliersProductsService;


    public SuppliersController(SupplierService supplierService, ProductService productService, SuppliersProductsService suppliersProductsService) {
        this.supplierService = supplierService;
        this.productService = productService;
        this.suppliersProductsService = suppliersProductsService;
    }

    @PostMapping("/add")
    public String addSupplier(@RequestParam String Sname,
                              @RequestParam BigDecimal Slattitude,
                              @RequestParam BigDecimal Slongitude) {

        supplierService.add(Sname, Slattitude, Slongitude);

        return "redirect:/supplychain";
    }

    @PostMapping("/edit")
    public String editSupplier(@RequestParam Long id,
                               @RequestParam String name,
                               @RequestParam BigDecimal Slattitude,
                               @RequestParam BigDecimal Slongitude) {

        supplierService.save(id,name,Slattitude,Slongitude);
        return "redirect:/supplychain";
    }

    @PostMapping("/delete/{id}")
    public String deleteSupplier(@PathVariable Long id) {
        supplierService.deleteById(id);
        return "redirect:/supplychain";
    }

    @GetMapping("/view/{id}")
    public String viewSupplier(@PathVariable Long id, Model model) {
        List<SuppliersProducts> products = suppliersProductsService.findById_SupplierId(id);
        Supplier supplier = supplierService.getById(id);
        model.addAttribute("products", products);
        model.addAttribute("supplier", supplier);
        return "supplier";
    }

    @PostMapping("/view/{id}/add")
    public String addProduct(@PathVariable Long id,
                             @RequestParam String sku,
                             @RequestParam(required = false) String name,
                             @RequestParam BigDecimal price,
                             RedirectAttributes redirectAttributes) {

        Supplier supplier = supplierService.getById(id);
        Product product = productService.findBySku(sku);

        if (product == null) {
                redirectAttributes.addFlashAttribute("error", "Товара с таким SKU не существует");
                return "redirect:/suppliers/view/" + id;
            }

        SupplierProductId idComposite = new SupplierProductId();
        idComposite.setSupplier(supplier);
        idComposite.setProduct(product);

        if (!suppliersProductsService.existsById(idComposite)) {
            SuppliersProducts suppliersProduct = new SuppliersProducts();
            suppliersProduct.setId(idComposite);
            suppliersProduct.setPrice(price);
            suppliersProductsService.save(suppliersProduct);
        }

        return "redirect:/suppliers/view/" + id;
    }

    @PostMapping("/suppliers/view/{supplierId}/delete")
    public String deleteProduct(@PathVariable Long supplierId,
                                @RequestParam("sku") String sku) {
        productService.deleteBySku(sku);
        return "redirect:/supplier/view/" + supplierId;
    }

    @PostMapping("view/{id}/edit")
    public String editProductPrice(
            @PathVariable Long id,
            @RequestParam("productId") Long productId,
            @RequestParam("price") BigDecimal newPrice,
            RedirectAttributes redirectAttributes
    ) {
        try {
            SupplierProductId key = new SupplierProductId();
            key.setProduct(productService.getById(productId));
            key.setSupplier(supplierService.getById(id));// если ключ составной
            SuppliersProducts supplierProduct = suppliersProductsService.findById(key);

            supplierProduct.setPrice(newPrice);
            suppliersProductsService.save(supplierProduct);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении цены: " + e.getMessage());
        }

        return "redirect:/suppliers/view/" + id;
    }

    @PostMapping("/view/{id}/delete")
    public String deleteProductFromSupplier(
            @PathVariable("id") Long supplierId,
            @RequestParam("id_p") Long productId) {

        Supplier s = supplierService.getById(supplierId);
        Product p = productService.getById(productId);
        SupplierProductId idComposite = new SupplierProductId();
        idComposite.setSupplier(s);
        idComposite.setProduct(p);

       suppliersProductsService.deleteById(idComposite);

        return "redirect:/suppliers/view/" + supplierId;
    }



}
