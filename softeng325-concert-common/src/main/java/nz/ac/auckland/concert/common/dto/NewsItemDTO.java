package nz.ac.auckland.concert.common.dto;

import nz.ac.auckland.concert.common.jaxb.LocalDateTimeAdapter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDateTime;

@XmlRootElement(name="NewsItem")
public class NewsItemDTO {

    @XmlAttribute(name="id")
    private Long id;

    @XmlElement(name="timestamp")
    @XmlJavaTypeAdapter(value = LocalDateTimeAdapter.class)
    private LocalDateTime timeStamp;

    @XmlElement(name="content")
    private String content;


    public NewsItemDTO(){}

    public NewsItemDTO(Long id, LocalDateTime timeStamp, String content){
        this.id = id;
        this.timeStamp = timeStamp;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public String getContent() {
        return content;
    }
}
