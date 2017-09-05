package fadulousbms.auxilary;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import fadulousbms.managers.SessionManager;
import fadulousbms.model.FileMetadata;
import fadulousbms.model.Message;

import javax.print.PrintException;
import javax.swing.*;
import java.io.*;
import java.util.AbstractMap;
import java.util.ArrayList;

/**
 * Created by ghost on 2017/01/28.
 */
public class IO
{
    public static final String TAG_INFO = "info";
    public static final String TAG_WARN = "warning";
    public static final String TAG_ERROR = "error";
    private static final String TAG = "IO";


    public static void log(String src, String tag, String msg)
    {
        switch (tag.toLowerCase())
        {
            case TAG_INFO:
                if (Globals.DEBUG_INFO.getValue().toLowerCase().equals("on"))
                    System.out.println(String.format("%s> %s: %s", src, tag, msg));
                break;
            case TAG_WARN:
                if (Globals.DEBUG_WARNINGS.getValue().toLowerCase().equals("on"))
                    System.out.println(String.format("%s> %s: %s", src, tag, msg));
                break;
            case TAG_ERROR:
                if (Globals.DEBUG_ERRORS.getValue().toLowerCase().equals("on"))
                    System.err.println(String.format("%s> %s: %s", src, tag, msg));
                break;
            default://fallback for custom tags
                System.out.println(String.format("%s> %s: %s", src, tag, msg));
                break;
        }
    }

    public static void showMessage(String title, String msg, String type)
    {
        switch (type.toLowerCase())
        {
            case TAG_INFO:
                JOptionPane.showMessageDialog(null, msg, title, JOptionPane.INFORMATION_MESSAGE);
                break;
            case TAG_WARN:
                JOptionPane.showMessageDialog(null, msg, title, JOptionPane.WARNING_MESSAGE);
                break;
            case TAG_ERROR:
                JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
                break;
            default:
                System.err.println("IO> unknown message type '" + type + "'");
                JOptionPane.showMessageDialog(null, msg, title, JOptionPane.PLAIN_MESSAGE);
                break;
        }
    }

    public static void logAndAlert(String title, String msg, String type)
    {
        log(title, type, msg);
        showMessage(title, msg, type);
    }

    public static String readStream(InputStream stream) throws IOException
    {
        //Get message from input stream
        BufferedReader in = new BufferedReader(new InputStreamReader(stream));
        if(in!=null)
        {
            StringBuilder msg = new StringBuilder();
            String line;
            while ((line = in.readLine())!=null)
            {
                msg.append(line + "\n");
            }
            in.close();

            //try to read response as JSON object - default method of responses by server.
            Gson gson = new GsonBuilder().create();
            try
            {
                Message message = gson.fromJson(msg.toString(), Message.class);
                if (message != null)
                    if (message.getMessage() != null)
                        if (message.getMessage().length() > 0)
                            return message.getMessage();
            }catch (JsonSyntaxException e)
            {
                IO.log(TAG, IO.TAG_WARN, "message from server not in standard (JSON) Message format.");
            }
            return msg.toString();
        }else IO.logAndAlert(TAG, "could not read error stream from server response.", IO.TAG_ERROR);
        return null;
    }

    public static void viewIndexPage(String title, FileMetadata[] documents, String path)
    {
        try
        {
            PDF.createDocumentIndex(title, documents, path);
        } catch (IOException e)
        {
            IO.logAndAlert("Error","Could not successfully generate index page: " + e.getMessage(), IO.TAG_ERROR);
        }
    }

