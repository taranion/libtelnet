package org.prelle.telnet.option;

/**
 * Typsichere Aufzählung der von MXP definierten Elemente.
 *
 * Der Enum-Konstantenname entspricht dabei genau dem normalisierten
 * (großgeschriebenen) Elementnamen, wie ihn auch {@link MxpSupportTable}
 * intern verwendet - siehe {@link #name()}.
 *
 * Da diese Aufzählung geschlossen ist, aber vom Server potenziell auch
 * (noch) unbekannte Elemente gemeldet werden können, bleibt die
 * eigentliche Datenhaltung in MxpSupportTable string-basiert. Dieses Enum
 * ist lediglich eine typsichere Komfort-Schicht für die Elemente, die man
 * im eigenen Code bereits kennt und gezielt abfragen möchte.
 *
 * Neue Elemente einfach hier ergänzen - MxpSupportTable selbst muss dafür
 * nicht angepasst werden.
 */
public enum MXPElement {
    A,
    B,
    BODY,
    BOLD,
    BR,
    C,
    COLOR,
    DEST,
    ELEMENT,
    EM,
    ENTITY,
    EXPIRE,
    FONT,
    FRAME,
    GAUGE,
    H,
    H1,
    H2,
    H3,
    H4,
    H5,
    H6,
    HEAD,
    HIGH,
    HR,
    HTML,
    I,
    IMG,
    ITALIC,
    LI,
    MUSIC,
    OL,
    P,
    PASSWORD,
    PRE,
    S,
    SAMP,
    SEND,
    SMALL,
    SOUND,
    STAT,
    STRIKEOUT,
    STRONG,
    SUPPORT,
    TITLE,
    TT,
    U,
    UL,
    UNDERLINE,
    USER,
    VAR,
    VERSION
}
