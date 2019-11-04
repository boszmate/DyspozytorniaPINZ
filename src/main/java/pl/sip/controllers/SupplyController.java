package pl.sip.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import pl.sip.dto.NewMapPointer;
import pl.sip.dto.SupplyTicket;
import pl.sip.dto.Warehouse;
import pl.sip.services.MapPointerService;
import pl.sip.services.SupplyTicketService;
import pl.sip.utils.SipFunctions;
import pl.sip.utils.SortByDistance;

import javax.validation.Valid;
import java.lang.reflect.Array;
import java.util.*;
import java.util.logging.Logger;
//import java.util.stream.IntStream;

@Controller
public class SupplyController {

    private final SupplyTicketService ticketService;
    private final MapPointerService pointerService;
    private Logger log = Logger.getLogger("SupplyController");

    private final int MAX_TICKETS_PER_DRIVER = 5;

    @Autowired
    public SupplyController(SupplyTicketService ticketService, MapPointerService pointerService) {
        this.ticketService = ticketService;
        this.pointerService = pointerService;
    }

    @GetMapping("/supply")
    public String showSupply(Model model){
        ArrayList<SupplyTicket> supplyTickets = ticketService.showTickets();
        StringBuilder tableFill = new StringBuilder("Obecnie nie mamy zadnych zamowien");

        if( !supplyTickets.isEmpty() ) {
            tableFill = new StringBuilder("<table id='tabela_dostawy'>" +
                    "<tr>" +
                    "<th style=\"width: 10%\">Numer zamowienia</th>" +
                    "<th>Nazwa sklepu" + "</th>" +
                    "<th style='display: none'>Lon</th>" +
                    "<th style='display: none'>Lat</th>" +
                    "<th>Store Id</th>" +
                    "<th>Driver Id</th>" +
                    "<th>Czas trwania</th>" +
                    "<th>Status</th>" +
                    "<th>Oczekiwana data dostawy</th>" +
                    "<th>Numer trasy</th>" +
                    "</tr>\n");
            for (SupplyTicket ticket : supplyTickets) {
                if (!ticket.isCompleted()) {
                    String shopName = ticketService.getShopsName(ticket.getShopId());
                    float shopLon = ticketService.getShopsLon(ticket.getShopId());
                    float shopLat = ticketService.getShopsLat(ticket.getShopId());
                    float storeLon = ticketService.getStoreLon(ticket.getStoreId());
                    float storeLat = ticketService.getStoreLat(ticket.getStoreId());

                    String htmlTag = "<tr><td>" + ticket.getTicketId() + "</td><td>" + shopName +
                            "</td><td style='display: none'>" + shopLon + "</td><td style='display: none'>" + shopLat +
                            "</td><td style='display: none'>" + storeLon + "</td><td style='display: none'>" + storeLat +
                            "</td><td>" + ticket.getStoreId() + "</td><td>"+ ticket.getDriverId() +"</td><td>"+
                            ticket.getDuration() + "</td><td>" + ticket.getTicketStatus() + "</td><td>"+
                            ticket.getDeliveryDate() + "</td><td>" + ticket.getPath() + "</td></tr>\n";
                    tableFill.append(htmlTag);
                }
            }
            tableFill.append("</table>");
        }

        model.addAttribute("deliveryTicketFill", tableFill.toString());

        return "supply";
    }

    @RequestMapping(value = "/acceptDelivery", method = RequestMethod.GET)
    public String acceptDelivery(Model model){
        ArrayList<SupplyTicket> supplyTickets = ticketService.showTickets();

        /*for (SupplyTicket ticket: supplyTickets){
            if(ticket.getTicketStatus().equals("oczekujace")){
                ticket.setTicketStatus("w realizacji");

                log.info("Przed wyznaczeniem trasy");
                //TODO: use new distance algorithm instead of this
                createTicketDataNaiveAlgorithm(ticket);
                log.info("Po wyznaczeniu trasy");
            }
        }*/

        //create Distance map per not-started ticket
        log.info("Przed wyznaczeniem trasy");
        createTicketNewWay(supplyTickets);
        log.info("Po wyznaczeniu trasy");

        return showSupply(model);
    }

