package com.dfaenterprises.samesies;

import com.dfaenterprises.samesies.model.User;
import com.google.appengine.api.datastore.DatastoreService;
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
public class ActivationServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DatastoreService ds = DS.getDS();
        String param = req.getParameter("user_id");
        if (param == null) {
            resp.sendError(400, "Bad Request: Must specify a user_id");
        } else {
            long uid = Long.parseLong(param);
            try {
                User user = new User(ds.get(KeyFactory.createKey("User", uid)));
                user.setStatus(User.Status.ACTIVATED);
                DS.put(ds, user);
                resp.sendRedirect("http://samesies.org/activated.html");
            } catch (EntityNotFoundException e) {
                resp.sendError(404, "User " + uid + " Not Found");
            }
        }
    }
}
