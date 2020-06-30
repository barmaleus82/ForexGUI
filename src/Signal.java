import java.util.Formatter;

enum SignalFlag { BUY, SELL, CLOSE }

class Signal{
    SignalFlag signalFlag;
    String  name,
            fullName;
    int     serialNum;
    double  open,
            tProfVolume,
            sLossVolume,
            range,
            spoonUp,
            spoonMiddle,
            spoonDown,
            minBefore,
            maxBefore;

    Signal(SignalFlag fl, int serialNum, String name, String fullName, double open, double tProf, double sLoss,
           double range, double spoonUp, double spoonMidlle, double spoonDown, double minBefore, double maxBefore){
        this.signalFlag = fl;
        this.serialNum = serialNum;
        this.name = name;
        this.fullName = fullName;
        this.open = open;
        this.tProfVolume = tProf;
        this.sLossVolume = sLoss;
        this.range = range;
        this.spoonUp = spoonUp;
        this.spoonMiddle = spoonMidlle;
        this.spoonDown = spoonDown;
        this.minBefore = minBefore;
        this.maxBefore = maxBefore;
    }

    @Override
    public String toString(){
        return new Formatter().format(" %s open=%.4f tp=%.4f sl=%.4f range=%.4f",
                name,
                open,
                tProfVolume,
                sLossVolume,
                range).toString();
    }
}

