package org.prelle.telnet.option;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Verwaltet eine Liste von unterstützten/nicht unterstützten XML-Elementen
 * und deren Attributen, wie sie z.B. im MXP-Protokoll ("+element.attribut")
 * übertragen werden.
 *
 * Format der Tokens:
 *   +element            -> Element wird unterstützt
 *   -element            -> Element-Support wird zurückgezogen
 *   +element.attribut   -> Attribut "attribut" von <element> wird unterstützt
 *   -element.attribut   -> Attribut-Support wird zurückgezogen
 *
 * Die Struktur ist bewusst offen gehalten: unbekannte Elemente/Attribute
 * werden beim Parsen automatisch angelegt, und man kann jederzeit
 * programmatisch neue hinzufügen (addElement/addAttribute).
 */
public class MxpSupportTable {

    /** Support-Status eines einzelnen Elements inkl. seiner Attribute. */
    public static class ElementSupport {
        private boolean supported;
        private final Map<String, Boolean> attributes = new LinkedHashMap<>();

        public boolean isSupported() {
            return supported;
        }

        public boolean isAttributeSupported(String attribute) {
            return attributes.getOrDefault(attribute.toUpperCase(), Boolean.FALSE);
        }

        public Set<String> getSupportedAttributes() {
            Set<String> result = new LinkedHashSet<>();
            for (Map.Entry<String, Boolean> e : attributes.entrySet()) {
                if (e.getValue()) {
                    result.add(e.getKey());
                }
            }
            return result;
        }

        void setSupported(boolean supported) {
            this.supported = supported;
        }

        void setAttributeSupported(String attribute, boolean supported) {
            attributes.put(attribute.toUpperCase(), supported);
        }
    }

    /**
     * Elemente, die laut MXP-Spec (https://mudstandards.org/mud/mxp#text-formatting)
     * semantisch identisch sind, aber unter mehreren Namen gemeldet werden können
     * ("Any above tag name can be used for HTML compatibility, although the <X> tag
     * is preferred for MUD usage"). Als kanonische Form wird jeweils die von der
     * Spec bevorzugte Kurzform verwendet.
     *
     * Achtung: die Spec garantiert nicht, dass ein Client wirklich alle Aliase
     * meldet - daher werden hier alle Varianten auf denselben internen Eintrag
     * abgebildet, statt sich auf Vollständigkeit der Meldung zu verlassen.
     */
    private static final Map<String, String> ALIASES = buildAliases();

    private static Map<String, String> buildAliases() {
        Map<String, String> aliases = new HashMap<>();
        aliases.put("BOLD", "B");
        aliases.put("STRONG", "B");
        aliases.put("ITALIC", "I");
        aliases.put("EM", "I");
        aliases.put("IMAGE", "IMG");
        aliases.put("UNDERLINE", "U");
        aliases.put("STRIKEOUT", "S");
        aliases.put("HIGH", "H");
        aliases.put("C", "COLOR");
        // Weitere Aliase, beobachtet bei Mudlet/MUSHclient (nicht in der offiziellen Spec
        // explizit als "Alias" benannt, aber laut Spec-Text mit identischer Bedeutung):
        aliases.put("USERNAME", "USER");
        aliases.put("PASS", "PASSWORD");
        aliases.put("V", "VAR");
        return aliases;
    }

    /** Normalisiert einen Elementnamen: Großschreibung + Auflösung bekannter Aliase. */
    private static String canonicalize(String elementName) {
        String upper = elementName.toUpperCase();
        return ALIASES.getOrDefault(upper, upper);
    }

    /**
     * Attribut-Aliase, die (anders als Element-Aliase) nur innerhalb eines
     * bestimmten Elements gelten. Schlüssel-Format: "ELEMENT.ATTRIBUT"
     * (ELEMENT bereits kanonisiert). Beobachtet z.B. bei Mudlet/MUSHclient:
     * <FONT FGCOLOR=..> ist gleichbedeutend mit <FONT COLOR=..>.
     */
    private static final Map<String, String> ATTRIBUTE_ALIASES = buildAttributeAliases();

