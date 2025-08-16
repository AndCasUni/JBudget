package it.unicam.cs.mpgc.jbudget125637.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record Operation(String id,
                        String autore,
                        String desc,
                        double amount,
                        String date,
                        List<String> tags) {

    public Operation {
        if (id == null || desc == null ||  date == null) {
            throw new IllegalArgumentException("All fields must be non-null");
        }

    }
    public String getId() {
        return id;
    }
    public String getAutore() {
        return autore;
    }
    public String getDesc() {
        return desc;
    }
    public double getAmount() {
        return amount;
    }
    public String getDate() {
        return date;
    }
    public List<String> getTags() {
        return tags;
    }
    @Override
    public String toString() {
        return "Operation{" +
                "id='" + id + '\'' +
                ", desc='" + desc + '\'' +
                ", amount=" + amount +
                ", date='" + date + '\'' +
                '}';
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Operation operation = (Operation) o;
        return Double.compare(operation.amount, amount) == 0 &&
                id.equals(operation.id) &&
                desc.equals(operation.desc) &&
                date.equals(operation.date);
    }
    @Override
    public int hashCode() {
        int result;
        long temp;
        result = id.hashCode();
        result = 31 * result + desc.hashCode();
        temp = Double.doubleToLongBits(amount);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + date.hashCode();
        return result;
    }
    public void addElementToXML() {
        try {
            File xmlFile = new File("app/data/operations.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setIgnoringElementContentWhitespace(true);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc;

            // Carica il documento XML esistente oppure creane uno nuovo
            if (xmlFile.exists()) {
                doc = dBuilder.parse(xmlFile);
            } else {
                doc = dBuilder.newDocument();
                Element rootElement = doc.createElement("operations");
                doc.appendChild(rootElement);
            }
            doc.getDocumentElement().normalize();

            // Creazione nuovo nodo <operation>
            Element operationElement = doc.createElement("operation");

            // <author>
            Element authorElement = doc.createElement("author");
            authorElement.appendChild(doc.createTextNode(this.autore));
            operationElement.appendChild(authorElement);

            // <description>
            Element descriptionElement = doc.createElement("description");
            descriptionElement.appendChild(doc.createTextNode(this.desc));
            operationElement.appendChild(descriptionElement);

            // <date>
            Element dateElement = doc.createElement("date");

            String formattedDate = this.date; // già una stringa
            try {
                // Conversione da yyyy-MM-dd a dd/MM/yyyy
                LocalDate parsedDate = LocalDate.parse(this.date);
                formattedDate = parsedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (Exception e) {
                // Se non riesce a parsare, mantiene la stringa originale
            }

            dateElement.appendChild(doc.createTextNode(formattedDate));
            operationElement.appendChild(dateElement);

            // <amount>
            Element amountElement = doc.createElement("amount");
            amountElement.appendChild(doc.createTextNode(String.valueOf(this.amount)));
            operationElement.appendChild(amountElement);

            // <tags>
            Element tagsElement = doc.createElement("tags");
            int idCounter = 1;
            for (String tag : this.tags) {
                Element tagElement = doc.createElement("tag");
                tagElement.setAttribute("id", String.valueOf(idCounter++));
                tagElement.appendChild(doc.createTextNode(tag));
                tagsElement.appendChild(tagElement);
            }
            operationElement.appendChild(tagsElement);

            // Aggiungi la nuova <operation> al root <operations>
            doc.getDocumentElement().appendChild(operationElement);

            // Salvataggio su file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(xmlFile);
            transformer.transform(source, result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
