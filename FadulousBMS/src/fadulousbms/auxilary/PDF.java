package fadulousbms.auxilary;

import fadulousbms.managers.*;
import fadulousbms.model.*;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.PageDrawer;
import org.apache.pdfbox.rendering.PageDrawerParameters;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.Vector;

import javax.print.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterJob;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

/**
 * Created by ghost on 2017/02/10.
 */
public class PDF
{
    private static final String TAG = "PDF";
    private static final int LINE_HEIGHT=20;
    private static final int LINE_END = 300;
    private static final int TEXT_VERT_OFFSET=LINE_HEIGHT/4;
    private static final int ROW_COUNT = 35;
    private static final Insets page_margins = new Insets(100,10,100,10);
    private static int quote_page_count=1;

    public static void printPDF(final byte[] byteStream) throws PrintException
    {
        PrinterJob printerJob = PrinterJob.getPrinterJob();

        PrintService printService=null;
        if(printerJob.printDialog())
        {
            printService = printerJob.getPrintService();
        }
        if(printService!=null)
        {
            DocFlavor docType = DocFlavor.INPUT_STREAM.AUTOSENSE;

            DocPrintJob printJob = printService.createPrintJob();
            Doc documentToBePrinted = new SimpleDoc(new ByteArrayInputStream(byteStream), docType, null);
            printJob.print(documentToBePrinted, null);
        }else{
            IO.logAndAlert("Print Job", "Print job cancelled.", IO.TAG_INFO);
        }
    }

    private static void drawHorzLines(PDPageContentStream contents, int y_start, int page_width, Insets offsets) throws IOException
    {
        contents.setStrokingColor(new Color(171, 170, 166));
        //horizontal top title underline
        contents.moveTo(offsets.left, y_start);
        contents.lineTo(page_width-offsets.right, y_start);
        contents.stroke();
        for(int i=y_start;i>offsets.bottom;i-=LINE_HEIGHT)
        {
            //horizontal underline
            contents.moveTo(offsets.left, i-LINE_HEIGHT);
            contents.lineTo(page_width-offsets.right, i-LINE_HEIGHT);
            contents.stroke();
            //line_pos-=LINE_HEIGHT;
        }
    }

    private static void drawVertLines(PDPageContentStream contents, int[] x_positions, int y_start) throws IOException
    {
        for(int x: x_positions)
        {
            contents.moveTo(x, y_start);
            contents.lineTo(x, page_margins.bottom);
            contents.stroke();
        }
    }

    public static void createDocumentIndex(String title, FileMetadata[] fileMetadata, String path) throws IOException
    {
        // Create a new document with an empty page.
        final PDDocument document = new PDDocument();
        final PDPage page = new PDPage(PDRectangle.A4);

        final float w = page.getBBox().getWidth();
        final float h = page.getBBox().getHeight();

        //Add page to document
        document.addPage(page);

        // Adobe Acrobat uses Helvetica as a default font and
        // stores that under the name '/Helv' in the resources dictionary
        PDFont font = PDType1Font.HELVETICA;
        PDResources resources = new PDResources();
        resources.put(COSName.getPDFName("Helv"), font);

        PDPageContentStream contents = new PDPageContentStream(document, page);
        int logo_h = 60;
        PDImageXObject logo = PDImageXObject.createFromFile("images/logo.png", document);
        contents.drawImage(logo, (w/2)-80, 770, 160, logo_h);

        int line_pos = (int)h-logo_h-LINE_HEIGHT;

        /** draw horizontal lines **/
        drawHorzLines(contents, line_pos, (int)w, page_margins);
        /** draw vertical lines **/
        final int[] col_positions = {75, (int)((w / 2) + 100), (int)((w / 2) + 200)};
        drawVertLines(contents, col_positions, line_pos-LINE_HEIGHT);
        line_pos = (int)h-logo_h-LINE_HEIGHT;

        /** begin text from the top**/
        contents.beginText();
        contents.setFont(font, 12);
        line_pos-=10;
        //Heading text
        addTextToPageStream(contents, title, 16,(int)(w/2)-70, line_pos);
        line_pos-=LINE_HEIGHT;//next line

        //Create column headings
        addTextToPageStream(contents,"Index", 14,10, line_pos);
        addTextToPageStream(contents,"Label", 14, col_positions[0]+10, line_pos);
        addTextToPageStream(contents,"Required?", 14,col_positions[1]+10, line_pos);
        addTextToPageStream(contents,"Available?", 14,col_positions[2]+10, line_pos);

        contents.endText();
        line_pos-=LINE_HEIGHT;//next line

        //int pos = line_pos;
        for(FileMetadata metadata : fileMetadata)
        {
            contents.beginText();
            addTextToPageStream(contents, String.valueOf(metadata.getIndex()), 14, 20, line_pos);

            if(metadata.getLabel().length()>=105)
                addTextToPageStream(contents, metadata.getLabel(), 6, 80, line_pos);
            else if(metadata.getLabel().length()>=85)
                addTextToPageStream(contents, metadata.getLabel(), 8, 80, line_pos);
            else if(metadata.getLabel().length()>=45)
                addTextToPageStream(contents, metadata.getLabel(), 11, 80, line_pos);
            else if(metadata.getLabel().length()<45)
                addTextToPageStream(contents, metadata.getLabel(), 14, 80, line_pos);

            addTextToPageStream(contents, String.valueOf(metadata.getRequired()), 14, (int) (w / 2)+120, line_pos);
            contents.endText();

            //Availability field to be filled in by official
            line_pos-=LINE_HEIGHT;//next line

            //if reached bottom of page, add new page and reset cursor.
            if(line_pos<page_margins.bottom)
            {
                contents.close();
                final PDPage new_page = new PDPage(PDRectangle.A4);
                contents = new PDPageContentStream(document, new_page);
                //Add page to document
                document.addPage(new_page);
                contents.setFont(font, 14);

                line_pos = (int)h-logo_h-20;
                IO.log(TAG, IO.TAG_INFO, "Added new page.");
                //draw horizontal lines
                drawHorzLines(contents, line_pos, (int)w, page_margins);
                //draw vertical lines
                drawVertLines(contents, col_positions, line_pos);
                line_pos -= 10;//move cursor down a bit for following
            }
        }

        contents.close();

        document.save(path);
        document.close();

        PDFViewer pdfViewer = PDFViewer.getInstance();
        pdfViewer.doOpen(path);
        pdfViewer.setVisible(true);
    }

