package com.maidiploma.supplychainapp.service;

import com.maidiploma.supplychainapp.model.Product;
import com.maidiploma.supplychainapp.model.Warehouse;
import com.maidiploma.supplychainapp.model.WarehousesProducts;
import com.maidiploma.supplychainapp.model.compositeKeys.WarehouseProductId;
import com.maidiploma.supplychainapp.repository.ProductRepository;
import com.maidiploma.supplychainapp.repository.WarehouseRepository;
import com.maidiploma.supplychainapp.repository.WarehousesProductsRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Service
public class WarehousesProductsService {
    private final WarehousesProductsRepository warehousesProductsRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    public WarehousesProductsService(WarehousesProductsRepository warehousesProductsRepository, ProductRepository productRepository, WarehouseRepository warehouseRepository) {
        this.warehousesProductsRepository = warehousesProductsRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
    }

    public void importExcel(MultipartFile file, Long warehouseId) throws IOException {
        try (InputStream is = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            rowIterator.next();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                String stockDateStr = getCellString(row.getCell(0));  // Дата
                String sku =  getCellString(row.getCell(1));           // Код товара
                String productName =  getCellString(row.getCell(2));   // Название товара
                Integer quantity = (int) row.getCell(3).getNumericCellValue(); // Количество

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                LocalDate stockDate = LocalDate.parse(stockDateStr, formatter);

                System.out.println(sku);

                Product product = productRepository.findBySku(sku).orElseThrow();
                Warehouse warehouse = warehouseRepository.findById(warehouseId).orElseThrow();

                WarehouseProductId warehouseProductId = new WarehouseProductId();
                warehouseProductId.setWarehouse(warehouse);
                warehouseProductId.setProduct(product);
                warehouseProductId.setStockDate(stockDate);

                WarehousesProducts warehousesProducts = new WarehousesProducts();
                warehousesProducts.setId(warehouseProductId);
                warehousesProducts.setQuantity(quantity);

                warehousesProductsRepository.save(warehousesProducts);
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
                        return String.valueOf((long) val); // Убираем .0
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



    public void save(WarehousesProducts stock) {
        warehousesProductsRepository.save(stock);
    }

    public List<Object[]> getTotalOccupiedPerDay() {
        return warehousesProductsRepository.getTotalOccupiedPerDay();
    }

    public void fillZeroStock(LocalDate startDate, LocalDate curDate, List<Warehouse> warehouses, List<Product> products) {
        System.out.println(startDate + " " + curDate + " " + warehouses + " " + products);
        for (LocalDate date = startDate; !date.isAfter(curDate); date = date.plusDays(1)) {
            for (Warehouse warehouse : warehouses) {
                for (Product product : products) {

                    WarehouseProductId id = new WarehouseProductId();
                    id.setWarehouse(warehouse);
                    id.setProduct(product);
                    id.setStockDate(date);
                    if(warehousesProductsRepository.findById(id).isPresent()) { continue;}

                    WarehousesProducts stock = new WarehousesProducts();
                    stock.setId(id);
                    stock.setQuantity(0);

                    warehousesProductsRepository.save(stock);
                }
            }
        }
    }

    public void fillPreviousStock( LocalDate startDate, LocalDate curDate, List<Warehouse> warehouses, List<Product> products) {
        for (LocalDate date = startDate; !date.isAfter(curDate); date = date.plusDays(1)) {
            for (Warehouse warehouse : warehouses) {
                for (Product product : products) {
                    WarehouseProductId id = new WarehouseProductId();
                    id.setWarehouse(warehouse);
                    id.setProduct(product);
                    id.setStockDate(startDate);
                    WarehousesProducts wp = new WarehousesProducts();
                    WarehousesProducts wp1 = new WarehousesProducts();

                    if (warehousesProductsRepository.findById(id).isPresent()) {
                        wp = warehousesProductsRepository.findById(id).get();
                        WarehouseProductId id1 = new WarehouseProductId();
                        id1.setWarehouse(warehouse);
                        id1.setProduct(product);
                        id1.setStockDate(date);

                        wp1.setId(id1);
                        wp1.setQuantity(wp.getQuantity());
                        warehousesProductsRepository.save(wp1);
                    }
                }
            }
        }
    }

        public void extractPreviousStock( LocalDate startDate, LocalDate curDate, Warehouse warehouse, Product product, int extract) {
            for (LocalDate date = startDate; !date.isAfter(curDate); date = date.plusDays(1)) {
                        WarehouseProductId id = new WarehouseProductId();
                        id.setWarehouse(warehouse);
                        id.setProduct(product);
                        id.setStockDate(date);
                        System.out.println(startDate);

                        WarehousesProducts wp1 = new WarehousesProducts();

                        if (warehousesProductsRepository.findById(id).isPresent()) {
                            System.out.println("уже найдена запис@@ь");
                            wp1 = warehousesProductsRepository.findById(id).get();
                            int newQ = wp1.getQuantity() - extract;
                            System.out.println(" " + wp1.getQuantity() + " + " + newQ);
                            wp1.setQuantity(newQ);
                            warehousesProductsRepository.save(wp1);
                        }
            }
        }


    public List<WarehousesProducts> findById_Warehouse_IdAndId_StockDate(Long id, LocalDate curDate) {
        return warehousesProductsRepository.findById_Warehouse_IdAndId_StockDate(id, curDate);
    }

    public List<Object[]> getTotalOccupiedPerDayByWarehouse(Long id) {
        return warehousesProductsRepository.getTotalOccupiedPerDayByWarehouse(id);
    }

    public List<WarehousesProducts> getAll() {
        return warehousesProductsRepository.findAll();
    }

    public List<WarehousesProducts> findById_Warehouse_IdAndId_Product_IdOrderById_StockDate(Long warehouseId, Long productId) {
        return warehousesProductsRepository.findById_Warehouse_IdAndId_Product_IdOrderById_StockDate(warehouseId, productId);
    }

    public Integer findQuantityByWarehouseIdAndProductIdAndStockDate(Long warehouseId, Long productId, LocalDate stockDate) {
        return warehousesProductsRepository.findQuantityByWarehouseIdAndProductIdAndStockDate(warehouseId, productId, stockDate);
    };


}