    private void createTicketNewWay(ArrayList<SupplyTicket> supplyTickets){

        //struktura wygląda następująco:        Map< STORE_ID, Map< DISTANCE, Map< TICKET_ID_LIST, SHOP_ID_LIST( == PATH) >>>
        Map<Integer, Map<Double, Map<ArrayList<Integer>, ArrayList<Integer>>>> finalDistanceMap = new HashMap<>();

        List<NewMapPointer>allWarehouses = pointerService.showStoreTable();

        ArrayList<SupplyTicket> nextFewTickets = new ArrayList<>();

        int ticketCounter = 0;
        for(SupplyTicket ticket: supplyTickets) {
            log.info(ticket.toString());
            if (!ticket.getTicketStatus().equals("oczekujace"))
                continue;
            if (ticketCounter == MAX_TICKETS_PER_DRIVER)
                break;
            nextFewTickets.add(ticket);
            ticketCounter++;
        }

        log.info("" + nextFewTickets.size());

        Map<Integer, Map<Double, Map<ArrayList<Integer>, ArrayList<Integer>>>> distanceMap = new HashMap<>();
        for (NewMapPointer warehouse : allWarehouses) {
            log.info(warehouse.toString());
            //map of <Distance, <[ticketIds] [path]>>
            Map<Double, Map<ArrayList<Integer>, ArrayList<Integer>>> distance_path_map;
            distance_path_map = calculateShortestDistanceAndPath(nextFewTickets, warehouse);

            distanceMap.put(warehouse.getPointId(), distance_path_map);
        }

        double shortestDistance = 10000;
        for(Map.Entry<Integer, Map<Double, Map<ArrayList<Integer>, ArrayList<Integer>>>> shortestPathPerWarehouse: distanceMap.entrySet()){
            Integer warehouseId = shortestPathPerWarehouse.getKey();
            Map<Double, Map<ArrayList<Integer>, ArrayList<Integer>>> distance_path_map = shortestPathPerWarehouse.getValue();
            double distance = distance_path_map.entrySet().iterator().next().getKey();
            if(distance < shortestDistance){
                shortestDistance = distance;
                finalDistanceMap = new HashMap<>();
                finalDistanceMap.put(warehouseId, shortestPathPerWarehouse.getValue());
            }
        }

        //TODO: [Hubert] ????? XDD
        Map<ArrayList<Integer>, ArrayList<Integer>> sicketId_and_path =
                finalDistanceMap.entrySet().iterator().next().getValue().entrySet().iterator().next().getValue();

        ArrayList<Integer> ticketIdList = sicketId_and_path.entrySet().iterator().next().getKey();
        ArrayList<Integer> pathList = sicketId_and_path.entrySet().iterator().next().getValue();
        int pathId = -1;
        //TODO: checkAvailableDrivers based on calculated duration
        for(int n = 0; n < MAX_TICKETS_PER_DRIVER; n++){
            if(n == ticketCounter)
                break;
            Integer shopId = pathList.get(n);
            Integer ticketId = ticketIdList.get(n);
            SupplyTicket ticket = new SupplyTicket();
            ticket.setShopId(shopId);
            ticket.setTicketId(ticketId);
            ticket.setShopName(ticketService.getShopsName(shopId));
            ticket.setTicketStatus("w realizacji");
            //to powinno brać odległość
            double distance = finalDistanceMap.entrySet().iterator().next().getValue().entrySet().iterator().next().getKey();
            ticket.setDuration(SipFunctions.calculateDuration(distance));
            ticket.setDistance(distance);
            ticket.setDriverId(1);
            ticket.setStoreId(finalDistanceMap.entrySet().iterator().next().getKey());
            ticket.setDeliveryDate("date");
            //TODO: Można tutaj dodać jeszcze pole do tabeli z trasą - trasa znajduje się w zmiennej `pathList`
            if(n==0)
                pathId = ticketId;
            ticket.setPath(pathId);
            log.info("Updating: ");
            log.info(ticket.toString());
            ticketService.createTicketNew(ticket);
        }
    }

