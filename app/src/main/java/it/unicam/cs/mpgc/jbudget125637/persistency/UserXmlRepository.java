package it.unicam.cs.mpgc.jbudget125637.persistency;

import it.unicam.cs.mpgc.jbudget125637.model.Author;
import org.w3c.dom.*;

import javax.xml.parsers.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UserXmlRepository implements IOOperationRepository<Author> {

    private final String filePath = "app/data/users.xml";

    /**
     * Legge gli utenti dal file XML e li restituisce come lista di oggetti Author.
     *
     * @return Lista di oggetti Author letti dal file XML.
     */
    @Override
    public List<Author> read() {
        List<Author> authors = new ArrayList<>();
        try {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = dBuilder.parse(new File(filePath));
            NodeList userList = doc.getElementsByTagName("user");

            for (int i = 0; i < userList.getLength(); i++) {
                Element el = (Element) userList.item(i);
                String id = el.getAttribute("id");
                String name = el.getAttribute("name");
                authors.add(new Author(id, name));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return authors;
    }

    /**
     * metodi per implementazioni future
     */
    @Override
    public void save(List<Author> items) {
        // sviluppi futuri: Implementa la scrittura su users.xml
    }

    @Override
    public void delete(String id) {
        // sviluppi futuri: Implementa la cancellazione di un utente specifico
    }

    @Override
    public void delete(boolean all) {
        // sviluppi futuri: Implementa la cancellazione di tutti gli utenti
    }
}
