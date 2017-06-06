/**
 * Created by emilearseneault on 2017-06-05.
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.*;
import java.util.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class FileAnalysis {

    private static String url = "jdbc:postgresql://s6ie1702.gel.usherbrooke.ca:5432/postgres";
    private static String user = "administrateur";
    private static String passwd = "s6infoe17";

    public static void main(String[] args)
    {
        System.out.println("Program Start");

        // GET FILE LIST


        String path = "/Users/emilearseneault/Desktop/foremilio/src/TestingFile.txt";
        List<String> filePathArray = getFileList(42);
        System.out.println(filePathArray);

        // ANALYSE
        //getFileContent(path);
        //getFileMetaData(path);

        //getFileMetaData(filePathArray.get(0));

        // Faire l'analyse ou les analyses
        List<String> analyse = sizeAnalysis(filePathArray);
        System.out.println(analyse);

        // Pass Analysis to Database
        // Pass objects : Doc 1, Doc 2, Methode, Commentaire, Pourcentage, Debut 1, Fin 1, Debut 2, Fin 2
        // Pour itération 1 : Doc 1, Doc 2, Methode (Size), Commentaire (SizeOfFile)


    }

    public static List<String> getFileList(int remiseID) {

        List<String> filePathList = new ArrayList<String>();

        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(url, user, passwd);

            //Création d'un objet Statement
            Statement state = conn.createStatement();

            //Query d'insert
            String remiseQuery = "SELECT id, location, nom from itération1.document WHERE rem_id = " + remiseID + ";";
            String noremiseQuery = "SELECT id, location, nom from itération1.document;";
            String query = null;

            if (remiseID == 0) {
                query = noremiseQuery;
            }
            else {
                query = remiseQuery;
            }

            ResultSet result = state.executeQuery(query);

            // Get path and name columnid to concatenate
            ResultSetMetaData resultMeta = result.getMetaData();
            int pathID = 0;
            int nameID = 0;

            for(int i = 1; i <= resultMeta.getColumnCount();i++) {
                if (resultMeta.getColumnLabel(i).equals("location")){
                    pathID = i;
                }
                if (resultMeta.getColumnLabel(i).equals("nom")){
                    nameID = i;
                }
            }

            // Concatenate path and filename to return
            String filePath = null;
            String fileName = null;
            while(result.next()) {
                    filePath = result.getObject(pathID).toString();
                    fileName = result.getObject(nameID).toString();
                    filePathList.add(filePath + fileName);
            }

            result.close();
            state.close();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return filePathList;
    }

    public static void getFileContent(String filepathname)
    {
        File file = new File(filepathname);
        BufferedReader reader = null;


        List<String> fileArray = new ArrayList<String>();

        try {
            reader = new BufferedReader(new FileReader(file));
            String text = null;

            while ((text = reader.readLine()) != null) {
                fileArray.add(text);
            }
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
            fileArray = null;
        }
        finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            }
            catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        System.out.println(fileArray);
    }

    public static void getFileMetaData(String filepathname)
    {
        Path filepath = Paths.get(filepathname);

        try {
            BasicFileAttributes attr = Files.readAttributes(filepath, BasicFileAttributes.class);

            System.out.println("creationTime: " + attr.creationTime());
            System.out.println("lastAccessTime: " + attr.lastAccessTime());
            System.out.println("lastModifiedTime: " + attr.lastModifiedTime());

            System.out.println("isDirectory: " + attr.isDirectory());
            System.out.println("isOther: " + attr.isOther());
            System.out.println("isRegularFile: " + attr.isRegularFile());
            System.out.println("isSymbolicLink: " + attr.isSymbolicLink());
            System.out.println("size: " + attr.size());
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static long getSizeOfFile(String filepathname)
    {
        Path filepath = Paths.get(filepathname);
        long size = 0;

        try {
            BasicFileAttributes attr = Files.readAttributes(filepath, BasicFileAttributes.class);
            size = attr.size();
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return size;
    }

    public static List<String> sizeAnalysis(List<String> filePathList)
    {

        List<Long> fileSizeArray = new ArrayList<Long>();
        List<Long> uniqueFileSize = new ArrayList<Long>();
        List<String> sameSizeNameTemp = new ArrayList<String>();
        List<Integer> sameSizeIndexTemp = new ArrayList<Integer>();
        List<String> analysisStringArray = new ArrayList<String>();

        String analysisString = "";

        // Get all sizes and uniques
        int uniqueIndex = 0;
        boolean found = false;
        for (int i = 0; i < filePathList.size(); i++)
        {
            fileSizeArray.add(getSizeOfFile(filePathList.get(i)));

            // Find uniques
            uniqueIndex = uniqueFileSize.size();
            found = false;

            while (uniqueIndex != 0 && found == false) {
                uniqueIndex--;
                if (fileSizeArray.get(i).equals(uniqueFileSize.get(uniqueIndex))) {
                    found = true;
                }
            }

            if (uniqueIndex == 0 && found == false) {
                uniqueFileSize.add(fileSizeArray.get(i));
            }
            uniqueIndex = 0;
        }

        System.out.println(uniqueFileSize);

        // Find same sizes
        for (int i = 0; i < uniqueFileSize.size(); i++) {

            for (int j = 0; j < fileSizeArray.size(); j++){
                if (uniqueFileSize.get(i).equals(fileSizeArray.get(j))) {
                    sameSizeNameTemp.add(filePathList.get(j));
                    sameSizeIndexTemp.add(j);
                }
            }

            for (int j = 0; j < sameSizeNameTemp.size(); j++) {
                for (int k = 0; k < sameSizeNameTemp.size(); k++){
                    // Create record
                    if (j != k){
                        analysisString = "{Doc 1 : " + filePathList.get(sameSizeIndexTemp.get(j))
                                + ", Doc 2 : " + filePathList.get(sameSizeIndexTemp.get(k))
                                + ", Methode : size, Commentaire : " + fileSizeArray.get(sameSizeIndexTemp.get(j)) + "}";
                        analysisStringArray.add(analysisString);
                    }
                }
            }

            sameSizeNameTemp.clear();
            sameSizeIndexTemp.clear();
        }

        return analysisStringArray;
    }
}
