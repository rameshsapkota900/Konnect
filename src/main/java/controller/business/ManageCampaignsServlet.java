package controller.business;

import dao.CampaignDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import model.Campaign;
import model.User;
import util.FileUploadUtil; // For handling product image uploads

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

@WebServlet("/business/campaigns")
@MultipartConfig( // Enable for image uploads
    fileSizeThreshold = 1024 * 1024 * 1,  // 1 MB
    maxFileSize = 1024 * 1024 * 10, // 10 MB
    maxRequestSize = 1024 * 1024 * 15 // 15 MB
)
public class ManageCampaignsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private CampaignDAO campaignDAO;

    public void init() {
        campaignDAO = new CampaignDAO();
        System.out.println("ManageCampaignsServlet Initialized");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("ManageCampaignsServlet: doGet called");
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("user") : null;

        // --- Authorization Check ---
        if (loggedInUser == null || !loggedInUser.isBusiness()) {
            response.sendRedirect(request.getContextPath() + "/login?error=auth");
            return;
        }
        if (loggedInUser.isBanned()) {
            session.invalidate();
            response.sendRedirect(request.getContextPath() + "/login?error=banned");
            return;
        }
        // --- End Authorization Check ---

        String action = request.getParameter("action");
        String campaignIdParam = request.getParameter("id");

        try {
            int businessId = loggedInUser.getUserId();

             // Check for messages from POST redirects
            if ("created".equals(request.getParameter("success"))) request.setAttribute("successMessage", "Campaign created successfully!");
            if ("updated".equals(request.getParameter("success"))) request.setAttribute("successMessage", "Campaign updated successfully!");
            if ("deleted".equals(request.getParameter("success"))) request.setAttribute("successMessage", "Campaign deleted successfully.");
            if ("notFound".equals(request.getParameter("error"))) request.setAttribute("errorMessage", "Campaign not found or you do not have permission to edit it.");
            if ("invalidData".equals(request.getParameter("error"))) request.setAttribute("errorMessage", "Invalid data submitted for campaign.");
             if ("serverError".equals(request.getParameter("error"))) request.setAttribute("errorMessage", "A server error occurred while managing campaigns.");


            if ("create".equals(action)) {
                // Show the 'create campaign' form
                 System.out.println("Action: create (show form)");
                 request.setAttribute("campaign", new Campaign()); // Send empty campaign object
                 request.setAttribute("formAction", "create"); // Indicate form is for creation
                 RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/business/campaign_form.jsp");
                 dispatcher.forward(request, response);

            } else if ("edit".equals(action) && campaignIdParam != null) {
                // Show the 'edit campaign' form
                 System.out.println("Action: edit (show form), Campaign ID: " + campaignIdParam);
                 int campaignId = Integer.parseInt(campaignIdParam);
                 Campaign campaign = campaignDAO.getCampaignById(campaignId, false); // Don't need business details here

                 // Verify ownership and existence
                 if (campaign != null && campaign.getBusinessUserId() == businessId) {
                     request.setAttribute("campaign", campaign);
                     request.setAttribute("formAction", "edit"); // Indicate form is for editing
                     RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/business/campaign_form.jsp");
                     dispatcher.forward(request, response);
                 } else {
                      System.err.println("Edit failed: Campaign " + campaignId + " not found or not owned by Business " + businessId);
                     response.sendRedirect(request.getContextPath() + "/business/campaigns?error=notFound");
                 }
            } else {
                 // Default action: List the business's campaigns
                 System.out.println("Action: list campaigns for Business ID: " + businessId);
                 List<Campaign> campaigns = campaignDAO.getCampaignsByBusinessId(businessId);
                 request.setAttribute("campaigns", campaigns);
                 RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/business/manage_campaigns.jsp");
                 dispatcher.forward(request, response);
            }
        } catch (NumberFormatException e) {
             System.err.println("Invalid Campaign ID format: " + campaignIdParam);
             response.sendRedirect(request.getContextPath() + "/business/campaigns?error=invalidId");
        } catch (Exception e) {
            System.err.println("Error in ManageCampaignsServlet doGet: " + e.getMessage());
            e.printStackTrace();
             request.setAttribute("errorMessage", "An error occurred while loading campaign data.");
             RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/business/manage_campaigns.jsp"); // Show list view with error
             dispatcher.forward(request, response);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("ManageCampaignsServlet: doPost called");
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("user") : null;

        // --- Authorization Check ---
        if (loggedInUser == null || !loggedInUser.isBusiness()) {
            response.sendRedirect(request.getContextPath() + "/login?error=auth");
            return;
        }
        if (loggedInUser.isBanned()) {
            session.invalidate();
            response.sendRedirect(request.getContextPath() + "/login?error=banned");
            return;
        }
        // --- End Authorization Check ---

        String action = request.getParameter("action");
        int businessId = loggedInUser.getUserId();
        String redirectUrl = request.getContextPath() + "/business/campaigns"; // Default redirect
        String errorMessage = null;
        String successParam = null; // To add to redirect URL

        try {
            if ("create".equals(action)) {
                System.out.println("Action: process create campaign");
                Campaign newCampaign = extractCampaignFromRequest(request, businessId);
                int newCampaignId = campaignDAO.createCampaign(newCampaign);
                if (newCampaignId > 0) {
                    successParam = "created";
                } else {
                     errorMessage = (String) request.getAttribute("validationError"); // Get validation error if set
                     if (errorMessage == null) errorMessage = "Failed to create campaign due to a server error.";
                     // Forward back to form with error and populated data
                      request.setAttribute("errorMessage", errorMessage);
                      request.setAttribute("campaign", newCampaign); // Send back the data user entered
                      request.setAttribute("formAction", "create");
                      RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/business/campaign_form.jsp");
                      dispatcher.forward(request, response);
                      return; // Stop further processing
                }

            } else if ("edit".equals(action)) {
                 System.out.println("Action: process edit campaign");
                 String campaignIdParam = request.getParameter("campaignId"); // Hidden field in edit form
                 if (campaignIdParam == null) throw new IllegalArgumentException("Campaign ID missing for edit.");

                 int campaignId = Integer.parseInt(campaignIdParam);
                 Campaign existingCampaign = campaignDAO.getCampaignById(campaignId, false); // Get existing data

                 // Verify ownership
                 if (existingCampaign == null || existingCampaign.getBusinessUserId() != businessId) {
                     throw new SecurityException("Permission denied to edit campaign ID: " + campaignId);
                 }

                Campaign updatedCampaign = extractCampaignFromRequest(request, businessId);
                updatedCampaign.setCampaignId(campaignId); // Set the ID for update

                 // Handle image update logic (keep old if no new one uploaded)
                 if (updatedCampaign.getProductImagePath() == null && existingCampaign.getProductImagePath() != null) {
                     updatedCampaign.setProductImagePath(existingCampaign.getProductImagePath());
                 }

                 boolean updated = campaignDAO.updateCampaign(updatedCampaign);
                 if (updated) {
                     successParam = "updated";
                 } else {
                      errorMessage = (String) request.getAttribute("validationError");
                      if (errorMessage == null) errorMessage = "Failed to update campaign. Please try again.";
                      // Forward back to form with error
                       request.setAttribute("errorMessage", errorMessage);
                       request.setAttribute("campaign", updatedCampaign); // Send back attempted update data
                       request.setAttribute("formAction", "edit");
                       RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/business/campaign_form.jsp");
                       dispatcher.forward(request, response);
                       return;
                 }

            } else if ("delete".equals(action)) {
                System.out.println("Action: process delete campaign");
                String campaignIdParam = request.getParameter("campaignId");
                 if (campaignIdParam == null) throw new IllegalArgumentException("Campaign ID missing for delete.");

                 int campaignId = Integer.parseInt(campaignIdParam);
                 // DAO method verifies ownership internally
                 boolean deleted = campaignDAO.deleteCampaign(campaignId, businessId);
                 if (deleted) {
                     successParam = "deleted";
                 } else {
                     errorMessage = "Failed to delete campaign. It might not exist or you don't have permission.";
                     redirectUrl += "?error=deleteFailed"; // Use specific error param
                 }
            } else {
                 System.err.println("Invalid POST action received: " + action);
                 redirectUrl += "?error=invalidAction";
            }

        } catch (IllegalArgumentException | SecurityException e) {
             System.err.println("Error processing campaign POST request: " + e.getMessage());
             // If validation error happened in extractCampaignFromRequest, it might be in request attributes
             errorMessage = (String) request.getAttribute("validationError");
             if (errorMessage == null) errorMessage = "Invalid data submitted: " + e.getMessage();
             redirectUrl += "?error=invalidData";
             // Consider forwarding back to the form for create/edit errors instead of just redirecting
             // (Requires more state management or passing error details)

        } catch (Exception e) {
             System.err.println("Unexpected error processing campaign POST: " + e.getMessage());
             e.printStackTrace();
             redirectUrl += "?error=serverError";
        }

        // Redirect after successful action or simple errors
         if (successParam != null) {
             redirectUrl += "?success=" + successParam;
         } else if (errorMessage != null && !response.isCommitted()) { // Add error if not forwarded already
             // Avoid adding generic error params if already forwarded with specific error
             // We might have already redirected with specifics error params in catch blocks
             // redirectUrl += "?error=failed"; // Generic failure param
         }

         if (!response.isCommitted()) { // Only redirect if not already forwarded
              System.out.println("Redirecting after POST action to: " + redirectUrl);
              response.sendRedirect(redirectUrl);
         }
    }


    /**
     * Extracts campaign data from the request, performs validation, and handles file upload.
     * Sets a "validationError" request attribute if validation fails.
     * @param request HttpServletRequest
     * @param businessId The ID of the owning business user
     * @return A populated Campaign object, or null if critical errors occur.
     * @throws IOException
     * @throws ServletException
     */
    private Campaign extractCampaignFromRequest(HttpServletRequest request, int businessId) throws IOException, ServletException {
        Campaign campaign = new Campaign();
        campaign.setBusinessUserId(businessId);
        String validationError = null;

        try {
            campaign.setTitle(request.getParameter("title"));
            campaign.setDescription(request.getParameter("description"));
            campaign.setRequirements(request.getParameter("requirements"));
            campaign.setStatus(request.getParameter("status"));

            // Validate required fields
            if (campaign.getTitle() == null || campaign.getTitle().trim().isEmpty()) validationError = "Campaign title cannot be empty.";
            else if (campaign.getDescription() == null || campaign.getDescription().trim().isEmpty()) validationError = "Campaign description cannot be empty.";
            else if (campaign.getStatus() == null || campaign.getStatus().trim().isEmpty()) validationError = "Campaign status must be selected.";
            // Add more validation (length limits, status value check)

            // Parse budget
            String budgetStr = request.getParameter("budget");
            if (budgetStr != null && !budgetStr.trim().isEmpty()) {
                try {
                    campaign.setBudget(new BigDecimal(budgetStr.trim()));
                    if (campaign.getBudget().compareTo(BigDecimal.ZERO) < 0) {
                         validationError = "Budget cannot be negative.";
                    }
                } catch (NumberFormatException e) {
                     validationError = "Invalid budget format. Please enter a number (e.g., 500.00).";
                }
            } else {
                 campaign.setBudget(null); // Allow null budget
            }

            // Parse dates
             String startDateStr = request.getParameter("startDate");
             String endDateStr = request.getParameter("endDate");
             try {
                 if (startDateStr != null && !startDateStr.trim().isEmpty()) {
                     campaign.setStartDate(Date.valueOf(startDateStr.trim()));
                 } else {
                      campaign.setStartDate(null); // Allow null start date
                 }
                 if (endDateStr != null && !endDateStr.trim().isEmpty()) {
                     campaign.setEndDate(Date.valueOf(endDateStr.trim()));
                 } else {
                      campaign.setEndDate(null); // Allow null end date
                 }
                 // Optional: Validate end date is after start date
                 if (campaign.getStartDate() != null && campaign.getEndDate() != null && campaign.getEndDate().before(campaign.getStartDate())) {
                      validationError = "End date cannot be before the start date.";
                 }
             } catch (IllegalArgumentException e) {
                  validationError = "Invalid date format. Please use YYYY-MM-DD.";
             }


            // Handle File Upload (Product Image)
            Part filePart = request.getPart("productImageFile"); // Matches name in form
             String uploadedFilePath = null;
             String existingImagePath = request.getParameter("existingImagePath"); // Get path if editing

             if (filePart != null && filePart.getSize() > 0) {
                 System.out.println("Product image file received: " + filePart.getSubmittedFileName());
                 uploadedFilePath = FileUploadUtil.saveFile(request, filePart, "product_images");
                 if (uploadedFilePath != null) {
                     System.out.println("New product image saved: " + uploadedFilePath);
                     campaign.setProductImagePath(uploadedFilePath);
                      // Optionally delete old file if replacing during edit (handled by saveFile overwrite or explicit delete)
                      if (existingImagePath != null && !existingImagePath.isEmpty() && !existingImagePath.equals(uploadedFilePath)) {
                          FileUploadUtil.deleteFile(request, existingImagePath);
                      }
                 } else {
                      // Upload failed, keep existing path if available, set validation error
                      campaign.setProductImagePath(existingImagePath); // Keep old path if editing
                      validationError = (String) request.getAttribute("fileError");
                      if (validationError == null) validationError = "Failed to upload product image.";
                       System.err.println("Product image upload failed.");
                 }
             } else {
                  // No new file, keep existing path if editing
                  campaign.setProductImagePath(existingImagePath);
                   System.out.println("No new product image uploaded. Keeping existing path: " + existingImagePath);
             }


        } catch (Exception e) {
             // Catch unexpected errors during extraction
             System.err.println("Error extracting campaign data from request: " + e.getMessage());
             validationError = "An unexpected error occurred while processing campaign data.";
             e.printStackTrace();
        }

        if (validationError != null) {
            request.setAttribute("validationError", validationError);
             System.err.println("Validation Error during campaign extraction: " + validationError);
             // Return the partially populated campaign object so the form can be refilled
        }

        return campaign; // Return the campaign object, even if validation failed (so form can be repopulated)
    }

}