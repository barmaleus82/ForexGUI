import org.jdatepicker.impl.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.text.*;

class MainFrame extends JFrame{

    static final int DAY_MS = 86_400_000;

    RateFactory rateFactory;

    JPopupMenu popupSave;
    JPopupMenu popupLoad;

    ArrayList<JCheckBox> arrCheck = new ArrayList<JCheckBox>();
    ArrayList<JLabel> arrLabel = new ArrayList<JLabel>();
    ArrayList<JTextField>   arrFile = new ArrayList<JTextField>(),
                            arrCoef = new ArrayList<JTextField>(),
                            arrCoef_12AM = new ArrayList<JTextField>(),
                            arrComment = new ArrayList<JTextField>();

    ArrayList<JButton> arrButton = new ArrayList<JButton>();

    JTextField  textStartBalance = new JTextField(10);
    JTextField  textMinEmulOrderCount = new JTextField(5);

    ArrayList<JDatePickerImpl> arrPeriodDate = new ArrayList<JDatePickerImpl>();

    JCheckBox   cbThreadRun = new JCheckBox("Thread run"),
                cbTPJustOnSwap = new JCheckBox("TP on swap"),
                cbCutTPSLOnSwap = new JCheckBox("Cut TP/SL swap"),
                cb3Min5Max = new JCheckBox("3 minus/ 5 max"),
                cbSLdown = new JCheckBox("SL down"),
                cbCloseIfNewSignal = new JCheckBox("Close if new signal"),
                cbJustOneInTime = new JCheckBox("Оne signal in time"),
                cbSignal_0AM = new JCheckBox("00:00"),
                cbSignal_7AM = new JCheckBox("07:00"),
                cbSignal_13AM = new JCheckBox("13:00"),
                cbSignal_23AM = new JCheckBox("23:00");

    JComboBox comboEmulateType = new JComboBox(new String[] {"Spoon 2","Spoon 3"});
    JComboBox comboPrintTypeTree = new JComboBox(new String[] {"Year", "Month", "Day","Day time"});

    JScrollPane jScrollPanel;

    JProgressBar progressBar;
    int curProgressVolume = 0;
    int maxProgressVolume = 0;

