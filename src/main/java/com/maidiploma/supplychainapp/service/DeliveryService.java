package com.maidiploma.supplychainapp.service;

import com.maidiploma.supplychainapp.model.*;
import com.maidiploma.supplychainapp.model.compositeKeys.DeliveryProductId;
import com.maidiploma.supplychainapp.model.compositeKeys.WarehouseProductId;
import com.maidiploma.supplychainapp.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final ProductRepository productRepository;
    private final DeliveryRepository deliveryRepository;
    private final DeliveriesProductsRepository deliveriesProductsRepository;
    private final WarehousesProductsRepository warehouseProductRepository;
    private final DeliveriesProductsService deliveriesProductsService;
    private final SettingsService settingsService;

public List<Integer> getTimeDiff( Long warehouseId,Long supplierId, Long toWarehouseId) {
    List<Duration> durations = deliveryRepository.findDeliveryDelaysBetweenWarehouseAndSupplierOrWarehouse(warehouseId, supplierId, toWarehouseId);
    List<Integer> days = durations.stream()
            .map(d -> (int) d.toDays())
            .collect(Collectors.toList());
    return  days;
}
    public Delivery findById(Long id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Delivery not found with id: " + id));
    }

    @Transactional
    public void deleteDelivery(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        deliveriesProductsRepository.deleteAllByDelivery(delivery);
        deliveryRepository.delete(delivery);
    }



    public void saveDeliveryWithProducts(LocalDate date, LocalDate expectedDate, Long fromId, Long toId, MultipartFile file, SupplyChainEdge edge) throws IOException {

        Delivery delivery = new Delivery();
        delivery.setDeliveryDate(date);
        delivery.setEdge_id(edge);
        delivery.setExpectedDate(expectedDate);
        deliveryRepository.save(delivery);

        List<DeliveriesProducts> deliveriesProducts = parseProductsFromExcel(file.getInputStream(), delivery);
        deliveriesProductsRepository.saveAll(deliveriesProducts);

        Warehouse toWarehouse = edge.getToNodeId().getWarehouse();

        for (DeliveriesProducts dp : deliveriesProducts) {
            Product product = dp.getId().getProduct();
            int quantity = dp.getQuantity();
            LocalDate  cur_date = settingsService.getEnd();
            for (LocalDate d = date; !d.isAfter(cur_date); d = d.plusDays(1)) {
                WarehouseProductId wpId = new WarehouseProductId();
                wpId.setWarehouse(toWarehouse);
                wpId.setProduct(product);
                wpId.setStockDate(d);

                Optional<WarehousesProducts> optionalStock = warehouseProductRepository.findById(wpId);
                if (optionalStock.isPresent()) {
                    WarehousesProducts stock = optionalStock.get();
                    stock.setQuantity(stock.getQuantity() + quantity);
                    warehouseProductRepository.save(stock);
                } else {
                    WarehousesProducts newStock = new WarehousesProducts();
                    newStock.setId(wpId);
                    newStock.setQuantity(quantity);
                    warehouseProductRepository.save(newStock);
                }
            }

        }
    }

    private List<DeliveriesProducts> parseProductsFromExcel(InputStream is, Delivery delivery) throws IOException {
        List<DeliveriesProducts> result = new ArrayList<>();
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

            DeliveriesProducts dp = new DeliveriesProducts();
            DeliveryProductId dpId = new DeliveryProductId();
            dpId.setProduct(product);
            dpId.setDelivery(delivery);

            dp.setId(dpId);
            dp.setQuantity(quantity);

            result.add(dp);
        }

        return result;
    }

    public List<Delivery> getAll() {
        return  deliveryRepository.findAll(Sort.by(Sort.Direction.DESC, "deliveryDate"));

    }

    public HttpServletResponse downloadExcel(Long id, HttpServletResponse response) throws IOException {
        Delivery delivery = deliveryRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Delivery not found with id: " + id));
        List<DeliveriesProducts> products = deliveriesProductsService.findByDelivery(delivery);

        String warehouseName = delivery.getEdge_id().getToNodeId().getName();
        String fileName = warehouseName + "_" + delivery.getDeliveryDate() + "_поставка.xlsx";

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Поставка");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Код товара");
        header.createCell(1).setCellValue("Название товара");
        header.createCell(2).setCellValue("Количество товара");

        int rowNum = 1;
        for (DeliveriesProducts dp : products) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(dp.getId().getProduct().getSku());
            row.createCell(1).setCellValue(dp.getId().getProduct().getName());
            row.createCell(2).setCellValue(dp.getQuantity());
        }

        workbook.write(response.getOutputStream());
        workbook.close();
        return response;
    }

}

