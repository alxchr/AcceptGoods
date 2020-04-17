package ru.abch.acceptgoods;

public class CheckCode {
    /*
    private static String scanPattern = "UI%%\\w{7}";
    private static String shortNumber = "\\d{4}";
    private static String tinyNumber ="\\d-\\d+";

     */
    private static String cellNumber = "19\\w+";
    private static String goodsCode = "28\\w+";
    private static String cell = "\\d+.\\d+";
    /*
    public static boolean checkCode(String code){
        return code.matches(scanPattern);
    }
    public static boolean checkShortNumber(String code){
        return code.matches(shortNumber);
    }
    public static boolean checkTinyNumber(String code){
        return code.matches(tinyNumber);
    }

     */
    public static boolean checkCell(String code){
        return code.matches(cellNumber);
    }
    public static boolean checkGoods(String code){
        return code.matches(goodsCode);
    }
    public static boolean checkCellStr(String code){
        return code.matches(cell);
    }
}
