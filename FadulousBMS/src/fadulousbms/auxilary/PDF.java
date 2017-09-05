package fadulousbms.auxilary;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fadulousbms.managers.ClientManager;
import fadulousbms.managers.EmployeeManager;
import fadulousbms.managers.QuoteManager;
import fadulousbms.managers.SessionManager;
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
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;

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

    public static void viewPDF(String path) throws IOException
    {
        int w = 640, h = 480;
        File file = new File(path);

        PDDocument doc = PDDocument.load(file);
        PDFRenderer renderer = new BMSPDFRenderer(doc);
        BufferedImage image = renderer.renderImageWithDPI(0, 320);
        Canvas image_container = new Canvas(image, w, h);
        image_container.repaint();
        JFrame viewer = new JFrame();
        viewer.setSize(new Dimension(w,h));
        //viewer.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        viewer.setTitle("PDF Viewer");
        viewer.setLocationRelativeTo(null);
        viewer.add(image_container);
        viewer.setVisible(true);

        viewer.addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentResized(ComponentEvent e)
            {
                super.componentResized(e);
                image_container.setWidth(viewer.getWidth());
                image_container.setHeight(viewer.getHeight());
            }
        });
    }

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

        PDFViewer pdfViewer = new PDFViewer(true);
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
        Employee contact = quote.getContactPerson();
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

        int col_divider_start = line_pos;

        //vertical line going through center of page
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo((w/2), center_vert_line_start);
        contents.lineTo((w/2),(col_divider_start+LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
        contents.stroke();
        //
        contents.moveTo((w/2), (col_divider_start+(int)Math.ceil(LINE_HEIGHT/2)));
        contents.lineTo((w/2),(col_divider_start-LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
        contents.stroke();

        contents.beginText();
        addTextToPageStream(contents,"Site Location: " + quote.getSitename(),PDType1Font.HELVETICA, 13,20, line_pos);
        addTextToPageStream(contents,"Total Incl. VAT: "+String.valueOf(DecimalFormat.getCurrencyInstance().format(quote.getTotal()+quote.getTotal()*(Quote.VAT/100))), PDType1Font.COURIER_BOLD_OBLIQUE, 14, (int)((w/2)+15), line_pos);
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
                contents.moveTo(80, (col_divider_start-LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
                contents.lineTo(80, line_pos+LINE_HEIGHT/2);
                contents.stroke();
                //vertical line going through center of page
                contents.setStrokingColor(Color.BLACK);
                contents.moveTo((w/2), (col_divider_start-LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
                contents.lineTo((w/2),line_pos+LINE_HEIGHT/2);
                contents.stroke();
                //#3+
                for(int i=1;i<5;i++)//7 cols in total
                {
                    contents.moveTo((w/2)+55*i, (col_divider_start-LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
                    contents.lineTo((w/2)+55*i,line_pos+LINE_HEIGHT/2);
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
                addTextToPageStream(contents, String.valueOf(DecimalFormat.getCurrencyInstance().format(item.getRateValue())), digit_font_size,col_pos+5, line_pos);
                col_pos+=55;//next column
                //Labour col
                addTextToPageStream(contents, String.valueOf(DecimalFormat.getCurrencyInstance().format(item.getLabourCost())), digit_font_size,col_pos+5, line_pos);
                col_pos+=55;//next column
                //Total col
                double total = item.getQuantityValue()*item.getRateValue()+item.getLabourCost();
                sub_total+=total;
                addTextToPageStream(contents, String.valueOf(DecimalFormat.getCurrencyInstance().format(total)), digit_font_size,col_pos+5, line_pos);

                line_pos -= LINE_HEIGHT;//next line
            }
            IO.log(TAG, IO.TAG_INFO, "successfully created quote PDF.");
        }else IO.log(TAG, IO.TAG_INFO, "quote has no resources.");
        col_pos = 0;
        line_pos -= LINE_HEIGHT;//skip another line
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
        addTextToPageStream(contents, "Sub-Total Excl. VAT: ", PDType1Font.COURIER_BOLD_OBLIQUE, 14,col_pos+30, line_pos);
        addTextToPageStream(contents, String.valueOf(DecimalFormat.getCurrencyInstance().format(sub_total)), PDType1Font.COURIER_BOLD_OBLIQUE, 14,(int)(5+(w/2)), line_pos);
        line_pos -= LINE_HEIGHT;//next line

        double vat = sub_total*(Quote.VAT/100);
        addTextToPageStream(contents, "VAT: ", PDType1Font.COURIER_BOLD_OBLIQUE, 14,col_pos+30, line_pos);
        addTextToPageStream(contents, String.valueOf(DecimalFormat.getCurrencyInstance().format(vat)), PDType1Font.COURIER_BOLD_OBLIQUE, 14,(int)(5+(w/2)), line_pos);
        contents.endText();

        int col_divider_end = line_pos;
        line_pos -= LINE_HEIGHT*2;//next 2nd line
        //solid horizontal lines after quote_items
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo(10, col_divider_end+LINE_HEIGHT+LINE_HEIGHT/2);
        contents.lineTo(w-10, col_divider_end+LINE_HEIGHT+LINE_HEIGHT/2);
        contents.stroke();
        contents.moveTo(10, col_divider_end+LINE_HEIGHT/2);
        contents.lineTo(w-10, col_divider_end+LINE_HEIGHT/2);
        contents.stroke();
        contents.moveTo(10, col_divider_end-LINE_HEIGHT+LINE_HEIGHT/2);
        contents.lineTo(w-10, col_divider_end-LINE_HEIGHT+LINE_HEIGHT/2);
        contents.stroke();

        //quote content column dividers
        //#1
        contents.moveTo(80, (col_divider_start-LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
        contents.lineTo(80, col_divider_end+LINE_HEIGHT+LINE_HEIGHT/2);
        contents.stroke();
        //vertical line going through center of page again
        contents.setStrokingColor(Color.BLACK);
        contents.moveTo((w/2), (col_divider_start-LINE_HEIGHT+(int)Math.ceil(LINE_HEIGHT/2)));
        contents.lineTo((w/2),col_divider_end-LINE_HEIGHT/2);
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

        document.save("bin/quote_"+quote.get_id()+".pdf");
        document.close();

        PDFViewer pdfViewer = new PDFViewer(true);
        pdfViewer.setVisible(true);
        pdfViewer.doOpen("bin/quote_" + quote.get_id() + ".pdf");
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
                addTextToPageStream(contents, "CONTACT: " + job.getQuote().getContactPerson(), 12, 20, line_pos);
                addTextToPageStream(contents, "CELL: " + job.getQuote().getContactPerson().getCell(), 12, (int)(w/2)+30, line_pos);
                addTextToPageStream(contents, "TEL: " + job.getQuote().getContactPerson().getTel(), 12, (int)(w/2)+150, line_pos);
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

        document.save("bin/job_card_"+job.get_id()+".pdf");
        document.close();

        PDFViewer pdfViewer = new PDFViewer(true);
        pdfViewer.setVisible(true);
        pdfViewer.doOpen("bin/job_card_" + job.get_id() + ".pdf");
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
