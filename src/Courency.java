import org.jdatepicker.impl.JDatePickerImpl;
import javax.swing.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

class Courency extends Thread{
    static double balance;
    String      name;
    MainFrame   mainFrame;
    int         digitsAfterZero;
    public boolean emulateFlag;
    double  spred,
            swapBuy,
            swapSell,
            result,
            rateUSD,
            ordersVolume;

    ArrayList<String>       paramArr = new ArrayList<>();
    TreeMap<Date,Rate1D>    rates1D = new TreeMap<>(),
                            rates1D7 = new TreeMap<>(),
                            rates1D13 = new TreeMap<>(),
                            rates1D23 = new TreeMap<>();
    TreeMap<Date,Signal>    signalMap = new TreeMap<>();
    TreeMap<Date,Double>    dayTree = new TreeMap<>(),
                            monthTree = new TreeMap<>(),
                            yearTree = new TreeMap<>(),
                            dateTimeTree = new TreeMap<>();
    TreeSet<Order>          orders = new TreeSet<>();

    Courency(MainFrame mf, String nm) {
        this.mainFrame = mf;
        this.name = nm.trim();
        for (JCheckBox cb : mainFrame.arrCheck){
            if (cb.isSelected()){
                this.ordersVolume += 200;
            }
        }
        readSettingsAndRatesFiles();
    }

