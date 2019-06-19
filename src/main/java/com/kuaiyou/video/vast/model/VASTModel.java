//
//  VASTModel.java
//
//  Copyright (c) 2014 Nexage. All rights reserved.
//

package com.kuaiyou.video.vast.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import com.kuaiyou.obj.ExtensionBean;
import com.kuaiyou.utils.AdViewUtils;
import com.kuaiyou.utils.XmlTools;
import com.qq.e.comm.util.StringUtil;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.text.TextUtils;

public class VASTModel implements Serializable {

    private static final long serialVersionUID = 4318368258447283733L;

    private transient Document vastsDocument;
//    private String pickedMediaFileURL = null;
//    private String pickedVideoType;
//    private int videoWidth = 0;
//    private int videoHeight = 0;

    private boolean isVaild = true;

    private ArrayList<VASTCreative> vastCreativesList = new ArrayList<VASTCreative>();
    private ArrayList<VASTCompanionAd> vastCompanionAds = new ArrayList<VASTCompanionAd>();
    //private ArrayList<VASTNonLinear> vastNonLinears = new ArrayList<VASTNonLinear>(); //wilder 2019
    private ExtensionBean extensionBean;

    // Videoclicks xpath expression
    private static final String impressionXPATH = "//Impression";

    // Error url xpath expression
    private static final String errorUrlXPATH = "//Error";

    private static final String creativeXPATH = "//Creative";

    private static final String wrapperXPATH = "//Wrapper//Creative";

    private static final String inlineXPATH = "//InLine//Creative";

    private static final String extendsionXPATH = "//Extension";

    private boolean hasSequence = true;

    public VASTModel(Document vasts, boolean isWrapper) {
        this.vastsDocument = vasts;
        if (isWrapper) {
            vastCreativesList = getWraperCreative();
            return;
        }
        vastCreativesList = getInlineCreatives(true);
        if (null == vastCreativesList || vastCreativesList.isEmpty()) {
            vastCreativesList = getInlineCreatives(false);
            hasSequence = false;
        }
        getExtensions();
    }

    public Document getVastsDocument() {
        return vastsDocument;
    }

    public VASTCreative getAppropriateCreative() {
        if (null != vastCreativesList && !vastCreativesList.isEmpty())
            return vastCreativesList.get(0);
        return null;
    }

    public ArrayList<VASTCompanionAd> getCompanionAdList() {
        if (null != vastCompanionAds)
            return vastCompanionAds;
        return null;
    }

    public void setAppropriateCreative(ArrayList<VASTCreative> creatives) {
        if (null != creatives && !creatives.isEmpty())
            vastCreativesList = new ArrayList<VASTCreative>(creatives);
    }

    public ArrayList<VASTCreative> getCreativeList() {
        return vastCreativesList;
    }


