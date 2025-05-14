package com.maidiploma.supplychainapp.service;

import com.maidiploma.supplychainapp.model.*;
import com.maidiploma.supplychainapp.repository.SupplierRepository;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.*;

import static org.aspectj.runtime.internal.Conversions.longValue;

@Service
public class CalculateService {
    private final SupplyChainEdgeService supplyChainEdgeService;
    private final ProductService productService;
    private final WarehouseService warehouseService;
    private final ShipmentsProductsService shipmentsProductsService;
    private final SettingsService settingsService;
    private final ForecastService forecastService;
    private final WarehousesProductsService warehousesProductsService;
    private final DeliveryService deliveryService;
    private final SupplierRepository supplierRepository;
    private final SuppliersProductsService suppliersProductsService;

    public CalculateService(SupplyChainEdgeService supplyChainEdgeService, ProductService productService, SupplierService supplierService, SuppliersProductsService suppliersProductsService, SupplyChainNodeService supplyChainNodeService, WarehouseService warehouseService, ShipmentsProductsService shipmentsProductsService, SettingsService settingsService, ForecastService forecastService, WarehousesProductsService warehousesProductsService, DeliveryService deliveryService, SupplierRepository supplierRepository) {
        this.supplyChainEdgeService = supplyChainEdgeService;
        this.productService = productService;
        this.warehouseService = warehouseService;
        this.shipmentsProductsService = shipmentsProductsService;
        this.settingsService = settingsService;
        this.forecastService = forecastService;
        this.warehousesProductsService = warehousesProductsService;
        this.deliveryService = deliveryService;
        this.supplierRepository = supplierRepository;
        this.suppliersProductsService = suppliersProductsService;
    }

    public List<ProductWithCategory> ABCXYZ(double a, double b, double c, double x, double y, double z) {

        List<Product> products = productService.getAll();
        List<Warehouse> warehouses = warehouseService.getAll();

        Map<Long, Long> productToQty = new HashMap<>(products.size());
        long grandTotal = 0L;

        for (Product product : products) {
            long sumForProduct = 0L;
            Long productId = product.getId();

            for (Warehouse wh : warehouses) {
                Long warehouseId = wh.getId();
                Long quantity = shipmentsProductsService.getTotalQuantitySentFromWarehouse(warehouseId, productId);
                BigDecimal totalMoney = product.getPrice().multiply(BigDecimal.valueOf(quantity));
                sumForProduct += totalMoney.longValue();
            }

            productToQty.put(productId, sumForProduct);
            grandTotal += sumForProduct;
        }

        List<Map.Entry<Long, Long>> sorted =
                new ArrayList<>(productToQty.entrySet());
        sorted.sort((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()));

        List<ProductWithCategory> result = new ArrayList<>();
        long cumulative = 0L;

        for (Map.Entry<Long, Long> entry : sorted) {cumulative += entry.getValue();
            double fraction = (double)cumulative / grandTotal;

            String category;
            if (fraction <= a) {
                category = "A";
            } else if (fraction <= a+b) {
                category = "B";
            } else {
                category = "C";
            }

            double coefVar = getCoef(entry.getKey());

            if (coefVar <= x) {
                category = category + "X";
            } else if (coefVar <= y) {
                category = category + "Y";
            } else {
                category = category + "Z";
            }
            Product product = productService.getById(entry.getKey());
            result.add(new ProductWithCategory(product.getId(), product.getSku(), product.getName(),category));
        }

        return result;
    }

    private double getCoef(Long productId) {
        HashSet<Long> warehousesId = getEndWarehouses(); /// change to only end warehouse
        double sum = 0L;
        for (Long whId : warehousesId) {
            HistoricalData d = shipmentsProductsService.getDemand( settingsService.getStart(), settingsService.getEnd(),  warehouseService.findById(whId), productService.getById(productId));
            sum += calculateStandardDeviation(d.getQuantities())/calculateMean(d.getQuantities());
        }
        return sum/warehousesId.size();
    }
    private void getChild(HashSet<Long> warehouses, Long parentId) {
        List<Long> childIds = supplyChainEdgeService.findChildWarehouseIdsByWarehouseId(parentId);
        if (childIds.isEmpty()) {warehouses.add(parentId);}
        else {
            for (Long childId : childIds) {
                getChild(warehouses, childId);
            }
        }

    }
    private HashSet<Long> getEndWarehouses() {
        List<Supplier> suppliers = supplierRepository.findAll();
        HashSet<Long> warehouses = new HashSet<>();
        for (Supplier supplier : suppliers) {
            List<Long> children = supplyChainEdgeService.findChildWarehouseIdsBySupplierId(supplier.getId());
            for (Long child : children) {
                getChild(warehouses, child);
            }

        }
        return warehouses;
    }


