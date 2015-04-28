package com.heshapps.kodiplayerswitcher;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Created by Mahesh Asolkar on 4/26/15.
 */
public class PlayerCoreFactoryHandler {
    public String factoryPath;
    private static final String TAG = "PlayerCoreFactoryHndlr";
    private static final String FACTORY_TEMPLATE_FILE = "template.playercorefactory.xml";
    Context context;

    public PlayerCoreFactoryHandler(Context context, String path) {
        this.factoryPath = path;
        this.context = context;
    }

    public boolean changePlayer(String selectedPlayer) {
        int toastDuration = Toast.LENGTH_LONG;

        File file = new File(factoryPath);
        Uri factoryUri = Uri.fromFile(new File(this.factoryPath));
        if (file.exists()) {
            android.util.Log.i(TAG, "Player Core Factory file exists");

            Toast toast = Toast.makeText(this.context, this.factoryPath + " exists", toastDuration);
            toast.show();
        } else {
            android.util.Log.i(TAG, this.factoryPath + " does not exist, will generate one");
            newFactoryFromTemplate();

            Toast toast = Toast.makeText(this.context, "Created new Player Core Factory", toastDuration);
            toast.show();
        }

        try {

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            Document document = documentBuilder.parse(factoryUri.toString());

            // Get the 'rules' tag near the bottom of playercorefactory.xml
            Node rules = document.getElementsByTagName("rules").item(0);

            // For all rules defined in the section, update player name
            NodeList nodes = rules.getChildNodes();

            for (int i = 0; i < nodes.getLength(); i++) {

                Node rule = nodes.item(i);

                // Player to selected player
                NamedNodeMap attribute = rule.getAttributes();
                if (attribute != null) {
                    Node nodeAttr = attribute.getNamedItem("player");

                    android.util.Log.i(TAG, "Changing player from " + nodeAttr.getTextContent() + " to " + selectedPlayer);

                    nodeAttr.setTextContent(selectedPlayer);
                }
            }

            // write the DOM object to the file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();

            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(document);

            StreamResult streamResult = new StreamResult(new File(this.factoryPath));
            transformer.transform(domSource, streamResult);

            CharSequence text = "Player is " + selectedPlayer;
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(this.context, text, duration);
            toast.show();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (SAXException sae) {
            sae.printStackTrace();
        }
        return true;
    }

    private boolean newFactoryFromTemplate() {

        AssetManager assetManager = this.context.getAssets();
        android.util.Log.i(TAG, "Got asset manager");

        InputStream in = null;
        OutputStream out = null;
        try  {
            in = assetManager.open(FACTORY_TEMPLATE_FILE);
            android.util.Log.i(TAG, "Got asset file " + FACTORY_TEMPLATE_FILE);

            String newFileName = "file:/" + this.factoryPath;
            out = new BufferedOutputStream(new FileOutputStream(newFileName));

            android.util.Log.i(TAG, "Got in and out");

            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1)
            {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (Exception e) {
            android.util.Log.e(TAG, e.getMessage());
        }finally{
            if(in!=null){
                try {
                    in.close();
                } catch (IOException e) {
                    android.util.Log.e(TAG, "Exception while closing input stream" + e.getMessage());
                }
            }
            if(out!=null){
                try {
                    out.close();
                } catch (IOException e) {
                    android.util.Log.e(TAG, "Exception while closing output stream" + e.getMessage());
                }
            }
        }
        return true;
    }
}
