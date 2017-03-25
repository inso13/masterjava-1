package ru.javaops.masterjava.export;

import org.thymeleaf.context.WebContext;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.AbstractDao;
import ru.javaops.masterjava.persist.dao.UserDao;
import ru.javaops.masterjava.persist.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.javaops.masterjava.export.ThymeleafListener.engine;

@WebServlet("/")
@MultipartConfig
public class UploadServlet extends HttpServlet {

    private final UserExport userExport = new UserExport();
    private static List<User> usersToImport = new ArrayList<>();
    private UserDao userDao = DBIProvider.getDBI().onDemand(UserDao.class);
    private static AtomicInteger successCount = new AtomicInteger(0);
    private static AtomicInteger failCount = new AtomicInteger(0);
    private static List<User> failUsers = new ArrayList<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final WebContext webContext = new WebContext(req, resp, req.getServletContext(), req.getLocale());
        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");
        if (action==null) {engine.process("export", webContext, resp.getWriter());}
        else if (action.equals("import"))
        {
            successCount.set(0);
            failCount.set(0);
            for (User user:usersToImport)
            {
                    try {
                        userDao.insert(user);
                        successCount.incrementAndGet();
                    } catch (Exception e) {

                        failCount.incrementAndGet();
                        failUsers.add(user);
                    }
            }
            webContext.setVariable("sucessCount", successCount);
            webContext.setVariable("failCount", failCount);
            webContext.setVariable("failUsers", failUsers);
            engine.process("importResult", webContext, resp.getWriter());
        }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final WebContext webContext = new WebContext(req, resp, req.getServletContext(), req.getLocale());

        try {
//            http://docs.oracle.com/javaee/6/tutorial/doc/glraq.html
            Part filePart = req.getPart("fileToUpload");
            try (InputStream is = filePart.getInputStream()) {
                List<User> users = userExport.process(is);
                usersToImport.clear();
                failUsers.clear();
                usersToImport.addAll(users);
                webContext.setVariable("users", users);
                engine.process("result", webContext, resp.getWriter());
            }
        } catch (Exception e) {
            webContext.setVariable("exception", e);
            engine.process("exception", webContext, resp.getWriter());
        }
    }
}