    private static Map<String, String> buildAttributeAliases() {
        Map<String, String> aliases = new HashMap<>();
        aliases.put("FONT.FGCOLOR", "COLOR");
        aliases.put("FONT.BGCOLOR", "BACK");
        return aliases;
    }

    /** elementName muss bereits kanonisiert sein (siehe {@link #canonicalize}). */
    private static String canonicalizeAttribute(String elementName, String attributeName) {
        String upperAttribute = attributeName.toUpperCase();
        return ATTRIBUTE_ALIASES.getOrDefault(elementName + "." + upperAttribute, upperAttribute);
    }

    private final Map<String, ElementSupport> elements = new LinkedHashMap<>();

    /**
     * Parst einen kompletten Support-String (z.B. den vom Server gesendeten
     * "+a +a.href +a.hint ... -frame.left ...") und aktualisiert die interne
     * Tabelle. Kann mehrfach aufgerufen werden (z.B. für inkrementelle Updates).
     */
    public void parse(String input) {
        if (input == null || input.isBlank()) {
            return;
        }
        for (String token : input.trim().split("\\s+")) {
            if (token.isEmpty()) {
                continue;
            }
            parseToken(token);
        }
    }

    private void parseToken(String token) {
        char prefix = token.charAt(0);
//        if (prefix != '+' && prefix != '-') {
//            // Unbekanntes/ungültiges Token einfach ignorieren
//            return;
//        }
        boolean supported = (prefix != '-');
        String path = token.substring((prefix=='+'||prefix=='-') ? 1 : 0).trim();
        int dotIndex = path.indexOf('.');

        String elementName = canonicalize(dotIndex == -1 ? path : path.substring(0, dotIndex));
        ElementSupport element = elements.computeIfAbsent(elementName, k -> new ElementSupport());

        if (dotIndex == -1) {
            element.setSupported(supported);
        } else {
            String attributeName = canonicalizeAttribute(elementName, path.substring(dotIndex + 1));
            element.setAttributeSupported(attributeName, supported);
        }
    }

    // ---- Abfrage-API ----

    public boolean isElementSupported(String elementName) {
        ElementSupport el = elements.get(canonicalize(elementName));
        return el != null && el.isSupported();
    }

    public boolean isElementSupported(MXPElement element) {
        return isElementSupported(element.name());
    }

    public boolean isAttributeSupported(String elementName, String attributeName) {
        String canonicalElement = canonicalize(elementName);
        ElementSupport el = elements.get(canonicalElement);
        return el != null && el.isAttributeSupported(canonicalizeAttribute(canonicalElement, attributeName));
    }

    public boolean isAttributeSupported(MXPElement element, String attributeName) {
        return isAttributeSupported(element.name(), attributeName);
    }

    /** Liefert das ElementSupport-Objekt oder null, falls das Element unbekannt ist. */
    public ElementSupport getElement(String elementName) {
        return elements.get(canonicalize(elementName));
    }

    public ElementSupport getElement(MXPElement element) {
        return getElement(element.name());
    }

    public Set<String> getSupportedElements() {
        Set<String> result = new LinkedHashSet<>();
        for (Map.Entry<String, ElementSupport> e : elements.entrySet()) {
            if (e.getValue().isSupported()) {
                result.add(e.getKey());
            }
        }
        return result;
    }

    // ---- Programmatisches Hinzufügen (z.B. für eigene Erweiterungen) ----

    public void addElement(String elementName) {
        elements.computeIfAbsent(canonicalize(elementName), k -> new ElementSupport())
                .setSupported(true);
    }

    public void addElement(MXPElement element) {
        addElement(element.name());
    }

    public void removeElement(String elementName) {
        ElementSupport el = elements.get(canonicalize(elementName));
        if (el != null) {
            el.setSupported(false);
        }
    }

    public void removeElement(MXPElement element) {
        removeElement(element.name());
    }

    public void addAttribute(String elementName, String attributeName) {
        String canonicalElement = canonicalize(elementName);
        elements.computeIfAbsent(canonicalElement, k -> new ElementSupport())
                .setAttributeSupported(canonicalizeAttribute(canonicalElement, attributeName), true);
    }

