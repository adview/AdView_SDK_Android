//
//  VASTProcessor.java
//
//  Copyright (c) 2014 Nexage. All rights reserved.
//

package com.kuaiyou.video.vast.processor;

import android.renderscript.ScriptGroup;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilderFactory;

import com.kuaiyou.utils.AdViewUtils;
import com.kuaiyou.utils.ConstantValues;
import com.kuaiyou.utils.TLSSocketFactory;
import com.kuaiyou.utils.XmlTools;
import com.kuaiyou.video.vast.VASTPlayer;
import com.kuaiyou.video.vast.model.VASTModel;
import com.kuaiyou.video.vast.model.VAST_DOC_ELEMENTS;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class is responsible for taking a VAST 2.0 XML file, parsing it,
 * validating it, and creating a valid VASTModel object corresponding to it.
 * <p/>
 * It can handle "regular" VAST XML files as well as VAST wrapper files.
 */
public final class VASTProcessor {
    private static final String TAG = "VASTProcessor";

    // Maximum number of VAST files that can be read (wrapper file(s) + actual
    // target file)
    private static final int MAX_VAST_LEVELS = 5;
    private static final boolean IS_VALIDATION_ON = false;

    private VASTMediaPicker mediaPicker;
    private ArrayList<VASTModel> vastModel;
    private ArrayList<VASTModel> wrapperModel;
    private StringBuilder mergedVastDocs = new StringBuilder(500);

    public VASTProcessor(VASTMediaPicker mediaPicker) {
        this.mediaPicker = mediaPicker;
    }

    public ArrayList<VASTModel> getModel() {
        return vastModel;
    }
    public ArrayList<VASTModel> getWrapperModel() {
        return wrapperModel;
    }

