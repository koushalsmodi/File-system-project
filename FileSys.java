/*
 * FileSys.java uses recursion and trees to create a computer's file system.
 */

// imported the necessary required packages 
import java.util.Scanner;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Comparator;
import java.nio.file.Path;
import java.nio.file.Paths;

/*
 * User class has attributes such as name, type: directory or file, parent directory reference, 
 * subdirectories, and file content .
 */
class User {
    private String name;
    private boolean isDirectory;
    private User parent;
    private List<User> subDirectories;
    private String content;
    private static User currentDirectory;
    private static User rootDirectory;

    // creating a directory
    User(User parent, String name, boolean isDirectory) {
        this.name = name;
        this.parent = parent;
        this.isDirectory = isDirectory;
        this.subDirectories = new ArrayList<>();
        this.content = ""; // initialize content for directories
        if (parent != null) {
            parent.addSubDirectory(this);
        }
    }

    // creating a file with content
    User(User parent, String name, boolean isDirectory, String content) {
        this(parent, name, isDirectory);
        if (!isDirectory) {
            this.content = content;
        }
    }

    // getters and setters
    String getName() {
        return name;
    }

    boolean isDirectory() {
        return isDirectory;
    }

    User getParent() {
        return parent;
    }

    List<User> getSubDirectories() {
        return subDirectories;
    }

    String getContent() {
        return content;
    }

    void setContent(String content) {
        this.content = content;
    }

    // adding a subdirectory to the current directory
    void addSubDirectory(User directory) {
        subDirectories.add(directory);
    }

    // getters and setters

    static User getCurrentDirectory() {
        return currentDirectory;
    }

    static void setCurrentDirectory(User currentDirectory) {
        User.currentDirectory = currentDirectory;
    }

    static User getRootDirectory() {
        return rootDirectory;
    }

    static void setRootDirectory(User rootDirectory) {
        User.rootDirectory = rootDirectory;
    }

    // finding a User by its path
    Path getPath() {
        if (parent == null) {
            return Paths.get(name);
        } else {
            return parent.getPath().resolve(name);
        }
    }

    boolean isRoot() {
        return parent == null;
    }

    static User getByPath(User user, Path path) {
        // iterating through each subdirectory to check for a match
        List<User> subdirUsers = user.getSubDirectories();
        for (User dir : subdirUsers) {
            if (dir.isDirectory() && path.toString().endsWith(dir.name)) {
                return dir;
            }
        }
        return null;
    }

    long getSize() {
        // calculate byte size
        return content.getBytes().length;
    }

    boolean isFile() {
        // if a User is a file
        return !isDirectory;
    }
}

