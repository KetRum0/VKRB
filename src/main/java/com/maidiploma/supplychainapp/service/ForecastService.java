package com.maidiploma.supplychainapp.service;

import com.github.signaflo.timeseries.TestData;
import com.github.signaflo.timeseries.TimeSeries;
import com.github.signaflo.timeseries.forecast.Forecast;
import com.github.signaflo.timeseries.model.arima.Arima;
import com.github.signaflo.timeseries.model.arima.ArimaOrder;
import com.maidiploma.supplychainapp.model.*;
import com.maidiploma.supplychainapp.repository.ProductRepository;
import com.maidiploma.supplychainapp.repository.ShipmentRepository;
import com.maidiploma.supplychainapp.repository.ShipmentsProductsRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ForecastService {
    @Autowired
    private ShipmentsProductsRepository shipmentsProductsRepository;

    @Autowired
    private SettingsService settingsService;

    public HistoricalData getBestModelResult(Long productId, Long warehouseId, Integer days) {
        HistoricalData hd = this.getHistoricalDemand(productId, warehouseId, settingsService.getStart(), settingsService.getEnd());
        List<Integer> d = hd.getQuantities();
        int size = d.size();
        List<Integer> predict = new ArrayList<>();
        int train_n = (int) (size * 0.7);
        int test_n = size - train_n;

        List<Integer> train = new ArrayList<>(d.subList(0, train_n));
        List<Integer> test = new ArrayList<>(d.subList(train_n, size));

        predict = calcMA(d, 1, test_n);
        double bestError = calculateMAE(predict, test);
        int choose = 0;
        double bestAlpha = 0.0;
        double bestBeta = 0.0;
        double besty = 0.0;
        double bestphi = 0.0;

        for (double aa = 0.05; aa <= 0.5; aa += 0.05) {
            predict = calcSES(train, aa, test_n);
            predict = predict.subList(train_n, size);
            double error = calculateMAE(predict, test);
            if (error < bestError) {
                bestError = error;
                bestAlpha = aa;
                choose = 1;
            }
        }

        for (double aa = 0.05; aa <= 0.5; aa += 0.05) {
            for (double bb = 0.05; bb <= 0.5; bb += 0.05) {
                for (double pphi = 0.05; pphi <= 1.0; pphi += 0.05) {
                    predict = calcDES(train, test_n, aa, bb,pphi);
                    predict = predict.subList(train_n, size);
                    double error = calculateMAE(predict, test);
                    if (error < bestError) {
                        bestError = error;
                        bestAlpha = aa;
                        bestBeta = bb;
                        bestphi = pphi;
                        choose = 2;
                    }
                }

            }
        }

        for (double aa = 0.05; aa <= 0.5; aa += 0.05) {
            for (double bb = 0.05; bb <= 0.5; bb += 0.05) {
                for (double yy = 0.05; yy <= 0.3; yy += 0.05) {
                    for (double pphi = 0.05; pphi < 1.0; pphi += 0.05) {
                        predict = calcTES(train, test_n, aa, bb, yy, pphi);
                        predict = predict.subList(train_n, size);
                        double error = calculateMAE(predict, test);
                        if (error < bestError) {
                            bestError = error;
                            bestAlpha = aa;
                            bestBeta = bb;
                            besty = yy;
                            bestphi = 0.3;
                            choose = 3;
                        }
                    }
                    }
                }
            }

            System.out.println("bestAlpha = " + bestAlpha + ", bestBeta = " + bestBeta + ", besty = " + besty + ", choose = " + choose + " error = " + bestError);

            List<LocalDate> dates = new ArrayList<>(hd.getDates());
            for (int i = 0; i < days; i++) {
                dates.add(dates.getLast().plusDays(1));
            }

            if (choose == 0) {
                predict = MA(productId, warehouseId, days, d.size() - 1).getQuantities();
            } else if (choose == 1) {
                predict = SES(productId, warehouseId, days, bestAlpha).getQuantities();
            } else if (choose == 2) {
                predict = DES(productId, warehouseId, days, bestAlpha, bestBeta).getQuantities();
            } else if (choose == 3) {
                predict = TES(productId, warehouseId, days, bestAlpha, bestBeta, besty).getQuantities();
            } else predict = TES(productId, warehouseId, days, bestAlpha, bestBeta, besty).getQuantities();

            HistoricalData result = new HistoricalData();
            result.setDates(dates);
            result.setQuantities(predict);
            return result;

        }

    public HistoricalData getHistoricalDemand(Long productId, Long warehouseId, LocalDate startDate, LocalDate endDate) {
        List<ShipmentsProducts> shipments = shipmentsProductsRepository
                .findById_Shipment_Warehouse_IdAndId_Product_Id(warehouseId, productId);

        HistoricalData historicalData = new HistoricalData();

        for (ShipmentsProducts shipmentProduct : shipments) {
            if (!shipmentProduct.getId().getShipment().getShipmentDate().isBefore(startDate) &&
                    !shipmentProduct.getId().getShipment().getShipmentDate().isAfter(endDate)) {

                historicalData.addEntry(
                        shipmentProduct.getId().getShipment().getShipmentDate(),
                        shipmentProduct.getQuantity()
                );
            }
        }

        return historicalData;
    }


    public HistoricalData MA(Long productId, Long warehouseId, Integer days, Integer period) {

        HistoricalData hd = this.getHistoricalDemand(productId, warehouseId, settingsService.getStart(), settingsService.getEnd());
        List<Integer> d = hd.getQuantities();
        int size = d.size();
        List<Integer> predict = new ArrayList<>(size);

        if (period == null) {
            int bestPeriod = days-1;
            period = bestPeriod;
        }

        List<LocalDate> dates = new ArrayList<>(hd.getDates());
        for (int i = 0; i < days; i++) {
            dates.add(dates.getLast().plusDays(1));
        }

        int sum = 0;
        List<Integer> window = new ArrayList<>();
        for(int i = 0; i < period; i++) {
            predict.add(null);
            window.add(d.get(i));
            sum += d.get(i);

        }
        predict.add(sum / period);

        for (int i = period; i < d.size(); i++) {
            sum -= window.removeFirst();
            sum += d.get(i);
            window.add(d.get(i));
            predict.add(sum / period);
        }

        predict.addAll(calcMA(d, period, days));
        for (int i = 0; i < predict.size() - days-1; i++) {
            predict.set(i, null);
        }

        HistoricalData result = new HistoricalData();
        result.setDates(dates);
        result.setQuantities(predict);

        return result;
    }

    private List<Integer> calcMA(List<Integer> d, int period, int days) {
        int sum = 0;
        for (int i = d.size() - period; i < d.size(); i++) {
            sum += d.get(i);
        }
        int res = sum / period;
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            result.add(res);
        }
        return result;
    }

    private double calculateMAE(List<Integer> pred, List<Integer> real) {
        if (pred.size() != real.size()) {
            System.out.println("ERROR");
            return Double.MAX_VALUE;
        }
        double error = 0.0;
        for (int i = 0; i < pred.size(); i++) {
            error += Math.abs(pred.get(i) - real.get(i));
        }
        return error / pred.size();
    }


    public HistoricalData SES(Long productId, Long warehouseId, int days, Double alpha) {
        HistoricalData hd = this.getHistoricalDemand(productId, warehouseId, settingsService.getStart(), settingsService.getEnd());
        List<Integer> d = hd.getQuantities();
        int size = d.size();
        List<Integer> predict = new ArrayList<>();

        System.out.println(d);

        if (alpha == null) {
            System.out.println("a is null");

            int train_n = (int) (size * 0.7);
            int test_n = size - train_n;

            List<Integer> train = new ArrayList<>(d.subList(0, train_n));
            List<Integer> test = new ArrayList<>(d.subList(train_n, size));

            double bestError = Double.MAX_VALUE;
            double bestAlpha = 0.5;

            for (double aa = 0.05; aa < 0.5; aa+=0.01) {
                predict = calcSES(train, aa, test_n);
                predict = predict.subList(train_n, size);
                double error = calculateMAE(predict,test);
                if (error < bestError) {
                    bestError = error;
                    bestAlpha = aa;
                }
            }
            //System.out.println("Best alpha: " + bestAlpha + ", Best MAE on train: " + bestError);
            alpha = bestAlpha;
        }

        List<LocalDate> dates = new ArrayList<>(hd.getDates());
        for (int i = 0; i < days; i++) {
            dates.add(dates.getLast().plusDays(1));
        }

        predict  = calcSES(d, alpha, days);

        for (int i = 0; i < predict.size() - days-1; i++) {
            predict.set(i, null);
        }

        HistoricalData result = new HistoricalData();
        result.setDates(dates);
        result.setQuantities(predict);
        return result;
    }

    private List<Integer> calcSES(List<Integer> d, double alpha, int days) {
        List<Integer> predict = new ArrayList<>();
        predict.add(d.get(0));
        for (int i = 0; i < d.size() + days; i++) {
            if(i >= d.size()) predict.add(predict.getLast());
            else {
                int x = (int) (alpha * d.get(i) + (1-alpha) * predict.get(i));
                predict.add(x);
            }
        }
        return predict;
    }

    public HistoricalData DES(Long productId, Long warehouseId, int days, Double alpha, Double beta) {
        HistoricalData hd = this.getHistoricalDemand(productId, warehouseId, settingsService.getStart(), settingsService.getEnd());
        List<Integer> d = hd.getQuantities();
        int size = d.size();
        List<Integer> predict = new ArrayList<>();
    double phi = 0.0;
        if (alpha == null && beta == null) {
            int train_n = (int) (size * 0.7);
            int test_n = size - train_n;

            List<Integer> train = new ArrayList<>(d.subList(0, train_n));
            List<Integer> test = new ArrayList<>(d.subList(train_n, size));
            double bestAlpha = 0.5;
            double bestBeta = 0.5;
            double bestError = Double.MAX_VALUE;
            double bestphi = 0.0;

            for (double aa = 0.05; aa <= 0.5; aa+=0.01) {
                for (double bb = 0.05; bb <= 0.5; bb+=0.01) {
                    for(double pphi = 0.05; pphi <= 1.0; pphi+=0.01) {
                        predict = calcDES(train, test_n, aa, bb, pphi);
                        predict = predict.subList(train_n, size);
                        double error = calculateMAE(predict,test);
                        if (error < bestError) {
                            bestError = error;
                            bestAlpha = aa;
                            bestBeta = bb;
                            bestphi = pphi;
                        }
                    }

                }
            }

            System.out.println("Best alpha: " + bestAlpha + " nbeta " + bestBeta + "besetphi " + bestphi + ", Best MAE on train: " + bestError);
            phi = bestphi;
            alpha = bestAlpha;
            beta = bestBeta;
        }

        List<LocalDate> dates = new ArrayList<>(hd.getDates());
        for (int i = 0; i < days; i++) {
            dates.add(dates.getLast().plusDays(1));
        }

        predict = calcDES(d,days,alpha,beta,phi);

        for (int i = 0; i < predict.size() - days; i++) {
            predict.set(i, null);
        }

        HistoricalData result = new HistoricalData();
        result.setDates(dates);
        result.setQuantities(predict);
        return result;
    }

    private List<Integer> calcDES(List<Integer> d, int days, Double alpha, Double beta, Double phi) {
        List<Integer> predict = new ArrayList<>();
        List<Double> A = new ArrayList<>();
        List<Double> B = new ArrayList<>();
        A.add(Double.valueOf(d.get(0)));
        B.add((double) (d.get(1) - d.get(0)));
        predict.add(d.get(0));

        for (int i = 1; i < d.size() + days; i++) {
            if(i >= d.size()){
                int x = (int) (A.get(i-1) + phi*B.get(i-1));
                predict.add(x);
                A.add(Double.valueOf(predict.get(i)));
                B.add(phi*B.get(i-1));
            }
            else {
                int x = (int) (A.get(i-1) + phi*B.get(i-1));
                predict.add(x);
                A.add(alpha*d.get(i) + (1-alpha)*(A.get(i-1) + phi*B.get(i-1)));
                B.add(beta*(A.get(i)-A.get(i-1)) + (1-beta)*phi*B.get(i-1));
            }
        }
        return predict;
    }

    public HistoricalData TES(Long productId, Long warehouseId, int days, Double alpha, Double beta, Double y) {
        HistoricalData hd = this.getHistoricalDemand(productId, warehouseId, settingsService.getStart(), settingsService.getEnd());
        List<Integer> d = hd.getQuantities();
        int size = d.size();
        List<Integer> predict = new ArrayList<>();
        double phi = 0.5;

        if (alpha == null) {
            int train_n = (int) (size * 0.7);
            int test_n = size - train_n;
            List<Integer> train = new ArrayList<>(d.subList(0, train_n));
            List<Integer> test = new ArrayList<>(d.subList(train_n, size));
            double bestAlpha = 0.5;
            double bestBeta = 0.5;
            double besty = 0.5;
            double bestphi = 0.5;
            double bestError = Double.MAX_VALUE;

            for (double aa = 0.05; aa < 0.5; aa+=0.05) {
                for (double bb = 0.05; bb < 0.5; bb+=0.05) {
                    for (double yy = 0.05;yy < 0.3;yy+=0.05) {
                        for (double pphi = 0;pphi <= 1.0 ;pphi+=0.05) {

                            predict = calcTES(train, test_n, aa, bb, yy,pphi);
                            predict = predict.subList(train_n, size);
                            double error = calculateMAE(predict,test);

                            if (error <= bestError) {
                                bestError = error;
                                bestAlpha = aa;
                                bestBeta = bb;
                                besty = yy;
                                bestphi = pphi;
                        }

                        }
                    }
                }
            }

//            System.out.println("Best alpha: " + bestAlpha + " nbeta " + bestBeta + " y " + besty + " phi " + bestphi + ", Best MAE on train: " + bestError);
//            System.out.println(predict);
//            System.out.println(test);
            alpha = bestAlpha;
            beta = bestBeta;
            phi = bestphi;
            y = besty;
        }
        List<LocalDate> dates = new ArrayList<>(hd.getDates());
        for (int i = 0; i < days; i++) {
            dates.add(dates.getLast().plusDays(1));
        }
        predict = calcTES(d,days,alpha,beta,y,phi);


        for (int i = 0; i < predict.size() - days; i++) {
            predict.set(i, null);
        }

        HistoricalData result = new HistoricalData();
        result.setDates(dates);
        result.setQuantities(predict);
        return result;
    }

    private List<Integer> calcTES(List<Integer> d, int days, double alpha, double beta, double y, double phi) {
        List<Integer> predict = new ArrayList<>();

        List<Double> A = new ArrayList<>();
        List<Double> B = new ArrayList<>();
        A.add(Double.valueOf(d.get(0)));
        B.add((double) (d.get(1) - d.get(0)));
        predict.add(d.get(0));
        List<Double>s =new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            double sum = 0.0;
            int count = 0;

            for (int j = i; j < d.size(); j += 7) {
                sum += d.get(j);
                count++;
            }

            s.add( sum / count);
        }

        double mean = 0.0;
        for (int i = 0; i < 7; i++) {
            mean += s.get(i);
        }
        mean /= 7;

        for (int i = 0; i < 7; i++) {
            s.set(i,s.get(i) / mean);
        }

        A.add(d.get(0)/s.get(0));
        B.add(d.get(1)/s.get(1)-d.get(0)/s.get(0));

        for (int i = 1; i < 7; i++) {
            int x = (int) ((int) (A.get(i-1) + phi * B.get(i-1)) * s.get(i));
            predict.add(x);
            A.add(alpha*d.get(i)/s.get(i) + (1-alpha)*(A.get(i-1) + phi*B.get(i-1)));
            B.add(beta*(A.get(i)-A.get(i-1)) + (1-beta)*phi*B.get(i-1));
        }

        for (int i = 7; i < d.size(); i++) {
            int x = (int) ((int) (A.get(i-1) + phi * B.get(i-1)) * s.get(i-7));
            predict.add(x);
            A.add(alpha*d.get(i)/s.get(i-7) + (1-alpha)*(A.get(i-1) + phi*B.get(i-1)));
            B.add(beta*(A.get(i)-A.get(i-1)) + (1-beta)*phi*B.get(i-1));
            s.add( y*d.get(i)/A.get(i) + (1-y)*s.get(i-7));

        }
        for (int i = d.size(); i < d.size()+days; i++) {
            int x = (int) ((int) (A.get(i-1) + phi * B.get(i-1)) * s.get(i-7));
            predict.add(x);
            A.add(Double.valueOf(predict.get(i))/s.get(i-7));
            B.add(phi*B.get(i-1));
            s.add(s.get(i-7));
        }
        return predict;
    }

}