    MainFrame(){
        super("Forex");
        this.rateFactory = new RateFactory(this);
        fillFrame();
    }
    void loadSettings(MainFrame mf, String suf){
        mf.rateFactory = new RateFactory(mf);
        try {
            String directoryName = new File("").getAbsolutePath()+"\\Save\\"+suf+"\\";
            FileReader sFile = new FileReader(directoryName+"settings.ini");
            Scanner scan = new Scanner(sFile);
            while (scan.hasNextLine()) {
                String bufStr = scan.nextLine();
                String data = bufStr.substring(bufStr.indexOf(":") + 1);
                if (bufStr.contains("Period")) {
                    String[] arrStr = data.split(";");
                    mf.arrPeriodDate.get(0).getJFormattedTextField().setText(arrStr[0]);
                    mf.arrPeriodDate.get(1).getJFormattedTextField().setText(arrStr[1]);
                } else if (bufStr.contains("Algorithm")) {
                    mf.comboEmulateType.setSelectedIndex(Integer.parseInt(data));
                } else if (bufStr.contains("PrintTree")) {
                    mf.comboPrintTypeTree.setSelectedIndex(Integer.parseInt(data));
                } else if (bufStr.contains("StartBalance")) {
                    mf.textStartBalance.setText(data);
                } else if (bufStr.contains("CheckBox")) {
                    String[] arrStr = data.split(";");
                    for(int i=0; i< arrStr.length; i++){
                        mf.arrCheck.get(i).setSelected(Boolean.parseBoolean(arrStr[i]));
                    }
                } else if (bufStr.contains("FileName")) {
                    String[] arrStr = data.split(";");
                    for(int i=0; i< arrStr.length; i++){
                        mf.arrFile.get(i).setText(arrStr[i]);
                        mf.arrLabel.get(i).setText("");
                    }
                } else if (bufStr.contains("Comment")) {
                    String[] arrStr = data.split(";");
                    for(int i=0; i< arrStr.length; i++){
                        mf.arrComment.get(i).setText(arrStr[i]);
                    }
                }else if(bufStr.contains("cbJustOneInTime")){
                    mf.cbJustOneInTime.setSelected(Boolean.parseBoolean(data));
                }else if(bufStr.contains("cbThreadRun")){
                    mf.cbThreadRun.setSelected(Boolean.parseBoolean(data));
                }else if(bufStr.contains("cbTPJustOnSwap")){
                    mf.cbTPJustOnSwap.setSelected(Boolean.parseBoolean(data));
                }else if(bufStr.contains("cbCutTPSLOnSwap")){
                    mf.cbCutTPSLOnSwap.setSelected(Boolean.parseBoolean(data));
                }else if(bufStr.contains("cbCutTPSLOnSwap")){
                    mf.cbCutTPSLOnSwap.setSelected(Boolean.parseBoolean(data));
                }else if(bufStr.contains("cbSLdown")){
                    mf.cbSLdown.setSelected(Boolean.parseBoolean(data));
                }else if(bufStr.contains("cb3Min5Max")){
                    mf.cb3Min5Max.setSelected(Boolean.parseBoolean(data));
                }else if(bufStr.contains("cbSignal_0AM")){
                    mf.cbSignal_0AM.setSelected(Boolean.parseBoolean(data));
                }else if(bufStr.contains("cbSignal_7AM")){
                    mf.cbSignal_7AM.setSelected(Boolean.parseBoolean(data));
                }else if(bufStr.contains("cbSignal_13AM")){
                    mf.cbSignal_13AM.setSelected(Boolean.parseBoolean(data));
                }else if(bufStr.contains("cbSignal_23AM")){
                    mf.cbSignal_23AM.setSelected(Boolean.parseBoolean(data));
                }
            }
            for (int i = 0; i < mf.arrCheck.size(); i++) {
                if (mf.arrFile.get(i).getText().trim().length()!=0) {
                    File file = new File(directoryName + mf.arrFile.get(i).getText() + ".ini");
                    if (file.exists()) {
                        scan = new Scanner(new FileReader(file));
                        String bufStr = "";
                        while (scan.hasNextLine()) {
                            String bufStr2 = scan.nextLine();
                            if (bufStr2.length()==0){
                                continue;
                            }else if (bufStr2.substring(0,1).equals("*")) {
                                continue;
                            }else{
                                bufStr += bufStr2 + ";";
                            }
                        }
                        mf.arrCoef.get(i).setText(bufStr);
                    }
                }
            }
            sFile.close();

        }catch (Exception ex){
            ex.printStackTrace();
            //JOptionPane.showMessageDialog(null, "Error load "+suf);
        }
    }
    void saveSettings(MainFrame mf, String suf){
        try {
            String directoryName = new File("").getAbsolutePath()+"\\Save\\"+suf+"\\";

            FileWriter sFile = new FileWriter(directoryName+"settings.ini");
            sFile.write("// Save settings " + new Date() + "\n");
            sFile.write("Period:" + mf.arrPeriodDate.get(0).getJFormattedTextField().getText() + ";"+
                    mf.arrPeriodDate.get(1).getJFormattedTextField().getText()+"\n");
            sFile.write("Algorithm:" + mf.comboEmulateType.getSelectedIndex() + "\n");
            sFile.write("PrintTree:" + mf.comboPrintTypeTree.getSelectedIndex() + "\n");
            sFile.write("StartBalance:" + mf.textStartBalance.getText() + "\n");

            sFile.write("cbJustOneInTime:" + mf.cbJustOneInTime.isSelected() + "\n");
            sFile.write("cbThreadRun:" + mf.cbThreadRun.isSelected() + "\n");
            sFile.write("cbTPJustOnSwap:" + mf.cbTPJustOnSwap.isSelected() + "\n");
            sFile.write("cbCutTPSLOnSwap:" + mf.cbCutTPSLOnSwap.isSelected() + "\n");
            sFile.write("cbSLdown:" + mf.cbSLdown.isSelected() + "\n");
            sFile.write("cb3Min5Max:" + mf.cb3Min5Max.isSelected() + "\n");
            sFile.write("cbSignal_0AM:" + mf.cbSignal_0AM.isSelected() + "\n");
            sFile.write("cbSignal_7AM:" + mf.cbSignal_7AM.isSelected() + "\n");
            sFile.write("cbSignal_13AM:" + mf.cbSignal_13AM.isSelected() + "\n");
            sFile.write("cbSignal_23AM:" + mf.cbSignal_23AM.isSelected() + "\n");
            sFile.write("cbCloseIfNewSignal:" + mf.cbCloseIfNewSignal.isSelected() + "\n");

            String bufStr = "CheckBox:";
            for (int i = 0; i < mf.arrCheck.size(); i++) {
                bufStr+=Boolean.toString(mf.arrCheck.get(i).isSelected())+";";
            }
            sFile.write(bufStr + "\n");

            bufStr = "FileName:";
            for (int i = 0; i < mf.arrFile.size(); i++) {
                bufStr+= mf.arrFile.get(i).getText()+";";
            }
            sFile.write(bufStr + "\n");

            bufStr = "Comment:";
            for (int i = 0; i < mf.arrFile.size(); i++) {
                bufStr+= mf.arrComment.get(i).getText()+";";
            }
            sFile.write(bufStr + "\n");

            sFile.close();
        }catch (Exception ex){
            JOptionPane.showMessageDialog(null, "Error save "+suf);
        }
    }
    void fillSaveLoadMenu(){
        popupLoad = new JPopupMenu();
        popupSave = new JPopupMenu();
        JMenuItem bufMenu;
        FrameActionListeter listeter = new FrameActionListeter();

        File[] directories = new File("Save").listFiles(
                new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.isDirectory();
                    }
                });
        for (int i=0; i< directories.length; i++){
            String dir = directories[i].getPath().substring(5,directories[i].getPath().length());

            bufMenu = new JMenuItem(dir);
            bufMenu.setActionCommand("menuSave:"+dir);
            bufMenu.addActionListener(listeter);
            popupSave.add(bufMenu);

            bufMenu = new JMenuItem(dir);
            bufMenu.setActionCommand("menuLoad:"+dir);
            bufMenu.addActionListener(listeter);
            popupLoad.add(bufMenu);
        }
    }

    static String financeDouble(double d){
        if ((int)d*100 == 0){
            return "";
        }else {
            return new DecimalFormat("###,###,###.00").format(d);
        }
    }

    public class DateLabelFormatter extends AbstractFormatter {
        private String datePattern = "dd.MM.yyyy";
        private SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern);

        @Override
        public Object stringToValue(String text) throws ParseException {
            Calendar cal = Calendar.getInstance();
            cal.setTime((Date) dateFormatter.parseObject(text));
            return cal;
        }

        @Override
        public String valueToString(Object value) throws ParseException {
            if (value != null) {
                Calendar cal = (Calendar) value;
                return dateFormatter.format(cal.getTime());
            }
            return "";
        }
    }

    public void setProgress(int value){
        if (value == 0){
            curProgressVolume = 0;
            maxProgressVolume = 0;
        }else if (value > 0){
            maxProgressVolume += value;
            if (value>1) {
                pack();
            }
        }else if (value < 0){
            curProgressVolume -= value;
        }

        if ((maxProgressVolume == 0)||
                (curProgressVolume == maxProgressVolume)){
            progressBar.setValue(0);
        }else {
            progressBar.setValue(curProgressVolume * 100 / maxProgressVolume);
        }
    }

    private void fillFrame(){
        FrameActionListeter listeter = new FrameActionListeter();
        //buttons
        JButton buttonLoad = new JButton("Load");
        JButton buttonSave = new JButton("Save");
        buttonLoad.setActionCommand("pressed_Load");
        buttonLoad.addActionListener(listeter);
        buttonSave.setActionCommand("pressed_Save");
        buttonSave.addActionListener(listeter);

        JButton buttonPrintTree = new JButton("Print Trees");
        buttonPrintTree.setActionCommand("pressed_PrintTree");
        buttonPrintTree.addActionListener(listeter);
        JButton buttonPrintOrders = new JButton("Print orders");
        buttonPrintOrders.setActionCommand("pressed_PrintOrders");
        buttonPrintOrders.addActionListener(listeter);
        JButton buttonPrintRates = new JButton("Print rates");
        buttonPrintRates.setActionCommand("pressed_PrintRates");
        buttonPrintRates.addActionListener(listeter);

        JButton buttonEmulate = new JButton("Emulate");
        JButton buttonCreate = new JButton("Create");
        JButton buttonCurrentSignal = new JButton("Last signal");
        JButton buttonPrintSignals = new JButton("Print signals");

        buttonEmulate.setActionCommand("pressed_Emulate");
        buttonEmulate.addActionListener(listeter);
        buttonCreate.setActionCommand("pressed_Create");
        buttonCreate.addActionListener(listeter);
        buttonCurrentSignal.setActionCommand("pressed_CurrentSignal");
        buttonCurrentSignal.addActionListener(listeter);
        buttonPrintSignals.setActionCommand("pressed_PrintSignals");
        buttonPrintSignals.addActionListener(listeter);

        progressBar = new JProgressBar();

        Box boxH, boxV;

        // -------- toolBar ----------------
        JToolBar toolBar = new JToolBar();
        toolBar.add(buttonLoad);
        toolBar.add(buttonSave);
        getContentPane().add(toolBar,BorderLayout.NORTH);

        //------------ fill SaveLoad menu ------------
        fillSaveLoadMenu();

        // ------------- Files -> CENTER -----------
        for (int i=0; i<14; i++){
            arrCheck.add(new JCheckBox(""));
            arrLabel.add(new JLabel(""));
            arrLabel.get(i).setPreferredSize(new Dimension(150,26));
            arrFile.add(new JTextField("",6));
            arrButton.add(new JButton("Select file"));
            arrCoef.add(new JTextField("",25));
            arrCoef_12AM.add(new JTextField("",25));
//            arrCoef1.add(new JTextField("",8));
//            arrCoef2.add(new JTextField("",8));
//            arrCoef3.add(new JTextField("",8));
//            arrCoef4.add(new JTextField("",8));
//            arrCoef5.add(new JTextField("",8));
            arrComment.add(new JTextField("",80));
        }

        boxV = Box.createVerticalBox();
        boxV.setBorder(new TitledBorder("Files with courency"));
        boxV.add(Box.createVerticalStrut(5));
        for (int i=0; i < arrCheck.size(); i++) {
            boxH = Box.createHorizontalBox();
            boxH.add(Box.createHorizontalStrut(5));
            boxH.add(arrCheck.get(i));
            boxH.add(Box.createHorizontalStrut(5));
            boxH.add(arrFile.get(i));
            boxH.add(Box.createHorizontalStrut(5));
            boxH.add(arrLabel.get(i));
            boxH.add(Box.createHorizontalStrut(5));
//            boxH.add(arrCoef.get(i));
//            boxH.add(Box.createHorizontalStrut(20));
//            boxH.add(arrCoef_12AM.get(i));
//            boxH.add(Box.createHorizontalStrut(20));

//            boxH.add(arrCoef1.get(i));
//            boxH.add(Box.createHorizontalStrut(5));
//            boxH.add(arrCoef2.get(i));
//            boxH.add(Box.createHorizontalStrut(5));
//            boxH.add(arrCoef3.get(i));
//            boxH.add(Box.createHorizontalStrut(5));
//            boxH.add(arrCoef4.get(i));
//            boxH.add(Box.createHorizontalStrut(5));
//            boxH.add(arrCoef5.get(i));
//            boxH.add(Box.createHorizontalStrut(5));
            boxH.add(arrComment.get(i));
            boxH.add(Box.createHorizontalStrut(5));
            boxH.add(arrButton.get(i));
            boxH.add(Box.createHorizontalStrut(5));

            boxV.add(boxH);
            boxV.add(Box.createVerticalStrut(5));
        }
        progressBar.setStringPainted(true);
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        boxV.add(progressBar);
        getContentPane().add(boxV,BorderLayout.CENTER);

        //-------------- Calendar + settings -> WEST -----
        JPanel panel = new JPanel();

        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");

        JDatePanelImpl datePanel;
        JDatePickerImpl datePicker;
        //date from
        datePanel = new JDatePanelImpl(new UtilDateModel(),p);
        datePicker = new JDatePickerImpl(datePanel,new DateLabelFormatter());
        datePicker.setTextEditable(true);
        datePicker.setPreferredSize(new Dimension(100,26));
        arrPeriodDate.add(datePicker);
        //date to
        datePanel = new JDatePanelImpl(new UtilDateModel(),p);
        datePicker = new JDatePickerImpl(datePanel,new DateLabelFormatter());
        datePicker.setTextEditable(true);
        datePicker.setPreferredSize(new Dimension(100,26));
        arrPeriodDate.add(datePicker);

        // Определение менеджера расположения
        Box boxVOuter = Box.createVerticalBox();
        boxVOuter.setBorder(new TitledBorder("Settings"));

        JPanel grid = new JPanel();
        grid.setBorder(new TitledBorder("Period"));
        grid.setLayout(new GridLayout(2, 2, 5, 5));
        JLabel bufLabel = new JLabel("Date from :");
        bufLabel.setHorizontalAlignment(JLabel.RIGHT);
        grid.add(bufLabel);
        grid.add(arrPeriodDate.get(0));
        bufLabel = new JLabel("to :");
        bufLabel.setHorizontalAlignment(JLabel.RIGHT);
        grid.add(bufLabel);
        grid.add(arrPeriodDate.get(1));

        boxVOuter.add(grid);

        grid = new JPanel();
        grid.setBorder(new TitledBorder("Emulate"));
        grid.setLayout(new GridLayout(5, 2, 5, 5));
        comboEmulateType.setPreferredSize(new Dimension(100,26));
        grid.add(comboEmulateType);
        grid.add(textStartBalance);
        grid.add(cbThreadRun);
        grid.add(cbTPJustOnSwap);
        grid.add(cbCutTPSLOnSwap);
        grid.add(cb3Min5Max);
        grid.add(cbSLdown);
        grid.add(cbCloseIfNewSignal);
        grid.add(cbJustOneInTime);
        grid.add(buttonCreate);
        boxVOuter.add(grid);

        grid = new JPanel();
        grid.setBorder(new TitledBorder("Signals"));
        grid.setLayout(new GridLayout(1, 4, 5, 5));
        grid.add(cbSignal_0AM);
        grid.add(cbSignal_7AM);
        grid.add(cbSignal_13AM);
        grid.add(cbSignal_23AM);
        boxVOuter.add(grid);

        grid = new JPanel();
        grid.setBorder(new TitledBorder("Orders"));
        grid.setLayout(new GridLayout(1, 2, 5, 5));
        grid.add(textMinEmulOrderCount);
        grid.add(buttonEmulate);
        boxVOuter.add(grid);

        grid = new JPanel();
        grid.setBorder(new TitledBorder("Print"));
        grid.setLayout(new GridLayout(3, 2, 5, 5));

        comboPrintTypeTree.setPreferredSize(new Dimension(100,26));
        grid.add(comboPrintTypeTree);
        grid.add(buttonPrintTree);

        grid.add(buttonPrintOrders);
        grid.add(buttonPrintSignals);
        grid.add(buttonPrintRates);
        grid.add(buttonCurrentSignal);

        boxVOuter.add(grid);

        getContentPane().add(boxVOuter,BorderLayout.WEST);
//        // ---------- end Calendar + settings ----------------

        fillTable(new Object[]{},new ArrayList<Object[]>());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950,600);
        pack();
        setVisible(true);
    }

    public void fillTable(Object[] columnsHeader, ArrayList<Object[]> bufArr){

        //transform array
        Object[][] array = new Object[bufArr.size()][columnsHeader.length];
        for (int i=0 ; i < bufArr.size(); i++){
            for (int j=0; j < bufArr.get(i).length; j++){
                array[i][j] = bufArr.get(i)[j];
            }
        }
        //find max width
        String[] maxColumStr = new String[columnsHeader.length];
        for (int i=0; i< columnsHeader.length; i++){
            String maxStr = (columnsHeader[i] == null) ? " " : columnsHeader[i].toString();
            for (int j=0; j < array.length; j++) {
                if ((array[j][i] != null) && (maxStr.length() < array[j][i].toString().length())) {
                    maxStr = array[j][i].toString();
                }
            }
            maxColumStr[i] = maxStr;
        }
        //transform array
        for (int i=0; i < array.length; i++){
            for (int j=0; j< columnsHeader.length; j++){
                if (array[i][j] != null) {
                    array[i][j] = new Formatter().format(" %" + maxColumStr[j].length() + "s ",
                            array[i][j].toString()).toString();
                }
            }
        }

        //-------------- table ----------------------
        if (jScrollPanel!=null) {
            getContentPane().remove(jScrollPanel);
        }

//        for (int i=0; i < array.length; i++){
//            for (int j=0; j < columnsHeader.length; j++) {
//                String buf;
//                if (array[i][j] != null){
//                    if (array[i][j].toString().contains(".")) {
//                        String[] bufStrArr = array[i][j].toString().split("\\.");
//                        if (bufStrArr.length == 2) {    //maybe double
//                            try {
////                                buf = financeDouble(Double.parseDouble(array[i][j].toString()));
//                                array[i][j] = new Formatter().format("%12s", buf).toString();
//                            } catch (Exception ex) {
//                                System.out.println(array[i][j].toString());
//                                //ex.printStackTrace();
//                            }
//                        }
//                    }else {
//                        buf = array[i][j].toString();
//                        array[i][j] = new Formatter().format("%" + (buf.replace(" ", "").length() + 3) + "s ", buf).toString();
//                    }
//                }
//            }
////            System.out.println();
//        }
        DefaultTableModel tableModel = new DefaultTableModel(array,columnsHeader);
        JTable dataTable = new JTable(tableModel);
        Font newFont = new Font("Courier New", Font.PLAIN, 12);
        dataTable.setFont(newFont);
        dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (int i=0; i < columnsHeader.length; i++) {
            TableColumn column = dataTable.getColumnModel().getColumn(i);
            JTableHeader th = dataTable.getTableHeader();
            FontMetrics fm = th.getFontMetrics(newFont);//th.getFont());
            column.setPreferredWidth(fm.stringWidth(maxColumStr[i])+20);
        }
        tableModel = new DefaultTableModel(array,columnsHeader);

        jScrollPanel = new JScrollPane(dataTable,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jScrollPanel.setPreferredSize(new Dimension(800, 300));
        getContentPane().add(jScrollPanel,BorderLayout.SOUTH);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER );
        dataTable.setDefaultRenderer(String.class,centerRenderer);

        pack();
        revalidate();
        repaint();
        setVisible(true);
        //------------- end table -------------------
    }

    private void selectFile(JTextField curFild) throws Exception{
        JFileChooser fileopen = new JFileChooser(new File(".").getAbsolutePath());
        int ret = fileopen.showDialog(null, "Open file");
        if (ret == JFileChooser.APPROVE_OPTION) {
            curFild.setText(fileopen.getSelectedFile().getAbsolutePath());
        }
    }

    class FrameActionListeter implements ActionListener{
        public void actionPerformed(ActionEvent event){
            String strEv = event.getActionCommand();
            if (strEv.contains("pressed_SelectFile")){  // 8 files
                try {
                    int numFile =Integer.parseInt(strEv.substring(17, strEv.length()));
                    selectFile(arrFile.get(numFile));
                }catch (Exception ex){
                    JOptionPane.showMessageDialog(null, "Error select file");
                }
            }else if (strEv.equals("pressed_Save")){
                fillSaveLoadMenu();
                try {
                    // Create JMenuItems
                    Component b = (Component)event.getSource();
                    Point p = b.getLocationOnScreen();
                    popupSave.show(MainFrame.this,0,0);
                    popupSave.setLocation(p.x,p.y+b.getHeight());
                }catch (Exception ex){
                    JOptionPane.showMessageDialog(null, "Error save file: "+ex.getMessage());
                }
            }else if (strEv.contains("menuSave")){
                String[] strArr = strEv.split(":");
                saveSettings(MainFrame.this,strArr[1]);
            }else if (strEv.equals("pressed_Load")){
                fillSaveLoadMenu();
                try {
                    // Create JMenuItems
                    Component b = (Component)event.getSource();
                    Point p = b.getLocationOnScreen();
                    popupLoad.show(MainFrame.this,0,0);
                    popupLoad.setLocation(p.x,p.y+b.getHeight());
                }catch (Exception ex){
                    JOptionPane.showMessageDialog(null, "Error load file: "+ex.getMessage());
                }
            }else if (strEv.contains("menuLoad")){
                String[] strArr = strEv.split(":");
                loadSettings(MainFrame.this,strArr[1]);
            }else if (strEv.equals("pressed_Emulate")){
                cbThreadRun.setSelected(false);
                rateFactory.emulateOrders();
            }else if (strEv.equals("pressed_Create")){
                rateFactory.createOrders();
            }else if (strEv.equals("pressed_PrintOrders")){
                rateFactory.printOrders();
            }else if (strEv.equals("pressed_CurrentSignal")){
                rateFactory.lastSignal((String)comboEmulateType.getSelectedItem());
            }else if (strEv.equals("pressed_PrintTree")){
                rateFactory.printResultTree();
            }else if (strEv.equals("pressed_PrintSignals")){
                rateFactory.printSignals();
            }else if (strEv.equals("pressed_PrintRates")){
                rateFactory.printRates();
            }
        }
    }

    public static void main(String[] args) {
        JFrame.setDefaultLookAndFeelDecorated(true);
        MainFrame frame = new MainFrame();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