public class FileSys {
    public static void main(String[] args) {

        User rootNode = new User(null, "root", true);
        User.setCurrentDirectory(rootNode);
        User.setRootDirectory(rootNode);
        Scanner scan = new Scanner(System.in);
        try {
            while (true) {
                // get the user input as per the format needed and split it based on space
                System.out.print("prompt> ");
                String userInput = scan.nextLine();
                String[] splitting = userInput.split(" ");
                // take the first argument and ignore others
                switch (splitting[0]) {
                    /*
                     * handles cases for all commands:
                     * create, cat, rm, mkdir, rmdir, cd, ls, du, pwd, find, and exit
                     */

                    case "create":
                        if (splitting.length > 1) {
                            String newFileName = splitting[1];
                            if (fileOrDirectoryExists(newFileName)) {
                                System.out.println("ERROR: File or directory with the same name already exists.");
                            } else {
                                createFile(newFileName);
                                System.out.println("File created: " + newFileName);
                            }
                        } else {
                            System.out.println("ERROR: Invalid command");
                        }
                        break;

                    case "cat":
                        if (splitting.length > 1) {
                            String catFileName = splitting[1];
                            String fileContent = readFileContents(catFileName);
                            if (fileContent != null) {
                                System.out.println(fileContent);
                            } else {
                                System.out.println("ERROR: Cannot read file " + catFileName);
                            }
                        } else {
                            System.out.println("ERROR: Invalid command");
                        }
                        break;

                    case "rm":
                        if (splitting.length > 1) {
                            String rmFileName = splitting[1];
                            boolean fileRemoved = removeFile(rmFileName);
                            if (fileRemoved) {
                                System.out.println("File removed: " + rmFileName);
                            } else {
                                System.out.println("ERROR: Cannot remove file " + rmFileName);
                            }
                        } else {
                            System.out.println("ERROR: Invalid command");
                        }
                        break;

                    case "mkdir":
                        User newDirectory = mkdir(User.getCurrentDirectory(), splitting[1]);
                        if (newDirectory != null) {
                            System.out.println("Directory created: " + newDirectory.getName());
                        }
                        break;

                    case "rmdir":
                        if (splitting.length > 1) {
                            if (rmdir(splitting[1])) {
                                System.out.println("Directory removed: " + splitting[1]);
                            } else {
                                System.out.println("ERROR: Cannot remove directory " + splitting[1]);
                            }
                        } else {
                            System.out.println("ERROR: Invalid command");
                        }
                        break;

                    case "cd":
                        if (splitting.length > 1) {
                            cd(splitting[1]);
                        } else {
                            System.out.println("ERROR: Invalid command");
                        }
                        break;

                    case "ls":
                        ls();
                        break;

                    case "du":
                        long totalSize = calculateTotalSize(User.getCurrentDirectory());
                        System.out.println(totalSize);
                        break;

                    case "pwd":
                        pwd();
                        break;

                    case "find":
                        if (splitting.length < 2) {
                            System.out.println("ERROR: Please provide a target name for the find command.");
                        } else {
                            find(splitting[1]);
                        }
                        break;
                    // exit the FileSys program
                    case "exit":
                        System.out.println("Exiting the FileSys program.");
                        System.exit(0);
                        break;

                    default:
                        System.err
                                .println(
                                        "ERROR : Incorrect command or file/directory name not entered or other error");
                }
            }

        } finally {

            scan.close();
        }
    }
    /*
     * checking whether a file or directory with
     * a given name exists in the current directory by iterating through its 
     * subdirectories and return true if a match is found
     */

    private static boolean fileOrDirectoryExists(String name) {
        User currentDirectory = User.getCurrentDirectory();
        List<User> filesAndDirectories = currentDirectory.getSubDirectories();

        for (User fileOrDirectory : filesAndDirectories) {
            if (fileOrDirectory.getName().equals(name)) {
                return true;
            }
        }

        return false;
    }

    /*
     * if the command is create, verify if the valid argument exists
     * create a new file if no file or directory with the same name exists
     * prints an error otherwise
     */

    private static void createFile(String fileName) {
        User currentDirectory = User.getCurrentDirectory();
        User newFile = new User(currentDirectory, fileName, false); // isDirectory set here as false

        // read characters from keyboard input until a tilde (~) is entered
        StringBuilder contentBuilder = new StringBuilder();
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter the content of the file. Type '~' to finish.");

            while (true) {
                String line = scanner.nextLine();
                if (line.contains("~")) {
                    line = line.substring(0, line.indexOf("~"));
                    if (line.isEmpty()) {
                        break; 
                    }
                    contentBuilder.append(line);
                    break;
                }
                contentBuilder.append(line).append("\n");
            }
        } finally {
            // nothing 
        }

        // contentBuilder is not empty before creating a file
        if (contentBuilder.length() == 0) {
            System.out.println("ERROR: File not created. No content provided.");
            return;
        }

