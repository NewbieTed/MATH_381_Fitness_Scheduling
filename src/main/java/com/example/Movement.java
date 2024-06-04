package com.example;
public class Movement {
  private String name;
  private String movementType;
  private int difficulty;
  private String[] equipments;

  public Movement(String movementType, String name, int difficulty, String[] equipments) {
    this.name = name;
    this.movementType = movementType;
    this.difficulty = difficulty;
    this.equipments = equipments;
  }

  public String getName() {
    return this.name;
  }

  public String getType() {
    return this.movementType;
  }

  public String toString() {
    return this.name;
  }

  public int getDifficulty() {
    return difficulty;
  }

  public String[] getEquipments() {
    return this.equipments;
  }

  public boolean equals(Movement other) {
    if (other == this) {
      return true;
    }

    if (!(other instanceof Movement)) {
      return false;
    }

    return this.getName().equals(other.getName());
  }
}