    private ArrayList<VASTCreative> getCreatives(NodeList nodes) {
        ArrayList<VASTCreative> creatives = new ArrayList<VASTCreative>();
        try {
            VASTCreative creative;
            Node node;
            NodeList childList;
            // 解析creative的所有属性
            if (null != nodes) {
                for (int i = 0; i < nodes.getLength(); i++) {
                    creative = new VASTCreative();
                    node = nodes.item(i);
                    // 解析creative的所有属性
                    parseBean(creative, node);
                    // 获取creative的所有子节点
                    childList = node.getChildNodes();
                    if (null != childList) {
                        for (int j = 0; j < childList.getLength(); j++) {
                            Node childNode = childList.item(j);
                            String nodeName = childNode.getNodeName();
                            // 查找Linear节点
                            if (nodeName.equalsIgnoreCase("Linear")) {
                                // 获取skipoffset数据, 这里支持线性的nodes
                                getLinear(childNode, creative);
                                // 获取linear 所有子节点
                                NodeList mediaNodes = childNode.getChildNodes();
                                for (int k = 0; k < mediaNodes.getLength(); k++) {
                                    Node mediaNode = mediaNodes.item(k);
                                    if (mediaNode.getNodeName()
                                            .equalsIgnoreCase("Duration")) {
                                        // 获取duration数据
                                        getDuration(mediaNode, creative);
                                    } else if (mediaNode.getNodeName()
                                            .equalsIgnoreCase("MediaFiles")) {

                                        NodeList mediaFilesNode = mediaNode
                                                .getChildNodes();

                                        // 获取MediaFiles节点数据
                                        getMediaFiles(mediaFilesNode, creative);
                                    } else if (mediaNode.getNodeName()
                                            .equalsIgnoreCase("Icons")) {
                                        NodeList iconsNodes = mediaNode
                                                .getChildNodes();
                                        // 获取Icons节点数据
                                        getIcons(iconsNodes, creative);
                                    } else if (mediaNode.getNodeName()
                                            .equalsIgnoreCase("VideoClicks")) {
                                        // NodeList videoClicksNodes = mediaNode
                                        // .getChildNodes();
                                        // 获取VideoClicks节点数据
                                        getVideoClicks(mediaNode, creative);
                                        getVideoClicks();
                                    } else if (mediaNode.getNodeName()
                                            .equalsIgnoreCase("TrackingEvents")) {
                                        NodeList trackingEventsNodes = mediaNode
                                                .getChildNodes();
                                        // 获取TrackingEvents节点数据
                                        getTrackEvents(trackingEventsNodes,
                                                creative);
                                    } else if (mediaNode.getNodeName()
                                            .equalsIgnoreCase("AdParameters")){
                                        //wilder 2019 for adParameters for VPAID
                                        getAdParameters(mediaNode,creative);
                                    }
                                }
                                creatives.add(creative);
                            } else {
                                //(wilder 2019) nonLinear creative, or CompanionAds mode
                                if (nodeName.equalsIgnoreCase("CompanionAds")) {

                                    NamedNodeMap nodeMaps = childNode.getAttributes();
                                    Node requiredNode = nodeMaps.getNamedItem("required");
                                    if (null != requiredNode) {
                                        if (requiredNode.getNodeValue().equalsIgnoreCase("all")
                                                || requiredNode.getNodeValue().equalsIgnoreCase("any")) {
                                            NodeList companionsNodes = childNode.getChildNodes();
                                            // 获取CompanionAds节点数据
                                            getCompanions(companionsNodes);
                                        }
                                    } else {
                                        //wilder 2019 for new
                                        NodeList companionsNodes = childNode.getChildNodes();
                                        // 获取CompanionAds节点数据
                                        getCompanions(companionsNodes);
                                    }

                                }else if (nodeName.equalsIgnoreCase("NonLinearAds")) {
                                    //nonlinear
                                    NodeList nonLinearNodes = childNode.getChildNodes();
                                    getNonLinears(nonLinearNodes, creative);
                                    //creatives.add(creative);
                                }
                            }

                        } //for (each child in 1 creative)
                    }
                }//for (each creative)
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return creatives;
    }

    private ArrayList<VASTCreative> getWraperCreative() {
        AdViewUtils.logInfo("getWrapper");
        XPath xpath = XPathFactory.newInstance().newXPath();
        try {
            NodeList nodes = (NodeList) xpath.evaluate(wrapperXPATH,// creativeXPATH,
                    vastsDocument, XPathConstants.NODESET);
            return getCreatives(nodes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private ArrayList<VASTCreative> getInlineCreatives(boolean needSequence) {
        AdViewUtils.logInfo("getCreatives");
        XPath xpath = XPathFactory.newInstance().newXPath();
        try {
            NodeList nodes = (NodeList) xpath.evaluate(inlineXPATH,// creativeXPATH,
                    vastsDocument, XPathConstants.NODESET);

            return getCreatives(nodes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private Object parseBean(Object object, Node node) {
        try {
            NamedNodeMap attributes = node.getAttributes();
            if (null == attributes)
                return object;
            Node attributeNode;
            String value = null;
            Field[] fields = object.getClass().getDeclaredFields();
            for (Field field : fields) {
                attributeNode = attributes.getNamedItem(field.getName());
                if (null != attributeNode
                        && !TextUtils.isEmpty(attributeNode.getNodeValue())) {
                    value = attributeNode.getNodeValue();
                    setValue(object, field.getName(),
                            getValueByType(object, field.getName(), value));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return object;
    }

    private Object setValue(Object obj, String key, Object value) {
        Method method;
        try {
            if (null == value || null == obj || null == key)
                return obj;

            method = obj.getClass().getMethod(
                    "set" + key.substring(0, 1).toUpperCase() + key.substring(1),
                    new Class[]{value.getClass()});
            method.invoke(obj, value);
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return obj;
    }

    private Object getValueByType(Object obj, String key, String value) {
        try {
            Field field = obj.getClass().getDeclaredField(key);
            Class typeClass = field.getType();
            if (typeClass == String.class) {
                return value;
            } else if (typeClass == BigInteger.class) {
                return new BigInteger(value);
            } else if (typeClass == Boolean.class) {
                return Boolean.valueOf(value);
            } else if (typeClass == Integer.class) {
                return Integer.valueOf(value);
            } else
                return null;
        } catch (NoSuchFieldException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    private void getLinear(Node linearNode, VASTCreative creative) {
        if (null == linearNode)
            return;
        NamedNodeMap attrNodes = linearNode.getAttributes();
        if (null != attrNodes) {
            Node skipNode = attrNodes.getNamedItem("skipoffset");
            if (null != skipNode) {
                String timeStr = skipNode.getNodeValue();
                if (!TextUtils.isEmpty(timeStr)) {
                    SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
                    Date date = null;
                    try {
                        date = format.parse(timeStr);
                        creative.setSkipoffset((date.getSeconds()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    private void getDuration(Node durationNode, VASTCreative creative) {
        if (null != durationNode) {
            String timeStr = XmlTools.getElementValue(durationNode);
            if (!TextUtils.isEmpty(timeStr)) {
                SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
                Date date = null;
                try {
                    date = format.parse(timeStr);
                    creative.setDuration((date.getSeconds()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

        }

    }

    //wilder 2019 for VPAID getparameters
    private void getAdParameters(Node adParamNode, VASTCreative creative){
        AdParameters param = new AdParameters();
        String par = XmlTools.getElementValue(adParamNode);
        if (!TextUtils.isEmpty(par)) {
            //pre process param string, such as "//" start must be fixed
            par = par.replaceAll("\n|\r", "");
            if(!par.contains("http://") && (!par.contains(("https://"))) ) {
                par = par.replaceAll("//", "http://");
            }

            param.setText(par);
            creative.setAdParameters(param);
        }
    }
    //end wilder
    private void getMediaFiles(NodeList nodes, VASTCreative creative) {
        ArrayList<VASTMediaFile> mediaFiles = new ArrayList<VASTMediaFile>();
        try {
            Node node;
            VASTMediaFile mediaFile;
            String mediaURL;
            Node attributeNode;
            if (nodes != null) {
                for (int i = 0; i < nodes.getLength(); i++) {
                    mediaFile = new VASTMediaFile();
                    node = nodes.item(i);
                    if (null == node)
                        continue;
                    NamedNodeMap attributes = node.getAttributes();
                    if (null == attributes)
                        continue;
                    Field[] fields = mediaFile.getClass().getDeclaredFields();
                    for (Field field : fields) {
                        attributeNode = attributes
                                .getNamedItem(field.getName());
                        if (null != attributeNode
                                && !TextUtils.isEmpty(attributeNode
                                .getNodeValue())) {
                            String value = attributeNode.getNodeValue();
                            setValue(
                                    mediaFile,
                                    field.getName(),
                                    getValueByType(mediaFile, field.getName(),
                                            value));
                        }
                    }
                    mediaURL = XmlTools.getElementValue(node);
                    mediaFile.setValue(mediaURL);
                    /* wilder 2019 for VPAID
                    String apiFrame = mediaFile.getApiFramework();
                    if ( !TextUtils.isEmpty(apiFrame) && apiFrame.equalsIgnoreCase("VPAID") ) {
                        String type = mediaFile.getType();
                    } */
                    mediaFiles.add(mediaFile);

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        creative.setMediaFiles(mediaFiles);
    }

    private void getTrackEvents(NodeList nodes, VASTCreative creative) {
        AdViewUtils.logInfo("getTrackingUrls");

        List<String> tracking;
        HashMap<TRACKING_EVENTS_TYPE, List<String>> trackings = new HashMap<TRACKING_EVENTS_TYPE, List<String>>();
        try {
            Node node;
            String trackingURL;
            String eventName;
            TRACKING_EVENTS_TYPE key = null;

            if (nodes != null) {
                for (int i = 0; i < nodes.getLength(); i++) {
                    node = nodes.item(i);
                    NamedNodeMap attributes = node.getAttributes();
                    if (null == attributes)
                        continue;
                    eventName = (attributes.getNamedItem("event"))
                            .getNodeValue();
                    try {
                        key = TRACKING_EVENTS_TYPE.valueOf(eventName);
                    } catch (IllegalArgumentException e) {
                        AdViewUtils.logInfo("Event:" + eventName
                                + " is not valid. Skipping it.");
                        continue;
                    }

                    trackingURL = XmlTools.getElementValue(node);

                    if (trackings.containsKey(key)) {
                        tracking = trackings.get(key);
                        tracking.add(trackingURL);
                    } else {
                        tracking = new ArrayList<String>();
                        tracking.add(trackingURL);
                        trackings.put(key, tracking);

                    }

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        creative.setTrackings(trackings);
    }

    private void getVideoClicks(Node node, VASTCreative creative) {
        AdViewUtils.logInfo("getVideoClicks");

        VideoClicks videoClicks = new VideoClicks();

        try {
            NodeList childNodes = node.getChildNodes();

            Node child;
            String value = null;

            for (int childIndex = 0; childIndex < childNodes.getLength(); childIndex++) {

                child = childNodes.item(childIndex);
                String nodeName = child.getNodeName();

                if (nodeName.equalsIgnoreCase("ClickTracking")) {
                    value = XmlTools.getElementValue(child);
                    videoClicks.getClickTracking().add(value);

                } else if (nodeName.equalsIgnoreCase("ClickThrough")) {
                    value = XmlTools.getElementValue(child);
                    videoClicks.setClickThrough(value);

                } else if (nodeName.equalsIgnoreCase("CustomClick")) {
                    value = XmlTools.getElementValue(child);
                    videoClicks.getCustomClick().add(value);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        creative.setVideoClicks(videoClicks);
    }

    private void getIcons(NodeList nodes, VASTCreative creative) {
        AdViewUtils.logInfo("getIconClicks");

        VASTIcon vastIcon = new VASTIcon();
        ArrayList<VASTIcon> iconClickList = new ArrayList<VASTIcon>();
        try {
            Node node;

            if (nodes != null) {
                for (int i = 0; i < nodes.getLength(); i++) {
                    IconClicks iconClicks = new IconClicks();
                    node = nodes.item(i);
                    NodeList childNodes = node.getChildNodes();
                    NamedNodeMap attributes = node.getAttributes();
                    Node child, attributeNode;
                    String value = null;
                    if (null == attributes)
                        continue;
                    Field[] fields = vastIcon.getClass().getDeclaredFields();
                    for (Field field : fields) {
                        attributeNode = attributes
                                .getNamedItem(field.getName());
                        if (null != attributeNode
                                && !TextUtils.isEmpty(attributeNode
                                .getNodeValue())) {
                            value = attributeNode.getNodeValue();
                            setValue(
                                    vastIcon,
                                    field.getName(),
                                    getValueByType(vastIcon, field.getName(),
                                            value));
                        }
                    }

                    for (int childIndex = 0; childIndex < childNodes.getLength(); childIndex++) {

                        child = childNodes.item(childIndex);
                        String nodeName = child.getNodeName();

                        if (nodeName.equalsIgnoreCase("IconClicks")) {
                            NodeList clickChild = child.getChildNodes();
                            for (int x = 0; x < clickChild.getLength(); x++) {
                                Node childChildNode = clickChild.item(x);
                                String childChildNodeName = childChildNode.getNodeName();
                                if (childChildNodeName.equalsIgnoreCase("IconClickThrough")) {
                                    value = XmlTools.getElementValue(childChildNode);
                                    iconClicks.setClickThrough(value);
                                } else if (childChildNodeName
                                        .equalsIgnoreCase("IconClickTracking")) {
                                    value = XmlTools.getElementValue(childChildNode);
                                    iconClicks.getClickTracking().add(value);
                                }
                            }
                        } else if (nodeName.equalsIgnoreCase("StaticResource")) {
                            Node typeNode = child.getAttributes().getNamedItem(
                                    "type");
                            if (null != typeNode)
                                vastIcon.setValueType(typeNode.getNodeValue());
                            value = XmlTools.getElementValue(child);
                            vastIcon.setStaticValue(value);
                        } else if (nodeName.equalsIgnoreCase("HTMLResource")) {
                            value = XmlTools.getElementValue(child);
                            vastIcon.setHtmlValue(value);
                        } else if (nodeName.equalsIgnoreCase("IFrameResource")) {
                            value = XmlTools.getElementValue(child);
                            vastIcon.setiFrameValue(value);
                        }
                    }
                    vastIcon.setIconClicks(iconClicks);
                    iconClickList.add(vastIcon);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        creative.setVastIcons(iconClickList);
    }


    private ExtensionBean getExtensions() {
        //(wilder 2019)always means final page in vast
        AdViewUtils.logInfo("getExtensions");
        XPath xpath = XPathFactory.newInstance().newXPath();
        try {
            NodeList nodes = (NodeList) xpath.evaluate(extendsionXPATH,
                    vastsDocument, XPathConstants.NODESET);
            Node node;
            if (nodes != null) {
                if (nodes.getLength() == 0)
                    return null;
                extensionBean = new ExtensionBean();
                boolean isValidExtension = false;
                for (int i = 0; i < nodes.getLength(); i++) {
                    node = nodes.item(i);

                    NodeList childNodes = node.getChildNodes();

                    Node child;
                    String value = null;

                    for (int childIndex = 0; childIndex < childNodes
                            .getLength(); childIndex++) {

                        child = childNodes.item(childIndex);
                        String nodeName = child.getNodeName();

                        if (nodeName.equalsIgnoreCase("Ky_EndHtml")) {
                            value = XmlTools.getElementValue(child);
                            extensionBean.setEndPageHtml(value);
                            isValidExtension = true;
                        } else if (nodeName.equalsIgnoreCase("Ky_EndImage")) {
                            value = XmlTools.getElementValue(child);
                            extensionBean.setEndPageImage(value);
                            isValidExtension = true;
                        } else if (nodeName.equalsIgnoreCase("Ky_EndIconUrl")) {
                            value = XmlTools.getElementValue(child);
                            extensionBean.setEndPageIconUrl(value);
                            isValidExtension = true;
                        } else if (nodeName.equalsIgnoreCase("Ky_EndDesc")) {
                            value = XmlTools.getElementValue(child);
                            extensionBean.setEndPageDesc(value);
                            isValidExtension = true;
                        } else if (nodeName.equalsIgnoreCase("Ky_EndTitle")) {
                            value = XmlTools.getElementValue(child);
                            extensionBean.setEndPageTitle(value);
                        } else if (nodeName.equalsIgnoreCase("Ky_EndText")) {
                            value = XmlTools.getElementValue(child);
                            extensionBean.setEndPageText(value);
                            isValidExtension = true;
                        } else if (nodeName.equalsIgnoreCase("Ky_EndLink")) {
                            value = XmlTools.getElementValue(child);
                            extensionBean.setEndPageLink(value);
                            isValidExtension = true;
                        }
                    }
                }
                //wilder 2019 for extensions from none wrapper cases
                if (!isValidExtension) {
                    extensionBean = null;
                }else {
                    AdViewUtils.logInfo("++++ getExtensions: got extensions !!!!");
                }
            }

        } catch (Exception e) {
            extensionBean = null;
            e.printStackTrace();
            return null;
        }
        return extensionBean;
    }

    private void getCompanions(NodeList nodes) {
        AdViewUtils.logInfo("getCompanionClicks");
        CompanionClicks companionClicks = new CompanionClicks();
        try {
            Node node;
            if (nodes != null) {
                for (int i = 0; i < nodes.getLength(); i++) {
                    node = nodes.item(i);
                    VASTCompanionAd companionAds = new VASTCompanionAd();
                    Node child;
                    String value;
                    NodeList childNodes = node.getChildNodes();
                    parseBean(companionAds, node);
                    for (int childIndex = 0; childIndex < childNodes.getLength(); childIndex++) {

                        child = childNodes.item(childIndex);
                        String nodeName = child.getNodeName();

                        if (nodeName.equalsIgnoreCase("CompanionClickThrough")) {
                            value = XmlTools.getElementValue(child);
                            companionClicks.setClickThrough(value);
                        } else if (nodeName.equalsIgnoreCase("CompanionClickTracking")) {
                            value = XmlTools.getElementValue(child);
                            companionClicks.getClickTracking().add(value);
                        } else if (nodeName.equalsIgnoreCase("StaticResource")) {
                            Node typeNode = child.getAttributes().getNamedItem("type");
                            if (null != typeNode) {
                                companionAds.setValueType(typeNode.getNodeValue());
                            }else {
                                //wilder 2019 , here should add "creativeType", if no type, ths node will be
                                //deleted by video picker, see prefilterCompanions() in DefaultMediaPicker.java
                                typeNode = child.getAttributes().getNamedItem("creativeType");
                                if (null != typeNode) {
                                    companionAds.setValueType(typeNode.getNodeValue());
                                }

                            }
                            value = XmlTools.getElementValue(child);
                            companionAds.setStaticValue(value);

                        } else if (nodeName.equalsIgnoreCase("HTMLResource")) {
                            value = XmlTools.getElementValue(child);
                            companionAds.setHtmlValue(value);
                        } else if (nodeName.equalsIgnoreCase("IFrameResource")) {
                            value = XmlTools.getElementValue(child);
                            companionAds.setiFrameValue(value);
                        } else if (nodeName.equalsIgnoreCase("TrackingEvents")) {
                            NodeList childChildNodeLis = child.getChildNodes();
                            for (int x = 0; x < childChildNodeLis.getLength(); x++) {
                                Node childChildNode = childChildNodeLis.item(x);
                                String childChildNodeName = childChildNode.getNodeName();
                                if (childChildNodeName.equalsIgnoreCase("Tracking")) {
                                    NamedNodeMap childChildNodeAttrMaps = childChildNode.getAttributes();
                                    if (null != childChildNodeAttrMaps && childChildNodeAttrMaps.getLength() > 0) {
                                        Node childChildNodeAttr = childChildNodeAttrMaps.getNamedItem("creativeView");
                                        if (null != childChildNodeAttr) {
                                            value = XmlTools.getElementValue(childChildNode);
                                            companionClicks.getTrackingEvent().add(value);
                                        }
                                    }
                                }
                            }
                        }
                        companionAds.setCompanionClicks(companionClicks);
                    }
                    if (childNodes.getLength() > 0) {
                        vastCompanionAds.add(companionAds);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //wilder 2019 for nonlinear
    private void getNonLinears(NodeList nodes, VASTCreative creative) {
        AdViewUtils.logInfo("========= getNonLinears() ==============");
        //here we use companion clicks , cause they are same
        CompanionClicks nonLinearClicks = new CompanionClicks();
        ArrayList<VASTNonLinear> vastNonLinears = new ArrayList<VASTNonLinear>(); //wilder 2019

        try {
            Node node;
            if (nodes != null) {
                for (int i = 0; i < nodes.getLength(); i++) {
                    node = nodes.item(i);
                    VASTNonLinear nonLinearAd = new VASTNonLinear();
                    Node child;
                    String value;
                    NodeList childNodes = node.getChildNodes();
                    //parse fill attributes of NonLinear node
                    parseBean(nonLinearAd, node);
                    //child nodes in NonLinear node
                    for (int childIndex = 0; childIndex < childNodes.getLength(); childIndex++) {

                        child = childNodes.item(childIndex);
                        String nodeName = child.getNodeName();

                        if (nodeName.equalsIgnoreCase("NonLinearClickThrough")) {
                            value = XmlTools.getElementValue(child);
                            nonLinearClicks.setClickThrough(value);
                        }
                        else if (nodeName.equalsIgnoreCase("NonLinearClickTracking")) {
                            value = XmlTools.getElementValue(child);
                            nonLinearClicks.getClickTracking().add(value);
                        }
                        else if (nodeName.equalsIgnoreCase("AdParameters")) {
                            //value = XmlTools.getElementValue(child);
                            getAdParameters(child, creative);
                        }
                        else if (nodeName.equalsIgnoreCase("StaticResource")) {
                            Node typeNode = child.getAttributes().getNamedItem("type");
                            if (null != typeNode) {
                                nonLinearAd.setValueType(typeNode.getNodeValue());
                            }else {
                                //wilder 2019 , here should add "creativeType", if no type, ths node will be
                                //deleted by video picker, see prefilterCompanions() in DefaultMediaPicker.java
                                typeNode = child.getAttributes().getNamedItem("creativeType");
                                if (null != typeNode) {
                                    nonLinearAd.setValueType(typeNode.getNodeValue());
                                }
                            }
                            value = XmlTools.getElementValue(child);
                            nonLinearAd.setStaticValue(value);

                        } else if (nodeName.equalsIgnoreCase("HTMLResource")) {
                            value = XmlTools.getElementValue(child);
                            nonLinearAd.setHtmlValue(value);
                        } else if (nodeName.equalsIgnoreCase("IFrameResource")) {
                            value = XmlTools.getElementValue(child);
                            nonLinearAd.setiFrameValue(value);
                        } else if (nodeName.equalsIgnoreCase("TrackingEvents")) {
                            NodeList childChildNodeLis = child.getChildNodes();
                            for (int x = 0; x < childChildNodeLis.getLength(); x++) {
                                Node childChildNode = childChildNodeLis.item(x);
                                String childChildNodeName = childChildNode.getNodeName();
                                if (childChildNodeName.equalsIgnoreCase("Tracking")) {
                                    NamedNodeMap childChildNodeAttrMaps = childChildNode.getAttributes();
                                    if (null != childChildNodeAttrMaps && childChildNodeAttrMaps.getLength() > 0) {
                                        Node childChildNodeAttr = childChildNodeAttrMaps.getNamedItem("creativeView");
                                        if (null != childChildNodeAttr) {
                                            value = XmlTools.getElementValue(childChildNode);
                                            nonLinearClicks.getTrackingEvent().add(value);
                                        }
                                    }
                                }
                            }
                        }
                        nonLinearAd.setNonLinearClicks(nonLinearClicks);
                    }
                    if (childNodes.getLength() > 0) {
                        vastNonLinears.add(nonLinearAd);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        //(wilder 2019) nonlinear will be add to creative or not ?
        creative.setVastNonlinears(vastNonLinears);
    }
    //end wilder

    public VideoClicks getVideoClicks() {
        AdViewUtils.logInfo("getVideoClicks");

        VideoClicks videoClicks = new VideoClicks();

        XPath xpath = XPathFactory.newInstance().newXPath();

        try {
            NodeList nodes = (NodeList) xpath.evaluate("//VideoClicks",
                    vastsDocument, XPathConstants.NODESET);
            Node node;

            if (nodes != null) {
                for (int i = 0; i < nodes.getLength(); i++) {
                    node = nodes.item(i);

                    NodeList childNodes = node.getChildNodes();

                    Node child;
                    String value = null;

                    for (int childIndex = 0; childIndex < childNodes
                            .getLength(); childIndex++) {

                        child = childNodes.item(childIndex);
                        String nodeName = child.getNodeName();

                        if (nodeName.equalsIgnoreCase("ClickTracking")) {
                            value = XmlTools.getElementValue(child);
                            videoClicks.getClickTracking().add(value);

                        } else if (nodeName.equalsIgnoreCase("ClickThrough")) {
                            value = XmlTools.getElementValue(child);
                            videoClicks.setClickThrough(value);

                        } else if (nodeName.equalsIgnoreCase("CustomClick")) {
                            value = XmlTools.getElementValue(child);
                            videoClicks.getCustomClick().add(value);
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return videoClicks;
    }

    public List<String> getImpressions() {
        AdViewUtils.logInfo("getImpressions");

        List<String> list = getListFromXPath(impressionXPATH);

        return list;

    }

    public List<String> getErrorUrl() {

        AdViewUtils.logInfo("getErrorUrl");

        List<String> list = getListFromXPath(errorUrlXPATH);

        return list;

    }

    private List<String> getListFromXPath(String xPath) {

        AdViewUtils.logInfo("getListFromXPath");

        ArrayList<String> list = new ArrayList<String>();

        XPath xpath = XPathFactory.newInstance().newXPath();

        try {
            NodeList nodes = (NodeList) xpath.evaluate(xPath, vastsDocument,
                    XPathConstants.NODESET);
            Node node;

            if (nodes != null) {
                for (int i = 0; i < nodes.getLength(); i++) {
                    node = nodes.item(i);
                    list.add(XmlTools.getElementValue(node));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return list;

    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        AdViewUtils.logInfo("writeObject: about to write");
        oos.defaultWriteObject();

        String data = XmlTools.xmlDocumentToString(vastsDocument);
        // oos.writeChars();
        oos.writeObject(data);
        AdViewUtils.logInfo("done writing");

    }

    private void readObject(ObjectInputStream ois)
            throws ClassNotFoundException, IOException {
        AdViewUtils.logInfo("readObject: about to read");
        ois.defaultReadObject();

        String vastString = (String) ois.readObject();
        AdViewUtils.logInfo("vastString data is:\n" + vastString + "\n");

        vastsDocument = XmlTools.stringToDocument(vastString);

        AdViewUtils.logInfo("done reading");
    }

//    public String getPickedMediaFileURL() {
//        return pickedMediaFileURL;
//    }
//
//    public void setPickedMediaFileURL(String pickedMediaFileURL) {
//        this.pickedMediaFileURL = pickedMediaFileURL;
//    }
//
//    public  void setPickedMediaFileType(String type){
//        this.pickedVideoType=type;
//    }
//    public String getPickedMeidaFileType(){
//        return pickedVideoType;
//    }
//
//    public int getVideoWidth() {
//        return videoWidth;
//    }
//
//    public void setVideoWidth(int videoWidth) {
//        this.videoWidth = videoWidth;
//    }
//
//    public int getVideoHeight() {
//        return videoHeight;
//    }
//
//    public void setVideoHeight(int videoHeight) {
//        this.videoHeight = videoHeight;
//    }


    public ExtensionBean getExtensionBean() {
        return extensionBean;
    }

    public boolean isVaild() {
        return isVaild;
    }

    public void setVaild(boolean isVaild) {
        this.isVaild = isVaild;
    }

}
