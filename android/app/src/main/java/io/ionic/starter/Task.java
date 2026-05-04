package io.ionic.starter;

public class Task implements IlTask {
  private int id;
  private String description;
  private boolean isSelected;

  @Override
  public void setId(int id) {
    this.id = id;
  }

  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public void setIsSelected(boolean isSelected) {
    this.isSelected = isSelected;
  }

  @Override
  public int getId() {
    return id;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public boolean isSelected() {
    return isSelected;
  }
}
