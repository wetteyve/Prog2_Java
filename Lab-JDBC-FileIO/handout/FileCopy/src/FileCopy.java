import java.io.*;
import java.io.IOException;


public class FileCopy {
	public static void main(String[] args) throws IOException {

		/* Schreiben Sie ein Program welches Kopien von Dateien in einem Verzeichnis erstellt.
		 * Das Quell-Verzeichnis soll als Konsolenargument uebergeben und auf Korrektheit ueberprueft werden.
		 * Korrekt bedeutet, dass das Verzeichnis existiert und ausser zwei Dateien mit den Namen rmz450.jpg
		 * und rmz450-spec.txt nichts weiter enthaelt.
		 * Jede Datei soll zweimal kopiert werden, einmal zeichen-orientiert und einmal byte-orientiert.
		 * Dazu soll die jeweilige Datei geoeffnet und Element fuer Element gelesen und ebenso wieder geschrieben werden.
		 * Die Kopien sollen so benannt werden, damit aus dem Dateinamen hervorgeht, mit welcher Methode sie erstellt wurde.
		 * Oeffnen Sie die Kopien anschliessend mit einem entsprechenden Programm und erklaeren Sie die entsprechenden Effekte.
		 * Oeffnen Sie die Kopien anschliessend mit einem HEX-Editor und erklaeren Sie die Gruende fuer die Effekte.
		 */

		/* Teilaufgabe a - Verzeichnisstruktur */
        String dirpath = args[0];
        File file = new File(dirpath);
        File pwd = new File(".");
        System.out.println(pwd.getAbsolutePath());
        if (file.isDirectory()) {
            System.out.println("Directory " + dirpath + " exists.");
        } else {
            System.out.println("Directory " + dirpath + " does not exist. Terminating program because lol.");
            return;
        }

        boolean rmz450jpg = false;
        boolean rmz450spectx = false;

        File[] files = file.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                System.out.println("Directory within " + dirpath + " detected. Terminating program, lol!");
            } else {
                switch(f.getName()) {
                    case "rmz450.jpg": rmz450jpg = true;
                    break;
                    case "rmz450-spec.txt": rmz450spectx = true;
                    break;
                    default: System.out.println("Directory " + dirpath + " does not comply with predefined structure. Terminating program.");
                    return;
                }
            }
        }
        if (rmz450jpg && rmz450spectx) {
            System.out.println("Directory structure test passed successfully.");
        } else {
            System.out.println("Directory " + dirpath + " does not comply with predefined structure. Terminating program.");
            return;
        }

		/* Teilaufgabe b - Kopieren von Dateien */
        FileInputStream fis = null;
        FileOutputStream fos = null;
        FileReader frin = null;
        FileWriter frout = null;

        System.out.println("Initiating file copies.");
        for (File f : files) {
            try{
                fis = new FileInputStream(f);
                fos = new FileOutputStream("copy-bin-"+f.getName());
                frin = new FileReader(f);
                frout = new FileWriter("copy-char-"+f.getName());
                int b;
                System.out.println("Binary copy.");
                while ((b = fis.read()) != -1) {
                    fos.write(b);
                }
                int c;
                System.out.println("Character-oriented copy.");
                while ((c = frin.read()) != -1) {
                    frout.write(c);
                }
            }catch(IOException ioe){
                System.err.println("IOException. Terminating program."); return;
            }
            finally{
                System.out.println("Closing program correctly.");
                if (fis != null) {
                    fis.close(); fis=null;
                }
                if (fos != null) {
                    fos.close(); fos=null;
                }
                if (frin != null) {
                    frin.close(); frin=null;
                }
                if (frout != null) {
                    frout.close(); frout=null;
                }

            }
        }
	}
}
