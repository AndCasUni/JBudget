package it.unicam.cs.mpgc.jbudget125637.persistency;

import it.unicam.cs.mpgc.jbudget125637.model.Currency;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SettingsManager {
    private static final String SETTINGS_FILE_PATH = "app/config/settings.properties";
    private static final String CURRENCY_XML_PATH = "app/data/currency.xml";
    private static final String DEFAULT_CURRENCY = "EUR";

    public String getCurrency() {
        return loadProperties().getProperty("currency", DEFAULT_CURRENCY);
    }

    public boolean isDarkMode() {
        return Boolean.parseBoolean(loadProperties().getProperty("darkMode", "false"));
    }

    public void saveCurrency(String currency) {
        saveProperty("currency", currency);
    }

    public void saveDarkMode(boolean darkMode) {
        saveProperty("darkMode", Boolean.toString(darkMode));
    }

    public List<Currency> loadCurrencyCodes() {
        List<Currency> currencies = new ArrayList<>();
        try (FileInputStream in = new FileInputStream(CURRENCY_XML_PATH)) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(in);

            NodeList currencyNodes = doc.getElementsByTagName("currency");
            for (int i = 0; i < currencyNodes.getLength(); i++) {
                Element element = (Element) currencyNodes.item(i);
                String code = element.getAttribute("code");
                double toEuro = Double.parseDouble(element.getElementsByTagName("to_EUR").item(0).getTextContent());
                double fromEuro = Double.parseDouble(element.getElementsByTagName("from_EUR").item(0).getTextContent());
                currencies.add(new Currency(code, toEuro, fromEuro));
            }
        } catch (Exception e) {
            throw new RuntimeException("Errore nel caricamento delle valute", e);
        }
        return currencies;
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        File settingsFile = new File(SETTINGS_FILE_PATH);

        if (settingsFile.exists()) {
            try (FileInputStream in = new FileInputStream(settingsFile)) {
                props.load(in);
            } catch (Exception e) {
                // Log dell'errore ma continuiamo con i valori di default
            }
        }
        return props;
    }

    private void saveProperty(String key, String value) {
        Properties props = loadProperties();
        props.setProperty(key, value);

        File configDir = new File("app/config");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        try (FileOutputStream out = new FileOutputStream(SETTINGS_FILE_PATH)) {
            props.store(out, "Impostazioni utente");
        } catch (Exception e) {
            throw new RuntimeException("Errore nel salvataggio delle impostazioni", e);
        }
    }
}