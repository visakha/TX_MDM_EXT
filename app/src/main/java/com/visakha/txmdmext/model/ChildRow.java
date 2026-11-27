package com.visakha.txmdmext.model;

/** One row of a many-valued field group, keyed by its _id. Each model has its own records. */
public interface ChildRow {

  String childId();

  ChildGroup group();
}
