package ru.abch.acceptgoods;

public class GoodsPosition {
    String barcode, description, cell, id;
    int qnt;
    GoodsPosition(String id, String barcode, String description, String cell, int qnt){
        this.barcode = barcode;
        this.description = description;
        this.cell = cell;
        this.qnt = qnt;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public int getQnt() {
        return qnt;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getDescription() {
        return description;
    }

    public String getCell() {
        return cell;
    }
}
