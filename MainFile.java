/**
 *@author Mohammad Naushad Bhat
 * @version 1.5 16/03/2018
 * @since  1.0 5/03/2018
 */

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Main class of the program
 */
public class Main {
    //declaring required fields
    private Frame myFrame;
    private Label topLabel;
    private Label message;
    private Label errorMessage;
    private Label botLabel1;
    private Label botLabel2;
    private Panel panel;
    private Panel panel2;

    /**
     * Constructor of the Main Class
     */
    public Main(){
        makeGui();
    }

    /**
     * Fuction to make GUI for the program
     *
     */
    public void makeGui(){

        myFrame = new Frame("Assignment 1 Java 4-cse-A1");
        myFrame.setBackground(Color.white);
        myFrame.setSize(900,600);
        myFrame.setLayout(new GridLayout(7,1));

/**
 * This is used to close the window upon clicking cross button
 */
        myFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent){
                System.exit(0);
            }
        });

        topLabel = new Label();
        topLabel.setAlignment(Label.CENTER);
        message = new Label();
        message.setAlignment(Label.CENTER);
        errorMessage = new Label();
        errorMessage.setAlignment(Label.CENTER);
        botLabel1 = new Label();
        botLabel1.setAlignment(Label.CENTER);
        botLabel2 = new Label();
        botLabel2.setAlignment(Label.CENTER);

        panel = new Panel();
        panel.setLayout(new FlowLayout());
        panel.setBackground(Color.white);

        panel2 = new Panel();
        panel2.setLayout(new FlowLayout());
        panel2.setBackground(Color.white);

        myFrame.add(topLabel);
        myFrame.add(panel);
        myFrame.add(botLabel1);
        myFrame.add(botLabel2);
        myFrame.add(errorMessage);
        myFrame.add(panel2);


        myFrame.setVisible(true);

        topLabel.setText("Click on BROWSE button to select a file. To see final files , Click on SAVED FILES button");
    }

    /**
     * This function checks the operating System type. This function is needed to make the program platform independent.
     * This program however supports only Windows, Mac , unix and Linux Operating Systems
     *
     * @return  It returns the suitable path based on the Operating System
     */

    public static String checkOS(){
        String OS = System.getProperty("os.name").toLowerCase();
        if(OS.indexOf("win") >= 0){

            return "C:/";
        } else if(OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 ){
            return "/home/";
        } else if(OS.indexOf("mac") >= 0){
            return "/home/";
        } else
            return null;
    }

    /**
     * This function does the following tasks:
     *      It sets the BROWSE button in action to open File Dialogue(a Window) which lets us select any file on computer and open the file
     *      It then saves the file to a particular location assigned by checkOS() function.
     *      It displays the detaile about the file on the Frame
     *      It splits the file into a certain number of parts as given in the program.
     */
    private void showFiles() {

        Button button1 = new Button("BROWSE");
        myFrame.add(button1);
        panel.add(button1);
        Button button2 = new Button("SAVED FILES");
        myFrame.add(button2);
        panel2.add(button2);

        FileDialog fileDialogue = new FileDialog(myFrame);

        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileDialogue.setVisible(true);

                //storing path of selected file in fileName
                String fileName = fileDialogue.getDirectory() + fileDialogue.getFile();

                try {
                    // split function called from here and the fileName is passed to it , in which the path of the selected file is stored
                    split(fileName);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

               // getting extension of selected file
                String extension = "";
                int i = fileName.lastIndexOf('.');
                if (i > 0) {
                    extension = fileName.substring(i+1);// to get part of string substring() is used
                }

                File file = new File(fileName);
                botLabel1.setText("You have selected the file " + fileDialogue.getFile()
                        + " from Directory :->    " + fileDialogue.getDirectory() );
                botLabel2.setText( "The length of file is "+ file.length() + "  Bytes"
                        + " ( " + (file.length()/1024) + " kB ) and the extension (type of file) is " + extension);

                //Here first, the write protection of directory is checked and the the file is saved to that location
                if(file.canWrite()){
                    Path sourceFile = Paths.get(fileName);
                    Path targetFile = Paths.get(checkOS() + "copiedFile." + extension);//accepts sitable path from checkOS function

                    try {
                        Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException ex) {
                        errorMessage.setText("I/O Error when copying file. Check write protections.");
                    }


                }
            }
        });



        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileDialogue.setDirectory(checkOS());
                fileDialogue.setVisible(true);


            }
        });


    }

    /**
     * This function splits the file into a particular number of parts
     *
     * @param fileName fileName is passed as parameter to this function which is used in RandomAccessFile, which requires the path of file to be splitted.
     * @throws IOException
     */
    public static void split(String fileName) throws IOException {


        RandomAccessFile raf = new RandomAccessFile(fileName, "r");
        long numSplits = 10;
        long sourceSize = raf.length();
        long bytesPerSplit = sourceSize / numSplits;
        long remainingBytes = sourceSize % numSplits;

        int maxReadBufferSize = 8 * 1024; //8KB
        for (int destIx = 1; destIx <= numSplits; destIx++) {
            BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(checkOS() + "split." + destIx));//getting path by checkOS()
            if (bytesPerSplit > maxReadBufferSize) {
                long numReads = bytesPerSplit / maxReadBufferSize;
                long numRemainingRead = bytesPerSplit % maxReadBufferSize;
                for (int i = 0; i < numReads; i++) {
                    readWrite(raf, bw, maxReadBufferSize);
                }
                if (numRemainingRead > 0) {
                    readWrite(raf, bw, numRemainingRead);
                }
            } else {
                readWrite(raf, bw, bytesPerSplit);
            }
            bw.close();
        }
        if (remainingBytes > 0)

        {
            BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(checkOS() + "split." + (numSplits + 1)));
            readWrite(raf, bw, remainingBytes);
            bw.close();
        }
        raf.close();

    }

    /**
     *
     * @param raf
     * @param bw
     * @param numBytes
     * @throws IOException
     */
    public static void readWrite(RandomAccessFile raf, BufferedOutputStream bw, long numBytes) throws IOException {
        byte[] buf = new byte[(int) numBytes];
        int val = raf.read(buf);
        if (val != -1) {
            bw.write(buf);
        }
    }

    /**
     *
     * @param args used to accept input from command line
     * @throws IOException
     */
    //Main function -> to make the program work
    public static void main(String[] args) throws IOException {
        Main awt = new Main();
        awt.checkOS();
        awt.showFiles();
    }

}


