
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import redis.clients.jedis.Jedis;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

@WebServlet(urlPatterns = "/GetNotepadById")
public class GetNotepadById extends HttpServlet {
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://180.76.242.48/LinuxExam";
    static final String USER = "root";
    static final String PASS = "7272&@&@WAsd";
    static final String SQL_QURERY_NOTEPAD_BY_ID = "SELECT id, notepadContent FROM t_notepad WHERE id=?";
    static final String REDIS_URL = "127.0.0.1";

    static Connection conn = null;
    static Jedis jedis = null;

    public void init() {
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            jedis = new Jedis(REDIS_URL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        try {
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        getServletContext().log(request.getParameter("id"));

        String json = jedis.get(request.getParameter("id"));

        if (json == null) {
            Notepad note = getNotepad(Integer.parseInt(request.getParameter("id")));

            Gson gson = new Gson();
            json = gson.toJson(note, new TypeToken<Notepad>() {
            }.getType());

            jedis.set(request.getParameter("id"), json);
            out.println(json);

        } else {
            out.println(json);
        }
        out.flush();
        out.close();
    }

    public Notepad getNotepad(int id) {
        Notepad note = new Notepad();
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(SQL_QURERY_NOTEPAD_BY_ID);
            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                note.id = rs.getInt("id");
                note.notepadContent = rs.getString("notepadContent");
            }

            rs.close();
            stmt.close();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }

        return note;

    }

    class Notepad{
        int id;
        String notepadContent;
        String notepadTime;

        @Override
        public String toString() {
            return "Notepad [id=" + id + ", notepadContent=" + notepadContent + ", notepadTime=" + notepadTime + "]";
        }
    }

}