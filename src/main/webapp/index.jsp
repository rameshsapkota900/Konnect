<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("pageTitle", "Welcome"); %> <%-- Set specific page title --%>
<%@ include file="/jsp/common/header.jsp" %> <%-- Include header relative to webapp root --%>

<style>
    /* Add specific styles for the landing page */
    .landing-hero {
        text-align: center;
        padding: 4rem 1rem;
        background-color: rgba(0, 0, 0, 0.2); /* Slight overlay */
        border-radius: 8px;
        margin: 2rem 0;
    }
    .landing-hero h1 {
        font-size: 2.8rem;
        margin-bottom: 1rem;
        color: var(--primary-color);
        font-weight: 700;
    }
    .landing-hero p {
        font-size: 1.2rem;
        color: var(--text-color); /* Use main text color */
        margin-bottom: 2.5rem;
        max-width: 700px;
        margin-left: auto;
        margin-right: auto;
        line-height: 1.7;
    }
    .landing-actions .btn {
        font-size: 1.1rem;
        padding: 1rem 2.5rem; /* Larger buttons */
        margin: 0 0.8rem;
    }

     .features-section {
         padding: 3rem 0;
         display: grid;
         grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
         gap: 2rem;
         text-align: center;
     }
     .feature-item {
         background-color: var(--card-bg);
         padding: 2rem;
         border-radius: 8px;
         border: 1px solid var(--border-color);
     }
      .feature-item h3 {
          color: var(--primary-color);
          margin-bottom: 1rem;
      }
       .feature-item p {
           color: var(--text-color-muted);
           font-size: 0.95rem;
       }

</style>

<%-- Landing Page Content --%>
<div class="landing-page">

    <%-- Hero Section --%>
    <section class="landing-hero">
        <h1>Welcome to Konnect</h1>
        <p>The premier platform designed to seamlessly connect innovative businesses with talented content creators. Find your perfect match, manage collaborations, and grow together.</p>
        <div class="landing-actions">
            <a href="<%= contextPath %>/login" class="btn btn-primary">Login</a>
            <a href="<%= contextPath %>/register" class="btn btn-secondary">Register</a>
        </div>
    </section>

     <%-- Features Section (Optional) --%>
     <section class="features-section">
         <div class="feature-item">
             <h3>For Creators</h3>
             <p>Discover exciting brand campaigns, showcase your media kit, manage applications, and collaborate directly with businesses looking for your unique voice.</p>
              <a href="<%= contextPath %>/register?role=creator" class="btn btn-secondary btn-sm" style="margin-top:1rem;">Register as Creator</a>
         </div>
         <div class="feature-item">
             <h3>For Businesses</h3>
             <p>Create targeted campaigns, search and filter a diverse pool of creators, manage applications, send invites, and streamline your influencer marketing efforts.</p>
              <a href="<%= contextPath %>/register?role=business" class="btn btn-secondary btn-sm" style="margin-top:1rem;">Register as Business</a>
         </div>
         <div class="feature-item">
             <h3>Simple & Secure</h3>
             <p>Enjoy a user-friendly interface, direct messaging capabilities, and robust admin oversight, all built on reliable technology for a secure experience.</p>
              <a href="<%= contextPath %>/login" class="btn btn-secondary btn-sm" style="margin-top:1rem;">Get Started</a>
         </div>
     </section>

</div>

<%@ include file="/jsp/common/footer.jsp" %> <%-- Include footer relative to webapp root --%>