    public void addAttribute(MXPElement element, String attributeName) {
        addAttribute(element.name(), attributeName);
    }

    public void removeAttribute(String elementName, String attributeName) {
        String canonicalElement = canonicalize(elementName);
        ElementSupport el = elements.get(canonicalElement);
        if (el != null) {
            el.setAttributeSupported(canonicalizeAttribute(canonicalElement, attributeName), false);
        }
    }

    public void removeAttribute(MXPElement element, String attributeName) {
        removeAttribute(element.name(), attributeName);
    }

    // ---- Demo ----

    public static void main(String[] args) {
        String input = "+a +a.href +a.hint +a.expire +b +bold +br +color +color.fore +color.back "
                + "+frame +frame.name +frame.left +frame.right -frame.right +version "
                + "+i +italic +em +u +underline +s +strikeout +strong +h +high";

        MxpSupportTable table = new MxpSupportTable();
        table.parse(input);

        // b, bold und strong sind Aliase und werden intern zusammengeführt
        System.out.println("MXPElement.B unterstützt:      " + table.isElementSupported(MXPElement.B));
        System.out.println("MXPElement.BOLD unterstützt:   " + table.isElementSupported(MXPElement.BOLD));
        System.out.println("MXPElement.STRONG unterstützt: " + table.isElementSupported(MXPElement.STRONG));
        // ebenso i / italic / em -> I, u / underline -> U, s / strikeout -> S, h / high -> H
        System.out.println("MXPElement.EM unterstützt:     " + table.isElementSupported(MXPElement.EM));

        System.out.println("Element <a> unterstützt:      " + table.isElementSupported(MXPElement.A));
        System.out.println("Attribut a.href unterstützt:  " + table.isAttributeSupported(MXPElement.A, "href"));
        System.out.println("Element <frame> unterstützt:  " + table.isElementSupported(MXPElement.FRAME));
        System.out.println("frame.left unterstützt:       " + table.isAttributeSupported(MXPElement.FRAME, "left"));
        System.out.println("frame.right unterstützt:      " + table.isAttributeSupported(MXPElement.FRAME, "right"));
        System.out.println("Unbekanntes Element <xyz>:    " + table.isElementSupported("XYZ"));
        System.out.println("Alle unterstützten Elemente:  " + table.getSupportedElements());
        System.out.println("Attribute von <color>:        " + table.getElement(MXPElement.COLOR).getSupportedAttributes());

        // Beispiel mit dem tatsächlichen Mudlet-SUPPORT-String
        String mudletInput = "+head +body +afk +title +username +pass +samp +h +high +i +option +bold "
                + "+xch_page +reset +strong +recommend_option +support +ul +em +send +send.href "
                + "+send.hint +send.xch_cmd +send.xch_hint +send.prompt +p +hr +html +user +password "
                + "+a +a.href +a.xch_cmd +a.xch_hint +underline +b +img +img.src +img.xch_mode +pre "
                + "+li +ol +c +c.fore +c.back +color +color.fore +color.back +font +font.color "
                + "+font.back +font.fgcolor +font.bgcolor +u +mxp +mxp.off +version +br +v +var +italic";

        MxpSupportTable mudletTable = new MxpSupportTable();
        mudletTable.parse(mudletInput);

        // username/pass sind Aliase von user/password, v ist Alias von var
        System.out.println("MXPElement.USER unterstützt:      " + mudletTable.isElementSupported(MXPElement.USER));
        System.out.println("MXPElement.VAR unterstützt:       " + mudletTable.isElementSupported(MXPElement.VAR));
        // font.fgcolor/.bgcolor sind Attribut-Aliase von font.color/.back
        System.out.println("font.color unterstützt:           " + mudletTable.isAttributeSupported(MXPElement.FONT, "color"));
        System.out.println("font.back unterstützt:            " + mudletTable.isAttributeSupported(MXPElement.FONT, "back"));
        // afk, mxp, option, reset, recommend_option sind proprietär und (noch) nicht im Enum -
        // funktionieren aber trotzdem transparent über die String-API:
        System.out.println("'afk' (proprietär) unterstützt:   " + mudletTable.isElementSupported("afk"));
    }
}
