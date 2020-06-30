import java.util.Date;
import java.util.TreeMap;

class Rate1D extends Rate{
    TreeMap<Date,Rate> rate1H = new TreeMap<>();
    TreeMap<Date,Rate> rate15M = new TreeMap<>();
    double up, mid, down;

    Rate1D(double open, double max, double min, double close, double up, double mid, double down){
        super(open, max, min, close);
        this.up = up;
        this.mid = mid;
        this.down = down;
    }

    void addRate(String period, Date dt, double open, double max, double min, double close){
        switch (period){
            case ("1H") :
                rate1H.put(dt,new Rate(open, max, min, close));
                break;
            case ("15M"):
                rate15M.put(dt,new Rate(open, max, min, close));
                break;
        }
    }
}
