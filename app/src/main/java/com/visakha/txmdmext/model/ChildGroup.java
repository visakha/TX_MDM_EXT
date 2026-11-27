package com.visakha.txmdmext.model;

/** A many-valued field group of a model: its key in the payload and its Type 2 history table. */
public interface ChildGroup {

  String jsonName();

  String table();
}
