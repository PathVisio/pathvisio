package org.pathvisio.gexplugin;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.pathvisio.core.util.FileUtils;

public class FileConverter {

    private static List<List<HSSFCell>> cellGrid;

    public File convertExcelToText(String filename) throws IOException {
        try {
            cellGrid = new ArrayList<List<HSSFCell>>();
            // FileInputStream myInput = new FileInputStream(filename);
            FileInputStream myInput = new FileInputStream(new File(filename));
            XSSFWorkbook workbook = new XSSFWorkbook(myInput);
            XSSFSheet sheet = workbook.getSheetAt(0);
            //POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);
            //HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);
            // HSSFSheet mySheet = myWorkBook.getSheetAt(0);
            Iterator<?> rowIter = sheet.rowIterator();

            while (rowIter.hasNext()) {
                HSSFRow myRow = (HSSFRow) rowIter.next();
                Iterator<?> cellIter = myRow.cellIterator();
                List<HSSFCell> cellRowList = new ArrayList<HSSFCell>();
                while (cellIter.hasNext()) {
                    HSSFCell myCell = (HSSFCell) cellIter.next();
                    cellRowList.add(myCell);
                }
                cellGrid.add(cellRowList);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        File file = new File(FileUtils.removeExtension(filename)+".txt");

        PrintStream stream = new PrintStream(file);
        for (int i = 0; i < cellGrid.size(); i++) {
            List<HSSFCell> cellRowList = cellGrid.get(i);
            for (int j = 0; j < cellRowList.size(); j++) {
                HSSFCell myCell = (HSSFCell) cellRowList.get(j);
                String stringCellValue = myCell.toString();
                stream.print(stringCellValue + "\t");
            }
            stream.println("");
        }
        return file;
    }
}
