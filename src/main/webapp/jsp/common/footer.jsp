<%-- Closing tags for main content structure from header.jsp --%>
    </div> <%-- End .container.page-content --%>
</main> <%-- End .main-content --%>

<footer class="main-footer">
    <div class="container">
        <p>&copy; <%= new java.text.SimpleDateFormat("yyyy").format(new java.util.Date()) %> Konnect Platform. All rights reserved.</p>
        <%-- Add other footer links if needed, e.g., Privacy Policy, Terms of Service --%>
         <%-- <p><a href="#">Privacy Policy</a> | <a href="#">Terms of Service</a></p> --%>
    </div>
</footer>

<%-- Include main JavaScript file --%>
<script src="<%= request.getContextPath() %>/js/script.js"></script>

<%-- Optional: Add page-specific JavaScript includes here if needed --%>
<%-- <script src="<%= request.getContextPath() %>/js/some-page-specific.js"></script> --%>

</body>
</html>
