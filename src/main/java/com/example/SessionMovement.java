
package com.example;

public class SessionMovement {
  private Movement movement;
  private String activity;
  private int numReps;

  public SessionMovement(Movement movement, int numReps) {
    this.movement = movement;
    this.numReps = numReps;
  }

  public Movement getMovement() {
    return this.movement;
  }

  public void setActivity(String activity) {
    this.activity = activity;
  }

  public String getActivity() {
    return this.activity;
  }


  public int getNumReps() {
    return this.numReps;
  }

  public String toString() {
    return this.numReps + " " + this.movement.getName() + " " + this.getActivity();
  }

  public boolean isSameMov(SessionMovement other) {
    if (other == this) {
      return true;
    }

    if (!(other instanceof SessionMovement)) {
      return false;
    }

    return this.getMovement().getName().equals(other.getMovement().getName());
  }


  public boolean isSameType(SessionMovement other) {
    if (other == this) {
      return true;
    }

    if (!(other instanceof SessionMovement)) {
      return false;
    }

    return this.getMovement().getType().equals(other.getMovement().getType());
  }
}