    public  double calculateMean(List<Integer> data) {
        double sum = 0;
        for (int num : data) {
            sum += num;
        }
        return sum / data.size();
    }

    public  double calculateStandardDeviation(List<Integer> data) {
        double mean = calculateMean(data);
        double sumSquaredDiffs = 0;
        for (int num : data) {
            sumSquaredDiffs += Math.pow(num - mean, 2);
        }
        return Math.sqrt(sumSquaredDiffs / data.size());
    }

    public ProductOpt calculateSingle(int s, int R, Long productId, Long warehouseId) {
        Product product = productService.getById(productId);
        Warehouse warehouse = warehouseService.findById(warehouseId);
        ProductOpt productOpt = new ProductOpt();

        List<Long> childrenId = supplyChainEdgeService.findChildWarehouseIdsByWarehouseId(warehouseId);

        double d_mean = 0L;
        double d_stdDev = 0L;
        NormalDistribution normal = new NormalDistribution();
        double z = normal.inverseCumulativeProbability((double) s /100);
        SupplyChainEdge edge = new SupplyChainEdge();
        Optional<SupplyChainEdge> edge1 = supplyChainEdgeService.findEdgeByToWarehouseId(warehouseId);
        if (edge1.isPresent()) {edge = edge1.get();}
        int L = edge.getLength();
        int t = edge.getLength();
        double l_mean = t;
        double l_stdDev = 0;
        int D = 0;
        int Q = 0;
        int S = 0;
        int SS = 0;
        int I = warehousesProductsService.findQuantityByWarehouseIdAndProductIdAndStockDate(warehouseId, productId, settingsService.getEnd());
        List<Integer> deliveryTimes = new ArrayList<>();
        if (edge.getFromNodeId().getWarehouse() != null) {
            deliveryTimes = deliveryService.getTimeDiff(edge.getFromNodeId().getWarehouse().getId(), null, warehouseId);
        } else if (edge.getToNodeId().getSupplier() != null) {
            deliveryTimes = deliveryService.getTimeDiff(null, edge.getToNodeId().getSupplier().getId(), warehouseId);
        }
        else deliveryTimes.add(1);

        for (int i = 0; i < deliveryTimes.size(); i++) {
            deliveryTimes.set(i, deliveryTimes.get(i) + t);
        }

        l_mean = calculateMean(deliveryTimes);
        l_stdDev = calculateStandardDeviation(deliveryTimes);
        int risk_period = t;
        List<Integer> dd = new ArrayList<>();
        for(int i = 0; i < risk_period; i++) {
            dd.add(0);
        }
        if (!childrenId.isEmpty()) {
            List<Integer> allDDR = new ArrayList<>(); //make more optimal
            for (int i = 0; i < R; i++) {
                allDDR.add(0);
            }
            for (Long childId : childrenId) {
                ProductOpt childOpt = calculateSingle(s, R,  productId, childId);
                d_mean += childOpt.getMean();
                d_stdDev += childOpt.getDev(); //change
                S += childOpt.getOptimalStock();
                I += childOpt.getI();
                I -= childOpt.getOptimalOrder();
                if(I < 0) I = 0;
                List<Integer> childD = childOpt.getD();
                int size = childD.size();
                childD = childD.subList(size- R, size);
                for(int i = 0; i < childD.size(); i++) {
                    allDDR.set(i, allDDR.get(i)+childD.get(i));
                }

                size = childD.size();
                int startIndex = Math.max(0, size - (risk_period));
                childD = childD.subList(startIndex, size);
                for (int i = 0; i < risk_period; i++) {
                    dd.set(i, dd.get(i)+childD.get(i));
                }

            }

            for (int d : dd) {
                D += d;
            }

            SS = (int) Math.ceil(z * Math.sqrt((l_mean) * d_stdDev * d_stdDev + d_mean * d_mean * l_stdDev * l_stdDev));

            dd = allDDR;
        }
        else {
            risk_period = t+R;

            HistoricalData hd = shipmentsProductsService.getDemand(settingsService.getStart(), settingsService.getEnd(), warehouse, product);
            d_mean = calculateMean(hd.getQuantities());
            d_stdDev = calculateStandardDeviation(hd.getQuantities());
            dd = forecastService.getBestModelResult(productId, warehouseId, risk_period).getQuantities();
            int size = dd.size();
            int startIndex = Math.max(0, size - (risk_period));
            dd = dd.subList(startIndex, size);
            for (int d : dd) {
                D += d;
            }
            SS = (int) Math.ceil(z * Math.sqrt((l_mean + R) * d_stdDev * d_stdDev + d_mean * d_mean * l_stdDev * l_stdDev));

        }

        S += D + SS;
        Q = S - I;
        if (Q < 0) Q = 0;

        productOpt = new ProductOpt(productId, product.getSku(), product.getName(), D, SS, S, Q);
        productOpt.setDev(d_stdDev);
        productOpt.setMean(d_mean);
        productOpt.setI(I);
        productOpt.setD(dd);

        return productOpt;

    }