    public static void printIndexPage(String path)
    {
        IO.log(TAG, IO.TAG_INFO, "printing index page.");
        //Validate session - also done on server-side don't worry ;)
        SessionManager smgr = SessionManager.getInstance();
        if (smgr.getActive() != null)
        {
            if (!smgr.getActive().isExpired())
            {
                try
                {
                    //Prepare headers
                    ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", smgr.getActive().getSessionId()));

                    File file= new File(path);
                    if(file.exists())
                    {
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        FileInputStream fis = new FileInputStream(file);
                        byte[] buffer = new byte[512];
                        int read;
                        while ((read = fis.read(buffer)) > 0)
                        {
                            IO.log(TAG, IO.TAG_INFO, "read " + read + " bytes.");
                            outputStream.write(buffer, 0, read);
                        }
                        outputStream.flush();
                        outputStream.close();
                        fis.close();

                        IO.log(TAG, IO.TAG_INFO, "PDF total size: " + outputStream.toByteArray().length + " bytes. Ready for printing.");

                        PDF.printPDF(outputStream.toByteArray());
                        IO.log(TAG, IO.TAG_INFO, "Printing: Safety Index Page ]");
                    }else{
                        IO.logAndAlert("Error", "File 'bin/safety_index.pdf' was not found - have you generated the index page?", IO.TAG_ERROR);
                    }
                }catch (PrintException e)
                {
                    IO.logAndAlert(TAG, e.getMessage(), IO.TAG_ERROR);
                }catch (IOException e)
                {
                    IO.logAndAlert(TAG, e.getMessage(), IO.TAG_ERROR);
                }
            } else IO.logAndAlert("Session Expired", "Active session has expired.", IO.TAG_ERROR);
        } else IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
    }

    public static void printSelectedDocuments(FileMetadata[] documents)
    {
        IO.log(TAG, IO.TAG_INFO, "printing selected documents.");
        //Validate session - also done on server-side don't worry ;)
        SessionManager smgr = SessionManager.getInstance();
        if (smgr.getActive() != null)
        {
            if (!smgr.getActive().isExpired())
            {
                try
                {
                    for (FileMetadata fileMetadata : documents)
                    {
                        if (fileMetadata.isMarked())
                        {
                            //Prepare headers
                            ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                            headers.add(new AbstractMap.SimpleEntry<>("Cookie", smgr.getActive().getSessionId()));

                            long start = System.currentTimeMillis();
                            byte[] doc = RemoteComms.sendFileRequest(fileMetadata.getPdf_path(), headers);
                            long ellapsed = System.currentTimeMillis() - start;
                            IO.log(TAG, IO.TAG_INFO, "File [" + fileMetadata.getLabel() + "] download complete, size: " + doc.length + " bytes in " + ellapsed + "msec.");

                            PDF.printPDF(doc);
                            IO.log(TAG, IO.TAG_INFO, "Printing: " + fileMetadata.getLabel() + " ["+fileMetadata.get_id()+"]");
                        }
                    }
                }catch (PrintException e)
                {
                    IO.logAndAlert(TAG, e.getMessage(), IO.TAG_ERROR);
                }catch (IOException e)
                {
                    IO.logAndAlert(TAG, e.getMessage(), IO.TAG_ERROR);
                }
            } else IO.logAndAlert("Session Expired", "Active session has expired.", IO.TAG_ERROR);
        } else IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
    }

    public static void printAllDocuments(FileMetadata[] documents)
    {
        IO.log(TAG, IO.TAG_INFO, "initiating atch printing of all documents...");
        //Validate session - also done on server-side don't worry ;)
        SessionManager smgr = SessionManager.getInstance();
        if (smgr.getActive() != null)
        {
            if (!smgr.getActive().isExpired())
            {
                try
                {
                    for (FileMetadata fileMetadata : documents)
                    {
                        //Prepare headers
                        ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                        headers.add(new AbstractMap.SimpleEntry<>("Cookie", smgr.getActive().getSessionId()));

                        long start = System.currentTimeMillis();
                        byte[] doc = RemoteComms.sendFileRequest(fileMetadata.getPdf_path(), headers);
                        long ellapsed = System.currentTimeMillis() - start;
                        IO.log(TAG, IO.TAG_INFO, "File [" + fileMetadata.getLabel() + "] download complete, size: " + doc.length + " bytes in " + ellapsed + "msec.");

                        PDF.printPDF(doc);
                        IO.log(TAG, IO.TAG_INFO, "Printing: " + fileMetadata.getLabel() + " ["+fileMetadata.get_id()+"]");
                    }
                }catch (PrintException e)
                {
                    IO.logAndAlert(TAG, e.getMessage(), IO.TAG_ERROR);
                }catch (IOException e)
                {
                    IO.logAndAlert(TAG, e.getMessage(), IO.TAG_ERROR);
                }
            } else IO.logAndAlert("Session Expired", "Active session has expired.", IO.TAG_ERROR);
        } else IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
    }
}
