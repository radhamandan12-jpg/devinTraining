package com.example.claudedemo.tools;

import java.util.List;
import java.util.Map;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DbTools {

    private final JdbcTemplate jdbc;

    public DbTools(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Tool(description = "Save a note to the MySQL database")
    public String saveNote(@ToolParam(description = "The note text to store") String content) {
        jdbc.update("INSERT INTO notes (content) VALUES (?)", content);
        return "Saved note: " + content;
    }

    @Tool(description = "List all notes stored in the MySQL database")
    public List<Map<String, Object>> listNotes() {
        return jdbc.queryForList("SELECT id, content, created_at FROM notes ORDER BY id DESC LIMIT 20");
    }

    @Tool(description = "Search notes in the MySQL database containing a keyword")
    public List<Map<String, Object>> searchNotes(
            @ToolParam(description = "Keyword to search for in note content") String keyword) {
        return jdbc.queryForList(
                "SELECT id, content, created_at FROM notes WHERE content LIKE ? ORDER BY id DESC LIMIT 20",
                "%" + keyword + "%");
    }
}
