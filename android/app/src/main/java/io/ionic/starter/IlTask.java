package io.ionic.starter;

public interface IlTask {
  // Setters
  void setId(int id);
  void setDescription(String description);
  void setIsSelected(boolean isSelected);

  // Getters
  int getId();
  String getDescription();
  boolean isSelected();
}
