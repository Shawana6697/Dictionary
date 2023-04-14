package assignment.dictionary;

import javafx.application.Platform;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author Mahmoud Algharbawi, Nicolas Hidalgo, Shawana Tahseen
 *mfa0106, nh0277  st0611
 */

public class MisSpellActionThread implements Runnable {

    private final String textFileName;
    private final String dictionaryFileName;
    private final LinesToDisplay myLines;
    private final DictionaryInterface<String, String> myDictionary;
    DictionaryController controller;
    private boolean dictionaryLoaded;

    /**
     * Constructor for objects of class MisspellActionThread
     *
     * @param controller
     */
    public MisSpellActionThread(DictionaryController controller) {
        super();

        this.controller = controller;
        textFileName = "src/main/resources/assignment/dictionary/check.txt";
        dictionaryFileName = "src/main/resources/assignment/dictionary/sampleDictionary.txt";

        myDictionary = new HashedMapAdaptor<String, String>();
        myLines = new LinesToDisplay();
        dictionaryLoaded = false;

    }

    @Override
    public void run() {

        loadDictionary(dictionaryFileName, myDictionary);


        Platform.runLater(() -> {
            if (dictionaryLoaded) {
                controller.SetMsg("The Dictionary has been loaded");
            } else {
                controller.SetMsg("No Dictionary is loaded");
            }
        });

        checkWords(textFileName, myDictionary);

    }

    /**
     * Load the words into the dictionary.
     *
     * @param theFileName   The name of the file holding the words to put in the
     *                      dictionary.
     * @param theDictionary The dictionary to load.
     */
    public void loadDictionary(String theFileName, DictionaryInterface<String, String> theDictionary) {
        Scanner input;
        try {
            String inString;
            String correctWord;

            input = new Scanner(new File(theFileName));


            while (input.hasNext()) // read until  end of file
            {
                correctWord = input.next();
                theDictionary.add(correctWord, correctWord);
            }
            dictionaryLoaded = true;


        } catch (IOException e) {
            System.out.println("There was an error in reading or opening the file: " + theFileName);
            System.out.println(e.getMessage());
        }

    }

    /**
     * Get the words to check, check them, then put Wordlets into myLines. When
     * a single line has been read do an animation step to wait for the user.
     */
    public void checkWords(String theFileName, DictionaryInterface<String, String> theDictionary) {
        Scanner input;
        try {
            String inString;
            String aWord;

            input = new Scanner(new File(theFileName));


            while (input.hasNextLine()) // Read until end of file
            {
// ADD CODE HERE
// >>>>>>>>>>> ADDED CODE >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
                String line = input.nextLine();

                for (String word : line.split(" ")) {
                    this.checkWord(word,theDictionary);
                }
                myLines.nextLine();
//<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

                this.showLines(myLines);
            }

        } catch (IOException e) {}

    }

    /**
     * Check the spelling of a single word.
     */
    public boolean checkWord(String word, DictionaryInterface<String, String> theDictionary) {
        boolean result = false;
        // ADD CODE HERE
//>>>>>>>>>>> ADDED CODE >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        myLines.addWordlet(new Wordlet(" " + word+ " ", theDictionary.contains(word)));
//<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        return result;
    }

    private void showLines(LinesToDisplay lines) {
        try {
            Thread.sleep(500);

            Platform.runLater(() -> {
                System.out.println("Updating View" + lines.toString());
                controller.UpdateView(lines);
                if (lines != null) {
                    System.out.println("Woohooo lines not null");
                    controller.UpdateView(lines);
                }
            });
        } catch (InterruptedException ex) {
            System.out.println(ex);
        }
    }

} // end class MisspellActionThread