    public int process(String xmlData) {
        AdViewUtils.logInfo("process");
        vastModel = null;
        //recursion search last doc tree contains media nodes
        int error = processUri(xmlData, 0);

        if (error != VASTPlayer.ERROR_NONE) {
            return error;
        }
        ArrayList<Document> mainDocList = new ArrayList<Document>();
        int stratPos = 0, endPos = 0;
        String tmpStr = mergedVastDocs.toString();

        Pattern pattern = Pattern.compile("<Ad[>| ]");
        Matcher matcher = pattern.matcher(tmpStr);

        //whole doc list
        while (matcher.find(endPos)) {
//            String tmp = matcher.group();
            stratPos = matcher.start();
            endPos = tmpStr.indexOf("</Ad>", stratPos);
            String temp = tmpStr.substring(stratPos, endPos) + "</Ad>";
            //AdViewUtils.logInfo(temp);
            mainDocList.add(XmlTools.stringToDocument(temp));
        }

        try {
            vastModel = new ArrayList<VASTModel>();
            wrapperModel = new ArrayList<VASTModel>();
            for (int i = 0; i < mainDocList.size(); i++) {
                AdViewUtils.logInfo("+++++++ maindoclist [" + i + "]  ++++++++");
                if (isWrapper(mainDocList.get(i))) {
                    VASTModel wrapper = new VASTModel(mainDocList.get(i), true);
                    wrapperModel.add(wrapper);
                } else {
                    VASTModel model = new VASTModel(mainDocList.get(i), false);
                    //(wilder 2019) here just add nodes which has valid nodes for MIME, see defaultpicker.java
                    if (VASTModelPostValidator.validate(model, mediaPicker)) {
                        vastModel.add(model);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mainDocList.isEmpty()) {
            AdViewUtils.logInfo("{{{{{{{{{{{{{{{{{{ mainDocList is empty }}}}}}}}}}}}}}}}}}}");
            return VASTPlayer.ERROR_XML_PARSE;
        }
        //even wrapper tree, vastModel still has media nodes, if no ,means error
        if (vastModel.isEmpty()) {
            AdViewUtils.logInfo("{{{{{{{{{{{{{{{{{{ vastModel is empty }}}}}}}}}}}}}}}}}}}");
            return VASTPlayer.ERROR_POST_VALIDATION;
        }

        return VASTPlayer.ERROR_NONE;
    }


    private int processUri(InputStream is, int depth) {
        AdViewUtils.logInfo("processUri");

        if (depth >= MAX_VAST_LEVELS) {
            String message = "VAST wrapping exceeded max limit of "
                    + MAX_VAST_LEVELS + "";
            AdViewUtils.logInfo(message);
            return VASTPlayer.ERROR_EXCEEDED_WRAPPER_LIMIT;
        }

        Document doc = createDoc(is);
        try {
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (doc == null) {
            return VASTPlayer.ERROR_XML_PARSE;
        }

//        if (IS_VALIDATION_ON) {
//            if (!validateAgainstSchema(doc)) {
//                return VASTPlayer.ERROR_SCHEMA_VALIDATION;
//            }
//        }

        merge(doc); //it will maintain mergedVastDocs, got <VAST> nodes

        // check to see if this is a VAST wrapper ad
        NodeList uriToNextDoc = doc
                .getElementsByTagName(VAST_DOC_ELEMENTS.vastAdTagURI.getValue());
        if (uriToNextDoc == null || uriToNextDoc.getLength() == 0) {
            // This isn't a wrapper ad, so we're done.
            return VASTPlayer.ERROR_NONE;
        } else {
            // This is a wrapper ad, so move on to the wrapped ad and process
            // it.
            AdViewUtils.logInfo("Doc is a wrapper. ");
            Node node = uriToNextDoc.item(0);
            String nextUri = XmlTools.getElementValue(node);
            AdViewUtils.logInfo("Wrapper URL: " + nextUri);
            InputStream nextIs = null;
            try {
                //wilder 2019 for SSL
                nextIs = (InputStream)AdViewUtils.getInputStreamURL(nextUri);
                if ( null == nextIs ) {
                    //some server fix
                    if (nextUri.startsWith("https:")) {
                        /* (wilder 2019) it seems not need in many cases
                        nextUri = "https://" + nextUri.substring(8).replace("/", "//");
                        nextIs = (InputStream) AdViewUtils.getInputStreamURL(nextUri);
                        */
                    }
                    if (null == nextIs) {
                        AdViewUtils.logInfo("<<<<< getInputStream() final error :  " + nextUri + ">>>>>>");
                        return VASTPlayer.ERROR_XML_OPEN_OR_READ;
                    }
                }
                //end
                //nextIs = nextUrl.openStream();
            } catch (Exception e) {
                e.printStackTrace();
                return VASTPlayer.ERROR_XML_OPEN_OR_READ;
            }
            int error = processUri(nextIs, depth + 1);
            return error;
        }
    }

    private boolean isWrapper(Document doc) {
        try {
            NodeList uriToNextDoc = doc.getElementsByTagName(VAST_DOC_ELEMENTS.vastAdTagURI.getValue());
            if (uriToNextDoc == null || uriToNextDoc.getLength() == 0) {
                // This isn't a wrapper ad, so we're done.
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private int processUri(String xml, int depth) {
        InputStream is = null;
        try {
            is = new ByteArrayInputStream(xml.getBytes(Charset.defaultCharset()
                    .name()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return processUri(is, depth);
    }

    private Document createDoc(InputStream xml) {
        AdViewUtils.logInfo("About to create doc from InputStream");
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml);
            doc.getDocumentElement().normalize();
            AdViewUtils.logInfo("Doc successfully created.");
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Document wrapMergedVastDocWithVasts() {
        AdViewUtils.logInfo("wrapmergedVastDocWithVasts");
        mergedVastDocs.insert(0, "<VASTS>");
        mergedVastDocs.append("</VASTS>");

        String merged = mergedVastDocs.toString();
        AdViewUtils.logInfo("Merged VAST doc:\n" + merged);

        Document doc = XmlTools.stringToDocument(merged);
        return doc;

    }

    private void merge(Document newDoc) {
        AdViewUtils.logInfo("About to merge doc into main doc.");

        NodeList nl = newDoc.getElementsByTagName("VAST");

        Node newDocElement = nl.item(0);

        String doc = XmlTools.xmlDocumentToString(newDocElement);
        mergedVastDocs.append(doc);

        AdViewUtils.logInfo("Merge successful.");
    }

    // Validator using mfXerces.....
//    private boolean validateAgainstSchema(Document doc) {
//        AdViewUtils.logInfo("About to validate doc against schema.");
//        InputStream stream = VASTProcessor.class
//                // .getResourceAsStream("assets/vast_2_0_1_schema.xsd");
//                .getResourceAsStream("assets/vast3_draft.xsd");
//        String xml = XmlTools.xmlDocumentToString(doc);
//        boolean isValid = XmlValidation.validate(stream, xml);
//        try {
//            stream.close();
//        } catch (IOException e) {
//        }
//        return isValid;
//    }

}