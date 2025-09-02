package it.unicam.cs.mpgc.jbudget125637.service;

import it.unicam.cs.mpgc.jbudget125637.model.Tags;
import it.unicam.cs.mpgc.jbudget125637.persistency.TagXmlRepository;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;
import java.util.*;

public class TagService {
    private final TagXmlRepository tagRepository;
    private final Map<String, List<String>> tagHierarchy;

    public TagService() {
        this.tagRepository = new TagXmlRepository();
        this.tagHierarchy = loadTagHierarchy();
    }

    /**
     * Restituisce tutte le sottocategorie di una categoria specifica
     * @param category Nome della categoria
     * @return Lista di sottocategorie, o tutte se category è null o vuota
     */
    public List<String> getSubcategoriesByCategory(String category) {
        if (category == null || category.isBlank()) {
            return getAllSubcategories();
        }
        return tagHierarchy.getOrDefault(category, Collections.emptyList());
    }

    /**
     * Restituisce tutti i tag (padri e figli)
     * @return Lista di tutti i tag
     */
    public List<Tags> getAllTags() {
        return tagRepository.readChild();
    }

    /**
     * Restituisce i tag figli di una macrocategoria
     * @param parentTag Nome della macrocategoria
     * @return Lista di tag figli
     */
    public List<String> getChildTags(String parentTag) {
        return tagHierarchy.getOrDefault(parentTag, Collections.emptyList());
    }



    /**
     * Restituisce tutte le sottocategorie senza filtro
     * @return Lista di tutte le sottocategorie
     */
    private List<String> getAllSubcategories() {
        List<String> allSubcategories = new ArrayList<>();
        for (List<String> subcategories : tagHierarchy.values()) {
            allSubcategories.addAll(subcategories);
        }
        return allSubcategories;
    }

    /**
     * Carica la gerarchia dei tag dal file XML
     * @return Mappa della gerarchia dei tag
     */
    private Map<String, List<String>> loadTagHierarchy() {
        Map<String, List<String>> hierarchy = new HashMap<>();
        try {
            File xmlFile = new File("app/data/tags.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            NodeList tagNodes = doc.getElementsByTagName("tag");
            for (int i = 0; i < tagNodes.getLength(); i++) {
                Element tagElement = (Element) tagNodes.item(i);
                String parentName = tagElement.getAttribute("name");
                List<String> children = new ArrayList<>();

                NodeList childNodes = tagElement.getElementsByTagName("chtag");
                for (int j = 0; j < childNodes.getLength(); j++) {
                    Element childElement = (Element) childNodes.item(j);
                    children.add(childElement.getAttribute("name"));
                }

                hierarchy.put(parentName, children);
            }
        } catch (Exception e) {
            throw new RuntimeException("Errore nel caricamento della gerarchia dei tag", e);
        }
        return hierarchy;
    }

}