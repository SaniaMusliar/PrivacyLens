package com.privacylens.model;

/**
 * Broad topic categories used by EvidenceValidator to check whether
 * retrieved evidence actually relates to the question being asked,
 * rather than just sharing a keyword with it.
 */
public enum Topic {
    DATA_COLLECTION,
    DATA_SHARING,
    RETENTION,
    USER_CONTROLS,
    PURPOSE,
    ADVERTISING,
    GENERAL
}
