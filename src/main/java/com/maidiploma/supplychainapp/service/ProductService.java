package com.maidiploma.supplychainapp.service;

import com.maidiploma.supplychainapp.model.*;
import com.maidiploma.supplychainapp.model.compositeKeys.ShipmentProductId;
import com.maidiploma.supplychainapp.model.compositeKeys.SupplierProductId;
import com.maidiploma.supplychainapp.model.compositeKeys.WarehouseProductId;
import com.maidiploma.supplychainapp.repository.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final WarehousesProductsService warehousesProductsService;
    private final SettingsService settingsService;
    private final WarehouseRepository warehouseRepository;
    private final SupplierRepository supplierRepository;
    private final SuppliersProductsRepository suppliersProductsRepository;
    private final ShipmentRepository shipmentRepository;
    private final ShipmentsProductsRepository shipmentsProductsRepository;

    public ProductService(ProductRepository productRepository, WarehousesProductsService warehousesProductsService, SettingsService settingsService, WarehouseRepository warehouseRepository, SupplierRepository supplierRepository, SuppliersProductsRepository suppliersProductsRepository, ShipmentRepository shipmentRepository, ShipmentsProductsRepository shipmentsProductsRepository) {
        this.productRepository = productRepository;
        this.warehousesProductsService = warehousesProductsService;
        this.settingsService = settingsService;
        this.warehouseRepository = warehouseRepository;
        this.supplierRepository = supplierRepository;
        this.suppliersProductsRepository = suppliersProductsRepository;
        this.shipmentRepository = shipmentRepository;
        this.shipmentsProductsRepository = shipmentsProductsRepository;
    }


    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        productRepository.delete(product);
    }

    public List<Product> getAll() {
        return productRepository.findAll();
    }

    public void save(Product product) {
        productRepository.save(product);
        warehousesProductsService.fillZeroStock(settingsService.getStart(), settingsService.getEnd(), warehouseRepository.findAll(), List.of(product));

    }

    public void exportExcel(HttpServletResponse response) throws IOException {
        List<Product> productList = productRepository.findAll();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Список товаров");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Код товара");
        header.createCell(1).setCellValue("Название");
        header.createCell(2).setCellValue("Объём упаковки");
        header.createCell(3).setCellValue("Вес");

        int rowNum = 1;
        for (Product product : productList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(product.getSku());
            row.createCell(1).setCellValue(product.getName());
            row.createCell(2).setCellValue(product.getVolume().setScale(2, RoundingMode.HALF_UP).toString());
            row.createCell(3).setCellValue(product.getWeight().setScale(2, RoundingMode.HALF_UP).toString());
        }

        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String filename = URLEncoder.encode("СписокТоваров.xlsx", StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + filename);

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    public void importExcel(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);

            List<Product> productsToSave = new ArrayList<>();
            int addedCount = 0;
            int skippedCount = 0;

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String sku = getCellString(row.getCell(0)).trim();

                if (productRepository.existsBySku(sku)) {
                    skippedCount++;
                    continue;
                }

                Product product = new Product();
                product.setSku(sku);
                product.setName(getCellString(row.getCell(1)).trim());
                product.setVolume(new BigDecimal(getCellString(row.getCell(2))));
                product.setWeight(new BigDecimal(getCellString(row.getCell(3))));

                productsToSave.add(product);
                addedCount++;
            }
            productRepository.saveAll(productsToSave);
            warehousesProductsService.fillZeroStock(settingsService.getStart(), settingsService.getEnd(), warehouseRepository.findAll(), productsToSave);
            workbook.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public Product findBySku(String sku) {
        return productRepository.findBySku(sku).orElse(null);
    }

    public void deleteBySku(String sku) {
        Optional<Product> optional = productRepository.findBySku(sku);
        optional.ifPresent(productRepository::delete);
    }

    public void updateProductSupplier(Long supplierId, String sku, Double price) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new NoSuchElementException("Продукт не найден"));

        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new NoSuchElementException("Поставщик не найден"));

        SupplierProductId id = new SupplierProductId();
        id.setSupplier(supplier);
        id.setProduct(product);

        SuppliersProducts productSupplier = suppliersProductsRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Связь между поставщиком и продуктом не найдена"));

        productSupplier.setPrice(BigDecimal.valueOf(price));

        suppliersProductsRepository.save(productSupplier);
    }

    public Product getById(Long productId) {
        return productRepository.findById(productId).orElseThrow();
    }

    public void importDemandFromExcel(MultipartFile file, Long productId, Long warehouseId) throws IOException {
        try (InputStream is = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            rowIterator.next();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                String stockDateStr = getCellString(row.getCell(0));  // Дата
                Integer quantity = (int) row.getCell(1).getNumericCellValue(); // Количество

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                LocalDate stockDate = LocalDate.parse(stockDateStr, formatter);


                Product product = productRepository.findById(productId).orElseThrow();
                Warehouse warehouse = warehouseRepository.findById(warehouseId).orElseThrow();

                List<ShipmentsProducts> shipmentsProducts = new ArrayList<>();

                Shipment shipment = new Shipment();
                if (!shipmentRepository.findByShipmentDateAndWarehouse(stockDate, warehouse).isEmpty()) {
                    shipment = shipmentRepository.findByShipmentDateAndWarehouse(stockDate, warehouse).get(0);
                    shipmentsProducts = shipmentsProductsRepository.findById_Shipment(shipment);
                    for (ShipmentsProducts shipmentsProduct : shipmentsProducts) {
                        if (shipmentsProduct.getId().getProduct().getId().equals(product.getId())) {
                            shipmentsProduct.setQuantity(shipmentsProduct.getQuantity() + quantity);
                            shipmentsProductsRepository.save(shipmentsProduct);
                            continue;
                        }
                    }
                } else {
                    shipment.setShipmentDate(stockDate);
                    shipment.setWarehouse(warehouse);
                    shipmentRepository.save(shipment);
                }

                ShipmentProductId id = new ShipmentProductId();
                id.setProduct(product);
                id.setShipment(shipment);

                ShipmentsProducts sp = new ShipmentsProducts();
                sp.setQuantity(quantity);
                sp.setId(id);
                shipmentsProductsRepository.save(sp);

                List<WarehousesProducts> warehousesProducts = warehousesProductsService.findById_Warehouse_IdAndId_StockDate(warehouseId, stockDate);
                WarehousesProducts need = new WarehousesProducts();
                for (WarehousesProducts warehousesProduct : warehousesProducts) {
                    if (warehousesProduct.getId().getProduct().getId().equals(product.getId()))
                        need = warehousesProduct;
                }

                int newQ = need.getQuantity() - quantity;
                if (newQ < 0) newQ = 0;
                System.out.println(newQ + " " + need.getQuantity());
                need.setQuantity(newQ);
                System.out.println(need.getQuantity() + " " + newQ);
                warehousesProductsService.save(need);
                warehousesProductsService.extractPreviousStock(stockDate.plusDays(1), settingsService.getEnd(), warehouse, product, quantity);

            }

            workbook.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getCellString(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    Date date = cell.getDateCellValue();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                    return formatter.format(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                } else {
                    double val = cell.getNumericCellValue();
                    if (val == (long) val) {
                        return String.valueOf((long) val);
                    } else {
                        return String.valueOf(val);
                    }
                }
            case STRING:
                return cell.getStringCellValue();
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            default:
                return cell.toString();
        }
    }
}