    //private methods
    private void readSettingsAndRatesFiles(){
        try {
            Scanner scanner;
            SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm");
            String dirRatesName = "C:\\DATA\\Java\\Projects\\ForexGUI\\Rates\\";
            String[] arrStr;
            Date day, bufDate;

            //**************************************
            //reading settings for all courencys
            scanner = new Scanner(new File("settings.ini"));
            while (scanner.hasNextLine()) {
                arrStr = scanner.nextLine().split(";");
                if (arrStr[0].substring(0, arrStr[0].indexOf("1440")).equals(name)) {
                    this.digitsAfterZero = Integer.parseInt(arrStr[1]);
                    this.spred = Double.parseDouble(arrStr[2]) / Math.pow(10, digitsAfterZero);
                    this.swapBuy = Double.parseDouble(arrStr[3]) / Math.pow(10, digitsAfterZero);
                    this.swapSell = Double.parseDouble(arrStr[4]) / Math.pow(10, digitsAfterZero);
                    this.rateUSD = Double.parseDouble(arrStr[5]);
                    break;
                }
            }

            //**************************************
            //reading 1Day reates + middle range
            day = null;
            double lastClose = 0.0;
            Date lastDay = null;
            scanner = new Scanner(new File(dirRatesName + name + "1440.csv"));

            ArrayDeque<Double> upDeque = new ArrayDeque<>();
            ArrayDeque<Double> downDeque = new ArrayDeque<>();
            int daysRange = 15;

            Date startPeriod2 = getDate(mainFrame.arrPeriodDate.get(0));
            Date firstDate = null;

            Date startPeriod = addDate(startPeriod2, -30);
            Date endPeriod = getDate(mainFrame.arrPeriodDate.get(1));
            Double up = 0.0, mid = 0.0, down = 0.0;

            while (scanner.hasNextLine()) {
                arrStr = scanner.nextLine().split(",");
                day = format.parse(arrStr[0] + " " + arrStr[1]);
                //System.out.println(getDateTime(day,1));
                if ((day.compareTo(startPeriod) >= 0) && (day.compareTo(endPeriod) <= 0)) {
                    rates1D.put(day, new Rate1D(
                            Double.parseDouble(arrStr[2]),
                            Double.parseDouble(arrStr[3]),
                            Double.parseDouble(arrStr[4]),
                            Double.parseDouble(arrStr[5]),
                            up,
                            mid,
                            down)
                    );
                    if ((firstDate == null) && (day.compareTo(startPeriod2) >= 0)) {
                        firstDate = day;
                        mainFrame.arrPeriodDate.get(0).getJFormattedTextField().setText(
                                new SimpleDateFormat("dd.MM.yyyy").format(firstDate));
                    }
                }

                if (upDeque.size() == daysRange) {
                    up = (up * daysRange - upDeque.pollFirst() + Double.parseDouble(arrStr[3])) / daysRange;
                    down = (down * daysRange - downDeque.pollFirst() + Double.parseDouble(arrStr[4])) / daysRange;
                    mid = round(down + (up - down) / 2);
                } else {
                    up = (up * upDeque.size() + Double.parseDouble(arrStr[3])) / (upDeque.size() + 1);
                    down = (down * downDeque.size() + Double.parseDouble(arrStr[4])) / (downDeque.size() + 1);
                    ;
                }
                upDeque.add(Double.parseDouble(arrStr[3]));
                downDeque.add(Double.parseDouble(arrStr[4]));

                if (day.compareTo(endPeriod) > 0) {
                    lastDay = day;
                    break;
                }
                lastClose = Double.parseDouble(arrStr[5]);
            }

            if (lastDay == null) {  //for last signal
                rates1D.put(new Date(day.getTime() + 86_400_000), new Rate1D(lastClose, 0.0, 0.0, 0.0,
                        up, mid, down));
            } else {
                rates1D.put(lastDay, new Rate1D(lastClose, 0.0, 0.0, 0.0,
                        up, mid, down));
            }

            //**************************************
            //reading 1Hour reates
            Date prevDate = null;
            scanner = new Scanner(new File(dirRatesName + name + "60.csv"));
            ArrayList<String> file60StrArr = new ArrayList<>();
            while (scanner.hasNextLine()) {
                String bufStr = scanner.nextLine();
                arrStr = bufStr.split(",");
                day = format.parse(arrStr[0] + " 00:00");
                bufDate = format.parse(arrStr[0] + " " + arrStr[1]);
                if (rates1D.containsKey(day)) {
                    //rate from 00:00 to 23:00
                    if (name.equals("GOLD") && (arrStr[1].equals("01:00")) && (prevDate != null) &&
                            (!getDateTime(prevDate, 1).substring(11).equals("00:00"))) {
                        rates1D.get(day).addRate(
                                "1H",
                                day,
                                Double.parseDouble(arrStr[2]),
                                Double.parseDouble(arrStr[3]),
                                Double.parseDouble(arrStr[4]),
                                Double.parseDouble(arrStr[5])
                        );
                    } else {
                        rates1D.get(day).addRate(
                                "1H",
                                bufDate,
                                Double.parseDouble(arrStr[2]),
                                Double.parseDouble(arrStr[3]),
                                Double.parseDouble(arrStr[4]),
                                Double.parseDouble(arrStr[5])
                        );
                    }
                    file60StrArr.add(bufStr);
                }
                prevDate = bufDate;
            }

            class innerParser {
                TreeMap<Date,Rate1D> parser(ArrayList<String> arrStr, String hoursStr) throws Exception {
                    int hoursInt = Integer.parseInt(hoursStr);
                    TreeMap<Date,Rate1D> bufMap = new TreeMap<>();
                    Date prevDate = null, day = null, bufDate = null;

                    for (String bufStr : arrStr) {
                        String[] bufArrStr = bufStr.split(",");
                        if (Integer.parseInt(bufArrStr[1].substring(0,2)) >= hoursInt){
                            prevDate = format.parse(bufArrStr[0]+" "+hoursStr+":00");
                        }else{
                            if (prevDate==null){
                                int minusTime = (86_400_000/24)*(23-hoursInt);
                                prevDate = new Date(format.parse(bufArrStr[0] + " 00:00").getTime()-minusTime);
                            }
                        }

                        if (!bufMap.containsKey(prevDate)){
                            bufMap.put(prevDate,
                                    new Rate1D(0.0,0.0,0.0,0.0,0.0,0.0,0.0));
                        }
                        bufMap.get(prevDate).addRate(
                                "1H",
                                format.parse(bufArrStr[0]+" "+bufArrStr[1]),
                                Double.parseDouble(bufArrStr[2]),
                                Double.parseDouble(bufArrStr[3]),
                                Double.parseDouble(bufArrStr[4]),
                                Double.parseDouble(bufArrStr[5])

                        );
                    }

                    double lastClose = 0;
                    for(Date dt:bufMap.keySet()){
                        Rate1D curRateMap = bufMap.get(dt);
                        Double open = 0.0, min = 100000.0, max = 0.0, close = 0.0;
                        for (Date dt2:curRateMap.rate1H.keySet()){
                            Rate curRate = curRateMap.rate1H.get(dt2);
                            if (open == 0.0){
                                open = curRate.open;
                            }
                            min = Math.min(min,curRate.min);
                            max = Math.max(max,curRate.max);
                            close = curRate.close;
                        }
                        curRateMap.open = open;
                        curRateMap.min = min;
                        curRateMap.max = max;
                        curRateMap.close = close;

                        lastClose = close;
                    }
                    if (!bufMap.containsKey(new Date(getDate(mainFrame.arrPeriodDate.get(1)).getTime() + (86_400_000/24)*(hoursInt)))) {
                        bufMap.put(new Date(getDate(mainFrame.arrPeriodDate.get(1)).getTime() + (86_400_000/24)*(hoursInt)),
                                new Rate1D(lastClose, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
                    }
                    return bufMap;
                }
            }

            rates1D7= new innerParser().parser(file60StrArr,"07");
            rates1D13=new innerParser().parser(file60StrArr,"13");
            rates1D23=new innerParser().parser(file60StrArr,"23");

        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    private double getVolume(){
        if (mainFrame.cbThreadRun.isSelected()){
            double bal = ((int)balance)/ ordersVolume;
            if (bal < 1.0) {
                return 0.1;
            }else if (bal < 10.0) {
                return round(bal / 10, 1);
            }else if (bal < 80){
                return (int) (bal / 10);
            }else {
                return 8.0;
            }
        }else{
            return 0.1;
        }
    }                         //0.1-8.0 order volume

    //methods for dates and hours
    static Date getDate(JDatePickerImpl dt) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
            return format.parse(dt.getJFormattedTextField().getText());
        }catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }
    static Date getDate(JDatePickerImpl dt,String command) {
        try {
            if (command == "beginMonth") {
                SimpleDateFormat formatDay = new SimpleDateFormat("dd.MM.yyyy");
                SimpleDateFormat format = new SimpleDateFormat("MM.yyyy");
                return formatDay.parse("01." + format.format(Courency.getDate(dt)));
            }else {
                return null;
            }
        }catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }
    static Date addDate(Date dt, int days){
        return new Date(dt.getTime() + (long)days*86400000);
    }
    static String getDateTime(Date dt, int fl){
        switch (fl) {
            case 1:     //date + time
                return new SimpleDateFormat("dd.MM.yy HH:mm").format(dt);
            case 2:     //date
                return new SimpleDateFormat("dd.MM.yy").format(dt);
            case 3:     //invert date
                return new SimpleDateFormat("yy.MM.dd").format(dt);
            case 4:     //time
                return new SimpleDateFormat("HH:mm").format(dt);
            case 5:     //day midnight
                return new SimpleDateFormat("dd.MM.yy").format(dt) + " 00:00";
            default:
                return "";
        }
    }  //1-date time, 2-date, 3-american date, 4-time, 5-day midnight
    static int getHourFormDate(Date dt){
        return Integer.parseInt(new SimpleDateFormat("dd.MM.yy HH:mm").format(dt).substring(9,11));
    }

    //methods for rounding numbers
    double round(double d){
        d = d * Math.pow(10, digitsAfterZero);
        int i = (int) Math.round(d);
        return  (double) i / Math.pow(10, digitsAfterZero);
    }                     //round digitsAfterZero
    static double round(double d, int digits){
        d = d * Math.pow(10, digits);
        int i = (int) Math.round(d);
        return  (double) i / Math.pow(10, digits);
    }   //round with param

    //main methods for filling signals
    TreeMap<Date,Signal> fillSpoon(String paramStr){ //not working
        return null;
    }
    TreeMap<Date,Signal> fillSpoon2(String paramStr, int numSignal, TreeMap<Date,Rate1D> rates){
        if (paramStr.length() == 0)
            return null;

        TreeMap<Date,Signal> bufSignalMap = new TreeMap<>();
        String[] param = paramStr.split(",");

        int     period  = Integer.parseInt(param[0]),
                numType = Integer.parseInt(param[1]),
                days    = Integer.parseInt(param[2]),
                percTP  = Integer.parseInt(param[3]),
                percSL  = Integer.parseInt(param[4]);
        String  signalFlag = param[5],
                signalFlag2 = param[6],
                orderStat="";

        if (param.length>7){
            orderStat = param[7];
        }

        ArrayList<Date> dateArr = new ArrayList<>();
        dateArr.addAll(rates.keySet());

        try{
            //86400000
            int startIndex = dateArr.indexOf(new Date(getDate(mainFrame.arrPeriodDate.get(0)).getTime()+
                    (86_400_000/24)*period));

            Set<Date> prevPeriodKeySet = null;

            if ((startIndex < 0)||(startIndex < days)){
                startIndex = 0;
            }

            for (int i = startIndex; i < dateArr.size(); i++) { //
                if (bufSignalMap.containsKey(dateArr.get(i))){
                    System.out.println(name+" "+numType+" "+orderStat+" "+getDateTime(dateArr.get(i),1));
                    continue;
                }

                Date    startDate  = dateArr.get(i - days),
                        finishDate = dateArr.get(i);
                Rate1D  startRate  = rates.get(startDate),
                        finishRate = rates.get(finishDate);

                SimpleDateFormat format = new SimpleDateFormat("dd.MM.yy HH:mm");

                //********************************************
                //not open if grow/down from prev period
                class InnerClass{
                    double minBefore, maxBefore;

                    boolean runSignalOrNot(TreeMap<Date,Rate1D> bufRate, Date finishDate, String time){
                        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yy HH:mm");
                        Rate    startPrevRate = null;
                        Date    startPrevDate = null;
                        double  min = 1000000.0,
                                max = 0.0;

                        try{
                            startPrevDate = format.parse(getDateTime(finishDate,2)+" "+time);
                        }catch (Exception ex){
                            ex.printStackTrace();
                            return false;
                        }

                        if (!bufRate.containsKey(startPrevDate)){
                            System.err.println("not contain key: "+getDateTime(startPrevDate,1)+" time="+time);
                            return false;
                        }
                        ArrayList<Date> bufDateArr = new ArrayList<>(bufRate.get(startPrevDate).rate1H.keySet());
                        if (bufDateArr.size()==0){
                            System.err.println("bufDateArr.size()==0 "+getDateTime(startPrevDate,1)+" time="+time);
                            return false;
                        }
                        startPrevRate = bufRate.get(startPrevDate).rate1H.get(bufDateArr.get(0));

                        bufDateArr = new ArrayList<>(bufRate.get(startPrevDate).rate1H.keySet());

                        for(int pos = 0; pos < bufDateArr.size(); pos++){
                            if (bufDateArr.get(pos).compareTo(finishDate)<0){
                                Rate curRate = bufRate.get(startPrevDate).rate1H.get(bufDateArr.get(pos));
                                if (curRate.max > startPrevRate.open) {
                                    maxBefore += curRate.max - startPrevRate.open;
                                }
                                if (curRate.min < startPrevRate.open){
                                    minBefore += startPrevRate.open - curRate.min;
                                }
                                min = Math.min(min,curRate.min);
                                max = Math.max(max,curRate.max);
                            }else{
                                pos = bufDateArr.size();
                            }
                        }

//                        if  (((signalFlag.compareTo("Sell") == 0) && ((maxBefore > minBefore) || (finishRate.open > startPrevRate.open))) || //growing
//                                ((signalFlag.compareTo("Buy") == 0) && ((maxBefore < minBefore) || (finishRate.open < startPrevRate.open)))){ //falling
//                            return false;
//                        }
                        if  (signalFlag.compareTo("Sell") == 0) {
                            if ((maxBefore > minBefore) || //(finishRate.open > startPrevRate.open) ||
                                ( (startPrevRate.open - min) > (startPrevRate.open - finishRate.open)*2)){
                                return false;
                            }
                        }else if (signalFlag.compareTo("Buy") == 0) {
                            if ((maxBefore < minBefore) || //(finishRate.open < startPrevRate.open) ||
                                ( (max - startPrevRate.open) > (finishRate.open - startPrevRate.open)*2)){
                                return false;
                            }
                        }

                        return true;
                    }
                }
                //end inner class

                InnerClass innerClass = new InnerClass();
                switch (period){
                    case 7:
                        if (innerClass.runSignalOrNot(rates1D, finishDate,"00:00")==false)
                            continue;
                        break;
                    case 13:
                        if (innerClass.runSignalOrNot(rates1D7,finishDate,"07:00")==false)
                            continue;
                        break;
                    case 23:
                        if (innerClass.runSignalOrNot(rates1D13, finishDate,"13:00")==false)
                            continue;
                        break;
                    case 0:
                }

                //********************************************
                double  middleStart = startRate.min + (startRate.max - startRate.min)/2,
                        maxChange = 0.0, minChange = 0.0;

                for (int k = 1; k < days; k++) {
                    maxChange += rates.get(dateArr.get(i - k)).max - startRate.max;
                    minChange += rates.get(dateArr.get(i - k)).min - startRate.min;
                }

                double  maxFinish2 = startRate.max + (maxChange / (days - 1));//* days;
                double  minFinish2 = startRate.min + (minChange / (days - 1));//* days;
                double  middleFinish = minFinish2 + (maxFinish2 - minFinish2) / 2;
                double  sizeBeetwinUpDown = maxFinish2 - minFinish2;

                Double bufTP = round((double) percTP/100*(sizeBeetwinUpDown));
                Double bufSL = round((double) percSL/100*(sizeBeetwinUpDown));
                if (bufSL*Math.pow(10,digitsAfterZero)<10){
                    bufSL = 10/Math.pow(10,digitsAfterZero);
                }

                int curNumType = 0;
                if (middleStart > middleFinish) {//снижает
                    if ((startRate.max - startRate.min) < (maxFinish2 - minFinish2)) { //расширяется
                        curNumType = 0;
                    }else { //сужается
                        curNumType = 1;
                    }
                }else if (middleStart < middleFinish) {//растет
                    if ((startRate.max - startRate.min) < (maxFinish2 - minFinish2)) { //расширяется
                        curNumType = 2;
                    }else { //сужается
                        curNumType = 3;
                    }
                }

//                maxFinish3
//                maxFinish2
//                maxFinish1
//                middleFinish
//                minFinish1
//                minFinish2
//                minFinish3

                int curNumType2 = 0;

                double maxFinish1 = middleFinish + sizeBeetwinUpDown/4;
                double maxFinish3 = maxFinish2 + sizeBeetwinUpDown/4;
                double maxFinish4 = maxFinish3 + sizeBeetwinUpDown/4;

                double minFinish1 = middleFinish - sizeBeetwinUpDown/4;
                double minFinish3 = middleFinish - sizeBeetwinUpDown/4;
                double minFinish4 = middleFinish - sizeBeetwinUpDown/4;

                double curOpen = rates.get(dateArr.get(i)).open;

                if (curOpen > maxFinish4) {
                    if (signalFlag2.compareTo("up5")!=0) {
                        continue;
                    }
                }else if (curOpen > maxFinish3){
                    if (signalFlag2.compareTo("up4")!=0) {
                        continue;
                    }
                }else if (curOpen > maxFinish2){
                    if (signalFlag2.compareTo("up3")!=0) {
                        continue;
                    }
                }else if (curOpen > maxFinish1){
                    if (signalFlag2.compareTo("up2")!=0) {
                        continue;
                    }
                }else if (curOpen > middleFinish){
                    if (signalFlag2.compareTo("up")!=0) {
                        continue;
                    }
                }else if (curOpen > minFinish1){
                    if (signalFlag2.compareTo("down")!=0) {
                        continue;
                    }
                }else if (curOpen > minFinish2){
                    if (signalFlag2.compareTo("down2")!=0) {
                        continue;
                    }
                }else if (curOpen > minFinish3){
                    if (signalFlag2.compareTo("down3")!=0) {
                        continue;
                    }
                }else if (curOpen > minFinish4){
                    if (signalFlag2.compareTo("down4")!=0) {
                        continue;
                    }
                }else {
                    if (signalFlag2.compareTo("down5")!=0) {
                        continue;
                    }
                }


                if(!mainFrame.cbJustOneInTime.isSelected()){
                    finishDate = new Date(finishDate.getTime()+numSignal);
                }

                if (numType == curNumType){
                    if (signalFlag.compareTo("Buy")==0) {  //buy
                        bufSignalMap.put(finishDate,
                                new Signal(
                                        SignalFlag.BUY,
                                        numSignal+1,
                                        new Formatter().format("%d-%d.Buy.%d-%d.%s",
                                                numSignal+1,
                                                period,
                                                //days,
                                                percTP,
                                                percSL,
                                                orderStat).toString(),
                                        paramStr, //instead nameSignal
                                        finishRate.open,
                                        bufTP,
                                        bufSL,
                                        sizeBeetwinUpDown,
                                        maxFinish2,
                                        middleFinish,
                                        minFinish2,
                                        innerClass.minBefore,
                                        innerClass.maxBefore
                                )
                        );
                    }else if (signalFlag.compareTo("Sell")==0){     //sell
                        bufSignalMap.put(finishDate,
                                new Signal(
                                        SignalFlag.SELL,
                                        numSignal+1,
                                        new Formatter().format("%d-%d.Sell.%d-%d.%s",
                                                numSignal+1,
                                                period,
                                                //days,
                                                percTP,
                                                percSL,
                                                orderStat).toString(),
                                        paramStr, //istead nameSignal
                                        finishRate.open,
                                        bufTP,
                                        bufSL,
                                        sizeBeetwinUpDown,
                                        maxFinish2,
                                        middleFinish,
                                        minFinish2,
                                        innerClass.minBefore,
                                        innerClass.maxBefore
                                )
                        );
                    }
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return bufSignalMap;
    }
    TreeMap<Date,Signal> fillSpoon3(String paramStr, TreeMap<Date,Rate1D> rates){
        return null;
//        if (paramStr.length() == 0)
//            return null;
//
//        TreeMap<Date,Signal> bufSignalMap = new TreeMap<>();
//
//        String[] param = paramStr.split(",");
//        int     period  = Integer.parseInt(param[0]),
//                numType = Integer.parseInt(param[1]),
//                days    = Integer.parseInt(param[2]);
//
//        String  orderStat="";
//        if (param.length>7){
//            orderStat = param[7];
//        }
//
//        ArrayList<Date> dateArr = new ArrayList<>();
//        dateArr.addAll(rates.keySet());
//
//        try{
//            //86400000
//            int startIndex = dateArr.indexOf(new Date(getDate(mainFrame.arrPeriodDate.get(0)).getTime()+
//                    (86_400_000/24)*period));
//
//            Set<Date> prevPeriodKeySet = null;
//
//            if ((startIndex < 0)||(startIndex < days)){
//                startIndex = 0;
//            }
//
//            for (int i = startIndex; i < dateArr.size(); i++) { //
//                if (bufSignalMap.containsKey(dateArr.get(i))){
//                    System.out.println(name+" "+numType+" "+orderStat+" "+getDateTime(dateArr.get(i),1));
//                    continue;
//                }
//
//                Date    startDate  = dateArr.get(i - days),
//                        finishDate = dateArr.get(i);
//                Rate1D  startRate  = rates.get(startDate),
//                        finishRate = rates.get(finishDate);
//
//                SimpleDateFormat format = new SimpleDateFormat("dd.MM.yy HH:mm");
//
//                //********************************************
//                //not open if grow/down from prev period
//                class InnerClass{
//                    double minBefore, maxBefore;
//
//                    boolean runSignalOrNot(TreeMap<Date,Rate1D> bufRate, Date finishDate, String time){
//                        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yy HH:mm");
//                        Rate    startPrevRate = null;
//                        Date    startPrevDate = null;
//                        double  min = 1000000.0,
//                                max = 0.0;
//
//                        try{
//                            startPrevDate = format.parse(getDateTime(finishDate,2)+" "+time);
//                        }catch (Exception ex){
//                            ex.printStackTrace();
//                            return false;
//                        }
//
//                        if (!bufRate.containsKey(startPrevDate)){
//                            System.err.println("not contain key: "+getDateTime(startPrevDate,1)+" time="+time);
//                            return false;
//                        }
//                        ArrayList<Date> bufDateArr = new ArrayList<>(bufRate.get(startPrevDate).rate1H.keySet());
//                        if (bufDateArr.size()==0){
//                            System.err.println("bufDateArr.size()==0 "+getDateTime(startPrevDate,1)+" time="+time);
//                            return false;
//                        }
//                        startPrevRate = bufRate.get(startPrevDate).rate1H.get(bufDateArr.get(0));
//
//                        bufDateArr = new ArrayList<>(bufRate.get(startPrevDate).rate1H.keySet());
//
//                        for(int pos = 0; pos < bufDateArr.size(); pos++){
//                            if (bufDateArr.get(pos).compareTo(finishDate)<0){
//                                Rate curRate = bufRate.get(startPrevDate).rate1H.get(bufDateArr.get(pos));
//                                if (curRate.max > startPrevRate.open) {
//                                    maxBefore += curRate.max - startPrevRate.open;
//                                }
//                                if (curRate.min < startPrevRate.open){
//                                    minBefore += startPrevRate.open - curRate.min;
//                                }
//                                min = Math.min(min,curRate.min);
//                                max = Math.max(max,curRate.max);
//                            }else{
//                                pos = bufDateArr.size();
//                            }
//                        }
//
////                        if  (((signalFlag.compareTo("Sell") == 0) && ((maxBefore > minBefore) || (finishRate.open > startPrevRate.open))) || //growing
////                                ((signalFlag.compareTo("Buy") == 0) && ((maxBefore < minBefore) || (finishRate.open < startPrevRate.open)))){ //falling
////                            return false;
////                        }
//                        if  (signalFlag.compareTo("Sell") == 0) {
//                            if ((maxBefore > minBefore) || //(finishRate.open > startPrevRate.open) ||
//                                    ( (startPrevRate.open - min) > (startPrevRate.open - finishRate.open)*2)){
//                                return false;
//                            }
//                        }else if (signalFlag.compareTo("Buy") == 0) {
//                            if ((maxBefore < minBefore) || //(finishRate.open < startPrevRate.open) ||
//                                    ( (max - startPrevRate.open) > (finishRate.open - startPrevRate.open)*2)){
//                                return false;
//                            }
//                        }
//
//                        return true;
//                    }
//                }
//                //end inner class
//
//                InnerClass innerClass = new InnerClass();
//                switch (period){
//                    case 7:
//                        if (innerClass.runSignalOrNot(rates1D, finishDate,"00:00")==false)
//                            continue;
//                        break;
//                    case 13:
//                        if (innerClass.runSignalOrNot(rates1D7,finishDate,"07:00")==false)
//                            continue;
//                        break;
//                    case 23:
//                        if (innerClass.runSignalOrNot(rates1D13, finishDate,"13:00")==false)
//                            continue;
//                        break;
//                    case 0:
//                }
//
//                //********************************************
//                double  middleStart = startRate.min + (startRate.max - startRate.min)/2,
//                        maxChange = 0.0, minChange = 0.0;
//
//                for (int k = 1; k < days; k++) {
//                    maxChange += rates.get(dateArr.get(i - k)).max - startRate.max;
//                    minChange += rates.get(dateArr.get(i - k)).min - startRate.min;
//                }
//
//                double  maxFinish2 = startRate.max + (maxChange / (days - 1));//* days;
//                double  minFinish2 = startRate.min + (minChange / (days - 1));//* days;
//                double  middleFinish = minFinish2 + (maxFinish2 - minFinish2) / 2;
//                double  sizeBeetwinUpDown = maxFinish2 - minFinish2;
//
//                Double bufTP = round((double) percTP/100*(sizeBeetwinUpDown));
//                Double bufSL = round((double) percSL/100*(sizeBeetwinUpDown));
//                if (bufSL*Math.pow(10,digitsAfterZero)<10){
//                    bufSL = 10/Math.pow(10,digitsAfterZero);
//                }
//
//                int curNumType = 0;
//                if (middleStart > middleFinish) {//снижает
//                    if ((startRate.max - startRate.min) < (maxFinish2 - minFinish2)) { //расширяется
//                        curNumType = 0;
//                    }else { //сужается
//                        curNumType = 1;
//                    }
//                }else if (middleStart < middleFinish) {//растет
//                    if ((startRate.max - startRate.min) < (maxFinish2 - minFinish2)) { //расширяется
//                        curNumType = 2;
//                    }else { //сужается
//                        curNumType = 3;
//                    }
//                }
//
////                maxFinish3
////                maxFinish2
////                maxFinish1
////                middleFinish
////                minFinish1
////                minFinish2
////                minFinish3
//
//                int curNumType2 = 0;
//
//                double maxFinish1 = middleFinish + sizeBeetwinUpDown/4;
//                double maxFinish3 = maxFinish2 + sizeBeetwinUpDown/4;
//                double maxFinish4 = maxFinish3 + sizeBeetwinUpDown/4;
//
//                double minFinish1 = middleFinish - sizeBeetwinUpDown/4;
//                double minFinish3 = middleFinish - sizeBeetwinUpDown/4;
//                double minFinish4 = middleFinish - sizeBeetwinUpDown/4;
//
//                double curOpen = rates.get(dateArr.get(i)).open;
//
//                if (curOpen > maxFinish4) {
//                    if (signalFlag2.compareTo("up5")!=0) {
//                        continue;
//                    }
//                }else if (curOpen > maxFinish3){
//                    if (signalFlag2.compareTo("up4")!=0) {
//                        continue;
//                    }
//                }else if (curOpen > maxFinish2){
//                    if (signalFlag2.compareTo("up3")!=0) {
//                        continue;
//                    }
//                }else if (curOpen > maxFinish1){
//                    if (signalFlag2.compareTo("up2")!=0) {
//                        continue;
//                    }
//                }else if (curOpen > middleFinish){
//                    if (signalFlag2.compareTo("up")!=0) {
//                        continue;
//                    }
//                }else if (curOpen > minFinish1){
//                    if (signalFlag2.compareTo("down")!=0) {
//                        continue;
//                    }
//                }else if (curOpen > minFinish2){
//                    if (signalFlag2.compareTo("down2")!=0) {
//                        continue;
//                    }
//                }else if (curOpen > minFinish3){
//                    if (signalFlag2.compareTo("down3")!=0) {
//                        continue;
//                    }
//                }else if (curOpen > minFinish4){
//                    if (signalFlag2.compareTo("down4")!=0) {
//                        continue;
//                    }
//                }else {
//                    if (signalFlag2.compareTo("down5")!=0) {
//                        continue;
//                    }
//                }
//
//
//                if(!mainFrame.cbJustOneInTime.isSelected()){
//                    finishDate = new Date(finishDate.getTime()+numSignal);
//                }
//
//                if (numType == curNumType){
//                    if (signalFlag.compareTo("Buy")==0) {  //buy
//                        bufSignalMap.put(finishDate,
//                                new Signal(
//                                        SignalFlag.BUY,
//                                        numSignal+1,
//                                        new Formatter().format("%d-%d.Buy.%d-%d.%s",
//                                                numSignal+1,
//                                                period,
//                                                //days,
//                                                percTP,
//                                                percSL,
//                                                orderStat).toString(),
//                                        paramStr, //instead nameSignal
//                                        finishRate.open,
//                                        bufTP,
//                                        bufSL,
//                                        sizeBeetwinUpDown,
//                                        maxFinish2,
//                                        middleFinish,
//                                        minFinish2,
//                                        innerClass.minBefore,
//                                        innerClass.maxBefore
//                                )
//                        );
//                    }else if (signalFlag.compareTo("Sell")==0){     //sell
//                        bufSignalMap.put(finishDate,
//                                new Signal(
//                                        SignalFlag.SELL,
//                                        numSignal+1,
//                                        new Formatter().format("%d-%d.Sell.%d-%d.%s",
//                                                numSignal+1,
//                                                period,
//                                                //days,
//                                                percTP,
//                                                percSL,
//                                                orderStat).toString(),
//                                        paramStr, //istead nameSignal
//                                        finishRate.open,
//                                        bufTP,
//                                        bufSL,
//                                        sizeBeetwinUpDown,
//                                        maxFinish2,
//                                        middleFinish,
//                                        minFinish2,
//                                        innerClass.minBefore,
//                                        innerClass.maxBefore
//                                )
//                        );
//                    }
//                }
//            }
//        }catch (Exception ex){
//            ex.printStackTrace();
//        }
//        return bufSignalMap;
    }

    void addNewSignal(TreeMap<Date,Signal> bufMap){
        for (Date dt : bufMap.keySet()) {
            if (!signalMap.containsKey(dt)){
                signalMap.put(dt,bufMap.get(dt));
            }
        }
    }
    private Order addNewOrder(Order curOrder){
        orders.add(curOrder);

        //days, month and years result
        if (!emulateFlag) {
            for (Transaction curTr : curOrder.trans) {
                try {
                    SimpleDateFormat formatDay = new SimpleDateFormat("dd.MM.yyyy");
                    SimpleDateFormat formatMonth = new SimpleDateFormat("MM.yyyy");
                    SimpleDateFormat formatYear = new SimpleDateFormat("yyyy");

                    Date day =   formatDay.parse(formatDay.format(curTr.dateClose));
                    Date month = formatDay.parse("01." + formatMonth.format(curTr.dateClose));
                    Date year =  formatDay.parse("01.01." + formatYear.format(curTr.dateClose));

                    //tree dateTime ------------------------------
                    if (rates1D.containsKey(day)){
                        double lastRes=0.0;
                        for (Date dt: rates1D.get(day).rate1H.keySet()){
                            Rate curRate = rates1D.get(day).rate1H.get(dt);
                            double res = 0.0;
                            if (dt.compareTo(curTr.dateOpen) < 0) {
                                res = 0.0;
                            } else if(dt.compareTo(curTr.dateClose) < 0){
                                if (curOrder.signal.signalFlag == SignalFlag.BUY) {
                                    res += (curRate.close - curTr.open);
                                } else if (curOrder.signal.signalFlag == SignalFlag.SELL) {
                                    res += (curTr.open - curRate.close);
                                }
                            } else if(dt.compareTo(curTr.dateClose) == 0){
                                if (curOrder.signal.signalFlag == SignalFlag.BUY) {
                                    res += (curTr.close - curTr.open);
                                } else if (curOrder.signal.signalFlag == SignalFlag.SELL) {
                                    res += (curTr.open - curTr.close);
                                }
                            } else if (dt.compareTo(curTr.dateClose) > 0) {
                                res = lastRes;
                            }
                            lastRes = res;
                            res = res * curOrder.volume * Math.pow(10, digitsAfterZero + 1) * rateUSD;
                            dateTimeTree.put(dt,round(res, 2));
                        }
                    }

                    //tree Day, Month, Year -----------------------
                    if (dayTree.containsKey(day)) {
                        dayTree.put(day, round(dayTree.get(day) + curTr.result, 2));
                    } else {
                        dayTree.put(day, round(curTr.result, 2));
                    }
                    if (monthTree.containsKey(month)) {
                        monthTree.put(month, round(monthTree.get(month) + curTr.result, 2));
                    } else {
                        monthTree.put(month, round(curTr.result, 2));
                    }
                    if (yearTree.containsKey(year)) {
                        yearTree.put(year, round(yearTree.get(year) + curTr.result, 2));
                    } else {
                        yearTree.put(year, round(curTr.result, 2));
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
//                    JOptionPane.showMessageDialog(mainFrame, "Error creating trees");
                }
            }
        }

        this.result += curOrder.result;
        curOrder.strResultHours+= (int)Math.round(curOrder.result);
        return null;
    }//add order to arr and assign them to null

    public void run() {
        if (!emulateFlag) {
            dayTree.clear();
            monthTree.clear();
            yearTree.clear();
            dateTimeTree.clear();
        }

        orders.clear();
        result = 0;
        Order order = null;
        try {
            Date startDate = Courency.getDate(mainFrame.arrPeriodDate.get(0));
            Date endDate = Courency.getDate(mainFrame.arrPeriodDate.get(1));

            mainFrame.setProgress(rates1D.size());
            Rate curRate = null;
            Date lastDate = null;
            int hoursCount = 0;
            for (Date dayDate : rates1D.keySet()) {
                if ((dayDate.compareTo(startDate) < 0) || (dayDate.compareTo(endDate) > 0)) {
                    continue;
                }
                //swap open order
                if ((order != null) && (curRate != null)) {
                    //correct sLoss every night swap
                    if (mainFrame.cbSLdown.isSelected()&&
                        ((order.trans.size()>=1)&&(!getDateTime(order.trans.get(0).dateOpen,1).contains("23:00"))) ||
                        ((order.trans.size()>=2)&&(getDateTime(order.trans.get(0).dateOpen,1).contains("23:00")))) {

                        double newSL = order.trans.get(order.trans.size()-1).close - (order.open - order.sLoss);
                        if (order.signal.signalFlag == SignalFlag.SELL){
                            if (newSL < order.signal.open){
                                order.sLoss = order.signal.open;
                            }else{
                                order.sLoss = Math.min(order.sLoss,newSL);
                            }
                        }else{ //BUY
                            if (newSL > order.open){
                                order.sLoss = order.open;
                            }else{
                                order.sLoss = Math.max(order.sLoss,newSL);
                            }
                        }
                    }
                    //order max 5 days, if order result < 0 - max 3 days
                    if (mainFrame.cb3Min5Max.isSelected()) {
                        if ((order.result < 0) && (order.trans.size() >= 3) ||
                            (order.trans.size() > 4)) {
                            order = addNewOrder(order);
                        }
                    }
                    if (order != null) {
                        if (order.signal.signalFlag == SignalFlag.BUY) {
                            order.openTransaction(this, lastDate, curRate.close + swapBuy);
                        } else if (order.signal.signalFlag == SignalFlag.SELL) {
                            order.openTransaction(this, lastDate, curRate.close - swapSell);
                        }
                    }
                }

                for (Date curDate : rates1D.get(dayDate).rate1H.keySet()) {
                    curRate = rates1D.get(dayDate).rate1H.get(curDate);
                    lastDate = curDate;

                    //we have a signal at this time
                    if (signalMap.containsKey(curDate)) {
                        Signal curSignal = signalMap.get(curDate);
                        if ((order != null)&&(mainFrame.cbCloseIfNewSignal.isSelected())) {
                            if ((curSignal.signalFlag != order.signal.signalFlag)){
                                    //(curSignal.rateNum < order.signal.rateNum){
                                System.out.println(curSignal.signalFlag+"!="+order.signal.signalFlag+";"+curSignal.serialNum +"<"+order.signal.serialNum);
                                order.closeTransaction(curDate, order.open);
                                order = addNewOrder(order);
                            }
                        }
                        if (order == null) {
                            // NEW ORDER !!!
                            order = new Order(curSignal, curDate, this, getVolume());
                            mainFrame.setProgress(1);
                            hoursCount = 0;
                        }
                    }

                    if (order != null){
                        double newMin = (order.open - curRate.min)*Math.pow(10,digitsAfterZero);
                        double newMax = (curRate.max - order.open)*Math.pow(10,digitsAfterZero);
                        int bufRes = (int)Math.round((curRate.open-order.open)*Math.pow(10,digitsAfterZero));
                        if (name.contains("GOLD")){
                            bufRes = bufRes/100;
                        }
                        order.strResultHours += ((order.signal.signalFlag == SignalFlag.BUY) ? 1 : -1)*bufRes+",";
                        if ((curRate.open > curRate.min)&&(order.min < newMin)) {
                            if (order.signal.signalFlag != SignalFlag.BUY) {
                                order.min = newMin; //
                                order.minHours = hoursCount;
                            }else{
                                order.max = newMin; //
                                order.maxHours = hoursCount;
                            }
                        }
                        if ((curRate.open < curRate.max)&&(order.max < newMax)) {
                            if (order.signal.signalFlag != SignalFlag.BUY) {
                                order.max = newMax; //
                                order.maxHours = hoursCount;
                            }else{
                                order.min = newMax; //
                                order.minHours = hoursCount;
                            }
                        }

                        // close on SL or TP
                        if (order.signal.signalFlag == SignalFlag.BUY) {
                            if (curRate.min <= order.sLoss) {
                                order.closeTransaction(curDate, order.sLoss);
                                order = addNewOrder(order);
                            } else if ((curRate.max >= order.tProf) && (!mainFrame.cbTPJustOnSwap.isSelected())) {
                                order.closeTransaction(curDate, order.tProf);
                                order = addNewOrder(order);
                            }
                        } else if (order.signal.signalFlag == SignalFlag.SELL) {
                            if (curRate.max >= order.sLoss) {
                                order.closeTransaction(curDate, order.sLoss);
                                order = addNewOrder(order);
                            } else if ((curRate.min <= order.tProf) && (!mainFrame.cbTPJustOnSwap.isSelected())) {
                                order.closeTransaction(curDate, order.tProf);
                                order = addNewOrder(order);
                            }
                        }

                        //order not closed and we have signal "CLOSE"
                        if ((order != null) && (signalMap.containsKey(curDate))) {
                            if (signalMap.get(curDate).signalFlag == SignalFlag.CLOSE) {
                                order.closeTransaction(curDate, curRate.open);
                                order = addNewOrder(order);
                            }
                        }
//                        if ((order !=null)&&(mainFrame.cbSLdown.isSelected())){
//                            double newSL = order.trans.get(order.trans.size()-1).close - (order.open - order.sLoss);
//                            if (order.signal.signalFlag == SignalFlag.SELL){
//                                if (newSL < order.signal.open){
//                                    order.sLoss = order.signal.open;
//                                }else{
//                                    order.sLoss = Math.min(order.sLoss,newSL);
//                                }
//                            }else{ //BUY
//                                if (newSL > order.open){
//                                    order.sLoss = order.open;
//                                }else{
//                                    order.sLoss = Math.max(order.sLoss,newSL);
//                                }
//                            }
//                        }
                    }
                    hoursCount++;
                }

                //swap day
                if ((order != null) && (curRate != null)) {
                    // fix tranzaction
                    order.closeTransaction(lastDate, curRate.close);

                    if (mainFrame.cbCutTPSLOnSwap.isSelected()) {
                        double saldo;
                        if (order.trans.size() == 1) {
                            saldo = order.trans.get(0).saldo;
                        } else {
                            saldo = order.trans.get(order.trans.size() - 1).saldo -
                                    order.trans.get(order.trans.size() - 2).saldo;
                        }
                        if ((saldo > 0) && (order.trans.get(order.trans.size() - 1).saldo > 0)) {
                            if (order.signal.signalFlag == SignalFlag.BUY) {
                                order.tProf = curRate.close + swapBuy + (order.signal.tProfVolume);
                                order.sLoss = curRate.close + swapBuy - (order.signal.sLossVolume);
                            } else if (order.signal.signalFlag == SignalFlag.SELL) {
                                order.tProf = curRate.close - swapSell + (order.signal.tProfVolume);
                                order.sLoss = curRate.close - swapSell - (order.signal.sLossVolume);
                            }
                        }
                    } else if (mainFrame.cbTPJustOnSwap.isSelected()) {
                        if (order.signal.signalFlag == SignalFlag.BUY) {
                            if (order.trans.get(order.trans.size() - 1).close > order.tProf) {
                                order = addNewOrder(order);
                            }
                        } else if (order.signal.signalFlag == SignalFlag.SELL) {
                            if (order.trans.get(order.trans.size() - 1).close < order.tProf) {
                                order = addNewOrder(order);
                            }
                        }
                    }

                    //if order not close yet
                    if (order != null) {
                        if (mainFrame.cbCutTPSLOnSwap.isSelected()) {
                            if (order.signal.signalFlag == SignalFlag.BUY) {
                                order.tProf = order.open + (order.signal.tProfVolume) * (1 - (double) order.trans.size() / 10);
                                order.sLoss = order.open - (order.signal.sLossVolume) * (1 - (double) order.trans.size() / 10);
                            } else if (order.signal.signalFlag == SignalFlag.SELL) {
                                order.tProf = order.open + (order.signal.tProfVolume) * (1 - (double) order.trans.size() / 10);
                                order.sLoss = order.open - (order.signal.sLossVolume) * (1 - (double) order.trans.size() / 10);
                            }
                        }
                    }
                }
                if (mainFrame.cbThreadRun.isSelected()) {
                    sleep(1);
                }
                mainFrame.setProgress(-1);
            }

            if (order != null) {
                addNewOrder(order);
            }
            if (!emulateFlag) {
                for (int i = 0; i < mainFrame.arrLabel.size(); i++) {
                    if (mainFrame.arrFile.get(i).getText().contains(this.name)) {
                        mainFrame.arrLabel.get(i).setText("<html><font color='green'>" +
                                new Formatter().format("%.2f (%d/%d)", this.result, orders.size(),
                                        (int)(this.result/orders.size())).toString()+"</font></html>");
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        mainFrame.setProgress(0);
    }

    @Override
    public String toString(){
        return new Formatter().format("%s, rates1D = %d",
                name,rates1D.size()
        ).toString();
    }

}