    private Map<Double, Map<ArrayList<Integer>, ArrayList<Integer>>> calculateShortestDistanceAndPath(ArrayList<SupplyTicket> tickets,
                                                                              NewMapPointer warehouse){
        Map<Double, Map<ArrayList<Integer>, ArrayList<Integer>>> distance_path_map = new HashMap<>();

        ArrayList<Integer> shortestDeliveryPath = new ArrayList<>();
        ArrayList<Integer> ticketIds = new ArrayList<>();
        double shortestDistance = 1000;
        if (tickets.size() == 1){
            ticketIds.add(tickets.get(0).getTicketId());
            ArrayList<Integer> deliveryPath = new ArrayList<>();

            String shopName = ticketService.getShopsName(tickets.get(0).getShopId());
            NewMapPointer shop = pointerService.getPointerByName(shopName);
            double distance = SipFunctions.calculateDistanceInStraightLine(shop, warehouse);
            shortestDistance = distance;
            deliveryPath.add(shop.getPointId());

            shortestDeliveryPath = deliveryPath;
        }
        else {
            for (SupplyTicket ticket : tickets) {
                ticketIds.add(ticket.getTicketId());

                ArrayList<Integer> deliveryPath = new ArrayList<>();

                String shopName = ticketService.getShopsName(ticket.getShopId());
                NewMapPointer shop1 = pointerService.getPointerByName(shopName);

                double distance = SipFunctions.calculateDistanceInStraightLine(warehouse, shop1);
                deliveryPath.add(shop1.getPointId());

                ArrayList<SupplyTicket> otherTickets = new ArrayList<>();
                for (SupplyTicket otherTicket : tickets) {
                    if (otherTicket != ticket) {
                        otherTickets.add(otherTicket);
                    }
                }
                while (otherTickets.size() > 1) {
                    Map<NewMapPointer, Double> bestPath = getBestPath(shop1, otherTickets);
                    double closer = bestPath.entrySet().iterator().next().getValue();
                    NewMapPointer otherShop = bestPath.entrySet().iterator().next().getKey();

                    deliveryPath.add(otherShop.getPointId());
                    distance += closer;
                }
                String lastShopName = ticketService.getShopsName(otherTickets.get(0).getShopId());
                NewMapPointer lastShop = pointerService.getPointerByName(lastShopName);

                deliveryPath.add(lastShop.getPointId());
                distance += SipFunctions.calculateDistanceInStraightLine(lastShop, warehouse);

                if (distance < shortestDistance) {
                    shortestDistance = distance;
                    shortestDeliveryPath = deliveryPath;
                }
            }
        }
        Map<ArrayList<Integer>, ArrayList<Integer>> tickets_and_path = new HashMap<>();
        tickets_and_path.put(ticketIds, shortestDeliveryPath);

        distance_path_map.put(shortestDistance, tickets_and_path);

        return distance_path_map;

    }

    private HashMap<NewMapPointer, Double> getBestPath(NewMapPointer shop, ArrayList<SupplyTicket> otherTickets){
        HashMap<NewMapPointer, Double> bestPath = new HashMap<>();
        double bestDistance = 10000000;
        NewMapPointer bestShop = null;
        SupplyTicket bestTicket = null;

        log.info(";S");
        log.info("" + otherTickets.size());
        for (SupplyTicket otherTicket: otherTickets){
            String otherShopName = ticketService.getShopsName(otherTicket.getShopId());
            log.info(otherShopName);
            NewMapPointer otherShop = pointerService.getPointerByName(otherShopName);
            double distance = SipFunctions.calculateDistanceInStraightLine(shop, otherShop);
            log.info("" + distance);
            log.info("best distance: " + bestDistance);

            if(distance < bestDistance){
                bestDistance = distance;
                bestShop = otherShop;
                bestTicket = otherTicket;
            }
            log.info("new best shop: " + bestShop);
        }
        otherTickets.remove(bestTicket);
        bestPath.put(bestShop, bestDistance);
        return bestPath;
    }

    private void createTicketDataNaiveAlgorithm(SupplyTicket ticket){
        String date = ticket.getDeliveryDate().split(" ")[0];
        String hour = ticket.getDeliveryDate().split(" ")[1];

        ArrayList<NewMapPointer> warehouses = pointerService.showStoreTable();

        ticket.setShopName(ticketService.getShopsName(ticket.getShopId())); // TODO: [Hubert] na Boga, zmień to ._. // [Hubert] zmienię jak będzie czas
        log.info(ticket.toString());
        NewMapPointer whereToDeliver = pointerService.getPointerByName(ticket.getShopName());
        boolean isDriverAlreadyPicked = false;

        while(!isDriverAlreadyPicked) {
            List<Warehouse>calculatedWarehouses = calculateWarehousesByTime(warehouses, date, hour, whereToDeliver);
            if(!calculatedWarehouses.isEmpty()) {
                Collections.sort(calculatedWarehouses, new SortByDistance());
                for (Warehouse store : calculatedWarehouses) {
                    if (store.getAvailableDrivers() > 0) {
                        ticket.setDriverId(store.getDriverId());
                        ticket.setDistance(store.getDistance());
                        ticket.setStoreId(store.getStoreId());
                        ticket.setDuration(SipFunctions.calculateDuration(store.getDistance()));
                        ticket.setDeliveryDate(date + " " + hour);
                        ticket.setTicketStatus("w realizacji");
                        isDriverAlreadyPicked = true;
                        break;
                    }
                }
            }
            String newDate = SipFunctions.tryNextHour(date, hour);
            date = newDate.split(" ")[0];
            hour = newDate.split(" ")[1];
        }

        ticketService.createTicketNaive(ticket);
    }

