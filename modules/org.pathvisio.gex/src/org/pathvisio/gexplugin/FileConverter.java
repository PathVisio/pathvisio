package org.pathvisio.gexplugin;


import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.pathvisio.core.util.FileUtils;


/**
 * File converter to convert xls,xlsx,csv and tsv files to tab delimited text files
 * in PathVisio
 */
public class FileConverter {
    static final String FILE_XLS = "xls";
    static final String FILE_CSV = "csv";
    static final String FILE_TSV = "tsv";
    static final String FILE_XLSX = "xlsx";
    static final String SHEET = "sheet";


    /*
    Check file type and assign it to suitable coverter
    */
public File convertFile(String fileName) throws IOException{
    String fileExtension=FileUtils.getExtension(fileName);
    File file=null;
   if (fileExtension.equals(FILE_XLS)){
      file= convertExcelToText(fileName);
   }else if(fileExtension.equals(FILE_CSV)){
       file=convertCsvToText(fileName);
   }else if(fileExtension.equals(FILE_TSV)){

   }else if(fileExtension.equals(FILE_XLSX)){

   }
return file;
}

    /*Convert excel files to tab delimited text files*/
    public File convertExcelToText(String filename) throws IOException {
        List<List<HSSFCell>> cellGrid=null;
        try {
             cellGrid = new ArrayList<List<HSSFCell>>();
             FileInputStream input = new FileInputStream(filename);
            POIFSFileSystem fileSystem = new POIFSFileSystem(input);
            HSSFWorkbook workBook = new HSSFWorkbook(fileSystem);
             HSSFSheet mySheet = workBook.getSheetAt(0);
            Iterator<?> rowIter = mySheet.rowIterator();

            while (rowIter.hasNext()) {
                HSSFRow row = (HSSFRow) rowIter.next();
                Iterator<?> cellIter = row.cellIterator();
                List<HSSFCell> cellRowList = new ArrayList<HSSFCell>();
                while (cellIter.hasNext()) {
                    HSSFCell cell = (HSSFCell) cellIter.next();
                    cellRowList.add(cell);
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
                String stringCellValue = myCell.toString().replaceAll(",","\t");
                stream.print(stringCellValue + "\t");
            }
            stream.println("");
        }
        return file;
    }

    /*Convert csv files to tab delimited text*/
    public File convertCsvToText(String filename) throws IOException {
        ArrayList<ArrayList<String>> allRowAndColData = null;
        ArrayList<String> oneRowData = null;
        String currentLine;
        FileInputStream fis = new FileInputStream(filename);
        DataInputStream myInput = new DataInputStream(fis);
        int count = 0;
        File convertExcelToText =null;
        try {
            allRowAndColData = new ArrayList<ArrayList<String>>();
            while ((currentLine = myInput.readLine()) != null) {
                oneRowData = new ArrayList<String>();
                String oneRowArray[] = currentLine.split(";");
                for (int j = 0; j < oneRowArray.length; j++) {
                    oneRowData.add(oneRowArray[j]);
                }
                allRowAndColData.add(oneRowData);
                count++;
            }

            HSSFWorkbook workBook = new HSSFWorkbook();
            HSSFSheet sheet = workBook.createSheet(SHEET);
            for (int i = 0; i < allRowAndColData.size(); i++) {
                ArrayList<?> ardata = (ArrayList<?>) allRowAndColData.get(i);
                HSSFRow row = sheet.createRow((short) 0 + i);
                for (int k = 0; k < ardata.size(); k++) {
                    HSSFCell cell = row.createCell((short) k);
                    cell.setCellValue(ardata.get(k).toString());
                }
            }
            String outFile=FileUtils.removeExtension(filename)+"_excel.xls";
            FileOutputStream fileOutputStream =  new FileOutputStream(outFile);
            workBook.write(fileOutputStream);
            fileOutputStream.close();
            convertExcelToText = convertExcelToText(outFile);
        } catch (Exception ex) {

        }
        return convertExcelToText;
    }
}
