package edu.opjms.main;

import edu.opjms.fileListPopup.FileListPopup;
import edu.opjms.global.systemFolder.FolderProviderKt;
import edu.opjms.templating.Templator;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import java.io.File;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

//    public static final StackPane STAGE_ROOT = new StackPane();

    @Override
    public void start(Stage primaryStage) {
        //            final Parent root = FXMLLoader.load(getClass().getResource("/fxml/TitleScreen.fxml"));
/*
            STAGE_ROOT.getChildren().setAll(root);


            Scene scene = new Scene(STAGE_ROOT);
*/

//            primaryStage.setScene(scene);
        final var templateDataFolder = FolderProviderKt.getDataFolder().resolve("templates");







        Templator templator = new Templator(primaryStage, getHostServices(), templateDataFolder);
//        var scene = new Scene(templator)


       /* final int year = FolderProviderKt.getCurrentSession();
        final File parent = FolderProviderKt.getDataFolder();

        final File thisSession = FolderProviderKt.sessionFolder(year, parent);
        final File oldSession = FolderProviderKt.sessionFolder(year - 1, parent);

        System.out.println(thisSession.getPath());
        System.out.println(oldSession.getPath());

        Scene scene = new Scene(new FileListPopup(thisSession, oldSession));
        scene.setFill(Color.WHITESMOKE);*/

//        primaryStage.setScene(scene);
//        new JMetro(scene, Style.LIGHT);

//        new Thread(this::createFolder).start();

        primaryStage.show();


    }

    /*private void createFolder() {
        final File dataFolder = FolderProviderKt.getDataFolder();
        final var dataFolderPath = Path.of(dataFolder.toURI());

        if (Files.notExists(dataFolderPath)) {
            try {
                Files.createDirectories(dataFolderPath);
            } catch (IOException e) {
                Logger.getLogger("Main#createFolder").log(Level.WARNING, "Could not create global data folder");
                e.printStackTrace();
            }
        }

        final int year = FolderProviderKt.getCurrentSession();

        final Path currentSession = dataFolderPath.resolve(year + "-" + (year + 1));

        if (Files.notExists(currentSession)) {
            try {
                Files.createDirectory(currentSession);
            } catch (IOException e) {
                Logger.getLogger("Main#createFolder").log(Level.WARNING,
                        "Could not create container folder for this session");
                e.printStackTrace();
            }
        }





        //delete folders older than 2 years old
        try (var folders = Files.newDirectoryStream(dataFolderPath)) {

            final int baseYear = year - 3;

            FileVisitor<? super Path> deleter = new FileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    throw exc;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            };

            for (final Path folder : folders) {

                final int folderYear = getYear(folder.getFileName().toString().substring(0, 4));

                if (folderYear < baseYear) {
                    Files.walkFileTree(folder, deleter);
                }
            }


        } catch (IOException e) {
            Logger.getLogger("Main#createFolder").log(Level.WARNING,
                    "Could not delete old folders");
            e.printStackTrace();
        }

    }*/


    /**
     * Returns the integral value of string. Returns -1 if string's length > 4 or string contains
     * any other character than a digit
     *
     * @param string The string to be parsed
     * @return integral value of string or -1
     */
    private static int getYear(String string) {
        final int length = string.length();

        if (length > 4) // we assume an year with size > 4 to be invalid
            return -1;

        int ret = 0;

        for (int i = 0; i < length; i++) {

            char c = string.charAt(i);

            if (c >= '0' && c <= '9') {
                ret *= 10;
                ret += c - '0'; // subtraction from code point of '0' gives the integral value
            } else
                return -1;
        }

        return ret;
    }

}
