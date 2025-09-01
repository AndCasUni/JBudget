package it.unicam.cs.mpgc.jbudget125637.persistency;

import it.unicam.cs.mpgc.jbudget125637.model.Tags;
import org.w3c.dom.*;

import javax.xml.parsers.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class TagXmlRepository implements IOOperationRepository<Tags> {

    private final String filePath = "app/data/tags.xml";

    /**
     * Legge il file XML e restituisce una lista di Tags.
     *
     * @return Lista di Tags letti dal file XML.
     */
    @Override
    public List<Tags> read() {
        List<Tags> tags = new ArrayList<>();
        try {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = dBuilder.parse(new File(filePath));
            NodeList tagList = doc.getElementsByTagName("tag");

            for (int i = 0; i < tagList.getLength(); i++) {
                Element tagElement = (Element) tagList.item(i);
                String id = tagElement.getAttribute("id");
                String name = tagElement.getAttribute("name");
                //if (tagElement.getAttribute("isParent").equals("true")) {
                tags.add(new Tags(id, name, true));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tags;
    }

    /**
     * Legge il file XML e restituisce una lista di Tags, includendo i figli.
     *
     * @return Lista di Tags letti dal file XML, inclusi i figli.
     */
    public List<Tags> readChild() {
        List<Tags> tags = new ArrayList<>();
        try (FileInputStream in = new FileInputStream("app/data/tags.xml")) {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(in);
            doc.getDocumentElement().normalize();

            NodeList tagList = doc.getElementsByTagName("tag");

            for (int i = 0; i < tagList.getLength(); i++) {
                Element tag = (Element) tagList.item(i);

                // Aggiungo il tag principale
                String tagId = tag.getAttribute("id");
                String tagName = tag.getAttribute("name");
                tags.add(new Tags(tagId, tagName, true));

                // Aggiungo tutti i figli nell'ordine
                NodeList childList = tag.getElementsByTagName("chtag");
                for (int j = 0; j < childList.getLength(); j++) {
                    Element sub = (Element) childList.item(j);
                    String childId = sub.getAttribute("id");
                    String childName = sub.getAttribute("name");
                    tags.add(new Tags(childId, childName, false));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return tags;
    }

    //implementazioni future non necessarie per il progetto attuale
    @Override
    public void save(List<Tags> items) {
        // Implementazione per scrivere su tags.xml
    }

    @Override
    public void delete(String id) {
        // Cerca <tag id="..."> e lo rimuove
    }

    @Override
    public void delete(boolean all) {
        // Svuota completamente <tags>
    }
}
