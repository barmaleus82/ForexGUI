import java.util.*;

class RateFactory {
    ArrayList<Courency> courArr = new ArrayList<>();
    MainFrame mainFrame;

    RateFactory(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    void readFiles(){
        courArr.clear();
        for (int i=0; i< mainFrame.arrCheck.size(); i++){
            if (!mainFrame.arrCheck.get(i).isSelected()) {
                mainFrame.arrLabel.get(i).setText("");
            }else {
                try {
                    // add and fill new courency
                    Courency bufCour = new Courency(mainFrame, mainFrame.arrFile.get(i).getText());
                    if (mainFrame.arrCoef.get(i).getText().length()!=0){
                        bufCour.paramArr.addAll(Arrays.asList(mainFrame.arrCoef.get(i).getText().split(";")));
                    }
                    courArr.add(bufCour);
               }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }
    }
    void fillSignals(boolean justOneAtTime){
        String typeEmulate = (String)mainFrame.comboEmulateType.getSelectedItem();
        mainFrame.setProgress(0);
        Courency.balance = Integer.parseInt(mainFrame.textStartBalance.getText());
        for (Courency cour : courArr) {
//            Integer[] bufArr = coefMap.get(cour);
            if (typeEmulate.equals("See prev day")) {
//                cour.fillSeePrevDay(bufArr[0],bufArr[1]);
            }else if (typeEmulate.equals("Middle 15 day")) {
//                cour.fillMiddle15(bufArr[0],bufArr[1]);
            }else if (typeEmulate.equals("Spoon")) {
//                cour.transSignals.clear();
//                for (String str : cour.paramArr){
//                    cour.fillSpoon(str);
//                }
            }else if (typeEmulate.equals("Spoon 2")) {
                cour.signalMap.clear();
                for(int i = 0; i < cour.paramArr.size();i++){
                    String[] arrStr = cour.paramArr.get(i).split(",");
                    if ((mainFrame.cbSignal_0AM.isSelected())&&(arrStr[0].equals("0"))) {
                        cour.addNewSignal(cour.fillSpoon2(cour.paramArr.get(i),i,cour.rates1D));
                    }
                    if ((mainFrame.cbSignal_7AM.isSelected())&&(arrStr[0].equals("7"))) {
                        cour.addNewSignal(cour.fillSpoon2(cour.paramArr.get(i),i,cour.rates1D7));
                    }
                    if ((mainFrame.cbSignal_13AM.isSelected())&&(arrStr[0].equals("13"))) {
                        cour.addNewSignal(cour.fillSpoon2(cour.paramArr.get(i),i,cour.rates1D13));
                    }
                    if ((mainFrame.cbSignal_23AM.isSelected())&&(arrStr[0].equals("23"))) {
                        cour.addNewSignal(cour.fillSpoon2(cour.paramArr.get(i),i,cour.rates1D23));
                    }
                }
            }else if (typeEmulate.equals("Spoon 3")) {
//                cour.transSignals.clear();
//                for(int i = 0; i<cour.paramArr.size();i++){
//                    cour.fillSpoon2(cour.paramArr.get(i),i,cour.rates1D12);
//                }
            }else if (typeEmulate.equals("Middle 15 day calm")) {
//                cour.fillSignalsMiddle15Calm(bufArr[0],bufArr[1],bufArr[2]);
            }
        }

    }
    void createOrders(){
        readFiles();
        fillSignals(true);
        for (Courency cour : courArr) {
            cour.emulateFlag = false;
            cour.setName(cour.name);
            cour.start();
        }
        printOrders();
    }
    void emulateOrders(){
        readFiles();
        Object[] bufHeader = null;
        ArrayList<Object[]> bufArr = new ArrayList();

        double maxValue = 0;
        double maxValueYear = 0;

        String typeEmulate = (String)mainFrame.comboEmulateType.getSelectedItem();

        for (Courency cour : courArr) {
            cour.emulateFlag = true;

            if (typeEmulate.equals("See prev day")) {
//
//                bufHeader = new Object[] {"Name","Operation","K1","K2","RESULT"};
//                bufArr = new ArrayList<Object[]>();
//
//                for (int i=50; i<=200; i+=10){ //%
//                    for (int j=50; j<=200; j+=10) { //%
//                        cour.fillSeePrevDay(i, j);
//                        cour.run();
//                        if (cour.result > maxValue) {
//                            maxValue = cour.result;
//                            bufArr.add(new Object[]{cour.name, "Maximum ", i, j, (int)maxValue});
//                        }
//                    }
//                }
            }else if (typeEmulate.equals("Middle 15 day")) {
//                bufHeader = new Object[] {"Name","Operation","K1","K2","max","min","RESULT"};
//                bufArr = new ArrayList<Object[]>();
//
//                for (int i=50; i<=150; i += 10) {   //calmDays
//                    for (int j = 50; j <=150; j += 10) {     //+2 calmRange
//                        cour.fillMiddle15(i, j);
//                        cour.run();
//
//                        double bufVal = 1;
//                        for (Date yearKey : cour.yearTree.keySet()){
//                            if (cour.yearTree.get(yearKey)<0){
//                                bufVal=0;
//                                break;
//                            }else {
//                                bufVal = bufVal + cour.yearTree.get(yearKey);
//                            }
//                        }
//
//                        int max=0,min=0;
//                        for (Order order : cour.orders){
//                            if (max < order.result){
//                                max = (int)order.result;
//                            }else if(min> order.result){
//                                min = (int)order.result;
//                            }
//                        }
//
//                        if (bufVal > maxValue){ //maxValue
//                            maxValue = bufVal;
//                            bufArr.add(new Object[]{cour.name,"Not minus year ",i,j,max,min,(int)maxValue});
//                        }
//                        if (cour.result > maxValueYear){    //maxValueYear
//                            maxValueYear = cour.result;
//                            bufArr.add(new Object[]{cour.name,"Maximum ",i,j,max,min,(int)maxValueYear});
//                        }
//                    }
//                }
            }else if (typeEmulate.equals("Spoon")) {

//                bufHeader = new Object[] {"Name","Operation","K1","K2","K3","max Order>0","min Order<0","Order count","res-","res+","RESULT","days",""};
//                bufArr = new ArrayList<Object[]>();
//                TreeMap<Double,Object[]> bufTree = new TreeMap<>();
//
//                try {
//                    for (int n = 0; n <4; n++) {   //position signal
//                        System.out.println();
//                        for (int i = 2; i <= 15; i++) {   //calmDays
//                            System.out.print("*");
//                            for (int j = 20; j <= 200; j += 10) {     //buy %
//                                for (int k = 10; k <= 200; k += 10) {     //sell %
//                                    for (String bufStr2 : new String[]{"Buy","Sell"}) { //type signals
//                                        cour.transSignals.clear();
//                                        cour.fillSpoon(""+n+","+i+","+j+","+k+","+bufStr2);
//                                        cour.run();
//
//                                        double totalMinus = 0, totalPlus = 0;
//                                        int orderMax = 0, orderMin = 0, transCount = 0;
//
//                                        HashMap<String, Double> bufMap = new HashMap<>();
//
//                                        for (Order order : cour.orders) {
//                                            if (order.result > orderMax) {
//                                                orderMax = (int) order.result;
//                                            } else if (order.result < orderMin) {
//                                                orderMin = (int) order.result;
//                                            }
//                                            for (Transaction tr : order.trans) {
//                                                if (tr.result < 0) {
//                                                    totalMinus += tr.result;
//                                                } else {
//                                                    totalPlus += tr.result;
//                                                }
//                                                transCount++;
//                                            }
//
//                                            if (bufMap.containsKey(order.name)) {
//                                                bufMap.put(order.name, bufMap.get(order.name) + order.result);
//                                            } else {
//                                                bufMap.put(order.name, order.result);
//                                            }
//                                        }
//
//                                        String bufStr = "";
//                                        for (String key : bufMap.keySet()) {
//                                            bufStr += key + " (" + cour.round(bufMap.get(key), 2) + ") ";
//                                        }
//                                        if ((-totalMinus/totalPlus*100)<60) {
//                                            bufTree.put(-totalMinus / totalPlus * 100, new Object[]{cour.name, "Emulate ", i, j, k,
//                                                    orderMax, orderMin, cour.orders.size(),
//                                                    (int) totalMinus,
//                                                    new Formatter().format("%d (%.2f)", (int) totalPlus, -totalMinus / totalPlus * 100).toString(),
//                                                    (int) cour.result,
//                                                    new Formatter().format("%.2f", transCount / cour.orders.size()).toString(),
//                                                    bufStr.trim()});
//                                        }
//                                    }
//                                }
//                            }
//                            mainFrame.setProgress(-1);
//                        }
//                    }
//                }catch (Exception ex){
//                    ex.printStackTrace();
//                }
//                Object[] keyArr = bufTree.keySet().toArray();
//                for (int i = 0; i < keyArr.length; i++){
//                    bufArr.add(bufTree.get(keyArr[i]));
//                }
            }else if (typeEmulate.equals("Spoon 2")) {
                bufHeader = new Object[] {"Name","K1","K2","K3","max","min","res+","+/h","res-","-/h","RES","res h","/h","/10","%","ord","mid1","mid2","signal"};
                TreeMap<Double,Object[]> bufTree = new TreeMap<>();
                TreeMap<String,Double> findBestCoefMap = new TreeMap<>();
                bufTree.put(-1000.0,bufHeader);
                try {
                    System.out.println();
                    System.out.println(cour.name);
                    for (int n = 0; n < 4; n++) {  //position signal
                        for (int i = 3; i <= 15; i += 2) {   //calmDays 4+1=>15
                            System.out.print("*");
                            for (int k = 5; k <= 20; k += 5) {      //SL %   10+5=>50
//                                for (int j = (k*2 < 50)? 50 : k*2; j <= 150; j += 10) {     //TP %   150
                                for (int j = 50 ; j <= 120; j += 10) {     //TP %   150
                                    for (String bufStr : new String[]{"Buy","Sell"}) { //type signals
                                        for (String bufStr2 : new String[]{"up","up2","up3","up4","up5","down","down2","down3","down4","down5"}) { //type signals2
//                                        for (String bufStr2 : new String[]{"up4","down4"}) { //type signals2
                                            for (String strPeriod : new String[]{"0","7","13","23"}){
                                                cour.signalMap.clear();
                                                if (typeEmulate.equals("Spoon 2")) {
                                                    if ((mainFrame.cbSignal_0AM.isSelected())&&(strPeriod.compareTo("0")==0)){
                                                        cour.addNewSignal(cour.fillSpoon2("0,"+n+","+i+","+j+","+k+","+bufStr+","+bufStr2,0,cour.rates1D));
                                                    }else if ((mainFrame.cbSignal_7AM.isSelected())&&(strPeriod.compareTo("7")==0)){
                                                        cour.addNewSignal(cour.fillSpoon2("7,"+n+","+i+","+j+","+k+","+bufStr+","+bufStr2,0,cour.rates1D7));
                                                    }else if ((mainFrame.cbSignal_13AM.isSelected())&&(strPeriod.compareTo("13")==0)){
                                                        cour.addNewSignal(cour.fillSpoon2("13,"+n+","+i+","+j+","+k+","+bufStr+","+bufStr2,0,cour.rates1D13));
                                                    }else if ((mainFrame.cbSignal_23AM.isSelected())&&(strPeriod.compareTo("23")==0)){
                                                        cour.addNewSignal(cour.fillSpoon2("23,"+n+","+i+","+j+","+k+","+bufStr+","+bufStr2,0,cour.rates1D23));
                                                    }
                                                }
                                                if (cour.signalMap.size()>0) {
                                                    cour.run();

                                                    double  totalMinus = 0.0,
                                                            totalPlus = 0.0,
                                                            transCount = 0.0,
                                                            totalOrderLikeRange = 0.0,
                                                            hoursPlus = 0.0,
                                                            hoursMinus = 0.0;

                                                    int     orderMax = 0,
                                                            orderMin = 0,
                                                            plusOrderCount = 0,
                                                            minusOrderCount = 0,
                                                            minusTransCount = 0,
                                                            plusTransCount = 0;

                                                    String strMinMax ="";
                                                    for (Order order : cour.orders) {
                                                        if (order.result > orderMax) {
                                                            orderMax = (int) order.result;
                                                        } else if (order.result < orderMin) {
                                                            orderMin = (int) order.result;
                                                        }
                                                        if (order.result > 0) {
                                                            plusOrderCount++;
                                                        } else {
                                                            minusOrderCount++;
                                                        }
                                                        for (Transaction tr : order.trans) {
                                                            if (tr.result < 0) {
//                                                                totalMinus += tr.result;
                                                                totalMinus += tr.result*100 / (order.signal.range * Math.pow(10, cour.digitsAfterZero));
                                                                hoursMinus += Courency.getHourFormDate(tr.dateClose) + 1;
                                                                minusTransCount++;
                                                            } else {
//                                                                totalPlus += tr.result;
                                                                totalPlus += tr.result*100 / (order.signal.range * Math.pow(10, cour.digitsAfterZero));
                                                                hoursPlus += Courency.getHourFormDate(tr.dateClose) + 1;
                                                                plusTransCount++;
                                                            }
                                                            transCount++;
                                                        }
                                                        totalOrderLikeRange += order.result / (order.signal.range * Math.pow(10, cour.digitsAfterZero));
                                                        strMinMax += order.min+"="+order.minHours+"/"+orderMax+"="+order.maxHours+";";
                                                    }

                                                    //72%(16=8-10)(8-10)
                                                    double resultProc = (1 + totalMinus / (totalPlus - totalMinus)) * 100;
                                                    String statistic = new Formatter().format("%.0f%s(%d=%d-%d)(%d-%d)",
                                                            resultProc,
                                                            "%",
                                                            cour.orders.size(),
                                                            plusOrderCount,
                                                            minusOrderCount,
                                                            plusTransCount,
                                                            minusTransCount).toString();

                                                    String signalName = new Formatter().format("%s,%d,%d,%d,%d,%s,%s,%s",
                                                            strPeriod, n, i, j, k,bufStr, bufStr2, statistic).toString();

                                                    if ((resultProc >= 66) && (transCount > 0) &&
                                                            (cour.orders.size()>= Integer.parseInt(mainFrame.textMinEmulOrderCount.getText()))) {
                                                        //System.out.println(-totalMinus / (totalPlus - totalMinus));
                                                        int middleOrderResultCoef = (int) ((totalPlus + totalMinus) / cour.orders.size() -
                                                                (totalPlus + totalMinus) / cour.orders.size() % 4) / 4;

                                                        Object[] objArr = new Object[]{   //-totalMinus / totalPlus * 100
                                                                cour.name+"("+cour.rateUSD+")",
                                                                i,
                                                                j,
                                                                k,
                                                                orderMax,
                                                                orderMin,
                                                                new Formatter().format("%d (%d/%d)", (int) totalPlus, plusTransCount, (int) hoursPlus).toString(),
                                                                new Formatter().format("%.2f", totalPlus / hoursPlus).toString(),
                                                                new Formatter().format("%d (%d/%d)", (int) totalMinus, minusTransCount, (int) hoursMinus).toString(),
                                                                new Formatter().format("%.2f", totalMinus / hoursMinus).toString(),
                                                                (int) (totalPlus+totalMinus),
                                                                (int)(hoursMinus + hoursPlus),
                                                                new Formatter().format("%.1f", totalPlus/hoursPlus).toString(),
                                                                middleOrderResultCoef,
                                                                new Formatter().format("%.2f", resultProc).toString(),
                                                                cour.orders.size(),
                                                                new Formatter().format("%.2f", (totalPlus + totalMinus) / cour.orders.size()).toString(),
                                                                new Formatter().format("%.4f", totalOrderLikeRange / cour.orders.size()).toString(),
                                                                strMinMax,
                                                                signalName
                                                        };

                                                        String keyString = strPeriod+","+n+","+i+","+bufStr2;
                                                        if (!findBestCoefMap.containsKey(keyString)){
                                                            findBestCoefMap.put(keyString,resultProc); //resultProc
                                                            bufTree.put(-resultProc, objArr); //resultProc
                                                        }else{
                                                            Double findProc = findBestCoefMap.get(keyString);
                                                            if (findProc < resultProc){ //resultProc
                                                                findBestCoefMap.put(keyString,resultProc); //resultProc
                                                                if (bufTree.containsKey(-findProc)) {
                                                                    bufTree.remove(-findProc);
//                                                                } else {
//                                                                    System.out.println("don't found"+(-findProc));
                                                                }
                                                                bufTree.put(-resultProc,objArr);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        System.out.println();
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                }
                Object[] keyArr = bufTree.keySet().toArray();
                for (int i = 0; i < keyArr.length; i++){
                    bufArr.add(bufTree.get(keyArr[i]));
                }
            }else if (typeEmulate.equals("Middle 15 day calm")){
//                for (int i=3; i<10; i++){   //calmDays
//                    for (int j=5; j<30; j=j+2){     //+2 calmRange
//                        for (int k=10; k<50; k=k+3){    //+5 startRange
//                            cour.fillSignalsMiddle15Calm(i,j,k);
//                            cour.run();
//                            double bufVal = cour.result;
//
//                            if (bufVal > maxValue){
//                                maxValue = bufVal;
//                                System.out.println("\t"+i+"\t"+j+"\t"+k+"\t"+cour.round(cour.result));
//                            }
//                        }
//                    }
//                }
            }
        }

        if (bufHeader != null) {
            mainFrame.fillTable(bufHeader,bufArr);
        }
    }

    void printOrders(){
        Object[] tableHeader  = new Object[12];
        ArrayList<Object[]> tableArray = new ArrayList<>();

        for (Courency cour : courArr) {
            if (cour.orders.size()==0){
                continue;
            }

            int daysTransaction = 0;
            TreeMap<Integer,Double[]> treeNumResult = new TreeMap<>();
            TreeMap<Integer,String> treeNumName = new TreeMap<>();
            for (Order curOrder : cour.orders) {
                //order head
                tableArray.add(curOrder.toArrayList());
                double  transPlusCount = 0.0,
                        transMinusCount = 0.0,
                        orderPlus = 0.0,
                        orderMinus = 0.0,
                        hoursTransPlus = 0.0,
                        hoursTransMinus = 0.0;
                //transactions lines
                for (Transaction curTrans : curOrder.trans){
                    tableArray.add(curTrans.toArrayList());
                    daysTransaction++;
                    if (curTrans.result>0){
                        transPlusCount++;
                        orderPlus+=curTrans.result;
                    }else{
                        transMinusCount++;
                        orderMinus+=curTrans.result;
                    }

                    //01.01.2020 12:00
                    double hours = Double.parseDouble(cour.getDateTime(curTrans.dateClose,1).substring(9,11))+1.0;
                    if (curOrder.result > 0){
                        hoursTransPlus += hours;
                    }else{
                        hoursTransMinus += hours;
                    }
                }

                Integer bufKey = Integer.parseInt(curOrder.signal.name.split("-")[0]);

                if (treeNumResult.containsKey(bufKey)){

                    Double[] bufArr = treeNumResult.get(bufKey);
                    bufArr[0]+= curOrder.result;
                    bufArr[1]++;
                    bufArr[2]+= (curOrder.result>0) ? 1:0;
                    bufArr[3]+= (curOrder.result<0) ? 1:0;
                    bufArr[4]+= orderPlus;
                    bufArr[5]+= orderMinus;
                    bufArr[6]+= transPlusCount;
                    bufArr[7]+= transMinusCount;
                    bufArr[8]+= hoursTransPlus;
                    bufArr[9]+= hoursTransMinus;
                    treeNumResult.put(bufKey,bufArr);
                }else{
                    //totalOrderResult,countTypeOrder,countPlusOrder,countMinusOrder,volumTransPlus,
                    //volumeTransMinus,countTransPlus,countTransMinus,hoursTransPlus,hoursTransMinus
                    treeNumName.put(bufKey,curOrder.signal.name);
                    treeNumResult.put(
                            bufKey,
                            new Double[]{
                                curOrder.result,
                                1.0,
                                (curOrder.result>0)? 1.0:0.0,
                                (curOrder.result<0) ? 1.0:0.0,
                                orderPlus,
                                orderMinus,
                                transPlusCount,
                                transMinusCount,
                                hoursTransPlus,
                                hoursTransMinus
                            }
                    );
                }
            }

            Date startPeriod = cour.getDate(mainFrame.arrPeriodDate.get(0));
            Date endPeriod = cour.getDate(mainFrame.arrPeriodDate.get(1));
            int daysPeriod =0;
            for (Date dt : cour.rates1D.keySet()){
                if ((dt.compareTo(startPeriod)>=0)&&(dt.compareTo(endPeriod)<=0)){
                    daysPeriod++;
                }
            }

            //total order total sum
            tableArray.add(new Object[]{null,null,cour.orders.size(),mainFrame.financeDouble(cour.result),
                    null,null,null,null,null,null,
                    new Formatter().format("days=%d  trans=%d (%.2f)",
                            daysPeriod,daysTransaction,(double)daysTransaction/daysPeriod*100).toString()
            });

            //downline order result
            for (Integer key : treeNumResult.keySet()){
                Double[] curBuf = treeNumResult.get(key);
                int perc = (int)cour.round(curBuf[4]/(curBuf[4]-curBuf[5])*100,0);
                tableArray.add(new Object[]{
                        null,
                        treeNumName.get(key),
                        new Formatter().format(" %.0f = +%.0f(%.1f)-%.0f(%.1f)",
                                curBuf[1],
                                curBuf[2],curBuf[8]/curBuf[2],
                                curBuf[3],curBuf[9]/curBuf[3]).toString(),
                        new Formatter().format("%.2f",curBuf[0]).toString(),
                        new Formatter().format("%.2f",curBuf[0]/curBuf[1]).toString(),
                        new Formatter().format("%.0f(%.0f)",curBuf[4],curBuf[6]).toString(),
                        new Formatter().format("%.0f(%.0f)",curBuf[5],curBuf[7]).toString(),
                        ""+perc+"%"
                });
            }
            tableArray.add(new Object[]{});

            mainFrame.fillTable(tableHeader,tableArray);
        }
    }
    void printRates(){
        readFiles();
        Object[] tableHeader  = new String[] {"Name","Rate name","Date","Open","Max","Min","Close","RANGE"};
        ArrayList<Object[]> tableArray = new ArrayList<>();

        class innerPrinter{
            void print(Courency cour, TreeMap<Date,Rate1D> rates, String nameRates){
                for (Date dt : rates.keySet()) {
                    Rate1D curRate = rates.get(dt);
                    tableArray.add(new Object[]{
                            cour.name,
                            nameRates,
                            cour.getDateTime(dt, 2),
                            curRate.open,
                            curRate.max,
                            curRate.min,
                            curRate.close,
                            new Formatter().format("%.4f - %.4f = %.4f",
                                    curRate.up,curRate.down,curRate.up-curRate.down).toString()
                    });
                    for (Date dt2 : curRate.rate1H.keySet()){
                        Rate curRate1H = curRate.rate1H.get(dt2);
                        tableArray.add(new Object[]{
                                cour.name,
                                "",
                                cour.getDateTime(dt2, 1),
                                curRate1H.open,
                                curRate1H.max,
                                curRate1H.min,
                                curRate1H.close,
                                ""
                        });
                    }
                }
            }
        }

        for (Courency cour : courArr) {
            if (mainFrame.cbSignal_0AM.isSelected()){
                new innerPrinter().print(cour,cour.rates1D,"rate 1D");
            }
            if (mainFrame.cbSignal_7AM.isSelected()){
                new innerPrinter().print(cour,cour.rates1D7,"rate 1D_7");
            }
            if (mainFrame.cbSignal_13AM.isSelected()){
                new innerPrinter().print(cour,cour.rates1D13,"rate 1D_13");
            }
            if (mainFrame.cbSignal_23AM.isSelected()){
                new innerPrinter().print(cour,cour.rates1D23,"rate 1D_23");
            }
        }
        mainFrame.fillTable(tableHeader,tableArray);
    }
    void printResultTree() {
        String[] tableHeader = new String[courArr.size()+3];
        ArrayList<Object[]> tableArray = new ArrayList<>();
        String elem = (String) mainFrame.comboPrintTypeTree.getSelectedItem();

        tableHeader[0] = "DATE";
        tableHeader[courArr.size()+1] = "TOTAL";
        tableHeader[courArr.size()+2] = "BALANCE";
        for (int i=0; i < courArr.size(); i++) {
            Courency cour = courArr.get(i);
            tableHeader[i+1] = cour.name;
        }

        TreeSet<Date> dateSet = new TreeSet<>();
        for (Courency cour : courArr){
            switch (elem){
                case "Year"     :dateSet.addAll(cour.yearTree.keySet()); break;
                case "Month"    :dateSet.addAll(cour.monthTree.keySet()); break;
                case "Day"      :dateSet.addAll(cour.rates1D.keySet()); break;
                case "Day time" :dateSet.addAll(cour.dateTimeTree.keySet()); break;
            }
        }

        double  totalResult = Integer.parseInt(mainFrame.textStartBalance.getText());


        Date startPeriod = null, endPeriod = null;
        try {
            if (elem == "Month") {
                startPeriod = Courency.getDate(mainFrame.arrPeriodDate.get(0),"beginMonth");
            }else {
                startPeriod = Courency.getDate(mainFrame.arrPeriodDate.get(0));
            }
            endPeriod = Courency.getDate(mainFrame.arrPeriodDate.get(1));
        }catch (Exception ex){
            ex.printStackTrace();
        }

        String strDay = "";
        double startBalance = totalResult;
        for (Date dt: dateSet){
            if ((dt.compareTo(startPeriod)<0)||
                ((dt.compareTo(endPeriod)>0)&&(elem!="Day time"))){
                continue;
            }
            Object[] rowArr = new Object[courArr.size()+3];

            double total=0.0;

            for (int j=0; j< courArr.size(); j++){
                TreeMap<Date,Double> bufTree = null;
                switch (elem){
                    case "Year"     :bufTree = courArr.get(j).yearTree; break;
                    case "Month"    :bufTree = courArr.get(j).monthTree; break;
                    case "Day"      :bufTree = courArr.get(j).dayTree; break;
                    case "Day time" :bufTree = courArr.get(j).dateTimeTree; break;
                }
                if (bufTree.containsKey(dt)){
                    rowArr[j+1] = new Formatter().format("%.2f",bufTree.get(dt)).toString();
                    total += bufTree.get(dt);
                }else{
                    rowArr[j+1] = "";
                }
            }
            rowArr[courArr.size()+1] = new Formatter().format("%.2f",total).toString();
            if (elem == "Day time") {
                rowArr[0] = Courency.getDateTime(dt, 1);
                if (Courency.getDateTime(dt, 2).compareTo(strDay) != 0){
                    tableArray.add(new Object[courArr.size()+3]);
                    startBalance = totalResult;
                }
                totalResult = startBalance + total;
            }else{
                rowArr[0] = Courency.getDateTime(dt,2);
                totalResult += total;
            }
            rowArr[courArr.size()+2] = new Formatter().format("%.2f",totalResult).toString();
            tableArray.add(rowArr);

            strDay = Courency.getDateTime(dt,2);
        }

        Object[] rowArr = new Object[courArr.size()+3];
        rowArr[0]= "TOTAL:";
        for (int j=0; j < courArr.size(); j++){
            Courency buf = courArr.get(j);
            rowArr[j+1]= new Formatter().format("%.2f (%d)",buf.result,buf.orders.size()).toString();
        }
        rowArr[courArr.size()+1]= new Formatter().format("%.2f",
                (totalResult - Integer.parseInt(mainFrame.textStartBalance.getText()))).toString() ;
        rowArr[courArr.size()+2]= new Formatter().format("%.2f",totalResult).toString();
        tableArray.add(rowArr);

        mainFrame.fillTable(tableHeader,tableArray);
    }
    void printSignals(){
        readFiles();
        fillSignals(false);

        Object[] tableHeader  = new String[] {"Courency", "Date", "Signal","Open","TP","TP vol","SL","SL vol","Open vol",""};
        ArrayList<Object[]> tableArray = new ArrayList<>();

        for (Courency cour : courArr) {
            for (Date dt : cour.signalMap.keySet()) {
                Signal curSignal = cour.signalMap.get(dt);

                String rangeStr = "";
                if (cour.rates1D.containsKey(dt)) {
                    rangeStr = new Formatter().format("%.4f - %.4f",
                            cour.rates1D.get(dt).up,
                            cour.rates1D.get(dt).down).toString();
                }
                String strDigits = Integer.toString(cour.digitsAfterZero);
//                System.out.println(curSignal);
                tableArray.add(new Object[]{
                        cour.name + " (" + cour.rateUSD + ")",
                        cour.getDateTime(dt, 1),
                        //rangeStr,
                        curSignal.name,
                        new Formatter().format("%." + strDigits + "f",curSignal.open).toString(),
                        new Formatter().format("%." + strDigits + "f",curSignal.open + curSignal.tProfVolume*((curSignal.signalFlag==SignalFlag.SELL) ? -1:1)).toString(),
                        new Formatter().format("%d",(int) (curSignal.tProfVolume * Math.pow(10, cour.digitsAfterZero))).toString(),
                        new Formatter().format("%." + strDigits + "f",curSignal.open - curSignal.sLossVolume*((curSignal.signalFlag==SignalFlag.SELL) ? -1:1)).toString(),
                        new Formatter().format("%d",(int) (curSignal.sLossVolume * Math.pow(10, cour.digitsAfterZero))).toString(),
                        (int)(cour.spred * Math.pow(10,cour.digitsAfterZero)),
                        new Formatter().format("%."+strDigits+"f-%."+strDigits+"f-%."+strDigits+"f",
                                curSignal.spoonUp,
                                curSignal.spoonMiddle,
                                curSignal.spoonDown).toString()

                });
            }
        }
        mainFrame.fillTable(tableHeader,tableArray);
    }

    void lastSignal(String typeEmulate){
        readFiles();
        fillSignals(false);

        Object[] tableHeader  = new String[] {"Courency", "Date","Range","Signal","Open","TP","SL"};
        ArrayList<Object[]> tableArray = new ArrayList<>();
        Date endPeriod = Courency.getDate(mainFrame.arrPeriodDate.get(1));

        for (Courency cour : courArr) {
            ArrayList<Date> dateArr = new ArrayList<>();
            dateArr.addAll(cour.signalMap.keySet());

            Date lastDay = null;
            if (dateArr.size()==0){
                continue;
            }else{
                lastDay = dateArr.get(dateArr.size()-1);
            }
            String rangeStr = "";

            if (lastDay.compareTo(endPeriod)<=0) {

            } else {
                Signal curSignal = cour.signalMap.get(lastDay);
                tableArray.add(new Object[]{
                        cour.name + " (" + cour.rateUSD + ")",
                        cour.getDateTime(lastDay, 1),
                        rangeStr,
                        curSignal.name,
                        new Formatter().format("%.4f", curSignal.open).toString(),
                        new Formatter().format("%.4f(%.4f)", curSignal.open + curSignal.tProfVolume, curSignal.tProfVolume).toString(),
                        new Formatter().format("%.4f(%.4f)", curSignal.open - curSignal.sLossVolume, curSignal.sLossVolume).toString()
                });
            }
        }
        mainFrame.fillTable(tableHeader,tableArray);
    }
}