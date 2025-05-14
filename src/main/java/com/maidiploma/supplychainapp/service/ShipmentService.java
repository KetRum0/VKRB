package com.maidiploma.supplychainapp.service;

import com.maidiploma.supplychainapp.model.*;
import com.maidiploma.supplychainapp.model.compositeKeys.ShipmentProductId;
import com.maidiploma.supplychainapp.model.compositeKeys.WarehouseProductId;
import com.maidiploma.supplychainapp.repository.ProductRepository;
import com.maidiploma.supplychainapp.repository.ShipmentsProductsRepository;
import com.maidiploma.supplychainapp.repository.ShipmentRepository;
import com.maidiploma.supplychainapp.repository.WarehousesProductsRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ProductRepository productRepository;
    private final ShipmentsProductsRepository shipmentsProductsRepository;
    private final WarehousesProductsRepository warehouseProductRepository;
    private final SettingsService settingsService;


    public Shipment findById(Long id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Shipment not found with id: " + id));
    }

    public void deleteShipment(Long id) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        shipmentsProductsRepository.deleteAllByShipment(shipment);
        shipmentRepository.delete(shipment);
    }

    public void saveShipmentWithProducts(LocalDate date, Warehouse warehouse, MultipartFile file) throws IOException {

        Shipment shipment = new Shipment();
        shipment.setShipmentDate(date);
        shipment.setWarehouse(warehouse);
        shipmentRepository.save(shipment);

        List<ShipmentsProducts> shipmentsProducts = parseProductsFromExcel(file.getInputStream(), shipment);
        shipmentsProductsRepository.saveAll(shipmentsProducts);

        applyShipmentToStock(shipment, shipmentsProducts, settingsService.getEnd());
    }

    public void applyShipmentToStock(Shipment shipment, List<ShipmentsProducts> shipmentsProducts, LocalDate cur_date) {
        Warehouse warehouse = shipment.getWarehouse();
        LocalDate shipmentDate = shipment.getShipmentDate();

        for (ShipmentsProducts sp : shipmentsProducts) {
            Product product = sp.getId().getProduct();
            int quantityToSubtract = sp.getQuantity();

            for (LocalDate date = shipmentDate; !date.isAfter(cur_date); date = date.plusDays(1)) {
                WarehouseProductId wpId = new WarehouseProductId();
                wpId.setWarehouse(warehouse);
                wpId.setProduct(product);
                wpId.setStockDate(date);

                Optional<WarehousesProducts> optionalStock = warehouseProductRepository.findById(wpId);
                if (optionalStock.isPresent()) {
                    WarehousesProducts stock = optionalStock.get();
                    int updatedQuantity = stock.getQuantity() - quantityToSubtract;
                    stock.setQuantity(Math.max(updatedQuantity, 0));
                    warehouseProductRepository.save(stock);
                } else {
                    System.out.printf("Нет запаса для склада %d, товара %d на дату %s%n",
                            warehouse.getId(), product.getId(), date);
                }
            }
        }
    }


    private List<ShipmentsProducts> parseProductsFromExcel(InputStream is, Shipment shipment) throws IOException {
        List<ShipmentsProducts> result = new ArrayList<>();
        Workbook workbook = new XSSFWorkbook(is);
        Sheet sheet = workbook.getSheetAt(0);
        DataFormatter formatter = new DataFormatter();

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;
            String sku = formatter.formatCellValue(row.getCell(0)).trim();
            String quantityCell = formatter.formatCellValue(row.getCell(2)).trim();
            int quantity = Integer.parseInt(quantityCell);

            Product product = productRepository.findBySku(sku)
                    .orElseThrow(() -> new RuntimeException("Продукт с SKU " + sku + " не найден"));

            ShipmentsProducts sp = new ShipmentsProducts();
            ShipmentProductId spId = new ShipmentProductId();
            spId.setProduct(product);
            spId.setShipment(shipment);

            sp.setId(spId);
            sp.setQuantity(quantity);

            result.add(sp);
        }

        workbook.close();
        return result;
    }

    public List<Shipment> findAll() {
        return shipmentRepository.findAll(Sort.by(Sort.Direction.DESC, "shipmentDate"));
    }

    public void exportExcel(HttpServletResponse response, Long id) throws IOException {
        Shipment shipment = shipmentRepository.findById(id).orElseThrow();
        List<ShipmentsProducts> products = shipmentsProductsRepository.findById_Shipment(shipment);

        String warehouseName = shipment.getWarehouse().getName();
        String fileName = warehouseName + "_" + shipment.getShipmentDate() + "_отгрузка.xlsx";

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Отгрузка");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Код товара");
        header.createCell(1).setCellValue("Название товара");
        header.createCell(2).setCellValue("Количество товара");

        int rowNum = 1;
        for (ShipmentsProducts sp : products) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(sp.getId().getProduct().getSku());
            row.createCell(1).setCellValue(sp.getId().getProduct().getName());
            row.createCell(2).setCellValue(sp.getQuantity());
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }


}