    public static void createBordersOnPage(PDPageContentStream contents, int page_w, int page_top, int page_bottom) throws IOException
    {
        //top border
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, page_top);
        contents.lineTo(page_w-10, page_top);
        contents.stroke();
        //left border
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, page_top);
        contents.lineTo(10, page_bottom-LINE_HEIGHT);
        contents.stroke();
        //right border
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(page_w-10, page_top);
        contents.lineTo(page_w-10, page_bottom-LINE_HEIGHT);
        contents.stroke();
        //bottom border
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, page_bottom-LINE_HEIGHT);
        contents.lineTo(page_w-10, page_bottom-LINE_HEIGHT);
        contents.stroke();
    }

    public static void createLinesAndBordersOnPage(PDPageContentStream contents, int page_w, int page_top, int page_bottom) throws IOException
    {
        boolean isTextMode=false;
        try
        {//try to end the text of stream.
            contents.endText();
            isTextMode=true;
        }catch (IllegalStateException e) {}
        //draw borders
        createBordersOnPage(contents, page_w, page_top, page_bottom);
        //draw horizontal lines
        int line_pos=page_top;
        for(int i=0;i<ROW_COUNT;i++)//35 rows
        {
            //horizontal underline
            contents.setStrokingColor(new Color(171, 170, 166));
            contents.moveTo(10, line_pos-LINE_HEIGHT);
            contents.lineTo(page_w-10, line_pos-LINE_HEIGHT);
            contents.stroke();
            line_pos-=LINE_HEIGHT;
        }
        if(isTextMode)
            contents.beginText();
    }

    public static void createPurchaseOrderPdf(PurchaseOrder purchaseOrder) throws IOException
    {
        if(purchaseOrder==null)
        {
            IO.logAndAlert("PDF Viewer", "PurchaseOrder object passed is null.", IO.TAG_ERROR);
            return;
        }
        //Prepare PDF data from database.
        //Load PurchaseOrder Supplier
        Supplier supplier = purchaseOrder.getSupplier();
        if(supplier==null)
        {
            IO.logAndAlert("PDF Viewer Error", "PurchaseOrder has no Supplier assigned to it.", IO.TAG_ERROR);
            return;
        }
        //Load PurchaseOrder contact person
        Employee employee = purchaseOrder.getContact_person();
        if(employee==null)
        {
            IO.logAndAlert("PDF Viewer Error", "PurchaseOrder has no contact person assigned to it.", IO.TAG_ERROR);
            return;
        }
        //Load PurchaseOrder contact person
        PurchaseOrderItem[] purchaseOrderItems = purchaseOrder.getItems();
        if(purchaseOrderItems==null)
        {
            IO.logAndAlert("PDF Viewer Error", "PurchaseOrder has no PurchaseOrderItems assigned to it.", IO.TAG_ERROR);
            return;
        }

        // Create a new document with an empty page.
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        // Adobe Acrobat uses Helvetica as a default font and
        // stores that under the name '/Helv' in the resources dictionary
        PDFont font = PDType1Font.HELVETICA;
        PDResources resources = new PDResources();
        resources.put(COSName.getPDFName("Helv"), font);

        PDPageContentStream contents = new PDPageContentStream(document, page);
        int logo_h = 60;
        //PDImageXObject logo = PDImageXObject.createFromFile("images/logo.png", document);
        //contents.drawImage(logo, 10, 770, 160, logo_h);

        float w = page.getBBox().getWidth();
        float h = page.getBBox().getHeight();
        int line_pos = (int)h-20;//(int)h-logo_h-20;
        int digit_font_size=9;

        /**Draw lines**/
        int center_vert_line_start = line_pos;
        int bottom_line = (int)h-logo_h-(ROW_COUNT+1)*LINE_HEIGHT;
        createLinesAndBordersOnPage(contents, (int)w, line_pos, bottom_line);

        /** begin text from the top**/
        contents.beginText();
        contents.setFont(font, 12);
        line_pos-=LINE_HEIGHT/2;

        int temp_pos = line_pos;
        //right text
        //addTextToPageStream(contents,"PURCHASE ORDER", PDType1Font.COURIER_BOLD_OBLIQUE, 17, (int)(w/2)+20, line_pos);
        //line_pos-=LINE_HEIGHT;//next line
        contents.endText();
        PDImageXObject logo = PDImageXObject.createFromFile("images/logo.png", document);
        contents.drawImage(logo, (int)(w/2)+ 20, line_pos-logo_h, 150, logo_h);
        contents.beginText();

        //line_pos=temp_pos;//revert to original line
        //line_pos-=LINE_HEIGHT;//next line

        //left text
        addTextToPageStream(contents,"Purchase Order #" + purchaseOrder.getNumber(), PDType1Font.COURIER_BOLD_OBLIQUE, 17,20, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(contents,"Date Generated:  " + (new SimpleDateFormat("yyyy-MM-dd").format(new Date(purchaseOrder.getDate_logged()*1000))), 12, 20, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(contents,"Overall Discount:  " + purchaseOrder.discountProperty().get(), 12,20 , line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(contents,"VAT:  " + purchaseOrder.getVat() + "%", 12,20 , line_pos);

        //line_pos=temp_pos;//revert to original line
        line_pos-=LINE_HEIGHT;//next line
        temp_pos=line_pos;

        //horizontal solid line after purchase order details
        contents.endText();
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, line_pos+LINE_HEIGHT/2);
        contents.lineTo(w-10, line_pos+LINE_HEIGHT/2);
        contents.stroke();

        //horizontal solid line after from/to labels
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, line_pos-LINE_HEIGHT/2);
        contents.lineTo(w-10, line_pos-LINE_HEIGHT/2);
        contents.stroke();

        //horizontal solid line after company details
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, line_pos-LINE_HEIGHT-LINE_HEIGHT/2);
        contents.lineTo(w-10, line_pos-LINE_HEIGHT-LINE_HEIGHT/2);
        contents.stroke();
        contents.beginText();

        //Company Info
        //Left Text: From
        addTextToPageStream(contents,"FROM", PDType1Font.HELVETICA_BOLD, 15,20, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(contents,Globals.COMPANY.getValue(), PDType1Font.HELVETICA_BOLD, 16,20, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(contents,"VAT No.: #############", 12,20, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(contents,"POSTAL ADDRESS: ##########", 12,20, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(contents,"CITY: ##########", 12,20, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(contents,"POSTAL CODE: ####", 12,20, line_pos);

        line_pos-=LINE_HEIGHT*2;//next 2ND line

        addTextToPageStream(contents,"PHYSICAL ADDRESS: ########", 12,20, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(contents,"CITY: ########", 12,20, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(contents,"PROVINCE/STATE: #######", 12,20, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(contents,"POSTAL CODE: ####", 12,20, line_pos);

        line_pos=temp_pos;//revert to original line
        //line_pos-=LINE_HEIGHT;//next line
        //temp_pos=line_pos;

        //Right Text: To
        int supplier_text_x = (int)(w/2)+5;
        addTextToPageStream(contents,"TO", PDType1Font.HELVETICA_BOLD, 15,supplier_text_x, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(contents, purchaseOrder.getSupplier().getSupplier_name(), PDType1Font.HELVETICA_BOLD, 16, supplier_text_x, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(contents,"VAT No.: #############", 12,supplier_text_x, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(contents,"POSTAL ADDRESS: " + purchaseOrder.getSupplier().getPostal_address(), 12,supplier_text_x, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(contents,"CITY: ##########", 12,supplier_text_x, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(contents,"POSTAL CODE: ####", 12,supplier_text_x, line_pos);

        line_pos-=LINE_HEIGHT*2;//next 2ND line

        addTextToPageStream(contents,"PHYSICAL ADDRESS: " + purchaseOrder.getSupplier().getPhysical_address(), 12,supplier_text_x, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(contents,"CITY: ########", 12,supplier_text_x, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(contents,"PROVINCE/STATE: #######", 12,supplier_text_x, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(contents,"POSTAL CODE: ####", 12,supplier_text_x, line_pos);

        //horizontal solid line after company details
        contents.endText();
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, line_pos-LINE_HEIGHT/2);
        contents.lineTo(w-10, line_pos-LINE_HEIGHT/2);
        contents.stroke();
        contents.beginText();

        line_pos-=LINE_HEIGHT;//next line
        temp_pos=line_pos;//backup current position

        //left text
        addTextToPageStream(contents,"Creator: " + purchaseOrder.getCreator(), PDType1Font.COURIER_BOLD_OBLIQUE, 12, 20, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(contents,"Tel    :  " + purchaseOrder.getCreator().getTel(), PDType1Font.HELVETICA_BOLD, 12,20, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(contents,"Cell   :  " + purchaseOrder.getCreator().getCell(), PDType1Font.HELVETICA_BOLD, 12,20, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(contents,"eMail :  " + purchaseOrder.getCreator().getEmail(), PDType1Font.HELVETICA_BOLD, 12,20, line_pos);

        line_pos=temp_pos;//revert line pos

        //right text
        addTextToPageStream(contents,"Supplier Contact: " + purchaseOrder.getContact_person(), PDType1Font.COURIER_BOLD_OBLIQUE, 12, (int)(w/2)+5, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(contents,"Tel    :  " + purchaseOrder.getContact_person().getTel(), PDType1Font.HELVETICA_BOLD, 12,(int)(w/2)+5, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(contents,"Cell   :  " + purchaseOrder.getContact_person().getCell(), PDType1Font.HELVETICA_BOLD, 12,(int)(w/2)+5, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(contents,"eMail :  " + purchaseOrder.getContact_person().getEmail(), PDType1Font.HELVETICA_BOLD, 12,(int)(w/2)+5, line_pos);
        line_pos-=LINE_HEIGHT;//next line

        //horizontal solid line
        contents.endText();
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, line_pos+LINE_HEIGHT/2);
        contents.lineTo(w-10, line_pos+LINE_HEIGHT/2);
        contents.stroke();
        contents.beginText();

        //horizontal solid line
        contents.endText();
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, line_pos-LINE_HEIGHT/2);
        contents.lineTo(w-10, line_pos-LINE_HEIGHT/2);
        contents.stroke();
        contents.beginText();

        contents.endText();

        //horizontal solid line after reps
        /*contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, line_pos+LINE_HEIGHT+LINE_HEIGHT/2);
        contents.lineTo(w-10, line_pos+LINE_HEIGHT+LINE_HEIGHT/2);
        contents.stroke();
        //horizontal solid line after request
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, line_pos+LINE_HEIGHT/2);
        contents.lineTo(w-10, line_pos+LINE_HEIGHT/2);
        contents.stroke();
        //solid horizontal line after site location, before quote_items
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, (line_pos-LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
        contents.lineTo(w-10, (line_pos-LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
        contents.stroke();*/

        int col_divider_start = line_pos-LINE_HEIGHT;

        //vertical line going through center of page
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo((w/2), center_vert_line_start);
        contents.lineTo((w/2),(col_divider_start-LINE_HEIGHT*2+(int)Math.ceil(LINE_HEIGHT/2)));
        contents.stroke();
        //
        contents.moveTo((w/2), (col_divider_start+(int)Math.ceil(LINE_HEIGHT/2)));
        contents.lineTo((w/2),(col_divider_start-LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
        contents.stroke();

        contents.beginText();

        //Column headings
        int col_pos = 10;
        addTextToPageStream(contents,"Item No.", PDType1Font.COURIER_BOLD,14,15, line_pos);
        col_pos += 80;
        addTextToPageStream(contents,"Item description", PDType1Font.COURIER_BOLD,14,col_pos+20, line_pos);
        col_pos = (int)(w/2);
        String[] cols = {"Unit", "Qty", "Cost", "Discount", "Total"};
        for(int i=0;i<5;i++)//7 cols in total
            addTextToPageStream(contents,cols[i], PDType1Font.COURIER_BOLD, 12,col_pos+(55*i)+2, line_pos);
        line_pos-=LINE_HEIGHT;//next line

        //Purchase Order Items
        col_pos = 10;
        double sub_total = 0;
        for(PurchaseOrderItem item: purchaseOrderItems)
        {
            //quote content column dividers
            contents.endText();
            //#1
            contents.moveTo(80, (col_divider_start+LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
            contents.lineTo(80, line_pos-LINE_HEIGHT/2);
            contents.stroke();
            //vertical line going through center of page
            contents.setStrokingColor(Color.BLACK);
            contents.moveTo((w/2), (col_divider_start-LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
            contents.lineTo((w/2),line_pos-LINE_HEIGHT/2);
            contents.stroke();
            //#3+
            for(int i=1;i<5;i++)//7 cols in total
            {
                contents.moveTo((w/2)+55*i, (col_divider_start+LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
                contents.lineTo((w/2)+55*i,line_pos-LINE_HEIGHT/2);
                contents.stroke();
            }
            contents.beginText();
            //end draw columns

            //if the page can't hold another 4 lines[current item, blank, sub-total, vat] add a new page
            if(line_pos-LINE_HEIGHT<h-logo_h-(ROW_COUNT*LINE_HEIGHT))
            {
                addTextToPageStream(contents, "Page "+quote_page_count, PDType1Font.COURIER_OBLIQUE, 14,(int)(w/2)-20, 30);
                //add new page
                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                //TODO: setup page, i.e. draw lines and stuff
                contents.close();
                contents = new PDPageContentStream(document, page);
                contents.beginText();
                line_pos = (int)h-logo_h;
                col_divider_start = line_pos+LINE_HEIGHT;
                createLinesAndBordersOnPage(contents, (int)w, line_pos+LINE_HEIGHT/2, bottom_line);
                quote_page_count++;
            }

            col_pos =0;//first column
            //Item # col
            addTextToPageStream(contents, item.getItem_number(), 12,col_pos+30, line_pos);
            col_pos += 80;//next column
            //Description col
            addTextToPageStream(contents, item.getItem_description(), 12,col_pos+5, line_pos);
            col_pos = (int)w/2;//next column - starts at middle of page
            //Unit col
            addTextToPageStream(contents,item.getUnit(), 12,col_pos+5, line_pos);
            col_pos+=55;//next column
            //Quantity col
            addTextToPageStream(contents,item.getQuantity(), digit_font_size,col_pos+5, line_pos);
            col_pos+=55;//next column
            //Cost col
            addTextToPageStream(contents, String.valueOf(DecimalFormat.getCurrencyInstance().format(item.getCostValue())), digit_font_size,col_pos+5, line_pos);
            col_pos+=55;//next column
            //Discount col
            addTextToPageStream(contents, String.valueOf(item.getDiscount()), digit_font_size,col_pos+5, line_pos);
            col_pos+=55;//next column
            //Total col
            sub_total+=item.getTotal();
            addTextToPageStream(contents, String.valueOf(DecimalFormat.getCurrencyInstance().format(item.getTotal())), digit_font_size,col_pos+5, line_pos);

            line_pos -= LINE_HEIGHT;//next line
        }
        IO.log(TAG, IO.TAG_INFO, "successfully created Purchase Order PDF.");
        //col_pos = 0;
        //line_pos -= LINE_HEIGHT;//skip another line
        /*if the page can't hold another 2 lines add a new page
        if(line_pos-LINE_HEIGHT*2<h-logo_h-(ROW_COUNT*LINE_HEIGHT) || temp_pos-LINE_HEIGHT*2<h-logo_h-(ROW_COUNT*LINE_HEIGHT))
        {
            //add new page
            page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            //TODO: setup page, i.e. draw lines and stuff
            contents.close();
            contents = new PDPageContentStream(document, page);
            contents.beginText();
            line_pos = (int)h-logo_h;
            col_divider_start = line_pos+LINE_HEIGHT;
        }*/
        //solid horizontal line
        int col_divider_end= line_pos;

        contents.endText();
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, line_pos+LINE_HEIGHT/2);
        contents.lineTo(w-10, line_pos+LINE_HEIGHT/2);
        contents.stroke();

        contents.beginText();
        addTextToPageStream(contents, "Sub-Total [Excl. VAT]: ", PDType1Font.COURIER_BOLD_OBLIQUE, 14,20, line_pos);
        addTextToPageStream(contents, String.valueOf(DecimalFormat.getCurrencyInstance().format(sub_total)), PDType1Font.COURIER_BOLD_OBLIQUE, 14,(int)(5+(w/2)), line_pos);
        line_pos -= LINE_HEIGHT;//next line

        //solid horizontal line
        contents.endText();
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, line_pos+LINE_HEIGHT/2);
        contents.lineTo(w-10, line_pos+LINE_HEIGHT/2);
        contents.stroke();

        double vat = sub_total*(purchaseOrder.getVatVal()/100);
        contents.beginText();
        addTextToPageStream(contents, "VAT: ", PDType1Font.COURIER_BOLD_OBLIQUE, 14,20, line_pos);
        addTextToPageStream(contents, String.valueOf(DecimalFormat.getCurrencyInstance().format(vat)), PDType1Font.COURIER_BOLD_OBLIQUE, 14, (int)(5+(w/2)), line_pos);
        line_pos -= LINE_HEIGHT;//next line

        //solid horizontal line
        contents.endText();
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, line_pos+LINE_HEIGHT/2);
        contents.lineTo(w-10, line_pos+LINE_HEIGHT/2);
        contents.stroke();

        contents.beginText();
        addTextToPageStream(contents, "Total [Incl. VAT]: ", PDType1Font.COURIER_BOLD_OBLIQUE, 14,20, line_pos);
        addTextToPageStream(contents, String.valueOf(DecimalFormat.getCurrencyInstance().format(sub_total + vat)), PDType1Font.COURIER_BOLD_OBLIQUE, 14, (int)(5+(w/2)), line_pos);
        contents.endText();
        line_pos -= LINE_HEIGHT;//next line

        //solid horizontal line
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, line_pos+LINE_HEIGHT/2);
        contents.lineTo(w-10, line_pos+LINE_HEIGHT/2);
        contents.stroke();

        //int col_divider_end = line_pos;
        line_pos -= LINE_HEIGHT*3;//next 3rd line
        /*solid horizontal lines after quote_items
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, col_divider_end+LINE_HEIGHT+LINE_HEIGHT/2);
        contents.lineTo(w-10, col_divider_end+LINE_HEIGHT+LINE_HEIGHT/2);
        contents.stroke();
        contents.moveTo(10, col_divider_end+LINE_HEIGHT/2);
        contents.lineTo(w-10, col_divider_end+LINE_HEIGHT/2);
        contents.stroke();
        contents.moveTo(10, col_divider_end-LINE_HEIGHT+LINE_HEIGHT/2);
        contents.lineTo(w-10, col_divider_end-LINE_HEIGHT+LINE_HEIGHT/2);
        contents.stroke();*/

        //quote content column dividers
        //#1
        contents.moveTo(80, (col_divider_start-LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
        contents.lineTo(80, col_divider_end+LINE_HEIGHT+LINE_HEIGHT/2);
        contents.stroke();
        //vertical line going through center of page again
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo((w/2), (col_divider_start-LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
        contents.lineTo((w/2), col_divider_end+LINE_HEIGHT+LINE_HEIGHT/2);
        //contents.lineTo((w/2),col_divider_end-LINE_HEIGHT/2);
        contents.stroke();
        //#3+
        for(int i=1;i<5;i++)//7 cols in total
        {
            contents.moveTo((w/2)+55*i, (col_divider_start-LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
            contents.lineTo((w/2)+55*i,col_divider_end+LINE_HEIGHT+LINE_HEIGHT/2);
            contents.stroke();
        }

        contents.beginText();

        if(purchaseOrder.getExtra()!=null)
            addTextToPageStream(contents, "P.S. "+purchaseOrder.getExtra(), PDType1Font.TIMES_ITALIC, 14,col_pos+5, line_pos);

        line_pos -= LINE_HEIGHT;//next line
        //if the page can't hold another 9 lines add a new page
        if(line_pos-(LINE_HEIGHT*4)<h-logo_h-(ROW_COUNT*LINE_HEIGHT))
        {
            addTextToPageStream(contents, "Page "+quote_page_count, PDType1Font.COURIER_OBLIQUE, 14,(int)(w/2)-20, 30);
            //add new page
            page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            contents.close();
            contents = new PDPageContentStream(document, page);
            contents.beginText();
            line_pos = (int)h-logo_h;
            createLinesAndBordersOnPage(contents, (int)w, line_pos+LINE_HEIGHT/2, bottom_line);
            quote_page_count++;
        }

        addTextToPageStream(contents, "Page "+quote_page_count, PDType1Font.COURIER_OBLIQUE, 14,(int)(w/2)-20, 30);
        contents.endText();
        contents.close();

        String path = "bin/pdf/purchase_order_" + purchaseOrder.getNumber() + ".pdf";
        int i=1;
        while(new File(path).exists())
        {
            path = "bin/pdf/purchase_order_" + purchaseOrder.getNumber() + "." + i + ".pdf";
            i++;
        }

        //Files.delete(Paths.get(path));//delete previous versions

        document.save(path);
        document.close();

        PDFViewer pdfViewer = PDFViewer.getInstance();
        pdfViewer.setVisible(true);

        pdfViewer.doOpen(path);
    }

    public static void createQuotePdf(Quote quote) throws IOException
    {
        if(quote==null)
        {
            IO.logAndAlert("PDF Viewer", "Quote object passed is null.", IO.TAG_ERROR);
            return;
        }
        //Prepare PDF data from database.
        //Load Quote Client
        Client client = quote.getClient();
        if(client==null)
        {
            IO.logAndAlert("PDF Viewer Error", "Quote has no client assigned to it.", IO.TAG_ERROR);
            return;
        }
        //Load Employees assigned to Quote
        Employee[] reps = quote.getRepresentatives();
        if(reps==null)
        {
            IO.logAndAlert("PDF Viewer Error", "Quote has no representatives(employees) assigned to it.", IO.TAG_ERROR);
            return;
        }
        Employee contact = quote.getContact_person();
        if(contact==null)
        {
            IO.logAndAlert("PDF Viewer Error", "Quote has no client contact person assigned to it.", IO.TAG_ERROR);
            return;
        }

        // Create a new document with an empty page.
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        // Adobe Acrobat uses Helvetica as a default font and
        // stores that under the name '/Helv' in the resources dictionary
        PDFont font = PDType1Font.HELVETICA;
        PDResources resources = new PDResources();
        resources.put(COSName.getPDFName("Helv"), font);

        PDPageContentStream contents = new PDPageContentStream(document, page);
        int logo_h = 60;
        PDImageXObject logo = PDImageXObject.createFromFile("images/logo.png", document);
        contents.drawImage(logo, 10, 770, 160, logo_h);

        float w = page.getBBox().getWidth();
        float h = page.getBBox().getHeight();
        int line_pos = (int)h-logo_h-20;
        int digit_font_size=9;

        /**Draw lines**/
        int center_vert_line_start = line_pos;
        int bottom_line = (int)h-logo_h-(ROW_COUNT+1)*LINE_HEIGHT;
        createLinesAndBordersOnPage(contents, (int)w, line_pos, bottom_line);

        /** begin text from the top**/
        contents.beginText();
        contents.setFont(font, 12);
        line_pos-=LINE_HEIGHT/2;
        //left text
        addTextToPageStream(contents,"Client Information", PDType1Font.COURIER_BOLD_OBLIQUE, 15,(int)((w/2)/4), line_pos);
        //right text
        addTextToPageStream(contents,"Quotation No.: " + quote.quoteProperty().getValue(), PDType1Font.COURIER_BOLD_OBLIQUE, 11, (int)(w/2)+5, line_pos);
        line_pos-=LINE_HEIGHT;//next line

        //left text
        addTextToPageStream(contents,"Company: " + client.getClient_name(), 12, 20, line_pos);
        //right text
        addTextToPageStream(contents,"Date Generated:  " + (new SimpleDateFormat("yyyy-MM-dd").format(new Date(quote.getDate_generated()*1000))), 12,(int)(w/2)+5, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        //left text
        addTextToPageStream(contents,"Company Tel: " + client.getTel(), 12,20, line_pos);
        //right text
        addTextToPageStream(contents,"Sale Consultant(s):", PDType1Font.COURIER_BOLD_OBLIQUE, 16,(int)((w/2)+((w/2)/4)), line_pos);

        //horizontal solid line after company details
        contents.endText();
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, line_pos-LINE_HEIGHT/2);
        contents.lineTo(w-10, line_pos-LINE_HEIGHT/2);
        contents.stroke();
        contents.beginText();

        line_pos-=LINE_HEIGHT;//next line

        int temp_pos = line_pos;
        //left text
        addTextToPageStream(contents,"Contact Person:  " + contact.toString(), PDType1Font.HELVETICA_BOLD, 12,20, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(contents,"Tel    :  " + contact.getTel(), PDType1Font.HELVETICA_BOLD, 12,120, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(contents,"Cell   :  " + contact.getCell(), PDType1Font.HELVETICA_BOLD, 12,120, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(contents,"eMail :  " + contact.getEmail(), PDType1Font.HELVETICA_BOLD, 12,120, line_pos);

        //horizontal solid line
        contents.endText();
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, line_pos-LINE_HEIGHT/2);
        contents.lineTo(w-10, line_pos-LINE_HEIGHT/2);
        contents.stroke();
        contents.beginText();

        line_pos-=LINE_HEIGHT;//next line (for external consultants)
        //temp_pos-=LINE_HEIGHT;//next line (for internal consultants)
        //Render sale representatives
        int int_rep_count=0;
        for(Employee employee : reps)
        {
            //if the page can't hold 4 more lines add a new page
            if(line_pos-(4*LINE_HEIGHT)<h-logo_h-(ROW_COUNT*LINE_HEIGHT) || temp_pos-(4*LINE_HEIGHT)<h-logo_h-(ROW_COUNT*LINE_HEIGHT))
            {
                addTextToPageStream(contents, "Page "+quote_page_count, PDType1Font.HELVETICA_OBLIQUE, 14,(int)(w/2)-20, 50);
                //add new page
                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                //TODO: setup page, i.e. draw lines and stuff
                contents.close();
                contents = new PDPageContentStream(document, page);
                temp_pos = (int)h-logo_h;
                line_pos = (int)h-logo_h;

                createLinesAndBordersOnPage(contents, (int)w, line_pos, line_pos+LINE_HEIGHT/2);

                contents.beginText();
                quote_page_count++;
            }

            if(!employee.isActiveVal())//external employee
            {
                addTextToPageStream(contents,"Contact Person:   " + employee.toString(), 12,20, line_pos);
                line_pos-=LINE_HEIGHT;//next line
                addTextToPageStream(contents,"Tel    :  " + employee.getTel(), 12,120, line_pos);
                line_pos-=LINE_HEIGHT;//next line
                addTextToPageStream(contents,"Cell   :  " + employee.getCell(), 12,120, line_pos);
                line_pos-=LINE_HEIGHT;//next line
                addTextToPageStream(contents,"eMail :  " + employee.getEmail(), 12,120, line_pos);
                line_pos-=LINE_HEIGHT;//next line
            }else {//internal representatives
                if(int_rep_count==0)//make first internal rep bold
                {
                    addTextToPageStream(contents, "Sale Consultant:  " + employee.toString(), PDType1Font.HELVETICA_BOLD, 12, (int) (w / 2) + 5, temp_pos);
                    temp_pos -= LINE_HEIGHT;//next line
                    addTextToPageStream(contents, "Tel    :  " + employee.getTel(), PDType1Font.HELVETICA_BOLD, 12, (int) (w / 2) + 105, temp_pos);
                    temp_pos -= LINE_HEIGHT;//next line
                    addTextToPageStream(contents, "Cell   :  " + employee.getCell(), PDType1Font.HELVETICA_BOLD, 12, (int) (w / 2) + 105, temp_pos);
                    temp_pos -= LINE_HEIGHT;//next line
                    addTextToPageStream(contents, "eMail :  " + employee.getEmail(), PDType1Font.HELVETICA_BOLD, 12, (int) (w / 2) + 105, temp_pos);
                    temp_pos -= LINE_HEIGHT;//next line
                }else
                {
                    addTextToPageStream(contents, "Sale Consultant:  " + employee.toString(), 12, (int) (w / 2) + 5, temp_pos);
                    temp_pos -= LINE_HEIGHT;//next line
                    addTextToPageStream(contents, "Tel    :  " + employee.getTel(), 12, (int) (w / 2) + 105, temp_pos);
                    temp_pos -= LINE_HEIGHT;//next line
                    addTextToPageStream(contents, "Cell   :  " + employee.getCell(), 12, (int) (w / 2) + 105, temp_pos);
                    temp_pos -= LINE_HEIGHT;//next line
                    addTextToPageStream(contents, "eMail :  " + employee.getEmail(), 12, (int) (w / 2) + 105, temp_pos);
                    temp_pos -= LINE_HEIGHT;//next line
                }
                int_rep_count++;
            }
        }
        //set the cursor to the line after the sale/client rep info
        line_pos = line_pos<temp_pos?line_pos:temp_pos;
        addTextToPageStream(contents,"Request: " + quote.getRequest(),PDType1Font.HELVETICA, 13,20, line_pos);
        line_pos-=LINE_HEIGHT;//next line

        contents.endText();

        //horizontal solid line after reps
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, line_pos+LINE_HEIGHT+LINE_HEIGHT/2);
        contents.lineTo(w-10, line_pos+LINE_HEIGHT+LINE_HEIGHT/2);
        contents.stroke();
        //horizontal solid line after request
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, line_pos+LINE_HEIGHT/2);
        contents.lineTo(w-10, line_pos+LINE_HEIGHT/2);
        contents.stroke();
        //solid horizontal line after site location, before quote_items
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, (line_pos-LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
        contents.lineTo(w-10, (line_pos-LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
        contents.stroke();

        int col_divider_start = line_pos-LINE_HEIGHT;

        //vertical line going through center of page
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo((w/2), center_vert_line_start);
        contents.lineTo((w/2),(col_divider_start+LINE_HEIGHT*2+(int)Math.ceil(LINE_HEIGHT/2)));
        contents.stroke();
        //
        contents.moveTo((w/2), (col_divider_start+(int)Math.ceil(LINE_HEIGHT/2)));
        contents.lineTo((w/2),(col_divider_start-LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
        contents.stroke();

        contents.beginText();
        addTextToPageStream(contents,"Site Location: " + quote.getSitename(),PDType1Font.HELVETICA, 13,20, line_pos);
        //addTextToPageStream(contents,"Total Incl. VAT: "+String.valueOf(DecimalFormat.getCurrencyInstance().format(quote.getTotal()+quote.getTotal()*(Quote.VAT/100))), PDType1Font.COURIER_BOLD_OBLIQUE, 14, (int)((w/2)+15), line_pos);
        line_pos-=LINE_HEIGHT;//next line

        contents.endText();
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, (line_pos-LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
        contents.lineTo(w-10, (line_pos-LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
        contents.stroke();
        contents.beginText();

        //Column headings
        int col_pos = 10;
        addTextToPageStream(contents,"Item No.", PDType1Font.COURIER_BOLD,14,15, line_pos);
        col_pos += 80;
        addTextToPageStream(contents,"Equipment description", PDType1Font.COURIER_BOLD,14,col_pos+20, line_pos);
        col_pos = (int)(w/2);
        String[] cols = {"Unit", "Qty", "Rate", "Labour", "Total"};
        for(int i=0;i<5;i++)//7 cols in total
            addTextToPageStream(contents,cols[i], PDType1Font.COURIER_BOLD, 12,col_pos+(55*i)+10, line_pos);
        line_pos-=LINE_HEIGHT;//next line

        //Actual quote information
        col_pos = 10;
        double sub_total = 0;
        if(quote.getResources()!=null)
        {
            for(QuoteItem item: quote.getResources())
            {
                //quote content column dividers
                contents.endText();
                //#1
                contents.moveTo(80, (col_divider_start+(int)Math.ceil(LINE_HEIGHT/2)));
                contents.lineTo(80, line_pos-LINE_HEIGHT/2);
                contents.stroke();
                //vertical line going through center of page
                contents.setStrokingColor(Color.BLACK);
                contents.moveTo((w/2), (col_divider_start-LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
                contents.lineTo((w/2),line_pos-LINE_HEIGHT/2);
                contents.stroke();
                //#3+
                for(int i=1;i<5;i++)//7 cols in total
                {
                    contents.moveTo((w/2)+55*i, (col_divider_start+(int)Math.ceil(LINE_HEIGHT/2)));
                    contents.lineTo((w/2)+55*i,line_pos-LINE_HEIGHT/2);
                    contents.stroke();
                }
                contents.beginText();
                //end draw columns

                //if the page can't hold another 4 lines[current item, blank, sub-total, vat] add a new page
                if(line_pos-LINE_HEIGHT<h-logo_h-(ROW_COUNT*LINE_HEIGHT))
                {
                    addTextToPageStream(contents, "Page "+quote_page_count, PDType1Font.COURIER_OBLIQUE, 14,(int)(w/2)-20, 30);
                    //add new page
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    //TODO: setup page, i.e. draw lines and stuff
                    contents.close();
                    contents = new PDPageContentStream(document, page);
                    contents.beginText();
                    line_pos = (int)h-logo_h;
                    col_divider_start = line_pos+LINE_HEIGHT;
                    createLinesAndBordersOnPage(contents, (int)w, line_pos+LINE_HEIGHT/2, bottom_line);
                    quote_page_count++;
                }

                col_pos =0;//first column
                //Item col
                addTextToPageStream(contents, item.getItem_number(), 12,col_pos+30, line_pos);
                col_pos += 80;//next column
                //Description col
                addTextToPageStream(contents, item.getResource().getResource_name(), 12,col_pos+5, line_pos);
                col_pos = (int)w/2;//next column - starts at middle of page
                //Unit col
                addTextToPageStream(contents,item.getUnit(), 12,col_pos+5, line_pos);
                col_pos+=55;//next column
                //Quantity col
                addTextToPageStream(contents,item.getQuantity(), digit_font_size,col_pos+5, line_pos);
                col_pos+=55;//next column
                //Rate col
                addTextToPageStream(contents, String.valueOf(DecimalFormat.getCurrencyInstance().format(item.getRate())), digit_font_size,col_pos+5, line_pos);
                col_pos+=55;//next column
                //Labour col
                //addTextToPageStream(contents, String.valueOf(DecimalFormat.getCurrencyInstance().format(item.getLabourCost())), digit_font_size,col_pos+5, line_pos);
                col_pos+=55;//next column
                //Total col
                sub_total+=item.getTotal();
                addTextToPageStream(contents, String.valueOf(DecimalFormat.getCurrencyInstance().format(item.getTotal())), digit_font_size,col_pos+5, line_pos);

                line_pos -= LINE_HEIGHT;//next line
            }
            IO.log(TAG, IO.TAG_INFO, "successfully created quote PDF.");
        }else IO.log(TAG, IO.TAG_INFO, "quote has no resources.");
        col_pos = 0;
        //line_pos -= LINE_HEIGHT;//skip another line
        /*if the page can't hold another 2 lines add a new page
        if(line_pos-LINE_HEIGHT*2<h-logo_h-(ROW_COUNT*LINE_HEIGHT) || temp_pos-LINE_HEIGHT*2<h-logo_h-(ROW_COUNT*LINE_HEIGHT))
        {
            //add new page
            page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            //TODO: setup page, i.e. draw lines and stuff
            contents.close();
            contents = new PDPageContentStream(document, page);
            contents.beginText();
            line_pos = (int)h-logo_h;
            col_divider_start = line_pos+LINE_HEIGHT;
        }*/
        //solid horizontal line
        int col_divider_end= line_pos;

        contents.endText();
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, line_pos+LINE_HEIGHT/2);
        contents.lineTo(w-10, line_pos+LINE_HEIGHT/2);
        contents.stroke();

        contents.beginText();
        addTextToPageStream(contents, "Sub-Total Excl. VAT: ", PDType1Font.COURIER_BOLD_OBLIQUE, 14,col_pos+30, line_pos);
        addTextToPageStream(contents, String.valueOf(DecimalFormat.getCurrencyInstance().format(sub_total)), PDType1Font.COURIER_BOLD_OBLIQUE, 14,(int)(5+(w/2)), line_pos);
        line_pos -= LINE_HEIGHT;//next line

        //solid horizontal line
        contents.endText();
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, line_pos+LINE_HEIGHT/2);
        contents.lineTo(w-10, line_pos+LINE_HEIGHT/2);
        contents.stroke();

        double vat = sub_total*(quote.getVat()/100);
        contents.beginText();
        addTextToPageStream(contents, "VAT [" + quote.getVat() + "%]: ", PDType1Font.COURIER_BOLD_OBLIQUE, 14,col_pos+30, line_pos);
        addTextToPageStream(contents, String.valueOf(DecimalFormat.getCurrencyInstance().format(vat)), PDType1Font.COURIER_BOLD_OBLIQUE, 14,(int)(5+(w/2)), line_pos);
        line_pos -= LINE_HEIGHT;//next line

        //solid horizontal line
        contents.endText();
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, line_pos+LINE_HEIGHT/2);
        contents.lineTo(w-10, line_pos+LINE_HEIGHT/2);
        contents.stroke();

        contents.beginText();
        addTextToPageStream(contents, "Total Incl. VAT: ", PDType1Font.COURIER_BOLD_OBLIQUE, 14,col_pos+30, line_pos);
        addTextToPageStream(contents, String.valueOf(DecimalFormat.getCurrencyInstance().format(sub_total + vat)), PDType1Font.COURIER_BOLD_OBLIQUE, 14,(int)(5+(w/2)), line_pos);
        contents.endText();
        line_pos -= LINE_HEIGHT;//next line

        //solid horizontal line
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, line_pos+LINE_HEIGHT/2);
        contents.lineTo(w-10, line_pos+LINE_HEIGHT/2);
        contents.stroke();

        //int col_divider_end = line_pos;
        line_pos -= LINE_HEIGHT*3;//next 3rd line
        /*solid horizontal lines after quote_items
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, col_divider_end+LINE_HEIGHT+LINE_HEIGHT/2);
        contents.lineTo(w-10, col_divider_end+LINE_HEIGHT+LINE_HEIGHT/2);
        contents.stroke();
        contents.moveTo(10, col_divider_end+LINE_HEIGHT/2);
        contents.lineTo(w-10, col_divider_end+LINE_HEIGHT/2);
        contents.stroke();
        contents.moveTo(10, col_divider_end-LINE_HEIGHT+LINE_HEIGHT/2);
        contents.lineTo(w-10, col_divider_end-LINE_HEIGHT+LINE_HEIGHT/2);
        contents.stroke();*/

        //quote content column dividers
        //#1
        contents.moveTo(80, (col_divider_start-LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
        contents.lineTo(80, col_divider_end+LINE_HEIGHT+LINE_HEIGHT/2);
        contents.stroke();
        //vertical line going through center of page again
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo((w/2), (col_divider_start-LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
        contents.lineTo((w/2), col_divider_end+LINE_HEIGHT+LINE_HEIGHT/2);
        //contents.lineTo((w/2),col_divider_end-LINE_HEIGHT/2);
        contents.stroke();
        //#3+
        for(int i=1;i<5;i++)//7 cols in total
        {
            contents.moveTo((w/2)+55*i, (col_divider_start-LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
            contents.lineTo((w/2)+55*i,col_divider_end+LINE_HEIGHT+LINE_HEIGHT/2);
            contents.stroke();
        }

        contents.beginText();

        if(quote.getExtra()!=null)
            addTextToPageStream(contents, "P.S. "+quote.getExtra(), PDType1Font.TIMES_ITALIC, 14,col_pos+5, line_pos);

        line_pos -= LINE_HEIGHT;//next line
        //if the page can't hold another 9 lines add a new page
        if(line_pos-(LINE_HEIGHT*4)<h-logo_h-(ROW_COUNT*LINE_HEIGHT))
        {
            addTextToPageStream(contents, "Page "+quote_page_count, PDType1Font.COURIER_OBLIQUE, 14,(int)(w/2)-20, 30);
            //add new page
            page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            contents.close();
            contents = new PDPageContentStream(document, page);
            contents.beginText();
            line_pos = (int)h-logo_h;
            createLinesAndBordersOnPage(contents, (int)w, line_pos+LINE_HEIGHT/2, bottom_line);
            quote_page_count++;
        }
        addTextToPageStream(contents, "TERMS AND CONDITIONS OF SALE", PDType1Font.HELVETICA_BOLD, 14,(int)(w/2)-130, line_pos);
        contents.endText();
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo((int)(w/2)-140, line_pos-LINE_HEIGHT/2);
        contents.lineTo((w/2)+120, line_pos-LINE_HEIGHT/2);
        contents.stroke();
        contents.beginText();

        line_pos -= LINE_HEIGHT;//next line
        addTextToPageStream(contents, "*Validity: Quote valid for 24 Hours.", PDType1Font.HELVETICA, 12,col_pos+30, line_pos);
        line_pos -= LINE_HEIGHT;//next line
        addTextToPageStream(contents, "*Payment Terms: COD / 30 Days on approved accounts. ", PDType1Font.HELVETICA, 12,col_pos+30, line_pos);
        line_pos -= LINE_HEIGHT;//next line
        addTextToPageStream(contents, "*Delivery: 1 - 6 Weeks, subject to stock availability.", PDType1Font.HELVETICA, 12,col_pos+30, line_pos);
        line_pos -= LINE_HEIGHT;//next line
        addTextToPageStream(contents, "*All pricing quoted, is subject to Rate of Exchange USD=R.", PDType1Font.HELVETICA_BOLD, 12,col_pos+30, line_pos);
        line_pos -= LINE_HEIGHT;//next line
        addTextToPageStream(contents, "*All goods / equipment remain the property of " + Globals.COMPANY.getValue()+ " until paid for completely. ", PDType1Font.HELVETICA, 12,col_pos+30, line_pos);
        line_pos -= LINE_HEIGHT;//next line
        addTextToPageStream(contents, "*" + Globals.COMPANY.getValue() + " reserves the right to retake posession of all equipment not paid for completely", PDType1Font.HELVETICA, 12,col_pos+30, line_pos);
        line_pos -= LINE_HEIGHT;//next line
        addTextToPageStream(contents, "  Within the payment term set out above.", PDType1Font.HELVETICA, 12,col_pos+30, line_pos);
        line_pos -= LINE_HEIGHT;//next line
        addTextToPageStream(contents, "*E & O E", PDType1Font.HELVETICA, 12,col_pos+30, line_pos);

        addTextToPageStream(contents, "Page "+quote_page_count, PDType1Font.COURIER_OBLIQUE, 14,(int)(w/2)-20, 30);
        contents.endText();
        contents.close();

        String path = "bin/pdf/quote_" + quote.get_id() + ".pdf";
        int i=1;
        while(new File(path).exists())
        {
            path = "bin/pdf/quote_" + quote.get_id() + "." + i + ".pdf";
            i++;
        }

        //Files.delete(Paths.get("bin/pdf/quote_"+quote.get_id()+".pdf"));//delete previous versions

        document.save(path);
        document.close();

        PDFViewer pdfViewer = PDFViewer.getInstance();
        pdfViewer.setVisible(true);

        pdfViewer.doOpen(path);//"bin/pdf/quote_" + quote.get_id() + ".pdf"
    }

    public static void createInvoicePdf(Invoice invoice) throws IOException
    {
        if(invoice==null)
        {
            IO.logAndAlert("PDF Viewer", "Invoice object passed is null.", IO.TAG_ERROR);
            return;
        }
        if(invoice.getJob()==null)
        {
            IO.logAndAlert("PDF Viewer", "Invoice->Job object passed is null.", IO.TAG_ERROR);
            return;
        }
        if(invoice.getJob().getQuote()==null)
        {
            IO.logAndAlert("PDF Viewer", "Invoice->Job->Quote object passed is null.", IO.TAG_ERROR);
            return;
        }
        Job job = invoice.getJob();
        Quote quote = invoice.getJob().getQuote();
        //Prepare PDF data from database.
        //Load Invoice Client
        Client client = invoice.getJob().getQuote().getClient();
        if(client==null)
        {
            IO.logAndAlert("PDF Viewer Error", "Quote has no client assigned to it.", IO.TAG_ERROR);
            return;
        }
        //Load Employees assigned to Quote
        Employee[] reps = invoice.getJob().getQuote().getRepresentatives();
        if(reps==null)
        {
            IO.logAndAlert("PDF Viewer Error", "Quote has no representatives(employees) assigned to it.", IO.TAG_ERROR);
            return;
        }
        Employee contact = quote.getContact_person();
        if(contact==null)
        {
            IO.logAndAlert("PDF Viewer Error", "Quote has no client contact person assigned to it.", IO.TAG_ERROR);
            return;
        }

        // Create a new document with an empty page.
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        // Adobe Acrobat uses Helvetica as a default font and
        // stores that under the name '/Helv' in the resources dictionary
        PDFont font = PDType1Font.HELVETICA;
        PDResources resources = new PDResources();
        resources.put(COSName.getPDFName("Helv"), font);

        PDPageContentStream contents = new PDPageContentStream(document, page);
        int logo_h = 60;
        //PDImageXObject logo = PDImageXObject.createFromFile("images/logo.png", document);
        //contents.drawImage(logo, 10, 770, 160, logo_h);

        float w = page.getBBox().getWidth();
        float h = page.getBBox().getHeight();
        int line_pos = (int)h-20;//(int)h-logo_h-20;
        int digit_font_size=9;

        /**Draw lines**/
        int bottom_line = (int)h-logo_h-(ROW_COUNT+1)*LINE_HEIGHT;
        createLinesAndBordersOnPage(contents, (int)w, line_pos, bottom_line);

        /** begin text from the top**/
        line_pos-=LINE_HEIGHT/2;

        //left text
        contents.beginText();
        int temp_pos = line_pos;
        addTextToPageStream(contents,"Invoice ID: " + invoice.get_id(), PDType1Font.COURIER_BOLD_OBLIQUE, 15,20, line_pos);
        line_pos-=LINE_HEIGHT;
        int center_vert_line_start = line_pos;
        addTextToPageStream(contents,"Date Generated:  " + (new SimpleDateFormat("yyyy-MM-dd").format(new Date(job.getDate_logged()*1000))), 12,20, line_pos);
        line_pos-=LINE_HEIGHT;
        addTextToPageStream(contents,"Creator:  " + invoice.getCreator(), 12,20, line_pos);
        line_pos-=LINE_HEIGHT;
        addTextToPageStream(contents,"Quote ID: " + quote.get_id(), 14,20, line_pos);
        //line_pos-=LINE_HEIGHT;
        //addTextToPageStream(contents,"Quote Date Generated: " + (new SimpleDateFormat("yyyy-MM-dd").format(new Date(quote.getDate_generated()*1000))), 12,(int)(w/2)+ 5, line_pos);

        line_pos=temp_pos;

        //right content
        contents.endText();
        PDImageXObject logo = PDImageXObject.createFromFile("images/logo.png", document);
        contents.drawImage(logo, (int)(w/2)+ 20, line_pos-logo_h-10, 150, logo_h);

        line_pos-=LINE_HEIGHT*4;
        temp_pos = line_pos;

        //horizontal solid line after company logo
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, line_pos+LINE_HEIGHT/2);
        contents.lineTo(w-10, line_pos+LINE_HEIGHT/2);
        contents.stroke();

        //horizontal solid line after consultants heading
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, line_pos-LINE_HEIGHT/2);
        contents.lineTo(w-10, line_pos-LINE_HEIGHT/2);
        contents.stroke();
        contents.beginText();

        //left text
        addTextToPageStream(contents,"Client Information", PDType1Font.COURIER_BOLD_OBLIQUE, 15,20, line_pos);
        line_pos-=LINE_HEIGHT;
        addTextToPageStream(contents,"Company: " + client.getClient_name(), 12, 20, line_pos);
        line_pos-=LINE_HEIGHT;
        addTextToPageStream(contents,"Company Tel: " + client.getTel(), 12,20, line_pos);

        line_pos=temp_pos;

        //right content
        addTextToPageStream(contents,"Job Number #" + job.getJob_number(), PDType1Font.COURIER_BOLD_OBLIQUE, 15, (int)(w/2)+ 5, line_pos);
        line_pos-=LINE_HEIGHT;
        addTextToPageStream(contents,"Date Generated:  " + (new SimpleDateFormat("yyyy-MM-dd").format(new Date(job.getDate_logged()*1000))), 12, (int)(w/2)+ 5, line_pos);
        line_pos-=LINE_HEIGHT;
        addTextToPageStream(contents,"Date Started:  " + (new SimpleDateFormat("yyyy-MM-dd").format(new Date(job.getDate_started()*1000))), 12,(int)(w/2)+ 5, line_pos);
        line_pos-=LINE_HEIGHT;
        addTextToPageStream(contents,"Date Completed:  " + (new SimpleDateFormat("yyyy-MM-dd").format(new Date(job.getDate_completed()*1000))), 12,(int)(w/2)+ 5, line_pos);
        line_pos-=LINE_HEIGHT;
        addTextToPageStream(contents,"Creator:  " + invoice.getCreator(), 12,(int)(w/2)+ 5, line_pos);
        //contents.endText();
        //PDImageXObject logo = PDImageXObject.createFromFile("images/logo.png", document);
        //contents.drawImage(logo, (int)(w/2)+ 20, line_pos-logo_h, 150, logo_h);

        line_pos-=LINE_HEIGHT;

        //horizontal solid line after job details
        contents.endText();
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, line_pos-LINE_HEIGHT/2);
        contents.lineTo(w-10, line_pos-LINE_HEIGHT/2);
        contents.stroke();
        contents.beginText();

        //horizontal solid line after company details
        contents.endText();
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, line_pos-LINE_HEIGHT/2);
        contents.lineTo(w-10, line_pos-LINE_HEIGHT/2);
        contents.stroke();
        contents.beginText();

        line_pos-=LINE_HEIGHT;//next line

        temp_pos = line_pos;
        //left text
        addTextToPageStream(contents,"Contact Person:  " + contact.toString(), PDType1Font.HELVETICA_BOLD, 12,20, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(contents,"Tel    :  " + contact.getTel(), PDType1Font.HELVETICA_BOLD, 12,120, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(contents,"Cell   :  " + contact.getCell(), PDType1Font.HELVETICA_BOLD, 12,120, line_pos);
        line_pos-=LINE_HEIGHT;//next line
        addTextToPageStream(contents,"eMail :  " + contact.getEmail(), PDType1Font.HELVETICA_BOLD, 12,120, line_pos);

        //horizontal solid line
        contents.endText();
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, line_pos-LINE_HEIGHT/2);
        contents.lineTo(w-10, line_pos-LINE_HEIGHT/2);
        contents.stroke();
        contents.beginText();

        line_pos-=LINE_HEIGHT;//next line (for external consultants)
        //temp_pos-=LINE_HEIGHT;//next line (for internal consultants)
        //Render sale representatives
        int int_rep_count=0;
        for(Employee employee : reps)
        {
            //if the page can't hold 4 more lines add a new page
            if(line_pos-(4*LINE_HEIGHT)<h-logo_h-(ROW_COUNT*LINE_HEIGHT) || temp_pos-(4*LINE_HEIGHT)<h-logo_h-(ROW_COUNT*LINE_HEIGHT))
            {
                addTextToPageStream(contents, "Page "+quote_page_count, PDType1Font.HELVETICA_OBLIQUE, 14,(int)(w/2)-20, 50);
                //add new page
                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                //TODO: setup page, i.e. draw lines and stuff
                contents.close();
                contents = new PDPageContentStream(document, page);
                temp_pos = (int)h-logo_h;
                line_pos = (int)h-logo_h;

                createLinesAndBordersOnPage(contents, (int)w, line_pos, line_pos+LINE_HEIGHT/2);

                contents.beginText();
                quote_page_count++;
            }

            if(!employee.isActiveVal())//external employee
            {
                addTextToPageStream(contents,"Contact Person:   " + employee.toString(), 12,20, line_pos);
                line_pos-=LINE_HEIGHT;//next line
                addTextToPageStream(contents,"Tel    :  " + employee.getTel(), 12,120, line_pos);
                line_pos-=LINE_HEIGHT;//next line
                addTextToPageStream(contents,"Cell   :  " + employee.getCell(), 12,120, line_pos);
                line_pos-=LINE_HEIGHT;//next line
                addTextToPageStream(contents,"eMail :  " + employee.getEmail(), 12,120, line_pos);
                line_pos-=LINE_HEIGHT;//next line
            }else {//internal representatives
                if(int_rep_count==0)//make first internal rep bold
                {
                    addTextToPageStream(contents, "Sale Consultant:  " + employee.toString(), PDType1Font.HELVETICA_BOLD, 12, (int) (w / 2) + 5, temp_pos);
                    temp_pos -= LINE_HEIGHT;//next line
                    addTextToPageStream(contents, "Tel    :  " + employee.getTel(), PDType1Font.HELVETICA_BOLD, 12, (int) (w / 2) + 105, temp_pos);
                    temp_pos -= LINE_HEIGHT;//next line
                    addTextToPageStream(contents, "Cell   :  " + employee.getCell(), PDType1Font.HELVETICA_BOLD, 12, (int) (w / 2) + 105, temp_pos);
                    temp_pos -= LINE_HEIGHT;//next line
                    addTextToPageStream(contents, "eMail :  " + employee.getEmail(), PDType1Font.HELVETICA_BOLD, 12, (int) (w / 2) + 105, temp_pos);
                    temp_pos -= LINE_HEIGHT;//next line
                }else
                {
                    addTextToPageStream(contents, "Sale Consultant:  " + employee.toString(), 12, (int) (w / 2) + 5, temp_pos);
                    temp_pos -= LINE_HEIGHT;//next line
                    addTextToPageStream(contents, "Tel    :  " + employee.getTel(), 12, (int) (w / 2) + 105, temp_pos);
                    temp_pos -= LINE_HEIGHT;//next line
                    addTextToPageStream(contents, "Cell   :  " + employee.getCell(), 12, (int) (w / 2) + 105, temp_pos);
                    temp_pos -= LINE_HEIGHT;//next line
                    addTextToPageStream(contents, "eMail :  " + employee.getEmail(), 12, (int) (w / 2) + 105, temp_pos);
                    temp_pos -= LINE_HEIGHT;//next line
                }
                int_rep_count++;
            }
        }
        //set the cursor to the line after the sale/client rep info
        line_pos = line_pos<temp_pos?line_pos:temp_pos;
        addTextToPageStream(contents,"Request: " + quote.getRequest(),PDType1Font.HELVETICA, 13,20, line_pos);
        line_pos-=LINE_HEIGHT;//next line

        contents.endText();

        //erase middle line by request field
        /*contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, line_pos+LINE_HEIGHT/2);
        contents.lineTo(w-10, line_pos+LINE_HEIGHT/2);
        contents.stroke();*/

        //horizontal solid line after reps
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, line_pos+LINE_HEIGHT+LINE_HEIGHT/2);
        contents.lineTo(w-10, line_pos+LINE_HEIGHT+LINE_HEIGHT/2);
        contents.stroke();
        //horizontal solid line after request
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, line_pos+LINE_HEIGHT/2);
        contents.lineTo(w-10, line_pos+LINE_HEIGHT/2);
        contents.stroke();
        //solid horizontal line after site location, before quote_items
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, (line_pos-LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
        contents.lineTo(w-10, (line_pos-LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
        contents.stroke();

        int col_divider_start = line_pos-LINE_HEIGHT;

        //vertical line going through center of page
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo((w/2), center_vert_line_start+LINE_HEIGHT/2);
        contents.lineTo((w/2),(col_divider_start+LINE_HEIGHT*2+(int)Math.ceil(LINE_HEIGHT/2)));
        contents.stroke();
        //
        contents.moveTo((w/2), (col_divider_start+(int)Math.ceil(LINE_HEIGHT/2)));
        contents.lineTo((w/2),(col_divider_start-LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
        contents.stroke();

        contents.beginText();
        addTextToPageStream(contents,"Site Location: " + quote.getSitename(),PDType1Font.HELVETICA, 13,20, line_pos);
        line_pos-=LINE_HEIGHT;//next line

        contents.endText();
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, (line_pos-LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
        contents.lineTo(w-10, (line_pos-LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
        contents.stroke();
        contents.beginText();

        //Column headings
        int col_pos = 10;
        addTextToPageStream(contents,"Item No.", PDType1Font.COURIER_BOLD,14,15, line_pos);
        col_pos += 80;
        addTextToPageStream(contents,"Equipment description", PDType1Font.COURIER_BOLD,14,col_pos+20, line_pos);
        col_pos = (int)(w/2);
        String[] cols = {"Unit", "Qty", "Rate", "Labour", "Total"};
        for(int i=0;i<5;i++)//7 cols in total
            addTextToPageStream(contents,cols[i], PDType1Font.COURIER_BOLD, 12,col_pos+(55*i)+10, line_pos);
        line_pos-=LINE_HEIGHT;//next line

        //Actual quote information
        col_pos = 10;
        double sub_total = 0;
        if(quote.getResources()!=null)
        {
            for(QuoteItem item: quote.getResources())
            {
                contents.endText();
                //quote content column dividers
                //#1
                contents.moveTo(80, (col_divider_start+(int)Math.ceil(LINE_HEIGHT/2)));
                contents.lineTo(80, line_pos-LINE_HEIGHT/2);
                contents.stroke();
                //vertical line going through center of page
                contents.setStrokingColor(Color.BLACK);
                contents.moveTo((w/2), (col_divider_start-LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
                contents.lineTo((w/2),line_pos-LINE_HEIGHT/2);
                contents.stroke();
                //#3+
                for(int i=1;i<5;i++)//7 cols in total
                {
                    contents.moveTo((w/2)+55*i, (col_divider_start+(int)Math.ceil(LINE_HEIGHT/2)));
                    contents.lineTo((w/2)+55*i,line_pos-LINE_HEIGHT/2);
                    contents.stroke();
                }
                contents.beginText();

                //if the page can't hold another 4 lines[current item, blank, sub-total, vat] add a new page
                if(line_pos-LINE_HEIGHT<h-logo_h-(ROW_COUNT*LINE_HEIGHT))
                {
                    addTextToPageStream(contents, "Page "+quote_page_count, PDType1Font.COURIER_OBLIQUE, 14,(int)(w/2)-20, 30);
                    //add new page
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    //TODO: setup page, i.e. draw lines and stuff
                    contents.close();
                    contents = new PDPageContentStream(document, page);
                    contents.beginText();
                    line_pos = (int)h-logo_h;
                    col_divider_start = line_pos+LINE_HEIGHT;
                    createLinesAndBordersOnPage(contents, (int)w, line_pos+LINE_HEIGHT/2, bottom_line);
                    quote_page_count++;
                }

                col_pos =0;//first column
                //Item col
                addTextToPageStream(contents, item.getItem_number(), 12,col_pos+30, line_pos);
                col_pos += 80;//next column
                //Description col
                addTextToPageStream(contents, item.getResource().getResource_name(), 12,col_pos+5, line_pos);
                col_pos = (int)w/2;//next column - starts at middle of page
                //Unit col
                addTextToPageStream(contents,item.getUnit(), 12,col_pos+5, line_pos);
                col_pos+=55;//next column
                //Quantity col
                addTextToPageStream(contents,item.getQuantity(), digit_font_size,col_pos+5, line_pos);
                col_pos+=55;//next column
                //Rate col
                addTextToPageStream(contents, String.valueOf(DecimalFormat.getCurrencyInstance().format(item.getRate())), digit_font_size,col_pos+5, line_pos);
                col_pos+=55;//next column
                //Labour col
                //addTextToPageStream(contents, String.valueOf(DecimalFormat.getCurrencyInstance().format(item.getLabourCost())), digit_font_size,col_pos+5, line_pos);
                col_pos+=55;//next column
                //Total col
                sub_total+=item.getTotal();
                addTextToPageStream(contents, String.valueOf(DecimalFormat.getCurrencyInstance().format(item.getTotal())), digit_font_size,col_pos+5, line_pos);

                line_pos -= LINE_HEIGHT;//next line
            }
            IO.log(TAG, IO.TAG_INFO, "successfully created quote PDF.");
        }else IO.log(TAG, IO.TAG_INFO, "quote has no resources.");
        col_pos = 0;
        //line_pos -= LINE_HEIGHT;//skip another line
        /*if the page can't hold another 2 lines add a new page
        if(line_pos-LINE_HEIGHT*2<h-logo_h-(ROW_COUNT*LINE_HEIGHT) || temp_pos-LINE_HEIGHT*2<h-logo_h-(ROW_COUNT*LINE_HEIGHT))
        {
            //add new page
            page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            //TODO: setup page, i.e. draw lines and stuff
            contents.close();
            contents = new PDPageContentStream(document, page);
            contents.beginText();
            line_pos = (int)h-logo_h;
            col_divider_start = line_pos+LINE_HEIGHT;
        }*/
        //solid horizontal line
        int col_divider_end= line_pos;

        contents.endText();
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, line_pos+LINE_HEIGHT/2);
        contents.lineTo(w-10, line_pos+LINE_HEIGHT/2);
        contents.stroke();

        contents.beginText();
        addTextToPageStream(contents, "Sub-Total Excl. VAT: ", PDType1Font.COURIER_BOLD_OBLIQUE, 14,col_pos+30, line_pos);
        addTextToPageStream(contents, String.valueOf(DecimalFormat.getCurrencyInstance().format(sub_total)), PDType1Font.COURIER_BOLD_OBLIQUE, 14,(int)(5+(w/2)), line_pos);
        line_pos -= LINE_HEIGHT;//next line

        //solid horizontal line
        contents.endText();
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, line_pos+LINE_HEIGHT/2);
        contents.lineTo(w-10, line_pos+LINE_HEIGHT/2);
        contents.stroke();

        double vat = sub_total*(quote.getVat()/100);
        contents.beginText();
        addTextToPageStream(contents, "VAT["+quote.getVat()+"%]: ", PDType1Font.COURIER_BOLD_OBLIQUE, 14,col_pos+30, line_pos);
        addTextToPageStream(contents, String.valueOf(DecimalFormat.getCurrencyInstance().format(vat)), PDType1Font.COURIER_BOLD_OBLIQUE, 14,(int)(5+(w/2)), line_pos);
        line_pos -= LINE_HEIGHT;//next line

        //solid horizontal line
        contents.endText();
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, line_pos+LINE_HEIGHT/2);
        contents.lineTo(w-10, line_pos+LINE_HEIGHT/2);
        contents.stroke();

        contents.beginText();
        addTextToPageStream(contents, "Total Incl. VAT: ", PDType1Font.COURIER_BOLD_OBLIQUE, 14,col_pos+30, line_pos);
        addTextToPageStream(contents, String.valueOf(DecimalFormat.getCurrencyInstance().format(sub_total + vat)), PDType1Font.COURIER_BOLD_OBLIQUE, 14,(int)(5+(w/2)), line_pos);
        contents.endText();
        line_pos -= LINE_HEIGHT;//next line

        //solid horizontal line
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, line_pos+LINE_HEIGHT/2);
        contents.lineTo(w-10, line_pos+LINE_HEIGHT/2);
        contents.stroke();

        //int col_divider_end = line_pos;
        line_pos -= LINE_HEIGHT*3;//next 3rd line
        /*solid horizontal lines after quote_items
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, col_divider_end+LINE_HEIGHT+LINE_HEIGHT/2);
        contents.lineTo(w-10, col_divider_end+LINE_HEIGHT+LINE_HEIGHT/2);
        contents.stroke();
        contents.moveTo(10, col_divider_end+LINE_HEIGHT/2);
        contents.lineTo(w-10, col_divider_end+LINE_HEIGHT/2);
        contents.stroke();
        contents.moveTo(10, col_divider_end-LINE_HEIGHT+LINE_HEIGHT/2);
        contents.lineTo(w-10, col_divider_end-LINE_HEIGHT+LINE_HEIGHT/2);
        contents.stroke();*/

        //quote content column dividers
        //#1
        contents.moveTo(80, (col_divider_start-LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
        contents.lineTo(80, col_divider_end+LINE_HEIGHT+LINE_HEIGHT/2);
        contents.stroke();
        //vertical line going through center of page again
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo((w/2), (col_divider_start-LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
        contents.lineTo((w/2), col_divider_end+LINE_HEIGHT+LINE_HEIGHT/2);
        //contents.lineTo((w/2),col_divider_end-LINE_HEIGHT/2);
        contents.stroke();
        //#3+
        for(int i=1;i<5;i++)//7 cols in total
        {
            contents.moveTo((w/2)+55*i, (col_divider_start-LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
            contents.lineTo((w/2)+55*i,col_divider_end+LINE_HEIGHT+LINE_HEIGHT/2);
            contents.stroke();
        }

        contents.beginText();

        if(quote.getExtra()!=null)
            addTextToPageStream(contents, "P.S. "+quote.getExtra(), PDType1Font.TIMES_ITALIC, 14,col_pos+5, line_pos);

        line_pos -= LINE_HEIGHT;//next line
        //if the page can't hold another 9 lines add a new page
        if(line_pos-(LINE_HEIGHT*4)<h-logo_h-(ROW_COUNT*LINE_HEIGHT))
        {
            addTextToPageStream(contents, "Page "+quote_page_count, PDType1Font.COURIER_OBLIQUE, 14,(int)(w/2)-20, 30);
            //add new page
            page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            contents.close();
            contents = new PDPageContentStream(document, page);
            contents.beginText();
            line_pos = (int)h-logo_h;
            createLinesAndBordersOnPage(contents, (int)w, line_pos+LINE_HEIGHT/2, bottom_line);
            quote_page_count++;
        }
        addTextToPageStream(contents, "TERMS AND CONDITIONS OF SALE", PDType1Font.HELVETICA_BOLD, 14,(int)(w/2)-130, line_pos);
        contents.endText();
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo((int)(w/2)-140, line_pos-LINE_HEIGHT/2);
        contents.lineTo((w/2)+120, line_pos-LINE_HEIGHT/2);
        contents.stroke();
        contents.beginText();

        line_pos -= LINE_HEIGHT;//next line
        addTextToPageStream(contents, "*Validity: Quote valid for 24 Hours.", PDType1Font.HELVETICA, 12,col_pos+30, line_pos);
        line_pos -= LINE_HEIGHT;//next line
        addTextToPageStream(contents, "*Payment Terms: COD / 30 Days on approved accounts. ", PDType1Font.HELVETICA, 12,col_pos+30, line_pos);
        line_pos -= LINE_HEIGHT;//next line
        addTextToPageStream(contents, "*Delivery: 1 - 6 Weeks, subject to stock availability.", PDType1Font.HELVETICA, 12,col_pos+30, line_pos);
        line_pos -= LINE_HEIGHT;//next line
        addTextToPageStream(contents, "*All pricing quoted, is subject to Rate of Exchange USD=R.", PDType1Font.HELVETICA_BOLD, 12,col_pos+30, line_pos);
        line_pos -= LINE_HEIGHT;//next line
        addTextToPageStream(contents, "*All goods / equipment remain the property of " + Globals.COMPANY.getValue()+ " until paid for completely. ", PDType1Font.HELVETICA, 12,col_pos+30, line_pos);
        line_pos -= LINE_HEIGHT;//next line
        addTextToPageStream(contents, "*" + Globals.COMPANY.getValue() + " reserves the right to retake posession of all equipment not paid for completely", PDType1Font.HELVETICA, 12,col_pos+30, line_pos);
        line_pos -= LINE_HEIGHT;//next line
        addTextToPageStream(contents, "  Within the payment term set out above.", PDType1Font.HELVETICA, 12,col_pos+30, line_pos);
        line_pos -= LINE_HEIGHT;//next line
        addTextToPageStream(contents, "*E & O E", PDType1Font.HELVETICA, 12,col_pos+30, line_pos);

        addTextToPageStream(contents, "Page "+quote_page_count, PDType1Font.COURIER_OBLIQUE, 14,(int)(w/2)-20, 30);
        contents.endText();
        contents.close();

        String path = "bin/pdf/invoice_" + job.get_id() + ".pdf";
        int i=1;
        while(new File(path).exists())
        {
            path = "bin/pdf/invoice_" + job.get_id() + "." + i + ".pdf";
            i++;
        }

        document.save(path);
        document.close();

        PDFViewer pdfViewer = PDFViewer.getInstance();
        pdfViewer.setVisible(true);
        pdfViewer.doOpen(path);
    }

    static int partition(Transaction arr[], int left, int right)
    {
        int i = left, j = right;
        Transaction tmp;
        double pivot = arr[(left + right) / 2].getDate();

        while (i <= j)
        {
            while (arr[i].getDate() < pivot)
                i++;
            while (arr[j].getDate() > pivot)
                j--;
            if (i <= j)
            {
                tmp = arr[i];
                arr[i] = arr[j];
                arr[j] = tmp;
                i++;
                j--;
            }
        }

        return i;
    }

    public static void quickSort(Transaction transactions[], int left, int right)
    {
        int index = partition(transactions, left, right);
        if (left < index - 1)
            quickSort(transactions, left, index - 1);
        if (index < right)
            quickSort(transactions, index, right);
    }

    public static void createGeneralJournalPdf(long start, long end) throws IOException
    {
        //Init managers and load data to memory
        AssetManager.getInstance().loadDataFromServer();
        ResourceManager.getInstance().loadDataFromServer();
        ExpenseManager.getInstance().loadDataFromServer();
        InvoiceManager.getInstance().loadDataFromServer();

        ArrayList<Transaction> transactions = new ArrayList<>();
        //Load assets
        for(Asset asset : AssetManager.getInstance().getAssets().values())
            transactions.add(new Transaction(asset.get_id(), asset.getDate_acquired(), asset));
        //Load Resources/Stock
        for(Resource resource : ResourceManager.getInstance().getResources().values())
            transactions.add(new Transaction(resource.get_id(), resource.getDate_acquired(), resource));
        //Load additional Expenses
        for(Expense expense: ExpenseManager.getInstance().getExpenses())
            transactions.add(new Transaction(expense.get_id(), expense.getDate_logged(), expense));
        //Load Service income (Invoices)
        for(Invoice invoice: InvoiceManager.getInstance().getInvoices().values())
            transactions.add(new Transaction(invoice.get_id(), invoice.getDate_generated(), invoice));

        Transaction[] transactions_arr = new Transaction[transactions.size()];
        transactions.toArray(transactions_arr);
        long start_ms = System.currentTimeMillis();
        quickSort(transactions_arr, 0, transactions.size()-1);
        //Arrays.sort(transactions_arr);
        //transactions_arr = selectionSort(transactions_arr);
        IO.log("PDF Creator> generateGeneralJournal",IO.TAG_INFO, "Sorted in: "+ (System.currentTimeMillis()-start_ms) + "ms");
        IO.log("PDF Creator> generateGeneralJournal",IO.TAG_INFO, "Transaction count: "+ transactions_arr.length);

        // Create a new document with an empty page.
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        // Adobe Acrobat uses Helvetica as a default font and
        // stores that under the name '/Helv' in the resources dictionary
        PDFont font = PDType1Font.HELVETICA;
        PDResources resources = new PDResources();
        resources.put(COSName.getPDFName("Helv"), font);

        PDPageContentStream contents = new PDPageContentStream(document, page);
        int logo_h = 60;
        PDImageXObject logo = PDImageXObject.createFromFile("images/logo.png", document);
        contents.drawImage(logo, 10, 770, 160, logo_h);

        float w = page.getBBox().getWidth();
        float h = page.getBBox().getHeight();
        int line_pos = (int)h-logo_h-20;
        int digit_font_size=9;

        /**Draw lines**/
        int center_vert_line_start = line_pos;
        int bottom_line = (int)h-logo_h-(ROW_COUNT+1)*LINE_HEIGHT;
        //createLinesAndBordersOnPage(contents, (int)w, line_pos, bottom_line);
        drawHorzLines(contents, line_pos, (int) w, new Insets(0,0,80,0));

        /** begin text from the top**/
        contents.beginText();
        contents.setFont(font, 12);
        line_pos-=LINE_HEIGHT/2;

        String gj_title = "General Journal";
        String date_start = (new SimpleDateFormat("yyyy/MM/dd").format(
                new Date(start * 1000)));
        String date_end = (new SimpleDateFormat("yyyy/MM/dd").format(
                new Date(end * 1000)));
        String date_generated = (new SimpleDateFormat("yyyy/MM/dd").format(
                new Date(System.currentTimeMillis())));
        addTextToPageStream(contents, gj_title, PDType1Font.COURIER_BOLD, 16,(int)(w-250), 830);
        addTextToPageStream(contents, "Company: " + Globals.COMPANY.getValue(), PDType1Font.COURIER, 12,(int)(w-250), 815);
        addTextToPageStream(contents, "Period: "+date_start+" - " + date_end, PDType1Font.COURIER, 12,(int)(w-200), 800);
        addTextToPageStream(contents, "Generated: " + date_generated, PDType1Font.COURIER, 12,(int)(w-200), 785);

        //horizontal solid line
        contents.endText();
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, line_pos-LINE_HEIGHT/2);
        contents.lineTo(w-10, line_pos-LINE_HEIGHT/2);
        contents.stroke();
        contents.beginText();
        line_pos-=LINE_HEIGHT;

        addTextToPageStream(contents,"Date", PDType1Font.COURIER_BOLD_OBLIQUE, 15,10, line_pos);
        addTextToPageStream(contents,"Account", PDType1Font.COURIER_BOLD_OBLIQUE, 15,150, line_pos);
        addTextToPageStream(contents,"Debit", PDType1Font.COURIER_BOLD_OBLIQUE, 15,(int)w-180, line_pos);
        addTextToPageStream(contents,"Credit", PDType1Font.COURIER_BOLD_OBLIQUE, 15,(int)w-100, line_pos);

        //horizontal solid line
        contents.endText();
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, line_pos-LINE_HEIGHT/2);
        contents.lineTo(w-10, line_pos-LINE_HEIGHT/2);
        contents.stroke();
        contents.beginText();

        line_pos-=LINE_HEIGHT;//next line

        int lines=0;
        int pages = 1;
        for(Transaction t : transactions_arr)
        {
            if(t.getDate()>=start && t.getDate()<=end)
            {
                if (lines > 15)
                {
                    //line_pos-=LINE_HEIGHT;
                    addTextToPageStream(contents, "page " + pages, PDType1Font.HELVETICA, 18, (int) (w / 2) - 50, 30);
                    contents.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    contents = new PDPageContentStream(document, page);
                    contents.drawImage(logo, 10, 770, 160, logo_h);
                    line_pos = (int) h - logo_h - 20;
                    //line_pos = 700;
                    drawHorzLines(contents, line_pos, (int) w, new Insets(0, 0, 80, 0));
                    line_pos -= LINE_HEIGHT / 2;
                    contents.beginText();

                    addTextToPageStream(contents, gj_title, PDType1Font.COURIER_BOLD, 16, (int) (w - 250), 830);
                    addTextToPageStream(contents, "Company: " + Globals.COMPANY.getValue(), PDType1Font.COURIER, 12, (int) (w - 250), 815);
                    addTextToPageStream(contents, "Period: "+date_start+" - " + date_end, PDType1Font.COURIER, 12,(int)(w-200), 800);
                    addTextToPageStream(contents, "Generated: " + date_generated, PDType1Font.COURIER, 12,(int)(w-200), 785);
                    line_pos -= LINE_HEIGHT;
                    pages++;
                    lines = 0;
                }
                addTextToPageStream(contents,
                        (new SimpleDateFormat("yyyy-MM-dd").format(
                                new Date(t.getDate() * 1000))),
                        PDType1Font.HELVETICA, 15, 10, line_pos);

                if (t.getBusinessObject() instanceof Invoice)
                {
                    //title
                    String title = ((Invoice) t.getBusinessObject()).getAccount();
                    if (title.length() <= 50)
                        addTextToPageStream(contents, title, PDType1Font.HELVETICA, 12, 105, line_pos);
                    if (title.length() > 50 && title.length() <= 60)
                        addTextToPageStream(contents, title, PDType1Font.HELVETICA, 10, 105, line_pos);
                    if (title.length() > 60)
                        addTextToPageStream(contents, title, PDType1Font.HELVETICA, 8, 105, line_pos);
                    //debit
                    double difference = 0;
                    if (((Invoice) t.getBusinessObject()).getJob() != null)
                        if (((Invoice) t.getBusinessObject()).getJob().getQuote() != null)
                            difference = ((Invoice) t.getBusinessObject()).getJob().getQuote().getTotal() - ((Invoice) t.getBusinessObject()).getReceivable();
                    addTextToPageStream(contents, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(difference), PDType1Font.HELVETICA, 15, (int) w - 180, line_pos);
                    line_pos -= LINE_HEIGHT;//next line
                    //account
                    addTextToPageStream(contents, "Accounts Receivable", PDType1Font.HELVETICA, 15, 105, line_pos);
                    //debit
                    addTextToPageStream(contents, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(((Invoice) t.getBusinessObject()).getReceivable()), PDType1Font.HELVETICA, 15, (int) w - 180, line_pos);
                    line_pos -= LINE_HEIGHT;//next line
                    //account
                    addTextToPageStream(contents, "Service Revenue", PDType1Font.HELVETICA, 15, 150, line_pos);
                    //credit
                    addTextToPageStream(contents, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(((Invoice) t.getBusinessObject()).getTotal()), PDType1Font.HELVETICA, 15, (int) w - 100, line_pos);
                } else if (t.getBusinessObject() instanceof Expense)
                {
                    //title
                    String title = ((Expense) t.getBusinessObject()).getExpense_title();
                    if (title.length() <= 50)
                        addTextToPageStream(contents, title, PDType1Font.HELVETICA, 12, 105, line_pos);
                    if (title.length() > 50 && title.length() <= 60)
                        addTextToPageStream(contents, title, PDType1Font.HELVETICA, 10, 105, line_pos);
                    if (title.length() > 60)
                        addTextToPageStream(contents, title, PDType1Font.HELVETICA, 8, 105, line_pos);
                    //debit
                    addTextToPageStream(contents, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(((Expense) t.getBusinessObject()).getExpense_value()), PDType1Font.HELVETICA, 15, (int) w - 180, line_pos);
                    line_pos -= LINE_HEIGHT;//next line
                    //account
                    addTextToPageStream(contents, ((Expense) t.getBusinessObject()).getAccount(), PDType1Font.HELVETICA, 15, 150, line_pos);
                    //credit
                    addTextToPageStream(contents, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(((Expense) t.getBusinessObject()).getExpense_value()), PDType1Font.HELVETICA, 15, (int) w - 100, line_pos);
                } else if (t.getBusinessObject() instanceof Asset)
                {
                    //title
                    String title = ((Asset) t.getBusinessObject()).getAsset_name();
                    if (title.length() <= 50)
                        addTextToPageStream(contents, "Purchased asset: " + title, PDType1Font.HELVETICA, 12, 105, line_pos);
                    if (title.length() > 50 && title.length() <= 60)
                        addTextToPageStream(contents, "Purchased asset: " + title, PDType1Font.HELVETICA, 10, 105, line_pos);
                    if (title.length() > 60)
                        addTextToPageStream(contents, "Purchased asset: " + title, PDType1Font.HELVETICA, 8, 105, line_pos);
                    //debit
                    addTextToPageStream(contents, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(((Asset) t.getBusinessObject()).getAsset_value()), PDType1Font.HELVETICA, 15, (int) w - 180, line_pos);
                    line_pos -= LINE_HEIGHT;//next line
                    //account
                    //addTextToPageStream(contents, ((Asset) t.getBusinessObject()).getAccount(), PDType1Font.HELVETICA, 15, 150, line_pos);
                    //credit
                    addTextToPageStream(contents, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(((Asset) t.getBusinessObject()).getAsset_value()), PDType1Font.HELVETICA, 15, (int) w - 100, line_pos);
                } else if (t.getBusinessObject() instanceof Resource)
                {
                    //title
                    String title = ((Resource) t.getBusinessObject()).getResource_name();
                    if (title.length() <= 50)
                        addTextToPageStream(contents, "Purchased stock: " + title, PDType1Font.HELVETICA, 12, 105, line_pos);
                    if (title.length() > 50 && title.length() <= 60)
                        addTextToPageStream(contents, "Purchased stock: " + title, PDType1Font.HELVETICA, 10, 105, line_pos);
                    if (title.length() > 60)
                        addTextToPageStream(contents, "Purchased stock: " + title, PDType1Font.HELVETICA, 8, 105, line_pos);
                    //debit
                    addTextToPageStream(contents, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(((Resource) t.getBusinessObject()).getResource_value()), PDType1Font.HELVETICA, 15, (int) w - 180, line_pos);
                    line_pos -= LINE_HEIGHT;//next line
                    //account
                    //addTextToPageStream(contents, ((Resource) t.getBusinessObject()).getAccount(), PDType1Font.HELVETICA, 15, 150, line_pos);
                    //credit
                    addTextToPageStream(contents, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(((Resource) t.getBusinessObject()).getResource_value()), PDType1Font.HELVETICA, 15, (int) w - 100, line_pos);
                }
                //addTextToPageStream(contents,"Account", PDType1Font.COURIER_BOLD_OBLIQUE, 15,100, line_pos);
                //addTextToPageStream(contents,"Credit", PDType1Font.COURIER_BOLD_OBLIQUE, 15,(int)w-100, line_pos);
                lines++;
                line_pos -= LINE_HEIGHT;//next line
            }
        }
        addTextToPageStream(contents, "page " + pages, PDType1Font.HELVETICA, 18, (int)(w/2)-50, 30);

        //horizontal solid line
        contents.endText();
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(0, line_pos-LINE_HEIGHT/2);
        contents.lineTo(w, line_pos-LINE_HEIGHT/2);
        contents.stroke();

        String path = "bin/pdf/general_journal.pdf";
        int i=1;
        while(new File(path).exists())
        {
            path = "bin/pdf/general_journal" + "." + i + ".pdf";
            i++;
        }

        contents.close();
        document.save(path);
        document.close();

        PDFViewer pdfViewer = PDFViewer.getInstance();
        pdfViewer.setVisible(true);
        pdfViewer.doOpen(path);
    }

    private static PDPageContentStream initLedgerPage(PDDocument document,String account, long start, long end) throws IOException
    {
        // Create a new document with an empty page.
        //PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        // Adobe Acrobat uses Helvetica as a default font and
        // stores that under the name '/Helv' in the resources dictionary
        PDFont font = PDType1Font.HELVETICA;
        PDResources resources = new PDResources();
        resources.put(COSName.getPDFName("Helv"), font);

        PDPageContentStream contents = new PDPageContentStream(document, page);
        int logo_h = 60;
        PDImageXObject logo = PDImageXObject.createFromFile("images/logo.png", document);
        contents.drawImage(logo, 10, 770, 160, logo_h);

        float w = page.getBBox().getWidth();
        float h = page.getBBox().getHeight();
        int line_pos = (int)h-logo_h-20;

        /**Draw lines**/
        drawHorzLines(contents, line_pos, (int) w, new Insets(0,0,80,0));

        /** begin text from the top**/
        contents.beginText();
        contents.setFont(font, 12);
        line_pos-=LINE_HEIGHT/2;

        String ledger_title = account.substring(0,1).toUpperCase() + account.substring(1) + " General Ledger";
        String date_start = (new SimpleDateFormat("yyyy/MM/dd").format(
                new Date(start * 1000)));
        String date_end = (new SimpleDateFormat("yyyy/MM/dd").format(
                new Date(end * 1000)));
        String date_generated = (new SimpleDateFormat("yyyy/MM/dd").format(
                new Date(System.currentTimeMillis())));
        addTextToPageStream(contents, ledger_title, PDType1Font.COURIER_BOLD, 16,(int)(w/2-100), 830);
        addTextToPageStream(contents, "Company: " + Globals.COMPANY.getValue(), PDType1Font.COURIER, 12,(int)(w/2-100), 815);
        addTextToPageStream(contents, "Period: "+date_start+" - " + date_end, PDType1Font.COURIER, 12,(int)(w/2-100), 800);
        addTextToPageStream(contents, "Generated: " + date_generated, PDType1Font.COURIER, 12,(int)(w/2-100), 785);

        //horizontal solid line
        contents.endText();
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, line_pos-LINE_HEIGHT/2);
        contents.lineTo(w-10, line_pos-LINE_HEIGHT/2);
        contents.stroke();
        contents.beginText();
        line_pos-=LINE_HEIGHT;

        addTextToPageStream(contents,"Date", PDType1Font.COURIER_BOLD_OBLIQUE, 15,10, line_pos);
        addTextToPageStream(contents,"Description", PDType1Font.COURIER_BOLD_OBLIQUE, 15,150, line_pos);
        addTextToPageStream(contents,"Debit", PDType1Font.COURIER_BOLD_OBLIQUE, 15,(int)w-180, line_pos);
        addTextToPageStream(contents,"Credit", PDType1Font.COURIER_BOLD_OBLIQUE, 15,(int)w-100, line_pos);

        //horizontal solid line
        contents.endText();
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, line_pos-LINE_HEIGHT/2);
        contents.lineTo(w-10, line_pos-LINE_HEIGHT/2);
        contents.stroke();
        //contents.beginText();

        return contents;
    }

    public static void createGeneralLedgerPdf(long start, long end) throws IOException
    {
        //Init managers and load data to memory
        AssetManager.getInstance().loadDataFromServer();
        ResourceManager.getInstance().loadDataFromServer();
        ExpenseManager.getInstance().loadDataFromServer();
        InvoiceManager.getInstance().loadDataFromServer();
        RevenueManager.getInstance().loadDataFromServer();

        ArrayList<Transaction> transactions = new ArrayList<>();
        //Load assets
        for(Asset asset : AssetManager.getInstance().getAssets().values())
            transactions.add(new Transaction(asset.get_id(), asset.getDate_acquired(), asset));
        //Load Resources/Stock
        for(Resource resource : ResourceManager.getInstance().getResources().values())
            transactions.add(new Transaction(resource.get_id(), resource.getDate_acquired(), resource));
        //Load additional Expenses
        for(Expense expense: ExpenseManager.getInstance().getExpenses())
            transactions.add(new Transaction(expense.get_id(), expense.getDate_logged(), expense));
        //Load Service revenue (Invoices)
        for(Invoice invoice: InvoiceManager.getInstance().getInvoices().values())
            transactions.add(new Transaction(invoice.get_id(), invoice.getDate_generated(), invoice));
        //Load Additional income/revenue
        for(Revenue revenue: RevenueManager.getInstance().getRevenues())
            transactions.add(new Transaction(revenue.get_id(), revenue.getDate_logged(), revenue));

        Transaction[] transactions_arr = new Transaction[transactions.size()];
        transactions.toArray(transactions_arr);
        long start_ms = System.currentTimeMillis();
        quickSort(transactions_arr, 0, transactions.size()-1);
        //Arrays.sort(transactions_arr);
        //transactions_arr = selectionSort(transactions_arr);
        System.out.println("Sorted in: "+ (System.currentTimeMillis()-start_ms) + "ms");
        System.out.println("Transaction count: "+ transactions_arr.length);

        HashMap<String, Account> accounts_map = new HashMap<>();
        for(Transaction t : transactions_arr)
        {
            if(t.getBusinessObject() instanceof Expense)
            {
                Expense expense = ((Expense)t.getBusinessObject());
                String str_acc = expense.getAccount();
                Account acc = accounts_map.get(str_acc.toLowerCase());
                if(acc==null)
                {
                    //account not in map yet create it.
                    acc = new Account(str_acc.toLowerCase(), expense.getExpense_value(), 0);
                    acc.addTransaction(t);
                    accounts_map.put(str_acc.toLowerCase(), acc);
                }else{
                    //account in map, update its debit
                    acc.setDebit(acc.getDebit()+expense.getExpense_value());
                    acc.addTransaction(t);
                }
            }else if(t.getBusinessObject() instanceof Asset)
            {
                Asset asset = ((Asset)t.getBusinessObject());
                String str_acc = "%ACCOUNT";//asset.getAccount();
                Account acc = accounts_map.get(str_acc.toLowerCase());
                if(acc==null)
                {
                    //account not in map yet create it.
                    acc = new Account(str_acc.toLowerCase(), asset.getAsset_value(), 0);
                    acc.addTransaction(t);
                    accounts_map.put(str_acc.toLowerCase(), acc);
                }else{
                    //account in map, update its debit
                    acc.setDebit(acc.getDebit()+asset.getAsset_value());
                    acc.addTransaction(t);
                }
            }else if(t.getBusinessObject() instanceof Resource)
            {
                Resource resource = ((Resource)t.getBusinessObject());
                String str_acc = "%ACCOUNT";//resource.getAccount();
                Account acc = accounts_map.get(str_acc.toLowerCase());
                if(acc==null)
                {
                    //account not in map yet create it.
                    acc = new Account(str_acc.toLowerCase(), resource.getResource_value(), 0);
                    acc.addTransaction(t);
                    accounts_map.put(str_acc.toLowerCase(), acc);
                }else{
                    //account in map, update its debit
                    acc.setDebit(acc.getDebit()+resource.getResource_value());
                    acc.addTransaction(t);
                }
            }else if(t.getBusinessObject() instanceof Invoice)
            {
                Invoice invoice = ((Invoice)t.getBusinessObject());
                String str_acc = invoice.getAccount();
                Account acc = accounts_map.get(str_acc.toLowerCase());
                if(acc==null)
                {
                    //account not in map yet create it.
                    acc = new Account(str_acc.toLowerCase(), 0, invoice.getTotal());
                    acc.addTransaction(t);
                    accounts_map.put(str_acc.toLowerCase(), acc);
                }else{
                    //account in map, update its credit
                    acc.setCredit(acc.getCredit()+invoice.getTotal());
                    acc.addTransaction(t);
                }
                //Update service revenue
                acc = accounts_map.get("service revenue");
                if(acc==null)
                {
                    //account not in map yet create it.
                    acc = new Account("service revenue", 0, invoice.getTotal());
                    acc.addTransaction(t);
                    accounts_map.put("service revenue", acc);
                }else{
                    //account in map, update its credit
                    acc.setCredit(acc.getCredit()+invoice.getTotal());
                    acc.addTransaction(t);
                }
                //check difference between received and sale total
                double difference = 0;
                if (((Invoice) t.getBusinessObject()).getJob() != null)
                    if (((Invoice) t.getBusinessObject()).getJob().getQuote() != null)
                        difference = ((Invoice) t.getBusinessObject()).getJob().getQuote().getTotal() - ((Invoice) t.getBusinessObject()).getReceivable();
                if(difference>0)
                {
                    //customer paid on credit, add to accounts receivable
                    //Update accounts receivable
                    acc = accounts_map.get("accounts receivable");
                    if(acc==null)
                    {
                        //account not in map yet create it.
                        acc = new Account("accounts receivable",difference, 0);
                        acc.addTransaction(t);
                        accounts_map.put("accounts receivable", acc);
                    }else{
                        //account in map, update its credit
                        acc.setDebit(acc.getDebit()+difference);
                        acc.addTransaction(t);
                    }
                }
            }else if(t.getBusinessObject() instanceof Revenue)
            {
                Revenue revenue = ((Revenue)t.getBusinessObject());
                String str_acc = revenue.getAccount();
                Account acc = accounts_map.get(str_acc.toLowerCase());
                if(acc==null)
                {
                    //account not in map yet create it.
                    acc = new Account(str_acc.toLowerCase(), 0, revenue.getRevenue_value());
                    acc.addTransaction(t);
                    accounts_map.put(str_acc.toLowerCase(), acc);
                }else{
                    //account in map, update its credit
                    acc.setCredit(acc.getCredit()+revenue.getRevenue_value());
                    acc.addTransaction(t);
                }
                //Update additional revenue
                acc = accounts_map.get("additional revenue");
                if(acc==null)
                {
                    //account not in map yet create it.
                    acc = new Account("additional revenue", 0, revenue.getRevenue_value());
                    acc.addTransaction(t);
                    accounts_map.put("additional revenue", acc);
                }else{
                    //account in map, update its credit
                    acc.setCredit(acc.getCredit()+revenue.getRevenue_value());
                    acc.addTransaction(t);
                }
            }
        }

        PDDocument document = new PDDocument();
        PDPageContentStream contents = null;
        int logo_h = 60, pages= 1, line_pos= 0, w=0, h=0;

        for(Account account : accounts_map.values())
        {
            System.out.println("account: " + account.getAccount_name() + ", debit: "+ account.getDebit() +
                                ", credit: " + account.getCredit()+", transactions: "+ account.getTransactions().size());

            if(contents!=null)
            {
                contents.endText();
                contents.close();
            }
            contents = initLedgerPage(document, account.getAccount_name(), start, end);
            contents.beginText();
            h = (int)document.getPage(0).getBBox().getHeight();
            w = (int)document.getPage(0).getBBox().getWidth();
            line_pos = h-logo_h-LINE_HEIGHT*3-LINE_HEIGHT/2;
            pages++;
            int lines=0;
            double credit=0;
            double debit=0;

            //Generate ledger for each account
            for(Transaction transaction : account.getTransactions())
            {
                if(transaction.getDate()>=start && transaction.getDate()<=end)
                {
                    if (lines > 32)
                    {
                        //line_pos-=LINE_HEIGHT;
                        addTextToPageStream(contents, "page " + pages, PDType1Font.HELVETICA, 18, (int) (w / 2) - 50, 30);
                        contents.close();
                        contents = initLedgerPage(document, account.getAccount_name(), start, end);
                        contents.beginText();
                        pages++;
                        lines = 0;
                        line_pos = h-logo_h-LINE_HEIGHT*3-LINE_HEIGHT/2;
                    }
                    addTextToPageStream(contents,
                            (new SimpleDateFormat("yyyy-MM-dd").format(
                                    new Date(transaction.getDate() * 1000))),
                            PDType1Font.HELVETICA, 15, 10, line_pos);

                    if (transaction.getBusinessObject() instanceof Revenue)
                    {
                        System.out.println(((Revenue) transaction.getBusinessObject()).getRevenue_title() + " in Account: " + account.getAccount_name());
                        //title
                        String title = ((Revenue) transaction.getBusinessObject()).getRevenue_title();
                        //TODO: add to Additional Revenue account
                        addTextToPageStream(contents, title, PDType1Font.HELVETICA, 15, 105, line_pos);
                        //debit
                        /*double difference = 0;
                        if (((Invoice) transaction.getBusinessObject()).getJob() != null)
                            if (((Invoice) transaction.getBusinessObject()).getJob().getQuote() != null)
                                difference = ((Invoice) transaction.getBusinessObject()).getJob().getQuote().getTotal() - ((Invoice) t.getBusinessObject()).getReceivable();*/
                        if(!account.getAccount_name().toLowerCase().equals("additional revenue") && !account.getAccount_name().toLowerCase().equals("accounts receivable"))
                        {
                            //debit
                            addTextToPageStream(contents, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(((Revenue) transaction.getBusinessObject()).getRevenue_value()), PDType1Font.HELVETICA, 15, w - 180, line_pos);
                            debit += ((Revenue) transaction.getBusinessObject()).getRevenue_value();
                        }else
                        {
                            //credit
                            addTextToPageStream(contents, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(((Revenue) transaction.getBusinessObject()).getRevenue_value()), PDType1Font.HELVETICA, 15, w - 100, line_pos);
                            credit += ((Revenue) transaction.getBusinessObject()).getRevenue_value();
                        }
                    }else if (transaction.getBusinessObject() instanceof Invoice)
                    {
                        //title
                        String title = "Service Revenue";
                        //TODO: add to Service Revenue account if diff>0 add to accounts receivable
                        addTextToPageStream(contents, title, PDType1Font.HELVETICA, 15, 105, line_pos);
                        //debit
                        double difference = 0;
                        if (((Invoice) transaction.getBusinessObject()).getJob() != null)
                            if (((Invoice) transaction.getBusinessObject()).getJob().getQuote() != null)
                                difference = ((Invoice) transaction.getBusinessObject()).getJob().getQuote().getTotal() - ((Invoice) transaction.getBusinessObject()).getReceivable();
                        double invoice_total = ((Invoice) transaction.getBusinessObject()).getJob().getQuote().getTotal();
                        if(!account.getAccount_name().toLowerCase().equals("service revenue"))
                        {
                            if(!account.getAccount_name().toLowerCase().equals("accounts receivable"))
                            {
                                //debit
                                addTextToPageStream(contents, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(invoice_total), PDType1Font.HELVETICA, 15, w - 180, line_pos);
                                debit += invoice_total;
                            }else{
                                //purchased on credit, debit accounts receivable
                                addTextToPageStream(contents, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(difference), PDType1Font.HELVETICA, 15, w - 180, line_pos);
                                debit += difference;
                            }
                        }else
                        {
                            //credit
                            addTextToPageStream(contents, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(invoice_total), PDType1Font.HELVETICA, 15, w - 100, line_pos);
                            credit += invoice_total;
                        }
                    } else if (transaction.getBusinessObject() instanceof Expense)
                    {
                        //title
                        String title = ((Expense) transaction.getBusinessObject()).getExpense_title();
                        if (title.length() <= 50)
                            addTextToPageStream(contents, title, PDType1Font.HELVETICA, 14, 105, line_pos);
                        if (title.length() > 50 && title.length() <= 60)
                            addTextToPageStream(contents, title, PDType1Font.HELVETICA, 10, 105, line_pos);
                        if (title.length() > 60)
                            addTextToPageStream(contents, title, PDType1Font.HELVETICA, 8, 105, line_pos);
                        //debit
                        //addTextToPageStream(contents, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(((Expense) transaction.getBusinessObject()).getExpense_value()), PDType1Font.HELVETICA, 15, (int) w - 180, line_pos);
                        //credit
                        addTextToPageStream(contents, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(((Expense) transaction.getBusinessObject()).getExpense_value()), PDType1Font.HELVETICA, 15,  w - 100, line_pos);
                        credit+=((Expense) transaction.getBusinessObject()).getExpense_value();
                    } else if (transaction.getBusinessObject() instanceof Asset)
                    {
                        //title
                        String title = ((Asset) transaction.getBusinessObject()).getAsset_name();
                        if (title.length() <= 50)
                            addTextToPageStream(contents, "Purchased asset: " + title, PDType1Font.HELVETICA, 12, 105, line_pos);
                        if (title.length() > 50 && title.length() <= 60)
                            addTextToPageStream(contents, "Purchased asset: " + title, PDType1Font.HELVETICA, 10, 105, line_pos);
                        if (title.length() > 60)
                            addTextToPageStream(contents, "Purchased asset: " + title, PDType1Font.HELVETICA, 8, 105, line_pos);
                        //debit
                        addTextToPageStream(contents, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(((Asset) transaction.getBusinessObject()).getAsset_value()), PDType1Font.HELVETICA, 15,w - 180, line_pos);
                        debit+=((Asset) transaction.getBusinessObject()).getAsset_value();
                        //credit
                        //addTextToPageStream(contents, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(((Asset) transaction.getBusinessObject()).getAsset_value()), PDType1Font.HELVETICA, 15, (int) w - 100, line_pos);
                    } else if (transaction.getBusinessObject() instanceof Resource)
                    {
                        //title
                        String title = ((Resource) transaction.getBusinessObject()).getResource_name();
                        if (title.length() <= 50)
                            addTextToPageStream(contents, "Purchased stock: " + title, PDType1Font.HELVETICA, 12, 105, line_pos);
                        if (title.length() > 50 && title.length() <= 60)
                            addTextToPageStream(contents, "Purchased stock: " + title, PDType1Font.HELVETICA, 10, 105, line_pos);
                        if (title.length() > 60)
                            addTextToPageStream(contents, "Purchased stock: " + title, PDType1Font.HELVETICA, 8, 105, line_pos);
                        //debit
                        addTextToPageStream(contents, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(((Resource) transaction.getBusinessObject()).getResource_value()), PDType1Font.HELVETICA, 15, w - 180, line_pos);
                        debit+=((Resource) transaction.getBusinessObject()).getResource_value();
                        //credit
                        //addTextToPageStream(contents, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(((Resource) transaction.getBusinessObject()).getResource_value()), PDType1Font.HELVETICA, 15, (int) w - 100, line_pos);
                    }
                    //addTextToPageStream(contents,"Account", PDType1Font.COURIER_BOLD_OBLIQUE, 15,100, line_pos);
                    //addTextToPageStream(contents,"Credit", PDType1Font.COURIER_BOLD_OBLIQUE, 15,(int)w-100, line_pos);
                    lines++;
                    line_pos -= LINE_HEIGHT;//next line
                }
            }
            //horizontal solid line
            contents.endText();
            contents.setStrokingColor(Color.BLACK);
            contents.moveTo(0, line_pos-LINE_HEIGHT/2+LINE_HEIGHT);
            contents.lineTo(w, line_pos-LINE_HEIGHT/2+LINE_HEIGHT);
            contents.stroke();
            contents.beginText();

            addTextToPageStream(contents, "Totals", PDType1Font.COURIER_BOLD_OBLIQUE, 16, 105, line_pos);
            addTextToPageStream(contents, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(debit), PDType1Font.HELVETICA, 14, w - 200, line_pos);
            addTextToPageStream(contents, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(credit), PDType1Font.HELVETICA, 14, w - 100, line_pos);
            line_pos-=LINE_HEIGHT;

            //horizontal solid line
            contents.endText();
            contents.setStrokingColor(Color.BLACK);
            contents.moveTo(0, line_pos-LINE_HEIGHT/2+LINE_HEIGHT);
            contents.lineTo(w, line_pos-LINE_HEIGHT/2+LINE_HEIGHT);
            contents.stroke();
            contents.beginText();

            addTextToPageStream(contents, "Closing Balance", PDType1Font.COURIER_BOLD_OBLIQUE, 16, 105, line_pos);
            double balance = debit-credit;
            if(balance>0)
                addTextToPageStream(contents, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(Math.abs(balance)), PDType1Font.HELVETICA, 14, w - 200, line_pos);
            else addTextToPageStream(contents, Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(Math.abs(balance)), PDType1Font.HELVETICA, 14, w - 100, line_pos);

            //horizontal solid line
            contents.endText();
            contents.setStrokingColor(Color.BLACK);
            contents.moveTo(0, line_pos-LINE_HEIGHT/2);
            contents.lineTo(w, line_pos-LINE_HEIGHT/2);
            contents.stroke();

            contents.beginText();
            addTextToPageStream(contents, "page " + pages, PDType1Font.HELVETICA, 18, (w/2)-50, 30);
        }

        String path = "bin/pdf/general_ledger.pdf";
        int i=1;
        while(new File(path).exists())
        {
            path = "bin/pdf/general_ledger" + "." + i + ".pdf";
            i++;
        }

        contents.close();
        document.save(path);
        document.close();

        PDFViewer pdfViewer = PDFViewer.getInstance();
        pdfViewer.setVisible(true);
        pdfViewer.doOpen(path);
    }

    public static void createJobCardPdf(Job job) throws IOException
    {
        if(SessionManager.getInstance().getActive()==null)
        {
            IO.logAndAlert(TAG, "Active session object is null.", IO.TAG_ERROR);
            return;
        }
        if(SessionManager.getInstance().getActive().isExpired())
        {
            IO.logAndAlert(TAG, "Active session has expired.", IO.TAG_ERROR);
            return;
        }
        if(job==null)
        {
            IO.log(TAG, IO.TAG_ERROR, "Job object passed is null.");
            return;
        }
        if(job.getQuote()==null)
        {
            IO.log(TAG, IO.TAG_ERROR, "Job's Quote object is null.");
            return;
        }
        if(job.getQuote().getClient()==null)
        {
            IO.log(TAG, IO.TAG_ERROR, "Job Quote's Client object is null.");
            return;
        }

        //ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
        //headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSessionId()));
        //Client client;
        //String client_json = null;//RemoteComms.sendGetRequest("/api/client/" + job.getClient_id(), headers);
        //client = new GsonBuilder().create().fromJson(client_json, Client.class);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        // Create a new document with an empty page.
        PDDocument document = new PDDocument();
        //PDPage page = new PDPage(PDRectangle.A4);
        //document.addPage(page);

        // Adobe Acrobat uses Helvetica as a default font and
        // stores that under the name '/Helv' in the resources dictionary
        PDFont font = PDType1Font.HELVETICA;
        PDResources resources = new PDResources();
        resources.put(COSName.getPDFName("Helv"), font);

        PDPageContentStream contents;// = new PDPageContentStream(document, page);

        if(job.getAssigned_employees()!=null)
        {
            PDImageXObject logo = PDImageXObject.createFromFile("images/logo.png", document);

            for (Employee employee : job.getAssigned_employees())
            {
                //contents.close();
                final PDPage new_page = new PDPage(PDRectangle.A4);
                //Add page to document
                document.addPage(new_page);
                contents = new PDPageContentStream(document, new_page);
                contents.setFont(font, 14);

                //line_pos = (int)h-logo_h-20;
                IO.log("Job PDF Exporter", IO.TAG_INFO, "added new page.");
                int logo_h = 60;
                float w = new_page.getBBox().getWidth();
                float h = new_page.getBBox().getHeight();
                int line_pos = (int) h - logo_h - 20;
                final int VERT_LINE_START = line_pos;
                //float center_horz = (w/2)-20;
                int digit_font_size = 9;

                contents.drawImage(logo, (int) (w / 2) - 80, 770, 160, logo_h);

                /**Draw lines**/
                createLinesAndBordersOnPage(contents, (int)w, line_pos, (int)h-logo_h-(ROW_COUNT+1)*LINE_HEIGHT);
                createBordersOnPage(contents, (int)w, line_pos, line_pos);

                /** begin text from the top**/
                contents.beginText();
                contents.setFont(font, 12);
                line_pos -= LINE_HEIGHT/2;
                String str_job_card = job.getQuote().getClient().getClient_name() + ": "
                                        + job.getQuote().getSitename() + " JOB CARD";
                addTextToPageStream(contents, str_job_card, PDType1Font.HELVETICA_BOLD, 14, (int) (w / 2)-100, line_pos);
                line_pos -= LINE_HEIGHT * 2;//next line

                addTextToPageStream(contents, "JOB NUMBER: " + job.getJob_number(), 12, 20, line_pos);
                addTextToPageStream(contents, "CUSTOMER: " + job.getQuote().getClient().getClient_name(), 14, (int)(w/2)+30, line_pos);
                line_pos -= LINE_HEIGHT;//next line
                addTextToPageStream(contents, "SITENAME: " + job.getQuote().getSitename(), 12, 20, line_pos);
                addTextToPageStream(contents, "STATUS: " + (job.isJob_completed()?"completed":"pending"), 12, (int)(w/2)+30, line_pos);
                line_pos -= LINE_HEIGHT;//next line
                addTextToPageStream(contents, "CONTACT: " + job.getQuote().getContact_person(), 12, 20, line_pos);
                addTextToPageStream(contents, "CELL: " + job.getQuote().getContact_person().getCell(), 12, (int)(w/2)+30, line_pos);
                addTextToPageStream(contents, "TEL: " + job.getQuote().getContact_person().getTel(), 12, (int)(w/2)+150, line_pos);
                line_pos -= LINE_HEIGHT;//next line

                //addTextToPageStream(contents, "Date Logged: " + LocalDate.parse(formatter.format(new Date(job.getDate_logged()*1000))), 12, 10, line_pos);
                //addTextToPageStream(contents, "Planned Start Date: " + LocalDate.parse(formatter.format(new Date(job.getPlanned_start_date()*1000))), 12, (int)(w/2)+30, line_pos);
                //line_pos -= LINE_HEIGHT;//next line
                //addTextToPageStream(contents, "Date Assigned: " + LocalDate.parse(formatter.format(new Date(job.getDate_assigned()*1000))), 12, 10, line_pos);
                addTextToPageStream(contents, "DATE STARTED: " + (job.getDate_started()>0?LocalDate.parse(formatter.format(new Date(job.getDate_started()*1000))):"N/A"), 12, 20, line_pos);
                addTextToPageStream(contents, "DATE COMPLETED: " + (job.isJob_completed()?LocalDate.parse(formatter.format(new Date(job.getDate_completed()*1000))):"N/A"), 12, (int)(w/2)+30, line_pos);
                line_pos -= LINE_HEIGHT;//next line
                addTextToPageStream(contents, "ASSIGNED EMPLOYEE: " + employee, 12, 20, line_pos);
                addTextToPageStream(contents, "TEL: " + employee.getTel(), 12, (int)(w/2)+30, line_pos);
                addTextToPageStream(contents, "CELL: " + employee.getCell(), 12, (int)(w/2)+150, line_pos);
                line_pos -= LINE_HEIGHT;//next line
                addTextToPageStream(contents, "REQUEST: " + job.getQuote().getRequest(), 12, 20, line_pos);
                line_pos -= LINE_HEIGHT;//next line
                contents.endText();

                //vertical lines
                contents.setStrokingColor(Color.BLACK);
                //vertical line going through center of page
                contents.moveTo((w / 2), VERT_LINE_START-LINE_HEIGHT);
                contents.lineTo((w / 2), line_pos+LINE_HEIGHT+LINE_HEIGHT/2);
                contents.stroke();
                //
                contents.moveTo((w / 2), line_pos+LINE_HEIGHT/2);
                contents.lineTo((w / 2), LINE_END);
                contents.stroke();
                //#1
                contents.moveTo(95, line_pos+LINE_HEIGHT/2);
                contents.lineTo(95, LINE_END);
                contents.stroke();
                //#2
                contents.moveTo(195, line_pos+LINE_HEIGHT/2);
                contents.lineTo(195, LINE_END);
                contents.stroke();
                //draw horizontal line
                createBordersOnPage(contents, (int)w, line_pos+LINE_HEIGHT/2, line_pos+LINE_HEIGHT/2);

                contents.beginText();
                addTextToPageStream(contents, "DATE " , PDType1Font.HELVETICA_BOLD, 12, 20, line_pos);
                addTextToPageStream(contents, "TIME IN ", PDType1Font.HELVETICA_BOLD, 12, 120, line_pos);
                addTextToPageStream(contents, "TIME OUT ", PDType1Font.HELVETICA_BOLD, 12, 220, line_pos);
                addTextToPageStream(contents, "DESCRIPTION OF WORK DONE ", PDType1Font.HELVETICA_BOLD, 12, (int)(w/2)+70, line_pos);

                line_pos = LINE_END - LINE_HEIGHT/2;//(int) h - logo_h - LINE_HEIGHT - (LINE_HEIGHT*30) - LINE_HEIGHT/2;

                addTextToPageStream(contents, "Materials Used" , 14, 100, line_pos);
                addTextToPageStream(contents, "Model/Serial" , 14, (int)(w/2)+50, line_pos);
                addTextToPageStream(contents, "Quantity" , 14, (int) w-100, line_pos);
                final int BORDER_START = line_pos;
                line_pos -= LINE_HEIGHT;//next line
                for(QuoteItem item : job.getQuote().getResources())
                {
                    addTextToPageStream(contents, item.getResource().getResource_name() , 14, 20, line_pos);
                    addTextToPageStream(contents, item.getResource().getResource_serial() , 14, (int)(w/2)+20, line_pos);
                    addTextToPageStream(contents, item.getQuantity() , 14, (int) w-80, line_pos);
                    line_pos -= LINE_HEIGHT;//next line
                }
                contents.endText();
                createBordersOnPage(contents, (int)w, BORDER_START+LINE_HEIGHT/2, BORDER_START+LINE_HEIGHT/2);
                createBordersOnPage(contents, (int)w, BORDER_START+LINE_HEIGHT/2, line_pos+LINE_HEIGHT+LINE_HEIGHT/2);

                contents.close();
            }
        }else
        {
            IO.logAndAlert(TAG, "job " + job.get_id() + " has no assigned employees.", IO.TAG_ERROR);
            return;
        }

        String path = "bin/pdf/job_card_" + job.get_id() + ".pdf";
        int i=1;
        while(new File(path).exists())
        {
            path = "bin/pdf/job_card_" + job.get_id() + "." + i + ".pdf";
            i++;
        }

        document.save(path);
        document.close();

        PDFViewer pdfViewer = PDFViewer.getInstance();
        pdfViewer.setVisible(true);
        pdfViewer.doOpen(path);
    }

    public static void addTextToPageStream(PDPageContentStream contents, String text, int font_size, int x, int y) throws IOException
    {
        PDFont font = PDType1Font.HELVETICA;
        contents.setFont(font, font_size);
        contents.setTextMatrix(new Matrix(1, 0, 0, 1, x, y-TEXT_VERT_OFFSET));
        contents.showText(text);
    }

    public static void addTextToPageStream(PDPageContentStream contents, String text, PDFont font,int font_size, int x, int y) throws IOException
    {
        contents.setFont(font, font_size);
        contents.setTextMatrix(new Matrix(1, 0, 0, 1, x, y-TEXT_VERT_OFFSET));
        contents.showText(text);
    }

    /**
     * Example PDFRenderer subclass, uses MyPageDrawer for custom rendering.
     */
    private static class BMSPDFRenderer extends PDFRenderer
    {
        BMSPDFRenderer(PDDocument document)
        {
            super(document);
        }

        @Override
        protected PageDrawer createPageDrawer(PageDrawerParameters parameters) throws IOException
        {
            return new BMSPageDrawer(parameters);
        }
    }

    /**
     * Example PageDrawer subclass with custom rendering.
     */
    private static class BMSPageDrawer extends PageDrawer
    {
        BMSPageDrawer(PageDrawerParameters parameters) throws IOException
        {
            super(parameters);
        }

        /**
         * Color replacement.
         */
        @Override
        protected Paint getPaint(PDColor color) throws IOException
        {
            // if this is the non-stroking color
            if (getGraphicsState().getNonStrokingColor() == color)
            {
                // find red, ignoring alpha channel
                if (color.toRGB() == (Color.RED.getRGB() & 0x00FFFFFF))
                {
                    // replace it with blue
                    return Color.BLUE;
                }
            }
            return super.getPaint(color);
        }

        /**
         * Glyph bounding boxes.
         */
        @Override
        protected void showGlyph(Matrix textRenderingMatrix, PDFont font, int code, String unicode,
                                 Vector displacement) throws IOException
        {
            // draw glyph
            super.showGlyph(textRenderingMatrix, font, code, unicode, displacement);

            /*// bbox in EM -> user units
            Shape bbox = new Rectangle2D.Float(0, 0, font.getWidth(code) / 1000, 1);
            AffineTransform at = textRenderingMatrix.createAffineTransform();
            bbox = at.createTransformedShape(bbox);

            // save
            Graphics2D graphics = getGraphics();
            Color color = graphics.getColor();
            Stroke stroke = graphics.getStroke();
            Shape clip = graphics.getClip();

            // draw
            graphics.setClip(graphics.getDeviceConfiguration().getBounds());
            graphics.setColor(Color.RED);
            graphics.setStroke(new BasicStroke(.5f));
            graphics.draw(bbox);

            // restore
            graphics.setStroke(stroke);
            graphics.setColor(color);
            graphics.setClip(clip);*/
        }

        /**
         * Filled path bounding boxes.
         */
        @Override
        public void fillPath(int windingRule) throws IOException
        {
            // bbox in user units
            //Shape bbox = getLinePath().getBounds2D();

            // draw path (note that getLinePath() is now reset)
            super.fillPath(windingRule);

            // save
            /*Graphics2D graphics = getGraphics();
            Color color = graphics.getColor();
            Stroke stroke = graphics.getStroke();
            Shape clip = graphics.getClip();

            // draw
            graphics.setClip(graphics.getDeviceConfiguration().getBounds());
            graphics.setColor(Color.GREEN);
            graphics.setStroke(new BasicStroke(.5f));
            graphics.draw(bbox);

            // restore
            graphics.setStroke(stroke);
            graphics.setColor(color);
            graphics.setClip(clip);*/
        }

        /**
         * Custom annotation rendering.
         */
        @Override
        public void showAnnotation(PDAnnotation annotation) throws IOException
        {
            // save
            saveGraphicsState();

            // 35% alpha
            getGraphicsState().setNonStrokeAlphaConstant(0.35);
            super.showAnnotation(annotation);

            // restore
            restoreGraphicsState();
        }
    }
}