    @RequestMapping(value = "/supplyDeliveryRequest", method = RequestMethod.GET)
    public String mapPointerRegister(Model model){
        ArrayList<NewMapPointer> shopList = pointerService.showShopTable();
        StringBuilder shopOptions = new StringBuilder();
        StringBuilder shopDay = new StringBuilder();
        StringBuilder shopMonth = new StringBuilder();
        StringBuilder shopYear = new StringBuilder();
        StringBuilder shopHour = new StringBuilder();
        StringBuilder shopMinute = new StringBuilder();

        for(NewMapPointer shop: shopList){
            String htmlTag = "<option>" + shop.getPointName() + "</option>";
            shopOptions.append(htmlTag);
        }

        for (int i=1; i<=60; i++){
            if (i<= 12){
                shopMonth.append("<option>").append(i).append("</option>");
            }
            if(i<=31){
                shopDay.append("<option>").append(i).append("</option>");
            }
            if(i >= 8 && i< 16){
                shopHour.append("<option>").append(i).append("</option>");
            }
            if(i<=10) {
                shopMinute.append("<option>").append("0").append(i - 1).append("</option>");
            }
            else
                shopMinute.append("<option>").append(i - 1).append("</option>");
            shopYear.append("<option>").append(2018 + i).append("</option>");
        }


        model.addAttribute("supplyDeliveryRequestForm", new SupplyTicket());
        model.addAttribute("shopIdOptions", shopOptions.toString());
        model.addAttribute("shopDay", shopDay.toString());
        model.addAttribute("shopMonth", shopMonth.toString());
        model.addAttribute("shopYear", shopYear.toString());
        model.addAttribute("shopHour", shopHour.toString());
        model.addAttribute("shopMinute", shopMinute.toString());

        return "supplyDeliveryRequest";
    }

    @RequestMapping(value = "/supplyDeliveryRequest", method = RequestMethod.POST)
    public String checkMapPointerRegister(@ModelAttribute("supplyDeliveryRequestForm") @Valid SupplyTicket ticket,
                                          BindingResult result,
                                          Model model){

        if(result.hasErrors()){
            model.addAttribute("error_msg", "Wrong credentials!");
            return "home";
        }
        else{
            ticketService.createTicketEntry(ticket);
            return "redirect:/supply";
        }
    }

    private ArrayList<Warehouse> calculateWarehousesByTime(ArrayList<NewMapPointer> warehouses,
                                                           String date,
                                                           String hour,
                                                           NewMapPointer whereToDeliver){
        ArrayList<Warehouse>calculatedWarehouses = new ArrayList<>();
        for(NewMapPointer store: warehouses){
            //function picks first suitable
            double distance = SipFunctions.calculateDistanceInStraightLine(whereToDeliver, store);
            int availableDrivers = checkAvailableDrivers(store.getPointId(), distance, date, hour);
            if (availableDrivers != 0) {

                Warehouse calculatedStore = new Warehouse();
                calculatedStore.setAvailableDrivers(availableDrivers);
                calculatedStore.setStoreId(store.getPointId());
                calculatedStore.setDistance(distance);
                calculatedStore.setDriverId(availableDrivers);

                calculatedWarehouses.add(calculatedStore);
            }
        }
        return calculatedWarehouses;
    }

    private int checkAvailableDrivers(int storeId, double distance, String deliveryDate, String deliveryHour){
        int[] drivers = ticketService.getDriversByStoreId(storeId);
        double deliveryDuration = SipFunctions.calculateDuration(distance);
        ArrayList<SupplyTicket> driversTickets = ticketService.getTicketsByDrivers(drivers);
        for(int driver: drivers){
            boolean ifAvailableForDelivery = checkAvailability(driver, driversTickets, deliveryDate, deliveryHour, deliveryDuration);
            if(ifAvailableForDelivery){
                return driver;
            }
        }

        return 0;
    }

    private boolean checkAvailability(int driverId,
                                             ArrayList<SupplyTicket> driversTickets,
                                             String deliveryDate,
                                             String deliveryHour,
                                             double deliveryDuration) {
        for(SupplyTicket ticket: driversTickets){
            if (ticket.getDriverId() == driverId){
                String ticketFullDate = ticket.getDeliveryDate().split(" ")[0];
                String ticketFullTime = ticket.getDeliveryDate().split(" ")[1];
                if (ticketFullDate.equals(deliveryDate)){
                    String[] deliveryFullTime = deliveryHour.split((":"));
                    String[] ticketTime = ticketFullTime.split((":"));
                    int deliveryHourInt = Integer.parseInt(deliveryFullTime[0]);
                    int deliveryMinuteInt = Integer.parseInt(deliveryFullTime[1]);
                    int ticketHourInt = Integer.parseInt(ticketTime[0]);
                    int ticketMinuteInt = Integer.parseInt(ticketTime[1]);

                    //in minutes
                    int difference = 60 * (Math.abs(deliveryHourInt - ticketHourInt)) +
                            (Math.abs(deliveryMinuteInt - ticketMinuteInt));
                    if((difference - deliveryDuration) - ticket.getDuration() < 0){
                        return false;
                    }
                }
            }
        }
        return true;
    }
}