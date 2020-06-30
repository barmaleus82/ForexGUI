import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;

public class Transaction {
    Courency courency;
    Date    dateOpen,
            dateClose;
    double  open,
            close,
            result,
            saldo;

    Transaction(Courency courency, Date dateOpen, double open) {
        this.courency = courency;
        this.dateOpen = dateOpen;
        this.open = open;
    }

    void closeTransaction(Date dateClose, double close, double result, double saldo){
        this.dateClose = dateClose;
        this.close = close;
        this.result = result;
        this.saldo = saldo;
    }

    public Object[] toArrayList(){
        try {
            String  strOpenSignal="",
                    strCloseSignal="";

            //00:00
            Date dayDate = new SimpleDateFormat("dd.MM.yy HH:mm").parse(courency.getDateTime(dateClose, 5));

            String comment="";
            if (courency.rates1D.containsKey(dayDate)) {
                Rate curRate = courency.rates1D.get(dayDate);
                String strRnd = Integer.toString(courency.digitsAfterZero);
                comment = new Formatter().format("%."+strRnd+"f=%."+strRnd+"f"+"(%.0f)-%."+strRnd+"f(%.0f)=%."+strRnd+"f(%.0f)",
                    curRate.open,
                    curRate.max,
                    (curRate.max-curRate.open) * Math.pow(10,courency.digitsAfterZero),
                    curRate.min,
                    (curRate.min-curRate.open) * Math.pow(10,courency.digitsAfterZero),
                    curRate.close,
                    (curRate.close-curRate.open) * Math.pow(10,courency.digitsAfterZero)).toString();
            }

            return new Object[]{
                    "", //courency.name,
                    " ",
                    strOpenSignal,
                    courency.getDateTime(dateOpen,1),
                    new Formatter().format("%."+courency.digitsAfterZero+"f",open).toString(),
                    courency.getDateTime(dateClose,1),
                    new Formatter().format("%."+courency.digitsAfterZero+"f",close).toString(),
                    new Formatter().format("%.2f",this.result).toString(),
                    new Formatter().format("%.2f",this.saldo).toString(),
                    strCloseSignal,
                    comment
            };
        }catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString(){
        Formatter f = new Formatter();
        f.format("\t\t%s\t%.6f\t%s\t%.6f;\tres=%.2f",
                Courency.getDateTime(dateOpen,1),
                open,
                Courency.getDateTime(dateClose,1),
                close,
                result
        );
        return f.toString();
    }
}
