package com.psr.financial.Utility;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import com.itextpdf.text.Document;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.psr.financial.CustomerDetailsActivity;
import com.psr.financial.Database.EmiDBManager;
import com.psr.financial.Models.CustomerBean;
import com.psr.financial.Models.EMIBean;
import com.psr.financial.R;
import com.psr.financial.Utilities;
import com.psr.financial.WebviewLayout;

import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ExportDatabaseCSVTask {

    Context context;

    public ExportDatabaseCSVTask(Context context) {
        this.context = context;
    }

    public void createExcelSheet(CustomerBean customer) {
        if (isFolderAlreadyCreated()) {
            //createNewExcelSheet(customer);
            showAlertDialogButtonClicked(customer, null);
        }
    }

    public void createExcelSheet(List<CustomerBean> customers) {
        if (isFolderAlreadyCreated()) {
            //createAllCustomerExcelSheet(customers);
            showAlertDialogButtonClicked(null, customers);
        }
    }
    public File filePath() {
        String path = File.separator + CustomerDetailsActivity.FOLDER_NAME;
        return new File(directoryPath() + path);
    }

    public File directoryPath() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
//        } else {
//            return Environment.getExternalStorageDirectory();
//        }
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
    }
    public boolean isFolderAlreadyCreated() {
        // File folder = new File(Environment.getExternalStorageDirectory() + File.separator + CustomerDetailsActivity.FOLDER_NAME);
        File file = filePath();
        // new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.d("App", "failed to create directory");
                return false;
            }
        }
        return true;
    }

    public void showAlertDialogButtonClicked(final CustomerBean customer, final List<CustomerBean> customers) {

        // create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select");

        // set the custom layout
        final View customLayout = ((Activity)context).getLayoutInflater().inflate(R.layout.custom_file_selection_layout, null);
        builder.setView(customLayout);

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });

        final AlertDialog dialog = builder.create();
        final ImageButton excelButton = (ImageButton) customLayout.findViewById(R.id.excel_button);
        excelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (customers != null) {
                    createAllCustomerExcelSheet(customers, true);
                } else {
                    createNewExcelSheet(customer, true);
                }
                dialog.dismiss();
            }
        });
        final ImageButton pdfButton = (ImageButton) customLayout.findViewById(R.id.pdf_button);
        pdfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //createNewExcelSheet(customer, false);
                if (customers != null) {
                    createAllCustomerExcelSheet(customers, false);
                } else {
                    createNewExcelSheet(customer, false);
                }
                dialog.dismiss();
            }
        });

        // create and show the alert dialog
        dialog.show();
    }

    /*void createNewExcelSheet(CustomerBean customer) {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet firstSheet = workbook.createSheet(customer.getName() + " Sheet");
        HSSFRow rowA = firstSheet.createRow(0);
        rowA.createCell(0).setCellValue(new HSSFRichTextString("Customer Id"));
        rowA.createCell(1).setCellValue(new HSSFRichTextString("Name"));
        rowA.createCell(2).setCellValue(new HSSFRichTextString("Phone"));
        rowA.createCell(3).setCellValue(new HSSFRichTextString("Place"));
        rowA.createCell(4).setCellValue(new HSSFRichTextString("Amount"));
        rowA.createCell(5).setCellValue(new HSSFRichTextString("Received"));
        rowA.createCell(6).setCellValue(new HSSFRichTextString("Balance"));
        //rowA.createCell(7).setCellValue(new HSSFRichTextString("Months"));

        int defaultHeaderSize = 7;
        Map<String, List<EMIBean>> dateByEmiBean = CustomerDetailsActivity.getEMiByDate(customer);
        for (int i=0;i<dateByEmiBean.size();i++) {
            Object firstKey = dateByEmiBean.keySet().toArray()[i];
            rowA.createCell(defaultHeaderSize+i).setCellValue(new HSSFRichTextString(String.valueOf(firstKey)));
        }

        List<HSSFRow> rows = new ArrayList<>();
        HSSFRow rowB = firstSheet.createRow(1);
        rows.add(rowB);
        rowB.createCell(0).setCellValue(new HSSFRichTextString(customer.getCustomerId()));
        rowB.createCell(1).setCellValue(new HSSFRichTextString(customer.getName()));
        rowB.createCell(2).setCellValue(new HSSFRichTextString(customer.getPhone()));
        rowB.createCell(3).setCellValue(new HSSFRichTextString(customer.getPlace()));
        rowB.createCell(4).setCellValue(new HSSFRichTextString(String.valueOf(customer.getAmount())));
        rowB.createCell(5).setCellValue(new HSSFRichTextString(String.valueOf(customer.getReceived())));
        rowB.createCell(6).setCellValue(new HSSFRichTextString(String.valueOf(customer.getBalance())));
        //rowB.createCell(7).setCellValue(new HSSFRichTextString(String.valueOf(customer.getMonths())));

        int lastRowSize = 0;
        for (int i=0;i<dateByEmiBean.size();i++) {
            Object firstKey = dateByEmiBean.keySet().toArray()[i];
            List<EMIBean> emiBeansTemps = dateByEmiBean.get(firstKey);
            lastRowSize = emiBeansTemps.size() > lastRowSize ? emiBeansTemps.size() : lastRowSize;
        }
        for (int i=0;i<lastRowSize;i++) {
            HSSFRow rowC = firstSheet.createRow(2 + i);
            rows.add(rowC);
        }

        for (int i=0;i<dateByEmiBean.size();i++) {
            Object firstKey = dateByEmiBean.keySet().toArray()[i];
            List<EMIBean> emiBeansTemps = dateByEmiBean.get(firstKey);
            for (int j=0;j<emiBeansTemps.size();j++) {
                rows.get(j).createCell(defaultHeaderSize+i).setCellValue(new HSSFRichTextString(String.valueOf(emiBeansTemps.get(j).getAmount())));
            }
        }

        FileOutputStream fos = null;
        String fileName = customer.getName() + customer.getCustomerId() + "_Finance" + ".xls";
        try {
            String filePath = Environment.getExternalStorageDirectory() + File.separator + CustomerDetailsActivity.FOLDER_NAME;
            File file ;
            file = new File(filePath, fileName);
            fos = new FileOutputStream(file);
            workbook.write(fos);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Toast.makeText(context, "Excel Sheet Generated", Toast.LENGTH_SHORT).show();

            String excelFile = (Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + CustomerDetailsActivity.FOLDER_NAME + File.separator + fileName);
            File file = new File(excelFile);
            shareFile(customer.getName(), file);
        }
    }*/

    void createAllCustomerExcelSheet(List<CustomerBean> customers, boolean isExcel) {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet firstSheet = workbook.createSheet( " All_Customer_Sheet");
        HSSFRow rowA = firstSheet.createRow(0);
        rowA.createCell(0).setCellValue(new HSSFRichTextString("Serial No"));
        rowA.createCell(1).setCellValue(new HSSFRichTextString("Created On"));
        rowA.createCell(2).setCellValue(new HSSFRichTextString("Name"));
        rowA.createCell(3).setCellValue(new HSSFRichTextString("Phone"));
        rowA.createCell(4).setCellValue(new HSSFRichTextString("Phone2"));
        rowA.createCell(5).setCellValue(new HSSFRichTextString("Place"));
        rowA.createCell(6).setCellValue(new HSSFRichTextString("Amount"));
        rowA.createCell(7).setCellValue(new HSSFRichTextString("Received"));
        rowA.createCell(8).setCellValue(new HSSFRichTextString("Balance"));
        rowA.createCell(9).setCellValue(new HSSFRichTextString("Last Payment Date"));
        rowA.createCell(10).setCellValue(new HSSFRichTextString("Last Payment Amount"));

        List<HSSFRow> rows = new ArrayList<>();
        for (int i=0;i<customers.size();i++) {
            HSSFRow rowB = firstSheet.createRow(1 + i);
            rows.add(rowB);
        }

        for (int i=0;i<customers.size();i++) {
            CustomerBean customer = customers.get(i);
            rows.get(i).createCell(0).setCellValue(new HSSFRichTextString(String.valueOf(i+1)));
            rows.get(i).createCell(1).setCellValue(new HSSFRichTextString(Utilities.getDateStringFromTimeStamp(customer.getCreatedOn(), null)));
            rows.get(i).createCell(2).setCellValue(new HSSFRichTextString(customer.getName()));
            rows.get(i).createCell(3).setCellValue(new HSSFRichTextString(customer.getPhone()));
            rows.get(i).createCell(4).setCellValue(new HSSFRichTextString(customer.getPhone2()));
            rows.get(i).createCell(5).setCellValue(new HSSFRichTextString(customer.getPlace()));
            rows.get(i).createCell(6).setCellValue(new HSSFRichTextString(String.valueOf(customer.getAmount())));
            rows.get(i).createCell(7).setCellValue(new HSSFRichTextString(String.valueOf(customer.getReceived())));
            rows.get(i).createCell(8).setCellValue(new HSSFRichTextString(String.valueOf(customer.getBalance())));

            if (customer.getLastPayment() != null) {
                rows.get(i).createCell(9).setCellValue(new HSSFRichTextString(Utilities.getDateStringFromTimeStamp(customer.getLastPayment().getDate(), null)));
                rows.get(i).createCell(10).setCellValue(new HSSFRichTextString(String.valueOf(customer.getLastPayment().getAmount())));
            }
        }

        FileOutputStream fos = null;
        String fileName = "All_Customer_Finance_Details" + ".xls";
        try {
            String filePath = filePath().getAbsolutePath();
            // Environment.getExternalStorageDirectory() + File.separator + CustomerDetailsActivity.FOLDER_NAME;
            File file ;
            file = new File(filePath, fileName);
            fos = new FileOutputStream(file);
            workbook.write(fos);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Toast.makeText(context, "Excel Sheet Generated", Toast.LENGTH_SHORT).show();

            String excelFile = filePath().getAbsolutePath() + File.separator + fileName;
            // (Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + CustomerDetailsActivity.FOLDER_NAME + File.separator + fileName);
            File file = new File(excelFile);
            if (isExcel) {
                showExcelViewOption("All Customer", file);
                //openExcelWebView("All Customer", fileName);
            } else {
                try {
                    excel2pdf(file, "All Customer");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void createNewExcelSheet(CustomerBean customer, boolean isExcel) {
        EmiDBManager emiDBManager = new EmiDBManager(context);
        emiDBManager.open();
        List<EMIBean> emis = emiDBManager.getCustomerEmis(customer.getCustomerId());
        customer.setEmi(emis);

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet firstSheet = workbook.createSheet(customer.getName() + " Sheet");

        List<HSSFRow> rows = new ArrayList<>();
        HSSFRow rowA = firstSheet.createRow(0);
        rows.add(rowA);
        rowA.createCell(0).setCellValue(new HSSFRichTextString("Serial No"));
        rowA.createCell(1).setCellValue(new HSSFRichTextString("Created On"));
        rowA.createCell(2).setCellValue(new HSSFRichTextString("Name"));
        rowA.createCell(3).setCellValue(new HSSFRichTextString("Phone"));
        rowA.createCell(4).setCellValue(new HSSFRichTextString("Phone2"));
        rowA.createCell(5).setCellValue(new HSSFRichTextString("Place"));
        rowA.createCell(6).setCellValue(new HSSFRichTextString("Amount"));
        rowA.createCell(7).setCellValue(new HSSFRichTextString("Received"));
        rowA.createCell(8).setCellValue(new HSSFRichTextString("Balance"));

        HSSFRow rowB = firstSheet.createRow(1);
        rows.add(rowB);
        rowB.createCell(0).setCellValue(new HSSFRichTextString(String.valueOf(1)));
        rowB.createCell(1).setCellValue(new HSSFRichTextString(Utilities.getDateStringFromTimeStamp(customer.getCreatedOn(), null)));
        rowB.createCell(2).setCellValue(new HSSFRichTextString(customer.getName()));
        rowB.createCell(3).setCellValue(new HSSFRichTextString(customer.getPhone()));
        rowB.createCell(4).setCellValue(new HSSFRichTextString(customer.getPhone2()));
        rowB.createCell(5).setCellValue(new HSSFRichTextString(customer.getPlace()));
        rowB.createCell(6).setCellValue(new HSSFRichTextString(String.valueOf(customer.getAmount())));
        rowB.createCell(7).setCellValue(new HSSFRichTextString(String.valueOf(customer.getReceived())));
        rowB.createCell(8).setCellValue(new HSSFRichTextString(String.valueOf(customer.getBalance())));

        rowB = firstSheet.createRow(2);
        rows.add(rowB);
        Map<String, List<EMIBean>> dateByEmiBean = CustomerDetailsActivity.getEMiByDate(customer);
        for (int i=0;i<dateByEmiBean.size();i++) {
            rowB = firstSheet.createRow(3+i);
            rows.add(rowB);
        }

        rows.get(2).createCell(0).setCellValue(new HSSFRichTextString(""));
        for (int i=0;i<dateByEmiBean.size();i++) {
            Object firstKey = dateByEmiBean.keySet().toArray()[i];
            rows.get(3+i).createCell(0).setCellValue(new HSSFRichTextString(String.valueOf(firstKey)));

            double totalAmount = 0.0;
            for (int col=0;col<dateByEmiBean.get(firstKey).size();col++) {
                totalAmount = totalAmount + dateByEmiBean.get(firstKey).get(col).getAmount();
            }
            rows.get(3+i).createCell(1).setCellValue(new HSSFRichTextString(String.valueOf(totalAmount)));
//            if (dateByEmiBean.get(firstKey).size()>1) {
//                rows.get(3 + i).createCell(2).setCellValue(new HSSFRichTextString(" "));
//                for (int col = 0; col < dateByEmiBean.get(firstKey).size(); col++) {
//                    rows.get(3 + i).createCell(col + 3).setCellValue(new HSSFRichTextString(String.valueOf(dateByEmiBean.get(firstKey).get(col).getAmount())));
//                }
//            }
        }

        FileOutputStream fos = null;
        String fileName = customer.getName().replace(" ", "") + customer.getCustomerId() + "_Finance" + ".xls";
        try {
            String filePath = filePath().getAbsolutePath(); // Environment.getExternalStorageDirectory() + File.separator + CustomerDetailsActivity.FOLDER_NAME;
            File file ;
            file = new File(filePath, fileName);
            fos = new FileOutputStream(file);
            workbook.write(fos);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Toast.makeText(context, "Excel Sheet Generated", Toast.LENGTH_SHORT).show();

            String excelFile = filePath().getAbsolutePath() + File.separator + fileName;
            // (Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + CustomerDetailsActivity.FOLDER_NAME + File.separator + fileName);
            File file = new File(excelFile);

            if (isExcel) {
                showExcelViewOption(customer.getName(), file);
                //openExcelWebView(customer.getName(), fileName);
            } else {
                String name = customer.getName().replace(" ", "") + customer.getCustomerId();
                try {
                    excel2pdf(file, name);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void openExcelWebView(String name, String fileName) {
        Intent i = new Intent(context, WebviewLayout.class);
        i.putExtra("name", name);
        i.putExtra("fileName", fileName);
        context.startActivity(i);
    }

    public void showExcelViewOption(final String name, final File file) {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        //builder.setTitle("Choose an animal");

        // add a radio button list
        final String[] collects = context.getResources().getStringArray(R.array.excelViewOption);
        builder.setItems(collects, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // View
                        viewExcelFile(file);
                        break;
                    case 1: // Share
                        shareExcelFile(name, file);
                        break;
                }
            }
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void shareExcelFile(String name, File file) {
        //System.out.println("file://"+file.getAbsolutePath());
        Intent intentShareFile = new Intent(Intent.ACTION_SEND);

        intentShareFile.setType(URLConnection.guessContentTypeFromName(file.getName()));
        if (Build.VERSION.SDK_INT < 24) {
            intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + file.getAbsolutePath()));
        } else {
            Uri apkURI = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
            //intentShareFile.setDataAndType(apkURI, "application/xls");
            intentShareFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intentShareFile.putExtra(Intent.EXTRA_STREAM, apkURI);
        }
        //if you need
        intentShareFile.putExtra(Intent.EXTRA_SUBJECT, name + " Finance Details");
        intentShareFile.putExtra(Intent.EXTRA_TEXT, "World Finance Team");

        context.startActivity(Intent.createChooser(intentShareFile, "Share File"));
    }

    public void viewExcelFile(File inputFile) {
        String mFilePath = inputFile.getAbsolutePath();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            File file=new File(mFilePath);
            Uri uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            intent.setDataAndType(uri, "application/vnd.ms-excel");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(intent);
        } else {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse("file://" + inputFile.getAbsolutePath()), "application/vnd.ms-excel");
            intent = Intent.createChooser(intent, "Open File");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    public void excel2pdf(File file, String name) throws Exception {

        String fileName = name + "_Finance" + ".pdf";
        String filePath = filePath().getAbsolutePath();
        // Environment.getExternalStorageDirectory() + File.separator + CustomerDetailsActivity.FOLDER_NAME;
        File outputFile = new File(filePath, fileName);

        //First we read the Excel file in binary format into FileInputStream

        FileInputStream input_document = new FileInputStream(file); //new File("C:\\excel_to_pdf.xls"));
        // Read workbook into HSSFWorkbook
        HSSFWorkbook my_xls_workbook = new HSSFWorkbook(input_document);
        // Read worksheet into HSSFSheet
        HSSFSheet my_worksheet = my_xls_workbook.getSheetAt(0);
        // To iterate over the rows
        Iterator<Row> rowIterator = my_worksheet.iterator();

        //We will create output PDF document objects at this point
        Document iText_xls_2_pdf = new Document();
        PdfWriter.getInstance(iText_xls_2_pdf, new FileOutputStream(outputFile)); //"Excel2PDF_Output.pdf"));
        iText_xls_2_pdf.open();
        //we have two columns in the Excel sheet, so we create a PDF table with two columns
        //Note: There are ways to make this dynamic in nature, if you want to.
        //PdfPTable my_table = new PdfPTable(2);
        //We will use the object below to dynamically add new data to the table

        //int width = 400;
        //float[] columnWidths = {width,width,width,width,width,width,width,width};
        PdfPTable my_table = new PdfPTable(9);
        if (name.equalsIgnoreCase("All Customer")) {
            my_table = new PdfPTable(11);
        }
        //my_table.setWidths(new int[]{width,width,width,width,width,width,width,width});
        //my_table.setTotalWidth(width*8);
        my_table.setWidthPercentage(100);
        PdfPCell table_cell;
        //Loop through rows.
        while(rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Iterator<Cell> cellIterator = row.cellIterator();
            while(cellIterator.hasNext()) {
                Cell cell = cellIterator.next(); //Fetch CELL
                switch(cell.getCellType()) { //Identify CELL type
                    //you need to add more code here based on
                    //your requirement / transformations
                    case Cell.CELL_TYPE_STRING:
                        //Push the data from Excel to PDF Cell
                        table_cell=new PdfPCell(new Phrase(cell.getStringCellValue()));
                        //feel free to move the code below to suit to your needs
                        my_table.addCell(table_cell);
                        break;
                }
                //next line
            }
            my_table.completeRow();
        }
        //Finally add the table to PDF document
        iText_xls_2_pdf.add(my_table);
        iText_xls_2_pdf.close();
        //we created our pdf file..
        input_document.close(); //close xls

        openPdfFile(outputFile);
    }

    public void openPdfFile(File inputFile) {
        String mFilePath = inputFile.getAbsolutePath();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            File file=new File(mFilePath);
            Uri uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(intent);
        } else {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse("file://" + inputFile.getAbsolutePath()), "application/pdf");
            intent = Intent.createChooser(intent, "Open File");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}