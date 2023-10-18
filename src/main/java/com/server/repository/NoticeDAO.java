package com.server.repository;

import com.server.mapper.HomeMapper;
import com.server.model.NoticeDTO;
import com.server.service.HomeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@Slf4j
public class NoticeDAO {

    private final HomeMapper homeMapper;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public NoticeDAO(HomeMapper homeMapper, JdbcTemplate jdbcTemplate) {
        this.homeMapper = homeMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    /* 공지사항 리스트 */
    public List<NoticeDTO> getNotice(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        log.info("offset : {}", offset);

        String sql = "SELECT * FROM NOTICE LIMIT "+offset+", "+pageSize;
        return jdbcTemplate.query(sql, (resultSet, rowNum) -> noticeEntity(resultSet));
    }
    private NoticeDTO noticeEntity(ResultSet resultSet) throws SQLException {
        NoticeDTO entity = new NoticeDTO();
        entity.setId(resultSet.getInt("NOTICE_SEQ"));
        entity.setTitle(resultSet.getString("NOTICE_TITLE"));
        entity.setDate(resultSet.getString("NOTICE_DATE"));
        entity.setBody(resultSet.getString("NOTICE_CONTENT"));
        // Set other properties as needed
        log.info("entity : {}", entity);

        return entity;
    }

    /* HOME 화면 공지사항 리스트 [가장 최신 3개] */
    public List<NoticeDTO> getHomeNotice() {
        String sql = "SELECT * FROM NOTICE ORDER BY NOTICE_DATE DESC LIMIT 3";
        return jdbcTemplate.query(sql, (resultSet, rowNum) -> homeNoticeEntity(resultSet));
    }
    private NoticeDTO homeNoticeEntity(ResultSet resultSet) throws SQLException {
        NoticeDTO entity = new NoticeDTO();
        entity.setId(resultSet.getInt("NOTICE_SEQ"));
        entity.setTitle(resultSet.getString("NOTICE_TITLE"));
        entity.setDate(resultSet.getString("NOTICE_DATE"));
        // Set other properties as needed
        log.info("entity : {}", entity);

        return entity;
    }
}