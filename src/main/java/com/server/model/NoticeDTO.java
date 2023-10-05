package com.server.model;

import lombok.Data;

@Data
public class NoticeDTO {

    private int noticeSeq;
    private String strNoticeTitle;
    private String strNoticeDate;
    private String strNoticeContent;
}
