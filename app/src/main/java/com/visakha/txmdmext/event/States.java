package com.visakha.txmdmext.event;

/** states object. createMode is in real events but not in schema 002. */
public record States(String base, String validation, String createMode) {

  public static final States EMPTY = new States(null, null, null);
}
