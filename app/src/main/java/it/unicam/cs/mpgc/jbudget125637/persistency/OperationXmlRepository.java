package it.unicam.cs.mpgc.jbudget125637.persistency;

import it.unicam.cs.mpgc.jbudget125637.model.Operation;
import it.unicam.cs.mpgc.jbudget125637.model.Tags;
import org.w3c.dom.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class OperationXmlRepository implements IOOperationRepository<Operation> {

    private final String filePath = "app/data/operations.xml";

    /**
     * Legge il file XML e converte ogni elemento <operation> in un oggetto Operation.
     * Gestisce la conversione delle date dal formato "dd/MM/yyyy" a "yyyy-MM-dd".
     * Legge i tag associati ad ogni operazione, se presenti.
     */
    @Override
    public List<Operation> read() {
        List<Operation> operations = new ArrayList<>();
        try {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = dBuilder.parse(new File(filePath));
            doc.getDocumentElement().normalize();

            NodeList list = doc.getElementsByTagName("operation");

            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy"); // nel file
            DateTimeFormatter isoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");  // nel model

            for (int i = 0; i < list.getLength(); i++) {
                Element el = (Element) list.item(i);

                String id = el.getElementsByTagName("id").item(0).getTextContent();
                String author = el.getElementsByTagName("author").item(0).getTextContent();
                String description = el.getElementsByTagName("description").item(0).getTextContent();
                String rawDate = el.getElementsByTagName("date").item(0).getTextContent();

                // conversione in ISO per il model
                String date;
                try {
                    LocalDate parsed = LocalDate.parse(rawDate, inputFormatter);
                    date = parsed.format(isoFormatter);
                } catch (Exception e) {
                    date = rawDate; // fallback
                }

                double amount = Double.parseDouble(el.getElementsByTagName("amount").item(0).getTextContent());

                // Leggi i tag
                List<Tags> tags = new ArrayList<>();
                NodeList tagList = el.getElementsByTagName("tag");
                for (int j = 0; j < tagList.getLength(); j++) {
                    Element tagElement = (Element) tagList.item(j);

                    // Prendo l’attributo id (se presente)
                    String tagId = tagElement.hasAttribute("id") ? tagElement.getAttribute("id") : "";

                    // Prendo il contenuto del tag (es. "Stipendio")
                    String tagName = tagElement.getTextContent();

                    Boolean isParent = tagElement.hasAttribute("isParent");

                    tags.add(new Tags(tagId, tagName, isParent));
                }


                operations.add(new Operation(id, author, description, amount, date, tags));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return operations;
    }

    /**
     * Salva una lista di oggetti Operation nel file XML.
     * Converte le date dal formato "yyyy-MM-dd" a "dd/MM/yyyy" per la memorizzazione.
     * Scrive i tag associati ad ogni operazione, se presenti.
     */
    @Override
    public void save(List<Operation> items) {
        try {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = dBuilder.newDocument();

            Element root = doc.createElement("operations");
            doc.appendChild(root);

            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // nel model
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy"); // nel file

            for (Operation op : items) {
                Element opElem = doc.createElement("operation");

                Element idElem = doc.createElement("id");
                idElem.appendChild(doc.createTextNode(op.getId()));
                opElem.appendChild(idElem);

                Element authorElem = doc.createElement("author");
                authorElem.appendChild(doc.createTextNode(op.getAutore()));
                opElem.appendChild(authorElem);

                Element descElem = doc.createElement("description");
                descElem.appendChild(doc.createTextNode(op.getDesc()));
                opElem.appendChild(descElem);

                Element dateElem = doc.createElement("date");
                String formattedDate;
                try {
                    LocalDate parsed = LocalDate.parse(op.getDate(), inputFormatter);
                    formattedDate = parsed.format(outputFormatter);
                } catch (Exception e) {
                    formattedDate = op.getDate();
                }
                dateElem.appendChild(doc.createTextNode(formattedDate));
                opElem.appendChild(dateElem);

                Element amountElem = doc.createElement("amount");
                amountElem.appendChild(doc.createTextNode(String.valueOf(op.getAmount())));
                opElem.appendChild(amountElem);

                // Se i tag esistono, li scrivo
                Element tagsElem = doc.createElement("tags");
                if (op.getTags() != null && !op.getTags().isEmpty()) {
                    for (Tags t : op.getTags()) {
                        Element tagElem = doc.createElement("tag");
                        tagElem.setAttribute("id", t.id());
                        tagElem.appendChild(doc.createTextNode(t.description()));
                        tagsElem.appendChild(tagElem);
                    }
                }
                opElem.appendChild(tagsElem);

                root.appendChild(opElem);
            }

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            DOMSource source = new DOMSource(doc);
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                transformer.transform(source, new StreamResult(fos));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Elimina un'operazione specifica dal file XML in base al suo ID.
     * Rilegge il file, rimuove l'elemento corrispondente e riscrive il file aggiornato.
     */
    @Override
    public void delete(String id) {
        try {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = dBuilder.parse(new File(filePath));

            NodeList list = doc.getElementsByTagName("operation");
            for (int i = 0; i < list.getLength(); i++) {
                Element el = (Element) list.item(i);
                String opId = el.getElementsByTagName("id").item(0).getTextContent();
                if (opId.equals(id)) {
                    el.getParentNode().removeChild(el);
                    break;
                }
            }

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                transformer.transform(source, new StreamResult(fos));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Elimina tutte le operazioni dal file XML.
     * Crea un backup del file originale prima di sovrascriverlo con un file vuoto.
     */
    @Override
    public void delete(boolean all) {
        if (all) {
            try {
                File file = new File(filePath);
                File backup = new File("app/data/operations_backup.xml");

                if (file.exists()) {
                    if (backup.exists()) backup.delete();
                    if (!file.renameTo(backup)) {
                        throw new Exception("Impossibile rinominare il file per il backup.");
                    }
                }

                String emptyXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><operations></operations>";
                try (FileOutputStream out = new FileOutputStream(file, false)) {
                    out.write(emptyXml.getBytes("UTF-8"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
