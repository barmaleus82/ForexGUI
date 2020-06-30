import java.text.SimpleDateFormat;
import java.util.*;

class Order implements Comparable<Order>{
    Courency courency;
    Signal  signal;
    int     hours,
            minHours,
            maxHours;
    double  tProf,
            sLoss,
            open,
            volume, //0.1-8
            result,
            min,
            max;
    String  strResultHours="";
    ArrayList<Transaction> trans = new ArrayList<>();

    Order(Signal signal, Date dateOpen, Courency courency, double volume){
        this.courency = courency;
        this.signal = signal;
        this.volume = volume;
        if (signal.signalFlag == SignalFlag.BUY){
            this.open = signal.open + courency.spred;
            this.tProf = signal.open + signal.tProfVolume;
            this.sLoss = signal.open - signal.sLossVolume;
        }else if (signal.signalFlag == SignalFlag.SELL){
            this.open = signal.open - courency.spred;
            this.tProf = signal.open - signal.tProfVolume;
            this.sLoss = signal.open + signal.sLossVolume;
        }

        openTransaction(courency, dateOpen, this.open);
    }

    void openTransaction(Courency courency, Date dateOpen, double open){
        trans.add(new Transaction(courency,dateOpen,open));
    }
    void closeTransaction(Date dateClose, double close){
        double res =0;
        if (signal.signalFlag == SignalFlag.BUY){
            res = close - trans.get(trans.size()-1).open;
        }else if (signal.signalFlag == SignalFlag.SELL){
            res = trans.get(trans.size()-1).open - close;
        }
        res = res * 10 * volume * Math.pow(10,courency.digitsAfterZero) * courency.rateUSD;
        Courency.balance += res;
        this.result += res;

        this.hours += Courency.getHourFormDate(dateClose)+1;
        trans.get(trans.size()-1).closeTransaction(dateClose,close,res,this.result);
    }

    public Object[] toArrayList(){
        String strDigits = Integer.toString(courency.digitsAfterZero);
        int tp = (int)courency.round((tProf - open)*Math.pow(10,courency.digitsAfterZero),2);
        int sl = (int)courency.round((open - sLoss)*Math.pow(10,courency.digitsAfterZero),2);

        try {
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yy");
            Date dateOrder = format.parse(Courency.getDateTime(trans.get(0).dateOpen, 1));
            if (courency.rates1D.containsKey(dateOrder)) {
                Rate1D curRate = courency.rates1D.get(dateOrder);
                return new Object[]{
                        courency.name+"("+courency.rateUSD+")",
                        signal.name,
                        "",
                        courency.getDateTime(trans.get(0).dateOpen,1),
                        "op="+courency.round(trans.get(0).open),
                        "tp="+courency.round(tProf),
                        "sl="+courency.round(sLoss),
                        "cl="+courency.round(trans.get(trans.size()-1).close),
                        "vol="+volume,
                        new Formatter().format("%.2f",result).toString(),
//                        new Formatter().format("tpv=%d, slv=%d (%."+strDigits+"f-%."+strDigits+"f-%."+strDigits+"f),"+
//                                        "minBefore =%."+strDigits+"f, maxBefor =%."+strDigits+"f",
//                                tp,
//                                sl,
//                                signal.spoonUp,
//                                signal.spoonMiddle,
//                                signal.spoonDown,
//                                signal.minBefore,
//                                signal.maxBefore).toString()
                        new Formatter().format("tpv=%d, slv=%d (%.0f/%d-%.0f/%d),",//+
                                        //"minBefore =%."+strDigits+"f, maxBefor =%."+strDigits+"f",
                                tp,
                                sl,
                                min,
                                minHours,
                                max,
                                maxHours
//                                signal.minBefore,
//                                signal.maxBefore
                        ).toString(),
                        strResultHours
                };
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString(){
        Formatter f = new Formatter();
        f.format("%s\t%s\top=%.4f\ttp=%.4f\tsl=%.4f\tcl=%.4f\tvol=%.1f\t%10.2f \n",
                signal.name,
                Courency.getDateTime(trans.get(0).dateOpen,1),
                trans.get(0).open,
                tProf,
                sLoss,
                trans.get(trans.size()-1).close,
                volume,
                result);
        for (Transaction curTrans : trans){
            f.format("%s \n",curTrans);
        }
        return f.toString();
    }

    @Override
    public int compareTo(Order that){
        Date    dtThis = this.trans.get(0).dateOpen,
                dtThat = that.trans.get(0).dateOpen;
        if ((dtThis.compareTo(dtThat))==0){
            return (int)(that.result*100-this.result*100);
        }else{
            return (dtThis.compareTo(dtThat));
        }
    }
}