    private void addChildren(Long parentId, List<Long> childrenId, Map<Long, Node> map, List<Long> endWarehouses) {
        if(childrenId.isEmpty()) {endWarehouses.add(parentId); return;}
        for(Long childId : childrenId) {
            Node childNode = new Node(childId);
        //    childNode.h = longValue(warehouseService.findById(childId).getCost());
            childNode.t = supplyChainEdgeService.findEdgeLengthByWarehouseIdAndWarehouseId(parentId, childId);
            childNode.M = childNode.t + map.get(parentId).M; //change
            List<Long> childrenIds = supplyChainEdgeService.findChildWarehouseIdsByWarehouseId(childId);
            System.out.println(childrenIds);
            childNode.parents.add(parentId);
            childNode.children.addAll(childrenIds);
            map.put(childId, childNode);
            addChildren(childId, childrenIds, map, endWarehouses);
        }
    }

    public int getOptimalOrder(Long productId) {

        double z = 1.64;

        //загрузка цепи
        Long mainSuppplierId = suppliersProductsService.findSuppliersByProductId(productId).get(0).getId(); //поставщик он всегда один

        BigDecimal cost = suppliersProductsService.findPriceBySupplierAndProduct(mainSuppplierId, productId); //не нужен потом в конце

        List<Long> endWarehouses = new ArrayList<>(); //идишники складов с которых надо найти среднее отклонение

        List<Long> startNodesId = supplyChainEdgeService.findChildWarehouseIdsBySupplierId(mainSuppplierId);
        Map<Long, Node> map = new HashMap<>();

        for(Long startNodeId : startNodesId) {
            Node startNode = new Node(startNodeId);
            startNode.cost = longValue(cost);
          //  startNode.h = longValue(warehouseService.findById(startNodeId).getCost());
            startNode.t = supplyChainEdgeService.findEdgeLengthBySupplierIdAndWarehouseId(mainSuppplierId, startNodeId);
            startNode.M = startNode.t;
            List<Long> childrenId = supplyChainEdgeService.findChildWarehouseIdsByWarehouseId(startNodeId);
            startNode.children.addAll(childrenId);
            map.put(startNodeId, startNode);
            addChildren(startNodeId, childrenId, map, endWarehouses);

            for(Long endNodeId : endWarehouses) {
                double dev = longValue(shipmentsProductsService.getDev(endNodeId));
                map.get(endNodeId).stdDev = longValue(dev);
                addDev(endNodeId, dev, map);
            }


        }


        //тест
        map = Map.of(
                1L, new Node(List.of(2L,3L),new ArrayList<>(), 20,1,20,20,3,3,58,1L),
                2L, new Node(List.of(4L),List.of(1L), 20,2,20,20,2,5,30,2L),
                3L, new Node(List.of(5L,6L),List.of(1L), 20,5,20,20,3,6,54,3L),
                4L, new Node(new ArrayList<>(),List.of(2L), 20,1,20,20,4,9,30,4L),
                5L, new Node(new ArrayList<>(),List.of(3L), 20,3,20,20,1,7,20,5L),
                6L, new Node(new ArrayList<>(),List.of(3L), 20,3,20,20,5,11,50,6L)
        );

        int numOfNodes = map.size();

        //маркировка
        List<Node> U = new ArrayList<>();
        Map<Long, Node> curNodes = new HashMap<>();

        for (Map.Entry<Long, Node> entry : map.entrySet()) {
            curNodes.put(entry.getKey(), new Node(entry.getValue()));

        }

        while(U.size() != numOfNodes) {
            for(Node cur : curNodes.values()) {
                if((cur.children.isEmpty() && cur.parents.size() <= 1 )|| (cur.parents.isEmpty() && cur.children.size() <= 1)) {
                    if (cur.children.isEmpty()) {
                        List<Long> parents = cur.parents;
                        for (Long parentId : parents) {
                            curNodes.get(parentId).children.remove(cur.nodeId);
                        }
                    }
                    if (cur.parents.isEmpty()) {
                        List<Long> children = cur.children;
                        for (Long childId : children) {
                            curNodes.get(childId).parents.remove(cur.nodeId);
                        }
                    }
                    U.add(map.get(cur.nodeId));
                    curNodes.remove(cur.nodeId);
                    break;
                }
            }
        }

        List<Long> nonMarked = new ArrayList<>();

        for(Node cur : map.values()) {
            nonMarked.add(cur.nodeId);
        }
        List<Long> marked = new ArrayList<>();


        //прямой проход
        for (Node node : U) {
            marked.add(node.nodeId);
        }

        List<int[][]> G = new ArrayList<>(nonMarked.size()-1);
        List<int[]> Phi = new ArrayList<>(nonMarked.size()-1);
        List<int[]> L = new ArrayList<>(nonMarked.size()-1);
        int[] GL = new int[nonMarked.size()];
        int[] GST = new int[nonMarked.size()];

        while (G.size() <= nonMarked.size()-1) {
            G.add(new int[0][0]);
        }
        while (Phi.size() <= nonMarked.size()-1) {
            Phi.add(new int[0]);
        }
        while (L.size() <= nonMarked.size()-1) {
            L.add(new int[0]);
        }

        for(int cur_i = 0; cur_i < U.size(); cur_i++) {
            Node node = U.get(cur_i);
            int cur_II = nonMarked.indexOf(node.nodeId);
            int max_lambda = node.M - node.t;
            int max_phi = node.M;
            if(node.children.isEmpty()) {
                max_phi = 0;}

            int[][] g = new int[max_lambda+1][max_phi+1];
            for(int i = 0; i <= max_lambda; i++) {
                for(int j = 0; j <= max_phi; j++) {
                    if(i+node.t-j < 0) {
                        g[i][j] = -1;
                        continue;
                    }
                    else g[i][j] = (int) (node.h*z*node.stdDev* Math.sqrt(i+node.t-j));
                    List<Long> children = node.children;
                    for(Long childId : children) {
                        if(marked.indexOf(childId) < cur_i) {
                            int minL = Integer.MAX_VALUE;
                            int[] t = L.get(nonMarked.indexOf(childId));
                            for(int y = j; y < t.length; y++) {
                                if(t[y] < minL) {
                                    minL = t[y];
                                }
                            }
                            if(minL == Integer.MAX_VALUE) {minL=0;}
                            g[i][j] += minL;
                        }
                    }
                    for(Long parentId : node.parents) {
                        if(marked.indexOf(parentId) < cur_i) {
                            int minPhi = Integer.MAX_VALUE;

                            int[] t = Phi.get((nonMarked.indexOf(parentId)));
                            for(int x = 0; x <= i; x++) {
                                if(t[x] < minPhi) {
                                    minPhi = t[x];
                                }
                            }
                            g[i][j] += minPhi;
                      }
                    }
                }
            }
            int[] phi = new int[max_phi+1];
            int[] l = new int[max_lambda+1];
            for(int i = 0; i <= max_phi; i++) {
                phi[i] = Integer.MAX_VALUE;
                for(int j = 0; j <= max_lambda; j++) {
                    if(phi[i] > g[j][i] && g[j][i] != -1) {
                        phi[i] = g[j][i];
                    }
                }
            }

            for(int i = 0; i <= max_lambda; i++) {
                l[i] = Integer.MAX_VALUE;
                for(int j = 0; j <= max_phi; j++) {
                    if(l[i] > g[i][j] && g[i][j] != -1) {
                        l[i] = g[i][j];
                    }
                }
            }


            if(cur_i == U.size()-1) {
                int minG = Integer.MAX_VALUE;
                int gst = 0;
                int gl = 0;
                for(int i = 0; i <= max_lambda; i++) {
                    for(int j = 0; j <= max_phi; j++) {
                        if(g[i][j] < minG) {
                            minG = g[i][j];
                            gst = j;
                            gl = i;
                        }
                    }
                }
                GST[cur_II] = gst;
                GL[cur_II] = gl;
            }

            G.set(cur_II, g);
            Phi.set(cur_II , phi);
            L.set(cur_II, l);

        }

        //обратный проход
        for(int i = U.size()-2; i >= 0; i--) {
            boolean stop = false;
            Node node = map.get(marked.get(i));
            List<Long> parents = node.parents;
            for(Long parent : parents) {
                if(marked.indexOf(parent) > i) {
                    int orig  = nonMarked.indexOf(node.nodeId);
                    int orig2 = nonMarked.indexOf(parent);
                    GL[orig] = GST[orig2];
                    int[][] g = G.get(orig);
                    int lambda = GST[orig2];
                    int minG = Integer.MAX_VALUE;
                    if(node.children.isEmpty()) {
                     GST[orig] = 0;
                        stop = true;
                        break;
                    }
                    for(int j = 0; j <= GL[orig] + node.t; j++) {
                        if(g[lambda][j] < minG) {
                            minG = g[lambda][j];
                            GST[orig] = j;
                        }
                    }
                    stop = true;
                    break;
                }
            }
            if(!stop) {
                List<Long> children = node.children;
                for(Long child : children) {
                    if(marked.indexOf(child) > i) {
                        int orig  = nonMarked.indexOf(node.nodeId);
                        int orig2 = nonMarked.indexOf(child);
                        GST[orig] = GL[orig2];
                        int[][] g = G.get(orig);
                        int phi = GST[orig];
                        int minG = Integer.MAX_VALUE;
                        for(int j = Math.max(0,GST[orig] - node.t); j < Math.min(node.M, g.length); j++) {
                            if(g[j][phi] < minG) {
                                minG = g[j][phi];
                                GL[orig] = j;
                            }
                        }
                    }

                }
            }
        }
        return 0;
    }



