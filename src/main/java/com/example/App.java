package com.example;

import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;

import java.util.*;
import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class App {

  public static final int SECS_PER_REP = 3;
  public static final int REST_TIME = 30;
  public static final int ERROR_RANGE = 90;
  public static final int WARMUP_TIME = 3;
  public static final int[][] SETS_RANGE = { { 2, 3 }, { 3, 6 }, { 3, 6 } };
  public static int[][] REPS_RANGE = { { 10, 15 }, { 10, 15 }, { 10, 25 } };

  public static int timeSelection;
  public static int numTrainDays;
  public static Map<String, List<Movement>> allMovements;
  public static Map<String, Integer> typeCounter;
  public static Map<Movement, Integer> movementCounter;
  public static int size;
  public static int difficulty;
  public static List<List<SessionMovement>> finalSchedule;
  public static List<String> activities;
  public static Map<String, Integer> activityCounter;

  /**
   * Filters movements that cannot be done by the user and returns the list of
   * possible excercises
   * the user can do, group by body parts excercised
   * 
   * @param scanner the scanner to read from user input
   * @return Map<String, List<Movement>>: the list of avaliable excercises, group
   *         by body parts excercised
   */
  public static Map<String, List<Movement>> buildData(Scanner scanner) {

    List<String[]> data = new ArrayList<>(); // List to store all the read data
    try {
      Scanner file = new Scanner(new FileReader(
        "src/main/java/com/example/Movements.csv"));
      String line = "";
      String splitBy = ",";
      line = file.nextLine();
      while (file.hasNextLine()) {
        line = file.nextLine();
        data.add(line.split(splitBy));
      }

      file.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    allMovements = new HashMap<>(); // Actual map to store all the data

    // Formatting all data to meet the requirements to store the data and add the
    // data
    for (String[] movement : data) {
      String[] equipments = movement[3].split(", ");

      for (int i = 0; i < equipments.length; i++) {
        if (equipments[i].charAt(0) == '\"') {
          equipments[i] = equipments[i].substring(
            1, equipments[i].length());
        }
        if (equipments[i].charAt(equipments[i].length() - 1) == '\"') {
          equipments[i] = equipments[i].substring(
            0, equipments[i].length() - 1);
        }
      }

      int level = 0;
      switch (movement[2]) {
        case "Beginner":
          level = 1;
          break;
        case "Intermediate":
          level = 2;
          break;
        case "Advanced":
          level = 3;
          break;
        default:
          System.out.println("Impossible");
          break;
      }

      Movement mov = new Movement(movement[0], movement[1], level, equipments);
      if (allMovements.containsKey(movement[0])) {
        allMovements.get(movement[0]).add(mov);
      } else {
        List<Movement> part_move = new ArrayList<>();
        part_move.add(mov);
        allMovements.put(movement[0], part_move);
      }
    }

    List<String> forbiddenTypes = new ArrayList<>();
    scanner.nextLine();

    // Ask if all body parts are fine
    for (String type : allMovements.keySet()) {
      System.out.print("Do you have any injuries at your " 
               + type + " (y for yes and other for no)?: ");
      String ans = scanner.nextLine();
      if (ans.equals("y")) {
        forbiddenTypes.add(type);
      }
    }

    // Remove all the movements that cannot be done by the user because of injured
    // body part
    for (String e : forbiddenTypes) {
      allMovements.remove(e);
    }

    // Determine the experience of the user
    try (Scanner infoScanner = new Scanner(System.in)) {
      System.out.println("How long have you been training? \n" +
          "1: 0 - 8 months \n" +
          "2: 8 - 12 months \n" +
          "3: More than 1 year");
      int durationChoice = infoScanner.nextInt();

      System.out.println("What is your average excercise " + 
          "frequency per week in the last year? \n" +
          "1: 0 - 2 times per week \n" +
          "2: 3 - 4 times per week \n" +
          "3: More than 4 times per week");

      int frequencyChoice = infoScanner.nextInt();
      difficulty = Math.min(durationChoice, frequencyChoice);

      // Check for avaliable equipments
      Set<String> validEquipments = new HashSet<>();
      validEquipments.add("None");
      Set<String> invalidEquipments = new HashSet<>();
      infoScanner.nextLine();
      for (String type : allMovements.keySet()) {
        List<Movement> movements = allMovements.get(type);

        for (int i = 0; i < movements.size(); i++) {
          Movement m = movements.get(i);

          // Remove inaccurate difficulty
          if (m.getDifficulty() > difficulty) {
            movements.remove(i);
            i--;
          } else {
            String[] equipments = m.getEquipments();
            boolean flag = false;
            for (int j = 0; j < equipments.length; j++) {
              if (!validEquipments.contains(equipments[j]) && 
                  !invalidEquipments.contains(equipments[j])) {
                System.out.print("Do you have equipment: " + equipments[j] 
                  + " (y for yes and other for no)?: ");

                String ans = infoScanner.nextLine();

                if (!ans.equals("y")) {
                  invalidEquipments.add(equipments[j]);
                } else {
                  validEquipments.add(equipments[j]);
                }
              }

              if (invalidEquipments.contains(equipments[j])) {
                flag = true;
              }

            }
            if (flag) {
              movements.remove(i);
              i--;
            }
          }
        }
      }
    }

    // Remove list if list is empty
    List<String> removedList = new ArrayList<>();
    for (String type : allMovements.keySet()) {
      if (allMovements.get(type).size() == 0) {
        removedList.add(type);
      }
    }

    for (String type : removedList) {
      allMovements.remove(type);
    }

    typeCounter = new HashMap<>();
    for (String e : allMovements.keySet()) {
      typeCounter.put(e, 0);
    }

    return allMovements;
  }

  public static void initialzeActivities() throws FileNotFoundException {
    activities = new ArrayList<>();

    try (Scanner file = new Scanner(new FileReader(
      "src/main/java/com/example/Activities.csv"))) {
      String line = "";
      while (file.hasNextLine()) {
        line = file.nextLine();
        activities.add(line);
      }
    }

  }


  /**
   * Initialize all the data needed to create the fitness schedule
   * 
   * @return Map<String, List<Movement>>: the list of avaliable excercises, group
   *         by body parts excercised
   * @throws FileNotFoundException 
   */
  public static Map<String, List<Movement>> initialzeData() throws FileNotFoundException {
    try (Scanner inputScanner = new Scanner(System.in)) {
      System.out.println("Welcome to the training session scheduling system,");
      System.out.println("Enter the number of train days you" + 
                           "like to schedule for (1 - 14)");

      numTrainDays = inputScanner.nextInt();

      // generate schedule - get information
      System.out.println("Length of each session? \n" +
          "1: 30 minutes \n" +
          "2: 45 minutes \n" +
          "3: 60 minutes \n" +
          "please enter one of the choices above. ");
      int timeChoice = inputScanner.nextInt();

      switch (timeChoice) {
        case 1:
          timeSelection = (30 - WARMUP_TIME) * 60;
          REPS_RANGE[0][0] = 6;
          REPS_RANGE[1][0] = 6;
          REPS_RANGE[2][0] = 6;
          break;

        case 2:
          timeSelection = (45 - WARMUP_TIME) * 60;
          break;

        case 3:
          timeSelection = (60 - WARMUP_TIME) * 60;
          break;

        default:
          System.out.println("Impossible");
          break;
      }

      buildData(inputScanner);

      // for (String e : data.keySet()) {
      // System.out.print(e + ": [");
      // List<Movement> mvs = data.get(e);
      // for (int i = 0; i < mvs.size() - 1; i++) {
      // System.out.print(mvs.get(i).getName() + ", ");
      // }
      // System.out.print(mvs.get(mvs.size() - 1).getName());
      // System.out.println("]");
      // }
    }

    movementCounter = new HashMap<>();
    for (String pt : allMovements.keySet()) {
      List<Movement> mvs = allMovements.get(pt);
      for (Movement m : mvs) {
        movementCounter.put(m, 0);
      }
    }

    size = 0;
    for (String e : allMovements.keySet()) {
      size += allMovements.get(e).size();
    }

    initialzeActivities();
    return allMovements;
  }

  // Check if the body parts used is balanced
  public static boolean checkPartDiff() {
    int min = 2147483647;
    int max = -1;

    for (String type : typeCounter.keySet()) {
      int val = typeCounter.get(type);
      if (min > val) {
        min = val;
      }
      if (max < val) {
        max = val;
      }
    }

    return (max - min) <= 1;
  }

  // Check if the number of movements used is balanced
  public static boolean checkMovDiff(String part) {
    int min = 2147483647;
    int max = -1;

    for (Movement mov : movementCounter.keySet()) {
      String type = mov.getType();
      if (type.equals(part)) {
        int val = movementCounter.get(mov);
        if (min > val) {
          min = val;
        }
        if (max < val) {
          max = val;
        }
      }
    }

    return (max - min) <= 1;
  }

  // Calculate total time the schedule takes (in seconds)
  public static int calcTotalTime(List<List<Integer>> movIntructs) {
    int sum = 0;

    for (List<Integer> mov : movIntructs) {
      for (int rep : mov) {
        sum += rep * SECS_PER_REP;
        sum += REST_TIME;
      }
    }

    return sum;
  }

  // Check if the session schedule is a valid session schedule
  public static boolean isValidSchedule(List<SessionMovement> mvs) {
    for (int i = 1; i < mvs.size(); i++) {
      if (mvs.get(i).isSameType(mvs.get(i - 1))) {
        return false;
      }
    }
    return true;
  }



  // Format the result in a form that can be written out as a csv file
  public static List<String[]> formatOutput(List<List<SessionMovement>> schedule) {
    List<String[]> res = new ArrayList<>();
    int maxcol = 0;
    for (int i = 0; i < schedule.size(); i++) {
      if (schedule.get(i).size() > maxcol) {
        maxcol = schedule.get(i).size();
      }
    }
    String[] header = new String[2 * maxcol + 1];
    header[0] = "Day";
    for (int i = 0; i < maxcol; i++) {
      header[2 * i + 1] = "Num Reps";
      header[2 * i + 2] = "Movement";
    }
    res.add(header);
    for (int i = 0; i < schedule.size(); i++) {
      
      String[] e = new String[2 * schedule.get(i).size() + 1];
      e[0] = String.valueOf(i + 1);
      for (int j = 0; j < schedule.get(i).size(); j++) {
        e[2 * j + 1] = String.valueOf(schedule.get(i).get(j).getNumReps());
        e[2 * j + 2] = schedule.get(i).get(j).getMovement().getName();
      }
      res.add(e);
    }
    return res;
  }

  public static boolean isValidActivitySchedule(List<String> a) {
    for (int i = 1; i < a.size(); i++) {
      if (a.get(i).equals(a.get(i - 1))) {
        return false;
      }
    }
    return true;
  }

  public static void generateSchedule(List<List<SessionMovement>> outputSchedule, 
                                      String fileName, List<String> parts) {
    Random rand = new Random();
    List<List<Movement>> schedule = new ArrayList<>();


    // Generate random schedule
    for (int i = outputSchedule.size(); i < numTrainDays; i++) {
      List<Movement> sessionSchedule = new ArrayList<>();
      int numMovementPerSession = 0;

      // Generate random number of movements for one session
      switch (timeSelection) {
        case (30 - WARMUP_TIME) * 60:
          numMovementPerSession = rand.nextInt(3) + 8;
          break;

        case (45 - WARMUP_TIME) * 60:
          numMovementPerSession = rand.nextInt(3) + 11;
          break;

        case (60 - WARMUP_TIME) * 60:
          numMovementPerSession = rand.nextInt(2) + 14;
          break;
        default:
          System.out.println("Wrong value entered");
          break;
      }

      // Pick a random list of movements to excercise
      for (int j = 0; j < numMovementPerSession; j++) {
        int partIndex = rand.nextInt(parts.size());

        List<Movement> movements = allMovements.get(parts.get(partIndex));
        int size = movements.size();
        Movement movement = null;

        // Generate random movement
        int index = rand.nextInt(size);
        movement = movements.get(index);

        // Add movement to a seesion schedule
        sessionSchedule.add(movement);
        typeCounter.put(movement.getType(), typeCounter.get(movement.getType()) + 1);
        movementCounter.put(movement, movementCounter.get(movement) + 1);

        // Check if newly added movement is valid
        boolean isPartValid = checkPartDiff();
        boolean isMovValid = checkMovDiff(movement.getType());
        if (!isPartValid || !isMovValid) {
          sessionSchedule.remove(movement);
          typeCounter.put(movement.getType(), typeCounter.get(movement.getType()) - 1);
          movementCounter.put(movement, movementCounter.get(movement) - 1);
          j -= 1;
        }

      }
      // Add session schedule to overall schedule
      schedule.add(sessionSchedule);
    }

    // Combine duplicates
    for (List<Movement> e : schedule) {
      e = new ArrayList<>(new HashSet<>(e));
    }

    // Generate random amount of sets and reps for each movement
    for (int k = 0; k < schedule.size(); k++) {
      int size = schedule.get(k).size();
      List<List<Integer>> numRepSet = new ArrayList<>();
      int sets = 0;
      int reps = 0;
      for (int i = 0; i < size; i++) {
        List<Integer> movReps = new ArrayList<>();

        // Set random amount of sets for each movement
        sets = rand.nextInt(SETS_RANGE[difficulty - 1][1] 
             - SETS_RANGE[difficulty - 1][0] + 1)
             + SETS_RANGE[difficulty - 1][0];

        // Set random amount of reps for each set
        for (int j = 0; j < sets; j++) {
          reps = rand.nextInt(REPS_RANGE[difficulty - 1][1] 
               - REPS_RANGE[difficulty - 1][0] + 1)
               + REPS_RANGE[difficulty - 1][0];

          movReps.add(reps);
        }
        numRepSet.add(movReps);
      }

      // Calculate the total time needed for the current schedule
      int totalTime = calcTotalTime(numRepSet);

      // Balance the schedule so that the total time meets 
      // the time requirement from the user
      while (Math.abs(totalTime - timeSelection) >= ERROR_RANGE) {
        if (totalTime < timeSelection) {
          if (timeSelection - totalTime >= SETS_RANGE[difficulty - 1][0]
              * REPS_RANGE[difficulty - 1][0]
              * SECS_PER_REP + REST_TIME) {
            
            // Try to add a set from the movement that has the least sets
            int min = numRepSet.get(0).size();
            int minMovement = 0;

            // Find the movement that has the least number of sets
            for (int i = 0; i < numRepSet.size(); i++) {
              int val = numRepSet.get(i).size();

              if (val < min) {
                min = val;
                minMovement = i;
              }
            }

            // Check if adding a set still meets the number of sets constraint
            if (numRepSet.get(minMovement).size() < SETS_RANGE[difficulty - 1][1]) {
              numRepSet.get(minMovement).add(rand.nextInt(REPS_RANGE[difficulty - 1][1]
                  - REPS_RANGE[difficulty - 1][0] + 1)
                  + REPS_RANGE[difficulty - 1][0]);
            } else {
              // Cannot add one more set, add 1 rep to the movement that has 
              // the least amount of rep in one set
              int minRow = 0;
              int minCol = 0;
              min = numRepSet.get(0).get(0);
              for (int i = 0; i < numRepSet.size(); i++) {
                int range = numRepSet.get(i).size();
                for (int j = 0; j < range; j++) {
                  int val = numRepSet.get(i).get(j);
                  if (val < min) {
                    min = val;
                    minRow = i;
                    minCol = j;
                  }
                }
              }

              // Check if added rep is still within the reps constraint
              if (min < REPS_RANGE[difficulty - 1][1]) {
                numRepSet.get(minRow).set(minCol, numRepSet.get(minRow).get(minCol) + 1);
              } else {
                // Cannot add another rep, this is the best we can do
                break;
              }
            }
          } else {
            // Add 1 rep to the movement that has the least amount of rep in one set
            int minRow = 0;
            int minCol = 0;
            int min = 10000000;
            for (int i = 0; i < numRepSet.size(); i++) {
              int range = numRepSet.get(i).size();
              for (int j = 0; j < range; j++) {
                int val = numRepSet.get(i).get(j);
                if (val < min) {
                  min = val;
                  minRow = i;
                  minCol = j;
                }
              }
            }

            // Check if added rep is still within the reps constraint
            if (min < REPS_RANGE[difficulty - 1][1]) {
              numRepSet.get(minRow).set(minCol, numRepSet.get(minRow).get(minCol) + 1);
            } else {
              // Cannot add another rep, this is the best we can do
              break;
            }
          }

        } else {
          if (totalTime - timeSelection >= SETS_RANGE[difficulty - 1][0]
              * REPS_RANGE[difficulty - 1][0]
              * SECS_PER_REP + REST_TIME) {

            // Try to remove a set from the movement that has the most sets
            int minRepCol = 0;
            int minval = numRepSet.get(0).get(0);

            int maxSize = numRepSet.get(0).size();
            int maxMovement = 0;

            // Find the movement that has the most number of sets
            for (int i = 0; i < numRepSet.size(); i++) {
              int val = numRepSet.get(i).size();

              if (val > maxSize) {
                maxSize = val;
                maxMovement = i;
              }
            }

            List<Integer> target = numRepSet.get(maxMovement);

            for (int i = 0; i < target.size(); i++) {
              int val = target.get(i);
              if (val < minval) {
                minval = val;
                minRepCol = i;
              }
            }

            // Check if removing a set still meets the number of sets constraint
            if (target.size() > SETS_RANGE[difficulty - 1][0]) {
              numRepSet.get(maxMovement).remove(minRepCol);
            } else {
              // Cannot remove one more set, remove 1 rep to the movement that
              // has the most amount of rep in one set
              int maxRow = 0;
              int maxCol = 0;
              int max = -1;
              for (int i = 0; i < numRepSet.size(); i++) {
                int range = numRepSet.get(i).size();
                for (int j = 0; j < range; j++) {
                  int val = numRepSet.get(i).get(j);
                  if (val > max) {
                    max = val;
                    maxRow = i;
                    maxCol = j;
                  }
                }
              }

              // Check if added removed is still within the reps constraint
              if (max > REPS_RANGE[difficulty - 1][0]) {
                numRepSet.get(maxRow).set(maxCol, numRepSet.get(maxRow).get(maxCol) - 1);
              } else {
                // Cannot remove another rep, this is the best we can do
                break;
              }
            }

          } else {
            // Find the movement that has the most number of sets
            int maxRow = 0;
            int maxCol = 0;
            int max = -1;
            for (int i = 0; i < numRepSet.size(); i++) {
              int range = numRepSet.get(i).size();
              for (int j = 0; j < range; j++) {
                int val = numRepSet.get(i).get(j);
                if (val > max) {
                  max = val;
                  maxRow = i;
                  maxCol = j;
                }
              }
            }

            // Remove 1 rep to the movement that has the most amount of rep in one set
            if (max > REPS_RANGE[difficulty - 1][0]) {
              numRepSet.get(maxRow).set(maxCol, numRepSet.get(maxRow).get(maxCol) - 1);
            } else {
              // Cannot remove another rep, this is the best we can do
              break;
            }
          }

        }

        // Update total time used for the schedule
        totalTime = calcTotalTime(numRepSet);
      }


      // Print out assignment for one session
      int time = 0;
      for (int i = 0; i < numRepSet.size(); i++) {
        List<Integer> mvs = numRepSet.get(i);
        System.out.print("Movement " + (i + 1) + ": ");
        System.out.print("[");
        for (int j = 0; j < mvs.size() - 1; j++) {
          System.out.print(mvs.get(j) + ", ");
          time += mvs.get(j) * SECS_PER_REP;
          time += REST_TIME;
        }
        System.out.println(mvs.get(mvs.size() - 1) + "]");
        time += mvs.get(mvs.size() - 1) * SECS_PER_REP;
        time += REST_TIME;
      }
      System.out.println("Total Time: " + (time / 60.0) + "\n");

      // Random assign movements to each session (including number of sets)
      List<SessionMovement> sessionSchedule = new ArrayList<>();
      for (int i = 0; i < schedule.get(k).size(); i++) {
        Movement mov = schedule.get(k).get(i);
        int num = numRepSet.get(i).size();
        for (int j = 0; j < num; j++) {
          SessionMovement sessionmov = new SessionMovement(mov, numRepSet.get(i).get(j));
          sessionSchedule.add(sessionmov);
        }
      }

      // Shuffle the movements so that the session schedule is valid
      while (!isValidSchedule(sessionSchedule)) {
        Collections.shuffle(sessionSchedule);
      }

      // Add schedule to final schedule
      outputSchedule.add(sessionSchedule);
    }


    // Generate random activities for the schedule
    int size = 0;
    for (int i = 0; i < outputSchedule.size(); i++) {
      size += outputSchedule.get(i).size();
    }

    List<String> act = new ArrayList<>();

    while (act.size() < size) {
      act.add(activities.get(rand.nextInt(activities.size())));

      if (!isValidActivitySchedule(act)) {
        act.remove(act.size() - 1);
      }
    }

    // for (int i = 0; i < act.size(); i++) {
    //   System.out.println(act.get(i));
    // }

    for (int i = 0; i < outputSchedule.size(); i++) {
      
      for (int j = 0; j < outputSchedule.get(i).size(); j++) {
        outputSchedule.get(i).get(j).setActivity(act.get(0));
        act.remove(0);
      }
    }

    // Print out final schedule
    for (int i = 0; i < outputSchedule.size(); i++) {
      System.out.println("Day " + (i + 1) + ": [");
      for (int j = 0; j < outputSchedule.get(i).size() - 1; j++) {
        System.out.println(outputSchedule.get(i).get(j) + ", ");
      }
      System.out.println(outputSchedule.get(i).get(
        outputSchedule.get(i).size() - 1) + "]");
    }


    int maxlen = 0;

    for (int i = 0; i < outputSchedule.size(); i++) {
        if (outputSchedule.get(i).size() > maxlen) {
            maxlen = outputSchedule.get(i).size();
        }
    }

    // Compile the result in a PNG file using LaTeX
    String latex = "\\begin{tabular}{|";

    for (int i = 0; i < outputSchedule.size(); i++) {
        latex += "c|c|c|c|c|c|c|c|c|c|";
    }
    latex += "} \\hline" + "\\multicolumn{" + (outputSchedule.size() * 10) + 
             "}{|c|}{ \\textbf{" + fileName + "}} \\\\ \\hline";

    for (int i = 0; i < outputSchedule.size() - 1; i++) {
        latex += "\\multicolumn{10}{|c|}{ \\textbf{Day " + (i + 1) + "}}" + "&";
    }
    latex += "\\multicolumn{10}{|c|}{ \\textbf{Day " + outputSchedule.size() + "}}";
    latex += "\\\\ \\hline";

    for (int i = 0; i < outputSchedule.size() - 1; i++) {
        latex += "\\multicolumn{2}{|c|}{ \\textbf{Mov #}}" + "&"
               + "\\multicolumn{2}{|c|}{ \\textbf{# Reps}}" + "&" 
               + "\\multicolumn{2}{|c|}{ \\textbf{Movement Name}}" + "&" 
               + "\\multicolumn{2}{|c|}{ \\textbf{Activity}}" + "&" 
               + "\\multicolumn{2}{|c|}{ \\textbf{Checked}}" + "&";
    }
    latex += "\\multicolumn{2}{|c|}{ \\textbf{Mov #}}" + "&" 
               + "\\multicolumn{2}{|c|}{ \\textbf{# Reps}}" + "&" 
               + "\\multicolumn{2}{|c|}{ \\textbf{Movement Name}}" + "&" 
               + "\\multicolumn{2}{|c|}{ \\textbf{Activity}}" + "&" 
               + "\\multicolumn{2}{|c|}{ \\textbf{Checked}}";

    latex += "\\\\ \\hline";

    for (int column = 0; column < maxlen; column++) {
        // int cnt = 0;
        for (int row = 0; row < outputSchedule.size(); row++) {
            if (column < outputSchedule.get(row).size()) {
                latex += "\\multicolumn{2}{|c|}{\\textbf{" + (column  + 1) + "}}" + "&" +
                     "\\multicolumn{2}{|c|}{\\text{" 
                     + outputSchedule.get(row).get(column).getNumReps() + "}}" + "&" +
                     "\\multicolumn{2}{|c|}{\\text{" 
                     + outputSchedule.get(row).get(column).getMovement().getName() + "}}" 
                     + "&" + "\\multicolumn{2}{|c|}{\\text{" 
                     + outputSchedule.get(row).get(column).getActivity() + "}}" + "&" + 
                     "\\multicolumn{2}{|c|}{\\text{" 
                     + "}}" + "&";
            } else {
                // cnt++;
                latex += " \\multicolumn{8}{|c|}{}&";
            }
        }

        if (latex.charAt(latex.length() - 1) == '&') {
            latex = latex.substring(0, latex.length() - 1);
        }

        latex += "\\\\ \\hline";
    }

    
    latex += "\\end{tabular}";

    // System.out.println(latex);

    TeXFormula formula = new TeXFormula(latex);
    formula.createPNG(TeXConstants.STYLE_DISPLAY, 20, "target/" + fileName 
                      + ".png", Color.WHITE, Color.BLACK);

  }

  public static void main(String[] args) throws IOException {

    // Initialize the usable list of movements
    allMovements = initialzeData();

    // List of usable body parts
    List<String> parts = new ArrayList<>();
    for (String part : allMovements.keySet()) {
      parts.add(part);
    }

    finalSchedule = new ArrayList<>();
    generateSchedule(finalSchedule, "Ver_1", parts);

    List<List<SessionMovement>> temp = new ArrayList<>();
    temp.add(finalSchedule.get(0));

    generateSchedule(temp, "Ver_2", parts);
  }
}