        newFile.setContent(contentBuilder.toString());

    }

    /*
     * if the command is cat, verify if the valid argument exists
     * read the contents of the specified file
     * and print the file contents if successful
     * or error message if unable to read the file
     */
    private static String readFileContents(String fileName) {
        User currentDirectory = User.getCurrentDirectory();
        List<User> files = currentDirectory.getSubDirectories(); // iterating through current directory's subdirectories

        for (User file : files) { // if a file with the matching name is found, return a string containing the file's content
            if (file.getName().equals(fileName) && file.isFile()) {                
                return "File content of " + fileName + "\n" + file.getContent();
            }
        }
        // else if file not found
        return null;
    }
    /*
     * if the command is rm, verify if the valid argument exists
     * remove the specific file
     * print the success message if file is removed
     * or error message if unable to remove the file
     */

    private static boolean removeFile(String fileName) {
        // iterating through subdirectories
        User currentDirectory = User.getCurrentDirectory();
        List<User> files = currentDirectory.getSubDirectories(); 

        Iterator<User> iterator = files.iterator();
        while (iterator.hasNext()) {
            User file = iterator.next();
            // if a file with the matching name is found, remove the file from the directory and return true
            if (file.getName().equals(fileName) && file.isFile()) {
                iterator.remove();
                return true;
            }
        }
        // else print an error message and return false
        System.out.println("ERROR: Unable to remove file " + fileName + ". File not found or is not a file.");
        return false;
    }

    /*
     * if the command is mkdir,
     * create a new directory with the specific name within the current directory
     * print the success message if created directory
     */

    // create new directory with the specifc name under the parent directory given
    private static User mkdir(User parent, String dirName) {

        if (directoryExists(parent, dirName)) {
            System.out.println("ERROR: Directory or file with the same name already exists.");
            return null;
        }

        User newDirectory = new User(parent, dirName, true);

        return newDirectory;
    }

    private static boolean directoryExists(User parent, String dirName) {

        List<User> subDirectories = parent.getSubDirectories();
        //  iterating list of subdirectories of the parent directory 
        // return true if a directory with the matching name is found which suggests that directory exists
        for (User entry : subDirectories) {
            if (entry.getName().equals(dirName)) {
                return true;
            }
        }
        return false;
    }
    /*
     * if the command is rmdir, verify if the valid argument exists
     * remove the specific directory
     * print the success message if directory is removed
     * or error message if unable to remove the directory or no argument provided
     */

    private static boolean rmdir(String dirName) {
        User currentDirectory = User.getCurrentDirectory();
        List<User> subDirectories = currentDirectory.getSubDirectories();

        // handle the case of an empty directory
        if (subDirectories == null || subDirectories.isEmpty()) {
            System.out.println("ERROR: Nothing inside the folder.");
            return false; // if no directories are present return false
        }

        // check for directory name
        Iterator<User> iterator = subDirectories.iterator();
        while (iterator.hasNext()) {
            User file = iterator.next();
            if (file.getName().equalsIgnoreCase(dirName) && file.isDirectory()) {
                iterator.remove();
                System.out.println("Deleted the directory");
                return true; // if directory is successfully removed return true
            }
        }

        System.out.println("ERROR: Directory '" + dirName + "' not found or not a directory");
        return false; // if directory is not found or not a directory return false
    }
    /*
     * if the command is cd,
     * change the current directory to one specified in the command line
     * if valid argument, then change or else invalid error message
     */

    private static void cd(String dirName) {
        User currentDirectory = User.getCurrentDirectory();
        /*
         * if dirName is "/",
         * changing current directory to be the root directory of the file system
         */
        if (dirName.equals("/")) {
            User.setCurrentDirectory(User.getRootDirectory());
            System.out.println("Current directory set to root: " + User.getCurrentDirectory().getName());
            return;
        }
        /*
         * if dirName is "..",
         * change the current directory to be the parent of the current directory of the
         * file system
         */

        if (dirName.equals("..")) {
            User parentDirectory = currentDirectory.getParent();
            if (parentDirectory != null) {
                User.setCurrentDirectory(parentDirectory);
                System.out.println("Current directory set to parent: " + User.getCurrentDirectory().getName());
            } else {
                System.out.println("ERROR: Already at the root directory.");
            }
            return;
        }

        // validating the resolved path using java.nio.file.Path for better path
        // navigation
        Path newPath = Paths.get(dirName);
        Path resolvedPath = currentDirectory.getPath().resolve(newPath).normalize();

        if (!resolvedPath.startsWith(User.getRootDirectory().getPath())) {
            System.out.println("ERROR: Invalid path specified.");
            return;
        }

        // if the specified directory is found, set the current directory to it
        User foundDirectory = User.getByPath(currentDirectory, resolvedPath);
        if (foundDirectory != null && foundDirectory.isDirectory()) {
            User.setCurrentDirectory(foundDirectory);
            System.out.println("Current directory set: " + User.getCurrentDirectory().getName());
        } else {
            // if the specified directory is not found or not a directory, print an error
            // message
            System.out.println("ERROR: Directory not found or not a directory");
        }
    }
    /*
     * if the command is ls,
     * print all the files and directories inside the current directory,
     * in alphabetical order and "*" after all directories
     */

    private static void ls() {
        User currentDirectory = User.getCurrentDirectory();
        List<User> fileList = currentDirectory.getSubDirectories();

        if (fileList == null || fileList.isEmpty()) {
            System.out.println("Empty directory");
            return;
        }

        // separating files and directories
        List<User> directories = new ArrayList<>();
        List<User> files = new ArrayList<>();

        for (User file : fileList) {
            if (file.isDirectory()) {
                directories.add(file);
            } else {
                files.add(file);
            }
        }

        // sorting the lists alphabetically
        directories.sort(Comparator.comparing(User::getName));
        files.sort(Comparator.comparing(User::getName));

        // printing directories with "(*)" after each and then printing files
        for (User directory : directories) {
            System.out.println(directory.getName() + " (*)");
        }
        for (User file : files) {
            System.out.println(file.getName());
        }
    }

    /*
     * if command is du,
     * finding and printing the total size (in bytes) of all the
     * files in this directory and all the files in all subdirectories
     */
    private static void du() {
        User currentDirectory = User.getCurrentDirectory();
        long totalSize = calculateTotalSize(currentDirectory);
        System.out.println(totalSize);
    }

    private static long calculateTotalSize(User directory) {
        long totalSize = 0;

        // looping through files and subdirectories
        for (User file : directory.getSubDirectories()) {
            if (file.isDirectory()) {
                // and if it is a directory, recursively calculate its size
                totalSize += calculateTotalSize(file);
            } else if (file.isFile()) {
                // and if it is a file, add its size to the total size
                totalSize += file.getSize();
            }
        }
        return totalSize;
    }

    /*
     * if command is pwd,
     * print the full directory path to the current directory,
     * starting from root
     */
    private static void pwd() {
        User currentDirectory = User.getCurrentDirectory();
        String fullPath = getFullPath(currentDirectory);
        System.out.println("Current directory path: " + fullPath);
    }

    private static String getFullPath(User directory) {
        // base case: if the directory is the root, return "/"
        if (directory.isRoot()) {
            return "/";
        }

        // inductive step: concatenate the directory name with the parent path
        String parentPath = getFullPath(directory.getParent());
        return parentPath.equals("/") ? parentPath + directory.getName() : parentPath + "/" + directory.getName();
    }

    /*
     * if command is find,
     * find all files or directories named "name" in the current directory or any
     * child
     * directory and print the full directory path of all such files or directories
     */
    private static void find(String targetName) {
        User currentDirectory = User.getCurrentDirectory();
        searchAndPrint(currentDirectory, targetName);
    }

    private static void searchAndPrint(User directory, String targetName) {
        // check if the current directory contains a file or directory with the target
        // name
        for (User file : directory.getSubDirectories()) {
            // printing the full path if a match is found
            if (file.getName().equals(targetName)) {
                System.out.println(getFullPath(file));
            }
            // recursively search in subdirectories
            if (file.isDirectory()) {
                searchAndPrint(file, targetName);
            }
        }
    }

}