    private void addDev(Long endNodeId, double dev, Map<Long, Node> map) {
        Node endNode = map.get(endNodeId);
        List<Long> parents = endNode.parents;
        for(Long parentId : parents) {
            Node parent = map.get(parentId);
            parent.stdDev += dev;
            addDev(parentId, dev, map);
        }
    }

    public int[] getStats(List<ProductWithCategory> pr) {
        int ax = 0, ay = 0, az = 0, bx = 0, by = 0, bz = 0, cx = 0, cy = 0, cz = 0;
        for (ProductWithCategory p : pr) {
            if (p.getCategory().equals("AX")) ax++;
            else if (p.getCategory().equals("AY")) ay++;
            else if (p.getCategory().equals("AZ")) az++;
            else if (p.getCategory().equals("BX")) bx++;
            else if (p.getCategory().equals("BY")) by++;
            else if (p.getCategory().equals("BZ")) bz++;
            else if (p.getCategory().equals("CX")) cx++;
            else if (p.getCategory().equals("CY")) cy++;
            else if (p.getCategory().equals("CZ")) cz++;
        }
        int[] data = {ax, ay, az, bx, by, bz, cx, cy, cz};
        return data;
    }

    public Map<Long, List<ProductOpt>> getProductbyWarehouse(int s, int r) {
        Map<Long, List<ProductOpt>> productsByWarehouse = new HashMap<>();
        List<Warehouse> warehouses = warehouseService.getAll();
        List<ProductWithCategory> products = ABCXYZ(0.8,0.15,0.05,0.15,0.25,0.6);
        for (Warehouse warehouse : warehouses) {
            List<ProductOpt> productOpts = new ArrayList<>();
            productsByWarehouse.put(warehouse.getId(), productOpts);
            for(ProductWithCategory product : products) {
                ProductOpt p = calculateSingle(s,r, product.getId(), warehouse.getId());
                p.setCategory(product.getCategory());
                productOpts.add(p);
            }
        }
        return productsByWarehouse;
    }
}
