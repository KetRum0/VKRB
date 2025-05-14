package com.maidiploma.supplychainapp.controllers;

import com.maidiploma.supplychainapp.model.ExportRequest;
import com.maidiploma.supplychainapp.model.OrderData;
import com.maidiploma.supplychainapp.model.ProductWithCategory;
import com.maidiploma.supplychainapp.model.Warehouse;
import com.maidiploma.supplychainapp.service.CalculateService;
import com.maidiploma.supplychainapp.service.ProductOpt;
import com.maidiploma.supplychainapp.service.ProductService;
import com.maidiploma.supplychainapp.service.WarehouseService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class CalculateController {

    private final CalculateService calculateService;
    private final WarehouseService warehouseService;
    private final ProductService productService;

    public CalculateController(CalculateService calculateService, WarehouseService warehouseService, ProductService productService) {
        this.calculateService = calculateService;
        this.warehouseService = warehouseService;
        this.productService = productService;
    }

    @GetMapping("/planner")
    public String getCalculatePage(Model model)  {
        List<Warehouse> warehouses = warehouseService.getAll();
        List<ProductOpt> products = new ArrayList<>();

        model.addAttribute("warehouses", warehouses);
        model.addAttribute("productsByWarehouse", products);

        return "calculate";
    }

    @PostMapping("/calculate/result")
    public String getResultPage(@RequestParam("s") int s,
                                @RequestParam("R") int R,
                                Model model)  {

        List<Warehouse> warehouses = warehouseService.getAll();
        Map<Long, List<ProductOpt>> productsByWarehouse = calculateService.getProductbyWarehouse(s, R);

        model.addAttribute("warehouses", warehouses);
        model.addAttribute("productsByWarehouse", productsByWarehouse);

        return "calculate";
    }

    @PostMapping("result/export")
    public ResponseEntity<byte[]> exportToExcel(@RequestBody ExportRequest request) throws Exception {
        String warehouseName = warehouseService.findById(request.getWarehouseId()).getName();
        List<OrderData> orders = request.getOrders();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Поставка");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Код товара");
        header.createCell(1).setCellValue("Название товара");
        header.createCell(2).setCellValue("Количество");

        for (int i = 0; i < orders.size(); i++) {
            OrderData order = orders.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(productService.getById(Long.valueOf(order.getProductId())).getSku());
            row.createCell(1).setCellValue(productService.getById(Long.valueOf(order.getProductId())).getName());
            row.createCell(2).setCellValue(order.getFinalOrderSize());
        }

        for (int i = 0; i < 3; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        String fileName = "Поставка_" + warehouseName + ".xlsx";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", encodedFileName);
        headers.setContentLength(outputStream.size());

        return ResponseEntity.ok()
                .headers(headers)
                .body(outputStream.toByteArray());
    }

}
