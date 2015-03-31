package com.dfaenterprises.samesies;

import com.dfaenterprises.samesies.model.CommunityUser;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Ari Weiland
 */
public class CommunitiesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        String param = req.getParameter("community_user_id");
        if (param == null) {
            resp.sendError(400, "Bad Request: Must specify a community_user_id");
        } else {
            long cuid = Long.parseLong(param);
            try {
                CommunityUser cu = new CommunityUser(ds.get(KeyFactory.createKey("CommunityUser", cuid)));
                cu.setIsValidated(true);
                EntityUtils.put(ds, cu);
                resp.sendRedirect("http://samesies.org/joined.html");
            } catch (EntityNotFoundException e) {
                resp.sendError(404, "CommunityUser " + cuid + " Not Found");
            }
        }
    }
}
