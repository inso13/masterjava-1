package ru.javaops.masterjava.showusers;

import org.thymeleaf.context.WebContext;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.UserDao;
import ru.javaops.masterjava.persist.model.User;

import static ru.javaops.masterjava.showusers.ThymeleafListener.engine;

/**
 * Created by Inso on 24.03.2017.
 */
@WebServlet("/")
@MultipartConfig
public class UsersServlet extends HttpServlet {
    private UserDao userDao = DBIProvider.getDBI().onDemand(UserDao.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final WebContext webContext = new WebContext(req, resp, req.getServletContext(), req.getLocale());
        List<User> users = userDao.getWithLimit(20);
        req.setAttribute("users", users);
        engine.process("users", webContext, resp.getWriter());
    }
}
