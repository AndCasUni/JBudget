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
     * @param category La categoria padre (null per tutte le sottocategorie)
     * @return Lista di sottocategorie
     */
    public List<String> getSubcategoriesByCategory(String category) {
        if (category == null || category.isBlank()) {
            return getAllSubcategories();
        }
        return tagHierarchy.getOrDefault(category, Collections.emptyList());
    }

    /**
     * Restituisce tutti i tag (padri e figli)
     * @return Lista completa di tutti i tag
     */
    public List<Tags> getAllTags() {
        return tagRepository.readChild();
    }

    /**
     * Restituisce solo i tag padre (categorie principali)
     * @return Lista delle categorie principali
     */
    public List<Tags> getParentTags() {
        return tagRepository.read();
    }

    /**
     * Restituisce i tag figli di una categoria specifica
     * @param parentTag La categoria padre
     * @return Lista dei tag figli
     */
    public List<String> getChildTags(String parentTag) {
        return tagHierarchy.getOrDefault(parentTag, Collections.emptyList());
    }

    /**
     * Espande i tag selezionati includendo tutti i tag figli
     * @param selectedTags Lista di tag selezionati
     * @return Set di tag espansi
     */
    public Set<String> expandTags(List<String> selectedTags) {
        Set<String> expandedTags = new HashSet<>();
        for (String tag : selectedTags) {
            expandedTags.add(tag);
            expandedTags.addAll(getChildTags(tag));
        }
        return expandedTags;
    }

    /**
     * Restituisce la gerarchia completa dei tag
     * @return Mappa con categoria padre -> lista figli
     */
    public Map<String, List<String>> getTagHierarchy() {
        return Collections.unmodifiableMap(tagHierarchy);
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

    /**
     * Verifica se un tag esiste
     * @param tagName Nome del tag da verificare
     * @return true se il tag esiste
     */
    public boolean tagExists(String tagName) {
        List<Tags> allTags = getAllTags();
        return allTags.stream()
                .anyMatch(tag -> tag.description().equalsIgnoreCase(tagName));
    }

    /**
     * Verifica se un tag è una categoria padre
     * @param tagName Nome del tag da verificare
     * @return true se è una categoria padre
     */
    public boolean isParentTag(String tagName) {
        List<Tags> parentTags = getParentTags();
        return parentTags.stream()
                .anyMatch(tag -> tag.description().equalsIgnoreCase(tagName));
    }

    /**
     * Restituisce la categoria padre di una sottocategoria
     * @param subcategory Sottocategoria
     * @return Nome della categoria padre, o null se non trovata
     */
    public String getParentCategory(String subcategory) {
        for (Map.Entry<String, List<String>> entry : tagHierarchy.entrySet()) {
            if (entry.getValue().contains(subcategory)) {
                return entry.getKey();
            }
        }
        return null;
    }
}