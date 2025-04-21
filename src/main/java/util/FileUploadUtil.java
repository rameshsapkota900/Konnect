package util;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class FileUploadUtil {

    // Base directory for uploads RELATIVE to the WebContent folder in your SOURCE project.
    // When deployed, Tomcat typically copies WebContent content to the deployment directory.
    // CONSIDER USING AN ABSOLUTE PATH OUTSIDE THE WEBAPP FOR PRODUCTION RELIABILITY.
    private static final String UPLOAD_DIR_RELATIVE_BASE = "uploads";

    // Allowed file extensions (example - customize as needed)
    // Use lowercase for comparison
    private static final String[] ALLOWED_MEDIA_KIT_EXTENSIONS = {".pdf", ".jpg", ".jpeg", ".png"};
    private static final String[] ALLOWED_IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp"};
    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10 MB example limit

    /**
     * Saves an uploaded file from a Part to a specified sub-directory within the base upload path.
     * Validates file extension and size.
     *
     * @param request       The HttpServletRequest.
     * @param part          The Part representing the uploaded file.
     * @param subDirectory  The sub-directory (e.g., "media_kits", "product_images").
     * @return The relative path to the saved file (e.g., "uploads/media_kits/unique_filename.pdf") or null on failure/validation error.
     * @throws IOException if an I/O error occurs during saving.
     * @throws ServletException if servlet related error occurs (less likely here).
     */
    public static String saveFile(HttpServletRequest request, Part part, String subDirectory) throws IOException, ServletException {
        if (part == null || part.getSize() == 0) {
            System.out.println("No file part received or file is empty.");
            request.setAttribute("fileError", "No file selected or file is empty.");
            return null; // No file uploaded or empty file
        }

        // --- File Validation ---
        String submittedFileName = Paths.get(part.getSubmittedFileName()).getFileName().toString(); // Sanitize filename
        if (submittedFileName.isEmpty()) {
            System.out.println("Submitted file name is empty.");
             request.setAttribute("fileError", "Invalid file name.");
            return null; // No filename submitted
        }

        // 1. Validate Size
        if (part.getSize() > MAX_FILE_SIZE_BYTES) {
             System.err.println("File upload failed: File size (" + part.getSize() + " bytes) exceeds limit ("+ MAX_FILE_SIZE_BYTES +" bytes). Filename: " + submittedFileName);
             request.setAttribute("fileError", "File is too large. Maximum size is " + (MAX_FILE_SIZE_BYTES / 1024 / 1024) + " MB.");
             return null;
        }

        // 2. Validate Extension
        String fileExtension = getFileExtension(submittedFileName);
        String[] allowedExtensions;
        if ("media_kits".equalsIgnoreCase(subDirectory)) {
             allowedExtensions = ALLOWED_MEDIA_KIT_EXTENSIONS;
        } else if ("product_images".equalsIgnoreCase(subDirectory)) {
             allowedExtensions = ALLOWED_IMAGE_EXTENSIONS;
        } else {
             System.err.println("File upload failed: Invalid upload sub-directory specified: " + subDirectory);
              request.setAttribute("fileError", "Invalid upload category.");
             return null; // Or use a default set of extensions?
        }

        if (!isExtensionAllowed(fileExtension, allowedExtensions)) {
            System.err.println("File upload failed: Disallowed file extension '" + fileExtension + "'. Filename: " + submittedFileName);
             request.setAttribute("fileError", "Invalid file type. Allowed types: " + String.join(", ", allowedExtensions));
            return null;
        }
        // --- End Validation ---


        // Generate a unique filename to avoid collisions and potential path traversal issues
        String uniqueID = UUID.randomUUID().toString();
        String uniqueFileName = uniqueID + (fileExtension != null ? fileExtension : ""); // Append original extension

        // Determine the absolute path for saving
        // IMPORTANT: getRealPath("") points to the root of the deployed web application.
        String applicationPath = request.getServletContext().getRealPath("");
        if (applicationPath == null) {
            // This can happen in some server configurations or if the context is not fully initialized.
            // Fallback or use a pre-configured absolute path.
             System.err.println("FATAL ERROR: Could not determine real path of the application. Uploads will likely fail.");
              request.setAttribute("fileError", "Server configuration error preventing file uploads.");
              // Consider throwing an exception or returning null
             return null;
        }

        Path uploadDirPath = Paths.get(applicationPath, UPLOAD_DIR_RELATIVE_BASE, subDirectory);
        Path destinationFilePath = uploadDirPath.resolve(uniqueFileName);

        System.out.println("Attempting to save file to absolute path: " + destinationFilePath);

        // Create the directories if they don't exist
        try {
            Files.createDirectories(uploadDirPath);
             System.out.println("Ensured upload directory exists: " + uploadDirPath);
        } catch (IOException e) {
            System.err.println("Could not create upload directory: " + uploadDirPath + " - Error: " + e.getMessage());
             request.setAttribute("fileError", "Could not create storage directory on server.");
            return null;
        }

        // Save the file using try-with-resources for the InputStream
        try (InputStream fileContent = part.getInputStream()) {
            Files.copy(fileContent, destinationFilePath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File saved successfully: " + destinationFilePath);

            // Return the RELATIVE path for database storage and URL generation
            // Use forward slashes for web paths, regardless of OS.
            String relativePath = UPLOAD_DIR_RELATIVE_BASE + "/" + subDirectory + "/" + uniqueFileName;
            System.out.println("Returning relative path: " + relativePath);
            return relativePath;

        } catch (IOException e) {
            System.err.println("Error saving uploaded file '" + submittedFileName + "' to '" + destinationFilePath + "': " + e.getMessage());
            e.printStackTrace(); // Print stack trace for detailed debugging
             request.setAttribute("fileError", "An error occurred while saving the file.");
            // Attempt to clean up partially created file if saving failed mid-way
            try {
                Files.deleteIfExists(destinationFilePath);
            } catch (IOException cleanupEx) {
                System.err.println("Error cleaning up partially saved file: " + cleanupEx.getMessage());
            }
            return null;
        }
    }

    /**
     * Deletes a file specified by its relative path within the application's upload directory.
     *
     * @param request       The HttpServletRequest to determine the application path.
     * @param relativePath  The relative path stored in the database (e.g., "uploads/media_kits/unique_filename.pdf").
     * @return true if the file was successfully deleted or did not exist, false otherwise.
     */
    public static boolean deleteFile(HttpServletRequest request, String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
             System.out.println("Delete file request ignored: Relative path is null or empty.");
            return true; // Nothing to delete
        }

        // Basic sanity check to prevent attempts to delete files outside the intended upload structure
        if (!relativePath.startsWith(UPLOAD_DIR_RELATIVE_BASE + "/") && !relativePath.startsWith(UPLOAD_DIR_RELATIVE_BASE + "\\")) {
             System.err.println("File deletion blocked: Path does not start with the expected upload base directory. Path: " + relativePath);
             return false;
        }

        String applicationPath = request.getServletContext().getRealPath("");
         if (applicationPath == null) {
             System.err.println("ERROR: Cannot delete file. Could not determine real path of the application.");
             return false;
         }

        // Construct the absolute path carefully
        Path absolutePath = Paths.get(applicationPath, relativePath);

        System.out.println("Attempting to delete file at absolute path: " + absolutePath);

        try {
            boolean deleted = Files.deleteIfExists(absolutePath);
            if (deleted) {
                System.out.println("File deleted successfully: " + absolutePath);
            } else {
                System.out.println("File not found for deletion (or already deleted): " + absolutePath);
            }
            return true; // Return true even if file wasn't found, as the goal is achieved (it's gone)
        } catch (IOException e) {
            System.err.println("Error deleting file '" + absolutePath + "': " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (SecurityException e) {
             System.err.println("Security error deleting file '" + absolutePath + "': Check file permissions. " + e.getMessage());
             return false;
        }
    }

    /**
     * Extracts the file extension (including the dot) from a filename.
     * @param filename The full filename (e.g., "document.pdf").
     * @return The extension in lowercase (e.g., ".pdf") or null if no extension.
     */
    private static String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex).toLowerCase();
        }
        return null; // No extension found
    }

    /**
     * Checks if a given file extension is in the list of allowed extensions.
     * @param extension The extension to check (e.g., ".pdf").
     * @param allowedExtensions An array of allowed extensions (e.g., {".pdf", ".jpg"}).
     * @return true if allowed, false otherwise.
     */
    private static boolean isExtensionAllowed(String extension, String[] allowedExtensions) {
        if (extension == null || allowedExtensions == null) {
            return false;
        }
        for (String allowed : allowedExtensions) {
            if (extension.equals(allowed)) {
                return true;
            }
        }
        return false;
    }
}